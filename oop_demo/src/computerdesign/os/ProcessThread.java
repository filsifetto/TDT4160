package computerdesign.os;

import computerdesign.memory.RegisterFile;

/**
 * ProcessThread - a thread of execution within a process.
 * 
 * A thread is the smallest unit of CPU scheduling. Multiple threads
 * within a process share:
 * - Code (text segment)
 * - Data (global variables)
 * - Heap
 * - Open files and other resources
 * 
 * Each thread has its own:
 * - Stack (for local variables and function calls)
 * - Registers (including PC and SP)
 * - Thread-local storage
 * 
 * Key insight: Threads are "lightweight processes" - context switching
 * between threads is faster because they share the same address space.
 * 
 * Thread States (similar to process states):
 * - NEW: Being created
 * - RUNNABLE: Ready to run
 * - RUNNING: Currently executing
 * - BLOCKED: Waiting for a resource
 * - TERMINATED: Finished execution
 */
public class ProcessThread {
    
    /**
     * Thread states.
     */
    public enum ThreadState {
        NEW,
        RUNNABLE,
        RUNNING,
        BLOCKED,
        TERMINATED
    }
    
    // Thread identification
    private final int threadId;         // Thread ID (within process)
    private final Process process;      // Parent process
    private final String name;          // Thread name
    
    // CPU state (each thread has its own)
    private int programCounter;
    private int[] savedRegisters;
    private int stackPointer;
    
    // Thread's stack
    private int stackBase;              // Bottom of stack
    private int stackSize;              // Stack size
    
    // Thread state
    private ThreadState state;
    private int priority;
    private long cpuTimeUsed;
    private Object waitingOn;           // What resource we're waiting for
    
    /**
     * Create a new thread.
     */
    public ProcessThread(int threadId, Process process, String name) {
        this.threadId = threadId;
        this.process = process;
        this.name = name;
        this.savedRegisters = new int[32];
        
        this.state = ThreadState.NEW;
        this.priority = process.getPriority();
        this.cpuTimeUsed = 0;
    }
    
    /**
     * Save thread's CPU state.
     */
    public void saveContext(RegisterFile registers, int pc) {
        this.programCounter = pc;
        for (int i = 0; i < 32; i++) {
            this.savedRegisters[i] = registers.read(i);
        }
        this.stackPointer = savedRegisters[2];
    }
    
    /**
     * Restore thread's CPU state.
     */
    public void restoreContext(RegisterFile registers) {
        for (int i = 0; i < 32; i++) {
            registers.write(i, savedRegisters[i]);
        }
    }
    
    /**
     * Start the thread.
     */
    public void start() {
        if (state == ThreadState.NEW) {
            state = ThreadState.RUNNABLE;
        }
    }
    
    /**
     * Block the thread waiting for a resource.
     */
    public void block(Object resource) {
        if (state == ThreadState.RUNNING) {
            state = ThreadState.BLOCKED;
            waitingOn = resource;
        }
    }
    
    /**
     * Unblock the thread.
     */
    public void unblock() {
        if (state == ThreadState.BLOCKED) {
            state = ThreadState.RUNNABLE;
            waitingOn = null;
        }
    }
    
    /**
     * Terminate the thread.
     */
    public void terminate() {
        state = ThreadState.TERMINATED;
    }
    
    /**
     * Yield the CPU to another thread.
     */
    public void yield() {
        if (state == ThreadState.RUNNING) {
            state = ThreadState.RUNNABLE;
        }
    }
    
    // State transitions
    public void setRunning() {
        if (state == ThreadState.RUNNABLE) {
            state = ThreadState.RUNNING;
        }
    }
    
    public void setRunnable() {
        if (state != ThreadState.TERMINATED) {
            state = ThreadState.RUNNABLE;
        }
    }
    
    // Getters and setters
    public int getThreadId() { return threadId; }
    public Process getProcess() { return process; }
    public String getName() { return name; }
    public ThreadState getState() { return state; }
    public int getProgramCounter() { return programCounter; }
    public void setProgramCounter(int pc) { this.programCounter = pc; }
    public int getStackPointer() { return stackPointer; }
    public void setStackPointer(int sp) { 
        this.stackPointer = sp;
        this.savedRegisters[2] = sp;
    }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public long getCpuTimeUsed() { return cpuTimeUsed; }
    public void addCpuTime(long cycles) { this.cpuTimeUsed += cycles; }
    public Object getWaitingOn() { return waitingOn; }
    
    public void setStackBase(int base) { this.stackBase = base; }
    public void setStackSize(int size) { this.stackSize = size; }
    public int getStackBase() { return stackBase; }
    public int getStackSize() { return stackSize; }
    
    /**
     * Get the Thread Control Block (TCB) as a string.
     */
    public String getTCB() {
        return String.format(
            "Thread Control Block:\n" +
            "  TID: %d (PID: %d)\n" +
            "  Name: %s\n" +
            "  State: %s\n" +
            "  PC: 0x%08X\n" +
            "  SP: 0x%08X\n" +
            "  Priority: %d\n" +
            "  CPU Time: %d cycles\n" +
            "  Waiting on: %s",
            threadId, process.getPid(), name, state, 
            programCounter, stackPointer, priority, cpuTimeUsed,
            waitingOn != null ? waitingOn.toString() : "nothing"
        );
    }
    
    @Override
    public String toString() {
        return String.format("Thread{tid=%d, pid=%d, name='%s', state=%s}", 
            threadId, process.getPid(), name, state);
    }
}

