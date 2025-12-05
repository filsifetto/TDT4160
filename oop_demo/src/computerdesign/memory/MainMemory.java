package computerdesign.memory;

import java.util.Arrays;

/**
 * MainMemory (RAM) - the largest but slowest memory in the hierarchy.
 * 
 * Key properties:
 * - Large capacity (simulated here, typically GBs in real systems)
 * - Slow access time (~100 cycles compared to ~1 for registers)
 * - Byte-addressable, but often accessed in words (4 bytes)
 * - Volatile (loses contents when power is off)
 * 
 * State: The memory array holds the program (instructions) and data.
 * This is where your program lives when it runs!
 */
public class MainMemory implements MemoryUnit {
    
    private final int[] memory;  // Word-addressable for simplicity
    private final int sizeInBytes;
    private final int accessTime;
    
    // Memory regions (simplified model)
    private int textSegmentStart = 0x00000000;      // Instructions
    private int dataSegmentStart = 0x10000000;      // Static data
    private int heapStart = 0x10010000;             // Dynamic data
    private int stackStart = 0x7FFFFFFC;            // Stack (grows down)
    
    /**
     * Create main memory with specified size.
     * @param sizeInBytes Size of memory in bytes (will be word-aligned)
     */
    public MainMemory(int sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
        this.memory = new int[sizeInBytes / 4];  // Word-addressable
        this.accessTime = 100;  // ~100 cycles for DRAM access
    }
    
    /**
     * Create main memory with default size (64KB for simulation).
     */
    public MainMemory() {
        this(64 * 1024);  // 64KB default
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
}

