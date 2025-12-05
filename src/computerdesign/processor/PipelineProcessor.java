package computerdesign.processor;

import computerdesign.alu.ALU;
import computerdesign.control.ControlUnit;
import computerdesign.control.ControlUnit.ControlSignals;
import computerdesign.instruction.Instruction;
import computerdesign.memory.MainMemory;
import computerdesign.memory.RegisterFile;
import computerdesign.pipeline.HazardUnit;
import computerdesign.pipeline.HazardUnit.ForwardSource;
import computerdesign.pipeline.HazardUnit.HazardResult;
import computerdesign.pipeline.PipelineRegister.*;

/**
 * PipelineProcessor - executes multiple instructions simultaneously.
 * 
 * The pipeline divides instruction execution into 5 stages, with each stage
 * working on a different instruction each cycle:
 * 
 *   Cycle:    1    2    3    4    5    6    7    8
 *   Inst 1:  IF   ID   EX  MEM   WB
 *   Inst 2:       IF   ID   EX  MEM   WB
 *   Inst 3:            IF   ID   EX  MEM   WB
 *   Inst 4:                 IF   ID   EX  MEM   WB
 * 
 * Advantages:
 * - High throughput (ideally CPI = 1, like single-cycle)
 * - Short clock period (like multi-cycle)
 * - Best of both worlds!
 * 
 * Challenges (Hazards):
 * - Data hazards: Solved by forwarding and stalling
 * - Control hazards: Solved by flushing (or prediction)
 * - Structural hazards: Solved by separate I-cache and D-cache
 * 
 * State: Pipeline registers hold the state of each instruction as it
 * progresses through the pipeline.
 */
public class PipelineProcessor implements Processor {
    
    // Processor components
    private int pc;
    private final RegisterFile registers;
    private final MainMemory memory;
    private final ALU alu;
    private final ControlUnit control;
    private final HazardUnit hazardUnit;
    
    // Pipeline registers (THE KEY STATE for pipelining)
    private IF_ID ifId;
    private ID_EX idEx;
    private EX_MEM exMem;
    private MEM_WB memWb;
    
    // Next state (double-buffered to prevent race conditions)
    private IF_ID nextIfId;
    private ID_EX nextIdEx;
    private EX_MEM nextExMem;
    private MEM_WB nextMemWb;
    
    // Processor state
    private boolean halted;
    private final ProcessorStats stats;
    
    public PipelineProcessor(MainMemory memory) {
        this.memory = memory;
        this.registers = new RegisterFile();
        this.alu = new ALU();
        this.control = new ControlUnit();
        this.hazardUnit = new HazardUnit();
        this.stats = new ProcessorStats();
        
        // Initialize pipeline registers
        ifId = new IF_ID();
        idEx = new ID_EX();
        exMem = new EX_MEM();
        memWb = new MEM_WB();
        
        nextIfId = new IF_ID();
        nextIdEx = new ID_EX();
        nextExMem = new EX_MEM();
        nextMemWb = new MEM_WB();
        
        reset();
    }
    
    public PipelineProcessor() {
        this(new MainMemory());
    }
    
    @Override
    public void cycle() {
        if (halted) return;
        
        // Detect hazards BEFORE executing stages
        HazardResult hazards = hazardUnit.detect(idEx, exMem, memWb);
        boolean needsFlush = hazardUnit.needsFlush(exMem);
        
        // Execute all stages IN REVERSE ORDER to avoid overwriting
        // (In hardware, they all execute simultaneously)
        executeWriteBack();
        executeMemory();
        executeExecute(hazards);
        executeDecode(hazards, needsFlush);
        executeFetch(hazards, needsFlush);
        
        // Update pipeline registers (clock edge)
        ifId = nextIfId;
        idEx = nextIdEx;
        exMem = nextExMem;
        memWb = nextMemWb;
        
        // Prepare next state buffers
        nextIfId = new IF_ID();
        nextIdEx = new ID_EX();
        nextExMem = new EX_MEM();
        nextMemWb = new MEM_WB();
        
        stats.incrementCycles();
    }
    
    /**
     * Fetch stage: Read instruction from memory.
     */
    private void executeFetch(HazardResult hazards, boolean needsFlush) {
        if (hazards.stall) {
            // Keep the same instruction in IF/ID (stall)
            nextIfId.pc = ifId.pc;
            nextIfId.instruction = ifId.instruction;
            nextIfId.valid = ifId.valid;
            stats.incrementStalls();
            return;
        }
        
        if (needsFlush) {
            // Flush - insert bubble
            nextIfId.clear();
            pc = hazardUnit.getBranchTarget(exMem);
            stats.incrementMispredictions();
            return;
        }
        
        // Normal fetch
        Instruction inst = fetch();
        
        if (inst.isHalt()) {
            nextIfId.clear();
            // Let the pipeline drain
            if (!ifId.valid && !idEx.valid && !exMem.valid && !memWb.valid) {
                halted = true;
            }
            return;
        }
        
        nextIfId.pc = pc;
        nextIfId.instruction = inst;
        nextIfId.valid = true;
        
        pc = pc + 4;
    }
    
    /**
     * Decode stage: Decode instruction and read registers.
     */
    private void executeDecode(HazardResult hazards, boolean needsFlush) {
        if (needsFlush || hazards.flushID_EX) {
            // Flush - insert bubble
            nextIdEx.clear();
            return;
        }
        
        if (hazards.stall) {
            // Insert bubble into ID/EX (stall)
            nextIdEx.clear();
            stats.incrementStalls();
            return;
        }
        
        if (!ifId.valid) {
            nextIdEx.clear();
            return;
        }
        
        Instruction inst = ifId.instruction;
        ControlSignals signals = control.decode(inst);
        
        nextIdEx.pc = ifId.pc;
        nextIdEx.instruction = inst;
        nextIdEx.control = signals;
        nextIdEx.rs1Value = registers.read(inst.getRs1());
        nextIdEx.rs2Value = registers.read(inst.getRs2());
        nextIdEx.immediate = inst.getImmediate();
        nextIdEx.rd = inst.getRd();
        nextIdEx.rs1 = inst.getRs1();
        nextIdEx.rs2 = inst.getRs2();
        nextIdEx.valid = true;
    }
    
    /**
     * Execute stage: Perform ALU operation.
     */
    private void executeExecute(HazardResult hazards) {
        if (!idEx.valid) {
            nextExMem.clear();
            return;
        }
        
        ControlSignals signals = idEx.control;
        
        // Apply forwarding
        int rs1Val = getForwardedValue(idEx.rs1Value, hazards.forwardA);
        int rs2Val = getForwardedValue(idEx.rs2Value, hazards.forwardB);
        
        // ALU operation
        int aluInputA = signals.auipc ? idEx.pc : rs1Val;
        int aluInputB = signals.aluSrcB ? idEx.immediate : rs2Val;
        int aluResult = alu.compute(aluInputA, aluInputB, signals.aluOp);
        
        // Branch/jump handling
        boolean branchTaken = false;
        int branchTarget = idEx.pc + 4;
        
        if (signals.branch) {
            stats.incrementBranches();
            branchTaken = alu.compare(rs1Val, rs2Val, signals.branchCond);
            if (branchTaken) {
                branchTarget = idEx.pc + idEx.immediate;
            }
        } else if (signals.jump) {
            branchTaken = true;
            if (idEx.instruction.getOpcode() == 0b1101111) {
                // JAL
                branchTarget = idEx.pc + idEx.immediate;
                aluResult = idEx.pc + 4;  // Return address
            } else {
                // JALR
                branchTarget = (rs1Val + idEx.immediate) & ~1;
                aluResult = idEx.pc + 4;  // Return address
            }
        }
        
        nextExMem.pc = idEx.pc;
        nextExMem.instruction = idEx.instruction;
        nextExMem.control = signals;
        nextExMem.aluResult = aluResult;
        nextExMem.rs2Value = rs2Val;
        nextExMem.rd = idEx.rd;
        nextExMem.branchTaken = branchTaken;
        nextExMem.branchTarget = branchTarget;
        nextExMem.valid = true;
    }
    
    /**
     * Memory stage: Access memory for loads/stores.
     */
    private void executeMemory() {
        if (!exMem.valid) {
            nextMemWb.clear();
            return;
        }
        
        ControlSignals signals = exMem.control;
        int memData = 0;
        
        if (signals.memRead) {
            memData = memory.read(exMem.aluResult);
            stats.incrementMemoryAccesses();
        }
        
        if (signals.memWrite) {
            memory.write(exMem.aluResult, exMem.rs2Value);
            stats.incrementMemoryAccesses();
        }
        
        nextMemWb.pc = exMem.pc;
        nextMemWb.instruction = exMem.instruction;
        nextMemWb.control = signals;
        nextMemWb.aluResult = exMem.aluResult;
        nextMemWb.memoryData = memData;
        nextMemWb.rd = exMem.rd;
        nextMemWb.valid = true;
    }
    
    /**
     * Write-back stage: Write result to register file.
     */
    private void executeWriteBack() {
        if (!memWb.valid) {
            return;
        }
        
        ControlSignals signals = memWb.control;
        
        if (signals.regWrite) {
            int writeData;
            if (signals.memToReg) {
                writeData = memWb.memoryData;
            } else {
                writeData = memWb.aluResult;
            }
            registers.write(memWb.rd, writeData);
        }
        
        stats.incrementInstructions();
    }
    
    /**
     * Get the forwarded value based on the forwarding source.
     */
    private int getForwardedValue(int regValue, ForwardSource source) {
        switch (source) {
            case EX_MEM:
                return exMem.aluResult;
            case MEM_WB:
                return memWb.control.memToReg ? memWb.memoryData : memWb.aluResult;
            default:
                return regValue;
        }
    }
    
    @Override
    public int run(int maxCycles) {
        int cyclesExecuted = 0;
        while (!halted && cyclesExecuted < maxCycles) {
            cycle();
            cyclesExecuted++;
        }
        return cyclesExecuted;
    }
    
    @Override
    public Instruction fetch() {
        int rawInstruction = memory.read(pc);
        return new Instruction(rawInstruction);
    }
    
    @Override
    public int getPC() {
        return pc;
    }
    
    @Override
    public void setPC(int address) {
        this.pc = address;
    }
    
    @Override
    public RegisterFile getRegisterFile() {
        return registers;
    }
    
    @Override
    public MainMemory getMemory() {
        return memory;
    }
    
    @Override
    public boolean isHalted() {
        return halted;
    }
    
    @Override
    public void reset() {
        pc = 0;
        halted = false;
        ifId.clear();
        idEx.clear();
        exMem.clear();
        memWb.clear();
        registers.reset();
        stats.reset();
    }
    
    @Override
    public ProcessorStats getStats() {
        return stats;
    }
    
    /**
     * Get pipeline state for visualization/debugging.
     */
    public String getPipelineState() {
        return String.format(
            "Pipeline State:\n" +
            "  IF/ID:  %s\n" +
            "  ID/EX:  %s\n" +
            "  EX/MEM: %s\n" +
            "  MEM/WB: %s",
            ifId, idEx, exMem, memWb
        );
    }
    
    @Override
    public String toString() {
        return String.format("PipelineProcessor{pc=0x%08X, halted=%b, CPI=%.2f}", 
            pc, halted, stats.getCPI());
    }
}

