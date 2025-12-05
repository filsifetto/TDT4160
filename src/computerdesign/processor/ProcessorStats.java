package computerdesign.processor;

/**
 * Statistics about processor execution.
 * 
 * Useful for comparing different processor implementations:
 * - Single-cycle: CPI = 1, but long cycle time
 * - Multi-cycle: CPI > 1, but shorter cycle time
 * - Pipeline: CPI approaches 1 with shorter cycle time (best of both worlds)
 */
public class ProcessorStats {
    private int cycleCount;
    private int instructionCount;
    private int memoryAccesses;
    private int cacheHits;
    private int cacheMisses;
    private int branchCount;
    private int branchMispredictions;
    private int stallCycles;
    
    public ProcessorStats() {
        reset();
    }
    
    public void reset() {
        cycleCount = 0;
        instructionCount = 0;
        memoryAccesses = 0;
        cacheHits = 0;
        cacheMisses = 0;
        branchCount = 0;
        branchMispredictions = 0;
        stallCycles = 0;
    }
    
    // Increment methods
    public void incrementCycles() { cycleCount++; }
    public void incrementCycles(int n) { cycleCount += n; }
    public void incrementInstructions() { instructionCount++; }
    public void incrementMemoryAccesses() { memoryAccesses++; }
    public void incrementCacheHits() { cacheHits++; }
    public void incrementCacheMisses() { cacheMisses++; }
    public void incrementBranches() { branchCount++; }
    public void incrementMispredictions() { branchMispredictions++; }
    public void incrementStalls() { stallCycles++; }
    public void incrementStalls(int n) { stallCycles += n; }
    
    // Getters
    public int getCycleCount() { return cycleCount; }
    public int getInstructionCount() { return instructionCount; }
    public int getMemoryAccesses() { return memoryAccesses; }
    public int getCacheHits() { return cacheHits; }
    public int getCacheMisses() { return cacheMisses; }
    public int getBranchCount() { return branchCount; }
    public int getBranchMispredictions() { return branchMispredictions; }
    public int getStallCycles() { return stallCycles; }
    
    /**
     * Cycles Per Instruction - key performance metric.
     * Ideal pipelined processor: CPI = 1
     * Real processors: CPI > 1 due to stalls, misses, etc.
     */
    public double getCPI() {
        return instructionCount == 0 ? 0 : (double) cycleCount / instructionCount;
    }
    
    /**
     * Cache hit rate - measures memory hierarchy effectiveness.
     */
    public double getCacheHitRate() {
        int total = cacheHits + cacheMisses;
        return total == 0 ? 0 : (double) cacheHits / total;
    }
    
    /**
     * Branch prediction accuracy.
     */
    public double getBranchAccuracy() {
        return branchCount == 0 ? 1.0 : 1.0 - ((double) branchMispredictions / branchCount);
    }
    
    @Override
    public String toString() {
        return String.format(
            "ProcessorStats {\n" +
            "  Cycles: %d\n" +
            "  Instructions: %d\n" +
            "  CPI: %.2f\n" +
            "  Memory Accesses: %d\n" +
            "  Cache Hit Rate: %.1f%%\n" +
            "  Branch Accuracy: %.1f%%\n" +
            "  Stall Cycles: %d\n" +
            "}",
            cycleCount, instructionCount, getCPI(),
            memoryAccesses, getCacheHitRate() * 100,
            getBranchAccuracy() * 100, stallCycles
        );
    }
}

