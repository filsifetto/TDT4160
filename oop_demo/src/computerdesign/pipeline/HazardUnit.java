package computerdesign.pipeline;

import computerdesign.pipeline.PipelineRegister.*;

/**
 * HazardUnit - detects and handles pipeline hazards.
 * 
 * Hazards are situations where the pipeline cannot proceed normally:
 * 
 * 1. Data Hazards: An instruction needs data that isn't ready yet
 *    - RAW (Read After Write): Most common, handled by forwarding or stalling
 *    - WAR (Write After Read): Not a problem in simple pipelines
 *    - WAW (Write After Write): Not a problem in simple pipelines
 * 
 * 2. Control Hazards: Branch/jump changes the PC unexpectedly
 *    - Solution: Predict, stall, or flush
 * 
 * 3. Structural Hazards: Two instructions need the same hardware
 *    - Solution: Duplicate hardware or stall
 * 
 * This unit implements:
 * - Forwarding (bypass) to avoid stalls when possible
 * - Stall detection when forwarding isn't possible (e.g., load-use)
 * - Flush signals for branch mispredictions
 */
public class HazardUnit {
    
    /**
     * Forwarding sources for ALU inputs.
     */
    public enum ForwardSource {
        REGISTER,    // Use value from register file (no forwarding)
        EX_MEM,      // Forward from EX/MEM pipeline register
        MEM_WB       // Forward from MEM/WB pipeline register
    }
    
    /**
     * Result of hazard detection.
     */
    public static class HazardResult {
        public boolean stall;           // Stall the pipeline (IF, ID stages)
        public boolean flushIF_ID;      // Flush IF/ID register
        public boolean flushID_EX;      // Flush ID/EX register
        public ForwardSource forwardA;  // Forwarding for ALU input A (rs1)
        public ForwardSource forwardB;  // Forwarding for ALU input B (rs2)
        
        public HazardResult() {
            stall = false;
            flushIF_ID = false;
            flushID_EX = false;
            forwardA = ForwardSource.REGISTER;
            forwardB = ForwardSource.REGISTER;
        }
        
        @Override
        public String toString() {
            return String.format("Hazard{stall=%b, flushIF_ID=%b, flushID_EX=%b, fwdA=%s, fwdB=%s}",
                stall, flushIF_ID, flushID_EX, forwardA, forwardB);
        }
    }
    
    /**
     * Detect hazards and determine forwarding/stalling.
     */
    public HazardResult detect(ID_EX idEx, EX_MEM exMem, MEM_WB memWb) {
        HazardResult result = new HazardResult();
        
        if (!idEx.valid) {
            return result;
        }
        
        int rs1 = idEx.rs1;
        int rs2 = idEx.rs2;
        
        // Check for forwarding from EX/MEM stage
        if (exMem.valid && exMem.control != null && exMem.control.regWrite && exMem.rd != 0) {
            if (exMem.rd == rs1) {
                result.forwardA = ForwardSource.EX_MEM;
            }
            if (exMem.rd == rs2) {
                result.forwardB = ForwardSource.EX_MEM;
            }
        }
        
        // Check for forwarding from MEM/WB stage
        if (memWb.valid && memWb.control != null && memWb.control.regWrite && memWb.rd != 0) {
            // Only forward from MEM/WB if not already forwarding from EX/MEM
            if (memWb.rd == rs1 && result.forwardA == ForwardSource.REGISTER) {
                result.forwardA = ForwardSource.MEM_WB;
            }
            if (memWb.rd == rs2 && result.forwardB == ForwardSource.REGISTER) {
                result.forwardB = ForwardSource.MEM_WB;
            }
        }
        
        // Check for load-use hazard (must stall, can't forward)
        // Load in EX/MEM, and dependent instruction in ID/EX
        if (exMem.valid && exMem.control != null && exMem.control.memRead && exMem.rd != 0) {
            if (exMem.rd == rs1 || exMem.rd == rs2) {
                result.stall = true;
                result.forwardA = ForwardSource.REGISTER;  // Will forward next cycle
                result.forwardB = ForwardSource.REGISTER;
            }
        }
        
        return result;
    }
    
    /**
     * Check if a branch/jump requires flushing.
     */
    public boolean needsFlush(EX_MEM exMem) {
        if (!exMem.valid || exMem.control == null) {
            return false;
        }
        
        // Flush if branch was taken or if it's a jump
        return exMem.branchTaken || exMem.control.jump;
    }
    
    /**
     * Get the correct PC after a branch/jump.
     */
    public int getBranchTarget(EX_MEM exMem) {
        return exMem.branchTarget;
    }
}

