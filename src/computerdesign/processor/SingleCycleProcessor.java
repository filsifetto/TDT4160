package computerdesign.processor;

import computerdesign.alu.ALU;
import computerdesign.control.ControlUnit;
import computerdesign.control.ControlUnit.ControlSignals;
import computerdesign.instruction.Instruction;
import computerdesign.memory.MainMemory;
import computerdesign.memory.RegisterFile;

/**
 * SingleCycleProcessor - executes one complete instruction per clock cycle.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE SIMPLEST PROCESSOR DESIGN
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * In a single-cycle processor, each instruction completes in exactly ONE cycle.
 * The entire datapath is used once per instruction, from fetch to writeback.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * SINGLE-CYCLE DATAPATH
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *                      ┌──────────────────────────────────────────────────────┐
 *                      │                                                      │
 *      ┌───┐   +4      │    ┌─────────┐                                      │
 *   ┌──│PC │◄──────────┼────│   MUX   │◄─ branch/jump target                 │
 *   │  └─┬─┘           │    └─────────┘                                      │
 *   │    │             │         ▲                                           │
 *   │    ▼             │         │                                           │
 *   │ ┌──────┐         │    ┌────┴────┐                                      │
 *   │ │Instr │         │    │ Branch  │                                      │
 *   │ │Memory│         │    │  Logic  │                                      │
 *   │ └──┬───┘         │    └────┬────┘                                      │
 *   │    │             │         │                                           │
 *   │    ▼             │         │                                           │
 *   │ ┌──────┐    ┌────┴───┐ ┌───┴───┐  ┌────────┐   ┌────────┐             │
 *   │ │Decode├───►│Register├►│  ALU  ├─►│  Data  ├──►│  MUX   ├────────────┐│
 *   │ │      │    │  File  │ │       │  │ Memory │   │        │            ││
 *   │ └──────┘    └────────┘ └───────┘  └────────┘   └────────┘            ││
 *   │                  ▲                                  │                 ││
 *   │                  │                                  │                 ││
 *   │                  └──────────────────────────────────┼─────────────────┘│
 *   │                         (write back to rd)          │                  │
 *   └─────────────────────────────────────────────────────┘                  │
 *                                                                            │
 *   ════════════════════════════════════════════════════════════════════════│══
 *   Everything above happens in ONE clock cycle!                             │
 *   ════════════════════════════════════════════════════════════════════════│══
 *                                                                            │
 * ═══════════════════════════════════════════════════════════════════════════════
 * TIMING ANALYSIS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Clock period must accommodate the SLOWEST instruction (critical path):
 * 
 *   LOAD instruction (lw) is typically the slowest:
 *   
 *   PC → Instr Memory → Register Read → ALU → Data Memory → Register Write
 *   │         │              │           │          │              │
 *   └─ 50ps ──┴── 200ps ─────┴── 30ps ───┴── 50ps ──┴── 200ps ─────┴── 30ps
 *                                                                      │
 *                                              Total: ~560ps ◄─────────┘
 * 
 *   But an ADD instruction only needs:
 *   PC → Instr Memory → Register Read → ALU → Register Write = ~360ps
 *   
 *   The ADD must WAIT for the full 560ps cycle anyway! Wasted time.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * ADVANTAGES AND DISADVANTAGES
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * ADVANTAGES:
 *   ✓ Simple to understand and design
 *   ✓ CPI (Cycles Per Instruction) = exactly 1
 *   ✓ No hazards (each instruction completes before next starts)
 *   ✓ Easy to verify correctness
 * 
 * DISADVANTAGES:
 *   ✗ Long clock period (slowest instruction determines cycle time)
 *   ✗ Poor hardware utilization (ALU idle during memory access, etc.)
 *   ✗ Not practical for modern high-performance systems
 * 
 * This design is GREAT for learning, but real processors use pipelining!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @see MultiCycleProcessor - Better utilization, variable CPI
 * @see PipelineProcessor - Overlapped execution, high throughput
 */
public class SingleCycleProcessor implements Processor {
    
    // Processor components (STATE is held here)
    private int pc;                          // Program Counter
    private final RegisterFile registers;   // Register file
    private final MainMemory memory;        // Main memory (instruction + data)
    
    // Combinational components (NO state)
    private final ALU alu;
    private final ControlUnit control;
    
    // Processor state
    private boolean halted;
    private final ProcessorStats stats;
    
    /**
     * Create a single-cycle processor with given memory.
     */
    public SingleCycleProcessor(MainMemory memory) {
        this.memory = memory;
        this.registers = new RegisterFile();
        this.alu = new ALU();
        this.control = new ControlUnit();
        this.stats = new ProcessorStats();
        this.pc = 0;
        this.halted = false;
    }
    
    /**
     * Create a single-cycle processor with default 64KB memory.
     */
    public SingleCycleProcessor() {
        this(new MainMemory());
    }
    
    @Override
    public void cycle() {
        if (halted) return;
        
        // ===== FETCH =====
        Instruction inst = fetch();
        
        // Check for halt
        if (inst.isHalt()) {
            halted = true;
            return;
        }
        
        // ===== DECODE =====
        ControlSignals signals = control.decode(inst);
        
        // Read register values (two read ports)
        int rs1Val = registers.read(inst.getRs1());
        int rs2Val = registers.read(inst.getRs2());
        int immediate = inst.getImmediate();
        
        // ===== EXECUTE =====
        // Select ALU input B (register or immediate)
        int aluInputB = signals.aluSrcB ? immediate : rs2Val;
        
        // For AUIPC, use PC as input A
        int aluInputA = signals.auipc ? pc : rs1Val;
        
        // Perform ALU operation
        ALU.Result aluResult = alu.execute(aluInputA, aluInputB, signals.aluOp);
        
        // ===== MEMORY =====
        int memData = 0;
        if (signals.memRead) {
            memData = memory.read(aluResult.value);
            stats.incrementMemoryAccesses();
        }
        if (signals.memWrite) {
            memory.write(aluResult.value, rs2Val);
            stats.incrementMemoryAccesses();
        }
        
        // ===== WRITE BACK =====
        if (signals.regWrite) {
            int writeData;
            if (signals.memToReg) {
                writeData = memData;  // Load instruction
            } else if (signals.link) {
                writeData = pc + 4;   // JAL/JALR - save return address
            } else {
                writeData = aluResult.value;  // ALU result
            }
            registers.write(inst.getRd(), writeData);
        }
        
        // ===== UPDATE PC =====
        int nextPC = pc + 4;  // Default: next sequential instruction
        
        if (signals.branch) {
            // Conditional branch
            stats.incrementBranches();
            boolean takeBranch = alu.compare(rs1Val, rs2Val, signals.branchCond);
            if (takeBranch) {
                nextPC = pc + immediate;
            }
        } else if (signals.jump) {
            // Unconditional jump
            if (inst.getOpcode() == 0b1101111) {
                // JAL: PC-relative
                nextPC = pc + immediate;
            } else {
                // JALR: register + immediate
                nextPC = (rs1Val + immediate) & ~1;  // Clear LSB
            }
        }
        
        pc = nextPC;
        stats.incrementCycles();
        stats.incrementInstructions();
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
        registers.reset();
        stats.reset();
    }
    
    @Override
    public ProcessorStats getStats() {
        return stats;
    }
    
    @Override
    public String toString() {
        return String.format("SingleCycleProcessor{pc=0x%08X, halted=%b}", pc, halted);
    }
}

