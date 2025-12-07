package computerdesign.vm;

import computerdesign.memory.MainMemory;

import java.util.*;

/**
 * VirtualMachine - A complete, isolated execution environment.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * VIRTUALIZATION IN THE ABSTRACTION HIERARCHY
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   ┌─────────────────────────────────────────────────────────────────────────────┐
 *   │  APPLICATIONS (highest abstraction)                                        │
 *   │    • Word processor, web browser, games                                    │
 *   │    • Sees: Files, windows, network                                         │
 *   ├─────────────────────────────────────────────────────────────────────────────┤
 *   │  OPERATING SYSTEM                                                          │
 *   │    • Process management, memory management, I/O                            │
 *   │    • Sees: Processes, virtual memory, devices                              │
 *   ├─────────────────────────────────────────────────────────────────────────────┤
 *   │  ★ VIRTUAL MACHINE (THIS CLASS) ★                                          │
 *   │    • Creates illusion of complete hardware                                 │
 *   │    • Sees: Virtual CPU, virtual RAM, virtual devices                       │
 *   │    • Enables: Multiple OSes, isolation, migration                          │
 *   ├─────────────────────────────────────────────────────────────────────────────┤
 *   │  ISA / MACRO-ARCHITECTURE                                                  │
 *   │    • The instruction set (RISC-V, x86, ARM)                                │
 *   │    • The contract between software and hardware                            │
 *   ├─────────────────────────────────────────────────────────────────────────────┤
 *   │  MICRO-ARCHITECTURE                                                        │
 *   │    • Pipeline, cache, branch predictor                                     │
 *   │    • How the ISA is implemented                                            │
 *   ├─────────────────────────────────────────────────────────────────────────────┤
 *   │  DIGITAL ELECTRONICS                                                       │
 *   │    • Logic gates, flip-flops, registers                                    │
 *   │    • 0s and 1s, clock signals                                              │
 *   ├─────────────────────────────────────────────────────────────────────────────┤
 *   │  ANALOG ELECTRONICS (lowest abstraction)                                   │
 *   │    • Transistors, voltages, currents                                       │
 *   │    • Physics of semiconductor devices                                      │
 *   └─────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WHAT IS A VIRTUAL MACHINE?
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * A Virtual Machine is an EFFICIENT, ISOLATED DUPLICATE of a real machine.
 * 
 * PROPERTIES (Popek & Goldberg, 1974):
 *   1. EQUIVALENCE: Software runs identically to on bare metal
 *   2. EFFICIENCY: Most instructions execute directly on hardware
 *   3. RESOURCE CONTROL: Hypervisor controls all resources
 * 
 * KEY INSIGHT: VMs create the illusion of having dedicated hardware!
 * 
 *   ┌──────────────────────────────────────────────────────────────────────────┐
 *   │  Guest OS thinks it's running on real hardware...                       │
 *   │                                                                          │
 *   │     ┌─────────────┐    ┌─────────────┐    ┌─────────────┐               │
 *   │     │ Guest OS 1  │    │ Guest OS 2  │    │ Guest OS 3  │               │
 *   │     │  (Linux)    │    │  (Windows)  │    │  (FreeBSD)  │               │
 *   │     └──────┬──────┘    └──────┬──────┘    └──────┬──────┘               │
 *   │            │                  │                  │                       │
 *   │     ┌──────┴──────┐    ┌──────┴──────┐    ┌──────┴──────┐               │
 *   │     │   VM 1      │    │   VM 2      │    │   VM 3      │               │
 *   │     │(vCPU, vRAM) │    │(vCPU, vRAM) │    │(vCPU, vRAM) │               │
 *   │     └──────┬──────┘    └──────┬──────┘    └──────┬──────┘               │
 *   │            └──────────────────┼──────────────────┘                       │
 *   │                               │                                          │
 *   │                    ┌──────────┴──────────┐                               │
 *   │                    │     HYPERVISOR      │                               │
 *   │                    │  (Virtual Machine   │                               │
 *   │                    │      Monitor)       │                               │
 *   │                    └──────────┬──────────┘                               │
 *   │                               │                                          │
 *   │            ┌──────────────────┼──────────────────┐                       │
 *   │            │                  │                  │                       │
 *   │         ┌──┴───┐          ┌───┴───┐          ┌───┴───┐                   │
 *   │         │ CPU  │          │ RAM   │          │ I/O   │                   │
 *   │         └──────┘          └───────┘          └───────┘                   │
 *   │                       PHYSICAL HARDWARE                                  │
 *   └──────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * TYPES OF HYPERVISORS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * TYPE 1 (Bare-Metal): Runs directly on hardware
 *   ┌───────────────────┐
 *   │    Guest OS       │
 *   ├───────────────────┤
 *   │   Hypervisor      │  ← VMware ESXi, Xen, Microsoft Hyper-V
 *   ├───────────────────┤
 *   │    Hardware       │
 *   └───────────────────┘
 *   + Better performance
 *   + More control over hardware
 *   - Requires dedicated server
 * 
 * TYPE 2 (Hosted): Runs on a host operating system
 *   ┌───────────────────┐
 *   │    Guest OS       │
 *   ├───────────────────┤
 *   │   Hypervisor      │  ← VirtualBox, VMware Workstation, QEMU
 *   ├───────────────────┤
 *   │    Host OS        │
 *   ├───────────────────┤
 *   │    Hardware       │
 *   └───────────────────┘
 *   + Easier to set up
 *   + Can use alongside normal apps
 *   - More overhead
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * HOW VIRTUALIZATION WORKS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * TRAP-AND-EMULATE (Classic Virtualization):
 * 
 *   1. Guest OS runs in user mode (deprivileged)
 *   2. Privileged instructions trap to hypervisor
 *   3. Hypervisor emulates the instruction
 *   4. Return to guest OS
 * 
 *   Guest OS                    Hypervisor
 *      │                           │
 *      │ Privileged instruction    │
 *      │ (e.g., modify page table) │
 *      │ ════════════════════════► │ (trap)
 *      │                           │
 *      │                           │ Emulate instruction:
 *      │                           │   - Update shadow page table
 *      │                           │   - Maintain isolation
 *      │                           │
 *      │ ◄════════════════════════ │ (return)
 *      │                           │
 *      ▼                           ▼
 * 
 * HARDWARE VIRTUALIZATION SUPPORT (Intel VT-x / AMD-V):
 * 
 *   • Guest and host run in separate "worlds"
 *   • VM Entry: Switch from host to guest
 *   • VM Exit: Switch from guest to host (on sensitive operations)
 *   • Guest OS can run in ring 0 (kernel mode) within its world
 * 
 *   ┌────────────────────┐    ┌────────────────────┐
 *   │   Guest World      │    │   Host World       │
 *   │  ┌──────────────┐  │    │  ┌──────────────┐  │
 *   │  │ Ring 3 (User)│  │    │  │ Ring 3 (User)│  │
 *   │  ├──────────────┤  │    │  ├──────────────┤  │
 *   │  │Ring 0 (Guest)│  │    │  │Ring 0 (Host) │  │
 *   │  │    Kernel    │  │    │  │  Hypervisor  │  │
 *   │  └──────────────┘  │    │  └──────────────┘  │
 *   └─────────┬──────────┘    └─────────┬──────────┘
 *             │       VM Exit           │
 *             │ ═══════════════════════►│
 *             │                         │
 *             │ ◄═══════════════════════│
 *             │       VM Entry          │
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * MEMORY VIRTUALIZATION (Two-Level Paging)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Challenge: Guest OS manages its own page tables, but we need isolation!
 * 
 * SHADOW PAGE TABLES (Software approach):
 *   - Hypervisor maintains "shadow" of guest page tables
 *   - Guest virtual → Guest physical → Host physical
 *   - Hypervisor intercepts page table modifications
 * 
 * NESTED PAGING / EPT / NPT (Hardware approach):
 *   - Guest page table: Guest Virtual → Guest Physical
 *   - Host page table (EPT): Guest Physical → Host Physical
 *   - Hardware walks BOTH tables automatically
 *   - Much faster than shadow page tables!
 * 
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  Address Translation with Nested Paging:                               │
 *   │                                                                         │
 *   │    Guest Virtual Address                                                │
 *   │           │                                                             │
 *   │           ▼                                                             │
 *   │    ┌──────────────┐                                                     │
 *   │    │ Guest Page   │  (controlled by Guest OS)                           │
 *   │    │    Table     │                                                     │
 *   │    └──────┬───────┘                                                     │
 *   │           │                                                             │
 *   │           ▼                                                             │
 *   │    Guest Physical Address                                               │
 *   │           │                                                             │
 *   │           ▼                                                             │
 *   │    ┌──────────────┐                                                     │
 *   │    │ Extended     │  (controlled by Hypervisor)                         │
 *   │    │ Page Table   │                                                     │
 *   │    └──────┬───────┘                                                     │
 *   │           │                                                             │
 *   │           ▼                                                             │
 *   │    Host Physical Address                                                │
 *   │           │                                                             │
 *   │           ▼                                                             │
 *   │    ┌──────────────┐                                                     │
 *   │    │  Physical    │                                                     │
 *   │    │   Memory     │                                                     │
 *   │    └──────────────┘                                                     │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * I/O VIRTUALIZATION
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * FULL EMULATION:
 *   - Hypervisor emulates complete device
 *   - Maximum compatibility, poor performance
 * 
 * PARAVIRTUALIZATION (virtio):
 *   - Guest knows it's virtualized
 *   - Uses efficient VM-aware drivers
 *   - Better performance
 * 
 * DEVICE PASSTHROUGH (SR-IOV):
 *   - Guest directly accesses physical device
 *   - Near-native performance
 *   - Device can only be used by one VM
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * RISC-V HYPERVISOR EXTENSION (H-Extension)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * RISC-V adds a new privilege mode for virtualization:
 * 
 *   Mode   │ Level │ Purpose
 *   ───────┼───────┼────────────────────────────────
 *   U      │   0   │ User applications
 *   S      │   1   │ Supervisor (OS kernel)
 *   H      │  2*   │ Hypervisor (new!)
 *   M      │   3   │ Machine (firmware, bootloader)
 * 
 *   *H-mode adds VU and VS modes (Virtualized User/Supervisor)
 * 
 * Key features:
 *   - Two-level page tables (VS and G)
 *   - Separate interrupt handling for guests
 *   - Trap virtualization (fewer VM exits)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class VirtualMachine {
    
    /** Virtual machine states. */
    public enum VMState {
        CREATED,        // VM created but not started
        RUNNING,        // VM is executing
        PAUSED,         // VM execution suspended
        HALTED,         // VM stopped cleanly
        CRASHED         // VM stopped due to error
    }
    
    /**
     * Virtual CPU state - what the guest sees as its CPU.
     */
    public static class VirtualCPU {
        private int[] registers = new int[32];  // General purpose registers
        private int pc;                          // Program counter
        private int[] csrs = new int[4096];     // Control/Status Registers
        private PrivilegeLevel privilegeLevel;
        
        // Virtual machine control
        private boolean interruptsEnabled;
        private long instructionsExecuted;
        private long cycleCount;
        
        public enum PrivilegeLevel {
            VS_USER(0, "VS-User"),       // Virtualized Supervisor User mode
            VS_SUPERVISOR(1, "VS-Supervisor"),  // Virtualized Supervisor mode
            HS_MODE(2, "HS-Mode");       // Hypervisor-extended Supervisor mode
            
            public final int level;
            public final String name;
            
            PrivilegeLevel(int level, String name) {
                this.level = level;
                this.name = name;
            }
        }
        
        public VirtualCPU() {
            this.privilegeLevel = PrivilegeLevel.VS_SUPERVISOR;
            this.interruptsEnabled = true;
            this.pc = 0;  // Will be set by loadProgram
        }
        
        public void reset() {
            Arrays.fill(registers, 0);
            pc = 0;  // Will be set by loadProgram
            privilegeLevel = PrivilegeLevel.VS_SUPERVISOR;
            instructionsExecuted = 0;
            cycleCount = 0;
        }
        
        // Register access
        public int getRegister(int index) {
            if (index == 0) return 0;  // x0 is always 0
            return registers[index & 0x1F];
        }
        
        public void setRegister(int index, int value) {
            if (index != 0) {
                registers[index & 0x1F] = value;
            }
        }
        
        public int getPC() { return pc; }
        public void setPC(int pc) { this.pc = pc; }
        public void advancePC() { this.pc += 4; }
        
        public PrivilegeLevel getPrivilegeLevel() { return privilegeLevel; }
        public void setPrivilegeLevel(PrivilegeLevel level) { this.privilegeLevel = level; }
        
        public long getInstructionsExecuted() { return instructionsExecuted; }
        public void incrementInstructions() { instructionsExecuted++; }
        
        public long getCycleCount() { return cycleCount; }
        public void incrementCycles(int count) { cycleCount += count; }
        
        @Override
        public String toString() {
            return String.format("vCPU{pc=0x%08X, mode=%s, instrs=%d}",
                pc, privilegeLevel.name, instructionsExecuted);
        }
    }
    
    // VM identification
    private final int vmId;
    private final String name;
    private VMState state;
    
    // Virtual hardware
    private final VirtualCPU vcpu;
    private final int memorySize;  // Virtual memory size in bytes
    private int[] memory;          // Guest physical memory
    
    // Nested page table (Guest Physical → Host Physical translation)
    private final Map<Integer, Integer> nestedPageTable;
    private final int pageSize = 4096;
    
    // Reference to host physical memory (managed by hypervisor)
    private MainMemory hostMemory;
    private int baseHostFrame;  // Starting frame in host memory
    
    // I/O virtualization
    private final Map<Integer, VirtualDevice> devices;
    
    // VM exit reasons
    public enum VMExitReason {
        PRIVILEGED_INSTRUCTION,
        PAGE_FAULT,
        EXTERNAL_INTERRUPT,
        HYPERCALL,
        IO_ACCESS,
        HALT,
        EXCEPTION
    }
    
    // Statistics
    private long vmExits;
    private long vmEntries;
    private Map<VMExitReason, Long> exitReasons;
    
    /**
     * Create a new virtual machine.
     * @param vmId Unique VM identifier
     * @param name Human-readable name
     * @param memorySize Virtual memory size in bytes
     */
    public VirtualMachine(int vmId, String name, int memorySize) {
        this.vmId = vmId;
        this.name = name;
        this.memorySize = memorySize;
        this.state = VMState.CREATED;
        
        this.vcpu = new VirtualCPU();
        this.memory = new int[memorySize / 4];  // Word-addressable
        
        this.nestedPageTable = new HashMap<>();
        this.devices = new HashMap<>();
        
        this.exitReasons = new EnumMap<>(VMExitReason.class);
        for (VMExitReason reason : VMExitReason.values()) {
            exitReasons.put(reason, 0L);
        }
    }
    
    /**
     * Attach VM to host physical memory via the hypervisor.
     */
    public void attachToHost(MainMemory hostMemory, int baseFrame) {
        this.hostMemory = hostMemory;
        this.baseHostFrame = baseFrame;
        
        // Set up initial nested page table mappings
        int numFrames = memorySize / pageSize;
        for (int i = 0; i < numFrames; i++) {
            // Guest physical frame i → Host physical frame (baseFrame + i)
            nestedPageTable.put(i, baseHostFrame + i);
        }
    }
    
    /**
     * Start the virtual machine.
     */
    public void start() {
        if (state == VMState.CREATED || state == VMState.HALTED) {
            state = VMState.RUNNING;
            // Don't reset vCPU - preserve PC set by loadProgram
            vmEntries++;
        }
    }
    
    /**
     * Pause the virtual machine.
     */
    public void pause() {
        if (state == VMState.RUNNING) {
            state = VMState.PAUSED;
            vmExits++;
            recordExit(VMExitReason.EXTERNAL_INTERRUPT);
        }
    }
    
    /**
     * Resume the virtual machine.
     */
    public void resume() {
        if (state == VMState.PAUSED) {
            state = VMState.RUNNING;
            vmEntries++;
        }
    }
    
    /**
     * Halt the virtual machine.
     */
    public void halt() {
        state = VMState.HALTED;
        vmExits++;
        recordExit(VMExitReason.HALT);
    }
    
    /**
     * Execute one instruction in the VM.
     * Returns the exit reason if a VM exit occurred, null otherwise.
     */
    public VMExitReason step() {
        if (state != VMState.RUNNING) {
            return null;
        }
        
        vcpu.incrementCycles(1);
        
        // Fetch instruction from guest physical memory
        int pc = vcpu.getPC();
        int guestPhysPage = pc / pageSize;
        int pageOffset = pc % pageSize;
        
        // Translate through nested page table
        Integer hostFrame = nestedPageTable.get(guestPhysPage);
        if (hostFrame == null) {
            // Nested page fault - VM exit
            vmExits++;
            recordExit(VMExitReason.PAGE_FAULT);
            return VMExitReason.PAGE_FAULT;
        }
        
        // Read instruction from guest memory
        int instruction = readGuestMemory(pc);
        
        // Decode and check if privileged
        if (isPrivilegedInstruction(instruction)) {
            // Trap to hypervisor
            vmExits++;
            recordExit(VMExitReason.PRIVILEGED_INSTRUCTION);
            return VMExitReason.PRIVILEGED_INSTRUCTION;
        }
        
        // Check for hypercall (ECALL with specific parameters)
        if (isHypercall(instruction)) {
            vmExits++;
            recordExit(VMExitReason.HYPERCALL);
            return VMExitReason.HYPERCALL;
        }
        
        // Execute instruction (simplified)
        executeInstruction(instruction);
        vcpu.incrementInstructions();
        
        return null;
    }
    
    /**
     * Execute multiple instructions.
     */
    public VMExitReason run(int maxInstructions) {
        VMExitReason exitReason = null;
        
        for (int i = 0; i < maxInstructions && state == VMState.RUNNING; i++) {
            exitReason = step();
            if (exitReason != null) {
                break;
            }
        }
        
        return exitReason;
    }
    
    /**
     * Check if instruction is privileged (requires hypervisor emulation).
     */
    private boolean isPrivilegedInstruction(int instruction) {
        int opcode = instruction & 0x7F;
        
        // SYSTEM instructions (CSR access, etc.)
        if (opcode == 0b1110011) {
            int funct3 = (instruction >> 12) & 0x7;
            // CSR instructions
            if (funct3 >= 1 && funct3 <= 3) {
                int csr = (instruction >> 20) & 0xFFF;
                // Check if CSR is hypervisor-controlled
                return isHypervisorCSR(csr);
            }
            // MRET, SRET
            if (funct3 == 0) {
                int funct7 = (instruction >> 25) & 0x7F;
                return funct7 == 0b0001000 || funct7 == 0b0011000;
            }
        }
        
        // SFENCE.VMA (virtual memory management)
        if (opcode == 0b1110011) {
            int funct7 = (instruction >> 25) & 0x7F;
            if (funct7 == 0b0001001) {
                return true;  // SFENCE.VMA
            }
        }
        
        return false;
    }
    
    /**
     * Check if CSR requires hypervisor handling.
     */
    private boolean isHypervisorCSR(int csr) {
        // CSRs in the 0x000-0x0FF range are user-level
        // CSRs in the 0x100-0x1FF range are supervisor-level
        // CSRs in the 0x200-0x2FF range are hypervisor-level (H-extension)
        // CSRs in the 0x300-0x3FF range are machine-level
        return (csr >= 0x100);  // All supervisor and above need trapping
    }
    
    /**
     * Check if instruction is a hypercall (guest-to-hypervisor call).
     */
    private boolean isHypercall(int instruction) {
        // ECALL instruction
        return instruction == 0x00000073;
    }
    
    /**
     * Execute a simple instruction (simplified emulation).
     */
    private void executeInstruction(int instruction) {
        int opcode = instruction & 0x7F;
        
        switch (opcode) {
            case 0b0110011:  // R-type (ADD, SUB, etc.)
                executeRType(instruction);
                vcpu.advancePC();
                break;
                
            case 0b0010011:  // I-type (ADDI, etc.)
                executeIType(instruction);
                vcpu.advancePC();
                break;
                
            case 0b0000011:  // Load
                executeLoad(instruction);
                vcpu.advancePC();
                break;
                
            case 0b0100011:  // Store
                executeStore(instruction);
                vcpu.advancePC();
                break;
                
            case 0b1100011:  // Branch
                executeBranch(instruction);
                break;
                
            case 0b1101111:  // JAL
                executeJAL(instruction);
                break;
                
            case 0b1100111:  // JALR
                executeJALR(instruction);
                break;
                
            default:
                // Unknown instruction - just advance PC
                vcpu.advancePC();
        }
    }
    
    // Simplified instruction execution helpers
    private void executeRType(int instr) {
        int rd = (instr >> 7) & 0x1F;
        int rs1 = (instr >> 15) & 0x1F;
        int rs2 = (instr >> 20) & 0x1F;
        int funct3 = (instr >> 12) & 0x7;
        int funct7 = (instr >> 25) & 0x7F;
        
        int a = vcpu.getRegister(rs1);
        int b = vcpu.getRegister(rs2);
        int result = 0;
        
        if (funct7 == 0) {
            switch (funct3) {
                case 0: result = a + b; break;  // ADD
                case 4: result = a ^ b; break;  // XOR
                case 6: result = a | b; break;  // OR
                case 7: result = a & b; break;  // AND
            }
        } else if (funct7 == 0b0100000) {
            if (funct3 == 0) result = a - b;  // SUB
        }
        
        vcpu.setRegister(rd, result);
    }
    
    private void executeIType(int instr) {
        int rd = (instr >> 7) & 0x1F;
        int rs1 = (instr >> 15) & 0x1F;
        int imm = (instr >> 20);  // Sign-extended
        if ((imm & 0x800) != 0) imm |= 0xFFFFF000;
        int funct3 = (instr >> 12) & 0x7;
        
        int a = vcpu.getRegister(rs1);
        int result = 0;
        
        switch (funct3) {
            case 0: result = a + imm; break;  // ADDI
            case 4: result = a ^ imm; break;  // XORI
            case 6: result = a | imm; break;  // ORI
            case 7: result = a & imm; break;  // ANDI
        }
        
        vcpu.setRegister(rd, result);
    }
    
    private void executeLoad(int instr) {
        int rd = (instr >> 7) & 0x1F;
        int rs1 = (instr >> 15) & 0x1F;
        int imm = (instr >> 20);
        if ((imm & 0x800) != 0) imm |= 0xFFFFF000;
        
        int addr = vcpu.getRegister(rs1) + imm;
        int value = readGuestMemory(addr);
        vcpu.setRegister(rd, value);
    }
    
    private void executeStore(int instr) {
        int rs1 = (instr >> 15) & 0x1F;
        int rs2 = (instr >> 20) & 0x1F;
        int imm = ((instr >> 7) & 0x1F) | (((instr >> 25) & 0x7F) << 5);
        if ((imm & 0x800) != 0) imm |= 0xFFFFF000;
        
        int addr = vcpu.getRegister(rs1) + imm;
        int value = vcpu.getRegister(rs2);
        writeGuestMemory(addr, value);
    }
    
    private void executeBranch(int instr) {
        int rs1 = (instr >> 15) & 0x1F;
        int rs2 = (instr >> 20) & 0x1F;
        int funct3 = (instr >> 12) & 0x7;
        
        int a = vcpu.getRegister(rs1);
        int b = vcpu.getRegister(rs2);
        boolean taken = false;
        
        switch (funct3) {
            case 0: taken = (a == b); break;  // BEQ
            case 1: taken = (a != b); break;  // BNE
            case 4: taken = (a < b); break;   // BLT
            case 5: taken = (a >= b); break;  // BGE
        }
        
        if (taken) {
            // Calculate branch offset (simplified)
            int imm = ((instr >> 31) << 12) | (((instr >> 7) & 1) << 11) |
                     (((instr >> 25) & 0x3F) << 5) | (((instr >> 8) & 0xF) << 1);
            if ((imm & 0x1000) != 0) imm |= 0xFFFFE000;
            vcpu.setPC(vcpu.getPC() + imm);
        } else {
            vcpu.advancePC();
        }
    }
    
    private void executeJAL(int instr) {
        int rd = (instr >> 7) & 0x1F;
        vcpu.setRegister(rd, vcpu.getPC() + 4);
        
        // Calculate jump offset (simplified)
        int imm = ((instr >> 31) << 20) | (((instr >> 12) & 0xFF) << 12) |
                 (((instr >> 20) & 1) << 11) | (((instr >> 21) & 0x3FF) << 1);
        if ((imm & 0x100000) != 0) imm |= 0xFFE00000;
        
        vcpu.setPC(vcpu.getPC() + imm);
    }
    
    private void executeJALR(int instr) {
        int rd = (instr >> 7) & 0x1F;
        int rs1 = (instr >> 15) & 0x1F;
        int imm = (instr >> 20);
        if ((imm & 0x800) != 0) imm |= 0xFFFFF000;
        
        int returnAddr = vcpu.getPC() + 4;
        int target = (vcpu.getRegister(rs1) + imm) & ~1;
        
        vcpu.setRegister(rd, returnAddr);
        vcpu.setPC(target);
    }
    
    /**
     * Read from guest physical memory.
     */
    public int readGuestMemory(int guestPhysAddr) {
        int wordIndex = (guestPhysAddr & (memorySize - 1)) / 4;
        if (wordIndex < memory.length) {
            return memory[wordIndex];
        }
        return 0;
    }
    
    /**
     * Write to guest physical memory.
     */
    public void writeGuestMemory(int guestPhysAddr, int value) {
        int wordIndex = (guestPhysAddr & (memorySize - 1)) / 4;
        if (wordIndex < memory.length) {
            memory[wordIndex] = value;
        }
    }
    
    /**
     * Load a program into guest memory.
     */
    public void loadProgram(int[] program, int guestPhysAddr) {
        for (int i = 0; i < program.length; i++) {
            writeGuestMemory(guestPhysAddr + i * 4, program[i]);
        }
        vcpu.setPC(guestPhysAddr);
    }
    
    /**
     * Record a VM exit reason.
     */
    private void recordExit(VMExitReason reason) {
        exitReasons.put(reason, exitReasons.get(reason) + 1);
    }
    
    /**
     * Register a virtual device.
     */
    public void registerDevice(int portBase, VirtualDevice device) {
        devices.put(portBase, device);
    }
    
    // Getters
    public int getVmId() { return vmId; }
    public String getName() { return name; }
    public VMState getState() { return state; }
    public VirtualCPU getVCPU() { return vcpu; }
    public int getMemorySize() { return memorySize; }
    public long getVMExits() { return vmExits; }
    public long getVMEntries() { return vmEntries; }
    public Map<VMExitReason, Long> getExitReasons() { return new HashMap<>(exitReasons); }
    
    /**
     * Get VM statistics.
     */
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Virtual Machine: %s (ID: %d)\n", name, vmId));
        sb.append(String.format("  State: %s\n", state));
        sb.append(String.format("  Memory: %d KB\n", memorySize / 1024));
        sb.append(String.format("  vCPU: %s\n", vcpu));
        sb.append(String.format("  VM Entries: %d, VM Exits: %d\n", vmEntries, vmExits));
        
        if (vmExits > 0) {
            sb.append("  Exit Reasons:\n");
            for (var entry : exitReasons.entrySet()) {
                if (entry.getValue() > 0) {
                    sb.append(String.format("    %s: %d (%.1f%%)\n",
                        entry.getKey(), entry.getValue(),
                        entry.getValue() * 100.0 / vmExits));
                }
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("VM{id=%d, name='%s', state=%s, mem=%dKB}",
            vmId, name, state, memorySize / 1024);
    }
    
    /**
     * Interface for virtual devices.
     */
    public interface VirtualDevice {
        int read(int offset);
        void write(int offset, int value);
        String getName();
    }
    
    /**
     * Simple virtual console device.
     */
    public static class VirtualConsole implements VirtualDevice {
        private StringBuilder output = new StringBuilder();
        
        @Override
        public int read(int offset) {
            return 0;  // No input support
        }
        
        @Override
        public void write(int offset, int value) {
            if (offset == 0) {
                output.append((char) value);
            }
        }
        
        @Override
        public String getName() {
            return "Virtual Console";
        }
        
        public String getOutput() {
            return output.toString();
        }
    }
}

