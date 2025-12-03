#!/usr/bin/env python3

import argparse
import math
import os
import statistics
import sys
import time
from array import array


def human_bytes(n: int) -> str:
    units = ["B", "KB", "MB", "GB"]
    i = 0
    x = float(n)
    while x >= 1024.0 and i < len(units) - 1:
        x /= 1024.0
        i += 1
    return f"{x:.1f} {units[i]}"


def generate_sizes(min_kb: int, max_bytes: int) -> list[int]:
    sizes = []
    min_bytes = min_kb * 1024
    # Use powers of two to sweep cache sizes cleanly
    pow_min = int(math.log2(max(1024, min_bytes)))
    pow_max = int(math.log2(max(1024, max_bytes)))
    for p in range(pow_min, pow_max + 1):
        sizes.append(1 << p)
    return sizes


def measure_stride_reads_array(num_elements: int, stride_elems: int, min_accesses: int, warmup: bool = True) -> float:
    # Allocate contiguous unsigned 64-bit array
    data = array("Q", [0]) * num_elements

    # Warm-up to populate caches/TLB
    if warmup:
        s = 0
        for i in range(0, num_elements, stride_elems):
            s ^= data[i]

    accesses_per_loop = max(1, (num_elements + stride_elems - 1) // stride_elems)
    loops = max(1, (min_accesses + accesses_per_loop - 1) // accesses_per_loop)

    start = time.perf_counter_ns()
    s = 0
    for _ in range(loops):
        for i in range(0, num_elements, stride_elems):
            s ^= data[i]
    end = time.perf_counter_ns()

    # Prevent dead-code elimination
    if s == -1:  # impossible, just to use s
        print("", file=sys.stderr)

    total_accesses = loops * accesses_per_loop
    ns_per_access = (end - start) / total_accesses
    return ns_per_access


def measure_stride_reads_list(num_elements: int, stride_elems: int, min_accesses: int, warmup: bool = True) -> float:
    # Python list of ints (pointer indirection heavy)
    data = list(range(num_elements))

    if warmup:
        s = 0
        for i in range(0, num_elements, stride_elems):
            s ^= data[i]

    accesses_per_loop = max(1, (num_elements + stride_elems - 1) // stride_elems)
    loops = max(1, (min_accesses + accesses_per_loop - 1) // accesses_per_loop)

    start = time.perf_counter_ns()
    s = 0
    for _ in range(loops):
        for i in range(0, num_elements, stride_elems):
            s ^= data[i]
    end = time.perf_counter_ns()

    if s == -1:
        print("", file=sys.stderr)

    total_accesses = loops * accesses_per_loop
    ns_per_access = (end - start) / total_accesses
    return ns_per_access


def main() -> int:
    parser = argparse.ArgumentParser(description="Measure memory access times across working set sizes using stride reads.")
    parser.add_argument("--min-kb", type=int, default=16, help="Minimum working set size in KB (default: 16)")
    parser.add_argument("--max-mb", type=int, default=512, help="Maximum working set size in MB (default: 512)")
    parser.add_argument("--max-gb", type=float, default=None, help="Maximum working set size in GB (overrides --max-mb if set)")
    parser.add_argument("--stride-bytes", type=int, default=64, help="Stride in bytes between accesses (default: 64)")
    parser.add_argument("--min-accesses", type=int, default=1_000_000, help="Minimum total element accesses per data point (default: 1,000,000)")
    parser.add_argument("--repeats", type=int, default=3, help="Number of repeated measurements to median (default: 3)")
    parser.add_argument("--list", dest="use_list", action="store_true", help="Also measure Python list (slower, includes object overhead)")
    parser.add_argument("--array", dest="use_array", action="store_true", help="Also measure array('Q') contiguous memory")
    parser.add_argument("--csv", dest="csv", action="store_true", help="Output CSV format")
    parser.add_argument("--no-warmup", dest="warmup", action="store_false", help="Disable warmup loop per size")
    args = parser.parse_args()

    # Default to both if neither explicitly chosen
    if not args.use_list and not args.use_array:
        args.use_list = True
        args.use_array = True

    element_size = 8  # bytes for array('Q')
    stride_elems = max(1, args.stride_bytes // element_size)

    max_bytes = (
        int(args.max_gb * 1024 * 1024 * 1024) if args.max_gb is not None else int(args.max_mb * 1024 * 1024)
    )
    sizes = generate_sizes(args.min_kb, max_bytes)

    if args.csv:
        header_cols = [
            "bytes",
            "human",
        ]
        if args.use_array:
            header_cols.append("ns_per_access_arrayQ")
        if args.use_list:
            header_cols.append("ns_per_access_list")
        print(",".join(header_cols))

    # Pretty table header for human-readable mode
    if not args.csv:
        header_cols = [
            "bytes",
            "size",
        ]
        if args.use_array:
            header_cols.append("array_Q ns/access")
        if args.use_list:
            header_cols.append("list ns/access")
        print(" ".join(f"{h:>16}" for h in header_cols))

    for size_bytes in sizes:
        # For the array benchmark, element count corresponds to size_bytes / 8
        num_elements_array = max(1, size_bytes // element_size)

        # For the list benchmark, we use the same element count to keep access counts comparable.
        num_elements_list = num_elements_array

        # Skip absurdly small sizes that would have too few elements
        if num_elements_array < 8:
            continue

        ns_array = None
        ns_list = None

        # Protect against MemoryError for very large allocations; skip if cannot allocate
        if args.use_array:
            try:
                # Measure median over repeats to reduce jitter
                samples = []
                for _ in range(args.repeats):
                    samples.append(
                        measure_stride_reads_array(
                            num_elements=num_elements_array,
                            stride_elems=stride_elems,
                            min_accesses=args.min_accesses,
                            warmup=bool(args.warmup),
                        )
                    )
                ns_array = statistics.median(samples)
            except MemoryError:
                ns_array = float("nan")
        if args.use_list:
            try:
                samples = []
                for _ in range(args.repeats):
                    samples.append(
                        measure_stride_reads_list(
                            num_elements=num_elements_list,
                            stride_elems=stride_elems,
                            min_accesses=args.min_accesses // 10,  # lists are slower
                            warmup=bool(args.warmup),
                        )
                    )
                ns_list = statistics.median(samples)
            except MemoryError:
                ns_list = float("nan")

        if args.csv:
            row = [str(size_bytes), human_bytes(size_bytes)]
            if args.use_array:
                row.append(f"{ns_array:.3f}" if ns_array is not None and not math.isnan(ns_array) else "nan")
            if args.use_list:
                row.append(f"{ns_list:.3f}" if ns_list is not None and not math.isnan(ns_list) else "nan")
            print(",".join(row))
        else:
            cols = [
                f"{size_bytes:>16}",
                f"{human_bytes(size_bytes):>16}",
            ]
            if args.use_array:
                cols.append(
                    f"{ns_array:>16.2f}" if ns_array is not None and not math.isnan(ns_array) else f"{float('nan'):>16}"
                )
            if args.use_list:
                cols.append(
                    f"{ns_list:>16.2f}" if ns_list is not None and not math.isnan(ns_list) else f"{float('nan'):>16}"
                )
            print(" ".join(cols))

    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        print("Interrupted", file=sys.stderr)
        sys.exit(130)


