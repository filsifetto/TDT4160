package computerdesign.pipeline;

import computerdesign.control.ControlUnit.ControlSignals;
import computerdesign.instruction.Instruction;

/**
 * PipelineRegister - holds data between pipeline stages.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WHERE THIS FITS IN THE PROCESSOR ARCHITECTURE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * The processor is divided into DATAPATH and CONTROL:
 * 
 *   DATAPATH (moves and transforms data):
 *     • ALU, Register File, Memory, Multiplexers
 *     • Pipeline Registers ← THIS CLASS!
 * 
 *   CONTROL (decides what happens when):
 *     • Control Unit (generates signals from instructions)
 *     • Hazard Unit (handles pipeline hazards)
 * 
 * Pipeline registers are DATAPATH components because they hold DATA that
 * flows through the processor. However, they also carry CONTROL SIGNALS
 * that travel with each instruction through the pipeline.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE FIVE-STAGE PIPELINE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   ┌─────┐    ┌─────┐    ┌─────┐    ┌─────┐    ┌─────┐    ┌─────┐
 *   │ IF  │───►│IF/ID│───►│ ID  │───►│ID/EX│───►│ EX  │───►│EX/MEM│──►...
 *   │     │    │     │    │     │    │     │    │     │    │      │
 *   │Fetch│    │ reg │    │Decode    │ reg │    │Execute   │ reg  │
 *   └─────┘    └─────┘    └─────┘    └─────┘    └─────┘    └──────┘
 *                                                               │
 *                                                               ▼
 *   ┌─────┐    ┌──────┐    ┌─────┐
 *   │ WB  │◄───│MEM/WB│◄───│ MEM │
 *   │     │    │      │    │     │
 *   │Write│    │ reg  │    │Memory
 *   │Back │    │      │    │Access
 *   └─────┘    └──────┘    └─────┘
 * 
 * Each stage (IF, ID, EX, MEM, WB) does ONE thing per cycle.
 * Pipeline registers (IF/ID, ID/EX, etc.) hold data BETWEEN stages.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WHY WE NEED PIPELINE REGISTERS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Without pipeline registers:
 *   - Data from one stage would immediately affect the next
 *   - We could only have ONE instruction in the pipeline
 *   - Same as a single-cycle processor (no pipelining benefit!)
 * 
 * With pipeline registers:
 *   - Data is "latched" at the clock edge
 *   - Each stage can work on a DIFFERENT instruction
 *   - 5 instructions can be "in flight" simultaneously!
 * 
 *   Cycle:  1    2    3    4    5    6    7    8
 *   I1:    IF   ID   EX   MEM  WB
 *   I2:         IF   ID   EX   MEM  WB
 *   I3:              IF   ID   EX   MEM  WB
 *   I4:                   IF   ID   EX   MEM  WB
 *   I5:                        IF   ID   EX   MEM  WB
 *   
 *   After cycle 5: completing 1 instruction per cycle (ideal CPI = 1)!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WHAT EACH REGISTER HOLDS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * IF/ID: Instruction bits + PC (fetched, ready for decode)
 * ID/EX: Register values + immediate + control signals (ready for ALU)
 * EX/MEM: ALU result + store data + control signals (ready for memory)
 * MEM/WB: Memory data + ALU result + control signals (ready for writeback)
 * 
 * KEY INSIGHT: Control signals TRAVEL with the instruction!
 * They're generated once in ID, then passed through ID/EX → EX/MEM → MEM/WB.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @see HazardUnit - Handles hazards that arise from pipelining
 * @see PipelineProcessor - Uses these registers
 * @see ControlUnit - Generates the control signals stored in these registers
 */
public class PipelineRegister {
    
    /**
     * IF/ID Pipeline Register - between Instruction Fetch and Instruction Decode.
     * 
     * After FETCH, we have:
     *   - The instruction (32 bits from instruction memory)
     *   - The PC (needed for PC-relative addressing, JAL, branches)
     * 
     * This gets passed to DECODE, which will:
     *   - Read the instruction fields (opcode, rs1, rs2, rd, immediate)
     *   - Read register values for rs1 and rs2
     *   - Generate control signals
     */
    public static class IF_ID {
        public int pc;                  // Program counter (for branches/jumps)
        public Instruction instruction; // The fetched 32-bit instruction
        public boolean valid;           // Is this a real instruction? (vs bubble)
        
        public IF_ID() {
            clear();
        }
        
        public void clear() {
            pc = 0;
            instruction = Instruction.nop();
            valid = false;
        }
        
        @Override
        public String toString() {
            return String.format("IF/ID{pc=0x%X, inst=%s, valid=%b}", 
                pc, instruction.disassemble(), valid);
        }
    }
    
    /**
     * ID/EX Pipeline Register - between Instruction Decode and Execute.
     * 
     * After DECODE, we have:
     *   - Control signals (what operation to perform)
     *   - Register values (rs1 and rs2, read from register file)
     *   - Immediate value (sign-extended)
     *   - Register numbers (for hazard detection and forwarding)
     * 
     * This gets passed to EXECUTE, which will:
     *   - Perform the ALU operation (add, sub, and, etc.)
     *   - Calculate branch/jump targets
     *   - Evaluate branch conditions
     * 
     * NOTE: We store rs1/rs2 numbers for FORWARDING!
     * The hazard unit needs to know which registers we're reading
     * to detect data dependencies.
     */
    public static class ID_EX {
        public int pc;                  // Program counter (for AUIPC, branches)
        public Instruction instruction; // Original instruction (for debugging)
        public ControlSignals control;  // Control signals (generated by decode)
        public int rs1Value;            // Value of source register 1
        public int rs2Value;            // Value of source register 2
        public int immediate;           // Sign-extended immediate
        public int rd;                  // Destination register number
        public int rs1;                 // Source register 1 number (for forwarding)
        public int rs2;                 // Source register 2 number (for forwarding)
        public boolean valid;           // Is this a real instruction?
        
        public ID_EX() {
            clear();
        }
        
        public void clear() {
            pc = 0;
            instruction = Instruction.nop();
            control = null;
            rs1Value = 0;
            rs2Value = 0;
            immediate = 0;
            rd = 0;
            rs1 = 0;
            rs2 = 0;
            valid = false;
        }
        
        @Override
        public String toString() {
            String inst = instruction != null ? instruction.disassemble() : "null";
            return String.format("ID/EX{pc=0x%X, inst=%s, valid=%b}", pc, inst, valid);
        }
    }
    
    /**
     * EX/MEM Pipeline Register - between Execute and Memory Access.
     * 
     * After EXECUTE, we have:
     *   - ALU result (computed value, or memory address for load/store)
     *   - Branch decision (was the branch taken?)
     *   - Branch target (where to jump if branch taken)
     *   - rs2 value (data to write for store instructions)
     * 
     * This gets passed to MEMORY, which will:
     *   - Read from data memory (for loads)
     *   - Write to data memory (for stores)
     *   - Signal branch/jump resolution to fetch stage
     * 
     * NOTE: We keep rs2Value for STORE instructions!
     * For "sw rs2, offset(rs1)", the address comes from ALU (rs1 + offset),
     * but we need rs2's value to write to memory.
     */
    public static class EX_MEM {
        public int pc;                  // Program counter
        public Instruction instruction; // Original instruction (for debugging)
        public ControlSignals control;  // Control signals (passed through)
        public int aluResult;           // Result of ALU operation
        public int rs2Value;            // Value to store (for SW, SB, SH)
        public int rd;                  // Destination register (for forwarding)
        public boolean branchTaken;     // Was the branch condition true?
        public int branchTarget;        // Target address if branch taken
        public boolean valid;           // Is this a real instruction?
        
        public EX_MEM() {
            clear();
        }
        
        public void clear() {
            pc = 0;
            instruction = Instruction.nop();
            control = null;
            aluResult = 0;
            rs2Value = 0;
            rd = 0;
            branchTaken = false;
            branchTarget = 0;
            valid = false;
        }
        
        @Override
        public String toString() {
            String inst = instruction != null ? instruction.disassemble() : "null";
            return String.format("EX/MEM{pc=0x%X, inst=%s, aluRes=0x%X, valid=%b}", 
                pc, inst, aluResult, valid);
        }
    }
    
    /**
     * MEM/WB Pipeline Register - between Memory Access and Write Back.
     * 
     * After MEMORY, we have:
     *   - ALU result (for arithmetic/logic instructions)
     *   - Memory data (for load instructions)
     *   - Control signals indicating which to use
     * 
     * This gets passed to WRITEBACK, which will:
     *   - Select the right value (ALU result or memory data)
     *   - Write to the destination register (if regWrite is true)
     * 
     * This is the LAST stage where data can be forwarded from!
     * If an instruction in ID needs a value that's in MEM/WB,
     * we can forward it instead of stalling.
     */
    public static class MEM_WB {
        public int pc;                  // Program counter
        public Instruction instruction; // Original instruction (for debugging)
        public ControlSignals control;  // Control signals (memToReg, regWrite)
        public int aluResult;           // Result from ALU (for R-type, I-type)
        public int memoryData;          // Data read from memory (for loads)
        public int rd;                  // Destination register number
        public boolean valid;           // Is this a real instruction?
        
        public MEM_WB() {
            clear();
        }
        
        public void clear() {
            pc = 0;
            instruction = Instruction.nop();
            control = null;
            aluResult = 0;
            memoryData = 0;
            rd = 0;
            valid = false;
        }
        
        @Override
        public String toString() {
            String inst = instruction != null ? instruction.disassemble() : "null";
            return String.format("MEM/WB{pc=0x%X, inst=%s, aluRes=0x%X, memData=0x%X, valid=%b}", 
                pc, inst, aluResult, memoryData, valid);
        }
    }
}

