# Examples

Practical demonstrations in various programming languages that complement the main Java compendium.

These examples provide hands-on experience with concepts that are easier to observe in lower-level languages (C) or with specific language features (Rust's overflow checking, Python's arbitrary precision).

## Overview

| Directory | Language | Topic | Course Relevance |
|-----------|----------|-------|------------------|
| `benchmark_demo/` | Python | Cache & parallelism | T6.1, T7.1 |
| `memory_demo/` | C | Page sizes, page faults | T6.3 |
| `overflow_demo/` | C/Java/Python/Rust | Integer overflow | T2.2 |
| `thread_demo/` | C/Python | Thread limits | OS concepts |

---

## benchmark_demo/

**Performance benchmarks for cache and parallelism.**

### Files
- `cache_benchmark.py` - Demonstrates cache effects through array access patterns
- `parallellism.py` - Measures parallelism benefits and Amdahl's Law in practice

### Run
```bash
python3 cache_benchmark.py
python3 parallellism.py
```

### What You'll Learn
- How stride affects cache hit rate
- Why sequential access is faster than random
- Practical limits of parallelism (Amdahl's Law)

---

## memory_demo/

**Low-level memory experiments in C.**

### Files
- `page_size_detector.c` - Detects system page size using timing analysis
- `pagefault_benchmark.c` - Measures page fault costs

### Build & Run
```bash
gcc -O2 page_size_detector.c -o page_size_detector
./page_size_detector

gcc -O2 pagefault_benchmark.c -o pagefault_benchmark
./pagefault_benchmark
```

### What You'll Learn
- Page size is typically 4KB (but can vary)
- Page faults are EXPENSIVE (thousands of cycles)
- TLB misses are cheaper than page faults but still significant

---

## overflow_demo/

**Integer overflow behavior across programming languages.**

### Files
- `overflow_c.c` - C: Silent wrapping (undefined behavior for signed!)
- `overflow_python.py` - Python: No overflow (arbitrary precision)
- `OverflowJava.java` - Java: Silent wrapping (defined behavior)
- `overflow_rust.rs` - Rust: Panic in debug, wrap in release
- `run_all_overflows.sh` - Run all demos
- `OVERFLOW_COMPARISON.md` - Detailed comparison

### Build & Run
```bash
# Run all at once
./run_all_overflows.sh

# Or individually
gcc overflow_c.c -o overflow_c && ./overflow_c
python3 overflow_python.py
javac OverflowJava.java && java OverflowJava
rustc overflow_rust.rs && ./overflow_rust
```

### What You'll Learn
- Two's complement wrapping: MAX_INT + 1 = MIN_INT
- Language design choices for safety vs performance
- Why Rust panics in debug mode (catches bugs early)

### Connection to Course
**T2.2**: Understanding integer representation and overflow detection.

---

## thread_demo/

**Thread creation and system limits.**

### Files
- `thread_overflow.c` - Creates threads until system refuses (POSIX threads)
- `thread_overflow.py` - Python threading limits

### Build & Run
```bash
gcc -pthread thread_overflow.c -o thread_overflow
./thread_overflow

python3 thread_overflow.py
```

### What You'll Learn
- System has limits on thread count
- Each thread requires stack space
- Thread creation has overhead

---

## Prerequisites

- **C**: `gcc` compiler
- **Python**: Python 3.x
- **Java**: `javac` and `java` (JDK 11+)
- **Rust**: `rustc` (optional)

## Relationship to Main Project

These examples are **supplements** to the main Java compendium in `src/computerdesign/`. 

| If you want to understand... | Look at... |
|------------------------------|------------|
| How cache works conceptually | `src/computerdesign/memory/Cache.java` |
| How cache affects real programs | `examples/benchmark_demo/cache_benchmark.py` |
| Virtual memory architecture | `src/computerdesign/memory/VirtualMemory.java` |
| Page faults in practice | `examples/memory_demo/pagefault_benchmark.c` |
| Two's complement math | `src/computerdesign/theory/NumberSystems.java` |
| Overflow in real languages | `examples/overflow_demo/` |

The Java compendium explains the *concepts*; these examples show them *in action*.

