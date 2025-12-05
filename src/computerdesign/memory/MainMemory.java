package computerdesign.memory;

import java.util.Arrays;
import java.util.BitSet;

/**
 * MainMemory (RAM) - the largest but slowest memory in the hierarchy.
 * 
 * KEY INSIGHT: Physical memory is HARDWARE STATE, shared by all processes!
 * This is the actual RAM - the bytes physically stored in DRAM chips.
 * 
 * Physical memory is divided into FRAMES (same size as virtual pages).
 * The OS manages frame allocation; processes see virtual addresses.
 * 
 * Key properties:
 * - Large capacity (simulated here, typically GBs in real systems)
 * - Slow access time (~100 cycles compared to ~1 for registers)
 * - Byte-addressable, but often accessed in words (4 bytes)
 * - Volatile (loses contents when power is off)
 * - Divided into fixed-size FRAMES for virtual memory support
 * 
 * State: The memory array holds the program (instructions) and data.
 * This is where your program lives when it runs!
 */
public class MainMemory implements MemoryUnit {
    
    private final int[] memory;  // Word-addressable for simplicity
    private final int sizeInBytes;
    private final int accessTime;
    
    // Frame management (for virtual memory)
    private final int frameSize;        // Size of each frame in bytes (matches page size)
    private final int totalFrames;      // Total number of frames
    private final BitSet frameMap;      // Tracks allocated frames (true = allocated)
    private int freeFrames;             // Number of free frames
    
    // Reserved frames for kernel/system use
    private final int reservedFrames;
    
    // Memory regions (simplified model - these are PHYSICAL addresses)
    private int textSegmentStart = 0x00000000;      // Instructions
    private int dataSegmentStart = 0x10000000;      // Static data
    private int heapStart = 0x10010000;             // Dynamic data
    private int stackStart = 0x7FFFFFFC;            // Stack (grows down)
    
    /**
     * Create main memory with specified size and frame size.
     * @param sizeInBytes Size of memory in bytes (will be word-aligned)
     * @param frameSize Size of each frame in bytes (must match page size)
     */
    public MainMemory(int sizeInBytes, int frameSize) {
        this.sizeInBytes = sizeInBytes;
        this.memory = new int[sizeInBytes / 4];  // Word-addressable
        this.accessTime = 100;  // ~100 cycles for DRAM access
        
        // Initialize frame management
        this.frameSize = frameSize;
        this.totalFrames = sizeInBytes / frameSize;
        this.frameMap = new BitSet(totalFrames);
        this.freeFrames = totalFrames;
        
        // Reserve first few frames for kernel/system
        this.reservedFrames = 4;  // Reserve 16KB for kernel
        for (int i = 0; i < reservedFrames; i++) {
            frameMap.set(i);
            freeFrames--;
        }
    }
    
    /**
     * Create main memory with specified size and default 4KB frames.
     * @param sizeInBytes Size of memory in bytes (will be word-aligned)
     */
    public MainMemory(int sizeInBytes) {
        this(sizeInBytes, 4096);  // Default 4KB frames
    }
    
    /**
     * Create main memory with default size (64KB for simulation).
     */
    public MainMemory() {
        this(64 * 1024, 4096);  // 64KB default, 4KB frames
    }
    
    @Override
    public int read(int address) {
        int wordIndex = addressToIndex(address);
        return memory[wordIndex];
    }
    
    @Override
    public void write(int address, int value) {
        int wordIndex = addressToIndex(address);
        memory[wordIndex] = value;
    }
    
    /**
     * Read a byte from memory.
     */
    public byte readByte(int address) {
        int word = read(address & ~0x3);  // Word-align
        int byteOffset = address & 0x3;
        return (byte) ((word >> (byteOffset * 8)) & 0xFF);
    }
    
    /**
     * Write a byte to memory.
     */
    public void writeByte(int address, byte value) {
        int wordAddress = address & ~0x3;
        int byteOffset = address & 0x3;
        int word = read(wordAddress);
        int mask = ~(0xFF << (byteOffset * 8));
        word = (word & mask) | ((value & 0xFF) << (byteOffset * 8));
        write(wordAddress, word);
    }
    
    /**
     * Read a halfword (16 bits) from memory.
     */
    public short readHalfword(int address) {
        int word = read(address & ~0x3);
        int halfOffset = (address >> 1) & 0x1;
        return (short) ((word >> (halfOffset * 16)) & 0xFFFF);
    }
    
    /**
     * Load a program (array of instructions) into memory.
     * @param instructions Array of 32-bit instructions
     * @param startAddress Where to load the program
     */
    public void loadProgram(int[] instructions, int startAddress) {
        for (int i = 0; i < instructions.length; i++) {
            write(startAddress + (i * 4), instructions[i]);
        }
    }
    
    /**
     * Load data into memory.
     */
    public void loadData(int[] data, int startAddress) {
        for (int i = 0; i < data.length; i++) {
            write(startAddress + (i * 4), data[i]);
        }
    }
    
    private int addressToIndex(int address) {
        // Simple mapping: treat address as word index * 4
        // In a real system, this would involve virtual memory translation
        int index = (address & 0x0000FFFF) / 4;  // Mask to fit our memory size
        if (index < 0 || index >= memory.length) {
            throw new IllegalArgumentException(
                String.format("Memory access out of bounds: 0x%08X", address)
            );
        }
        return index;
    }
    
    @Override
    public int getSize() {
        return sizeInBytes;
    }
    
    @Override
    public int getAccessTime() {
        return accessTime;
    }
    
    @Override
    public void reset() {
        Arrays.fill(memory, 0);
    }
    
    @Override
    public String getName() {
        return "MainMemory";
    }
    
    /**
     * Dump a range of memory (for debugging).
     */
    public String dump(int startAddress, int words) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Memory dump from 0x%08X:\n", startAddress));
        for (int i = 0; i < words; i++) {
            int addr = startAddress + (i * 4);
            sb.append(String.format("  0x%08X: 0x%08X\n", addr, read(addr)));
        }
        return sb.toString();
    }
    
    // Getters for memory regions
    public int getTextSegmentStart() { return textSegmentStart; }
    public int getDataSegmentStart() { return dataSegmentStart; }
    public int getHeapStart() { return heapStart; }
    public int getStackStart() { return stackStart; }
    
    // ==================== FRAME MANAGEMENT ====================
    // These methods are used by the OS for virtual memory support
    
    /**
     * Allocate a physical frame.
     * @return Frame number, or -1 if no frames available
     */
    public int allocateFrame() {
        if (freeFrames == 0) {
            return -1;  // Out of memory
        }
        
        // Find first free frame (skip reserved frames)
        int frame = frameMap.nextClearBit(reservedFrames);
        if (frame >= totalFrames) {
            return -1;  // No frames available
        }
        
        frameMap.set(frame);
        freeFrames--;
        return frame;
    }
    
    /**
     * Allocate multiple contiguous frames.
     * @param count Number of frames to allocate
     * @return Starting frame number, or -1 if not enough frames
     */
    public int allocateFrames(int count) {
        if (freeFrames < count) {
            return -1;
        }
        
        // Find contiguous free frames
        int start = reservedFrames;
        while (start + count <= totalFrames) {
            boolean found = true;
            for (int i = 0; i < count; i++) {
                if (frameMap.get(start + i)) {
                    found = false;
                    start = start + i + 1;
                    break;
                }
            }
            if (found) {
                // Allocate the frames
                for (int i = 0; i < count; i++) {
                    frameMap.set(start + i);
                }
                freeFrames -= count;
                return start;
            }
        }
        return -1;  // Not enough contiguous frames
    }
    
    /**
     * Free a physical frame.
     * @param frameNumber The frame to free
     */
    public void freeFrame(int frameNumber) {
        if (frameNumber >= reservedFrames && frameNumber < totalFrames) {
            if (frameMap.get(frameNumber)) {
                frameMap.clear(frameNumber);
                freeFrames++;
                
                // Zero out the frame for security
                int startWord = (frameNumber * frameSize) / 4;
                int endWord = startWord + (frameSize / 4);
                for (int i = startWord; i < endWord && i < memory.length; i++) {
                    memory[i] = 0;
                }
            }
        }
    }
    
    /**
     * Free multiple frames.
     * @param startFrame Starting frame number
     * @param count Number of frames to free
     */
    public void freeFrames(int startFrame, int count) {
        for (int i = 0; i < count; i++) {
            freeFrame(startFrame + i);
        }
    }
    
    /**
     * Get the physical address for a frame.
     * @param frameNumber The frame number
     * @return Physical address of the start of the frame
     */
    public int getFrameAddress(int frameNumber) {
        return frameNumber * frameSize;
    }
    
    /**
     * Get the frame number for a physical address.
     * @param physicalAddress The physical address
     * @return Frame number
     */
    public int getFrameNumber(int physicalAddress) {
        return physicalAddress / frameSize;
    }
    
    /**
     * Check if a frame is allocated.
     */
    public boolean isFrameAllocated(int frameNumber) {
        return frameNumber < totalFrames && frameMap.get(frameNumber);
    }
    
    /**
     * Read directly from a physical address (bypasses any translation).
     * This is what the MMU uses after translation.
     */
    public int readPhysical(int physicalAddress) {
        return read(physicalAddress);
    }
    
    /**
     * Write directly to a physical address (bypasses any translation).
     * This is what the MMU uses after translation.
     */
    public void writePhysical(int physicalAddress, int value) {
        write(physicalAddress, value);
    }
    
    // Frame management getters
    public int getFrameSize() { return frameSize; }
    public int getTotalFrames() { return totalFrames; }
    public int getFreeFrames() { return freeFrames; }
    public int getAllocatedFrames() { return totalFrames - freeFrames; }
    public int getReservedFrames() { return reservedFrames; }
    
    /**
     * Get frame allocation statistics.
     */
    public String getFrameStats() {
        return String.format(
            "Physical Memory Frames:\n" +
            "  Frame Size: %d bytes\n" +
            "  Total Frames: %d\n" +
            "  Reserved: %d\n" +
            "  Allocated: %d\n" +
            "  Free: %d\n" +
            "  Utilization: %.1f%%",
            frameSize, totalFrames, reservedFrames,
            getAllocatedFrames(), freeFrames,
            (double) getAllocatedFrames() / totalFrames * 100
        );
    }
}

