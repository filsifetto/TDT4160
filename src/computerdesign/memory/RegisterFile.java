package computerdesign.memory;

/**
 * RegisterFile - collection of all processor registers.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * REGISTERS: THE FASTEST STORAGE IN THE SYSTEM
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Registers are the ONLY storage the ALU can directly access.
 * All data must flow through registers to be operated on!
 * 
 *   Memory (slow) → Register → ALU → Register → Memory (slow)
 *                     ↑                 │
 *                     │     (fast!)     │
 *                     └─────────────────┘
 * 
 * Access time: ~0.25 ns (essentially "free" - within the clock cycle)
 * Compare to: L1 cache ~1 ns, DRAM ~100 ns
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * RISC-V REGISTER CONVENTIONS (ABI)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * ┌────────┬──────┬─────────────────────────────────────────────────────────────┐
 * │Register│ ABI  │ Description                                                │
 * │ Number │ Name │                                                            │
 * ├────────┼──────┼─────────────────────────────────────────────────────────────┤
 * │   x0   │ zero │ HARDWIRED TO ZERO - writes are ignored!                    │
 * │   x1   │  ra  │ Return address (saved by caller)                           │
 * │   x2   │  sp  │ Stack pointer (callee-saved)                               │
 * │   x3   │  gp  │ Global pointer (points to global data)                     │
 * │   x4   │  tp  │ Thread pointer (for thread-local storage)                  │
 * │  x5-7  │ t0-2 │ Temporaries (caller-saved - may be clobbered)              │
 * │   x8   │s0/fp │ Saved register / Frame pointer                             │
 * │   x9   │  s1  │ Saved register (callee must preserve)                      │
 * │ x10-11 │ a0-1 │ Function arguments / return values                         │
 * │ x12-17 │ a2-7 │ Function arguments                                         │
 * │ x18-27 │s2-11 │ Saved registers (callee must preserve)                     │
 * │ x28-31 │ t3-6 │ Temporaries (caller-saved)                                 │
 * └────────┴──────┴─────────────────────────────────────────────────────────────┘
 * 
 * CALLER-SAVED (t0-t6, a0-a7): Caller must save if it needs them after a call
 * CALLEE-SAVED (s0-s11, sp): Callee must restore before returning
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WHY x0 IS HARDWIRED TO ZERO
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Having a register always contain 0 is extremely useful:
 * 
 *   - Move:  mv  rd, rs    →  addi rd, rs, 0
 *   - Negate: neg rd, rs   →  sub  rd, x0, rs
 *   - Not:   not rd, rs    →  xori rd, rs, -1
 *   - NOP:   nop           →  addi x0, x0, 0
 *   - Jump:  j   offset    →  jal  x0, offset  (discard return address)
 * 
 * This lets RISC-V have fewer instruction types - more instructions
 * become special cases of others!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * REGISTER FILE HARDWARE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * The register file has:
 *   - TWO read ports (can read rs1 and rs2 simultaneously)
 *   - ONE write port (writes to rd at end of cycle)
 * 
 *        ┌──────────────────────────┐
 *   rs1 ─┤  Read Port 1  ───────────├──► rs1 value
 *        │                          │
 *   rs2 ─┤  Read Port 2  ───────────├──► rs2 value
 *        │                          │
 *    rd ─┤  Write Port  ◄───────────┤◄── write data
 *        │                          │
 *   WE ──┤  Write Enable            │
 *        └──────────────────────────┘
 * 
 * Reads are combinational (instant), writes happen at clock edge.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @see Register - Individual register implementation
 * @see Processor - Uses the register file for instruction execution
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

