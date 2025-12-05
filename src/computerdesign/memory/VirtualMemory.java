package computerdesign.memory;

/**
 * VirtualMemory - per-process virtual address space manager.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE BIG PICTURE: VIRTUAL MEMORY IS PROCESS STATE!
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Understanding the ownership of different memory concepts is CRUCIAL:
 * 
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                     HARDWARE STATE (shared)                            │
 *   │  ┌──────────────────────────────────────────────────────────────────┐  │
 *   │  │  Physical Memory (MainMemory)                                    │  │
 *   │  │  • The actual RAM chips                                          │  │
 *   │  │  • One instance for the whole system                             │  │
 *   │  │  • Divided into fixed-size FRAMES                                │  │
 *   │  │  • Managed by the OS (frame allocation)                          │  │
 *   │  └──────────────────────────────────────────────────────────────────┘  │
 *   └─────────────────────────────────────────────────────────────────────────┘
 *   
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                     PROCESS STATE (per-process)                        │
 *   │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
 *   │  │  Process A      │  │  Process B      │  │  Process C      │        │
 *   │  │  ┌───────────┐  │  │  ┌───────────┐  │  │  ┌───────────┐  │        │
 *   │  │  │Page Table │  │  │  │Page Table │  │  │  │Page Table │  │        │
 *   │  │  │ (VPN→PFN) │  │  │  │ (VPN→PFN) │  │  │  │ (VPN→PFN) │  │        │
 *   │  │  └───────────┘  │  │  └───────────┘  │  │  └───────────┘  │        │
 *   │  │  VirtualMemory  │  │  VirtualMemory  │  │  VirtualMemory  │        │
 *   │  └─────────────────┘  └─────────────────┘  └─────────────────┘        │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * Each process has its OWN page table, so:
 *   - Process A's address 0x1000 → Physical frame 5
 *   - Process B's address 0x1000 → Physical frame 9 (different!)
 *   - This is how ISOLATION works!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE FOUR POWERS OF VIRTUAL MEMORY
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 1. ISOLATION: Each process has its own address space
 *    - Process A cannot access Process B's memory
 *    - Buggy/malicious programs can't corrupt other processes
 * 
 * 2. PROTECTION: Page permissions prevent unauthorized access
 *    - Code pages: read + execute (no write → prevents code injection)
 *    - Data pages: read + write (no execute → prevents data execution)
 *    - Read-only pages: shared libraries, constants
 * 
 * 3. FLEXIBILITY: Virtual addresses can map anywhere in physical memory
 *    - Physical memory can be fragmented; virtual is contiguous
 *    - Easy to grow heap/stack - just map more pages
 *    - Can share physical frames between processes (shared memory, libraries)
 * 
 * 4. EFFICIENCY: Only allocate physical memory when needed (demand paging)
 *    - Process can have large virtual address space
 *    - Only pages that are actually USED consume physical memory
 *    - Unused pages can be swapped to disk
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * ADDRESS TRANSLATION FLOW
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   Virtual Address (from CPU)
 *         │
 *         ▼
 *   ┌─────────────────┐
 *   │    MMU/TLB      │  ← Hardware (fast path)
 *   │  Translation    │
 *   └────────┬────────┘
 *            │ TLB miss?
 *            ▼
 *   ┌─────────────────┐
 *   │   Page Table    │  ← PROCESS STATE (this class!)
 *   │    Walk         │
 *   └────────┬────────┘
 *            │ Page fault?
 *            ▼
 *   ┌─────────────────┐
 *   │   Page Fault    │  ← OS handles this
 *   │    Handler      │
 *   └────────┬────────┘
 *            │
 *            ▼
 *   Physical Address
 *         │
 *         ▼
 *   ┌─────────────────┐
 *   │ Physical Memory │  ← HARDWARE STATE (MainMemory)
 *   └─────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @see PageTable - The data structure that stores the mappings
 * @see MainMemory - The physical memory (hardware state)
 * @see Process - Each process owns a VirtualMemory instance
 */
public class VirtualMemory {
    
    /**
     * Exception thrown when a page fault occurs.
     */
    public static class PageFaultException extends RuntimeException {
        private final int virtualAddress;
        private final boolean isWrite;
        private final String reason;
        
        public PageFaultException(int virtualAddress, boolean isWrite, String reason) {
            super(String.format("Page fault at 0x%08X (%s): %s", 
                               virtualAddress, isWrite ? "write" : "read", reason));
            this.virtualAddress = virtualAddress;
            this.isWrite = isWrite;
            this.reason = reason;
        }
        
        public int getVirtualAddress() { return virtualAddress; }
        public boolean isWrite() { return isWrite; }
        public String getReason() { return reason; }
    }
    
    /**
     * Exception thrown on protection violation.
     */
    public static class ProtectionFaultException extends RuntimeException {
        private final int virtualAddress;
        private final String violation;
        
        public ProtectionFaultException(int virtualAddress, String violation) {
            super(String.format("Protection fault at 0x%08X: %s", virtualAddress, violation));
            this.virtualAddress = virtualAddress;
            this.violation = violation;
        }
        
        public int getVirtualAddress() { return virtualAddress; }
        public String getViolation() { return violation; }
    }
    
    // The page table - THIS IS THE PROCESS STATE
    private final PageTable pageTable;
    
    // Reference to physical memory - SHARED HARDWARE STATE
    private final MainMemory physicalMemory;
    
    // Virtual address space layout (process-specific)
    private int textStart;      // Code segment
    private int textEnd;
    private int dataStart;      // Initialized data
    private int dataEnd;
    private int bssStart;       // Uninitialized data
    private int bssEnd;
    private int heapStart;      // Dynamic allocation (grows up)
    private int heapEnd;
    private int stackStart;     // Stack (grows down)
    private int stackEnd;
    
    // Statistics
    private long reads;
    private long writes;
    private long pageFaults;
    
    /**
     * Create virtual memory for a process.
     * @param physicalMemory The shared physical memory (hardware)
     */
    public VirtualMemory(MainMemory physicalMemory) {
        this.physicalMemory = physicalMemory;
        this.pageTable = new PageTable(4096);  // 4KB pages
        
        // Default virtual address space layout (similar to Linux)
        this.textStart = 0x00400000;      // Code starts at 4MB
        this.dataStart = 0x10000000;      // Data at 256MB
        this.heapStart = 0x10800000;      // Heap after data
        this.heapEnd = heapStart;
        this.stackEnd = 0x7FFFFFFF;       // Stack at top of user space
        this.stackStart = stackEnd;       // Stack grows down
        
        this.reads = 0;
        this.writes = 0;
        this.pageFaults = 0;
    }
    
    /**
     * Read a word from a virtual address.
     * Performs address translation and permission checking.
     */
    public int read(int virtualAddress) {
        reads++;
        
        // Check permissions
        if (!pageTable.canRead(virtualAddress)) {
            if (!pageTable.isMapped(virtualAddress)) {
                pageFaults++;
                throw new PageFaultException(virtualAddress, false, "page not mapped");
            }
            throw new ProtectionFaultException(virtualAddress, "read not permitted");
        }
        
        // Translate virtual to physical
        int physicalAddress = pageTable.translate(virtualAddress);
        if (physicalAddress < 0) {
            pageFaults++;
            throw new PageFaultException(virtualAddress, false, "translation failed");
        }
        
        // Access physical memory
        return physicalMemory.read(physicalAddress);
    }
    
    /**
     * Write a word to a virtual address.
     * Performs address translation and permission checking.
     */
    public void write(int virtualAddress, int value) {
        writes++;
        
        // Check permissions
        if (!pageTable.canWrite(virtualAddress)) {
            if (!pageTable.isMapped(virtualAddress)) {
                pageFaults++;
                throw new PageFaultException(virtualAddress, true, "page not mapped");
            }
            throw new ProtectionFaultException(virtualAddress, "write not permitted");
        }
        
        // Translate virtual to physical
        int physicalAddress = pageTable.translate(virtualAddress);
        if (physicalAddress < 0) {
            pageFaults++;
            throw new PageFaultException(virtualAddress, true, "translation failed");
        }
        
        // Mark page as dirty
        pageTable.markDirty(virtualAddress);
        
        // Access physical memory
        physicalMemory.write(physicalAddress, value);
    }
    
    /**
     * Map a range of virtual pages to physical frames.
     * @param virtualStart Starting virtual address (page-aligned)
     * @param physicalStart Starting physical address (frame-aligned)
     * @param size Size in bytes to map
     * @param readable Allow reads
     * @param writable Allow writes
     * @param executable Allow execution
     */
    public void mapRange(int virtualStart, int physicalStart, int size,
                         boolean readable, boolean writable, boolean executable) {
        int pageSize = pageTable.getPageSize();
        int numPages = (size + pageSize - 1) / pageSize;
        
        for (int i = 0; i < numPages; i++) {
            int vpn = pageTable.getVPN(virtualStart + i * pageSize);
            int pfn = physicalStart / pageSize + i;
            pageTable.mapPage(vpn, pfn, readable, writable, executable);
        }
    }
    
    /**
     * Map a code segment (readable, executable).
     */
    public void mapCode(int virtualStart, int physicalStart, int size) {
        mapRange(virtualStart, physicalStart, size, true, false, true);
        this.textStart = virtualStart;
        this.textEnd = virtualStart + size;
    }
    
    /**
     * Map a data segment (readable, writable).
     */
    public void mapData(int virtualStart, int physicalStart, int size) {
        mapRange(virtualStart, physicalStart, size, true, true, false);
        this.dataStart = virtualStart;
        this.dataEnd = virtualStart + size;
    }
    
    /**
     * Map a heap page (readable, writable).
     */
    public void mapHeapPage(int virtualPage, int physicalFrame) {
        pageTable.mapPage(virtualPage, physicalFrame, true, true, false);
    }
    
    /**
     * Map a stack page (readable, writable).
     */
    public void mapStackPage(int virtualPage, int physicalFrame) {
        pageTable.mapPage(virtualPage, physicalFrame, true, true, false);
    }
    
    /**
     * Extend the heap by one page.
     * @param physicalFrame The physical frame to use
     * @return The new heap break address
     */
    public int extendHeap(int physicalFrame) {
        int pageSize = pageTable.getPageSize();
        int newPage = pageTable.getVPN(heapEnd);
        pageTable.mapPage(newPage, physicalFrame, true, true, false);
        heapEnd += pageSize;
        return heapEnd;
    }
    
    /**
     * Extend the stack by one page (grows down).
     * @param physicalFrame The physical frame to use
     * @return The new stack limit address
     */
    public int extendStack(int physicalFrame) {
        int pageSize = pageTable.getPageSize();
        stackStart -= pageSize;
        int newPage = pageTable.getVPN(stackStart);
        pageTable.mapPage(newPage, physicalFrame, true, true, false);
        return stackStart;
    }
    
    /**
     * Check if a virtual address is in the valid address space.
     */
    public boolean isValidAddress(int virtualAddress) {
        return pageTable.isMapped(virtualAddress);
    }
    
    /**
     * Handle a page fault (called by OS).
     * This is a simplified handler - real OS would check the fault type,
     * potentially load from swap, allocate a frame, etc.
     * 
     * @param virtualAddress The faulting address
     * @param physicalFrame The physical frame to map (allocated by OS)
     */
    public void handlePageFault(int virtualAddress, int physicalFrame) {
        int vpn = pageTable.getVPN(virtualAddress);
        
        // Determine permissions based on address range
        boolean readable = true;
        boolean writable = true;
        boolean executable = false;
        
        if (virtualAddress >= textStart && virtualAddress < textEnd) {
            // Code segment
            writable = false;
            executable = true;
        }
        
        pageTable.mapPage(vpn, physicalFrame, readable, writable, executable);
    }
    
    /**
     * Unmap all pages (called when process terminates).
     */
    public void unmapAll() {
        for (int vpn : pageTable.getEntries().keySet()) {
            pageTable.unmapPage(vpn);
        }
    }
    
    /**
     * Copy page mappings from another virtual memory (for fork).
     * In a real system, this would use copy-on-write.
     */
    public void copyMappingsFrom(VirtualMemory other) {
        for (var entry : other.pageTable.getEntries().entrySet()) {
            var pte = entry.getValue();
            if (pte.isValid()) {
                pageTable.mapPage(entry.getKey(), pte.getFrameNumber(),
                                 pte.isReadable(), pte.isWritable(), pte.isExecutable());
            }
        }
        
        // Copy layout
        this.textStart = other.textStart;
        this.textEnd = other.textEnd;
        this.dataStart = other.dataStart;
        this.dataEnd = other.dataEnd;
        this.heapStart = other.heapStart;
        this.heapEnd = other.heapEnd;
        this.stackStart = other.stackStart;
        this.stackEnd = other.stackEnd;
    }
    
    // Getters
    public PageTable getPageTable() { return pageTable; }
    public MainMemory getPhysicalMemory() { return physicalMemory; }
    
    public int getTextStart() { return textStart; }
    public int getTextEnd() { return textEnd; }
    public int getDataStart() { return dataStart; }
    public int getDataEnd() { return dataEnd; }
    public int getHeapStart() { return heapStart; }
    public int getHeapEnd() { return heapEnd; }
    public int getStackStart() { return stackStart; }
    public int getStackEnd() { return stackEnd; }
    
    public long getReads() { return reads; }
    public long getWrites() { return writes; }
    public long getPageFaults() { return pageFaults; }
    
    /**
     * Get statistics as a formatted string.
     */
    public String getStats() {
        return String.format(
            "VirtualMemory Stats:\n" +
            "  Reads: %d, Writes: %d\n" +
            "  Page Faults: %d\n" +
            "  Mapped Pages: %d\n" +
            "  Page Table Hit Rate: %.1f%%",
            reads, writes, pageFaults,
            pageTable.getMappedPageCount(),
            pageTable.getHitRate() * 100
        );
    }
    
    /**
     * Dump the virtual memory layout.
     */
    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("Virtual Memory Layout:\n");
        sb.append(String.format("  Text:  0x%08X - 0x%08X\n", textStart, textEnd));
        sb.append(String.format("  Data:  0x%08X - 0x%08X\n", dataStart, dataEnd));
        sb.append(String.format("  Heap:  0x%08X - 0x%08X (grows up)\n", heapStart, heapEnd));
        sb.append(String.format("  Stack: 0x%08X - 0x%08X (grows down)\n", stackStart, stackEnd));
        sb.append("\n");
        sb.append(pageTable.dump());
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("VirtualMemory{pages=%d, faults=%d}", 
                            pageTable.getMappedPageCount(), pageFaults);
    }
}

