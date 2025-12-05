package computerdesign.processor;

import computerdesign.alu.ALU;
import computerdesign.control.ControlUnit;
import computerdesign.control.ControlUnit.ControlSignals;
import computerdesign.instruction.Instruction;
import computerdesign.memory.MainMemory;
import computerdesign.memory.RegisterFile;

/**
 * MultiCycleProcessor - executes instructions over multiple clock cycles.
 * 
 * Unlike single-cycle, this processor breaks instruction execution into
 * multiple steps, each taking one clock cycle:
 * 
 * 1. Instruction Fetch (IF): Fetch instruction, increment PC
 * 2. Instruction Decode (ID): Decode, read registers
 * 3. Execute (EX): ALU operation
 * 4. Memory (MEM): Memory access (if needed)
 * 5. Write Back (WB): Write result to register (if needed)
 * 
 * Advantages:
 * - Shorter clock period (only needs to accommodate one stage)
 * - Different instructions take different numbers of cycles
 * - Better hardware utilization (reuse ALU for PC increment, etc.)
 * 
 * Disadvantages:
 * - CPI > 1 (typically 3-5 cycles per instruction)
 * - More complex control (FSM instead of combinational)
 * - Need intermediate registers between stages
 * 
 * State: This processor has MORE state than single-cycle because it needs
 * to remember intermediate results between cycles.
 */
public class MultiCycleProcessor implements Processor {
    
    // The stages of multi-cycle execution
    public enum Stage {
        FETCH,
        DECODE,
        EXECUTE,
        MEMORY,
        WRITEBACK
    }
    
    // Processor components
    private int pc;
    private final RegisterFile registers;
    private final MainMemory memory;
    private final ALU alu;
    private final ControlUnit control;
    
    // Multi-cycle state: intermediate registers
    private Stage currentStage;
    private Instruction currentInstruction;
    private ControlSignals currentSignals;
    private int rs1Value;
    private int rs2Value;
    private int aluResult;
    private int memoryData;
    
    // Processor state
    private boolean halted;
    private final ProcessorStats stats;
    
    public MultiCycleProcessor(MainMemory memory) {
        this.memory = memory;
        this.registers = new RegisterFile();
        this.alu = new ALU();
        this.control = new ControlUnit();
        this.stats = new ProcessorStats();
        reset();
    }
    
    public MultiCycleProcessor() {
        this(new MainMemory());
    }
    
    @Override
    public void cycle() {
        if (halted) return;
        
        switch (currentStage) {
            case FETCH:
                executeFetch();
                break;
            case DECODE:
                executeDecode();
                break;
            case EXECUTE:
                executeExecute();
                break;
            case MEMORY:
                executeMemory();
                break;
            case WRITEBACK:
                executeWriteBack();
                break;
        }
        
        stats.incrementCycles();
    }
    
    /**
     * Fetch stage: Read instruction from memory.
     */
    private void executeFetch() {
        currentInstruction = fetch();
        
        if (currentInstruction.isHalt()) {
            halted = true;
            return;
        }
        
        currentStage = Stage.DECODE;
    }
    
    /**
     * Decode stage: Decode instruction and read registers.
     */
    private void executeDecode() {
        currentSignals = control.decode(currentInstruction);
        rs1Value = registers.read(currentInstruction.getRs1());
        rs2Value = registers.read(currentInstruction.getRs2());
        
        currentStage = Stage.EXECUTE;
    }
    
    /**
     * Execute stage: Perform ALU operation.
     */
    private void executeExecute() {
        int immediate = currentInstruction.getImmediate();
        int aluInputA = currentSignals.auipc ? pc : rs1Value;
        int aluInputB = currentSignals.aluSrcB ? immediate : rs2Value;
        
        aluResult = alu.compute(aluInputA, aluInputB, currentSignals.aluOp);
        
        // Handle branches
        if (currentSignals.branch) {
            stats.incrementBranches();
            boolean takeBranch = alu.compare(rs1Value, rs2Value, currentSignals.branchCond);
            if (takeBranch) {
                pc = pc + immediate;
            } else {
                pc = pc + 4;
            }
            stats.incrementInstructions();
            currentStage = Stage.FETCH;
            return;
        }
        
        // Handle jumps
        if (currentSignals.jump) {
            if (currentInstruction.getOpcode() == 0b1101111) {
                // JAL
                aluResult = pc + 4;  // Return address
                pc = pc + immediate;
            } else {
                // JALR
                int target = (rs1Value + immediate) & ~1;
                aluResult = pc + 4;  // Return address
                pc = target;
            }
            
            if (currentSignals.regWrite) {
                currentStage = Stage.WRITEBACK;
            } else {
                stats.incrementInstructions();
                currentStage = Stage.FETCH;
            }
            return;
        }
        
        // Go to memory stage if needed, otherwise writeback or fetch
        if (currentSignals.memRead || currentSignals.memWrite) {
            currentStage = Stage.MEMORY;
        } else if (currentSignals.regWrite) {
            pc = pc + 4;
            currentStage = Stage.WRITEBACK;
        } else {
            pc = pc + 4;
            stats.incrementInstructions();
            currentStage = Stage.FETCH;
        }
    }
    
    /**
     * Memory stage: Access memory for loads/stores.
     */
    private void executeMemory() {
        if (currentSignals.memRead) {
            memoryData = memory.read(aluResult);
            stats.incrementMemoryAccesses();
        }
        
        if (currentSignals.memWrite) {
            memory.write(aluResult, rs2Value);
            stats.incrementMemoryAccesses();
        }
        
        pc = pc + 4;
        
        if (currentSignals.regWrite) {
            currentStage = Stage.WRITEBACK;
        } else {
            stats.incrementInstructions();
            currentStage = Stage.FETCH;
        }
    }
    
    /**
     * Write-back stage: Write result to register file.
     */
    private void executeWriteBack() {
        int writeData;
        if (currentSignals.memToReg) {
            writeData = memoryData;
        } else if (currentSignals.link) {
            writeData = aluResult;  // Return address for JAL/JALR
        } else {
            writeData = aluResult;
        }
        
        registers.write(currentInstruction.getRd(), writeData);
        
        stats.incrementInstructions();
        currentStage = Stage.FETCH;
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
        currentStage = Stage.FETCH;
        currentInstruction = null;
        currentSignals = null;
        rs1Value = 0;
        rs2Value = 0;
        aluResult = 0;
        memoryData = 0;
        registers.reset();
        stats.reset();
    }
    
    @Override
    public ProcessorStats getStats() {
        return stats;
    }
    
    /**
     * Get the current pipeline stage (for debugging/visualization).
     */
    public Stage getCurrentStage() {
        return currentStage;
    }
    
    @Override
    public String toString() {
        return String.format("MultiCycleProcessor{pc=0x%08X, stage=%s, halted=%b}", 
            pc, currentStage, halted);
    }
}

