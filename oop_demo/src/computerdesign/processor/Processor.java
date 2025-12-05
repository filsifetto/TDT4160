package computerdesign.processor;

import computerdesign.memory.RegisterFile;
import computerdesign.memory.MainMemory;
import computerdesign.instruction.Instruction;

/**
 * Processor - the central abstraction for CPU implementations.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE FUNDAMENTAL EXECUTION CYCLE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * ALL processors execute the same fundamental cycle (von Neumann model):
 * 
 *   ┌────────────────────────────────────────────────────────────────────────┐
 *   │                                                                        │
 *   │    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐           │
 *   │    │  FETCH  │───►│ DECODE  │───►│ EXECUTE │───►│  WRITE  │           │
 *   │    │         │    │         │    │         │    │  BACK   │           │
 *   │    └─────────┘    └─────────┘    └─────────┘    └─────────┘           │
 *   │         │                                            │                │
 *   │         │              ┌─────────┐                   │                │
 *   │         │              │ MEMORY  │◄──────────────────┘                │
 *   │         │              │ ACCESS  │ (for loads/stores)                 │
 *   │         │              └─────────┘                                    │
 *   │         │                                                             │
 *   │         └─────────────────────────────────────────────────────────────┘
 *   │                              (repeat)
 *   └────────────────────────────────────────────────────────────────────────┘
 * 
 * FETCH:    Read instruction from memory at address in PC
 * DECODE:   Figure out what the instruction does, read operands
 * EXECUTE:  Perform the operation (ALU computation)
 * MEMORY:   Access memory if needed (load/store)
 * WRITEBACK: Store result in destination register
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * PROCESSOR IMPLEMENTATIONS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Different implementations trade off simplicity, speed, and efficiency:
 * 
 * SINGLE-CYCLE: Each instruction takes exactly one (long) clock cycle
 *   + Simple design, CPI = 1
 *   - Clock period must fit the SLOWEST instruction
 *   - Poor hardware utilization
 * 
 * MULTI-CYCLE: Each instruction takes multiple (short) clock cycles
 *   + Shorter clock period
 *   + Better hardware utilization (reuse components)
 *   - CPI > 1 (varies by instruction)
 *   - More complex control
 * 
 * PIPELINED: Multiple instructions in flight simultaneously
 *   + High throughput (ideally CPI → 1)
 *   + Short clock period
 *   - Complex (hazards, forwarding, stalls)
 *   - Single instruction latency doesn't improve
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * PERFORMANCE EQUATION
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   Execution Time = Instructions × CPI × Clock Period
 * 
 *   Where:
 *     Instructions = Number of instructions in program (ISA dependent)
 *     CPI = Cycles Per Instruction (microarchitecture dependent)
 *     Clock Period = 1/Frequency (technology dependent)
 * 
 * Different processor designs optimize different parts of this equation.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @see SingleCycleProcessor - Simple but slow
 * @see MultiCycleProcessor - Better hardware utilization
 * @see PipelineProcessor - High throughput
 */
public interface Processor {
    
    /**
     * Execute a single instruction or advance the processor state by one cycle.
     * - Single-cycle: Completes one instruction per call
     * - Multi-cycle: Advances one phase of instruction execution
     * - Pipeline: Advances all pipeline stages by one cycle
     */
    void cycle();
    
    /**
     * Run the processor until a halt condition is reached.
     * @param maxCycles Maximum cycles to run (prevents infinite loops)
     * @return Number of cycles executed
     */
    int run(int maxCycles);
    
    /**
     * Fetch the next instruction from memory.
     * @return The fetched instruction
     */
    Instruction fetch();
    
    /**
     * Get the current program counter value.
     * @return Current PC
     */
    int getPC();
    
    /**
     * Set the program counter to a specific address.
     * @param address The new PC value
     */
    void setPC(int address);
    
    /**
     * Get the register file for this processor.
     * @return The processor's register file
     */
    RegisterFile getRegisterFile();
    
    /**
     * Get the main memory connected to this processor.
     * @return The main memory
     */
    MainMemory getMemory();
    
    /**
     * Check if the processor has halted.
     * @return true if halted
     */
    boolean isHalted();
    
    /**
     * Reset the processor to initial state.
     */
    void reset();
    
    /**
     * Get statistics about processor execution.
     * @return Processor statistics
     */
    ProcessorStats getStats();
}

