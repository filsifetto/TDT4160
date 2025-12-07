package computerdesign.vm;

import computerdesign.memory.MainMemory;
import computerdesign.vm.VirtualMachine.VMState;
import computerdesign.vm.VirtualMachine.VMExitReason;

import java.util.*;

/**
 * Hypervisor (Virtual Machine Monitor) - Manages multiple virtual machines.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE HYPERVISOR'S ROLE
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * The hypervisor sits BETWEEN the virtual machines and the physical hardware.
 * It is responsible for:
 * 
 *   1. MULTIPLEXING: Sharing physical resources among VMs
 *   2. ISOLATION: Ensuring VMs cannot interfere with each other
 *   3. EMULATION: Handling privileged operations for guest OSes
 *   4. SCHEDULING: Deciding which VM runs when
 * 
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                     HYPERVISOR ARCHITECTURE                            │
 *   │                                                                         │
 *   │  ┌─────────────────────────────────────────────────────────────────┐   │
 *   │  │                    Guest Virtual Machines                       │   │
 *   │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │   │
 *   │  │  │   VM 1      │  │   VM 2      │  │   VM 3      │   ...        │   │
 *   │  │  │ ┌─────────┐ │  │ ┌─────────┐ │  │ ┌─────────┐ │              │   │
 *   │  │  │ │Guest OS │ │  │ │Guest OS │ │  │ │Guest OS │ │              │   │
 *   │  │  │ └─────────┘ │  │ └─────────┘ │  │ └─────────┘ │              │   │
 *   │  │  │ vCPU  vRAM  │  │ vCPU  vRAM  │  │ vCPU  vRAM  │              │   │
 *   │  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │   │
 *   │  └─────────┼────────────────┼────────────────┼─────────────────────┘   │
 *   │            │                │                │                         │
 *   │            └────────────────┼────────────────┘                         │
 *   │                             │                                          │
 *   │  ┌──────────────────────────┴──────────────────────────────────────┐   │
 *   │  │                      HYPERVISOR CORE                            │   │
 *   │  │                                                                  │   │
 *   │  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │   │
 *   │  │  │   VM Exit    │  │   Memory     │  │     VM       │           │   │
 *   │  │  │   Handler    │  │   Manager    │  │  Scheduler   │           │   │
 *   │  │  └──────────────┘  └──────────────┘  └──────────────┘           │   │
 *   │  │                                                                  │   │
 *   │  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │   │
 *   │  │  │ Instruction  │  │    I/O       │  │   Nested     │           │   │
 *   │  │  │  Emulator    │  │  Virtualization│  │ Page Tables │           │   │
 *   │  │  └──────────────┘  └──────────────┘  └──────────────┘           │   │
 *   │  └──────────────────────────┬──────────────────────────────────────┘   │
 *   │                             │                                          │
 *   │  ┌──────────────────────────┴──────────────────────────────────────┐   │
 *   │  │                    PHYSICAL HARDWARE                            │   │
 *   │  │     ┌─────┐      ┌─────┐      ┌─────┐      ┌─────┐              │   │
 *   │  │     │ CPU │      │ RAM │      │ Disk│      │ NIC │              │   │
 *   │  │     └─────┘      └─────┘      └─────┘      └─────┘              │   │
 *   │  └─────────────────────────────────────────────────────────────────┘   │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * VM ENTRY AND EXIT
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * The hypervisor switches between VMs using VM Entry and VM Exit:
 * 
 * VM ENTRY (Hypervisor → Guest):
 *   1. Load guest CPU state (registers, PC, CSRs)
 *   2. Switch to guest privilege mode
 *   3. Start executing guest code
 *   4. Hardware runs until exit condition
 * 
 * VM EXIT (Guest → Hypervisor):
 *   Triggered by:
 *   - Privileged instruction execution
 *   - Memory access requiring emulation
 *   - External interrupt
 *   - Explicit hypercall (ECALL)
 *   - Timer expiration (for scheduling)
 *   
 *   Steps:
 *   1. Hardware saves guest state
 *   2. Switch to hypervisor mode
 *   3. Hypervisor examines exit reason
 *   4. Handle the exit (emulate, forward, etc.)
 *   5. Optionally switch to another VM
 *   6. VM Entry to continue
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * MEMORY MANAGEMENT
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Physical Memory Allocation:
 *   - Hypervisor divides physical memory among VMs
 *   - Each VM sees contiguous "guest physical" memory
 *   - Nested page tables translate to real physical addresses
 * 
 *   Physical Memory Layout Example:
 *   
 *   ┌────────────────────────────────────────────────────────┐
 *   │ 0MB   │ Hypervisor │ VM1 Memory │ VM2 Memory │ VM3... │
 *   │       │   (16MB)   │   (64MB)   │   (128MB)  │        │
 *   └────────────────────────────────────────────────────────┘
 *   
 *   VM1's guest physical 0x0 → Host physical 0x1000000 (16MB)
 *   VM2's guest physical 0x0 → Host physical 0x5000000 (80MB)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * VM SCHEDULING
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * The hypervisor must decide which VM runs on which CPU:
 * 
 *   TIME-SLICING: Each VM gets a time quantum
 *     ┌───────┬───────┬───────┬───────┬───────┬───────┐
 *     │ VM1   │ VM2   │ VM3   │ VM1   │ VM2   │ VM3   │...
 *     └───────┴───────┴───────┴───────┴───────┴───────┘
 *                        Time →
 * 
 *   PRIORITY-BASED: Important VMs get more CPU time
 *   FAIR-SHARE: VMs get proportional CPU time based on allocation
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class Hypervisor {
    
    /** Hypervisor type. */
    public enum Type {
        TYPE_1_BARE_METAL("Type 1 (Bare-Metal)", "Runs directly on hardware"),
        TYPE_2_HOSTED("Type 2 (Hosted)", "Runs on a host OS");
        
        public final String name;
        public final String description;
        
        Type(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
    
    // Hypervisor configuration
    private final String name;
    private final Type type;
    
    // Physical resources
    private final MainMemory physicalMemory;
    private final int totalMemory;
    private int allocatedMemory;
    private int reservedFrames;  // Frames reserved for hypervisor
    
    // Virtual machines
    private final Map<Integer, VirtualMachine> vms;
    private int nextVmId;
    private VirtualMachine currentVM;
    
    // Scheduling
    private final Queue<VirtualMachine> runQueue;
    private int timeQuantum;  // In instructions
    private int remainingQuantum;
    
    // Statistics
    private long totalVMExits;
    private long totalVMEntries;
    private long totalInstructionsEmulated;
    private long contextSwitches;
    
    /**
     * Create a new hypervisor.
     * @param name Hypervisor name
     * @param type Type 1 or Type 2
     * @param memorySize Total physical memory available
     */
    public Hypervisor(String name, Type type, int memorySize) {
        this.name = name;
        this.type = type;
        this.physicalMemory = new MainMemory(memorySize);
        this.totalMemory = memorySize;
        this.allocatedMemory = 0;
        
        // Reserve first 16KB for hypervisor
        this.reservedFrames = 4;
        this.allocatedMemory = reservedFrames * physicalMemory.getFrameSize();
        
        this.vms = new HashMap<>();
        this.nextVmId = 1;
        this.runQueue = new LinkedList<>();
        
        this.timeQuantum = 1000;  // Default: 1000 instructions per time slice
        this.remainingQuantum = timeQuantum;
    }
    
    /**
     * Create a new virtual machine.
     * @param name VM name
     * @param memorySize VM memory size in bytes
     * @return The created VM, or null if not enough memory
     */
    public VirtualMachine createVM(String name, int memorySize) {
        // Check if we have enough memory
        if (allocatedMemory + memorySize > totalMemory) {
            System.err.println("Hypervisor: Not enough memory for VM " + name);
            return null;
        }
        
        // Allocate frames for VM
        int framesNeeded = memorySize / physicalMemory.getFrameSize();
        int baseFrame = allocatedMemory / physicalMemory.getFrameSize();
        
        // Check if we can allocate contiguous frames
        for (int i = 0; i < framesNeeded; i++) {
            if (physicalMemory.isFrameAllocated(baseFrame + i)) {
                System.err.println("Hypervisor: Memory fragmentation prevents VM creation");
                return null;
            }
        }
        
        // Allocate the frames
        for (int i = 0; i < framesNeeded; i++) {
            physicalMemory.allocateFrame();
        }
        allocatedMemory += memorySize;
        
        // Create VM
        int vmId = nextVmId++;
        VirtualMachine vm = new VirtualMachine(vmId, name, memorySize);
        vm.attachToHost(physicalMemory, baseFrame);
        
        vms.put(vmId, vm);
        
        return vm;
    }
    
    /**
     * Destroy a virtual machine.
     */
    public void destroyVM(int vmId) {
        VirtualMachine vm = vms.remove(vmId);
        if (vm != null) {
            vm.halt();
            runQueue.remove(vm);
            if (currentVM == vm) {
                currentVM = null;
            }
            // In a real system, we would free the physical frames
        }
    }
    
    /**
     * Start a virtual machine.
     */
    public void startVM(int vmId) {
        VirtualMachine vm = vms.get(vmId);
        if (vm != null) {
            vm.start();
            runQueue.offer(vm);
        }
    }
    
    /**
     * Pause a virtual machine.
     */
    public void pauseVM(int vmId) {
        VirtualMachine vm = vms.get(vmId);
        if (vm != null) {
            vm.pause();
            runQueue.remove(vm);
        }
    }
    
    /**
     * Resume a virtual machine.
     */
    public void resumeVM(int vmId) {
        VirtualMachine vm = vms.get(vmId);
        if (vm != null && vm.getState() == VMState.PAUSED) {
            vm.resume();
            runQueue.offer(vm);
        }
    }
    
    /**
     * Run the hypervisor for a number of cycles.
     * This schedules and executes VMs.
     */
    public void run(int totalCycles) {
        int cyclesRemaining = totalCycles;
        
        while (cyclesRemaining > 0 && !runQueue.isEmpty()) {
            // Schedule next VM if needed
            if (currentVM == null || remainingQuantum <= 0) {
                scheduleNextVM();
            }
            
            if (currentVM == null) {
                break;  // No VMs to run
            }
            
            // Execute VM
            totalVMEntries++;
            int cyclesToRun = Math.min(remainingQuantum, cyclesRemaining);
            VMExitReason exitReason = currentVM.run(cyclesToRun);
            
            // Handle VM exit
            if (exitReason != null) {
                totalVMExits++;
                handleVMExit(currentVM, exitReason);
            }
            
            // Update quantum
            long instructionsRun = currentVM.getVCPU().getInstructionsExecuted();
            remainingQuantum -= cyclesToRun;
            cyclesRemaining -= cyclesToRun;
            
            // Check for preemption
            if (remainingQuantum <= 0) {
                preemptCurrentVM();
            }
        }
    }
    
    /**
     * Schedule the next VM to run.
     */
    private void scheduleNextVM() {
        if (runQueue.isEmpty()) {
            currentVM = null;
            return;
        }
        
        VirtualMachine nextVM = runQueue.poll();
        
        if (currentVM != nextVM) {
            contextSwitches++;
        }
        
        currentVM = nextVM;
        remainingQuantum = timeQuantum;
    }
    
    /**
     * Preempt the current VM (time slice expired).
     */
    private void preemptCurrentVM() {
        if (currentVM != null && currentVM.getState() == VMState.RUNNING) {
            // Put VM back in run queue
            runQueue.offer(currentVM);
            currentVM = null;
        }
    }
    
    /**
     * Handle a VM exit.
     */
    private void handleVMExit(VirtualMachine vm, VMExitReason reason) {
        switch (reason) {
            case PRIVILEGED_INSTRUCTION:
                emulatePrivilegedInstruction(vm);
                break;
                
            case PAGE_FAULT:
                handleNestedPageFault(vm);
                break;
                
            case HYPERCALL:
                handleHypercall(vm);
                break;
                
            case IO_ACCESS:
                emulateIO(vm);
                break;
                
            case HALT:
                // VM has halted, remove from run queue
                runQueue.remove(vm);
                break;
                
            case EXTERNAL_INTERRUPT:
                // Handle interrupt (e.g., timer, device)
                break;
                
            case EXCEPTION:
                // Inject exception into guest
                break;
        }
    }
    
    /**
     * Emulate a privileged instruction.
     */
    private void emulatePrivilegedInstruction(VirtualMachine vm) {
        totalInstructionsEmulated++;
        
        // Read the faulting instruction
        int pc = vm.getVCPU().getPC();
        int instruction = vm.readGuestMemory(pc);
        
        // Decode and emulate based on instruction type
        int opcode = instruction & 0x7F;
        
        if (opcode == 0b1110011) {  // SYSTEM
            int funct3 = (instruction >> 12) & 0x7;
            
            if (funct3 == 0) {
                // MRET/SRET - return from trap
                // In a real hypervisor, this would restore guest state
                vm.getVCPU().advancePC();
            } else {
                // CSR instruction
                int csr = (instruction >> 20) & 0xFFF;
                int rd = (instruction >> 7) & 0x1F;
                int rs1 = (instruction >> 15) & 0x1F;
                
                // Emulate CSR access
                emulateCsrAccess(vm, funct3, csr, rd, rs1);
            }
        }
        
        // Resume VM
        if (vm.getState() == VMState.RUNNING || vm.getState() == VMState.PAUSED) {
            vm.resume();
        }
    }
    
    /**
     * Emulate CSR access.
     */
    private void emulateCsrAccess(VirtualMachine vm, int funct3, int csr, int rd, int rs1) {
        // Simplified CSR emulation
        // In a real hypervisor, this would maintain shadow CSRs for the guest
        vm.getVCPU().advancePC();
    }
    
    /**
     * Handle a nested page fault.
     */
    private void handleNestedPageFault(VirtualMachine vm) {
        // In a real hypervisor, this would:
        // 1. Determine the faulting guest physical address
        // 2. Allocate a host physical frame if needed
        // 3. Update the nested page table
        // 4. Resume the VM
        
        // For simulation, just advance PC
        vm.getVCPU().advancePC();
        vm.resume();
    }
    
    /**
     * Handle a hypercall (guest requesting hypervisor service).
     */
    private void handleHypercall(VirtualMachine vm) {
        VirtualMachine.VirtualCPU vcpu = vm.getVCPU();
        
        // Hypercall number in a0 (x10), arguments in a1-a6 (x11-x16)
        int hypercallNum = vcpu.getRegister(10);
        
        switch (hypercallNum) {
            case 0:  // Exit
                vm.halt();
                break;
                
            case 1:  // Print character (a1 = character)
                int ch = vcpu.getRegister(11);
                System.out.print((char) ch);
                break;
                
            case 2:  // Get time
                vcpu.setRegister(10, (int) System.currentTimeMillis());
                break;
                
            default:
                // Unknown hypercall
                vcpu.setRegister(10, -1);  // Return error
        }
        
        vcpu.advancePC();
        vm.resume();
    }
    
    /**
     * Emulate I/O access.
     */
    private void emulateIO(VirtualMachine vm) {
        // In a real hypervisor, this would emulate device access
        vm.getVCPU().advancePC();
        vm.resume();
    }
    
    // Getters
    public String getName() { return name; }
    public Type getType() { return type; }
    public MainMemory getPhysicalMemory() { return physicalMemory; }
    public int getTotalMemory() { return totalMemory; }
    public int getAllocatedMemory() { return allocatedMemory; }
    public int getFreeMemory() { return totalMemory - allocatedMemory; }
    public int getVMCount() { return vms.size(); }
    public VirtualMachine getVM(int vmId) { return vms.get(vmId); }
    public Collection<VirtualMachine> getVMs() { return vms.values(); }
    public int getTimeQuantum() { return timeQuantum; }
    public void setTimeQuantum(int quantum) { this.timeQuantum = quantum; }
    
    /**
     * Get hypervisor statistics.
     */
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Hypervisor: %s\n", name));
        sb.append(String.format("  Type: %s\n", type.name));
        sb.append(String.format("  Physical Memory: %d KB total, %d KB allocated, %d KB free\n",
            totalMemory / 1024, allocatedMemory / 1024, (totalMemory - allocatedMemory) / 1024));
        sb.append(String.format("  Virtual Machines: %d\n", vms.size()));
        sb.append(String.format("  VM Entries: %d, VM Exits: %d\n", totalVMEntries, totalVMExits));
        sb.append(String.format("  Context Switches: %d\n", contextSwitches));
        sb.append(String.format("  Instructions Emulated: %d\n", totalInstructionsEmulated));
        
        if (!vms.isEmpty()) {
            sb.append("\n  Virtual Machines:\n");
            for (VirtualMachine vm : vms.values()) {
                sb.append(String.format("    [%d] %s: %s, %d KB\n",
                    vm.getVmId(), vm.getName(), vm.getState(), vm.getMemorySize() / 1024));
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("Hypervisor{name='%s', type=%s, vms=%d}",
            name, type.name, vms.size());
    }
    
    /**
     * Demonstrate virtualization concepts.
     */
    public static String demonstrateVirtualization() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  VIRTUALIZATION CONCEPTS\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("WHAT IS VIRTUALIZATION?\n");
        sb.append("  Virtualization creates the illusion of dedicated hardware\n");
        sb.append("  for multiple operating systems running simultaneously.\n\n");
        
        sb.append("KEY BENEFITS:\n");
        sb.append("  1. ISOLATION: VMs are completely separated\n");
        sb.append("     - One VM crash doesn't affect others\n");
        sb.append("     - Security boundaries between workloads\n\n");
        
        sb.append("  2. CONSOLIDATION: Multiple servers → one physical machine\n");
        sb.append("     - Better hardware utilization\n");
        sb.append("     - Reduced power and cooling costs\n\n");
        
        sb.append("  3. FLEXIBILITY: Easy to create, clone, migrate VMs\n");
        sb.append("     - Live migration between hosts\n");
        sb.append("     - Snapshot and restore\n\n");
        
        sb.append("  4. LEGACY SUPPORT: Run old OS on new hardware\n");
        sb.append("     - Hardware independence\n\n");
        
        sb.append("VIRTUALIZATION TECHNIQUES:\n");
        sb.append("  ┌───────────────────┬─────────────────────────────────────┐\n");
        sb.append("  │ Technique         │ Description                         │\n");
        sb.append("  ├───────────────────┼─────────────────────────────────────┤\n");
        sb.append("  │ Full Emulation    │ All instructions interpreted        │\n");
        sb.append("  │                   │ (QEMU in full emulation mode)       │\n");
        sb.append("  ├───────────────────┼─────────────────────────────────────┤\n");
        sb.append("  │ Binary Translation│ Privileged code translated on-fly   │\n");
        sb.append("  │                   │ (VMware, early VirtualBox)          │\n");
        sb.append("  ├───────────────────┼─────────────────────────────────────┤\n");
        sb.append("  │ Paravirtualization│ Guest OS modified to use hypercalls │\n");
        sb.append("  │                   │ (Xen PV, virtio drivers)            │\n");
        sb.append("  ├───────────────────┼─────────────────────────────────────┤\n");
        sb.append("  │ Hardware-Assisted │ CPU has VM support (VT-x, AMD-V)    │\n");
        sb.append("  │                   │ (Modern VMware, KVM, Hyper-V)       │\n");
        sb.append("  └───────────────────┴─────────────────────────────────────┘\n\n");
        
        sb.append("VM EXIT HANDLING:\n");
        sb.append("  When a VM tries to do something privileged:\n\n");
        sb.append("  Guest OS                    Hypervisor\n");
        sb.append("     │                           │\n");
        sb.append("     │ Write to page table       │\n");
        sb.append("     │ ════════════════════════► │ VM EXIT\n");
        sb.append("     │                           │\n");
        sb.append("     │                    ┌──────┴──────┐\n");
        sb.append("     │                    │ 1. Save state │\n");
        sb.append("     │                    │ 2. Emulate    │\n");
        sb.append("     │                    │ 3. Resume     │\n");
        sb.append("     │                    └──────┬──────┘\n");
        sb.append("     │                           │\n");
        sb.append("     │ ◄════════════════════════ │ VM ENTRY\n");
        sb.append("     │       (continue)          │\n");
        sb.append("     ▼                           ▼\n\n");
        
        return sb.toString();
    }
}

