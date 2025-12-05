package computerdesign.theory;

/**
 * CallingConvention - Function call mechanics and memory layout.
 * 
 * Covers learning goals: T2.3 (Function calls), T2.4 (Memory layout, addressing)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * WHAT HAPPENS DURING A FUNCTION CALL?
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * CALLER'S RESPONSIBILITIES (before call):
 *   1. Place arguments in a0-a7 (and stack if more than 8)
 *   2. Save caller-saved registers it needs after the call (t0-t6, a0-a7)
 *   3. Execute JAL or JALR (saves return address in ra)
 * 
 * CALLEE'S RESPONSIBILITIES (function prologue):
 *   1. Allocate stack frame: addi sp, sp, -framesize
 *   2. Save callee-saved registers it will use (s0-s11, ra)
 *   3. Set up frame pointer if needed: mv fp, sp
 * 
 * CALLEE'S RESPONSIBILITIES (function epilogue):
 *   1. Place return value in a0 (and a1 for 128-bit values)
 *   2. Restore callee-saved registers
 *   3. Deallocate stack frame: addi sp, sp, framesize
 *   4. Return: ret (jalr x0, 0(ra))
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * RISC-V REGISTER USAGE (ABI)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * ┌────────┬──────┬────────────┬───────────────────────────────────────────────┐
 * │Register│ ABI  │ Preserved? │ Description                                   │
 * ├────────┼──────┼────────────┼───────────────────────────────────────────────┤
 * │   x0   │ zero │    N/A     │ Hardwired zero                                │
 * │   x1   │  ra  │   Caller   │ Return address                                │
 * │   x2   │  sp  │   Callee   │ Stack pointer                                 │
 * │   x3   │  gp  │    N/A     │ Global pointer (linker sets, don't modify)    │
 * │   x4   │  tp  │    N/A     │ Thread pointer (OS sets)                      │
 * │  x5-7  │ t0-2 │   Caller   │ Temporaries                                   │
 * │   x8   │s0/fp │   Callee   │ Saved register / Frame pointer                │
 * │   x9   │  s1  │   Callee   │ Saved register                                │
 * │ x10-11 │ a0-1 │   Caller   │ Arguments / Return values                     │
 * │ x12-17 │ a2-7 │   Caller   │ Arguments                                     │
 * │ x18-27 │s2-11 │   Callee   │ Saved registers                               │
 * │ x28-31 │ t3-6 │   Caller   │ Temporaries                                   │
 * └────────┴──────┴────────────┴───────────────────────────────────────────────┘
 * 
 * CALLER-SAVED (volatile): Caller must save if it needs them after call
 *   t0-t6: Temporary values
 *   a0-a7: Arguments (caller knows what it passed)
 *   ra: Return address (JAL overwrites it)
 * 
 * CALLEE-SAVED (non-volatile): Callee must restore before returning
 *   s0-s11: Long-lived local variables
 *   sp: Stack pointer (must be same at function exit)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * RISC-V MEMORY MAP (Figure 2.13)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   High addresses (0xFFFF_FFFF for RV32)
 *   ┌─────────────────────────────────────┐
 *   │                                     │
 *   │            STACK                    │ ← sp (grows DOWN)
 *   │            ↓↓↓↓                     │
 *   │                                     │
 *   │         (unused)                    │
 *   │                                     │
 *   │            ↑↑↑↑                     │
 *   │            HEAP                     │ ← brk (grows UP)
 *   │                                     │
 *   ├─────────────────────────────────────┤
 *   │       Dynamic Data (.data)          │ ← Initialized globals
 *   ├─────────────────────────────────────┤
 *   │       Static Data (.bss)            │ ← Uninitialized globals (zeroed)
 *   │                                     │ ← gp (global pointer) points here
 *   ├─────────────────────────────────────┤
 *   │           Text (.text)              │ ← Program code
 *   │                                     │ ← pc starts here
 *   ├─────────────────────────────────────┤
 *   │          Reserved                   │
 *   └─────────────────────────────────────┘
 *   Low addresses (0x0000_0000)
 * 
 * TYPICAL ADDRESSES (RV32 Linux):
 *   Text segment:   0x0001_0000 - 0x0FFF_FFFF
 *   Static data:    0x1000_0000 - 0x1FFF_FFFF
 *   Heap:           Grows up from end of static data
 *   Stack:          Grows down from 0x7FFF_FFFF
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * STATIC vs DYNAMIC DATA
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * STATIC DATA:
 *   - Known at compile time
 *   - Fixed size, fixed location
 *   - Global variables, string constants
 *   - Accessed via gp (global pointer) + offset
 *   
 *   Example: static int count = 0;
 *   Access: lw t0, count(gp)   # gp + offset to count
 * 
 * DYNAMIC DATA:
 *   - Allocated at runtime (malloc/new)
 *   - Variable size
 *   - Heap: grows upward from end of static data
 *   - Stack: local variables, grows downward
 *   
 *   Heap: int* p = malloc(100);  # brk increases
 *   Stack: int arr[10];           # sp decreases
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * THE STACK FRAME
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * When function foo calls function bar:
 * 
 *                      High addresses
 *   ┌─────────────────────────────────────┐
 *   │     foo's caller's frame            │
 *   ├─────────────────────────────────────┤
 *   │     Argument 9+                     │ ← Arguments beyond a0-a7
 *   │     Saved ra (return to caller)     │
 *   │     Saved s0-s11 (if foo uses them) │
 *   │     Local variables                 │
 *   │     Spill area (if needed)          │ ← foo's frame
 *   ├─────────────────────────────────────┤ ← fp (s0) points here
 *   │     Argument 9+ for bar             │
 *   │     Saved ra (return to foo)        │
 *   │     Saved s-registers               │
 *   │     Local variables                 │ ← bar's frame
 *   └─────────────────────────────────────┘ ← sp points here
 *                      Low addresses
 * 
 * STACK RULES:
 *   - Stack grows DOWNWARD (toward lower addresses)
 *   - sp always points to the TOP of the stack (lowest used address)
 *   - sp must be 16-byte aligned (for RV64) or 8-byte aligned (RV32)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * EXAMPLE: COMPLETE FUNCTION CALL
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * C code:
 *   int add(int a, int b) { return a + b; }
 *   int main() { int x = add(3, 5); }
 * 
 * Assembly (simplified):
 * 
 *   add:
 *       # Prologue (simple function, no frame needed)
 *       # Arguments in a0, a1
 *       add  a0, a0, a1    # a0 = a + b (return value)
 *       ret                 # return (jalr x0, 0(ra))
 * 
 *   main:
 *       # Prologue
 *       addi sp, sp, -16   # Allocate frame (16-byte aligned)
 *       sw   ra, 12(sp)    # Save return address
 *       
 *       # Call add(3, 5)
 *       li   a0, 3         # First argument
 *       li   a1, 5         # Second argument
 *       jal  ra, add       # Call add, save return in ra
 *       
 *       # Result is in a0, store it
 *       mv   t0, a0        # x = result
 *       
 *       # Epilogue
 *       lw   ra, 12(sp)    # Restore return address
 *       addi sp, sp, 16    # Deallocate frame
 *       ret
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * LEAF vs NON-LEAF FUNCTIONS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * LEAF FUNCTION: Does not call other functions
 *   - May not need to save ra
 *   - May use only temporaries (no s-registers to save)
 *   - Often no stack frame at all!
 * 
 * NON-LEAF FUNCTION: Calls other functions
 *   - MUST save ra (will be overwritten by JAL)
 *   - Must save any s-registers it uses
 *   - Needs stack frame
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * RISC-V ADDRESSING MODES
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * RISC-V has exactly FOUR addressing modes:
 * 
 * 1. IMMEDIATE (I-type):
 *    addi t0, t1, 100    # t0 = t1 + 100
 *    Value encoded directly in instruction (12-bit signed)
 * 
 * 2. REGISTER (R-type):
 *    add t0, t1, t2      # t0 = t1 + t2
 *    All operands are registers
 * 
 * 3. BASE + DISPLACEMENT (Load/Store):
 *    lw t0, 8(sp)        # t0 = Memory[sp + 8]
 *    Address = base register + 12-bit signed offset
 * 
 * 4. PC-RELATIVE (Branches and JAL):
 *    beq t0, t1, label   # if t0==t1, PC = PC + offset
 *    jal ra, function    # ra = PC+4; PC = PC + offset
 *    Target = PC + signed offset (allows relocatable code)
 * 
 * NO INDIRECT ADDRESSING: Must use load to get address, then another load
 *    # To access *ptr:
 *    lw t0, ptr(gp)      # t0 = ptr (load the pointer)
 *    lw t1, 0(t0)        # t1 = *ptr (load through pointer)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * HANDLING LARGE CONSTANTS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Problem: Immediates are only 12 bits, but we need 32-bit constants!
 * 
 * Solution: LUI + ADDI (two instructions)
 * 
 *   lui  t0, 0x12345     # t0 = 0x12345000 (load upper 20 bits)
 *   addi t0, t0, 0x678   # t0 = 0x12345678 (add lower 12 bits)
 * 
 * AUIPC: Add Upper Immediate to PC (for PC-relative addressing)
 *   auipc t0, offset_hi  # t0 = PC + (offset_hi << 12)
 *   addi  t0, t0, offset_lo
 *   # Now t0 = PC + full 32-bit offset
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * PROGRAM TRANSLATION AND STARTUP (Figure 2.20)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │  Source Code (.c, .cpp)                                                │
 *   │         │                                                               │
 *   │         ▼ COMPILER                                                      │
 *   │  Assembly Code (.s)                                                     │
 *   │         │                                                               │
 *   │         ▼ ASSEMBLER                                                     │
 *   │  Object File (.o) ────────────────────────┐                             │
 *   │         │                                  │                             │
 *   │         │         Library (.a, .so)        │                             │
 *   │         │                │                 │                             │
 *   │         ▼                ▼                 ▼                             │
 *   │  ┌─────────────────────────────────────────────┐                        │
 *   │  │              LINKER                         │                        │
 *   │  └─────────────────────────────────────────────┘                        │
 *   │         │                                                               │
 *   │         ▼                                                               │
 *   │  Executable (a.out, .exe)                                               │
 *   │         │                                                               │
 *   │         ▼ LOADER (OS)                                                   │
 *   │  Process in Memory                                                      │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * LINKER resolves:
 *   - External references (function calls between files)
 *   - Library functions
 *   - Relocations (adjusts addresses for final memory layout)
 * 
 * LOADER:
 *   - Allocates memory for program
 *   - Copies text, data sections from executable
 *   - Sets up stack (sp), arguments
 *   - Jumps to entry point (_start, then calls main)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * TEXT REPRESENTATION
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * ASCII: 7-bit encoding, 128 characters
 *   - 'A' = 65, 'a' = 97, '0' = 48
 *   - Control codes: '\n' = 10, '\0' = 0 (null terminator)
 * 
 * UTF-8: Variable-length encoding (1-4 bytes)
 *   - ASCII-compatible for first 128 characters
 *   - Higher bytes signal multi-byte sequences
 * 
 * RISC-V string operations:
 *   lb t0, 0(a0)     # Load single byte
 *   sb t0, 0(a1)     # Store single byte
 *   # No dedicated string instructions - use loops!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class CallingConvention {
    
    // RISC-V register names
    public static final String[] REGISTER_NAMES = {
        "zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
        "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
        "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
        "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
    };
    
    // Register categories
    public static boolean isCallerSaved(int reg) {
        // t0-t6 (5-7, 28-31), a0-a7 (10-17), ra (1)
        if (reg == 1) return true;  // ra
        if (reg >= 5 && reg <= 7) return true;   // t0-t2
        if (reg >= 10 && reg <= 17) return true; // a0-a7
        if (reg >= 28 && reg <= 31) return true; // t3-t6
        return false;
    }
    
    public static boolean isCalleeSaved(int reg) {
        // s0-s11 (8-9, 18-27), sp (2)
        if (reg == 2) return true;  // sp
        if (reg == 8 || reg == 9) return true;  // s0-s1
        if (reg >= 18 && reg <= 27) return true; // s2-s11
        return false;
    }
    
    public static boolean isArgumentRegister(int reg) {
        return reg >= 10 && reg <= 17;  // a0-a7
    }
    
    public static boolean isTemporaryRegister(int reg) {
        return (reg >= 5 && reg <= 7) || (reg >= 28 && reg <= 31);
    }
    
    /**
     * Simulated stack frame for demonstration.
     */
    public static class StackFrame {
        public final String functionName;
        public final int[] savedRegisters;  // Indices of saved registers
        public final int[] savedValues;     // Values of saved registers
        public final int frameSize;
        public int[] localVariables;
        
        public StackFrame(String name, int frameSize, int[] savedRegs, int[] savedVals) {
            this.functionName = name;
            this.frameSize = frameSize;
            this.savedRegisters = savedRegs;
            this.savedValues = savedVals;
            this.localVariables = new int[frameSize / 4];
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Stack Frame: %s (%d bytes)\n", functionName, frameSize));
            for (int i = 0; i < savedRegisters.length; i++) {
                sb.append(String.format("  Saved %s = 0x%08X\n", 
                    REGISTER_NAMES[savedRegisters[i]], savedValues[i]));
            }
            return sb.toString();
        }
    }
    
    /**
     * Demonstrate function call sequence.
     */
    public static String demonstrateFunctionCall() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  FUNCTION CALL DEMONSTRATION\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("Consider: int result = add(3, 5);\n\n");
        
        sb.append("STEP 1: CALLER prepares arguments\n");
        sb.append("  li   a0, 3         # a0 = 3 (first argument)\n");
        sb.append("  li   a1, 5         # a1 = 5 (second argument)\n\n");
        
        sb.append("STEP 2: CALLER calls function\n");
        sb.append("  jal  ra, add       # ra = PC+4, PC = address of add\n\n");
        
        sb.append("STEP 3: CALLEE (add) executes\n");
        sb.append("  # Prologue: No stack frame needed for leaf function\n");
        sb.append("  add  a0, a0, a1    # a0 = 3 + 5 = 8 (return value in a0)\n");
        sb.append("  ret                # jalr x0, 0(ra) - return to caller\n\n");
        
        sb.append("STEP 4: CALLER receives result\n");
        sb.append("  mv   t0, a0        # result = a0 (= 8)\n\n");
        
        return sb.toString();
    }
    
    /**
     * Demonstrate memory layout.
     */
    public static String demonstrateMemoryLayout() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════════════════\n");
        sb.append("  MEMORY LAYOUT (RV32)\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("Address       | Content\n");
        sb.append("──────────────┼────────────────────────────────────────────\n");
        sb.append("0x7FFFFFF0    | ← sp (stack top, grows DOWN)\n");
        sb.append("    ...       | Stack frames (local vars, saved regs)\n");
        sb.append("0x7FFF0000    | Stack limit\n");
        sb.append("    ...       | (unused space)\n");
        sb.append("0x10020000    | ← Heap top (brk, grows UP)\n");
        sb.append("    ...       | Heap (malloc'd data)\n");
        sb.append("0x10010000    | ← Heap bottom\n");
        sb.append("0x10008000    | ← gp (global pointer)\n");
        sb.append("0x10000000    | Static data (.data, .bss)\n");
        sb.append("0x00010000    | Text segment (.text) ← PC starts here\n");
        sb.append("0x00000000    | Reserved (trap vectors, etc.)\n");
        
        return sb.toString();
    }
    
    /**
     * Demonstrate addressing modes.
     */
    public static String demonstrateAddressingModes() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════════════════\n");
        sb.append("  RISC-V ADDRESSING MODES\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        sb.append("1. IMMEDIATE:\n");
        sb.append("   addi t0, t1, 100   # t0 = t1 + 100\n");
        sb.append("   ori  t0, t1, 0xFF  # t0 = t1 | 0xFF\n\n");
        
        sb.append("2. REGISTER:\n");
        sb.append("   add  t0, t1, t2    # t0 = t1 + t2\n");
        sb.append("   mul  t0, t1, t2    # t0 = t1 × t2\n\n");
        
        sb.append("3. BASE + DISPLACEMENT:\n");
        sb.append("   lw   t0, 8(sp)     # t0 = Memory[sp + 8]\n");
        sb.append("   sw   t0, -4(fp)    # Memory[fp - 4] = t0\n\n");
        
        sb.append("4. PC-RELATIVE:\n");
        sb.append("   beq  t0, t1, loop  # if t0==t1: PC = PC + offset\n");
        sb.append("   jal  ra, func      # ra = PC+4; PC = PC + offset\n\n");
        
        sb.append("Loading 32-bit constant 0x12345678:\n");
        sb.append("   lui   t0, 0x12345   # t0 = 0x12345000\n");
        sb.append("   addi  t0, t0, 0x678 # t0 = 0x12345678\n");
        
        return sb.toString();
    }
    
    public static void main(String[] args) {
        System.out.println(demonstrateFunctionCall());
        System.out.println(demonstrateMemoryLayout());
        System.out.println(demonstrateAddressingModes());
    }
}

