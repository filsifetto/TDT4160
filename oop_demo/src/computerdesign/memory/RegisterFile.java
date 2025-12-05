package computerdesign.memory;

/**
 * RegisterFile - collection of all processor registers.
 * 
 * RISC-V has 32 general-purpose registers (x0-x31):
 * - x0 (zero): Hardwired to 0, writes ignored
 * - x1 (ra): Return address
 * - x2 (sp): Stack pointer
 * - x3 (gp): Global pointer
 * - x4 (tp): Thread pointer
 * - x5-x7 (t0-t2): Temporaries
 * - x8 (s0/fp): Saved register / frame pointer
 * - x9 (s1): Saved register
 * - x10-x17 (a0-a7): Function arguments / return values
 * - x18-x27 (s2-s11): Saved registers
 * - x28-x31 (t3-t6): Temporaries
 * 
 * Key insight: The register file has TWO read ports and ONE write port,
 * allowing two operands to be read simultaneously in a single cycle.
 */
public class RegisterFile implements MemoryUnit {
    
    private static final int NUM_REGISTERS = 32;
    private final Register[] registers;
    
    // RISC-V ABI register names
    private static final String[] REGISTER_NAMES = {
        "zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
        "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
        "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
        "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
    };
    
    public RegisterFile() {
        registers = new Register[NUM_REGISTERS];
        
        // Initialize all registers
        for (int i = 0; i < NUM_REGISTERS; i++) {
            // x0 is hardwired to zero (read-only)
            boolean isReadOnly = (i == 0);
            registers[i] = new Register(REGISTER_NAMES[i], i, isReadOnly);
        }
    }
    
    /**
     * Read from a register by number.
     * Simulates one read port of the register file.
     */
    @Override
    public int read(int regNum) {
        validateRegister(regNum);
        return registers[regNum].getValue();
    }
    
    /**
     * Write to a register by number.
     * Note: Writing to x0 has no effect.
     */
    @Override
    public void write(int regNum, int value) {
        validateRegister(regNum);
        registers[regNum].setValue(value);
    }
    
    /**
     * Read two registers simultaneously (models two read ports).
     * This is what happens in the decode stage.
     */
    public int[] readTwo(int rs1, int rs2) {
        return new int[] { read(rs1), read(rs2) };
    }
    
    /**
     * Get a register by number.
     */
    public Register getRegister(int regNum) {
        validateRegister(regNum);
        return registers[regNum];
    }
    
    /**
     * Get a register by ABI name.
     */
    public Register getRegister(String name) {
        for (Register reg : registers) {
            if (reg.getName().equals(name)) {
                return reg;
            }
        }
        throw new IllegalArgumentException("Unknown register: " + name);
    }
    
    private void validateRegister(int regNum) {
        if (regNum < 0 || regNum >= NUM_REGISTERS) {
            throw new IllegalArgumentException(
                "Invalid register number: " + regNum + ". Must be 0-31."
            );
        }
    }
    
    @Override
    public int getSize() {
        return NUM_REGISTERS * 4; // 32 registers * 4 bytes each
    }
    
    @Override
    public int getAccessTime() {
        return 0; // Register access is part of the cycle
    }
    
    @Override
    public void reset() {
        for (Register reg : registers) {
            reg.reset();
        }
    }
    
    @Override
    public String getName() {
        return "RegisterFile";
    }
    
    /**
     * Dump all register values (for debugging).
     */
    public String dump() {
        StringBuilder sb = new StringBuilder("Register File:\n");
        for (int i = 0; i < NUM_REGISTERS; i += 4) {
            for (int j = 0; j < 4 && i + j < NUM_REGISTERS; j++) {
                Register r = registers[i + j];
                sb.append(String.format("  x%-2d (%-4s) = %08X  ", 
                    r.getNumber(), r.getName(), r.getValue()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return dump();
    }
}

