# OOP Perspective on Computer Design

A Java project that models computer architecture concepts using Object-Oriented Programming. Inspired by Patterson & Hennessy's *Computer Organization and Design: RISC-V Edition*.

## Philosophy

Understanding computer architecture is easier when you can see the **state** and **behavior** of each component clearly. OOP provides a natural way to model this:

- **Classes** represent hardware components (ALU, registers, memory, etc.)
- **Fields** represent the **state** each component holds
- **Methods** represent the **operations** each component can perform
- **Interfaces** capture common behavior (e.g., all memory types can read/write)

## Project Structure

```
src/computerdesign/
├── processor/           # CPU implementations
│   ├── Processor.java          # Interface for all processors
│   ├── SingleCycleProcessor.java
│   ├── MultiCycleProcessor.java
│   └── PipelineProcessor.java
│
├── memory/              # Memory hierarchy
│   ├── MemoryUnit.java         # Interface for all memory
│   ├── Register.java           # Single register
│   ├── RegisterFile.java       # All 32 RISC-V registers
│   ├── Cache.java              # Cache with hit/miss tracking
│   └── MainMemory.java         # RAM
│
├── alu/                 # Arithmetic Logic Unit
│   └── ALU.java                # All arithmetic/logic operations
│
├── control/             # Control logic
│   └── ControlUnit.java        # Instruction decoding
│
├── instruction/         # RISC-V instructions
│   ├── Instruction.java        # Instruction encoding/decoding
│   └── InstructionDecoder.java # Disassembly
│
├── pipeline/            # Pipeline components
│   ├── PipelineRegister.java   # IF/ID, ID/EX, EX/MEM, MEM/WB
│   └── HazardUnit.java         # Data/control hazard detection
│
├── os/                  # Operating system concepts
│   ├── Process.java            # Process abstraction
│   ├── ProcessThread.java      # Thread abstraction
│   └── Scheduler.java          # CPU scheduling
│
└── Main.java            # Demonstration program
```

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

### 4. Process vs Thread

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
- **Virtual Memory**: Page tables, TLB
- **Superscalar**: Multiple pipelines
- **SIMD**: Vector operations

## References

- Patterson & Hennessy, *Computer Organization and Design: RISC-V Edition*
- RISC-V Specification: https://riscv.org/specifications/
- Hennessy & Patterson, *Computer Architecture: A Quantitative Approach*

## License

Educational purposes. Feel free to use and modify for learning!

