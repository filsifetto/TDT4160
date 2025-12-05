package computerdesign;

import computerdesign.alu.ALU;
import computerdesign.instruction.Instruction;
import computerdesign.memory.*;
import computerdesign.os.ProcessThread;
import computerdesign.os.Scheduler;
import computerdesign.processor.*;

/**
 * Main - Demonstration of the OOP Computer Design Model.
 * 
 * This program demonstrates how computer architecture concepts can be
 * understood through object-oriented programming. Each component is a
 * class with clear state and behavior.
 * 
 * Run this to see:
 * - Memory hierarchy in action
 * - Different processor architectures compared
 * - How instructions are executed
 * - Process and thread management
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║     Computer Organization and Design - OOP Perspective       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Run all demonstrations
        demonstrateMemoryHierarchy();
        demonstrateALU();
        demonstrateInstructions();
        demonstrateProcessors();
        demonstrateProcessAndThreads();
        
        System.out.println("\n✓ All demonstrations complete!");
    }
    
    /**
     * Demonstrate the memory hierarchy: Registers → Cache → Main Memory
     */
    private static void demonstrateMemoryHierarchy() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  MEMORY HIERARCHY DEMONSTRATION");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        // Create memory hierarchy
        MainMemory ram = new MainMemory(16 * 1024);  // 16KB RAM
        Cache l1Cache = new Cache("L1", 1024, 32, 4, ram);  // 1KB L1, 32B blocks, 4 cycle access
        
        System.out.println("Memory Hierarchy Created:");
        System.out.println("  • Main Memory: " + ram.getSize() + " bytes, " + ram.getAccessTime() + " cycle access");
        System.out.println("  • L1 Cache: " + l1Cache.getSize() + " bytes, " + l1Cache.getAccessTime() + " cycle access");
        System.out.println();
        
        // Create register file
        RegisterFile registers = new RegisterFile();
        System.out.println("Register File Created (32 registers):");
        System.out.println("  • Access time: " + registers.getAccessTime() + " cycles (same cycle!)");
        System.out.println();
        
        // Demonstrate register operations
        System.out.println("Register Operations:");
        registers.write(10, 42);     // a0 = 42
        registers.write(11, 100);    // a1 = 100
        System.out.println("  Write: a0 = 42, a1 = 100");
        System.out.println("  Read:  a0 = " + registers.read(10) + ", a1 = " + registers.read(11));
        System.out.println("  Note: x0 (zero) always reads 0: " + registers.read(0));
        registers.write(0, 999);  // Try to write to x0
        System.out.println("  After writing 999 to x0: " + registers.read(0) + " (unchanged!)");
        System.out.println();
        
        // Demonstrate cache behavior
        System.out.println("Cache Behavior (Locality):");
        
        // Write some data to memory through cache
        for (int i = 0; i < 10; i++) {
            l1Cache.write(i * 4, i * 10);
        }
        
        // Read data - should hit cache
        System.out.println("  First read of addresses 0-36 (cold cache):");
        System.out.println("    Hits: " + l1Cache.getHits() + ", Misses: " + l1Cache.getMisses());
        
        // Read again - should be cached
        for (int i = 0; i < 10; i++) {
            l1Cache.read(i * 4);
        }
        System.out.println("  Second read (warm cache):");
        System.out.println("    Hits: " + l1Cache.getHits() + ", Misses: " + l1Cache.getMisses());
        System.out.println("    Hit Rate: " + String.format("%.1f%%", l1Cache.getHitRate() * 100));
        System.out.println();
    }
    
    /**
     * Demonstrate the ALU (Arithmetic Logic Unit).
     */
    private static void demonstrateALU() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  ALU (ARITHMETIC LOGIC UNIT) DEMONSTRATION");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        ALU alu = new ALU();
        
        System.out.println("The ALU is COMBINATIONAL - no state, just computes!");
        System.out.println();
        
        // Arithmetic operations
        System.out.println("Arithmetic Operations:");
        System.out.println("  15 + 27 = " + alu.compute(15, 27, ALU.Operation.ADD));
        System.out.println("  100 - 42 = " + alu.compute(100, 42, ALU.Operation.SUB));
        System.out.println("  7 * 8 = " + alu.compute(7, 8, ALU.Operation.MUL));
        System.out.println("  100 / 7 = " + alu.compute(100, 7, ALU.Operation.DIV));
        System.out.println("  100 % 7 = " + alu.compute(100, 7, ALU.Operation.REM));
        System.out.println();
        
        // Logical operations
        System.out.println("Logical Operations:");
        System.out.println("  0xFF & 0x0F = 0x" + Integer.toHexString(alu.compute(0xFF, 0x0F, ALU.Operation.AND)));
        System.out.println("  0xF0 | 0x0F = 0x" + Integer.toHexString(alu.compute(0xF0, 0x0F, ALU.Operation.OR)));
        System.out.println("  0xFF ^ 0x0F = 0x" + Integer.toHexString(alu.compute(0xFF, 0x0F, ALU.Operation.XOR)));
        System.out.println();
        
        // Shift operations
        System.out.println("Shift Operations:");
        System.out.println("  1 << 4 = " + alu.compute(1, 4, ALU.Operation.SLL));
        System.out.println("  256 >> 4 = " + alu.compute(256, 4, ALU.Operation.SRL));
        System.out.println("  -16 >> 2 (arithmetic) = " + alu.compute(-16, 2, ALU.Operation.SRA));
        System.out.println();
        
        // Comparison operations (for branches)
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
        System.out.println("  RISC-V INSTRUCTION DEMONSTRATION");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        System.out.println("Instructions are just 32 bits of data!");
        System.out.println("The control unit decodes them to determine what to do.");
        System.out.println();
        
        // Create some instructions
        Instruction add = Instruction.add(10, 11, 12);    // add a0, a1, a2
        Instruction addi = Instruction.addi(10, 11, 5);   // addi a0, a1, 5
        Instruction lw = Instruction.lw(10, 11, 8);       // lw a0, 8(a1)
        Instruction sw = Instruction.sw(10, 11, 12);      // sw a0, 12(a1)
        Instruction beq = Instruction.beq(10, 11, 16);    // beq a0, a1, 16
        
        System.out.println("Instruction Encoding → Disassembly:");
        System.out.println("  " + add);
        System.out.println("  " + addi);
        System.out.println("  " + lw);
        System.out.println("  " + sw);
        System.out.println("  " + beq);
        System.out.println();
        
        System.out.println("Instruction Type Analysis:");
        System.out.println("  add:  Type=" + add.getType() + ", opcode=" + String.format("0x%02X", add.getOpcode()));
        System.out.println("  addi: Type=" + addi.getType() + ", opcode=" + String.format("0x%02X", addi.getOpcode()) + ", imm=" + addi.getImmediate());
        System.out.println("  lw:   Type=" + lw.getType() + ", opcode=" + String.format("0x%02X", lw.getOpcode()) + ", imm=" + lw.getImmediate());
        System.out.println();
    }
    
    /**
     * Demonstrate different processor architectures.
     */
    private static void demonstrateProcessors() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  PROCESSOR ARCHITECTURE COMPARISON");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        // Create a simple program: sum numbers 1 to 5
        // a0 = result (sum)
        // a1 = counter
        // a2 = limit (5)
        int[] program = {
            Instruction.addi(10, 0, 0).getRaw(),    // addi a0, zero, 0  (sum = 0)
            Instruction.addi(11, 0, 1).getRaw(),    // addi a1, zero, 1  (counter = 1)
            Instruction.addi(12, 0, 6).getRaw(),    // addi a2, zero, 6  (limit = 6)
            // Loop:
            Instruction.add(10, 10, 11).getRaw(),   // add a0, a0, a1    (sum += counter)
            Instruction.addi(11, 11, 1).getRaw(),   // addi a1, a1, 1    (counter++)
            Instruction.rType(0b0110011, 13, 0b010, 11, 12, 0).getRaw(), // slt a3, a1, a2
            Instruction.iType(0b1100011, 0, 0b001, 13, -12).getRaw(),  // bne a3, zero, -12
            0x00100073  // ebreak (halt)
        };
        
        System.out.println("Test Program: Sum of 1 to 5 (expected result: 15)");
        System.out.println();
        
        // Test single-cycle processor
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│ SINGLE-CYCLE PROCESSOR                                      │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        SingleCycleProcessor singleCycle = new SingleCycleProcessor();
        singleCycle.getMemory().loadProgram(program, 0);
        int cycles1 = singleCycle.run(1000);
        System.out.println("  Result (a0): " + singleCycle.getRegisterFile().read(10));
        System.out.println("  " + singleCycle.getStats());
        System.out.println();
        
        // Test multi-cycle processor
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│ MULTI-CYCLE PROCESSOR                                       │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        MultiCycleProcessor multiCycle = new MultiCycleProcessor();
        multiCycle.getMemory().loadProgram(program, 0);
        int cycles2 = multiCycle.run(1000);
        System.out.println("  Result (a0): " + multiCycle.getRegisterFile().read(10));
        System.out.println("  " + multiCycle.getStats());
        System.out.println();
        
        // Test pipelined processor
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│ PIPELINED PROCESSOR                                         │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        PipelineProcessor pipeline = new PipelineProcessor();
        pipeline.getMemory().loadProgram(program, 0);
        int cycles3 = pipeline.run(1000);
        System.out.println("  Result (a0): " + pipeline.getRegisterFile().read(10));
        System.out.println("  " + pipeline.getStats());
        System.out.println();
        
        // Summary
        System.out.println("COMPARISON SUMMARY:");
        System.out.println("  Single-Cycle: CPI = 1, but long clock period");
        System.out.println("  Multi-Cycle:  CPI > 1, but shorter clock period");
        System.out.println("  Pipeline:     CPI → 1, with short clock period (BEST!)");
        System.out.println();
    }
    
    /**
     * Demonstrate process and thread concepts.
     */
    private static void demonstrateProcessAndThreads() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  PROCESS AND THREAD DEMONSTRATION");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        // Create a scheduler
        Scheduler scheduler = new Scheduler(Scheduler.Algorithm.ROUND_ROBIN, 50);
        
        // Create some processes
        computerdesign.os.Process shell = new computerdesign.os.Process(1, "shell");
        computerdesign.os.Process editor = new computerdesign.os.Process(2, "editor");
        computerdesign.os.Process compiler = new computerdesign.os.Process(3, "compiler");
        
        System.out.println("Created Processes:");
        System.out.println("  " + shell);
        System.out.println("  " + editor);
        System.out.println("  " + compiler);
        System.out.println();
        
        // Set different priorities
        shell.setPriority(5);      // Higher priority (interactive)
        editor.setPriority(5);     // Higher priority (interactive)
        compiler.setPriority(15);  // Lower priority (background job)
        
        // Add to scheduler
        shell.setReady();
        editor.setReady();
        compiler.setReady();
        scheduler.addProcess(shell);
        scheduler.addProcess(editor);
        scheduler.addProcess(compiler);
        
        // Create threads in editor process
        ProcessThread mainThread = editor.getMainThread();
        ProcessThread spellCheck = editor.createThread(1, "spell-checker", 0x1000);
        ProcessThread autoSave = editor.createThread(2, "auto-save", 0x2000);
        
        System.out.println("Editor Process Threads:");
        System.out.println("  " + mainThread);
        System.out.println("  " + spellCheck);
        System.out.println("  " + autoSave);
        System.out.println();
        
        // Simulate scheduling
        System.out.println("Simulating Round-Robin Scheduling:");
        computerdesign.os.Process current = scheduler.schedule();
        current.setRunning();
        
        for (int i = 0; i < 200; i++) {
            boolean preempt = scheduler.tick();
            if (preempt) {
                System.out.println("  Cycle " + i + ": Preempting " + current.getName());
                computerdesign.os.Process next = scheduler.schedule();
                if (next != null) {
                    scheduler.contextSwitch(current, next);
                    current = next;
                    System.out.println("             Switching to " + current.getName());
                }
            }
        }
        System.out.println();
        
        // Show process control block
        System.out.println("Process Control Block (PCB) Example:");
        System.out.println(shell.getPCB());
        System.out.println();
        
        // Show scheduler stats
        System.out.println(scheduler.getStats());
        System.out.println();
    }
}

