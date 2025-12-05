# TDT4160 - Computer Architecture Compendium

A comprehensive study resource for **Computer Organization and Design**, modeling all key concepts through code. If you understand this project, you understand the course.

## 🎯 Purpose

This project is designed as a **learning compendium** for TDT4160 at NTNU. Every concept from the course is implemented in code with extensive documentation explaining the *why* behind each design decision.

**Philosophy**: Code is the best documentation. Each class represents a hardware component or concept, with Javadoc explaining how it maps to real computer architecture.

## 📚 Learning Goals

See **[LEARNING_GOALS.md](LEARNING_GOALS.md)** for the complete course objectives.

This project covers **all** topics:

| Topic | Package/Module | Key Classes |
|-------|----------------|-------------|
| **T1: Performance** | `theory/` | `Performance.java` - Iron Law, Amdahl's Law, AMAT |
| **T2: Instruction Set** | `instruction/`, `theory/` | `Instruction.java`, `CallingConvention.java`, `NumberSystems.java` |
| **T3: Single-cycle CPU** | `processor/`, `theory/` | `SingleCycleProcessor.java`, `DigitalLogic.java` |
| **T4: Multi-cycle CPU** | `processor/` | `MultiCycleProcessor.java` (FSM control) |
| **T5: Pipeline CPU** | `processor/`, `pipeline/` | `PipelineProcessor.java`, `HazardUnit.java`, `ExceptionHandler.java` |
| **T6: Memory System** | `memory/` | `Cache.java`, `TLB.java`, `VirtualMemory.java`, `PageTable.java` |
| **T7: Parallel Computing** | `theory/` | `ParallelComputing.java` - Flynn, Roofline, coherence |

## 🏗️ Project Structure

```
TDT4160/
├── src/                          # Main Java source (the core compendium)
│   └── computerdesign/
│       ├── alu/                  # Arithmetic Logic Unit
│       │   └── ALU.java          # T3.3: ALU operations, flags
│       │
│       ├── control/              # Control Unit
│       │   └── ControlUnit.java  # T3.1: Control signals, truth tables
│       │
│       ├── instruction/          # RISC-V Instructions
│       │   ├── Instruction.java  # T2.1: All 6 formats, 3 design principles
│       │   └── InstructionDecoder.java
│       │
│       ├── memory/               # Memory Hierarchy
│       │   ├── MemoryUnit.java   # Interface for all memory
│       │   ├── Register.java     # Fastest: 0 cycles
│       │   ├── RegisterFile.java # T2.3: ABI, calling conventions
│       │   ├── Cache.java        # T6.1: Set-associative, AMAT, write policies
│       │   ├── TLB.java          # T6.3: Translation Lookaside Buffer
│       │   ├── MainMemory.java   # Physical RAM, frame management
│       │   ├── PageTable.java    # T6.3: Virtual→Physical mapping
│       │   └── VirtualMemory.java# T6.3: Process address space
│       │
│       ├── processor/            # CPU Implementations
│       │   ├── Processor.java    # Interface: fetch-decode-execute
│       │   ├── SingleCycleProcessor.java  # T3.1: CPI=1, long cycle
│       │   ├── MultiCycleProcessor.java   # T4.1: FSM control
│       │   ├── PipelineProcessor.java     # T5.1: 5-stage, hazards
│       │   └── ExceptionHandler.java      # T5.2: Precise exceptions
│       │
│       ├── pipeline/             # Pipeline Components
│       │   ├── PipelineRegister.java  # IF/ID, ID/EX, EX/MEM, MEM/WB
│       │   └── HazardUnit.java        # T5.1: Forwarding, stalling
│       │
│       ├── os/                   # Operating System Concepts
│       │   ├── Process.java      # PCB, states, virtual memory
│       │   ├── ProcessThread.java# Thread vs process
│       │   └── Scheduler.java    # Round-robin, priority
│       │
│       ├── theory/               # Theoretical Foundations
│       │   ├── Performance.java       # T1.3: Iron Law, Amdahl's, power
│       │   ├── NumberSystems.java     # T2.2: Binary, hex, 2's complement
│       │   ├── DigitalLogic.java      # T3.2/T4.2: Gates, latches, FSM
│       │   ├── CallingConvention.java # T2.3: Stack, ABI, addressing
│       │   └── ParallelComputing.java # T7: Flynn, Roofline, coherence
│       │
│       └── Main.java             # Run all demonstrations
│
├── out/                          # Compiled classes
│
├── examples/                     # Practical demos in various languages
│   ├── benchmark_demo/           # Cache & parallelism benchmarks (Python)
│   ├── memory_demo/              # Page faults, page sizes (C)
│   ├── overflow_demo/            # Integer overflow comparison (C/Java/Python/Rust)
│   └── thread_demo/              # Thread creation limits (C/Python)
│
├── LEARNING_GOALS.md             # Official course objectives
└── README.md                     # This file
```

## 🚀 Quick Start

### Run the Main Demonstration

```bash
# Compile
cd TDT4160
javac -d out src/computerdesign/**/*.java

# Run all demonstrations
java -cp out computerdesign.Main

# Or run a specific topic
java -cp out computerdesign.Main performance
java -cp out computerdesign.Main parallel
java -cp out computerdesign.Main memory
```

### Available Demo Topics

```
performance  - Iron Law, Amdahl's Law, AMAT calculations
numbers      - Binary, hex, 2's complement, floating point
logic        - Gates, truth tables, latches, flip-flops
memory       - Cache hierarchy, hit rates, locality
tlb          - Translation Lookaside Buffer
alu          - Arithmetic and logical operations
instructions - RISC-V encoding, formats, disassembly
calling      - Calling conventions, stack, memory layout
processors   - Single-cycle vs multi-cycle vs pipeline
exceptions   - Exception handling in pipeline
virtual      - Virtual memory, page tables, isolation
parallel     - Flynn's taxonomy, Roofline model, coherence
threads      - Processes, threads, scheduling
```

## 🔑 Key Concepts by Topic

### T1: Performance

```
CPU Time = Instructions × CPI × Clock Period
         = Instructions × CPI / Clock Frequency

AMAT = Hit Time + Miss Rate × Miss Penalty

Amdahl's Speedup = 1 / ((1-f) + f/S)
```

See `src/computerdesign/theory/Performance.java`

### T2: Instructions

**Three Design Principles (Patterson & Hennessy):**
1. Simplicity favours regularity
2. Smaller is faster
3. Good design demands good compromises

See `src/computerdesign/instruction/Instruction.java`

### T3/T4/T5: Processor Architectures

```
┌─────────────────────────────────────────────────────────────┐
│ Single-Cycle: CPI=1, but clock = slowest instruction        │
│ Multi-Cycle:  CPI varies, faster clock, FSM control         │
│ Pipeline:     CPI→1, fast clock, hazard handling needed     │
└─────────────────────────────────────────────────────────────┘
```

### T6: Memory System

```
Registers → L1 Cache → L2 Cache → Main Memory → Disk
  0 cyc       1-4 cyc    10-20 cyc   100+ cyc    millions
```

**Virtual Memory**: Physical memory is HARDWARE state (shared), page tables are PROCESS state (per-process).

### T7: Parallel Computing

**Flynn's Taxonomy:**
- SISD: Traditional uniprocessor
- SIMD: Vector/GPU (one instruction, multiple data)
- MIMD: Multicore (multiple instructions, multiple data)

## 📁 Examples Directory

Additional practical demonstrations in various languages:

### `examples/overflow_demo/`
Integer overflow behavior across languages:
- **C**: Silent wrapping
- **Python**: Arbitrary precision (no overflow!)
- **Java**: Silent wrapping
- **Rust**: Panic in debug, wrap in release

### `examples/memory_demo/`
Low-level memory experiments:
- Page size detection
- Page fault benchmarking
- Memory access patterns

### `examples/benchmark_demo/`
Performance measurement:
- Cache performance testing
- Parallelism benchmarks

### `examples/thread_demo/`
Threading limits and behavior across platforms.

## 🎓 Study Tips

1. **Read the Javadoc** - Every class has extensive documentation with ASCII diagrams
2. **Run the demos** - See concepts in action with `java -cp out computerdesign.Main`
3. **Trace the code** - Follow instruction execution through the processors
4. **Modify and experiment** - Change cache sizes, add hazards, etc.

## 📖 References

- Patterson & Hennessy, *Computer Organization and Design: RISC-V Edition*
- RISC-V Specification: https://riscv.org/specifications/

## License

Educational use for TDT4160 at NTNU.
