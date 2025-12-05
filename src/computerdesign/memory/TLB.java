package computerdesign.memory;

/**
 * TLB - Translation Lookaside Buffer.
 * 
 * Covers learning goals: T6.3 (Virtual memory, TLB, address translation)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE PROBLEM: PAGE TABLE ACCESS IS SLOW
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Without TLB, every memory access requires TWO memory accesses:
 *   1. Access page table to translate virtual → physical address
 *   2. Access the actual data at the physical address
 * 
 * This DOUBLES memory latency - unacceptable!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE SOLUTION: TLB (Translation Lookaside Buffer)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * The TLB is a SMALL, FAST cache of recent virtual→physical translations.
 * 
 *   Virtual Address
 *         │
 *         ▼
 *   ┌─────────────┐    Hit     ┌──────────────────────────────────────────┐
 *   │    TLB      │───────────►│ Physical Address (fast - 1 cycle)        │
 *   │  (32-128    │            └──────────────────────────────────────────┘
 *   │  entries)   │
 *   └─────┬───────┘
 *         │ Miss
 *         ▼
 *   ┌─────────────┐            ┌──────────────────────────────────────────┐
 *   │ Page Table  │───────────►│ Physical Address (slow - 10-100 cycles)  │
 *   │ (in memory) │            │ + update TLB with new translation        │
 *   └─────────────┘            └──────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * TLB STRUCTURE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Each TLB entry contains:
 * 
 *   ┌───────────────────────────────────────────────────────────────────────────┐
 *   │ Valid │  VPN  │  PFN  │ Permissions │ ASID │ Global │ Dirty │ Accessed   │
 *   │ (1b)  │(20b)  │(20b)  │   (RWX)     │ (8b) │  (1b)  │ (1b)  │   (1b)     │
 *   └───────────────────────────────────────────────────────────────────────────┘
 * 
 * WHERE:
 *   VPN = Virtual Page Number (the lookup key)
 *   PFN = Physical Frame Number (the translation result)
 *   ASID = Address Space ID (identifies which process owns this entry)
 *   Global = If set, entry is valid for all ASIDs (kernel mappings)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * TLB ASSOCIATIVITY
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * TLBs are typically FULLY ASSOCIATIVE or HIGHLY SET-ASSOCIATIVE because:
 *   - Small size (32-128 entries) makes full search feasible
 *   - Conflict misses are very expensive (page table walk)
 *   - Each entry covers 4KB (or more), so spatial locality helps less
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * TLB MISS HANDLING
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Hardware-managed TLB (x86, ARM):
 *   - Hardware walks page table automatically
 *   - Transparent to software
 *   - More complex hardware
 * 
 * Software-managed TLB (MIPS, some RISC-V):
 *   - TLB miss causes exception
 *   - OS handler walks page table, fills TLB
 *   - Simpler hardware, more flexible
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * CONTEXT SWITCHES AND THE TLB
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Problem: When switching processes, TLB contains stale translations!
 * 
 * Solutions:
 *   1. Flush TLB on context switch (simple but slow - "TLB shootdown")
 *   2. Use ASID: Tag entries with process ID, no flush needed
 *   3. Global bit: Kernel pages valid across all contexts
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * TLB IN THE PIPELINE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * TLB access must happen BEFORE cache access (for physically-indexed cache):
 * 
 *   ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐
 *   │  Fetch  │ → │ Decode  │ → │ Execute │ → │ Memory  │
 *   └─────────┘   └─────────┘   └─────────┘   └────┬────┘
 *                                                  │
 *                               ┌──────────────────┴──────────────────┐
 *                               │ 1. TLB lookup (virtual → physical)  │
 *                               │ 2. Cache access (with physical addr)│
 *                               └─────────────────────────────────────┘
 * 
 * Virtually-indexed, physically-tagged (VIPT) cache can do both in parallel:
 *   - Use virtual address for cache index (no translation needed)
 *   - Use physical address for cache tag (from TLB)
 *   - Overlaps TLB access with cache access!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class TLB {
    
    /** TLB entry structure. */
    public static class TLBEntry {
        public boolean valid;
        public int vpn;           // Virtual Page Number
        public int pfn;           // Physical Frame Number
        public boolean readable;
        public boolean writable;
        public boolean executable;
        public int asid;          // Address Space ID (process ID)
        public boolean global;    // Valid for all ASIDs
        public boolean dirty;
        public boolean accessed;
        public long lastUsed;     // For LRU replacement
        
        public TLBEntry() {
            this.valid = false;
        }
        
        public void invalidate() {
            this.valid = false;
        }
        
        @Override
        public String toString() {
            if (!valid) return "[invalid]";
            String perms = (readable ? "R" : "-") + 
                          (writable ? "W" : "-") + 
                          (executable ? "X" : "-");
            return String.format("VPN=0x%05X → PFN=0x%05X [%s] ASID=%d%s%s",
                vpn, pfn, perms, asid, 
                global ? " GLOBAL" : "",
                dirty ? " DIRTY" : "");
        }
    }
    
    /** Result of a TLB lookup. */
    public static class TLBResult {
        public final boolean hit;
        public final int physicalAddress;
        public final TLBEntry entry;
        
        private TLBResult(boolean hit, int physicalAddress, TLBEntry entry) {
            this.hit = hit;
            this.physicalAddress = physicalAddress;
            this.entry = entry;
        }
        
        public static TLBResult hit(int physicalAddress, TLBEntry entry) {
            return new TLBResult(true, physicalAddress, entry);
        }
        
        public static TLBResult miss() {
            return new TLBResult(false, 0, null);
        }
    }
    
    private final TLBEntry[] entries;
    private final int numEntries;
    private final int pageSizeBits;  // log2(page size)
    
    private int currentAsid = 0;
    private long accessCounter = 0;
    
    // Statistics
    private int hits = 0;
    private int misses = 0;
    
    /**
     * Create a TLB.
     * 
     * @param numEntries Number of TLB entries (typically 32-128)
     * @param pageSize Page size in bytes (e.g., 4096)
     */
    public TLB(int numEntries, int pageSize) {
        this.numEntries = numEntries;
        this.pageSizeBits = log2(pageSize);
        this.entries = new TLBEntry[numEntries];
        
        for (int i = 0; i < numEntries; i++) {
            entries[i] = new TLBEntry();
        }
    }
    
    /**
     * Look up a virtual address in the TLB.
     * 
     * @param virtualAddress The virtual address to translate
     * @return TLBResult with hit/miss status and physical address if hit
     */
    public TLBResult lookup(int virtualAddress) {
        accessCounter++;
        int vpn = virtualAddress >>> pageSizeBits;
        int offset = virtualAddress & ((1 << pageSizeBits) - 1);
        
        // Fully associative search
        for (TLBEntry entry : entries) {
            if (entry.valid && entry.vpn == vpn) {
                // Check ASID (or global)
                if (entry.global || entry.asid == currentAsid) {
                    hits++;
                    entry.accessed = true;
                    entry.lastUsed = accessCounter;
                    
                    int physicalAddress = (entry.pfn << pageSizeBits) | offset;
                    return TLBResult.hit(physicalAddress, entry);
                }
            }
        }
        
        misses++;
        return TLBResult.miss();
    }
    
    /**
     * Insert a new translation into the TLB.
     * Uses LRU replacement if TLB is full.
     */
    public void insert(int vpn, int pfn, boolean readable, boolean writable, 
                       boolean executable, boolean global) {
        // First, look for an invalid entry
        for (TLBEntry entry : entries) {
            if (!entry.valid) {
                fillEntry(entry, vpn, pfn, readable, writable, executable, global);
                return;
            }
        }
        
        // All entries valid, use LRU replacement
        TLBEntry victim = selectVictimLRU();
        fillEntry(victim, vpn, pfn, readable, writable, executable, global);
    }
    
    private void fillEntry(TLBEntry entry, int vpn, int pfn, 
                          boolean readable, boolean writable, 
                          boolean executable, boolean global) {
        entry.valid = true;
        entry.vpn = vpn;
        entry.pfn = pfn;
        entry.readable = readable;
        entry.writable = writable;
        entry.executable = executable;
        entry.asid = currentAsid;
        entry.global = global;
        entry.dirty = false;
        entry.accessed = true;
        entry.lastUsed = accessCounter;
    }
    
    private TLBEntry selectVictimLRU() {
        TLBEntry victim = entries[0];
        for (TLBEntry entry : entries) {
            if (entry.lastUsed < victim.lastUsed) {
                victim = entry;
            }
        }
        return victim;
    }
    
    /**
     * Invalidate a specific VPN (e.g., when page is unmapped).
     */
    public void invalidate(int vpn) {
        for (TLBEntry entry : entries) {
            if (entry.valid && entry.vpn == vpn) {
                if (entry.global || entry.asid == currentAsid) {
                    entry.invalidate();
                }
            }
        }
    }
    
    /**
     * Flush all non-global entries (on context switch without ASID).
     */
    public void flush() {
        for (TLBEntry entry : entries) {
            if (!entry.global) {
                entry.invalidate();
            }
        }
    }
    
    /**
     * Flush all entries for a specific ASID.
     */
    public void flushAsid(int asid) {
        for (TLBEntry entry : entries) {
            if (entry.asid == asid && !entry.global) {
                entry.invalidate();
            }
        }
    }
    
    /**
     * Flush entire TLB including global entries.
     */
    public void flushAll() {
        for (TLBEntry entry : entries) {
            entry.invalidate();
        }
    }
    
    /**
     * Set the current Address Space ID (for context switches).
     */
    public void setAsid(int asid) {
        this.currentAsid = asid;
    }
    
    public int getAsid() {
        return currentAsid;
    }
    
    /**
     * Mark a TLB entry as dirty (on write).
     */
    public void markDirty(int vpn) {
        for (TLBEntry entry : entries) {
            if (entry.valid && entry.vpn == vpn) {
                if (entry.global || entry.asid == currentAsid) {
                    entry.dirty = true;
                }
            }
        }
    }
    
    // Statistics
    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    
    public double getHitRate() {
        int total = hits + misses;
        return total == 0 ? 0 : (double) hits / total;
    }
    
    public void resetStats() {
        hits = 0;
        misses = 0;
    }
    
    private static int log2(int n) {
        return (int) (Math.log(n) / Math.log(2));
    }
    
    @Override
    public String toString() {
        return String.format("TLB: %d entries, hit rate: %.1f%% (%d hits, %d misses)",
            numEntries, getHitRate() * 100, hits, misses);
    }
    
    /**
     * Dump TLB contents (for debugging).
     */
    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TLB Dump ===\n");
        sb.append(String.format("Current ASID: %d\n", currentAsid));
        sb.append(String.format("Statistics: %d hits, %d misses (%.1f%% hit rate)\n\n",
            hits, misses, getHitRate() * 100));
        
        int validCount = 0;
        for (int i = 0; i < numEntries; i++) {
            if (entries[i].valid) {
                sb.append(String.format("  [%2d] %s\n", i, entries[i]));
                validCount++;
            }
        }
        
        sb.append(String.format("\n%d/%d entries in use\n", validCount, numEntries));
        return sb.toString();
    }
}

