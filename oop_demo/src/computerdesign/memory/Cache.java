package computerdesign.memory;

/**
 * Cache - fast memory between processor and main memory.
 * 
 * Key concepts from Patterson & Hennessy:
 * - Temporal locality: Recently accessed data likely to be accessed again
 * - Spatial locality: Data near recently accessed data likely to be accessed
 * 
 * Cache organization:
 * - Block/Line: Unit of data transfer (typically 32-64 bytes)
 * - Set: Group of blocks where a memory address can be placed
 * - Way: Number of blocks per set (associativity)
 * 
 * This implementation is a direct-mapped cache (1-way set associative).
 * Each memory address maps to exactly one cache location.
 */
public class Cache implements MemoryUnit {
    
    // Cache line structure
    private static class CacheLine {
        boolean valid;
        int tag;
        int[] data;  // One block of words
        boolean dirty;  // For write-back policy
        
        CacheLine(int blockSizeWords) {
            this.valid = false;
            this.tag = 0;
            this.data = new int[blockSizeWords];
            this.dirty = false;
        }
        
        void reset() {
            valid = false;
            tag = 0;
            dirty = false;
            for (int i = 0; i < data.length; i++) {
                data[i] = 0;
            }
        }
    }
    
    private final String name;
    private final int accessTime;
    private final int numLines;
    private final int blockSizeWords;
    private final int blockSizeBytes;
    private final CacheLine[] lines;
    private final MemoryUnit nextLevel;  // Next level in hierarchy (L2 or RAM)
    
    // Statistics
    private int hits;
    private int misses;
    
    // Bit field sizes for address decomposition
    private final int offsetBits;
    private final int indexBits;
    
    /**
     * Create a cache.
     * @param name Cache name (e.g., "L1", "L2")
     * @param sizeBytes Total cache size in bytes
     * @param blockSizeBytes Size of each cache block in bytes
     * @param accessTime Access time in cycles
     * @param nextLevel Next level in memory hierarchy
     */
    public Cache(String name, int sizeBytes, int blockSizeBytes, 
                 int accessTime, MemoryUnit nextLevel) {
        this.name = name;
        this.accessTime = accessTime;
        this.blockSizeBytes = blockSizeBytes;
        this.blockSizeWords = blockSizeBytes / 4;
        this.numLines = sizeBytes / blockSizeBytes;
        this.nextLevel = nextLevel;
        
        // Calculate bit field sizes
        this.offsetBits = log2(blockSizeBytes);
        this.indexBits = log2(numLines);
        
        // Initialize cache lines
        this.lines = new CacheLine[numLines];
        for (int i = 0; i < numLines; i++) {
            lines[i] = new CacheLine(blockSizeWords);
        }
        
        this.hits = 0;
        this.misses = 0;
    }
    
    @Override
    public int read(int address) {
        int index = getIndex(address);
        int tag = getTag(address);
        int offset = getOffset(address);
        int wordOffset = offset / 4;
        
        CacheLine line = lines[index];
        
        if (line.valid && line.tag == tag) {
            // Cache hit!
            hits++;
            return line.data[wordOffset];
        } else {
            // Cache miss - fetch from next level
            misses++;
            loadBlock(address, index, tag);
            return line.data[wordOffset];
        }
    }
    
    @Override
    public void write(int address, int value) {
        int index = getIndex(address);
        int tag = getTag(address);
        int offset = getOffset(address);
        int wordOffset = offset / 4;
        
        CacheLine line = lines[index];
        
        if (line.valid && line.tag == tag) {
            // Cache hit - write to cache
            hits++;
            line.data[wordOffset] = value;
            line.dirty = true;  // Mark for write-back
        } else {
            // Cache miss - write-allocate policy
            misses++;
            loadBlock(address, index, tag);
            line.data[wordOffset] = value;
            line.dirty = true;
        }
    }
    
    /**
     * Load a block from the next level into the cache.
     */
    private void loadBlock(int address, int index, int tag) {
        CacheLine line = lines[index];
        
        // Write back if dirty (write-back policy)
        if (line.valid && line.dirty) {
            writeBack(index);
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
    }
    
    /**
     * Write a dirty block back to the next level.
     */
    private void writeBack(int index) {
        CacheLine line = lines[index];
        int blockAddress = reconstructAddress(line.tag, index);
        
        for (int i = 0; i < blockSizeWords; i++) {
            nextLevel.write(blockAddress + (i * 4), line.data[i]);
        }
        line.dirty = false;
    }
    
    /**
     * Flush all dirty blocks to next level.
     */
    public void flush() {
        for (int i = 0; i < numLines; i++) {
            if (lines[i].valid && lines[i].dirty) {
                writeBack(i);
            }
        }
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
    
    private int reconstructAddress(int tag, int index) {
        return (tag << (offsetBits + indexBits)) | (index << offsetBits);
    }
    
    private static int log2(int n) {
        return (int) (Math.log(n) / Math.log(2));
    }
    
    @Override
    public int getSize() {
        return numLines * blockSizeBytes;
    }
    
    @Override
    public int getAccessTime() {
        return accessTime;
    }
    
    @Override
    public void reset() {
        for (CacheLine line : lines) {
            line.reset();
        }
        hits = 0;
        misses = 0;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    // Statistics
    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    
    public double getHitRate() {
        int total = hits + misses;
        return total == 0 ? 0 : (double) hits / total;
    }
    
    @Override
    public String toString() {
        return String.format("%s Cache: %d bytes, %d lines, %d bytes/block, " +
            "hit rate: %.1f%% (%d hits, %d misses)",
            name, getSize(), numLines, blockSizeBytes,
            getHitRate() * 100, hits, misses);
    }
}

