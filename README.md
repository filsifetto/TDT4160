# TDT4160 - Computer Organization and Design

A collection of practical demonstrations and experiments for learning about computers, system programming, and how computers work at a fundamental level.

## Overview

This project contains various hands-on demonstrations covering:
- **Memory management** - Page sizes, page faults, and memory access patterns
- **Integer overflow** - How different programming languages handle arithmetic overflow
- **Threading** - Thread creation limits and system constraints
- **Performance benchmarks** - Cache behavior and parallelism

## Structure

```
├── benchmark_demo/      # Performance benchmarks
├── memory_demo/         # Memory and page size demonstrations
├── oop_demo/            # OOP model of computer architecture (Java)
├── overflow_demo/       # Integer overflow across languages
└── thread_demo/         # Thread creation and limits
```

## Getting Started

Each demo folder contains its own programs and documentation. Navigate to a folder and follow the instructions in the individual files.

### Prerequisites

- **C**: `gcc` compiler
- **Python**: Python 3.x
- **Java**: `javac` and `java`
- **Rust**: `rustc` or `cargo`

## Demos

### Overflow Demo (`overflow_demo/`)

Demonstrates arithmetic overflow handling across different programming languages:
- **C**: Silent wrapping behavior
- **Python**: No overflow (arbitrary precision integers)
- **Java**: Silent wrapping with optional detection
- **Rust**: Panics in debug, wraps in release, with explicit control methods

See `overflow_demo/OVERFLOW_COMPARISON.md` for detailed comparison.

### Memory Demo (`memory_demo/`)

Demonstrates memory page size detection and access timing:
- Page boundary detection
- Cold vs hot memory access timing
- Page fault measurements

### Thread Demo (`thread_demo/`)

Demonstrates thread creation and system limits using POSIX threads (C) and Python threading.

### Benchmark Demo (`benchmark_demo/`)

Various performance benchmarks including cache performance testing and parallelism benchmarks.

### OOP Demo (`oop_demo/`)

A Java project that models computer architecture using Object-Oriented Programming:
- **Processors**: Single-cycle, multi-cycle, and pipelined implementations
- **Memory hierarchy**: Registers, cache, main memory
- **Instructions**: RISC-V instruction encoding and decoding
- **OS concepts**: Processes, threads, and scheduling

See `oop_demo/README.md` for detailed documentation.

## Learning Goals

This project supports **TDT4160 - Datamaskiner - Grunnkurs** at NTNU.

See **[LEARNING_GOALS.md](LEARNING_GOALS.md)** for the complete course objectives (læringsutbyttebeskrivelser).

### Topic Coverage

| Course Topic | Demo/Project |
|--------------|--------------|
| T2: Instruksjonssett (RISC-V) | `oop_demo/` - Instruction encoding |
| T3: Enkeltsykelprosessor | `oop_demo/` - SingleCycleProcessor |
| T4: Flersykelprosessor | `oop_demo/` - MultiCycleProcessor |
| T5: Samlebåndsprosessor | `oop_demo/` - PipelineProcessor, HazardUnit |
| T6: Minnesystem | `oop_demo/` - Cache, Virtual Memory |
| T6: Minnesystem | `memory_demo/` - Page faults, page sizes |
| T7: Parallelle datamaskiner | `benchmark_demo/` - Parallelism |
| Overflow & representation | `overflow_demo/` - Integer overflow |
| Threads & processes | `thread_demo/`, `oop_demo/os/` |

## License

This project is for educational purposes.

