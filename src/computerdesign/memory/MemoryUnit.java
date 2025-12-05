package computerdesign.memory;

/**
 * MemoryUnit - abstraction for all memory components in the hierarchy.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE MEMORY HIERARCHY - A FUNDAMENTAL CONCEPT IN COMPUTER ARCHITECTURE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Memory systems face a fundamental tradeoff:
 *   - FAST memory is SMALL and EXPENSIVE
 *   - LARGE memory is SLOW and CHEAP
 * 
 * The solution: a HIERARCHY of memories, each level acting as a cache for
 * the level below. This exploits LOCALITY to give the illusion of fast,
 * large, cheap memory.
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                        THE MEMORY HIERARCHY                                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │  Level        │  Size        │  Access Time   │  Cost/Byte  │  Technology  │
 * ├───────────────┼──────────────┼────────────────┼─────────────┼──────────────┤
 * │  Registers    │  ~128 B      │  ~0.25 ns      │  $$$$$$     │  CMOS        │
 * │  L1 Cache     │  32-64 KB    │  ~1 ns         │  $$$$       │  SRAM        │
 * │  L2 Cache     │  256 KB-1 MB │  ~4 ns         │  $$$        │  SRAM        │
 * │  L3 Cache     │  2-32 MB     │  ~10 ns        │  $$         │  SRAM        │
 * │  Main Memory  │  4-64 GB     │  ~100 ns       │  $          │  DRAM        │
 * │  SSD          │  256 GB-4 TB │  ~100 µs       │  ¢          │  Flash       │
 * │  HDD          │  1-16 TB     │  ~10 ms        │  ¢          │  Magnetic    │
 * └───────────────┴──────────────┴────────────────┴─────────────┴──────────────┘
 * 
 * KEY INSIGHT: Each level is ~10-100x slower than the one above, but ~10-100x
 * larger. The hierarchy works because of LOCALITY:
 * 
 *   TEMPORAL LOCALITY: Recently accessed data is likely to be accessed again.
 *     Example: Loop counter variable is read every iteration.
 * 
 *   SPATIAL LOCALITY: Data near recently accessed data is likely to be accessed.
 *     Example: Array elements are often accessed sequentially.
 * 
 * This interface captures the common operations while allowing each level
 * to implement its specific characteristics (size, speed, eviction policy).
 * 
 * @see RegisterFile - Fastest level (~0 cycles in our simulation)
 * @see Cache - Intermediate levels (L1, L2, L3)
 * @see MainMemory - Physical RAM (slowest level we simulate)
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

