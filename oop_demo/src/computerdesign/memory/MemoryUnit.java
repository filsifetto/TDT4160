package computerdesign.memory;

/**
 * MemoryUnit interface - abstraction for all memory components.
 * 
 * The memory hierarchy (registers -> cache -> main memory -> disk) all
 * share common operations but differ in:
 * - Access time (registers: ~1 cycle, L1: ~4 cycles, RAM: ~100 cycles)
 * - Capacity (registers: ~32 words, L1: ~32KB, RAM: ~GBs)
 * - Cost per bit
 * 
 * This interface captures the common behavior while allowing each level
 * to implement its specific characteristics.
 */
public interface MemoryUnit {
    
    /**
     * Read a word from the specified address.
     * @param address The memory address to read from
     * @return The value at that address
     */
    int read(int address);
    
    /**
     * Write a word to the specified address.
     * @param address The memory address to write to
     * @param value The value to write
     */
    void write(int address, int value);
    
    /**
     * Get the size of this memory unit in bytes.
     * @return Size in bytes
     */
    int getSize();
    
    /**
     * Get the access time in cycles for this memory level.
     * @return Access time in cycles
     */
    int getAccessTime();
    
    /**
     * Reset the memory to initial state.
     */
    void reset();
    
    /**
     * Get the name/type of this memory unit (for debugging).
     * @return Name of this memory unit
     */
    String getName();
}

