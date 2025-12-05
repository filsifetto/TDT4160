package computerdesign.memory;

/**
 * Register - the fastest and smallest memory unit.
 * 
 * Key properties:
 * - Single-cycle access (built into the processor)
 * - Holds exactly one word (32 bits in RISC-V)
 * - Named/numbered (x0-x31 in RISC-V)
 * - Some have special purposes (x0 = zero, x1 = ra, x2 = sp, etc.)
 * 
 * State: A register holds a single 32-bit value that persists between cycles.
 */
public class Register implements MemoryUnit {
    private final String name;
    private final int number;
    private int value;
    private final boolean isReadOnly;  // For x0 (hardwired zero)
    
    /**
     * Create a new register.
     * @param name The register name (e.g., "x0", "ra", "sp")
     * @param number The register number (0-31)
     * @param isReadOnly If true, writes are ignored (for x0)
     */
    public Register(String name, int number, boolean isReadOnly) {
        this.name = name;
        this.number = number;
        this.isReadOnly = isReadOnly;
        this.value = 0;
    }
    
    public Register(String name, int number) {
        this(name, number, false);
    }
    
    @Override
    public int read(int address) {
        // Address is ignored for single register; always return value
        return value;
    }
    
    @Override
    public void write(int address, int value) {
        if (!isReadOnly) {
            this.value = value;
        }
        // Writes to x0 are silently ignored
    }
    
    /**
     * Direct read (no address needed for single register).
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Direct write (no address needed for single register).
     */
    public void setValue(int value) {
        if (!isReadOnly) {
            this.value = value;
        }
    }
    
    @Override
    public int getSize() {
        return 4; // 4 bytes = 32 bits
    }
    
    @Override
    public int getAccessTime() {
        return 0; // Registers are accessed within the same cycle
    }
    
    @Override
    public void reset() {
        value = 0;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public int getNumber() {
        return number;
    }
    
    public boolean isReadOnly() {
        return isReadOnly;
    }
    
    @Override
    public String toString() {
        return String.format("%s (x%d) = 0x%08X (%d)", name, number, value, value);
    }
}

