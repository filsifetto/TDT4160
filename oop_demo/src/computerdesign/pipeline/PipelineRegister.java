package computerdesign.pipeline;

import computerdesign.control.ControlUnit.ControlSignals;
import computerdesign.instruction.Instruction;

/**
 * PipelineRegister - holds data between pipeline stages.
 * 
 * In a pipelined processor, we need registers between each stage to hold
 * the intermediate results. Each register is named for the stages it connects:
 * - IF/ID: Between Fetch and Decode
 * - ID/EX: Between Decode and Execute
 * - EX/MEM: Between Execute and Memory
 * - MEM/WB: Between Memory and WriteBack
 * 
 * These registers are clocked - they update at the clock edge, allowing
 * multiple instructions to be "in flight" simultaneously.
 */
public class PipelineRegister {
    
    /**
     * IF/ID pipeline register.
     */
    public static class IF_ID {
        public int pc;
        public Instruction instruction;
        public boolean valid;
        
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
     * ID/EX pipeline register.
     */
    public static class ID_EX {
        public int pc;
        public Instruction instruction;
        public ControlSignals control;
        public int rs1Value;
        public int rs2Value;
        public int immediate;
        public int rd;
        public int rs1;
        public int rs2;
        public boolean valid;
        
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
     * EX/MEM pipeline register.
     */
    public static class EX_MEM {
        public int pc;
        public Instruction instruction;
        public ControlSignals control;
        public int aluResult;
        public int rs2Value;  // For stores
        public int rd;
        public boolean branchTaken;
        public int branchTarget;
        public boolean valid;
        
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
     * MEM/WB pipeline register.
     */
    public static class MEM_WB {
        public int pc;
        public Instruction instruction;
        public ControlSignals control;
        public int aluResult;
        public int memoryData;
        public int rd;
        public boolean valid;
        
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

