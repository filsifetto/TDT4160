package computerdesign.control;

import computerdesign.alu.ALU;
import computerdesign.instruction.Instruction;

/**
 * ControlUnit - the brain that orchestrates processor operations.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WHAT THE CONTROL UNIT DOES
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * The control unit decodes instructions and generates CONTROL SIGNALS that
 * tell the datapath components what to do. It's the "conductor" of the
 * processor orchestra!
 * 
 * For example, for the instruction "add x1, x2, x3":
 *   - regWrite = true (we're writing to x1)
 *   - aluOp = ADD (we want the ALU to add)
 *   - aluSrcB = false (second operand comes from register, not immediate)
 *   - memRead = false (no memory access)
 *   - memWrite = false (no memory access)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * CONTROL SIGNALS IN THE DATAPATH
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *                              Control Unit
 *                                   │
 *              ┌────────────────────┼────────────────────┐
 *              │                    │                    │
 *              ▼                    ▼                    ▼
 *   ┌──────────────────┐  ┌─────────────────┐  ┌─────────────────┐
 *   │   Register File  │  │      ALU        │  │  Data Memory    │
 *   │   ┌──────────┐   │  │  ┌──────────┐   │  │  ┌──────────┐   │
 *   │   │ regWrite ├───┼──│  │  aluOp   ├───┼──│  │ memRead  ├───│
 *   │   └──────────┘   │  │  └──────────┘   │  │  │ memWrite │   │
 *   └──────────────────┘  │  ┌──────────┐   │  │  └──────────┘   │
 *                         │  │ aluSrcB  ├───┼──│                 │
 *                         │  └──────────┘   │  └─────────────────┘
 *                         └─────────────────┘
 *                                   
 *                    ┌──────────────┴──────────────┐
 *                    │        Multiplexers         │
 *                    │  (memToReg, branch, etc.)   │
 *                    └─────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * CONTROL SIGNAL MEANINGS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * REGISTER CONTROL:
 *   regWrite:  Write result to destination register (rd)?
 * 
 * ALU CONTROL:
 *   aluOp:     What operation should the ALU perform?
 *   aluSrcB:   Where does ALU's second input come from?
 *              false = register (rs2), true = immediate
 * 
 * MEMORY CONTROL:
 *   memRead:   Read data from memory? (for loads)
 *   memWrite:  Write data to memory? (for stores)
 *   memToReg:  What goes to the register file?
 *              false = ALU result, true = memory data
 * 
 * PC CONTROL:
 *   branch:    Is this a conditional branch?
 *   jump:      Is this an unconditional jump?
 *   link:      Should we save PC+4 in rd? (for JAL/JALR)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * COMBINATIONAL VS SEQUENTIAL CONTROL
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * SINGLE-CYCLE: Control is purely combinational
 *   - Decode instruction → produce all signals immediately
 *   - All signals active for the entire cycle
 * 
 * MULTI-CYCLE: Control is sequential (FSM)
 *   - Different signals active in different cycles
 *   - State machine determines which signals when
 *   
 * PIPELINED: Control signals travel with the instruction
 *   - Decode once, carry signals through pipeline registers
 *   - Each stage uses appropriate signals
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @see Instruction - The input to the control unit
 * @see ALU - Uses aluOp to determine operation
 * @see Processor - Uses control signals to orchestrate execution
 */
public class ControlUnit {
    
    /**
     * Control signals produced by the control unit.
     * These signals control the datapath components.
     */
    public static class ControlSignals {
        // Register file control
        public boolean regWrite;      // Write to register file
        
        // ALU control
        public ALU.Operation aluOp;   // ALU operation to perform
        public boolean aluSrcB;       // 0 = rs2, 1 = immediate
        
        // Memory control
        public boolean memRead;       // Read from memory
        public boolean memWrite;      // Write to memory
        public boolean memToReg;      // 0 = ALU result, 1 = memory data
        
        // Branch/jump control
        public boolean branch;        // Conditional branch
        public boolean jump;          // Unconditional jump
        public ALU.BranchCondition branchCond;  // Branch condition
        
        // Immediate handling
        public boolean useUpperImm;   // Use upper immediate (LUI/AUIPC)
        public boolean auipc;         // Add upper immediate to PC
        
        // For JAL/JALR
        public boolean link;          // Save return address
        
        @Override
        public String toString() {
            return String.format(
                "ControlSignals{regWrite=%b, aluOp=%s, aluSrcB=%b, " +
                "memRead=%b, memWrite=%b, memToReg=%b, branch=%b, jump=%b}",
                regWrite, aluOp, aluSrcB, memRead, memWrite, memToReg, branch, jump
            );
        }
    }
    
    /**
     * Decode an instruction and generate control signals.
     * 
     * This is the main decoder logic that reads the opcode, funct3, and funct7
     * fields to determine what the instruction does.
     */
    public ControlSignals decode(Instruction inst) {
        ControlSignals signals = new ControlSignals();
        
        // Default values
        signals.regWrite = false;
        signals.aluOp = ALU.Operation.ADD;
        signals.aluSrcB = false;
        signals.memRead = false;
        signals.memWrite = false;
        signals.memToReg = false;
        signals.branch = false;
        signals.jump = false;
        signals.branchCond = ALU.BranchCondition.EQ;
        signals.useUpperImm = false;
        signals.auipc = false;
        signals.link = false;
        
        int opcode = inst.getOpcode();
        int funct3 = inst.getFunct3();
        int funct7 = inst.getFunct7();
        
        switch (opcode) {
            case 0b0110011:  // R-type (register-register)
                signals.regWrite = true;
                signals.aluOp = decodeRTypeALUOp(funct3, funct7);
                break;
                
            case 0b0010011:  // I-type (register-immediate)
                signals.regWrite = true;
                signals.aluSrcB = true;
                signals.aluOp = decodeITypeALUOp(funct3, funct7, inst.getImmediate());
                break;
                
            case 0b0000011:  // Load instructions
                signals.regWrite = true;
                signals.aluSrcB = true;
                signals.aluOp = ALU.Operation.ADD;
                signals.memRead = true;
                signals.memToReg = true;
                break;
                
            case 0b0100011:  // Store instructions
                signals.aluSrcB = true;
                signals.aluOp = ALU.Operation.ADD;
                signals.memWrite = true;
                break;
                
            case 0b1100011:  // Branch instructions
                signals.branch = true;
                signals.aluOp = ALU.Operation.SUB;
                signals.branchCond = decodeBranchCondition(funct3);
                break;
                
            case 0b1101111:  // JAL
                signals.regWrite = true;
                signals.jump = true;
                signals.link = true;
                break;
                
            case 0b1100111:  // JALR
                signals.regWrite = true;
                signals.aluSrcB = true;
                signals.aluOp = ALU.Operation.ADD;
                signals.jump = true;
                signals.link = true;
                break;
                
            case 0b0110111:  // LUI
                signals.regWrite = true;
                signals.aluOp = ALU.Operation.PASS_B;
                signals.aluSrcB = true;
                signals.useUpperImm = true;
                break;
                
            case 0b0010111:  // AUIPC
                signals.regWrite = true;
                signals.aluOp = ALU.Operation.ADD;
                signals.aluSrcB = true;
                signals.useUpperImm = true;
                signals.auipc = true;
                break;
                
            case 0b1110011:  // System (ECALL/EBREAK)
                // Handled specially
                break;
                
            default:
                // Unknown opcode - could throw exception or NOP
                break;
        }
        
        return signals;
    }
    
    /**
     * Decode ALU operation for R-type instructions.
     */
    private ALU.Operation decodeRTypeALUOp(int funct3, int funct7) {
        boolean isMulDiv = (funct7 == 0b0000001);
        
        if (isMulDiv) {
            switch (funct3) {
                case 0b000: return ALU.Operation.MUL;
                case 0b001: return ALU.Operation.MULH;
                case 0b100: return ALU.Operation.DIV;
                case 0b101: return ALU.Operation.DIVU;
                case 0b110: return ALU.Operation.REM;
                case 0b111: return ALU.Operation.REMU;
            }
        }
        
        switch (funct3) {
            case 0b000: return (funct7 == 0b0100000) ? ALU.Operation.SUB : ALU.Operation.ADD;
            case 0b001: return ALU.Operation.SLL;
            case 0b010: return ALU.Operation.SLT;
            case 0b011: return ALU.Operation.SLTU;
            case 0b100: return ALU.Operation.XOR;
            case 0b101: return (funct7 == 0b0100000) ? ALU.Operation.SRA : ALU.Operation.SRL;
            case 0b110: return ALU.Operation.OR;
            case 0b111: return ALU.Operation.AND;
            default: return ALU.Operation.ADD;
        }
    }
    
    /**
     * Decode ALU operation for I-type instructions.
     */
    private ALU.Operation decodeITypeALUOp(int funct3, int funct7, int imm) {
        switch (funct3) {
            case 0b000: return ALU.Operation.ADD;  // ADDI
            case 0b001: return ALU.Operation.SLL;  // SLLI
            case 0b010: return ALU.Operation.SLT;  // SLTI
            case 0b011: return ALU.Operation.SLTU; // SLTIU
            case 0b100: return ALU.Operation.XOR;  // XORI
            case 0b101: 
                // Check bit 30 of immediate for SRAI vs SRLI
                return ((imm >> 10) & 1) == 1 ? ALU.Operation.SRA : ALU.Operation.SRL;
            case 0b110: return ALU.Operation.OR;   // ORI
            case 0b111: return ALU.Operation.AND;  // ANDI
            default: return ALU.Operation.ADD;
        }
    }
    
    /**
     * Decode branch condition from funct3.
     */
    private ALU.BranchCondition decodeBranchCondition(int funct3) {
        switch (funct3) {
            case 0b000: return ALU.BranchCondition.EQ;   // BEQ
            case 0b001: return ALU.BranchCondition.NE;   // BNE
            case 0b100: return ALU.BranchCondition.LT;   // BLT
            case 0b101: return ALU.BranchCondition.GE;   // BGE
            case 0b110: return ALU.BranchCondition.LTU;  // BLTU
            case 0b111: return ALU.BranchCondition.GEU;  // BGEU
            default: return ALU.BranchCondition.EQ;
        }
    }
}

