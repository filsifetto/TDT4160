package computerdesign;

import computerdesign.alu.ALU;
import computerdesign.instruction.Instruction;
import computerdesign.memory.*;
import computerdesign.os.ProcessThread;
import computerdesign.os.Scheduler;
import computerdesign.processor.*;
import computerdesign.theory.*;
import computerdesign.vm.*;

/**
 * Main - Comprehensive demonstration of Computer Design concepts.
 * 
 * This program demonstrates all key concepts from TDT4160:
 * 
 * T1: Performance (Iron Law, Amdahl's Law, AMAT)
 * T2: Instructions (RISC-V, addressing modes, calling conventions)
 * T3: Single-cycle processor (combinational logic, ALU)
 * T4: Multi-cycle processor (sequential logic, FSM)
 * T5: Pipeline processor (hazards, forwarding, exceptions)
 * T6: Memory system (cache, virtual memory, TLB)
 * T7: Parallel computing (Flynn's taxonomy, Roofline, coherence)
 * 
 * Run this to understand everything needed for the exam!
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  TDT4160 - Computer Organization and Design - OOP Model      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Check for specific topic argument
        if (args.length > 0) {
            runSpecificDemo(args[0]);
            return;
        }
        
        // Run all demonstrations
        demonstrateAbstractionLevels();  // NEW: Show the complete hierarchy
        demonstratePerformance();
        demonstrateNumberSystems();
        demonstrateDigitalLogic();
        demonstrateMemoryHierarchy();
        demonstrateTLB();
        demonstrateALU();
        demonstrateInstructions();
        demonstrateCallingConventions();
        demonstrateProcessors();
        demonstrateExceptions();
        demonstrateVirtualMemory();
        demonstrateVirtualMachines();     // NEW: Virtual machines & hypervisor
        demonstrateParallelComputing();
        demonstrateProcessAndThreads();
        
        System.out.println("\n✓ All demonstrations complete!");
        System.out.println("\nTip: Run with argument to see specific topic:");
        System.out.println("  java computerdesign.Main abstraction");
        System.out.println("  java computerdesign.Main vm");
        System.out.println("  java computerdesign.Main performance");
        System.out.println("  java computerdesign.Main numbers");
        System.out.println("  java computerdesign.Main logic");
        System.out.println("  java computerdesign.Main parallel");
    }
    
    private static void runSpecificDemo(String topic) {
        switch (topic.toLowerCase()) {
            case "abstraction": demonstrateAbstractionLevels(); break;
            case "vm": demonstrateVirtualMachines(); break;
            case "performance": demonstratePerformance(); break;
            case "numbers": demonstrateNumberSystems(); break;
            case "logic": demonstrateDigitalLogic(); break;
            case "memory": demonstrateMemoryHierarchy(); break;
            case "tlb": demonstrateTLB(); break;
            case "alu": demonstrateALU(); break;
            case "instructions": demonstrateInstructions(); break;
            case "calling": demonstrateCallingConventions(); break;
            case "processors": demonstrateProcessors(); break;
            case "exceptions": demonstrateExceptions(); break;
            case "virtual": demonstrateVirtualMemory(); break;
            case "parallel": demonstrateParallelComputing(); break;
            case "threads": demonstrateProcessAndThreads(); break;
            default:
                System.out.println("Unknown topic: " + topic);
                System.out.println("Available: abstraction, vm, performance, numbers, logic,");
                System.out.println("           memory, tlb, alu, instructions, calling,");
                System.out.println("           processors, exceptions, virtual, parallel, threads");
        }
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // T1: PERFORMANCE
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Demonstrate performance concepts: Iron Law, Amdahl's Law, AMAT.
     * Covers: T1.3, T1.4, T6.1
     */
    private static void demonstratePerformance() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T1: PERFORMANCE METRICS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println(Performance.demonstrateIronLaw());
        System.out.println(Performance.amdahlTable(0.90));
        System.out.println(Performance.demonstrateAMAT());
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // T2: NUMBER SYSTEMS
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Demonstrate number representations: binary, hex, 2's complement.
     * Covers: T2.2
     */
    private static void demonstrateNumberSystems() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T2.2: NUMBER SYSTEMS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println("Base Conversion:");
        System.out.println("  42 decimal = " + NumberSystems.decimalToBinary(42) + " binary");
        System.out.println("  42 decimal = 0x" + NumberSystems.decimalToHex(42) + " hex");
        System.out.println("  0xFF hex = " + NumberSystems.hexToDecimal("FF") + " decimal");
        System.out.println();
        
        System.out.println(NumberSystems.demonstrateTwosComplement(5, 8));
        System.out.println(NumberSystems.demonstrateSignExtension(0b00000101, 8, 16));
        System.out.println(NumberSystems.demonstrateOverflow());
        System.out.println(NumberSystems.demonstrateBitwise());
        System.out.println(NumberSystems.floatComponents(12.375f));
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // T3/T4: DIGITAL LOGIC
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Demonstrate digital logic: gates, latches, flip-flops.
     * Covers: T3.2, T4.2
     */
    private static void demonstrateDigitalLogic() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T3.2/T4.2: DIGITAL LOGIC");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println(DigitalLogic.generateTruthTables());
        System.out.println(DigitalLogic.demonstrateFullAdder());
        System.out.println(DigitalLogic.demonstrateRippleCarryAdder(12, 7, 8));
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // T2.3: CALLING CONVENTIONS
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Demonstrate calling conventions and memory layout.
     * Covers: T2.3, T2.4
     */
    private static void demonstrateCallingConventions() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T2.3/T2.4: CALLING CONVENTIONS & MEMORY LAYOUT");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println(CallingConvention.demonstrateFunctionCall());
        System.out.println(CallingConvention.demonstrateMemoryLayout());
        System.out.println(CallingConvention.demonstrateAddressingModes());
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // T5.2: EXCEPTIONS
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Demonstrate exception handling in pipeline.
     * Covers: T5.2
     */
    private static void demonstrateExceptions() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T5.2: EXCEPTIONS AND INTERRUPTS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println(ExceptionHandler.demonstratePipelineException());
        
        // Demo exception handling
        ExceptionHandler handler = new ExceptionHandler(0x80000000);
        
        System.out.println("\nSimulating page fault:");
        int handlerAddr = handler.handleException(
            ExceptionHandler.ExceptionType.LOAD_PAGE_FAULT, 
            0x00010100,
            0xDEADBEEF
        );
        
        System.out.printf("  Handler at: 0x%08X\n", handlerAddr);
        System.out.printf("  mepc: 0x%08X (return here after handling)\n", handler.getMepc());
        System.out.printf("  mtval: 0x%08X (the bad address)\n", handler.getMtval());
        
        int returnPC = handler.returnFromException();
        System.out.printf("  After MRET, returning to: 0x%08X\n", returnPC);
        System.out.println();
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // T6: TLB
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Demonstrate TLB operation.
     * Covers: T6.3
     */
    private static void demonstrateTLB() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T6.3: TRANSLATION LOOKASIDE BUFFER (TLB)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        TLB tlb = new TLB(16, 4096);  // 16 entries, 4KB pages
        
        System.out.println("Created TLB with 16 entries, 4KB pages");
        System.out.println();
        
        // Add some translations
        tlb.insert(0x00001, 0x00010, true, false, true, false);  // VPN 1 → PFN 16
        tlb.insert(0x00002, 0x00020, true, true, false, false);  // VPN 2 → PFN 32
        
        // Lookup
        int virtualAddr1 = 0x00001000 + 100;  // Page 1, offset 100
        TLB.TLBResult result1 = tlb.lookup(virtualAddr1);
        
        System.out.println("TLB Lookup Demo:");
        System.out.printf("  Virtual 0x%08X: %s, Physical 0x%08X\n", 
            virtualAddr1, result1.hit ? "HIT" : "MISS", result1.physicalAddress);
        
        // Miss case
        int virtualAddr2 = 0x00003000;  // Not in TLB
        TLB.TLBResult result2 = tlb.lookup(virtualAddr2);
        System.out.printf("  Virtual 0x%08X: %s (would trigger page table walk)\n",
            virtualAddr2, result2.hit ? "HIT" : "MISS");
        
        System.out.println();
        System.out.println(tlb);
        System.out.println();
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // T7: PARALLEL COMPUTING
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Demonstrate parallel computing concepts.
     * Covers: T7.1, T7.2, T7.3
     */
    private static void demonstrateParallelComputing() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T7: PARALLEL COMPUTING");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println(ParallelComputing.demonstrateFlynn());
        System.out.println(ParallelComputing.demonstrateAmdahl());
        System.out.println(ParallelComputing.demonstrateRoofline());
        System.out.println(ParallelComputing.demonstrateCacheCoherence());
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // EXISTING DEMOS (enhanced)
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Demonstrate the memory hierarchy: Registers → Cache → Main Memory
     */
    private static void demonstrateMemoryHierarchy() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T6.1: MEMORY HIERARCHY");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        // Create memory hierarchy
        MainMemory ram = new MainMemory(16 * 1024);
        
        // Create 4-way set-associative L1 cache
        Cache l1Cache = new Cache("L1", 1024, 32, 4, ram,
            Cache.Associativity.FOUR_WAY, Cache.ReplacementPolicy.LRU);
        
        System.out.println("Memory Hierarchy Created:");
        System.out.println("  • Main Memory: " + ram.getSize() + " bytes, " + ram.getAccessTime() + " cycle access");
        System.out.println("  • L1 Cache: " + l1Cache.getSize() + " bytes, 4-way set-associative");
        System.out.println("    - " + l1Cache.getNumSets() + " sets × " + l1Cache.getNumWays() + " ways");
        System.out.println("    - Access time: " + l1Cache.getAccessTime() + " cycles");
        System.out.println();
        
        // Create register file
        RegisterFile registers = new RegisterFile();
        System.out.println("Register File Created (32 registers):");
        System.out.println("  • Access time: " + registers.getAccessTime() + " cycles (same cycle!)");
        System.out.println();
        
        // Demonstrate register operations
        System.out.println("Register Operations:");
        registers.write(10, 42);
        registers.write(11, 100);
        System.out.println("  Write: a0 = 42, a1 = 100");
        System.out.println("  Read:  a0 = " + registers.read(10) + ", a1 = " + registers.read(11));
        System.out.println("  Note: x0 (zero) always reads 0: " + registers.read(0));
        registers.write(0, 999);
        System.out.println("  After writing 999 to x0: " + registers.read(0) + " (unchanged!)");
        System.out.println();
        
        // Demonstrate cache behavior with AMAT
        System.out.println("Cache Behavior (with AMAT calculation):");
        
        for (int i = 0; i < 10; i++) {
            l1Cache.write(i * 4, i * 10);
        }
        
        System.out.println("  First read of addresses 0-36 (cold cache):");
        System.out.println("    Hits: " + l1Cache.getHits() + ", Misses: " + l1Cache.getMisses());
        
        for (int i = 0; i < 10; i++) {
            l1Cache.read(i * 4);
        }
        System.out.println("  Second read (warm cache):");
        System.out.println("    Hits: " + l1Cache.getHits() + ", Misses: " + l1Cache.getMisses());
        System.out.println("    Hit Rate: " + String.format("%.1f%%", l1Cache.getHitRate() * 100));
        System.out.println("    AMAT: " + String.format("%.2f cycles", l1Cache.calculateAMAT()));
        System.out.println();
        
        // Show AMAT formula
        System.out.println("AMAT Calculation:");
        System.out.println("  AMAT = Hit Time + Miss Rate × Miss Penalty");
        System.out.println("       = " + l1Cache.getAccessTime() + " + " + 
            String.format("%.2f", l1Cache.getMissRate()) + " × " + ram.getAccessTime());
        System.out.println("       = " + String.format("%.2f cycles", l1Cache.calculateAMAT()));
        System.out.println();
    }
    
    /**
     * Demonstrate the ALU (Arithmetic Logic Unit).
     */
    private static void demonstrateALU() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T3.3: ALU (ARITHMETIC LOGIC UNIT)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        ALU alu = new ALU();
        
        System.out.println("The ALU is COMBINATIONAL - no state, just computes!");
        System.out.println();
        
        System.out.println("Arithmetic Operations:");
        System.out.println("  15 + 27 = " + alu.compute(15, 27, ALU.Operation.ADD));
        System.out.println("  100 - 42 = " + alu.compute(100, 42, ALU.Operation.SUB));
        System.out.println("  7 * 8 = " + alu.compute(7, 8, ALU.Operation.MUL));
        System.out.println("  100 / 7 = " + alu.compute(100, 7, ALU.Operation.DIV));
        System.out.println();
        
        System.out.println("Logical Operations:");
        System.out.println("  0xFF & 0x0F = 0x" + Integer.toHexString(alu.compute(0xFF, 0x0F, ALU.Operation.AND)));
        System.out.println("  0xF0 | 0x0F = 0x" + Integer.toHexString(alu.compute(0xF0, 0x0F, ALU.Operation.OR)));
        System.out.println("  0xFF ^ 0x0F = 0x" + Integer.toHexString(alu.compute(0xFF, 0x0F, ALU.Operation.XOR)));
        System.out.println();
        
        System.out.println("Shift Operations:");
        System.out.println("  1 << 4 = " + alu.compute(1, 4, ALU.Operation.SLL));
        System.out.println("  256 >> 4 = " + alu.compute(256, 4, ALU.Operation.SRL));
        System.out.println("  -16 >> 2 (arithmetic) = " + alu.compute(-16, 2, ALU.Operation.SRA));
        System.out.println();
        
        System.out.println("Comparisons (for branching):");
        System.out.println("  5 < 10? " + alu.compare(5, 10, ALU.BranchCondition.LT));
        System.out.println("  5 == 5? " + alu.compare(5, 5, ALU.BranchCondition.EQ));
        System.out.println("  10 >= 5? " + alu.compare(10, 5, ALU.BranchCondition.GE));
        System.out.println();
    }
    
    /**
     * Demonstrate instruction encoding and decoding.
     */
    private static void demonstrateInstructions() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T2.1: RISC-V INSTRUCTIONS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println("PATTERSON & HENNESSY'S THREE PRINCIPLES:");
        System.out.println("  1. Simplicity favours regularity");
        System.out.println("  2. Smaller is faster");
        System.out.println("  3. Good design demands good compromises");
        System.out.println();
        
        System.out.println("Instructions are just 32 bits of data!");
        System.out.println("The control unit decodes them to determine what to do.");
        System.out.println();
        
        Instruction add = Instruction.add(10, 11, 12);
        Instruction addi = Instruction.addi(10, 11, 5);
        Instruction lw = Instruction.lw(10, 11, 8);
        Instruction sw = Instruction.sw(10, 11, 12);
        Instruction beq = Instruction.beq(10, 11, 16);
        
        System.out.println("Instruction Encoding → Disassembly:");
        System.out.println("  " + add);
        System.out.println("  " + addi);
        System.out.println("  " + lw);
        System.out.println("  " + sw);
        System.out.println("  " + beq);
        System.out.println();
        
        System.out.println("Instruction Type Analysis:");
        System.out.println("  add:  Type=" + add.getType() + " (Register-Register)");
        System.out.println("  addi: Type=" + addi.getType() + " (Immediate), imm=" + addi.getImmediate());
        System.out.println("  lw:   Type=" + lw.getType() + " (Load), offset=" + lw.getImmediate());
        System.out.println("  sw:   Type=" + sw.getType() + " (Store), offset=" + sw.getImmediate());
        System.out.println("  beq:  Type=" + beq.getType() + " (Branch), offset=" + beq.getImmediate());
        System.out.println();
    }
    
    /**
     * Demonstrate different processor architectures.
     */
    private static void demonstrateProcessors() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T3/T4/T5: PROCESSOR ARCHITECTURES");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        // Simple program: sum 1 to 5
        int[] program = {
            Instruction.addi(10, 0, 0).getRaw(),
            Instruction.addi(11, 0, 1).getRaw(),
            Instruction.addi(12, 0, 6).getRaw(),
            Instruction.add(10, 10, 11).getRaw(),
            Instruction.addi(11, 11, 1).getRaw(),
            Instruction.rType(0b0110011, 13, 0b010, 11, 12, 0).getRaw(),
            Instruction.iType(0b1100011, 0, 0b001, 13, -12).getRaw(),
            0x00100073
        };
        
        System.out.println("Test Program: Sum of 1 to 5 (expected result: 15)");
        System.out.println();
        
        // Single-cycle
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│ SINGLE-CYCLE PROCESSOR                                      │");
        System.out.println("│ CPI = 1, but clock period limited by longest instruction    │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        SingleCycleProcessor singleCycle = new SingleCycleProcessor();
        singleCycle.getMemory().loadProgram(program, 0);
        singleCycle.run(1000);
        System.out.println("  Result (a0): " + singleCycle.getRegisterFile().read(10));
        System.out.println("  " + singleCycle.getStats());
        System.out.println();
        
        // Multi-cycle
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│ MULTI-CYCLE PROCESSOR                                       │");
        System.out.println("│ CPI > 1, but shorter clock period (faster cycle)            │");
        System.out.println("│ Control unit is a FINITE STATE MACHINE                      │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        MultiCycleProcessor multiCycle = new MultiCycleProcessor();
        multiCycle.getMemory().loadProgram(program, 0);
        multiCycle.run(1000);
        System.out.println("  Result (a0): " + multiCycle.getRegisterFile().read(10));
        System.out.println("  " + multiCycle.getStats());
        System.out.println();
        
        // Pipeline
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│ PIPELINED PROCESSOR                                         │");
        System.out.println("│ CPI → 1 (with hazard handling), short clock period          │");
        System.out.println("│ Best throughput, but requires forwarding and stalling       │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        PipelineProcessor pipeline = new PipelineProcessor();
        pipeline.getMemory().loadProgram(program, 0);
        pipeline.run(1000);
        System.out.println("  Result (a0): " + pipeline.getRegisterFile().read(10));
        System.out.println("  " + pipeline.getStats());
        System.out.println();
        
        System.out.println("IRON LAW: CPU Time = Instructions × CPI × Clock Period");
        System.out.println("  Single-Cycle: Low CPI (1), but long clock period");
        System.out.println("  Multi-Cycle:  Variable CPI, shorter clock period");
        System.out.println("  Pipeline:     CPI near 1, short clock period = BEST!");
        System.out.println();
    }
    
    /**
     * Demonstrate virtual memory concepts.
     */
    private static void demonstrateVirtualMemory() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  T6.3: VIRTUAL MEMORY");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println("KEY INSIGHT:");
        System.out.println("  • Physical Memory (RAM) = HARDWARE state (shared)");
        System.out.println("  • Page Table = PROCESS state (per-process)");
        System.out.println();
        
        MainMemory physicalMemory = new MainMemory(64 * 1024);
        
        System.out.println("Physical Memory (Hardware State):");
        System.out.println("  Size: " + physicalMemory.getSize() + " bytes");
        System.out.println("  Frame Size: " + physicalMemory.getFrameSize() + " bytes");
        System.out.println("  Total Frames: " + physicalMemory.getTotalFrames());
        System.out.println();
        
        computerdesign.os.Process processA = new computerdesign.os.Process(100, "Process_A", physicalMemory);
        computerdesign.os.Process processB = new computerdesign.os.Process(200, "Process_B", physicalMemory);
        
        PageTable ptA = processA.getPageTable();
        PageTable ptB = processB.getPageTable();
        
        int frameForA = physicalMemory.allocateFrame();
        ptA.mapPage(0x00400, frameForA, true, false, true);
        
        int frameForB = physicalMemory.allocateFrame();
        ptB.mapPage(0x00400, frameForB, true, false, true);
        
        System.out.println("Process Isolation Demo:");
        System.out.println("  Both processes use virtual address 0x00400000, but:");
        System.out.printf("  Process A → Physical Frame %d\n", frameForA);
        System.out.printf("  Process B → Physical Frame %d\n", frameForB);
        System.out.println("  (Different physical addresses = ISOLATION!)");
        System.out.println();
        
        int virtualAddr = 0x00400000 + 100;
        int physAddrA = ptA.translate(virtualAddr);
        int physAddrB = ptB.translate(virtualAddr);
        
        physicalMemory.writePhysical(physAddrA, 42);
        physicalMemory.writePhysical(physAddrB, 99);
        
        System.out.println("  Process A writes 42, Process B writes 99 to same virtual addr");
        System.out.println("  Process A reads: " + physicalMemory.readPhysical(physAddrA));
        System.out.println("  Process B reads: " + physicalMemory.readPhysical(physAddrB));
        System.out.println("  (Each process sees its own value!)");
        System.out.println();
    }
    
    /**
     * Demonstrate process and thread concepts.
     */
    private static void demonstrateProcessAndThreads() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  OS: PROCESSES AND THREADS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        Scheduler scheduler = new Scheduler(Scheduler.Algorithm.ROUND_ROBIN, 50);
        
        computerdesign.os.Process shell = new computerdesign.os.Process(1, "shell");
        computerdesign.os.Process editor = new computerdesign.os.Process(2, "editor");
        computerdesign.os.Process compiler = new computerdesign.os.Process(3, "compiler");
        
        System.out.println("Created Processes:");
        System.out.println("  " + shell);
        System.out.println("  " + editor);
        System.out.println("  " + compiler);
        System.out.println();
        
        shell.setPriority(5);
        editor.setPriority(5);
        compiler.setPriority(15);
        
        shell.setReady();
        editor.setReady();
        compiler.setReady();
        scheduler.addProcess(shell);
        scheduler.addProcess(editor);
        scheduler.addProcess(compiler);
        
        ProcessThread mainThread = editor.getMainThread();
        ProcessThread spellCheck = editor.createThread(1, "spell-checker", 0x1000);
        ProcessThread autoSave = editor.createThread(2, "auto-save", 0x2000);
        
        System.out.println("Editor Process Threads:");
        System.out.println("  " + mainThread);
        System.out.println("  " + spellCheck);
        System.out.println("  " + autoSave);
        System.out.println();
        
        System.out.println("Round-Robin Scheduling (quantum = 50 cycles):");
        computerdesign.os.Process current = scheduler.schedule();
        current.setRunning();
        
        int contextSwitches = 0;
        for (int i = 0; i < 200; i++) {
            boolean preempt = scheduler.tick();
            if (preempt) {
                computerdesign.os.Process next = scheduler.schedule();
                if (next != null) {
                    scheduler.contextSwitch(current, next);
                    current = next;
                    contextSwitches++;
                }
            }
        }
        System.out.println("  Context switches in 200 cycles: " + contextSwitches);
        System.out.println();
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // ABSTRACTION LEVELS
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Demonstrate the complete computer abstraction hierarchy.
     * From analog electronics to applications.
     */
    private static void demonstrateAbstractionLevels() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  COMPUTER ABSTRACTION LEVELS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println("THE SEVEN LEVELS OF ABSTRACTION:");
        System.out.println();
        
        for (AbstractionLevels.Level level : AbstractionLevels.Level.values()) {
            String marker = (level == AbstractionLevels.Level.VIRTUAL_MACHINE) ? " ★" : "";
            System.out.printf("  Level %d: %s%s\n", level.level, level.name, marker);
            System.out.printf("           %s\n", level.description);
        }
        
        System.out.println();
        System.out.println("KEY INSIGHT: Each level hides complexity from the level above!");
        System.out.println("  • Applications don't know about cache misses");
        System.out.println("  • OS doesn't know about pipeline stalls");
        System.out.println("  • ISA doesn't know about transistor sizes");
        System.out.println();
        
        System.out.println(AbstractionLevels.demonstratePerspectives());
        System.out.println(AbstractionLevels.demonstrateInterfaces());
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // VIRTUAL MACHINES
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Demonstrate virtual machine concepts and hypervisor operation.
     */
    private static void demonstrateVirtualMachines() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  VIRTUAL MACHINES AND HYPERVISOR");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println(Hypervisor.demonstrateVirtualization());
        
        // Create a hypervisor
        System.out.println("LIVE DEMONSTRATION:\n");
        Hypervisor hypervisor = new Hypervisor("TDT4160-VMM", 
                                                Hypervisor.Type.TYPE_1_BARE_METAL, 
                                                1024 * 1024);  // 1 MB
        
        System.out.println("Created Hypervisor:");
        System.out.println("  Name: " + hypervisor.getName());
        System.out.println("  Type: " + hypervisor.getType().name);
        System.out.println("  Physical Memory: " + hypervisor.getTotalMemory() / 1024 + " KB");
        System.out.println();
        
        // Create virtual machines
        VirtualMachine vm1 = hypervisor.createVM("Ubuntu-VM", 256 * 1024);    // 256 KB
        VirtualMachine vm2 = hypervisor.createVM("Windows-VM", 256 * 1024);   // 256 KB
        VirtualMachine vm3 = hypervisor.createVM("FreeBSD-VM", 128 * 1024);   // 128 KB
        
        System.out.println("Created Virtual Machines:");
        System.out.println("  " + vm1);
        System.out.println("  " + vm2);
        System.out.println("  " + vm3);
        System.out.println();
        
        System.out.println("Memory Allocation:");
        System.out.printf("  Allocated: %d KB / %d KB (%.1f%%)\n",
            hypervisor.getAllocatedMemory() / 1024,
            hypervisor.getTotalMemory() / 1024,
            hypervisor.getAllocatedMemory() * 100.0 / hypervisor.getTotalMemory());
        System.out.printf("  Free: %d KB\n", hypervisor.getFreeMemory() / 1024);
        System.out.println();
        
        // Load a simple program into VM1 (address must be within VM's memory)
        int[] simpleProgram = {
            // addi x10, x0, 5    (a0 = 5)
            0x00500513,
            // addi x11, x0, 7    (a1 = 7)
            0x00700593,
            // add  x12, x10, x11 (a2 = a0 + a1 = 12)
            0x00B50633,
            // ecall              (hypercall - exit)
            0x00000073
        };
        vm1.loadProgram(simpleProgram, 0x1000);  // Load at 4KB offset
        
        System.out.println("Loaded program into " + vm1.getName() + ":");
        System.out.println("  addi a0, zero, 5   # a0 = 5");
        System.out.println("  addi a1, zero, 7   # a1 = 7");
        System.out.println("  add  a2, a0, a1    # a2 = a0 + a1 = 12");
        System.out.println("  ecall              # Exit via hypercall");
        System.out.println();
        
        // Start and run VMs
        hypervisor.startVM(vm1.getVmId());
        hypervisor.startVM(vm2.getVmId());
        hypervisor.startVM(vm3.getVmId());
        
        System.out.println("Running VMs with time-slicing...");
        hypervisor.setTimeQuantum(100);
        hypervisor.run(500);
        
        System.out.println();
        System.out.println("VM1 Execution Result:");
        VirtualMachine.VirtualCPU vcpu = vm1.getVCPU();
        System.out.println("  a0 (x10) = " + vcpu.getRegister(10));
        System.out.println("  a1 (x11) = " + vcpu.getRegister(11));
        System.out.println("  a2 (x12) = " + vcpu.getRegister(12) + " (5 + 7 = 12 ✓)");
        System.out.println();
        
        // Show hypervisor stats
        System.out.println(hypervisor.getStats());
        System.out.println();
        
        // Show how VM sees vs reality
        System.out.println("ISOLATION IN ACTION:");
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  VM's Perspective         │  Reality                         │");
        System.out.println("├──────────────────────────────────────────────────────────────┤");
        System.out.println("│  \"I have my own CPU\"     │  Sharing with other VMs          │");
        System.out.println("│  \"256 KB RAM is mine\"    │  It's actually host memory       │");
        System.out.println("│  \"Running in ring 0\"     │  Actually in VMX non-root mode   │");
        System.out.println("│  \"Direct hardware access\"│  Emulated by hypervisor          │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
    }
}
