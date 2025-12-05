package computerdesign.theory;

/**
 * Performance - Understanding computer performance metrics.
 * 
 * Covers learning goals: T1.3, T1.4, T6.1 (AMAT)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE IRON LAW OF PROCESSOR PERFORMANCE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                                                                         │
 *   │   CPU Time = Instructions × CPI × Clock Period                          │
 *   │              ─────────────   ───   ────────────                         │
 *   │                   │           │         │                               │
 *   │                   │           │         └─ Technology (transistor size) │
 *   │                   │           └─────────── Microarchitecture            │
 *   │                   └─────────────────────── ISA & Compiler               │
 *   │                                                                         │
 *   │   Or equivalently:                                                      │
 *   │                                                                         │
 *   │   CPU Time = Instructions × CPI / Clock Frequency                       │
 *   │                                                                         │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * WHERE:
 *   Instructions = Number of instructions executed (instruction count)
 *   CPI = Cycles Per Instruction (average)
 *   Clock Period = 1 / Clock Frequency (seconds per cycle)
 * 
 * WHAT AFFECTS EACH FACTOR:
 * 
 *   Instructions:
 *   - ISA design (RISC vs CISC)
 *   - Compiler optimization
 *   - Algorithm choice
 * 
 *   CPI:
 *   - Pipeline depth and efficiency
 *   - Cache hit rates
 *   - Branch prediction accuracy
 *   - Hazards and stalls
 * 
 *   Clock Frequency:
 *   - Transistor technology (nm process)
 *   - Pipeline depth
 *   - Critical path length
 *   - Power/thermal limits
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * RESPONSE TIME vs THROUGHPUT
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * RESPONSE TIME (Latency):
 *   - Time to complete ONE task
 *   - Important for interactive applications
 *   - "How long until I see the result?"
 * 
 * THROUGHPUT (Bandwidth):
 *   - Tasks completed per unit time
 *   - Important for batch processing, servers
 *   - "How many requests per second?"
 * 
 * Example:
 *   Single-issue processor: Latency = 5 cycles, Throughput = 0.2 inst/cycle
 *   Pipelined processor:    Latency = 5 cycles, Throughput = 1 inst/cycle
 *   (Pipeline improves throughput without improving latency!)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * AMDAHL'S LAW - The limit of parallelism
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *                           1
 *   Speedup = ─────────────────────────────
 *             (1 - f) + f/S
 * 
 *   WHERE:
 *     f = Fraction of execution time that can be improved (parallelized)
 *     S = Speedup factor for the improved part (e.g., number of processors)
 * 
 * KEY INSIGHT: The serial part LIMITS the maximum speedup!
 * 
 *   Example: 90% parallelizable, infinite processors:
 *     Speedup = 1 / (0.1 + 0.9/∞) = 1 / 0.1 = 10x maximum!
 * 
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  % Parallel │ 2 cores │ 4 cores │ 8 cores │ ∞ cores │ Max Speedup      │
 *   ├─────────────┼─────────┼─────────┼─────────┼─────────┼──────────────────┤
 *   │     50%     │  1.33x  │  1.60x  │  1.78x  │  2.00x  │ Limited by 50%   │
 *   │     75%     │  1.60x  │  2.29x  │  2.91x  │  4.00x  │ serial part      │
 *   │     90%     │  1.82x  │  3.08x  │  4.71x  │ 10.00x  │                  │
 *   │     99%     │  1.98x  │  3.88x  │  7.48x  │100.00x  │                  │
 *   └─────────────┴─────────┴─────────┴─────────┴─────────┴──────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * POWER AND ENERGY
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * DYNAMIC POWER (switching transistors):
 * 
 *   P_dynamic = α × C × V² × f
 *   
 *   WHERE:
 *     α = Activity factor (fraction of transistors switching)
 *     C = Capacitance (depends on transistor size and count)
 *     V = Voltage
 *     f = Clock frequency
 * 
 *   KEY INSIGHT: Power grows with V² and f!
 *   - Doubling frequency doubles power
 *   - Doubling voltage QUADRUPLES power
 *   - This is why we can't just keep increasing clock speed!
 * 
 * STATIC POWER (leakage):
 *   - Current flows even when transistors aren't switching
 *   - Worse at smaller transistor sizes
 *   - Major concern in modern processors
 * 
 * ENERGY vs POWER:
 *   Energy = Power × Time
 *   
 *   A faster processor might use more POWER but less ENERGY
 *   (because it finishes sooner)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * AMAT - Average Memory Access Time
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   AMAT = Hit Time + Miss Rate × Miss Penalty
 * 
 *   For multi-level cache:
 *   AMAT = L1_HitTime + L1_MissRate × (L2_HitTime + L2_MissRate × L2_MissPenalty)
 * 
 *   Example:
 *     L1: Hit time = 1 cycle, Miss rate = 5%
 *     L2: Hit time = 10 cycles, Miss rate = 20% (of L1 misses)
 *     Memory: Access time = 100 cycles
 *     
 *     AMAT = 1 + 0.05 × (10 + 0.20 × 100)
 *          = 1 + 0.05 × (10 + 20)
 *          = 1 + 0.05 × 30
 *          = 1 + 1.5
 *          = 2.5 cycles
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * BENCHMARKS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Why benchmarks?
 *   - Real workloads are complex and varied
 *   - Need standardized comparison between systems
 *   - Marketing claims need verification
 * 
 * Types:
 *   - Microbenchmarks: Test specific features (memory bandwidth, FP throughput)
 *   - Kernel benchmarks: Small important algorithms (matrix multiply, FFT)
 *   - Full application benchmarks: Real programs (compilers, databases)
 *   - Benchmark suites: Collections (SPEC CPU, PARSEC, MLPerf)
 * 
 * SPEC CPU:
 *   - Industry standard for CPU performance
 *   - SPECint (integer) and SPECfp (floating-point)
 *   - Geometric mean of normalized execution times
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class Performance {
    
    // ==================== IRON LAW CALCULATIONS ====================
    
    /**
     * Calculate CPU execution time using the Iron Law.
     * 
     * @param instructions Number of instructions executed
     * @param cpi Cycles per instruction (average)
     * @param clockFrequencyHz Clock frequency in Hz
     * @return Execution time in seconds
     */
    public static double cpuTime(long instructions, double cpi, double clockFrequencyHz) {
        return instructions * cpi / clockFrequencyHz;
    }
    
    /**
     * Calculate CPI from execution time, instruction count, and clock frequency.
     */
    public static double calculateCPI(double executionTimeSeconds, long instructions, double clockFrequencyHz) {
        return executionTimeSeconds * clockFrequencyHz / instructions;
    }
    
    /**
     * Calculate effective CPI with cache misses.
     * 
     * @param baseCPI Base CPI assuming all cache hits
     * @param memoryAccessesPerInstruction Average memory accesses per instruction
     * @param missRate Cache miss rate (0.0 to 1.0)
     * @param missPenaltyCycles Cycles lost per cache miss
     * @return Effective CPI including cache miss stalls
     */
    public static double effectiveCPI(double baseCPI, double memoryAccessesPerInstruction,
                                       double missRate, int missPenaltyCycles) {
        double missStalls = memoryAccessesPerInstruction * missRate * missPenaltyCycles;
        return baseCPI + missStalls;
    }
    
    // ==================== AMDAHL'S LAW ====================
    
    /**
     * Calculate speedup using Amdahl's Law.
     * 
     * @param parallelFraction Fraction of program that can be parallelized (0.0 to 1.0)
     * @param speedupFactor Speedup of the parallel portion (e.g., number of processors)
     * @return Overall speedup
     */
    public static double amdahlSpeedup(double parallelFraction, double speedupFactor) {
        double serialFraction = 1.0 - parallelFraction;
        return 1.0 / (serialFraction + parallelFraction / speedupFactor);
    }
    
    /**
     * Calculate maximum speedup (infinite parallelization of parallel portion).
     */
    public static double amdahlMaxSpeedup(double parallelFraction) {
        return 1.0 / (1.0 - parallelFraction);
    }
    
    /**
     * Print Amdahl's Law table for various core counts.
     */
    public static String amdahlTable(double parallelFraction) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Amdahl's Law for %.0f%% parallel:\n", parallelFraction * 100));
        sb.append("┌──────────┬──────────┐\n");
        sb.append("│  Cores   │ Speedup  │\n");
        sb.append("├──────────┼──────────┤\n");
        
        int[] cores = {1, 2, 4, 8, 16, 64, 256};
        for (int n : cores) {
            double speedup = amdahlSpeedup(parallelFraction, n);
            sb.append(String.format("│ %8d │ %7.2fx │\n", n, speedup));
        }
        sb.append("├──────────┼──────────┤\n");
        sb.append(String.format("│    ∞     │ %7.2fx │\n", amdahlMaxSpeedup(parallelFraction)));
        sb.append("└──────────┴──────────┘\n");
        
        return sb.toString();
    }
    
    // ==================== POWER CALCULATIONS ====================
    
    /**
     * Calculate dynamic power consumption.
     * 
     * @param activityFactor Fraction of transistors switching (0.0 to 1.0)
     * @param capacitancePF Capacitance in picofarads
     * @param voltageV Voltage in volts
     * @param frequencyHz Clock frequency in Hz
     * @return Power in watts
     */
    public static double dynamicPower(double activityFactor, double capacitancePF,
                                       double voltageV, double frequencyHz) {
        double capacitanceFarads = capacitancePF * 1e-12;
        return activityFactor * capacitanceFarads * voltageV * voltageV * frequencyHz;
    }
    
    /**
     * Calculate how power changes with voltage and frequency scaling.
     */
    public static double powerScaling(double originalPower, double voltageRatio, double frequencyRatio) {
        // P ∝ V² × f
        return originalPower * voltageRatio * voltageRatio * frequencyRatio;
    }
    
    /**
     * Calculate energy for a task (power × time).
     */
    public static double energy(double powerWatts, double timeSeconds) {
        return powerWatts * timeSeconds;  // Joules
    }
    
    // ==================== AMAT CALCULATIONS ====================
    
    /**
     * Calculate Average Memory Access Time for single-level cache.
     * 
     * @param hitTimeCycles Time for cache hit (cycles)
     * @param missRate Cache miss rate (0.0 to 1.0)
     * @param missPenaltyCycles Time for cache miss (cycles)
     * @return AMAT in cycles
     */
    public static double amat(double hitTimeCycles, double missRate, double missPenaltyCycles) {
        return hitTimeCycles + missRate * missPenaltyCycles;
    }
    
    /**
     * Calculate AMAT for two-level cache.
     * 
     * @param l1HitTime L1 hit time (cycles)
     * @param l1MissRate L1 miss rate
     * @param l2HitTime L2 hit time (cycles)
     * @param l2MissRate L2 miss rate (of L1 misses, "local miss rate")
     * @param memoryAccessTime Main memory access time (cycles)
     * @return AMAT in cycles
     */
    public static double amatTwoLevel(double l1HitTime, double l1MissRate,
                                       double l2HitTime, double l2MissRate,
                                       double memoryAccessTime) {
        return l1HitTime + l1MissRate * (l2HitTime + l2MissRate * memoryAccessTime);
    }
    
    /**
     * Calculate global miss rate from local miss rates.
     * Global miss rate = product of local miss rates
     */
    public static double globalMissRate(double... localMissRates) {
        double global = 1.0;
        for (double rate : localMissRates) {
            global *= rate;
        }
        return global;
    }
    
    // ==================== SPEEDUP CALCULATIONS ====================
    
    /**
     * Calculate speedup.
     */
    public static double speedup(double oldTime, double newTime) {
        return oldTime / newTime;
    }
    
    /**
     * Calculate percentage improvement.
     */
    public static double percentImprovement(double oldValue, double newValue) {
        return (oldValue - newValue) / oldValue * 100;
    }
    
    // ==================== DEMONSTRATION ====================
    
    /**
     * Demonstrate Iron Law calculations.
     */
    public static String demonstrateIronLaw() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  IRON LAW DEMONSTRATION\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        // Example: Comparing two processors
        sb.append("Comparing two processors running the same program:\n\n");
        
        // Processor A: Simple, low CPI, low frequency
        long instructions = 1_000_000_000L;  // 1 billion instructions
        double cpiA = 1.0;
        double freqA = 2.0e9;  // 2 GHz
        double timeA = cpuTime(instructions, cpiA, freqA);
        
        // Processor B: Complex, higher CPI, higher frequency
        double cpiB = 1.5;
        double freqB = 3.0e9;  // 3 GHz
        double timeB = cpuTime(instructions, cpiB, freqB);
        
        sb.append("Processor A: CPI = 1.0, Frequency = 2.0 GHz\n");
        sb.append(String.format("  Time = %d × %.1f / %.0e = %.3f seconds\n\n", 
                               instructions, cpiA, freqA, timeA));
        
        sb.append("Processor B: CPI = 1.5, Frequency = 3.0 GHz\n");
        sb.append(String.format("  Time = %d × %.1f / %.0e = %.3f seconds\n\n",
                               instructions, cpiB, freqB, timeB));
        
        sb.append(String.format("Processor A is %.2fx faster despite lower frequency!\n", timeB/timeA));
        sb.append("(Lower CPI matters more than higher frequency in this case)\n");
        
        return sb.toString();
    }
    
    /**
     * Demonstrate AMAT calculations.
     */
    public static String demonstrateAMAT() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════════════════\n");
        sb.append("  AMAT (Average Memory Access Time) DEMONSTRATION\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        // Single-level cache
        double hitTime = 1;
        double missRate = 0.05;  // 5%
        double missPenalty = 100;
        double singleLevel = amat(hitTime, missRate, missPenalty);
        
        sb.append("Single-level cache:\n");
        sb.append("  Hit time = 1 cycle, Miss rate = 5%, Miss penalty = 100 cycles\n");
        sb.append(String.format("  AMAT = %.0f + %.2f × %.0f = %.1f cycles\n\n", 
                               hitTime, missRate, missPenalty, singleLevel));
        
        // Two-level cache
        double l1Hit = 1, l1Miss = 0.05;
        double l2Hit = 10, l2Miss = 0.20;
        double memTime = 100;
        double twoLevel = amatTwoLevel(l1Hit, l1Miss, l2Hit, l2Miss, memTime);
        
        sb.append("Two-level cache:\n");
        sb.append("  L1: Hit time = 1 cycle, Miss rate = 5%\n");
        sb.append("  L2: Hit time = 10 cycles, Miss rate = 20% (of L1 misses)\n");
        sb.append("  Memory: 100 cycles\n");
        sb.append(String.format("  AMAT = %.0f + %.2f × (%.0f + %.2f × %.0f) = %.2f cycles\n\n",
                               l1Hit, l1Miss, l2Hit, l2Miss, memTime, twoLevel));
        
        sb.append(String.format("L2 cache reduces AMAT from %.1f to %.2f cycles (%.0f%% improvement)\n",
                               singleLevel, twoLevel, percentImprovement(singleLevel, twoLevel)));
        
        return sb.toString();
    }
    
    /**
     * Run all demonstrations.
     */
    public static void main(String[] args) {
        System.out.println(demonstrateIronLaw());
        System.out.println(amdahlTable(0.90));
        System.out.println(demonstrateAMAT());
    }
}

