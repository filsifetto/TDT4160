package computerdesign.memory;

import java.util.HashMap;
import java.util.Map;

/**
 * PageTable - the per-process mapping from virtual pages to physical frames.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * FUNDAMENTAL CONCEPT: THE PAGE TABLE IS PROCESS STATE!
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Each process has its own page table, giving it a private virtual address space.
 * The OS manages page tables; the MMU (hardware) uses them for translation.
 * 
 * WHY THIS MATTERS:
 * - Process A and Process B can both use virtual address 0x1000
 * - But their page tables map 0x1000 to DIFFERENT physical frames
 * - This provides ISOLATION without requiring processes to coordinate
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * VIRTUAL ADDRESS STRUCTURE (32-bit, 4KB pages)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *    31                    12 11                 0
 *   ┌────────────────────────┬───────────────────┐
 *   │  Virtual Page Number   │    Page Offset    │
 *   │       (VPN)            │                   │
 *   │      20 bits           │     12 bits       │
 *   └────────────────────────┴───────────────────┘
 *            │                        │
 *            │                        └─► Unchanged in translation
 *            │                            (same offset within the page)
 *            │
 *            └─► Index into page table
 *                Maps to Physical Frame Number (PFN)
 * 
 * ADDRESS TRANSLATION:
 *   Physical Address = (PageTable[VPN].PFN << 12) | Offset
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * PAGE TABLE ENTRY (PTE) STRUCTURE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │ PFN (20 bits) │ Flags: V │ R │ W │ X │ D │ A │ P │ ...     │
 *   └─────────────────────────────────────────────────────────────┘
 *                      │   │   │   │   │   │   │
 *                      │   │   │   │   │   │   └─ Present (in RAM vs swapped)
 *                      │   │   │   │   │   └───── Accessed (for LRU)
 *                      │   │   │   │   └───────── Dirty (written to)
 *                      │   │   │   └───────────── eXecutable
 *                      │   │   └───────────────── Writable
 *                      │   └───────────────────── Readable
 *                      └───────────────────────── Valid (mapping exists)
 * 
 * PROTECTION BITS EXAMPLES:
 *   - Code segment:  R-X (readable, executable, NOT writable)
 *   - Data segment:  RW- (readable, writable, NOT executable)
 *   - Read-only:     R-- (e.g., constants, shared libraries)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * PAGE FAULTS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * A page fault occurs when:
 *   1. Valid bit is 0 (page not mapped) → OS must allocate a frame
 *   2. Present bit is 0 (page swapped out) → OS must load from disk
 *   3. Permission violation → OS terminates process (segmentation fault)
 * 
 * Page fault handling:
 *   1. MMU raises exception, saves faulting address
 *   2. OS page fault handler runs
 *   3. OS allocates physical frame (may need to evict another page)
 *   4. OS updates page table entry
 *   5. OS returns to faulting instruction (retry)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @see VirtualMemory - Uses this page table for address translation
 * @see MainMemory - The physical memory that frames map to
 */
public class PageTable {
    
    /**
     * Page Table Entry (PTE) - represents one virtual-to-physical mapping.
     * 
     * Each PTE maps ONE virtual page to ONE physical frame.
     * The PTE contains both the mapping (frame number) and metadata (flags).
     * 
     * FLAG PURPOSES:
     * - valid:      Has this page ever been mapped? Used to detect access to 
     *               unallocated memory (causes segmentation fault).
     * 
     * - readable:   Can instructions read from this page? Almost always true.
     * 
     * - writable:   Can instructions write to this page? False for code segments
     *               and read-only data. Violation = segmentation fault.
     * 
     * - executable: Can the CPU fetch instructions from this page? True for code,
     *               false for data. Prevents code injection attacks (DEP/NX bit).
     * 
     * - dirty:      Has this page been written to since loaded? If true, the page
     *               must be written back to disk before the frame can be reused.
     *               Huge performance optimization: clean pages can be discarded!
     * 
     * - accessed:   Has this page been read/written recently? Used by page
     *               replacement algorithms (LRU approximation). OS clears this
     *               periodically; hardware sets it on access.
     * 
     * - present:    Is the page currently in physical RAM? If false, the page
     *               has been swapped to disk. Access triggers a page fault.
     */
    public static class PageTableEntry {
        private int frameNumber;     // Physical frame number (PFN)
        private boolean valid;       // Is this mapping valid?
        private boolean readable;    // Permission: can read?
        private boolean writable;    // Permission: can write?
        private boolean executable;  // Permission: can execute?
        private boolean dirty;       // Status: has been modified?
        private boolean accessed;    // Status: has been accessed recently?
        private boolean present;     // Status: is in physical memory?
        
        public PageTableEntry() {
            this.valid = false;
            this.readable = true;
            this.writable = true;
            this.executable = false;
            this.dirty = false;
            this.accessed = false;
            this.present = false;
            this.frameNumber = -1;
        }
        
        public PageTableEntry(int frameNumber, boolean readable, boolean writable, boolean executable) {
            this.frameNumber = frameNumber;
            this.valid = true;
            this.present = true;
            this.readable = readable;
            this.writable = writable;
            this.executable = executable;
            this.dirty = false;
            this.accessed = false;
        }
        
        // Getters and setters
        public int getFrameNumber() { return frameNumber; }
        public void setFrameNumber(int frameNumber) { 
            this.frameNumber = frameNumber;
            this.present = true;
        }
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public boolean isReadable() { return readable; }
        public void setReadable(boolean readable) { this.readable = readable; }
        
        public boolean isWritable() { return writable; }
        public void setWritable(boolean writable) { this.writable = writable; }
        
        public boolean isExecutable() { return executable; }
        public void setExecutable(boolean executable) { this.executable = executable; }
        
        public boolean isDirty() { return dirty; }
        public void setDirty(boolean dirty) { this.dirty = dirty; }
        
        public boolean isAccessed() { return accessed; }
        public void setAccessed(boolean accessed) { this.accessed = accessed; }
        
        public boolean isPresent() { return present; }
        public void setPresent(boolean present) { this.present = present; }
        
        @Override
        public String toString() {
            if (!valid) return "[invalid]";
            return String.format("[frame=%d, %s%s%s%s%s]",
                frameNumber,
                readable ? "R" : "-",
                writable ? "W" : "-",
                executable ? "X" : "-",
                dirty ? " dirty" : "",
                present ? "" : " swapped");
        }
    }
    
    // The actual page table - maps VPN to PTE
    private final Map<Integer, PageTableEntry> entries;
    
    // Page table configuration
    private final int pageSize;        // Bytes per page (typically 4KB)
    private final int offsetBits;      // Number of bits for page offset
    
    // Statistics
    private long lookups;
    private long hits;
    private long pageFaults;
    
    /**
     * Create a page table with default 4KB pages.
     */
    public PageTable() {
        this(4096);  // 4KB pages
    }
    
    /**
     * Create a page table with specified page size.
     * @param pageSize Size of each page in bytes (must be power of 2)
     */
    public PageTable(int pageSize) {
        if ((pageSize & (pageSize - 1)) != 0) {
            throw new IllegalArgumentException("Page size must be power of 2");
        }
        this.pageSize = pageSize;
        this.offsetBits = Integer.numberOfTrailingZeros(pageSize);
        this.entries = new HashMap<>();
        this.lookups = 0;
        this.hits = 0;
        this.pageFaults = 0;
    }
    
    /**
     * Extract the Virtual Page Number (VPN) from a virtual address.
     * 
     * The VPN is the upper bits of the address that index into the page table.
     * With 4KB pages (12 offset bits), a 32-bit address has a 20-bit VPN.
     * 
     * Example (4KB pages):
     *   Virtual Address: 0x12345678
     *   VPN:            0x12345    (upper 20 bits)
     *   Offset:         0x678      (lower 12 bits)
     * 
     * @param virtualAddress The full virtual address
     * @return The Virtual Page Number (index into page table)
     */
    public int getVPN(int virtualAddress) {
        return virtualAddress >>> offsetBits;
    }
    
    /**
     * Extract the page offset from a virtual address.
     * 
     * The offset is the lower bits that specify the byte within the page.
     * This offset is UNCHANGED during address translation - it's the same
     * in both the virtual and physical address.
     * 
     * @param virtualAddress The full virtual address
     * @return The offset within the page (0 to pageSize-1)
     */
    public int getOffset(int virtualAddress) {
        return virtualAddress & (pageSize - 1);
    }
    
    /**
     * Look up a virtual address in the page table.
     * 
     * This simulates what the MMU does in hardware. In a real system:
     * - First check TLB (Translation Lookaside Buffer) - not simulated here
     * - On TLB miss, walk the page table (what this method does)
     * - On page table miss, raise a page fault exception
     * 
     * @param virtualAddress The virtual address to look up
     * @return The PTE, or null if the page is not mapped
     */
    public PageTableEntry lookup(int virtualAddress) {
        lookups++;
        int vpn = getVPN(virtualAddress);
        PageTableEntry pte = entries.get(vpn);
        
        if (pte != null && pte.isValid() && pte.isPresent()) {
            hits++;
            pte.setAccessed(true);  // Hardware sets this bit on access
        }
        
        return pte;
    }
    
    /**
     * Translate a virtual address to a physical address.
     * 
     * This is the core operation of virtual memory! The translation:
     * 
     *   ┌──────────────────────────────────────────────────────────────┐
     *   │  Virtual Address                                            │
     *   │  ┌─────────────────────┬────────────────────┐               │
     *   │  │  VPN (page number)  │  Offset            │               │
     *   │  └─────────────────────┴────────────────────┘               │
     *   │           │                      │                          │
     *   │           ▼                      │                          │
     *   │    ┌─────────────┐               │                          │
     *   │    │ Page Table  │               │                          │
     *   │    │  (lookup)   │               │                          │
     *   │    └──────┬──────┘               │                          │
     *   │           │                      │                          │
     *   │           ▼                      ▼                          │
     *   │  ┌─────────────────────┬────────────────────┐               │
     *   │  │  PFN (frame num)    │  Offset (same!)    │               │
     *   │  └─────────────────────┴────────────────────┘               │
     *   │  Physical Address                                           │
     *   └──────────────────────────────────────────────────────────────┘
     * 
     * @param virtualAddress The virtual address to translate
     * @return Physical address, or -1 if translation fails (PAGE FAULT!)
     */
    public int translate(int virtualAddress) {
        PageTableEntry pte = lookup(virtualAddress);
        
        if (pte == null || !pte.isValid()) {
            pageFaults++;
            return -1;  // Page fault - page never mapped (likely a bug or attack)
        }
        
        if (!pte.isPresent()) {
            pageFaults++;
            return -1;  // Page fault - page swapped out (OS needs to load from disk)
        }
        
        // SUCCESS: Combine frame number with offset
        // The offset is unchanged - same position within the page/frame
        int offset = getOffset(virtualAddress);
        return (pte.getFrameNumber() << offsetBits) | offset;
    }
    
    /**
     * Map a virtual page to a physical frame.
     * @param virtualPage The virtual page number
     * @param physicalFrame The physical frame number
     * @param readable Can read from this page?
     * @param writable Can write to this page?
     * @param executable Can execute from this page?
     */
    public void mapPage(int virtualPage, int physicalFrame, 
                        boolean readable, boolean writable, boolean executable) {
        PageTableEntry pte = new PageTableEntry(physicalFrame, readable, writable, executable);
        entries.put(virtualPage, pte);
    }
    
    /**
     * Map a virtual page with default RW permissions.
     */
    public void mapPage(int virtualPage, int physicalFrame) {
        mapPage(virtualPage, physicalFrame, true, true, false);
    }
    
    /**
     * Unmap a virtual page.
     */
    public void unmapPage(int virtualPage) {
        PageTableEntry pte = entries.get(virtualPage);
        if (pte != null) {
            pte.setValid(false);
            pte.setPresent(false);
        }
    }
    
    /**
     * Mark a page as dirty (written to).
     */
    public void markDirty(int virtualAddress) {
        PageTableEntry pte = lookup(virtualAddress);
        if (pte != null) {
            pte.setDirty(true);
        }
    }
    
    /**
     * Check if a virtual address is mapped and valid.
     */
    public boolean isMapped(int virtualAddress) {
        PageTableEntry pte = entries.get(getVPN(virtualAddress));
        return pte != null && pte.isValid();
    }
    
    /**
     * Check read permission for an address.
     */
    public boolean canRead(int virtualAddress) {
        PageTableEntry pte = entries.get(getVPN(virtualAddress));
        return pte != null && pte.isValid() && pte.isReadable();
    }
    
    /**
     * Check write permission for an address.
     */
    public boolean canWrite(int virtualAddress) {
        PageTableEntry pte = entries.get(getVPN(virtualAddress));
        return pte != null && pte.isValid() && pte.isWritable();
    }
    
    /**
     * Check execute permission for an address.
     */
    public boolean canExecute(int virtualAddress) {
        PageTableEntry pte = entries.get(getVPN(virtualAddress));
        return pte != null && pte.isValid() && pte.isExecutable();
    }
    
    /**
     * Get all mapped page entries (for debugging/display).
     */
    public Map<Integer, PageTableEntry> getEntries() {
        return new HashMap<>(entries);
    }
    
    /**
     * Clear all accessed bits (for page replacement algorithms).
     */
    public void clearAccessedBits() {
        for (PageTableEntry pte : entries.values()) {
            pte.setAccessed(false);
        }
    }
    
    /**
     * Get the number of mapped pages.
     */
    public int getMappedPageCount() {
        int count = 0;
        for (PageTableEntry pte : entries.values()) {
            if (pte.isValid()) count++;
        }
        return count;
    }
    
    // Statistics getters
    public int getPageSize() { return pageSize; }
    public long getLookups() { return lookups; }
    public long getHits() { return hits; }
    public long getPageFaults() { return pageFaults; }
    
    public double getHitRate() {
        return lookups > 0 ? (double) hits / lookups : 0.0;
    }
    
    /**
     * Reset statistics.
     */
    public void resetStats() {
        lookups = 0;
        hits = 0;
        pageFaults = 0;
    }
    
    /**
     * Dump the page table for debugging.
     */
    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Page Table (page size: %d bytes, %d pages mapped):\n", 
                               pageSize, getMappedPageCount()));
        
        for (Map.Entry<Integer, PageTableEntry> entry : entries.entrySet()) {
            if (entry.getValue().isValid()) {
                sb.append(String.format("  VPN 0x%05X -> %s\n", 
                         entry.getKey(), entry.getValue()));
            }
        }
        
        sb.append(String.format("Stats: %d lookups, %d hits (%.1f%%), %d page faults\n",
                               lookups, hits, getHitRate() * 100, pageFaults));
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("PageTable{pages=%d, pageSize=%dB, faults=%d}", 
                            getMappedPageCount(), pageSize, pageFaults);
    }
}

