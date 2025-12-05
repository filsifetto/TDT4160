package computerdesign.instruction;

/**
 * InstructionDecoder - utility class for disassembling instructions.
 * 
 * Converts binary instructions back to human-readable assembly.
 * Useful for debugging and understanding what the processor is doing.
 */
public class InstructionDecoder {
    
    private static final String[] REG_NAMES = {
        "zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
        "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
        "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
        "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
    };
    
    /**
     * Disassemble an instruction to human-readable form.
     */
    public static String disassemble(Instruction inst) {
        int opcode = inst.getOpcode();
        int rd = inst.getRd();
        int rs1 = inst.getRs1();
        int rs2 = inst.getRs2();
        int funct3 = inst.getFunct3();
        int funct7 = inst.getFunct7();
        int imm = inst.getImmediate();
        
        switch (opcode) {
            case 0b0110011:  // R-type
                return disassembleRType(rd, rs1, rs2, funct3, funct7);
                
            case 0b0010011:  // I-type arithmetic
                return disassembleITypeArith(rd, rs1, imm, funct3, funct7);
                
            case 0b0000011:  // Loads
                return disassembleLoad(rd, rs1, imm, funct3);
                
            case 0b0100011:  // Stores
                return disassembleStore(rs1, rs2, imm, funct3);
                
            case 0b1100011:  // Branches
                return disassembleBranch(rs1, rs2, imm, funct3);
                
            case 0b1101111:  // JAL
                return String.format("jal %s, %d", reg(rd), imm);
                
            case 0b1100111:  // JALR
                return String.format("jalr %s, %s, %d", reg(rd), reg(rs1), imm);
                
            case 0b0110111:  // LUI
                return String.format("lui %s, 0x%X", reg(rd), imm >>> 12);
                
            case 0b0010111:  // AUIPC
                return String.format("auipc %s, 0x%X", reg(rd), imm >>> 12);
                
            case 0b1110011:  // System
                if (inst.getRaw() == 0x00000073) return "ecall";
                if (inst.getRaw() == 0x00100073) return "ebreak";
                return "system";
                
            default:
                return String.format("unknown (0x%08X)", inst.getRaw());
        }
    }
    
    private static String disassembleRType(int rd, int rs1, int rs2, int funct3, int funct7) {
        String mnemonic;
        
        if (funct7 == 0b0000001) {  // M extension
            switch (funct3) {
                case 0b000: mnemonic = "mul"; break;
                case 0b001: mnemonic = "mulh"; break;
                case 0b010: mnemonic = "mulhsu"; break;
                case 0b011: mnemonic = "mulhu"; break;
                case 0b100: mnemonic = "div"; break;
                case 0b101: mnemonic = "divu"; break;
                case 0b110: mnemonic = "rem"; break;
                case 0b111: mnemonic = "remu"; break;
                default: mnemonic = "unknown"; break;
            }
        } else {
            switch (funct3) {
                case 0b000: mnemonic = (funct7 == 0b0100000) ? "sub" : "add"; break;
                case 0b001: mnemonic = "sll"; break;
                case 0b010: mnemonic = "slt"; break;
                case 0b011: mnemonic = "sltu"; break;
                case 0b100: mnemonic = "xor"; break;
                case 0b101: mnemonic = (funct7 == 0b0100000) ? "sra" : "srl"; break;
                case 0b110: mnemonic = "or"; break;
                case 0b111: mnemonic = "and"; break;
                default: mnemonic = "unknown"; break;
            }
        }
        
        return String.format("%s %s, %s, %s", mnemonic, reg(rd), reg(rs1), reg(rs2));
    }
    
    private static String disassembleITypeArith(int rd, int rs1, int imm, int funct3, int funct7) {
        String mnemonic;
        
        switch (funct3) {
            case 0b000: mnemonic = "addi"; break;
            case 0b001: mnemonic = "slli"; imm &= 0x1F; break;
            case 0b010: mnemonic = "slti"; break;
            case 0b011: mnemonic = "sltiu"; break;
            case 0b100: mnemonic = "xori"; break;
            case 0b101: 
                mnemonic = ((imm >> 10) & 1) == 1 ? "srai" : "srli";
                imm &= 0x1F;
                break;
            case 0b110: mnemonic = "ori"; break;
            case 0b111: mnemonic = "andi"; break;
            default: mnemonic = "unknown"; break;
        }
        
        return String.format("%s %s, %s, %d", mnemonic, reg(rd), reg(rs1), imm);
    }
    
    private static String disassembleLoad(int rd, int rs1, int imm, int funct3) {
        String mnemonic;
        
        switch (funct3) {
            case 0b000: mnemonic = "lb"; break;
            case 0b001: mnemonic = "lh"; break;
            case 0b010: mnemonic = "lw"; break;
            case 0b100: mnemonic = "lbu"; break;
            case 0b101: mnemonic = "lhu"; break;
            default: mnemonic = "load"; break;
        }
        
        return String.format("%s %s, %d(%s)", mnemonic, reg(rd), imm, reg(rs1));
    }
    
    private static String disassembleStore(int rs1, int rs2, int imm, int funct3) {
        String mnemonic;
        
        switch (funct3) {
            case 0b000: mnemonic = "sb"; break;
            case 0b001: mnemonic = "sh"; break;
            case 0b010: mnemonic = "sw"; break;
            default: mnemonic = "store"; break;
        }
        
        return String.format("%s %s, %d(%s)", mnemonic, reg(rs2), imm, reg(rs1));
    }
    
    private static String disassembleBranch(int rs1, int rs2, int imm, int funct3) {
        String mnemonic;
        
        switch (funct3) {
            case 0b000: mnemonic = "beq"; break;
            case 0b001: mnemonic = "bne"; break;
            case 0b100: mnemonic = "blt"; break;
            case 0b101: mnemonic = "bge"; break;
            case 0b110: mnemonic = "bltu"; break;
            case 0b111: mnemonic = "bgeu"; break;
            default: mnemonic = "branch"; break;
        }
        
        return String.format("%s %s, %s, %d", mnemonic, reg(rs1), reg(rs2), imm);
    }
    
    private static String reg(int num) {
        return REG_NAMES[num & 0x1F];
    }
}

