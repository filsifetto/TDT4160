package computerdesign.pipeline;

import computerdesign.pipeline.PipelineRegister.*;

/**
 * HazardUnit - detects and handles pipeline hazards.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE PRICE OF PIPELINING: HAZARDS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Pipelining increases throughput by overlapping instruction execution.
 * But this creates HAZARDS - situations where the next instruction can't
 * execute because of a dependency on a previous instruction.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * HAZARD TYPE 1: DATA HAZARDS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Occur when an instruction depends on the result of a previous instruction
 * that hasn't completed yet.
 * 
 * RAW (Read After Write) - Most common:
 *   add  x1, x2, x3    # Writes x1 in WB stage (cycle 5)
 *   sub  x4, x1, x5    # Needs x1 in ID stage (cycle 3)!
 *   
 *   Cycle:    1    2    3    4    5    6
 *   add:     IF   ID   EX   MEM  WB
 *   sub:          IF   ID   EX   MEM  WB
 *                      ↑
 *                  Needs x1, but x1 not written yet!
 * 
 * SOLUTION 1: FORWARDING (Bypassing)
 *   Don't wait for WB - forward the result from EX/MEM or MEM/WB directly!
 *   
 *   Cycle:    1    2    3    4    5
 *   add:     IF   ID   EX   MEM  WB
 *   sub:          IF   ID   EX   MEM  WB
 *                      │    ↑
 *                      └────┘ Forward from EX/MEM!
 * 
 * SOLUTION 2: STALLING (when forwarding isn't possible)
 *   lw   x1, 0(x2)     # Data available after MEM stage
 *   add  x3, x1, x4    # Needs x1 in EX stage - can't forward in time!
 *   
 *   Must insert a "bubble" (NOP):
 *   Cycle:    1    2    3    4    5    6    7
 *   lw:      IF   ID   EX   MEM  WB
 *   add:          IF   ID   ──   EX   MEM  WB
 *                      ↑    │
 *                      │    stall (bubble)
 *                      │
 *                      Can forward from MEM/WB next cycle
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * HAZARD TYPE 2: CONTROL HAZARDS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Occur when a branch or jump changes the PC, but we've already fetched
 * the wrong instructions!
 * 
 *   beq  x1, x2, target  # Branch decision in EX stage
 *   add  x3, x4, x5      # Fetched speculatively - wrong if branch taken!
 *   sub  x6, x7, x8      # Also wrong if branch taken!
 *   
 *   Cycle:    1    2    3    4    5
 *   beq:     IF   ID   EX   MEM  WB
 *   add:          IF   ID   ──   ── (flushed if branch taken)
 *   sub:               IF   ──   ── (flushed if branch taken)
 * 
 * SOLUTIONS:
 *   1. STALL: Always wait until branch is resolved (slow)
 *   2. FLUSH: Execute speculatively, flush if wrong
 *   3. PREDICT: Guess branch outcome, flush if wrong
 *      - Static: Always predict not-taken, or always taken
 *      - Dynamic: Use history to predict (branch predictor)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * HAZARD TYPE 3: STRUCTURAL HAZARDS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Occur when two instructions need the same hardware resource.
 * 
 * Example: Single memory port for both instructions and data
 *   lw    ...         # MEM stage: reading data from memory
 *   ???              # IF stage: fetching instruction from memory
 *   
 *   Can't do both at once with single-ported memory!
 * 
 * SOLUTION: Duplicate hardware
 *   - Separate instruction and data caches (Harvard architecture)
 *   - Multiple register file ports
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @see PipelineProcessor - Uses this unit for hazard handling
 * @see PipelineRegister - The pipeline registers between stages
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

