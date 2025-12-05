package computerdesign.processor;

/**
 * ExceptionHandler - Managing exceptions and interrupts in a processor.
 * 
 * Covers learning goals: T5.2 (Exceptions and Interrupts)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * EXCEPTIONS vs INTERRUPTS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * EXCEPTION (Synchronous):
 *   - Caused by the currently executing instruction
 *   - Predictable, repeatable
 *   - Examples:
 *     - Undefined instruction
 *     - Arithmetic overflow
 *     - Page fault
 *     - System call (ECALL)
 *     - Breakpoint (EBREAK)
 * 
 * INTERRUPT (Asynchronous):
 *   - Caused by external events, NOT the current instruction
 *   - Can happen at any time
 *   - Examples:
 *     - Timer interrupt
 *     - I/O device ready
 *     - Inter-processor interrupt
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * PRECISE EXCEPTIONS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * A PRECISE exception maintains these properties:
 *   1. All instructions BEFORE the faulting instruction have completed
 *   2. The faulting instruction and all AFTER it have NO effect
 *   3. The state is as if instructions executed in-order up to the fault
 * 
 * WHY PRECISE EXCEPTIONS MATTER:
 *   - OS can restart the instruction after fixing the problem (e.g., page fault)
 *   - Debuggers can show consistent state
 *   - Easier to understand and debug
 * 
 * CHALLENGE IN PIPELINED PROCESSORS:
 *   - Multiple instructions in flight simultaneously
 *   - Later instruction may complete before earlier one faults
 *   - Must track and revert partial state changes
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * EXCEPTION HANDLING IN 5-STAGE PIPELINE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐
 *   │  IF     │ → │  ID     │ → │  EX     │ → │  MEM    │ → │  WB     │
 *   └────┬────┘   └────┬────┘   └────┬────┘   └────┬────┘   └────┬────┘
 *        │             │             │             │             │
 *   Page fault   Undefined    Overflow    Page fault     (rare)
 *   (I-cache)    instruction  Breakpoint  (D-cache)
 * 
 * EXCEPTIONS CAN OCCUR AT ANY STAGE!
 * 
 * PROBLEM: IF fetches instruction at cycle 1, EX overflows at cycle 3
 *          But instruction at cycle 2 is BETWEEN them in the pipeline!
 * 
 * SOLUTION: Exception status flows through pipeline with instruction
 *           Only commit at WB stage, only handle oldest exception
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * EXCEPTION HANDLING STEPS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 1. DETECT: Exception condition occurs (hardware or software check)
 * 
 * 2. FLUSH: Stop all newer instructions in pipeline
 *    - Convert to NOPs (pipeline bubbles)
 *    - Prevent any writes to registers or memory
 * 
 * 3. SAVE STATE: Record information about the exception
 *    - EPC (Exception PC): Address of faulting instruction
 *    - CAUSE: What type of exception
 *    - STATUS: Previous privilege level, interrupt enable, etc.
 * 
 * 4. TRANSFER CONTROL: Jump to exception handler
 *    - Set PC to handler address (trap vector)
 *    - Enter supervisor/machine mode
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * RISC-V EXCEPTION REGISTERS (CSRs)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * ┌────────────┬────────────────────────────────────────────────────────────────┐
 * │ Register   │ Purpose                                                        │
 * ├────────────┼────────────────────────────────────────────────────────────────┤
 * │ mstatus    │ Machine status (interrupt enable, previous mode)               │
 * │ mepc       │ Machine Exception PC (where to return)                         │
 * │ mcause     │ Exception cause code                                           │
 * │ mtval      │ Exception-specific value (bad address, illegal instr)          │
 * │ mtvec      │ Machine Trap Vector (handler address)                          │
 * │ mie/mip    │ Interrupt enable/pending bits                                  │
 * ├────────────┼────────────────────────────────────────────────────────────────┤
 * │ sstatus    │ Supervisor status (similar, for OS)                            │
 * │ sepc       │ Supervisor Exception PC                                        │
 * │ scause     │ Supervisor cause code                                          │
 * │ stval      │ Supervisor trap value                                          │
 * │ stvec      │ Supervisor Trap Vector                                         │
 * └────────────┴────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * EXCEPTION CAUSE CODES (RISC-V)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Exceptions (MSB = 0):
 *   0: Instruction address misaligned
 *   1: Instruction access fault (page fault on fetch)
 *   2: Illegal instruction
 *   3: Breakpoint (EBREAK)
 *   4: Load address misaligned
 *   5: Load access fault
 *   6: Store address misaligned
 *   7: Store access fault
 *   8: ECALL from U-mode (system call)
 *   9: ECALL from S-mode
 *  11: ECALL from M-mode
 *  12: Instruction page fault
 *  13: Load page fault
 *  15: Store page fault
 * 
 * Interrupts (MSB = 1):
 *   1: Supervisor software interrupt
 *   3: Machine software interrupt
 *   5: Supervisor timer interrupt
 *   7: Machine timer interrupt
 *   9: Supervisor external interrupt
 *  11: Machine external interrupt
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * OUT-OF-ORDER EXECUTION AND EXCEPTIONS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Modern processors execute instructions out of order for performance.
 * But exceptions must appear IN-ORDER (precise exceptions)!
 * 
 * SOLUTION: REORDER BUFFER (ROB)
 * 
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                        REORDER BUFFER                                  │
 *   │  ┌───────┬───────┬───────┬───────┬───────┬───────┬───────┬───────┐    │
 *   │  │ Instr │ Done? │Result │ Excep │ Instr │ Done? │Result │ Excep │    │
 *   │  │  I1   │   ✓   │  42   │  NO   │  I2   │   ✓   │  100  │  YES  │...│
 *   │  └───────┴───────┴───────┴───────┴───────┴───────┴───────┴───────┘    │
 *   │     ↑                             ↑                               ↑    │
 *   │    COMMIT                        I2 has exception!            ISSUE    │
 *   │    (oldest)                      Don't commit until I1 done   (newest) │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * ROB Rules:
 *   1. Instructions enter ROB in program order
 *   2. Instructions may COMPLETE (calculate result) out of order
 *   3. Instructions COMMIT (become visible) in order
 *   4. On exception: Flush ROB from exception point to newest
 *   5. Exception appears precise because only committed instructions are visible
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * VIRTUAL MACHINES AND EXCEPTIONS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * VIRTUAL MACHINE: Software that creates illusion of complete hardware
 * 
 * Type 1 (Bare-metal): Hypervisor runs directly on hardware
 *   Examples: VMware ESXi, Xen
 * 
 * Type 2 (Hosted): Hypervisor runs on a host OS
 *   Examples: VirtualBox, VMware Workstation
 * 
 * HOW VMs USE EXCEPTIONS:
 *   - Privileged instructions trap to hypervisor
 *   - Hypervisor emulates the instruction for guest OS
 *   - Guest OS thinks it's running on real hardware
 * 
 * RISC-V HYPERVISOR EXTENSION:
 *   - H-mode between M-mode and S-mode
 *   - Separate page tables for hypervisor and guest
 *   - Nested virtualization support
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class ExceptionHandler {
    
    /** Exception types in RISC-V. */
    public enum ExceptionType {
        // Synchronous exceptions (caused by instruction)
        INSTRUCTION_MISALIGNED(0, "Instruction address misaligned"),
        INSTRUCTION_ACCESS_FAULT(1, "Instruction access fault"),
        ILLEGAL_INSTRUCTION(2, "Illegal instruction"),
        BREAKPOINT(3, "Breakpoint (EBREAK)"),
        LOAD_MISALIGNED(4, "Load address misaligned"),
        LOAD_ACCESS_FAULT(5, "Load access fault"),
        STORE_MISALIGNED(6, "Store address misaligned"),
        STORE_ACCESS_FAULT(7, "Store access fault"),
        ECALL_U_MODE(8, "Environment call from U-mode"),
        ECALL_S_MODE(9, "Environment call from S-mode"),
        ECALL_M_MODE(11, "Environment call from M-mode"),
        INSTRUCTION_PAGE_FAULT(12, "Instruction page fault"),
        LOAD_PAGE_FAULT(13, "Load page fault"),
        STORE_PAGE_FAULT(15, "Store page fault"),
        
        // Asynchronous interrupts (external events)
        SUPERVISOR_SOFTWARE_INT(0x80000001L, "Supervisor software interrupt"),
        MACHINE_SOFTWARE_INT(0x80000003L, "Machine software interrupt"),
        SUPERVISOR_TIMER_INT(0x80000005L, "Supervisor timer interrupt"),
        MACHINE_TIMER_INT(0x80000007L, "Machine timer interrupt"),
        SUPERVISOR_EXTERNAL_INT(0x80000009L, "Supervisor external interrupt"),
        MACHINE_EXTERNAL_INT(0x8000000BL, "Machine external interrupt");
        
        public final long code;
        public final String description;
        
        ExceptionType(long code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public boolean isInterrupt() {
            return (code & 0x80000000L) != 0;
        }
    }
    
    /** Privilege modes in RISC-V. */
    public enum PrivilegeMode {
        USER(0, "U"),
        SUPERVISOR(1, "S"),
        MACHINE(3, "M");
        
        public final int level;
        public final String name;
        
        PrivilegeMode(int level, String name) {
            this.level = level;
            this.name = name;
        }
    }
    
    // Control and Status Registers (CSRs)
    private int mstatus;   // Machine status
    private int mepc;      // Machine exception PC
    private int mcause;    // Machine cause
    private int mtval;     // Machine trap value
    private int mtvec;     // Machine trap vector
    private int mie;       // Machine interrupt enable
    private int mip;       // Machine interrupt pending
    
    private PrivilegeMode currentMode = PrivilegeMode.MACHINE;
    private boolean globalInterruptEnable = true;
    
    // Statistics
    private int exceptionCount = 0;
    private int interruptCount = 0;
    
    /**
     * Create an exception handler.
     * 
     * @param trapVectorAddress Default address for trap handler
     */
    public ExceptionHandler(int trapVectorAddress) {
        this.mtvec = trapVectorAddress;
        this.mstatus = 0;
        this.mie = 0;
        this.mip = 0;
    }
    
    /**
     * Handle an exception.
     * 
     * @param type The type of exception
     * @param pc PC of the faulting instruction
     * @param trapValue Additional info (e.g., bad address, illegal instr encoding)
     * @return The address of the exception handler
     */
    public int handleException(ExceptionType type, int pc, int trapValue) {
        if (type.isInterrupt()) {
            interruptCount++;
        } else {
            exceptionCount++;
        }
        
        // Save exception state
        mepc = pc;
        mcause = (int) type.code;
        mtval = trapValue;
        
        // Save previous interrupt enable in mstatus
        // MIE (Machine Interrupt Enable) -> MPIE (previous)
        int prevMIE = (mstatus >> 3) & 1;
        mstatus = (mstatus & ~(1 << 7)) | (prevMIE << 7);  // MPIE = old MIE
        mstatus = mstatus & ~(1 << 3);  // Disable interrupts (MIE = 0)
        
        // Save previous privilege mode
        mstatus = (mstatus & ~(3 << 11)) | (currentMode.level << 11);  // MPP
        
        // Enter machine mode
        currentMode = PrivilegeMode.MACHINE;
        
        // Return handler address
        return getHandlerAddress(type);
    }
    
    /**
     * Get the handler address from mtvec.
     * Supports both direct and vectored modes.
     */
    private int getHandlerAddress(ExceptionType type) {
        int mode = mtvec & 3;
        int base = mtvec & ~3;
        
        if (mode == 0) {
            // Direct mode: all traps go to base address
            return base;
        } else {
            // Vectored mode: interrupts go to base + 4*cause
            if (type.isInterrupt()) {
                int cause = (int) (type.code & 0x7FFFFFFF);
                return base + 4 * cause;
            } else {
                return base;
            }
        }
    }
    
    /**
     * Return from exception (MRET instruction).
     * 
     * @return The PC to return to
     */
    public int returnFromException() {
        // Restore MIE from MPIE
        int mpie = (mstatus >> 7) & 1;
        mstatus = (mstatus & ~(1 << 3)) | (mpie << 3);  // MIE = MPIE
        mstatus = mstatus | (1 << 7);  // MPIE = 1
        
        // Restore privilege mode from MPP
        int mpp = (mstatus >> 11) & 3;
        currentMode = mpp == 0 ? PrivilegeMode.USER :
                      mpp == 1 ? PrivilegeMode.SUPERVISOR :
                                 PrivilegeMode.MACHINE;
        mstatus = mstatus & ~(3 << 11);  // MPP = 0 (U-mode)
        
        return mepc;
    }
    
    /**
     * Check if an interrupt is pending and enabled.
     */
    public boolean checkInterrupts() {
        if (!globalInterruptEnable) return false;
        if ((mstatus & (1 << 3)) == 0) return false;  // MIE disabled
        
        return (mie & mip) != 0;  // Any enabled and pending?
    }
    
    /**
     * Raise an interrupt.
     */
    public void raiseInterrupt(ExceptionType type) {
        if (type.isInterrupt()) {
            int bit = (int) (type.code & 0x7FFFFFFF);
            mip |= (1 << bit);
        }
    }
    
    /**
     * Clear an interrupt.
     */
    public void clearInterrupt(ExceptionType type) {
        if (type.isInterrupt()) {
            int bit = (int) (type.code & 0x7FFFFFFF);
            mip &= ~(1 << bit);
        }
    }
    
    /**
     * Enable/disable specific interrupts.
     */
    public void setInterruptEnable(ExceptionType type, boolean enable) {
        if (type.isInterrupt()) {
            int bit = (int) (type.code & 0x7FFFFFFF);
            if (enable) {
                mie |= (1 << bit);
            } else {
                mie &= ~(1 << bit);
            }
        }
    }
    
    // CSR accessors
    public int getMepc() { return mepc; }
    public int getMcause() { return mcause; }
    public int getMtval() { return mtval; }
    public int getMtvec() { return mtvec; }
    public int getMstatus() { return mstatus; }
    public PrivilegeMode getMode() { return currentMode; }
    
    public void setMepc(int value) { mepc = value; }
    public void setMtvec(int value) { mtvec = value; }
    
    // Statistics
    public int getExceptionCount() { return exceptionCount; }
    public int getInterruptCount() { return interruptCount; }
    
    /**
     * Demonstrate exception handling in pipeline.
     */
    public static String demonstratePipelineException() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  EXCEPTION HANDLING IN 5-STAGE PIPELINE\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("Cycle 1: I1 in IF, I2 in ID, I3 in EX*, I4 in MEM, I5 in WB\n");
        sb.append("         (* I3 causes exception in EX stage!)\n\n");
        
        sb.append("Step 1: DETECT\n");
        sb.append("  - ALU signals overflow on I3\n");
        sb.append("  - Exception bit set in EX/MEM register\n\n");
        
        sb.append("Step 2: FLUSH\n");
        sb.append("  - I1, I2 (newer than I3) → convert to NOPs\n");
        sb.append("  - Prevent any register/memory writes from I3\n\n");
        
        sb.append("Step 3: SAVE STATE\n");
        sb.append("  - mepc ← PC of I3 (faulting instruction)\n");
        sb.append("  - mcause ← overflow exception code\n");
        sb.append("  - Save mstatus (interrupt enable, privilege)\n\n");
        
        sb.append("Step 4: TRANSFER\n");
        sb.append("  - PC ← mtvec (trap handler address)\n");
        sb.append("  - Enter machine mode\n\n");
        
        sb.append("After exception:\n");
        sb.append("  - I4, I5 complete normally (older than I3)\n");
        sb.append("  - Handler runs, may fix problem and retry\n");
        sb.append("  - MRET returns to I3 (or next instruction)\n");
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("ExceptionHandler: mode=%s, mepc=0x%08X, mcause=0x%08X, " +
            "exceptions=%d, interrupts=%d",
            currentMode.name, mepc, mcause, exceptionCount, interruptCount);
    }
    
    public static void main(String[] args) {
        System.out.println(demonstratePipelineException());
        
        // Demo exception handling
        ExceptionHandler handler = new ExceptionHandler(0x80000000);
        
        System.out.println("\nSimulating page fault:");
        int handlerAddr = handler.handleException(
            ExceptionType.LOAD_PAGE_FAULT, 
            0x00010100,  // PC of faulting load
            0xDEADBEEF   // Bad address
        );
        
        System.out.printf("  Handler at: 0x%08X\n", handlerAddr);
        System.out.printf("  mepc: 0x%08X (return here after handling)\n", handler.getMepc());
        System.out.printf("  mtval: 0x%08X (the bad address)\n", handler.getMtval());
        
        // Simulate return from handler
        int returnPC = handler.returnFromException();
        System.out.printf("  Returning to: 0x%08X\n", returnPC);
    }
}

