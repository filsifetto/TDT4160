package computerdesign.os;

import java.util.ArrayList;
import java.util.List;

import computerdesign.memory.RegisterFile;

/**
 * Process - a program in execution.
 * 
 * A process is the OS's abstraction of a running program. It includes:
 * - Memory space: Code, data, heap, and stack
 * - CPU state: Registers, program counter
 * - OS state: Process ID, state, resources
 * 
 * Key insight from Patterson & Hennessy: The OS uses context switching to
 * share the CPU among multiple processes. Each process thinks it has the
 * whole machine to itself!
 * 
 * Process States:
 * - NEW: Being created
 * - READY: Waiting to be scheduled
 * - RUNNING: Currently executing on CPU
 * - WAITING: Blocked on I/O or other event
 * - TERMINATED: Finished execution
 */
public class Process {
    
    /**
     * Process states.
     */
    public enum ProcessState {
        NEW,
        READY,
        RUNNING,
        WAITING,
        TERMINATED
    }
    
    // Process identification
    private final int pid;              // Process ID
    private final String name;          // Process name
    private Process parent;             // Parent process
    private final List<Process> children;  // Child processes
    
    // CPU state (saved during context switch)
    private int programCounter;
    private int[] savedRegisters;       // All 32 RISC-V registers
    
    // Memory state
    private int textBase;               // Code segment start
    private int textSize;               // Code segment size
    private int dataBase;               // Data segment start
    private int dataSize;               // Data segment size
    private int heapBase;               // Heap start
    private int heapSize;               // Current heap size
    private int stackBase;              // Stack base (grows down)
    private int stackPointer;           // Current stack pointer
    
    // OS state
    private ProcessState state;
    private int priority;               // Scheduling priority (0 = highest)
    private long cpuTimeUsed;           // Total CPU time used (cycles)
    private long creationTime;          // When process was created
    private int exitCode;               // Exit status
    
    // Threading support
    private final List<ProcessThread> threads;
    private ProcessThread mainThread;
    
    /**
     * Create a new process.
     */
    public Process(int pid, String name) {
        this.pid = pid;
        this.name = name;
        this.children = new ArrayList<>();
        this.threads = new ArrayList<>();
        this.savedRegisters = new int[32];
        
        // Initialize memory regions (default values)
        this.textBase = 0x00000000;
        this.dataBase = 0x10000000;
        this.heapBase = 0x10010000;
        this.stackBase = 0x7FFFFFFC;
        
        this.state = ProcessState.NEW;
        this.priority = 10;  // Default priority
        this.cpuTimeUsed = 0;
        this.creationTime = System.currentTimeMillis();
        
        // Create main thread
        this.mainThread = new ProcessThread(0, this, "main");
        this.threads.add(mainThread);
    }
    
    /**
     * Context switch: Save CPU state from processor.
     * Called when this process is being switched OUT.
     */
    public void saveContext(RegisterFile registers, int pc) {
        this.programCounter = pc;
        for (int i = 0; i < 32; i++) {
            this.savedRegisters[i] = registers.read(i);
        }
        this.stackPointer = savedRegisters[2];  // sp is x2
    }
    
    /**
     * Context switch: Restore CPU state to processor.
     * Called when this process is being switched IN.
     */
    public void restoreContext(RegisterFile registers) {
        for (int i = 0; i < 32; i++) {
            registers.write(i, savedRegisters[i]);
        }
    }
    
    /**
     * Allocate memory on the heap.
     * Simple bump allocator - real OS would be more sophisticated.
     */
    public int allocateHeap(int size) {
        int address = heapBase + heapSize;
        heapSize += size;
        // Align to word boundary
        heapSize = (heapSize + 3) & ~3;
        return address;
    }
    
    /**
     * Push data onto the stack.
     */
    public int pushStack(int size) {
        stackPointer -= size;
        // Align to word boundary
        stackPointer = stackPointer & ~3;
        savedRegisters[2] = stackPointer;  // Update sp
        return stackPointer;
    }
    
    /**
     * Pop data from the stack.
     */
    public void popStack(int size) {
        stackPointer += size;
        savedRegisters[2] = stackPointer;
    }
    
    /**
     * Create a child process (fork).
     */
    public Process fork(int childPid, String childName) {
        Process child = new Process(childPid, childName);
        child.parent = this;
        child.priority = this.priority;
        
        // Copy registers
        System.arraycopy(this.savedRegisters, 0, child.savedRegisters, 0, 32);
        child.programCounter = this.programCounter;
        
        // Child gets its own memory space (in real OS, copy-on-write)
        child.textBase = this.textBase;
        child.textSize = this.textSize;
        child.dataBase = this.dataBase;
        child.dataSize = this.dataSize;
        child.heapBase = this.heapBase;
        child.heapSize = this.heapSize;
        child.stackBase = this.stackBase;
        child.stackPointer = this.stackPointer;
        
        this.children.add(child);
        child.state = ProcessState.READY;
        
        return child;
    }
    
    /**
     * Terminate the process.
     */
    public void terminate(int exitCode) {
        this.exitCode = exitCode;
        this.state = ProcessState.TERMINATED;
        
        // Terminate all threads
        for (ProcessThread thread : threads) {
            thread.terminate();
        }
    }
    
    /**
     * Create a new thread in this process.
     */
    public ProcessThread createThread(int threadId, String threadName, int startAddress) {
        ProcessThread thread = new ProcessThread(threadId, this, threadName);
        thread.setProgramCounter(startAddress);
        
        // Allocate stack for the new thread
        int threadStackSize = 4096;  // 4KB stack
        int threadStack = allocateHeap(threadStackSize);
        thread.setStackPointer(threadStack + threadStackSize);
        
        threads.add(thread);
        return thread;
    }
    
    // State transitions
    public void setReady() { 
        if (state != ProcessState.TERMINATED) {
            state = ProcessState.READY; 
        }
    }
    
    public void setRunning() { 
        if (state == ProcessState.READY) {
            state = ProcessState.RUNNING; 
        }
    }
    
    public void setWaiting() { 
        if (state == ProcessState.RUNNING) {
            state = ProcessState.WAITING; 
        }
    }
    
    public void wakeUp() {
        if (state == ProcessState.WAITING) {
            state = ProcessState.READY;
        }
    }
    
    // Getters
    public int getPid() { return pid; }
    public String getName() { return name; }
    public ProcessState getState() { return state; }
    public int getProgramCounter() { return programCounter; }
    public void setProgramCounter(int pc) { this.programCounter = pc; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public long getCpuTimeUsed() { return cpuTimeUsed; }
    public void addCpuTime(long cycles) { this.cpuTimeUsed += cycles; }
    public int getExitCode() { return exitCode; }
    public Process getParent() { return parent; }
    public List<Process> getChildren() { return new ArrayList<>(children); }
    public List<ProcessThread> getThreads() { return new ArrayList<>(threads); }
    public ProcessThread getMainThread() { return mainThread; }
    
    public int getStackPointer() { return stackPointer; }
    public void setStackPointer(int sp) { 
        this.stackPointer = sp; 
        this.savedRegisters[2] = sp;
    }
    
    /**
     * Get the Process Control Block (PCB) as a string.
     */
    public String getPCB() {
        return String.format(
            "Process Control Block:\n" +
            "  PID: %d\n" +
            "  Name: %s\n" +
            "  State: %s\n" +
            "  PC: 0x%08X\n" +
            "  SP: 0x%08X\n" +
            "  Priority: %d\n" +
            "  CPU Time: %d cycles\n" +
            "  Threads: %d\n" +
            "  Memory: text=0x%X, data=0x%X, heap=0x%X, stack=0x%X",
            pid, name, state, programCounter, stackPointer, priority,
            cpuTimeUsed, threads.size(), textBase, dataBase, heapBase, stackBase
        );
    }
    
    @Override
    public String toString() {
        return String.format("Process{pid=%d, name='%s', state=%s}", pid, name, state);
    }
}

