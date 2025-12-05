package computerdesign.instruction;

/**
 * Instruction - represents a single RISC-V instruction.
 * 
 * RISC-V has 6 instruction formats, all 32 bits:
 * - R-type: Register-register operations (add, sub, and, or, etc.)
 * - I-type: Immediate operations (addi, loads, jalr)
 * - S-type: Store operations
 * - B-type: Branch operations
 * - U-type: Upper immediate (lui, auipc)
 * - J-type: Jump (jal)
 * 
 * Key insight: The instruction is just data (32 bits). It becomes meaningful
 * only when the control unit decodes it and the datapath executes it.
 */
public class Instruction {
    
    private final int raw;  // The raw 32-bit instruction
    
    // Decoded fields (cached for efficiency)
    private final int opcode;
    private final int rd;
    private final int funct3;
    private final int rs1;
    private final int rs2;
    private final int funct7;
    private final InstructionType type;
    
    /**
     * Instruction types based on format.
     */
    public enum InstructionType {
        R_TYPE,  // Register-register
        I_TYPE,  // Immediate
        S_TYPE,  // Store
        B_TYPE,  // Branch
        U_TYPE,  // Upper immediate
        J_TYPE   // Jump
    }
    
    /**
     * Create an instruction from a 32-bit value.
     */
    public Instruction(int raw) {
        this.raw = raw;
        
        // Decode common fields
        this.opcode = raw & 0x7F;
        this.rd = (raw >> 7) & 0x1F;
        this.funct3 = (raw >> 12) & 0x07;
        this.rs1 = (raw >> 15) & 0x1F;
        this.rs2 = (raw >> 20) & 0x1F;
        this.funct7 = (raw >> 25) & 0x7F;
        
        // Determine instruction type from opcode
        this.type = determineType(opcode);
    }
    
    /**
     * Determine instruction type from opcode.
     */
    private InstructionType determineType(int opcode) {
        switch (opcode) {
            case 0b0110011:  // R-type (ADD, SUB, etc.)
                return InstructionType.R_TYPE;
                
            case 0b0010011:  // I-type arithmetic (ADDI, etc.)
            case 0b0000011:  // I-type loads (LW, etc.)
            case 0b1100111:  // JALR
            case 0b1110011:  // System
                return InstructionType.I_TYPE;
                
            case 0b0100011:  // S-type (SW, etc.)
                return InstructionType.S_TYPE;
                
            case 0b1100011:  // B-type (BEQ, etc.)
                return InstructionType.B_TYPE;
                
            case 0b0110111:  // LUI
            case 0b0010111:  // AUIPC
                return InstructionType.U_TYPE;
                
            case 0b1101111:  // JAL
                return InstructionType.J_TYPE;
                
            default:
                return InstructionType.R_TYPE;  // Default
        }
    }
    
    /**
     * Get the immediate value, sign-extended based on instruction type.
     * 
     * Each instruction type encodes the immediate differently:
     * - I-type: imm[11:0]
     * - S-type: imm[11:5] | imm[4:0]
     * - B-type: imm[12|10:5] | imm[4:1|11]
     * - U-type: imm[31:12]
     * - J-type: imm[20|10:1|11|19:12]
     */
    public int getImmediate() {
        switch (type) {
            case I_TYPE:
                // Sign-extend 12-bit immediate
                int immI = raw >> 20;
                return immI;  // Java >> is arithmetic (sign-extends)
                
            case S_TYPE:
                // Combine imm[11:5] and imm[4:0]
                int immS = ((raw >> 25) << 5) | ((raw >> 7) & 0x1F);
                // Sign-extend from 12 bits
                return (immS << 20) >> 20;
                
            case B_TYPE:
                // B-type immediate: imm[12|10:5|4:1|11]
                int immB = ((raw >> 31) << 12) |           // imm[12]
                          (((raw >> 7) & 0x1) << 11) |     // imm[11]
                          (((raw >> 25) & 0x3F) << 5) |    // imm[10:5]
                          (((raw >> 8) & 0xF) << 1);       // imm[4:1]
                return (immB << 19) >> 19;  // Sign-extend from 13 bits
                
            case U_TYPE:
                // Upper 20 bits, already in position
                return raw & 0xFFFFF000;
                
            case J_TYPE:
                // J-type immediate: imm[20|10:1|11|19:12]
                int immJ = ((raw >> 31) << 20) |           // imm[20]
                          (((raw >> 12) & 0xFF) << 12) |   // imm[19:12]
                          (((raw >> 20) & 0x1) << 11) |    // imm[11]
                          (((raw >> 21) & 0x3FF) << 1);    // imm[10:1]
                return (immJ << 11) >> 11;  // Sign-extend from 21 bits
                
            default:
                return 0;
        }
    }
    
    // Getters for decoded fields
    public int getRaw() { return raw; }
    public int getOpcode() { return opcode; }
    public int getRd() { return rd; }
    public int getFunct3() { return funct3; }
    public int getRs1() { return rs1; }
    public int getRs2() { return rs2; }
    public int getFunct7() { return funct7; }
    public InstructionType getType() { return type; }
    
    /**
     * Check if this is a NOP (ADDI x0, x0, 0).
     */
    public boolean isNop() {
        return raw == 0x00000013;
    }
    
    /**
     * Check if this is a halt instruction (EBREAK).
     */
    public boolean isHalt() {
        return raw == 0x00100073;
    }
    
    /**
     * Get a human-readable disassembly of this instruction.
     */
    public String disassemble() {
        return InstructionDecoder.disassemble(this);
    }
    
    @Override
    public String toString() {
        return String.format("0x%08X: %s", raw, disassemble());
    }
    
    // =========== Static factory methods for creating instructions ===========
    
    /**
     * Create an R-type instruction.
     */
    public static Instruction rType(int opcode, int rd, int funct3, int rs1, int rs2, int funct7) {
        int raw = (funct7 << 25) | (rs2 << 20) | (rs1 << 15) | 
                  (funct3 << 12) | (rd << 7) | opcode;
        return new Instruction(raw);
    }
    
    /**
     * Create an I-type instruction.
     */
    public static Instruction iType(int opcode, int rd, int funct3, int rs1, int imm) {
        int raw = ((imm & 0xFFF) << 20) | (rs1 << 15) | 
                  (funct3 << 12) | (rd << 7) | opcode;
        return new Instruction(raw);
    }
    
    /**
     * Create a NOP instruction.
     */
    public static Instruction nop() {
        return new Instruction(0x00000013);  // ADDI x0, x0, 0
    }
    
    /**
     * Create common instructions.
     */
    public static Instruction add(int rd, int rs1, int rs2) {
        return rType(0b0110011, rd, 0b000, rs1, rs2, 0b0000000);
    }
    
    public static Instruction sub(int rd, int rs1, int rs2) {
        return rType(0b0110011, rd, 0b000, rs1, rs2, 0b0100000);
    }
    
    public static Instruction addi(int rd, int rs1, int imm) {
        return iType(0b0010011, rd, 0b000, rs1, imm);
    }
    
    public static Instruction lw(int rd, int rs1, int offset) {
        return iType(0b0000011, rd, 0b010, rs1, offset);
    }
    
    public static Instruction sw(int rs2, int rs1, int offset) {
        // S-type encoding
        int imm11_5 = (offset >> 5) & 0x7F;
        int imm4_0 = offset & 0x1F;
        int raw = (imm11_5 << 25) | (rs2 << 20) | (rs1 << 15) | 
                  (0b010 << 12) | (imm4_0 << 7) | 0b0100011;
        return new Instruction(raw);
    }
    
    public static Instruction beq(int rs1, int rs2, int offset) {
        // B-type encoding
        int imm12 = (offset >> 12) & 0x1;
        int imm11 = (offset >> 11) & 0x1;
        int imm10_5 = (offset >> 5) & 0x3F;
        int imm4_1 = (offset >> 1) & 0xF;
        int raw = (imm12 << 31) | (imm10_5 << 25) | (rs2 << 20) | (rs1 << 15) |
                  (0b000 << 12) | (imm4_1 << 8) | (imm11 << 7) | 0b1100011;
        return new Instruction(raw);
    }
}

