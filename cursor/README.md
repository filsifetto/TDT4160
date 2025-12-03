# Cursor Archive - Organized Structure

This directory contains various programming demonstrations and benchmarks organized by topic.

## Directory Structure

### `overflow_demo/`
Demonstrates arithmetic overflow handling across different programming languages:
- **C**: Silent wrapping behavior
- **Python**: No overflow (arbitrary precision integers)
- **Java**: Silent wrapping with optional detection
- **Rust**: Panics in debug, wraps in release, with explicit control methods

**Files:**
- `overflow_c.c` - C overflow demonstration
- `overflow_python.py` - Python overflow demonstration
- `OverflowJava.java` - Java overflow demonstration
- `overflow_rust.rs` - Rust overflow demonstration
- `OVERFLOW_COMPARISON.md` - Detailed comparison document
- `run_all_overflows.sh` - Script to run all demonstrations

**Run all:** `cd overflow_demo && ./run_all_overflows.sh`

### `thread_demo/`
Demonstrates thread creation and system limits:
- **C**: POSIX threads (`pthread`)
- **Python**: Python threading module

**Files:**
- `thread_overflow.c` - C thread overflow program
- `thread_overflow.py` - Python thread overflow program

**Run:** Compile and run to see how many threads your system can create before hitting limits.

### `memory_demo/`
Demonstrates memory page size detection and access timing:
- Page boundary detection
- Cold vs hot memory access timing
- Page fault measurements

**Files:**
- `page_size_detector.c` - Detects page size and measures access times
- `pagefault_benchmark.c` - Measures page fault timing

**Run:** Compile and run to detect your system's page size and measure access times.

### `benchmarks/`
Various performance benchmarks:
- Cache performance testing
- Parallelism benchmarks

**Files:**
- `cache_benchmark.py` - Cache performance benchmark
- `parallellism.py` - Parallelism benchmark

### `cursor-vibe-coding-template/`
Template and documentation for coding practices.

## Quick Start

Each demo folder contains its own programs. Navigate to the folder and follow the instructions in the individual files or run the provided scripts.

## Notes

- Compiled binaries (`.o`, executables) are included for convenience but can be regenerated
- Some programs require specific compilers/interpreters (gcc, python3, javac, rustc)
- Check individual files for compilation and execution instructions

