package computerdesign.theory;

/**
 * ParallelComputing - Understanding parallel architectures and performance.
 * 
 * Covers learning goals: T7.1, T7.2, T7.3
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WHY PARALLELISM?
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * The "Power Wall": Can't increase clock frequency indefinitely
 *   - Power ∝ V² × f
 *   - Higher frequency → more heat → thermal limits
 *   - Solution: Add more cores instead of faster cores
 * 
 * The "Memory Wall": Memory latency hasn't kept up with CPU speed
 *   - Solution: Caches, prefetching, parallel memory access
 * 
 * The "ILP Wall": Limited instruction-level parallelism in single thread
 *   - Solution: Thread-level and data-level parallelism
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * TYPES OF PARALLELISM
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 1. INSTRUCTION-LEVEL PARALLELISM (ILP)
 *    - Parallelism within a single instruction stream
 *    - Exploited by pipelining, superscalar execution
 *    - Limited by dependencies
 * 
 * 2. THREAD-LEVEL PARALLELISM (TLP)
 *    - Multiple threads executing concurrently
 *    - Requires multi-core or multi-processor
 *    - Requires explicit programming (threads, tasks)
 * 
 * 3. DATA-LEVEL PARALLELISM (DLP)
 *    - Same operation on multiple data elements
 *    - SIMD instructions, GPU computing
 *    - Works well for arrays, matrices, images
 * 
 * PARALLELISM IN TIME vs SPACE:
 *   - Time (pipelining): Different stages work on different instructions
 *   - Space (superscalar/VLIW): Multiple execution units work simultaneously
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * FLYNN'S TAXONOMY
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *                          Data Streams
 *                    Single          Multiple
 *                ┌──────────────┬──────────────┐
 *         Single │    SISD      │    SIMD      │
 *   Instruction  │ Uniprocessor │ Vector/GPU   │
 *      Streams   ├──────────────┼──────────────┤
 *         Multiple │   MISD      │    MIMD      │
 *                │   (rare)     │ Multicore    │
 *                └──────────────┴──────────────┘
 * 
 * SISD (Single Instruction, Single Data):
 *   - Traditional uniprocessor
 *   - One instruction operates on one data element at a time
 *   - Example: Classic single-core CPU
 * 
 * SIMD (Single Instruction, Multiple Data):
 *   - One instruction operates on multiple data elements
 *   - Examples: 
 *     - SSE/AVX instructions (x86): 4-8 floats per instruction
 *     - NEON (ARM): 4 floats per instruction
 *     - GPU shader execution
 *   - Best for: Graphics, scientific computing, machine learning
 * 
 * MISD (Multiple Instruction, Single Data):
 *   - Multiple operations on same data (rare)
 *   - Example: Fault-tolerant systems (same computation, compare results)
 * 
 * MIMD (Multiple Instruction, Multiple Data):
 *   - Multiple processors, each executing different instructions on different data
 *   - Examples: Multi-core CPUs, clusters, distributed systems
 *   - Most general and flexible
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * STRONG vs WEAK SCALING
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * STRONG SCALING:
 *   - Fixed problem size, increase processors
 *   - Goal: Reduce time to solution
 *   - Amdahl's Law applies!
 *   - Eventually: Communication overhead dominates
 * 
 *   Speedup = T(1) / T(P) where P = number of processors
 * 
 * WEAK SCALING:
 *   - Problem size grows with processors (constant work per processor)
 *   - Goal: Solve larger problems in same time
 *   - Better scalability (avoids Amdahl's limit)
 *   - But: Memory per processor may limit this
 * 
 *   Efficiency = T(1) / T(P) where problem size = P × base size
 * 
 * Example:
 *   Strong: 1000x1000 matrix on 1 core in 100s, on 4 cores in 25s (ideal)
 *   Weak:   1000x1000 on 1 core, 2000x2000 on 4 cores, both in 100s
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * ROOFLINE MODEL
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Answers: "Is my application memory-bound or compute-bound?"
 * 
 *   Attainable Performance (GFLOPS) = min(Peak GFLOPS, Peak BW × OI)
 * 
 *   WHERE:
 *     OI = Operational Intensity = FLOPs / Bytes accessed
 * 
 *   Performance (GFLOPS/s)
 *        │     ┌──────────────────── Peak Compute (compute-bound region)
 *        │     │ 
 *     1000├────┼────────────●═══════════════
 *        │    /│           /│
 *      100├───/─┼──────────/─┼──────────────
 *        │  /  │         /  │
 *       10├─/───┼────────/───┼──────────────
 *        │/    │       /    │
 *        │  memory-  /   compute-
 *        │  bound   /    bound
 *        │ region  /     region
 *        └─────────┼─────────┼──────────────
 *              0.1   1     10    100    OI (FLOP/Byte)
 *                    ↑
 *                 "Ridge Point"
 * 
 * OPERATIONAL INTENSITY (OI):
 *   - Low OI (< ridge point): Memory-bound
 *     → Solution: Improve cache usage, reduce memory traffic
 *   - High OI (> ridge point): Compute-bound
 *     → Solution: Use more cores, SIMD, faster algorithms
 * 
 * Examples:
 *   - DAXPY (y = ax + y):   OI ≈ 0.25 (memory-bound)
 *   - Matrix multiply:      OI ≈ O(n) (can be compute-bound for large n)
 *   - Stencil codes:        OI ≈ 1-10 (depends on stencil size)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * MULTIPROCESSOR ARCHITECTURES
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * SHARED MEMORY (SMP/UMA):
 *   ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐
 *   │ Core0 │ │ Core1 │ │ Core2 │ │ Core3 │
 *   └───┬───┘ └───┬───┘ └───┬───┘ └───┬───┘
 *       │         │         │         │
 *   ════╪═════════╪═════════╪═════════╪════  (Bus/Interconnect)
 *                     │
 *               ┌─────┴─────┐
 *               │  Shared   │
 *               │  Memory   │
 *               └───────────┘
 *   
 *   + Simple programming model
 *   + Low latency for shared data
 *   - Scalability limited by bus/interconnect
 *   - Cache coherence overhead
 * 
 * DISTRIBUTED MEMORY:
 *   ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
 *   │ ┌───┐ ┌─────┐ │   │ ┌───┐ ┌─────┐ │   │ ┌───┐ ┌─────┐ │
 *   │ │CPU│ │Mem  │ │   │ │CPU│ │Mem  │ │   │ │CPU│ │Mem  │ │
 *   │ └───┘ └─────┘ │   │ └───┘ └─────┘ │   │ └───┘ └─────┘ │
 *   │    Node 0     │   │    Node 1     │   │    Node 2     │
 *   └───────┬───────┘   └───────┬───────┘   └───────┬───────┘
 *           │                   │                   │
 *   ════════╪═══════════════════╪═══════════════════╪════════
 *                         (Network)
 *   
 *   + Scales to thousands of nodes
 *   + Each node independent (no coherence)
 *   - Explicit data movement (MPI)
 *   - Higher latency for remote data
 * 
 * MODERN MULTI-CORE (NUMA):
 *   Hybrid: Shared memory within node, distributed between nodes
 *   NUMA = Non-Uniform Memory Access
 *   - Local memory is fast, remote memory is slow
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * CACHE COHERENCE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * PROBLEM: Each core has private cache. What if core 0 writes data that
 * core 1 has cached?
 * 
 *   Core 0 Cache: X = 5 (modified)
 *   Core 1 Cache: X = 3 (stale!)
 *   Memory:       X = 3
 * 
 * COHERENCE DEFINITION:
 *   1. A read returns the most recently written value
 *   2. Writes are eventually visible to all cores
 *   3. Writes to same location are serialized
 * 
 * SNOOPING PROTOCOL (MSI/MESI):
 *   Each cache line has a state:
 *   - Modified (M): Dirty, only copy
 *   - Exclusive (E): Clean, only copy
 *   - Shared (S): Clean, may have copies
 *   - Invalid (I): Not valid
 * 
 *   Caches "snoop" on bus transactions:
 *   - When core 0 writes, core 1 sees this and invalidates its copy
 *   - When core 1 reads, core 0 may need to provide its modified copy
 * 
 * DIRECTORY PROTOCOL (for larger systems):
 *   - Central directory tracks which caches have each block
 *   - Scales better than snooping for many cores
 * 
 * FALSE SHARING:
 *   Two variables on same cache line, different cores write to each
 *   → Constant invalidations even though no true sharing!
 *   Solution: Pad data to cache line boundaries
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * MEMORY CONSISTENCY
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * PROBLEM: In what order do other cores see writes?
 * 
 *   Core 0:              Core 1:
 *   X = 1                while (Y == 0) {}
 *   Y = 1                print(X)
 * 
 *   Can Core 1 print 0? Depends on memory consistency model!
 * 
 * SEQUENTIAL CONSISTENCY:
 *   - Strongest model
 *   - All cores see same order of all operations
 *   - Easy to program, but limits hardware optimizations
 * 
 * RELAXED CONSISTENCY:
 *   - Allow reordering of independent operations
 *   - Use memory barriers/fences when order matters
 *   - Better performance, harder to program
 * 
 * x86: Total Store Order (TSO) - relatively strong
 * ARM/RISC-V: Relaxed - requires explicit barriers
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * SYNCHRONIZATION
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * NEED: Mutual exclusion, ordering, communication
 * 
 * LOCK (Mutex):
 *   acquire_lock(&mutex);
 *   // critical section
 *   release_lock(&mutex);
 * 
 * IMPLEMENTATION: Atomic instructions
 *   - Test-and-set
 *   - Compare-and-swap (CAS)
 *   - Load-linked/Store-conditional (LL/SC)
 * 
 * RISC-V: LR (Load Reserved) / SC (Store Conditional)
 *   lr.w t0, (a0)      // Load and reserve
 *   sc.w t1, t2, (a0)  // Store if still reserved
 *   bnez t1, retry     // Retry if store failed
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * GPU ARCHITECTURE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   ┌─────────────────────────────────────────────────────────────────┐
 *   │                           GPU                                  │
 *   │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
 *   │  │    SM    │ │    SM    │ │    SM    │ │    SM    │  ...      │
 *   │  │ ┌──────┐ │ │ ┌──────┐ │ │ ┌──────┐ │ │ ┌──────┐ │           │
 *   │  │ │ Warp │ │ │ │ Warp │ │ │ │ Warp │ │ │ │ Warp │ │           │
 *   │  │ │ (32) │ │ │ │ (32) │ │ │ │ (32) │ │ │ │ (32) │ │           │
 *   │  │ └──────┘ │ │ └──────┘ │ │ └──────┘ │ │ └──────┘ │           │
 *   │  │ Shared   │ │ Shared   │ │ Shared   │ │ Shared   │           │
 *   │  │ Memory   │ │ Memory   │ │ Memory   │ │ Memory   │           │
 *   │  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
 *   │                                                                 │
 *   │  ═══════════════════════════════════════════════════════════   │
 *   │                        Global Memory (DRAM)                    │
 *   └─────────────────────────────────────────────────────────────────┘
 * 
 * KEY CONCEPTS:
 *   - SM (Streaming Multiprocessor): Contains many simple cores
 *   - Warp: 32 threads executing same instruction (SIMT = SIMD + threads)
 *   - Thread block: Group of warps sharing shared memory
 *   - Grid: All thread blocks for a kernel launch
 * 
 * PROGRAMMING MODEL (CUDA/OpenCL):
 *   - Write kernel function for single thread
 *   - Launch thousands/millions of threads
 *   - GPU schedules warps across SMs
 * 
 * GPU vs CPU:
 *   - CPU: Few powerful cores, optimized for latency
 *   - GPU: Many simple cores, optimized for throughput
 *   - GPU: Higher memory bandwidth, but higher latency
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * VECTOR PROCESSORS vs GPU vs SIMD
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   SIMD Instructions (SSE/AVX):
 *   - Fixed-width vectors (128/256/512 bits)
 *   - Part of CPU instruction set
 *   - Programmer/compiler uses explicitly
 *   - Low overhead, integrated with scalar code
 * 
 *   Vector Processors:
 *   - Variable-length vectors (up to thousands of elements)
 *   - Dedicated vector registers and functional units
 *   - Historical: Cray supercomputers
 *   - Modern: RISC-V Vector Extension
 * 
 *   GPUs:
 *   - SIMT model (warps of 32 threads)
 *   - Separate device memory, kernel launches
 *   - Massively parallel (thousands of cores)
 *   - Best for data-parallel workloads
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * DOMAIN-SPECIFIC ACCELERATORS (DSA)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * WHY DSA?
 *   - End of Dennard scaling: Can't just make transistors faster
 *   - Specialization trades generality for efficiency
 *   - 10-1000x better performance/watt for specific workloads
 * 
 * EXAMPLES:
 *   - TPU (Tensor Processing Unit): Matrix operations for ML
 *     - Systolic array architecture
 *     - High memory bandwidth, low precision
 *   
 *   - Neural network accelerators: Inference in edge devices
 *   - Video encoders/decoders: H.264/H.265
 *   - Cryptocurrency miners: SHA-256 hashing
 *   - Network processors: Packet processing
 * 
 * HETEROGENEOUS COMPUTING:
 *   Modern systems combine: CPU + GPU + DSAs
 *   Each handles workloads it's best suited for
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class ParallelComputing {
    
    // ==================== AMDAHL'S LAW ====================
    
    /**
     * Amdahl's Law: Maximum speedup with parallelization.
     * 
     * @param parallelFraction Fraction of work that can be parallelized (0-1)
     * @param processors Number of processors
     * @return Speedup factor
     */
    public static double amdahlSpeedup(double parallelFraction, int processors) {
        double serialFraction = 1.0 - parallelFraction;
        return 1.0 / (serialFraction + parallelFraction / processors);
    }
    
    /**
     * Maximum possible speedup (with infinite processors).
     */
    public static double amdahlMaxSpeedup(double parallelFraction) {
        return 1.0 / (1.0 - parallelFraction);
    }
    
    // ==================== ROOFLINE MODEL ====================
    
    /**
     * Roofline model: Attainable performance for a given operational intensity.
     * 
     * @param peakGFLOPS Peak compute performance (GFLOPS)
     * @param memoryBandwidth Memory bandwidth (GB/s)
     * @param operationalIntensity FLOPs per byte of memory accessed
     * @return Attainable performance (GFLOPS)
     */
    public static double rooflinePerformance(double peakGFLOPS, 
                                              double memoryBandwidth,
                                              double operationalIntensity) {
        double memoryBound = memoryBandwidth * operationalIntensity;
        return Math.min(peakGFLOPS, memoryBound);
    }
    
    /**
     * Calculate the ridge point (transition from memory-bound to compute-bound).
     */
    public static double ridgePoint(double peakGFLOPS, double memoryBandwidth) {
        return peakGFLOPS / memoryBandwidth;
    }
    
    /**
     * Determine if workload is memory-bound or compute-bound.
     */
    public static String boundedBy(double peakGFLOPS, 
                                    double memoryBandwidth,
                                    double operationalIntensity) {
        double ridge = ridgePoint(peakGFLOPS, memoryBandwidth);
        if (operationalIntensity < ridge) {
            return "MEMORY-BOUND (increase cache efficiency, reduce memory traffic)";
        } else {
            return "COMPUTE-BOUND (use more cores, SIMD, or faster algorithms)";
        }
    }
    
    // ==================== OPERATIONAL INTENSITY ====================
    
    /**
     * Operational intensity for DAXPY: Y = alpha*X + Y
     * Each element: 2 FLOPs (multiply, add), 24 bytes (read X, read Y, write Y, 8 bytes each)
     */
    public static double daxpyOI() {
        return 2.0 / 24.0;  // ~0.083 FLOP/byte
    }
    
    /**
     * Operational intensity for matrix multiply (naive, C = A × B).
     * For N×N matrices: 2N³ FLOPs, 3N² × 8 bytes (read A, B, write C)
     */
    public static double matmulOI(int n) {
        double flops = 2.0 * n * n * n;
        double bytes = 3.0 * n * n * 8;  // doubles = 8 bytes
        return flops / bytes;
    }
    
    // ==================== DEMONSTRATIONS ====================
    
    /**
     * Demonstrate Amdahl's Law.
     */
    public static String demonstrateAmdahl() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  AMDAHL'S LAW DEMONSTRATION\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        double[] fractions = {0.5, 0.75, 0.90, 0.95, 0.99};
        int[] processors = {1, 2, 4, 8, 16, 64, 256};
        
        sb.append("Speedup for different parallel fractions and processor counts:\n\n");
        sb.append("Parallel │");
        for (int p : processors) {
            sb.append(String.format(" %4d P│", p));
        }
        sb.append("  Max  │\n");
        sb.append("────────┼");
        for (int i = 0; i < processors.length; i++) {
            sb.append("───────┼");
        }
        sb.append("───────┤\n");
        
        for (double f : fractions) {
            sb.append(String.format("  %3.0f%%  │", f * 100));
            for (int p : processors) {
                double speedup = amdahlSpeedup(f, p);
                sb.append(String.format(" %5.1fx│", speedup));
            }
            sb.append(String.format(" %5.1fx│\n", amdahlMaxSpeedup(f)));
        }
        
        sb.append("\nKey insight: Even 95% parallel, max speedup is only 20x!\n");
        sb.append("Serial bottleneck severely limits scalability.\n");
        
        return sb.toString();
    }
    
    /**
     * Demonstrate Roofline model.
     */
    public static String demonstrateRoofline() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════════════════\n");
        sb.append("  ROOFLINE MODEL DEMONSTRATION\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        // Example system: modern CPU core
        double peakGFLOPS = 100;    // 100 GFLOPS peak
        double memBandwidth = 50;   // 50 GB/s memory bandwidth
        double ridge = ridgePoint(peakGFLOPS, memBandwidth);
        
        sb.append(String.format("System: Peak = %.0f GFLOPS, Bandwidth = %.0f GB/s\n", 
                               peakGFLOPS, memBandwidth));
        sb.append(String.format("Ridge Point: %.1f FLOP/Byte\n\n", ridge));
        
        // Example workloads
        sb.append("Workload Analysis:\n\n");
        
        // DAXPY
        double daxpyOI = daxpyOI();
        double daxpyPerf = rooflinePerformance(peakGFLOPS, memBandwidth, daxpyOI);
        sb.append(String.format("  DAXPY (Y = aX + Y):\n"));
        sb.append(String.format("    OI = %.3f FLOP/Byte\n", daxpyOI));
        sb.append(String.format("    Attainable: %.1f GFLOPS (%.0f%% of peak)\n", 
                               daxpyPerf, 100*daxpyPerf/peakGFLOPS));
        sb.append(String.format("    %s\n\n", boundedBy(peakGFLOPS, memBandwidth, daxpyOI)));
        
        // Small matrix multiply
        int n = 64;
        double mm64OI = matmulOI(n);
        double mm64Perf = rooflinePerformance(peakGFLOPS, memBandwidth, mm64OI);
        sb.append(String.format("  Matrix Multiply (%dx%d):\n", n, n));
        sb.append(String.format("    OI = %.1f FLOP/Byte\n", mm64OI));
        sb.append(String.format("    Attainable: %.1f GFLOPS (%.0f%% of peak)\n", 
                               mm64Perf, 100*mm64Perf/peakGFLOPS));
        sb.append(String.format("    %s\n\n", boundedBy(peakGFLOPS, memBandwidth, mm64OI)));
        
        // Large matrix multiply
        n = 1000;
        double mm1000OI = matmulOI(n);
        double mm1000Perf = rooflinePerformance(peakGFLOPS, memBandwidth, mm1000OI);
        sb.append(String.format("  Matrix Multiply (%dx%d):\n", n, n));
        sb.append(String.format("    OI = %.1f FLOP/Byte\n", mm1000OI));
        sb.append(String.format("    Attainable: %.1f GFLOPS (%.0f%% of peak)\n", 
                               mm1000Perf, 100*mm1000Perf/peakGFLOPS));
        sb.append(String.format("    %s\n\n", boundedBy(peakGFLOPS, memBandwidth, mm1000OI)));
        
        return sb.toString();
    }
    
    /**
     * Demonstrate Flynn's taxonomy with examples.
     */
    public static String demonstrateFlynn() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════════════════\n");
        sb.append("  FLYNN'S TAXONOMY\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("┌─────────────┬─────────────────────┬─────────────────────┐\n");
        sb.append("│             │ Single Data Stream  │ Multiple Data Strm  │\n");
        sb.append("├─────────────┼─────────────────────┼─────────────────────┤\n");
        sb.append("│ Single Instr│       SISD          │       SIMD          │\n");
        sb.append("│             │  Classic CPU core   │  Vector/GPU/SSE     │\n");
        sb.append("├─────────────┼─────────────────────┼─────────────────────┤\n");
        sb.append("│ Multiple    │       MISD          │       MIMD          │\n");
        sb.append("│ Instructions│  (Rare: redundant)  │  Multicore/Cluster  │\n");
        sb.append("└─────────────┴─────────────────────┴─────────────────────┘\n\n");
        
        sb.append("SISD Example: for (i=0; i<N; i++) C[i] = A[i] + B[i];\n");
        sb.append("  → One addition per cycle\n\n");
        
        sb.append("SIMD Example: Same loop with AVX-256\n");
        sb.append("  → Four additions per cycle (32-bit floats × 8 = 256 bits)\n");
        sb.append("  → 8x throughput improvement for this operation\n\n");
        
        sb.append("MIMD Example: Four threads on four cores\n");
        sb.append("  → Each thread processes N/4 elements independently\n");
        sb.append("  → 4x throughput (ideal, ignoring overhead)\n\n");
        
        return sb.toString();
    }
    
    /**
     * Demonstrate cache coherence scenarios.
     */
    public static String demonstrateCacheCoherence() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════════════════\n");
        sb.append("  CACHE COHERENCE - MESI PROTOCOL\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("States:\n");
        sb.append("  M (Modified): Dirty, exclusive copy\n");
        sb.append("  E (Exclusive): Clean, exclusive copy\n");
        sb.append("  S (Shared): Clean, may have copies elsewhere\n");
        sb.append("  I (Invalid): Not valid\n\n");
        
        sb.append("Scenario: Core 0 and Core 1 access variable X\n\n");
        
        sb.append("Step 1: Core 0 reads X (cache miss)\n");
        sb.append("  Core 0 cache: X = 5 (E - Exclusive)\n");
        sb.append("  Core 1 cache: X = Invalid\n");
        sb.append("  Memory: X = 5\n\n");
        
        sb.append("Step 2: Core 1 reads X (cache miss, Core 0 responds)\n");
        sb.append("  Core 0 cache: X = 5 (S - Shared)\n");
        sb.append("  Core 1 cache: X = 5 (S - Shared)\n");
        sb.append("  Memory: X = 5\n\n");
        
        sb.append("Step 3: Core 0 writes X = 10 (invalidates Core 1)\n");
        sb.append("  Core 0 cache: X = 10 (M - Modified)\n");
        sb.append("  Core 1 cache: X = Invalid\n");
        sb.append("  Memory: X = 5 (stale, but Core 0 has correct value)\n\n");
        
        sb.append("Step 4: Core 1 reads X (Core 0 provides, writes back)\n");
        sb.append("  Core 0 cache: X = 10 (S - Shared)\n");
        sb.append("  Core 1 cache: X = 10 (S - Shared)\n");
        sb.append("  Memory: X = 10 (updated)\n\n");
        
        sb.append("Key: Coherence protocol ensures all cores see consistent data!\n");
        
        return sb.toString();
    }
    
    // ==================== MAIN ====================
    
    public static void main(String[] args) {
        System.out.println(demonstrateAmdahl());
        System.out.println(demonstrateFlynn());
        System.out.println(demonstrateRoofline());
        System.out.println(demonstrateCacheCoherence());
    }
}

