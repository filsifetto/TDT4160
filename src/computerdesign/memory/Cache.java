package computerdesign.memory;

/**
 * Cache - fast memory between processor and main memory.
 * 
 * Covers learning goals: T6.1 (Memory hierarchy, cache organization)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WHY CACHES EXIST
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * The CPU is MUCH faster than main memory:
 *   - CPU can execute ~4 instructions per nanosecond
 *   - Main memory takes ~100 ns to respond
 *   - Without cache, CPU would wait ~400 cycles for every memory access!
 * 
 * Caches bridge this gap by keeping frequently-used data close to the CPU.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * LOCALITY PRINCIPLES (Why Caches Work)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * TEMPORAL LOCALITY: "If you used it recently, you'll probably use it again"
 *   - Loop counters, local variables
 *   - Function return addresses
 *   - Hot code paths
 * 
 * SPATIAL LOCALITY: "If you used address X, you'll probably use X+1, X+2..."
 *   - Array traversal
 *   - Sequential instruction fetch
 *   - Struct/object field access
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * CACHE ORGANIZATION
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * ADDRESS DECOMPOSITION (for cache lookup):
 * 
 *   ┌───────────────────────────────────────────────────────────────┐
 *   │           ADDRESS (e.g., 32 bits)                             │
 *   ├─────────────────┬─────────────────┬──────────────────────────┤
 *   │      TAG        │     INDEX       │        OFFSET            │
 *   │   (remaining)   │  (set select)   │    (byte in block)       │
 *   └─────────────────┴─────────────────┴──────────────────────────┘
 *           │                 │                    │
 *           │                 │                    └─► Select byte within block
 *           │                 └─► Select which set/line to check
 *           └─► Compare with stored tag to verify match
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * CACHE TYPES (ASSOCIATIVITY)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * DIRECT-MAPPED (1-way): Each address maps to exactly one cache line
 * 
 *   Set 0: ┌─────┐  ← Address with index=0 can ONLY go here
 *   Set 1: ├─────┤
 *   Set 2: ├─────┤
 *   Set 3: └─────┘
 *   
 *   + Simple: Just check one location
 *   + Fast: Single comparator
 *   - Conflict misses: Two addresses with same index fight
 * 
 * SET-ASSOCIATIVE (N-way): Address can go in any of N lines in its set
 * 
 *   4-way set associative:
 *          Way 0   Way 1   Way 2   Way 3
 *   Set 0: ┌─────┬─────┬─────┬─────┐
 *   Set 1: ├─────┼─────┼─────┼─────┤  ← Address with index=1 can go
 *   Set 2: ├─────┼─────┼─────┼─────┤    in any of these 4 ways
 *   Set 3: └─────┴─────┴─────┴─────┘
 *   
 *   + Fewer conflict misses
 *   - Need to check N tags in parallel
 *   - Need replacement policy (LRU, random)
 * 
 * FULLY ASSOCIATIVE: Address can go anywhere (like a hash table)
 *   
 *   + Minimum conflict misses
 *   - Must check ALL tags - expensive!
 *   - Used for small structures (TLB)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * AMAT - Average Memory Access Time
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   AMAT = Hit Time + Miss Rate × Miss Penalty
 * 
 *   Single-level cache:
 *     AMAT = T_cache + (1 - HitRate) × T_memory
 * 
 *   Multi-level cache (L1 + L2):
 *     AMAT = T_L1 + MissRate_L1 × (T_L2 + MissRate_L2 × T_memory)
 * 
 *   Example:
 *     L1: 1 cycle, 95% hit rate
 *     L2: 10 cycles, 80% hit rate (of L1 misses)
 *     Memory: 100 cycles
 *     
 *     AMAT = 1 + 0.05 × (10 + 0.20 × 100)
 *          = 1 + 0.05 × 30
 *          = 2.5 cycles
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WRITE POLICIES
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * WRITE-THROUGH: Write to both cache AND memory immediately
 *   + Memory always up-to-date, simple
 *   - Slow (every write goes to memory)
 *   Use with: Write buffer to hide latency
 * 
 * WRITE-BACK: Write only to cache, mark as "dirty", write to memory later
 *   + Fast writes (memory only updated on eviction)
 *   - Complex, memory can be stale
 *   
 * WRITE-ALLOCATE: On write miss, fetch block then write
 * NO-WRITE-ALLOCATE: On write miss, write directly to memory
 * 
 * Common combinations:
 *   - Write-back + Write-allocate (this implementation)
 *   - Write-through + No-write-allocate
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * REPLACEMENT POLICIES (for set-associative)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * When a set is full, which line to evict?
 * 
 * LRU (Least Recently Used):
 *   - Evict the line used longest ago
 *   - Best performance, but expensive to track
 * 
 * Random:
 *   - Evict a random line
 *   - Simple, surprisingly effective
 * 
 * FIFO (First In First Out):
 *   - Evict oldest line
 *   - Simpler than LRU, not as good
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @see MemoryUnit - Interface this implements
 * @see MainMemory - The next level in the hierarchy
 */
public class Cache implements MemoryUnit {
    
    /** Cache associativity types. */
    public enum Associativity {
        DIRECT_MAPPED(1),
        TWO_WAY(2),
        FOUR_WAY(4),
        EIGHT_WAY(8);
        
        public final int ways;
        Associativity(int ways) { this.ways = ways; }
    }
    
    /** Replacement policies for set-associative caches. */
    public enum ReplacementPolicy {
        LRU,    // Least Recently Used
        RANDOM  // Random replacement
    }
    
    // Cache line structure
    private static class CacheLine {
        boolean valid;
        int tag;
        int[] data;  // One block of words
        boolean dirty;  // For write-back policy
        long lastUsed;  // For LRU replacement
        
        CacheLine(int blockSizeWords) {
            this.valid = false;
            this.tag = 0;
            this.data = new int[blockSizeWords];
            this.dirty = false;
            this.lastUsed = 0;
        }
        
        void reset() {
            valid = false;
            tag = 0;
            dirty = false;
            lastUsed = 0;
            for (int i = 0; i < data.length; i++) {
                data[i] = 0;
            }
        }
    }
    
    private final String name;
    private final int accessTime;
    private final int numSets;
    private final int numWays;  // Associativity
    private final int blockSizeWords;
    private final int blockSizeBytes;
    private final CacheLine[][] sets;  // [set][way]
    private final MemoryUnit nextLevel;
    private final ReplacementPolicy replacementPolicy;
    
    // Statistics
    private int hits;
    private int misses;
    private long accessCounter;  // For LRU
    
    // Bit field sizes for address decomposition
    private final int offsetBits;
    private final int indexBits;
    
    /**
     * Create a direct-mapped cache (backwards compatible constructor).
     */
    public Cache(String name, int sizeBytes, int blockSizeBytes, 
                 int accessTime, MemoryUnit nextLevel) {
        this(name, sizeBytes, blockSizeBytes, accessTime, nextLevel, 
             Associativity.DIRECT_MAPPED, ReplacementPolicy.LRU);
    }
    
    /**
     * Create a cache with specified associativity.
     * 
     * @param name Cache name (e.g., "L1", "L2")
     * @param sizeBytes Total cache size in bytes
     * @param blockSizeBytes Size of each cache block in bytes
     * @param accessTime Access time in cycles
     * @param nextLevel Next level in memory hierarchy
     * @param associativity Cache associativity (ways per set)
     * @param replacementPolicy Policy for line replacement
     */
    public Cache(String name, int sizeBytes, int blockSizeBytes, 
                 int accessTime, MemoryUnit nextLevel,
                 Associativity associativity, ReplacementPolicy replacementPolicy) {
        this.name = name;
        this.accessTime = accessTime;
        this.blockSizeBytes = blockSizeBytes;
        this.blockSizeWords = blockSizeBytes / 4;
        this.numWays = associativity.ways;
        this.replacementPolicy = replacementPolicy;
        this.nextLevel = nextLevel;
        
        // Total lines / ways = number of sets
        int totalLines = sizeBytes / blockSizeBytes;
        this.numSets = totalLines / numWays;
        
        // Calculate bit field sizes
        this.offsetBits = log2(blockSizeBytes);
        this.indexBits = log2(numSets);
        
        // Initialize cache sets
        this.sets = new CacheLine[numSets][numWays];
        for (int s = 0; s < numSets; s++) {
            for (int w = 0; w < numWays; w++) {
                sets[s][w] = new CacheLine(blockSizeWords);
            }
        }
        
        this.hits = 0;
        this.misses = 0;
        this.accessCounter = 0;
    }
    
    @Override
    public int read(int address) {
        accessCounter++;
        int setIndex = getIndex(address);
        int tag = getTag(address);
        int offset = getOffset(address);
        int wordOffset = offset / 4;
        
        // Search all ways in the set
        for (int way = 0; way < numWays; way++) {
            CacheLine line = sets[setIndex][way];
            if (line.valid && line.tag == tag) {
                // Cache hit!
                hits++;
                line.lastUsed = accessCounter;
                return line.data[wordOffset];
            }
        }
        
        // Cache miss - fetch from next level
        misses++;
        int victimWay = selectVictim(setIndex);
        loadBlock(address, setIndex, victimWay, tag);
        return sets[setIndex][victimWay].data[wordOffset];
    }
    
    @Override
    public void write(int address, int value) {
        accessCounter++;
        int setIndex = getIndex(address);
        int tag = getTag(address);
        int offset = getOffset(address);
        int wordOffset = offset / 4;
        
        // Search all ways in the set
        for (int way = 0; way < numWays; way++) {
            CacheLine line = sets[setIndex][way];
            if (line.valid && line.tag == tag) {
                // Cache hit - write to cache
                hits++;
                line.data[wordOffset] = value;
                line.dirty = true;
                line.lastUsed = accessCounter;
                return;
            }
        }
        
        // Cache miss - write-allocate policy
        misses++;
        int victimWay = selectVictim(setIndex);
        loadBlock(address, setIndex, victimWay, tag);
        sets[setIndex][victimWay].data[wordOffset] = value;
        sets[setIndex][victimWay].dirty = true;
    }
    
    /**
     * Select a victim line for replacement using the configured policy.
     */
    private int selectVictim(int setIndex) {
        // First, look for an invalid line
        for (int way = 0; way < numWays; way++) {
            if (!sets[setIndex][way].valid) {
                return way;
            }
        }
        
        // All lines valid, use replacement policy
        switch (replacementPolicy) {
            case LRU:
                return selectLRU(setIndex);
            case RANDOM:
                return (int) (Math.random() * numWays);
            default:
                return 0;
        }
    }
    
    /**
     * Select the least recently used line in a set.
     */
    private int selectLRU(int setIndex) {
        int lruWay = 0;
        long lruTime = Long.MAX_VALUE;
        
        for (int way = 0; way < numWays; way++) {
            if (sets[setIndex][way].lastUsed < lruTime) {
                lruTime = sets[setIndex][way].lastUsed;
                lruWay = way;
            }
        }
        
        return lruWay;
    }
    
    /**
     * Load a block from the next level into the cache.
     */
    private void loadBlock(int address, int setIndex, int way, int tag) {
        CacheLine line = sets[setIndex][way];
        
        // Write back if dirty (write-back policy)
        if (line.valid && line.dirty) {
            writeBack(setIndex, way);
        }
        
        // Calculate block-aligned address
        int blockAddress = address & ~(blockSizeBytes - 1);
        
        // Load entire block from next level
        for (int i = 0; i < blockSizeWords; i++) {
            line.data[i] = nextLevel.read(blockAddress + (i * 4));
        }
        
        line.valid = true;
        line.tag = tag;
        line.dirty = false;
        line.lastUsed = accessCounter;
    }
    
    /**
     * Write a dirty block back to the next level.
     */
    private void writeBack(int setIndex, int way) {
        CacheLine line = sets[setIndex][way];
        int blockAddress = reconstructAddress(line.tag, setIndex);
        
        for (int i = 0; i < blockSizeWords; i++) {
            nextLevel.write(blockAddress + (i * 4), line.data[i]);
        }
        line.dirty = false;
    }
    
    /**
     * Flush all dirty blocks to next level.
     */
    public void flush() {
        for (int s = 0; s < numSets; s++) {
            for (int w = 0; w < numWays; w++) {
                if (sets[s][w].valid && sets[s][w].dirty) {
                    writeBack(s, w);
                }
            }
        }
    }
    
    // ==================== AMAT CALCULATIONS ====================
    
    /**
     * Calculate Average Memory Access Time.
     * AMAT = Hit Time + Miss Rate × Miss Penalty
     */
    public double calculateAMAT() {
        double hitRate = getHitRate();
        double missRate = 1.0 - hitRate;
        int missPenalty = nextLevel.getAccessTime();
        
        return accessTime + missRate * missPenalty;
    }
    
    /**
     * Calculate AMAT with a custom miss penalty.
     */
    public static double calculateAMAT(double hitTime, double missRate, double missPenalty) {
        return hitTime + missRate * missPenalty;
    }
    
    /**
     * Calculate two-level AMAT.
     */
    public static double calculateTwoLevelAMAT(double l1HitTime, double l1MissRate,
                                                double l2HitTime, double l2MissRate,
                                                double memoryTime) {
        return l1HitTime + l1MissRate * (l2HitTime + l2MissRate * memoryTime);
    }
    
    // Address decomposition
    private int getOffset(int address) {
        return address & ((1 << offsetBits) - 1);
    }
    
    private int getIndex(int address) {
        return (address >> offsetBits) & ((1 << indexBits) - 1);
    }
    
    private int getTag(int address) {
        return address >> (offsetBits + indexBits);
    }
    
    private int reconstructAddress(int tag, int setIndex) {
        return (tag << (offsetBits + indexBits)) | (setIndex << offsetBits);
    }
    
    private static int log2(int n) {
        return (int) (Math.log(n) / Math.log(2));
    }
    
    @Override
    public int getSize() {
        return numSets * numWays * blockSizeBytes;
    }
    
    @Override
    public int getAccessTime() {
        return accessTime;
    }
    
    @Override
    public void reset() {
        for (int s = 0; s < numSets; s++) {
            for (int w = 0; w < numWays; w++) {
                sets[s][w].reset();
            }
        }
        hits = 0;
        misses = 0;
        accessCounter = 0;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    // Statistics
    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    public int getNumSets() { return numSets; }
    public int getNumWays() { return numWays; }
    
    public double getHitRate() {
        int total = hits + misses;
        return total == 0 ? 0 : (double) hits / total;
    }
    
    public double getMissRate() {
        return 1.0 - getHitRate();
    }
    
    @Override
    public String toString() {
        String assocStr = numWays == 1 ? "direct-mapped" : numWays + "-way set-associative";
        return String.format("%s Cache: %d bytes, %d sets × %d ways (%s), %d bytes/block, " +
            "hit rate: %.1f%% (%d hits, %d misses), AMAT: %.2f cycles",
            name, getSize(), numSets, numWays, assocStr, blockSizeBytes,
            getHitRate() * 100, hits, misses, calculateAMAT());
    }
    
    /**
     * Detailed dump of cache contents (for debugging).
     */
    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== %s Cache Dump ===\n", name));
        sb.append(String.format("Configuration: %d sets × %d ways, %d bytes/block\n", 
                               numSets, numWays, blockSizeBytes));
        sb.append(String.format("Statistics: %d hits, %d misses (%.1f%% hit rate)\n\n",
                               hits, misses, getHitRate() * 100));
        
        for (int s = 0; s < numSets; s++) {
            boolean hasValidLine = false;
            for (int w = 0; w < numWays; w++) {
                if (sets[s][w].valid) hasValidLine = true;
            }
            if (!hasValidLine) continue;  // Skip empty sets
            
            sb.append(String.format("Set %d:\n", s));
            for (int w = 0; w < numWays; w++) {
                CacheLine line = sets[s][w];
                if (line.valid) {
                    sb.append(String.format("  Way %d: [%s] tag=0x%X data=0x%X...\n",
                        w, line.dirty ? "D" : " ", line.tag, 
                        line.data.length > 0 ? line.data[0] : 0));
                }
            }
        }
        
        return sb.toString();
    }
}
