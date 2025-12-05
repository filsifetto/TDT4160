# OOP Perspective on Computer Design

A Java project that models computer architecture concepts using Object-Oriented Programming. Inspired by Patterson & Hennessy's *Computer Organization and Design: RISC-V Edition*.

## Philosophy

Understanding computer architecture is easier when you can see the **state** and **behavior** of each component clearly. OOP provides a natural way to model this:

- **Classes** represent hardware components (ALU, registers, memory, etc.)
- **Fields** represent the **state** each component holds
- **Methods** represent the **operations** each component can perform
- **Interfaces** capture common behavior (e.g., all memory types can read/write)

## Processor Architecture: Datapath vs Control

A processor consists of two fundamental parts:

```
┌─────────────────────────────────────────────────────────────────────┐
│                           DATAPATH                                  │
│  "The roads and vehicles" - where data flows and gets transformed   │
│                                                                     │
│   Register File → Muxes → ALU → Memory → Pipeline Registers         │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                           CONTROL                                   │
│  "The traffic lights" - decides what happens when                   │
│                                                                     │
│   Control Unit (decoder) + Hazard Unit (forwarding & stalls)        │
└─────────────────────────────────────────────────────────────────────┘
```

**Stateful components** (values persist across clock cycles):
- Registers, RegisterFile, Memory, Pipeline Registers, PC

**Combinational components** (output depends only on current inputs):
- ALU, Control Unit, Hazard Unit, Multiplexers

## Project Structure

```
src/computerdesign/
├── alu/                 # DATAPATH: Computational unit (combinational)
│   └── ALU.java                
│
├── memory/              # DATAPATH: Storage hierarchy (stateful)
│   ├── MemoryUnit.java         # Common interface for all memory
│   ├── Register.java           # Single register
│   ├── RegisterFile.java       # All 32 RISC-V registers
│   ├── Cache.java              # Fast memory (L1, L2)
│   ├── MainMemory.java         # Physical RAM (HARDWARE state)
│   ├── PageTable.java          # Virtual→physical mapping (PROCESS state)
│   └── VirtualMemory.java      # Per-process address space
│
├── instruction/         # REPRESENTATION: How instruction bits are organized
│   ├── Instruction.java        # 32-bit RISC-V instruction encoding
│   └── InstructionDecoder.java # Disassembly for debugging
│
├── control/             # CONTROL: Decision logic (combinational)
│   └── ControlUnit.java        # Generates control signals from opcode
│
├── pipeline/            # PIPELINING: Stage separation
│   ├── PipelineRegister.java   # DATAPATH: IF/ID, ID/EX, EX/MEM, MEM/WB
│   └── HazardUnit.java         # CONTROL: Forwarding & stall logic
│
├── processor/           # INTEGRATION: Complete CPU implementations
│   ├── Processor.java          # Common interface
│   ├── SingleCycleProcessor.java
│   ├── MultiCycleProcessor.java
│   ├── PipelineProcessor.java
│   └── ProcessorStats.java     # Performance metrics
│
├── os/                  # SYSTEM SOFTWARE: OS concepts
│   ├── Process.java            # Process abstraction (owns page table!)
│   ├── ProcessThread.java      # Thread abstraction
│   └── Scheduler.java          # CPU scheduling
│
└── Main.java            # Demonstration program
```

## Patterson & Hennessy's Three Principles

The RISC-V instruction set embodies these fundamental design principles:

1. **Simplicity favours regularity**
   - Fixed 32-bit instruction size
   - rs1, rs2, rd always in the same bit positions
   - Three-operand format: `rd = rs1 op rs2`

2. **Smaller is faster**
   - Only 32 registers (sweet spot: not too few, not too many)
   - Simple instruction formats = less decode logic
   - Load/store architecture (only loads/stores access memory)

3. **Good design demands good compromises**
   - Immediate bits are "scrambled" in B-type/J-type, but sign bit always at [31]
   - 32-bit constants need two instructions (LUI + ADDI), but simple formats
   - No condition codes (simpler hardware, but comparisons need a register)

## Key Concepts Modeled

### 1. Memory Hierarchy
```
Registers (0 cycles) → L1 Cache (4 cycles) → Main Memory (100 cycles)
     ↑                       ↑                        ↑
   Fastest               Medium                   Slowest
   Smallest              Medium                   Largest
```

Each level implements the `MemoryUnit` interface but with different characteristics.

### 2. Processor Architectures

| Architecture | CPI | Clock Period | Throughput |
|--------------|-----|--------------|------------|
| Single-Cycle | 1 | Long (slowest instruction) | Low |
| Multi-Cycle | 3-5 | Short | Medium |
| Pipeline | ~1 | Short | High (best!) |

### 3. Pipeline Stages
```
IF → ID → EX → MEM → WB
│    │    │    │     │
│    │    │    │     └── Write result to register
│    │    │    └──────── Access memory (load/store)
│    │    └───────────── ALU operation
│    └────────────────── Decode instruction, read registers
└─────────────────────── Fetch instruction from memory
```

### 4. Virtual Memory: Hardware vs Process State

A key insight: **Physical memory is HARDWARE state, Page tables are PROCESS state.**

```
┌─────────────────────────────────────────────────────────────┐
│  HARDWARE STATE (shared)                                    │
│  └── MainMemory (physical RAM, divided into frames)         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  PROCESS STATE (per-process)                                │
│  └── PageTable (maps virtual pages → physical frames)       │
│      Process A: virtual 0x1000 → frame 5                    │
│      Process B: virtual 0x1000 → frame 9  (different!)      │
└─────────────────────────────────────────────────────────────┘
```

This is how each process gets its own isolated address space!

### 5. Process vs Thread

| Aspect | Process | Thread |
|--------|---------|--------|
| Memory | Own address space | Shared with process |
| Creation | Heavy (copy memory) | Light (just stack) |
| Context switch | Slow | Fast |
| Communication | IPC needed | Shared memory |

## Running the Demo

```bash
# Navigate to the project directory
cd oop_demo

# Compile all Java files
javac -d out src/computerdesign/**/*.java src/computerdesign/*.java

# Run the demonstration
java -cp out computerdesign.Main
```

Or use your favorite IDE (IntelliJ, Eclipse, VS Code with Java extension).

## What You'll Learn

1. **State vs Behavior**: Registers *hold* values (state), ALU *computes* values (behavior)
2. **Hierarchy**: Memory hierarchy trades off speed vs capacity
3. **Pipelining**: How to execute multiple instructions simultaneously
4. **Hazards**: Why pipelining isn't trivial (data dependencies, branches)
5. **Abstraction**: How the OS hides hardware complexity from programs

## Extending the Project

Ideas for further exploration:

- **Branch Prediction**: Reduce control hazards
- **Out-of-Order Execution**: Execute instructions when operands are ready
- **TLB (Translation Lookaside Buffer)**: Cache for page table entries
- **Superscalar**: Multiple pipelines
- **SIMD**: Vector operations
- **Multi-core**: Multiple processors with cache coherence

## References

- Patterson & Hennessy, *Computer Organization and Design: RISC-V Edition*
- RISC-V Specification: https://riscv.org/specifications/
- Hennessy & Patterson, *Computer Architecture: A Quantitative Approach*

## License

Educational purposes. Feel free to use and modify for learning!

