package computerdesign.processor;

import computerdesign.memory.RegisterFile;
import computerdesign.memory.MainMemory;
import computerdesign.instruction.Instruction;

/**
 * Processor interface - the central abstraction for CPU implementations.
 * 
 * Different processor designs (single-cycle, multi-cycle, pipelined) all
 * implement this interface but differ in HOW they execute instructions.
 * 
 * Key insight from Patterson & Hennessy: All processors must perform the same
 * fundamental operations, but timing and resource usage vary dramatically.
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

