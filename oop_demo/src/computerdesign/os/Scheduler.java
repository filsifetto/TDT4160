package computerdesign.os;

import java.util.*;

/**
 * Scheduler - decides which process/thread runs next.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE SCHEDULING PROBLEM
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Modern systems run many processes, but CPUs have limited cores.
 * The scheduler creates the ILLUSION of parallelism by rapidly switching
 * between processes (time-sharing).
 * 
 *   Time →  |-------|-------|-------|-------|-------|-------|
 *           |  P1   |  P2   |  P3   |  P1   |  P2   |  P3   |  ...
 *           
 *   Each process thinks it has the CPU to itself!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * SCHEDULING GOALS (often conflicting!)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * FAIRNESS:       Every process should get a fair share of CPU time
 * THROUGHPUT:     Maximize jobs completed per unit time
 * LATENCY:        Minimize time from request to response
 * TURNAROUND:     Minimize time from submission to completion
 * UTILIZATION:    Keep CPU busy (minimize idle time)
 * 
 * No single algorithm optimizes all goals - tradeoffs are inevitable!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * COMMON SCHEDULING ALGORITHMS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * FCFS (First Come First Served):
 *   ┌───────────────────────────────────────────────────┐
 *   │ P1 ████████████████████████ │ P2 ████ │ P3 ████  │
 *   └───────────────────────────────────────────────────┘
 *   + Simple, no starvation
 *   - "Convoy effect": short jobs wait behind long ones
 * 
 * ROUND ROBIN (Time-Sliced):
 *   ┌────────────────────────────────────────────────────────────┐
 *   │ P1 ██ │ P2 ██ │ P3 ██ │ P1 ██ │ P2 ██ │ P3 ██ │ P1 ██ │...
 *   └────────────────────────────────────────────────────────────┘
 *   + Fair, good response time for interactive tasks
 *   - Context switch overhead, poor for batch jobs
 *   Time quantum tradeoff:
 *     - Too short: excessive context switches
 *     - Too long: poor response time
 * 
 * PRIORITY:
 *   ┌───────────────────────────────────────────────────┐
 *   │ High █████████ │ Med ████████████ │ Low ████████ │
 *   └───────────────────────────────────────────────────┘
 *   + Important tasks run first
 *   - STARVATION: low priority may never run!
 *   Solution: "Aging" - increase priority over time
 * 
 * MULTILEVEL FEEDBACK QUEUE (used by most real OSes):
 *   - Multiple queues with different priorities
 *   - Processes move between queues based on behavior
 *   - Interactive → high priority, CPU-bound → low priority
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * CONTEXT SWITCHING
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * When switching from Process A to Process B:
 * 
 *   1. Save Process A's state:
 *      - All registers (including PC, SP)
 *      - CPU flags
 *      - Memory management info (page table base)
 *      
 *   2. Load Process B's state:
 *      - Restore all registers
 *      - Restore page table
 *      - Flush TLB (if needed)
 *      
 *   3. Resume execution at Process B's saved PC
 * 
 * Context switches are EXPENSIVE! (~1-10 microseconds)
 * - Direct cost: saving/restoring state
 * - Indirect cost: cache/TLB pollution
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @see Process - The entities being scheduled
 * @see ProcessThread - For fine-grained scheduling within a process
 */
public class Scheduler {
    
    /**
     * Scheduling algorithms.
     */
    public enum Algorithm {
        FCFS,           // First Come First Served
        ROUND_ROBIN,    // Time-sliced
        PRIORITY        // Priority-based
    }
    
    // Scheduler state
    private Algorithm algorithm;
    private final Queue<Process> readyQueue;
    private final List<Process> allProcesses;
    private Process currentProcess;
    private int timeQuantum;            // For round robin (in cycles)
    private int remainingQuantum;       // Cycles left for current process
    
    // Statistics
    private int contextSwitches;
    private long totalIdleTime;
    
    public Scheduler() {
        this(Algorithm.ROUND_ROBIN, 100);  // Default: round robin with 100 cycle quantum
    }
    
    public Scheduler(Algorithm algorithm, int timeQuantum) {
        this.algorithm = algorithm;
        this.timeQuantum = timeQuantum;
        this.remainingQuantum = timeQuantum;
        this.readyQueue = new LinkedList<>();
        this.allProcesses = new ArrayList<>();
        this.contextSwitches = 0;
        this.totalIdleTime = 0;
    }
    
    /**
     * Add a new process to the scheduler.
     */
    public void addProcess(Process process) {
        allProcesses.add(process);
        if (process.getState() == Process.ProcessState.READY ||
            process.getState() == Process.ProcessState.NEW) {
            process.setReady();
            enqueue(process);
        }
    }
    
    /**
     * Remove a process from the scheduler.
     */
    public void removeProcess(Process process) {
        readyQueue.remove(process);
        allProcesses.remove(process);
        if (currentProcess == process) {
            currentProcess = null;
        }
    }
    
    /**
     * Get the next process to run.
     * Returns null if no process is ready.
     */
    public Process schedule() {
        if (readyQueue.isEmpty()) {
            return null;
        }
        
        Process next;
        
        switch (algorithm) {
            case FCFS:
                next = readyQueue.poll();
                break;
                
            case ROUND_ROBIN:
                next = readyQueue.poll();
                remainingQuantum = timeQuantum;
                break;
                
            case PRIORITY:
                next = selectHighestPriority();
                break;
                
            default:
                next = readyQueue.poll();
        }
        
        if (next != null && next != currentProcess) {
            contextSwitches++;
        }
        
        return next;
    }
    
    /**
     * Called each cycle to potentially trigger preemption.
     */
    public boolean tick() {
        if (currentProcess == null) {
            totalIdleTime++;
            return false;
        }
        
        currentProcess.addCpuTime(1);
        
        if (algorithm == Algorithm.ROUND_ROBIN) {
            remainingQuantum--;
            if (remainingQuantum <= 0) {
                // Time quantum expired - preempt
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Perform a context switch.
     */
    public Process contextSwitch(Process oldProcess, Process newProcess) {
        // Save old process state
        if (oldProcess != null && oldProcess.getState() == Process.ProcessState.RUNNING) {
            oldProcess.setReady();
            enqueue(oldProcess);
        }
        
        // Load new process
        if (newProcess != null) {
            readyQueue.remove(newProcess);
            newProcess.setRunning();
        }
        
        currentProcess = newProcess;
        contextSwitches++;
        
        return newProcess;
    }
    
    /**
     * Block the current process (e.g., waiting for I/O).
     */
    public void blockCurrent() {
        if (currentProcess != null) {
            currentProcess.setWaiting();
            currentProcess = null;
        }
    }
    
    /**
     * Wake up a blocked process.
     */
    public void wakeUp(Process process) {
        if (process.getState() == Process.ProcessState.WAITING) {
            process.wakeUp();
            enqueue(process);
        }
    }
    
    /**
     * Enqueue a process according to the scheduling algorithm.
     */
    private void enqueue(Process process) {
        if (algorithm == Algorithm.PRIORITY) {
            // Insert in priority order
            List<Process> temp = new ArrayList<>(readyQueue);
            temp.add(process);
            temp.sort(Comparator.comparingInt(Process::getPriority));
            readyQueue.clear();
            readyQueue.addAll(temp);
        } else {
            readyQueue.offer(process);
        }
    }
    
    /**
     * Select the highest priority process.
     */
    private Process selectHighestPriority() {
        Process highest = null;
        for (Process p : readyQueue) {
            if (highest == null || p.getPriority() < highest.getPriority()) {
                highest = p;
            }
        }
        if (highest != null) {
            readyQueue.remove(highest);
        }
        return highest;
    }
    
    // Getters and setters
    public Algorithm getAlgorithm() { return algorithm; }
    public void setAlgorithm(Algorithm algorithm) { this.algorithm = algorithm; }
    public int getTimeQuantum() { return timeQuantum; }
    public void setTimeQuantum(int quantum) { this.timeQuantum = quantum; }
    public Process getCurrentProcess() { return currentProcess; }
    public int getReadyCount() { return readyQueue.size(); }
    public int getContextSwitches() { return contextSwitches; }
    public long getTotalIdleTime() { return totalIdleTime; }
    public List<Process> getAllProcesses() { return new ArrayList<>(allProcesses); }
    
    /**
     * Get scheduler statistics.
     */
    public String getStats() {
        int running = 0, ready = 0, waiting = 0, terminated = 0;
        for (Process p : allProcesses) {
            switch (p.getState()) {
                case RUNNING: running++; break;
                case READY: ready++; break;
                case WAITING: waiting++; break;
                case TERMINATED: terminated++; break;
                default: break;
            }
        }
        
        return String.format(
            "Scheduler Stats:\n" +
            "  Algorithm: %s\n" +
            "  Time Quantum: %d cycles\n" +
            "  Context Switches: %d\n" +
            "  Total Processes: %d\n" +
            "    Running: %d\n" +
            "    Ready: %d\n" +
            "    Waiting: %d\n" +
            "    Terminated: %d\n" +
            "  Idle Time: %d cycles",
            algorithm, timeQuantum, contextSwitches,
            allProcesses.size(), running, ready, waiting, terminated, totalIdleTime
        );
    }
    
    @Override
    public String toString() {
        return String.format("Scheduler{algorithm=%s, ready=%d, current=%s}",
            algorithm, readyQueue.size(), 
            currentProcess != null ? currentProcess.getName() : "none");
    }
}

