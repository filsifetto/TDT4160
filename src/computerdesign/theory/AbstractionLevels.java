package computerdesign.theory;

/**
 * AbstractionLevels - The complete hierarchy of computer system abstractions.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE SEVEN LAYERS OF COMPUTER ABSTRACTION
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Each layer provides a simpler interface for the layer above, hiding complexity.
 * This is the fundamental organizing principle of computer systems!
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                                                                             │
 * │  ╔═══════════════════════════════════════════════════════════════════════╗  │
 * │  ║  LEVEL 7: APPLICATIONS                                                ║  │
 * │  ║  • Web browsers, word processors, games                               ║  │
 * │  ║  • Sees: Files, windows, network connections                          ║  │
 * │  ║  • Language: High-level (Python, Java, JavaScript)                    ║  │
 * │  ╚═══════════════════════════════════════════════════════════════════════╝  │
 * │                              │ System Calls                                 │
 * │                              ▼                                              │
 * │  ╔═══════════════════════════════════════════════════════════════════════╗  │
 * │  ║  LEVEL 6: OPERATING SYSTEM                                            ║  │
 * │  ║  • Process management, memory management, file systems                ║  │
 * │  ║  • Sees: Processes, virtual memory, device drivers                    ║  │
 * │  ║  • Language: C, Rust, Assembly                                        ║  │
 * │  ╚═══════════════════════════════════════════════════════════════════════╝  │
 * │                              │ VM Interface / Hypercalls                    │
 * │                              ▼                                              │
 * │  ╔═══════════════════════════════════════════════════════════════════════╗  │
 * │  ║  LEVEL 5: VIRTUAL MACHINE                                             ║  │
 * │  ║  • Hypervisor creates illusion of dedicated hardware                  ║  │
 * │  ║  • Sees: Virtual CPU, virtual RAM, virtual devices                    ║  │
 * │  ║  • Enables: Multiple OSes, isolation, live migration                  ║  │
 * │  ╚═══════════════════════════════════════════════════════════════════════╝  │
 * │                              │ ISA (Instruction Set Architecture)           │
 * │                              ▼                                              │
 * │  ╔═══════════════════════════════════════════════════════════════════════╗  │
 * │  ║  LEVEL 4: MACRO-ARCHITECTURE (ISA)                                    ║  │
 * │  ║  • The contract between hardware and software                         ║  │
 * │  ║  • Sees: Registers, instructions, memory addresses                    ║  │
 * │  ║  • Examples: RISC-V, x86-64, ARM, MIPS                                ║  │
 * │  ╚═══════════════════════════════════════════════════════════════════════╝  │
 * │                              │ Implementation                               │
 * │                              ▼                                              │
 * │  ╔═══════════════════════════════════════════════════════════════════════╗  │
 * │  ║  LEVEL 3: MICRO-ARCHITECTURE                                          ║  │
 * │  ║  • How the ISA is implemented                                         ║  │
 * │  ║  • Sees: Pipeline stages, cache hierarchy, branch predictors          ║  │
 * │  ║  • Invisible to software, but affects performance                     ║  │
 * │  ╚═══════════════════════════════════════════════════════════════════════╝  │
 * │                              │ Logic Design                                 │
 * │                              ▼                                              │
 * │  ╔═══════════════════════════════════════════════════════════════════════╗  │
 * │  ║  LEVEL 2: DIGITAL ELECTRONICS                                         ║  │
 * │  ║  • Logic gates, flip-flops, registers, buses                          ║  │
 * │  ║  • Sees: 0s and 1s, clock signals, combinational/sequential logic     ║  │
 * │  ║  • Language: Verilog, VHDL                                            ║  │
 * │  ╚═══════════════════════════════════════════════════════════════════════╝  │
 * │                              │ Circuit Design                               │
 * │                              ▼                                              │
 * │  ╔═══════════════════════════════════════════════════════════════════════╗  │
 * │  ║  LEVEL 1: ANALOG ELECTRONICS                                          ║  │
 * │  ║  • Transistors, voltages, currents, resistance                        ║  │
 * │  ║  • Sees: Continuous signals, noise, power dissipation                 ║  │
 * │  ║  • Physics of semiconductor devices                                   ║  │
 * │  ╚═══════════════════════════════════════════════════════════════════════╝  │
 * │                                                                             │
 * │                         ┌───────────────────┐                               │
 * │                         │      PHYSICS      │                               │
 * │                         │ (Quantum effects, │                               │
 * │                         │  Maxwell's eqns)  │                               │
 * │                         └───────────────────┘                               │
 * └─────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WHY ABSTRACTION MATTERS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 1. MANAGES COMPLEXITY
 *    - Billions of transistors → comprehensible instruction set
 *    - Programmers don't need to understand physics
 *    - Hardware designers don't need to understand applications
 * 
 * 2. ENABLES INDEPENDENT EVOLUTION
 *    - Applications work on any ISA implementation
 *    - Microarchitecture can improve without changing ISA
 *    - New transistor technology doesn't require new software
 * 
 * 3. PROVIDES INTERFACES
 *    - ISA: Contract between hardware and software
 *    - System calls: Contract between OS and applications
 *    - VM interface: Contract between hypervisor and guest OS
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * KEY INSIGHT: THE ISA BOUNDARY
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *                    ┌─────────────────────────────────────┐
 *                    │         SOFTWARE DOMAIN             │
 *                    │   (Applications, OS, Compilers)     │
 *                    └───────────────────┬─────────────────┘
 *                                        │
 *            ════════════════════════════════════════════════
 *                              ISA BOUNDARY
 *            ────────────────────────────────────────────────
 *                                        │
 *                    ┌───────────────────┴─────────────────┐
 *                    │         HARDWARE DOMAIN             │
 *                    │   (Microarch, Circuits, Physics)    │
 *                    └─────────────────────────────────────┘
 * 
 * The ISA is the MOST IMPORTANT interface:
 *   - Software sees: Registers, instructions, memory model
 *   - Hardware implements: However it wants (pipelining, OoO, etc.)
 *   - As long as the ISA semantics are preserved, ANY implementation works!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * DETAILED BREAKDOWN OF EACH LEVEL
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * LEVEL 7: APPLICATIONS
 * ─────────────────────
 * 
 *   What you see:    GUI windows, files, network connections
 *   What's hidden:   System calls, memory allocation, threads
 *   Examples:        Chrome, Word, Minecraft, TensorFlow
 * 
 *   Application's View:
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  open("data.txt")  →  FILE*                                            │
 *   │  malloc(1024)      →  pointer to memory                                │
 *   │  socket(...)       →  network connection                               │
 *   │  Thread.start()    →  parallel execution                               │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 
 * LEVEL 6: OPERATING SYSTEM
 * ─────────────────────────
 * 
 *   What you see:    Processes, virtual memory, file systems, device drivers
 *   What's hidden:   Physical memory layout, hardware details
 *   Examples:        Linux, Windows, macOS, FreeBSD
 * 
 *   OS's Responsibilities:
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  PROCESS MANAGEMENT:  Create, schedule, terminate processes            │
 *   │  MEMORY MANAGEMENT:   Virtual memory, paging, protection               │
 *   │  FILE SYSTEM:         Organize data on storage devices                 │
 *   │  DEVICE DRIVERS:      Interface with hardware                          │
 *   │  SECURITY:            User permissions, isolation                      │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 
 * LEVEL 5: VIRTUAL MACHINE
 * ────────────────────────
 * 
 *   What you see:    Complete virtual hardware (vCPU, vRAM, vDevices)
 *   What's hidden:   Host OS, physical hardware, other VMs
 *   Examples:        VMware VM, VirtualBox VM, AWS EC2 instance
 * 
 *   VM's View:
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  "I have my own dedicated CPU with 4 cores"                            │
 *   │  "I have 8 GB of RAM all to myself"                                    │
 *   │  "I have a 100 GB disk"                                                │
 *   │  "I have a network card with IP 192.168.1.100"                         │
 *   └─────────────────────────────────────────────────────────────────────────┘
 *   
 *   Reality:
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  Sharing 1 of 64 physical cores with 50 other VMs                      │
 *   │  Memory is overcommitted, some pages may be on disk                    │
 *   │  Disk is a file on host, or slice of SAN storage                       │
 *   │  Network card is software-emulated (virtio)                            │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 
 * LEVEL 4: MACRO-ARCHITECTURE (ISA)
 * ──────────────────────────────────
 * 
 *   What you see:    Registers, instructions, memory addresses
 *   What's hidden:   Pipeline, cache, branch prediction
 *   Examples:        RISC-V, x86-64, ARMv8, MIPS
 * 
 *   ISA Defines:
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  REGISTERS:      x0-x31 (RISC-V), RAX-R15 (x86)                        │
 *   │  INSTRUCTIONS:   ADD, SUB, LOAD, STORE, BRANCH, ...                    │
 *   │  ADDRESSING:     Register, immediate, base+offset, PC-relative         │
 *   │  MEMORY MODEL:   Byte-addressable, endianness, alignment               │
 *   │  EXCEPTIONS:     What traps, when, with what state saved               │
 *   │  PRIVILEGE:      User/Supervisor/Machine modes                         │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 
 * LEVEL 3: MICRO-ARCHITECTURE
 * ───────────────────────────
 * 
 *   What you see:    Internal CPU implementation
 *   What's hidden:   Digital logic implementation details
 *   Examples:        Intel Skylake, AMD Zen 4, Apple M3
 * 
 *   Microarchitectural Features:
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  PIPELINE:          5-stage, 14-stage, superscalar, SMT                │
 *   │  CACHE HIERARCHY:   L1 (32KB), L2 (256KB), L3 (8MB shared)             │
 *   │  BRANCH PREDICTION: BTB, gshare, TAGE, perceptron                      │
 *   │  OUT-OF-ORDER:      Reorder buffer, reservation stations               │
 *   │  EXECUTION UNITS:   ALUs, FPUs, load/store units                       │
 *   │  PREFETCHERS:       Hardware stride detection, pattern learning        │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 
 * LEVEL 2: DIGITAL ELECTRONICS
 * ────────────────────────────
 * 
 *   What you see:    Logic gates, flip-flops, registers, buses
 *   What's hidden:   Transistor-level implementation
 *   Language:        Verilog, VHDL, SystemVerilog
 * 
 *   Digital Building Blocks:
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  COMBINATIONAL:     AND, OR, NOT, XOR, MUX, Decoder, ALU               │
 *   │  SEQUENTIAL:        D flip-flop, register, counter, FSM                │
 *   │  MEMORY:            SRAM (cache), register file                        │
 *   │  INTERCONNECT:      Buses, crossbars, NoC                              │
 *   │  CLOCKING:          Clock distribution, clock gating                   │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 *   Example - A 32-bit Register:
 *       ┌──────────────────────────────────────────────────────────────┐
 *   D ══╡  [D-FF] [D-FF] [D-FF] ... [D-FF] [D-FF] [D-FF]  (32 D-FFs)  ╞══ Q
 *       └────────────────────────┬─────────────────────────────────────┘
 *                                │
 *                               CLK
 * 
 * 
 * LEVEL 1: ANALOG ELECTRONICS
 * ───────────────────────────
 * 
 *   What you see:    Transistors, voltages, currents, timing
 *   What's hidden:   Quantum mechanical effects (mostly)
 *   Domain:          SPICE simulation, fabrication
 * 
 *   Analog Concerns:
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  TRANSISTORS:   CMOS (NMOS/PMOS), FinFET, GAA (Gate-All-Around)        │
 *   │  VOLTAGES:      VDD (1.0V in modern CPUs), threshold voltage            │
 *   │  TIMING:        Propagation delay, setup/hold times                     │
 *   │  POWER:         Dynamic power (CV²f), static power (leakage)           │
 *   │  NOISE:         Crosstalk, IR drop, electromigration                   │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 *   CMOS Inverter (NOT gate):
 *                   VDD
 *                    │
 *                ┌───┴───┐
 *          ──────┤ PMOS  ├──────┬──── Out
 *       In       └───────┘      │
 *                ┌───────┐      │
 *          ──────┤ NMOS  ├──────┘
 *                └───┬───┘
 *                    │
 *                   GND
 * 
 *   When In = 0V (logic 0): PMOS on, NMOS off → Out = VDD (logic 1)
 *   When In = VDD (logic 1): PMOS off, NMOS on → Out = 0V (logic 0)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class AbstractionLevels {
    
    /**
     * Enumeration of all abstraction levels.
     */
    public enum Level {
        ANALOG_ELECTRONICS(1, "Analog Electronics",
            "Transistors, voltages, currents - physics of computing",
            new String[]{"Transistors (CMOS, FinFET)", "Voltages (VDD, GND)", 
                        "Currents", "Power dissipation", "Timing margins"}),
        
        DIGITAL_ELECTRONICS(2, "Digital Electronics",
            "Logic gates, flip-flops, registers - binary computation",
            new String[]{"AND/OR/NOT gates", "Flip-flops", "Registers", 
                        "Combinational logic", "Sequential logic", "FSMs"}),
        
        MICROARCHITECTURE(3, "Micro-Architecture",
            "Pipeline, cache, branch prediction - CPU implementation",
            new String[]{"Pipeline stages", "Cache hierarchy", "Branch predictor",
                        "Out-of-order execution", "Forwarding", "Hazard detection"}),
        
        MACRO_ARCHITECTURE(4, "Macro-Architecture (ISA)",
            "Instructions, registers, memory model - the HW/SW contract",
            new String[]{"Instruction set (RISC-V, x86)", "Registers", 
                        "Addressing modes", "Memory model", "Exception handling"}),
        
        VIRTUAL_MACHINE(5, "Virtual Machine",
            "Hypervisor creates illusion of complete hardware",
            new String[]{"Virtual CPU", "Virtual memory", "Virtual devices",
                        "VM isolation", "Live migration", "Nested virtualization"}),
        
        OPERATING_SYSTEM(6, "Operating System",
            "Process, memory, and I/O management - resource abstraction",
            new String[]{"Processes", "Virtual memory", "File systems",
                        "Device drivers", "Scheduling", "System calls"}),
        
        APPLICATION(7, "Applications",
            "User-facing software - the purpose of computing",
            new String[]{"Web browsers", "Databases", "Games", "AI/ML",
                        "Word processors", "Scientific computing"});
        
        public final int level;
        public final String name;
        public final String description;
        public final String[] components;
        
        Level(int level, String name, String description, String[] components) {
            this.level = level;
            this.name = name;
            this.description = description;
            this.components = components;
        }
    }
    
    /**
     * Represent an interface between levels.
     */
    public enum Interface {
        PHYSICS_TO_ANALOG("Physics → Analog",
            "Semiconductor physics, quantum mechanics"),
        ANALOG_TO_DIGITAL("Analog → Digital",
            "Transistor-level to gate-level abstraction"),
        DIGITAL_TO_MICRO("Digital → Micro",
            "Logic design to microarchitecture"),
        MICRO_TO_ISA("Micro → ISA",
            "Implementation to architecture (hidden from software!)"),
        ISA_TO_VM("ISA → VM",
            "Trap-and-emulate, hardware virtualization support"),
        VM_TO_OS("VM → OS",
            "Hypercalls, virtual hardware interfaces"),
        OS_TO_APP("OS → Application",
            "System calls, libraries, APIs");
        
        public final String name;
        public final String description;
        
        Interface(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
    
    /**
     * Generate the complete abstraction hierarchy visualization.
     */
    public static String generateHierarchy() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  COMPUTER SYSTEM ABSTRACTION HIERARCHY\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        Level[] levels = Level.values();
        
        // Display from top (Application) to bottom (Analog)
        for (int i = levels.length - 1; i >= 0; i--) {
            Level level = levels[i];
            
            sb.append(String.format("  ╔═══════════════════════════════════════════════════════╗\n"));
            sb.append(String.format("  ║ LEVEL %d: %-47s ║\n", level.level, level.name.toUpperCase()));
            sb.append(String.format("  ║ %-55s ║\n", level.description));
            sb.append(String.format("  ╠═══════════════════════════════════════════════════════╣\n"));
            
            // Show components
            sb.append("  ║ Components:                                           ║\n");
            for (String component : level.components) {
                sb.append(String.format("  ║   • %-51s ║\n", component));
            }
            sb.append(String.format("  ╚═══════════════════════════════════════════════════════╝\n"));
            
            // Show interface to next level (if not bottom)
            if (i > 0) {
                Interface iface = Interface.values()[i];
                sb.append(String.format("                        │\n"));
                sb.append(String.format("           ─────────────┴─────────────\n"));
                sb.append(String.format("           %s\n", iface.name));
                sb.append(String.format("           ─────────────┬─────────────\n"));
                sb.append(String.format("                        │\n"));
                sb.append(String.format("                        ▼\n"));
            }
        }
        
        sb.append("\n");
        return sb.toString();
    }
    
    /**
     * Demonstrate how each level sees the same computation.
     */
    public static String demonstratePerspectives() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  HOW EACH LEVEL SEES: x = a + b\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("LEVEL 7 - APPLICATION:\n");
        sb.append("  ┌─────────────────────────────────────────────────────────┐\n");
        sb.append("  │  int x = a + b;     // Java/C/Python                   │\n");
        sb.append("  │  result = num1 + num2  # Just add two numbers          │\n");
        sb.append("  └─────────────────────────────────────────────────────────┘\n\n");
        
        sb.append("LEVEL 6 - OPERATING SYSTEM:\n");
        sb.append("  ┌─────────────────────────────────────────────────────────┐\n");
        sb.append("  │  Process P123 executing at virtual address 0x401234    │\n");
        sb.append("  │  Memory access: pages mapped, permissions OK           │\n");
        sb.append("  │  Context switch if time slice expires                  │\n");
        sb.append("  └─────────────────────────────────────────────────────────┘\n\n");
        
        sb.append("LEVEL 5 - VIRTUAL MACHINE:\n");
        sb.append("  ┌─────────────────────────────────────────────────────────┐\n");
        sb.append("  │  Guest executing on vCPU 0, no VM exit needed          │\n");
        sb.append("  │  ADD instruction is not privileged - runs natively     │\n");
        sb.append("  │  Guest physical 0x1234 → Host physical 0x800001234     │\n");
        sb.append("  └─────────────────────────────────────────────────────────┘\n\n");
        
        sb.append("LEVEL 4 - ISA (RISC-V):\n");
        sb.append("  ┌─────────────────────────────────────────────────────────┐\n");
        sb.append("  │  lw   t0, 0(a0)     # Load 'a' into t0                 │\n");
        sb.append("  │  lw   t1, 4(a0)     # Load 'b' into t1                 │\n");
        sb.append("  │  add  t2, t0, t1    # t2 = t0 + t1                     │\n");
        sb.append("  │  sw   t2, 8(a0)     # Store result to 'x'              │\n");
        sb.append("  └─────────────────────────────────────────────────────────┘\n\n");
        
        sb.append("LEVEL 3 - MICROARCHITECTURE:\n");
        sb.append("  ┌─────────────────────────────────────────────────────────┐\n");
        sb.append("  │  Cycle 1: IF   - Fetch 'add' from I-cache              │\n");
        sb.append("  │  Cycle 2: ID   - Decode, read t0, t1 from register file│\n");
        sb.append("  │  Cycle 3: EX   - ALU computes t0 + t1                  │\n");
        sb.append("  │  Cycle 4: MEM  - (no memory access needed)             │\n");
        sb.append("  │  Cycle 5: WB   - Write result to t2                    │\n");
        sb.append("  │                                                         │\n");
        sb.append("  │  With forwarding: Result available in cycle 3!         │\n");
        sb.append("  └─────────────────────────────────────────────────────────┘\n\n");
        
        sb.append("LEVEL 2 - DIGITAL LOGIC:\n");
        sb.append("  ┌─────────────────────────────────────────────────────────┐\n");
        sb.append("  │  32-bit Ripple Carry Adder:                            │\n");
        sb.append("  │                                                         │\n");
        sb.append("  │   A[0]  B[0]  Cin=0                                    │\n");
        sb.append("  │     │    │    │                                        │\n");
        sb.append("  │   ┌─┴────┴────┴─┐                                      │\n");
        sb.append("  │   │  Full Adder │─── S[0]                              │\n");
        sb.append("  │   └──────┬──────┘                                      │\n");
        sb.append("  │          │ Cout → Cin of next bit                      │\n");
        sb.append("  │          ▼                                              │\n");
        sb.append("  │   (repeat for 32 bits...)                              │\n");
        sb.append("  └─────────────────────────────────────────────────────────┘\n\n");
        
        sb.append("LEVEL 1 - ANALOG (for one XOR in Full Adder):\n");
        sb.append("  ┌─────────────────────────────────────────────────────────┐\n");
        sb.append("  │                    VDD (1.0V)                          │\n");
        sb.append("  │                       │                                 │\n");
        sb.append("  │   A ──┬──[PMOS]──[PMOS]──┬── Out                       │\n");
        sb.append("  │       │         │        │                              │\n");
        sb.append("  │   B ──┼─────────┘        │                              │\n");
        sb.append("  │       │                  │                              │\n");
        sb.append("  │       └──[NMOS]──[NMOS]──┘                             │\n");
        sb.append("  │                 │                                       │\n");
        sb.append("  │                GND (0V)                                 │\n");
        sb.append("  │                                                         │\n");
        sb.append("  │   Voltage at Out swings between 0V and 1.0V            │\n");
        sb.append("  │   Propagation delay: ~10 picoseconds                   │\n");
        sb.append("  └─────────────────────────────────────────────────────────┘\n\n");
        
        return sb.toString();
    }
    
    /**
     * Demonstrate the key interfaces between levels.
     */
    public static String demonstrateInterfaces() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  KEY INTERFACES BETWEEN ABSTRACTION LEVELS\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("THE ISA BOUNDARY (Most Important!):\n");
        sb.append("─────────────────────────────────────────────────────────────\n");
        sb.append("  The ISA is the contract between hardware and software.\n");
        sb.append("  Software relies on ISA semantics; hardware must implement them.\n\n");
        
        sb.append("        ┌─────────────────────────────────────┐\n");
        sb.append("        │         SOFTWARE DOMAIN             │\n");
        sb.append("        │   Compilers, OS, Applications       │\n");
        sb.append("        └───────────────────┬─────────────────┘\n");
        sb.append("                            │\n");
        sb.append("  ══════════════════════════╪══════════════════════════════\n");
        sb.append("                     ISA BOUNDARY\n");
        sb.append("      ADD x1, x2, x3 means: x1 ← x2 + x3 (ALWAYS)\n");
        sb.append("  ══════════════════════════╪══════════════════════════════\n");
        sb.append("                            │\n");
        sb.append("        ┌───────────────────┴─────────────────┐\n");
        sb.append("        │         HARDWARE DOMAIN             │\n");
        sb.append("        │  Single-cycle, Pipeline, OoO, etc.  │\n");
        sb.append("        └─────────────────────────────────────┘\n\n");
        
        sb.append("THE VM BOUNDARY:\n");
        sb.append("─────────────────────────────────────────────────────────────\n");
        sb.append("  Guest OS thinks it's running on real hardware.\n");
        sb.append("  Hypervisor intercepts privileged operations.\n\n");
        
        sb.append("        ┌─────────────────────────────────────┐\n");
        sb.append("        │           GUEST OS                  │\n");
        sb.append("        │   Executes in 'virtual' ring 0      │\n");
        sb.append("        └───────────────────┬─────────────────┘\n");
        sb.append("                            │\n");
        sb.append("  ══════════════════════════╪══════════════════════════════\n");
        sb.append("                     VM BOUNDARY\n");
        sb.append("      Privileged ops → VM Exit → Hypervisor emulates\n");
        sb.append("  ══════════════════════════╪══════════════════════════════\n");
        sb.append("                            │\n");
        sb.append("        ┌───────────────────┴─────────────────┐\n");
        sb.append("        │         HYPERVISOR                  │\n");
        sb.append("        │   Real ring 0 (or VMX root mode)    │\n");
        sb.append("        └─────────────────────────────────────┘\n\n");
        
        sb.append("THE SYSTEM CALL BOUNDARY:\n");
        sb.append("─────────────────────────────────────────────────────────────\n");
        sb.append("  Applications request OS services via system calls.\n");
        sb.append("  Traps to kernel mode, OS handles, returns to user mode.\n\n");
        
        sb.append("        ┌─────────────────────────────────────┐\n");
        sb.append("        │         APPLICATION                 │\n");
        sb.append("        │   User mode (ring 3)                │\n");
        sb.append("        └───────────────────┬─────────────────┘\n");
        sb.append("                            │ read(fd, buf, n)\n");
        sb.append("  ══════════════════════════╪══════════════════════════════\n");
        sb.append("                   SYSTEM CALL BOUNDARY\n");
        sb.append("      ECALL → trap to kernel → read from file → return\n");
        sb.append("  ══════════════════════════╪══════════════════════════════\n");
        sb.append("                            │\n");
        sb.append("        ┌───────────────────┴─────────────────┐\n");
        sb.append("        │         KERNEL                      │\n");
        sb.append("        │   Supervisor mode (ring 0)          │\n");
        sb.append("        └─────────────────────────────────────┘\n\n");
        
        return sb.toString();
    }
    
    /**
     * Show why abstraction enables innovation at each level.
     */
    public static String demonstrateEvolution() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  HOW ABSTRACTION ENABLES INDEPENDENT EVOLUTION\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("ANALOG LEVEL EVOLUTION:\n");
        sb.append("  10μm (1971) → 5nm (2023) → 2nm (coming)\n");
        sb.append("  Faster transistors, lower power, higher density\n");
        sb.append("  ★ Digital level unchanged! Same logic gates work.\n\n");
        
        sb.append("DIGITAL LEVEL EVOLUTION:\n");
        sb.append("  Static CMOS → Domino logic → Low-power techniques\n");
        sb.append("  ★ Microarchitecture unchanged! Same building blocks.\n\n");
        
        sb.append("MICROARCHITECTURE EVOLUTION:\n");
        sb.append("  Single-cycle → Pipeline → Superscalar → OoO\n");
        sb.append("  8086 (1978) → Core i9 (2023): 1000x faster, same x86 ISA!\n");
        sb.append("  ★ Software unchanged! Same programs run on new CPUs.\n\n");
        
        sb.append("ISA EVOLUTION:\n");
        sb.append("  CISC (x86) → RISC (ARM, RISC-V)\n");
        sb.append("  Extensions: SIMD (SSE, AVX), Vector (RVV)\n");
        sb.append("  ★ Old software runs! Binary compatibility preserved.\n\n");
        
        sb.append("VIRTUALIZATION EVOLUTION:\n");
        sb.append("  Software emulation → Binary translation → HW-assisted\n");
        sb.append("  Intel VT-x (2005) → AMD-V → ARM VHE → RISC-V H-extension\n");
        sb.append("  ★ Guest OS unchanged! Just runs faster.\n\n");
        
        sb.append("OS EVOLUTION:\n");
        sb.append("  Batch → Multitasking → Multiuser → Cloud-native\n");
        sb.append("  Containers (Docker) → Serverless (Lambda)\n");
        sb.append("  ★ Many applications unchanged! Same POSIX APIs.\n\n");
        
        sb.append("APPLICATION EVOLUTION:\n");
        sb.append("  Command line → GUI → Web → Mobile → AI-powered\n");
        sb.append("  ★ Lower layers unchanged! Same OS, same hardware.\n\n");
        
        return sb.toString();
    }
    
    /**
     * Main demonstration.
     */
    public static void main(String[] args) {
        System.out.println(generateHierarchy());
        System.out.println(demonstratePerspectives());
        System.out.println(demonstrateInterfaces());
        System.out.println(demonstrateEvolution());
    }
}

