package computerdesign.alu;

/**
 * ALU (Arithmetic Logic Unit) - the computational heart of the processor.
 * 
 * The ALU performs all arithmetic and logical operations:
 * - Arithmetic: ADD, SUB, MUL, DIV
 * - Logical: AND, OR, XOR, NOT
 * - Shifts: SLL, SRL, SRA
 * - Comparisons: SLT, SLTU
 * 
 * Key insight: The ALU is purely combinational logic - it has NO state.
 * Given inputs A, B, and operation, it immediately produces a result.
 * The result only becomes state when written to a register.
 */
public class ALU {
    
    /**
     * ALU operations (matching RISC-V ALU control signals).
     */
    public enum Operation {
        ADD,        // Addition
        SUB,        // Subtraction
        AND,        // Bitwise AND
        OR,         // Bitwise OR
        XOR,        // Bitwise XOR
        SLL,        // Shift left logical
        SRL,        // Shift right logical
        SRA,        // Shift right arithmetic
        SLT,        // Set less than (signed)
        SLTU,       // Set less than unsigned
        MUL,        // Multiplication (lower 32 bits)
        MULH,       // Multiplication (upper 32 bits, signed)
        DIV,        // Division (signed)
        DIVU,       // Division (unsigned)
        REM,        // Remainder (signed)
        REMU,       // Remainder (unsigned)
        PASS_B      // Pass through B (for LUI)
    }
    
    /**
     * Result of an ALU operation, including flags.
     */
    public static class Result {
        public final int value;
        public final boolean zero;      // Result is zero
        public final boolean negative;  // Result is negative (signed)
        public final boolean overflow;  // Arithmetic overflow occurred
        public final boolean carry;     // Carry/borrow occurred
        
        public Result(int value, boolean zero, boolean negative, 
                      boolean overflow, boolean carry) {
            this.value = value;
            this.zero = zero;
            this.negative = negative;
            this.overflow = overflow;
            this.carry = carry;
        }
        
        @Override
        public String toString() {
            return String.format("Result{value=0x%08X, zero=%b, neg=%b, ovf=%b, carry=%b}",
                value, zero, negative, overflow, carry);
        }
    }
    
    /**
     * Execute an ALU operation.
     * 
     * @param a First operand (typically rs1 value)
     * @param b Second operand (typically rs2 value or immediate)
     * @param op The operation to perform
     * @return The result with flags
     */
    public Result execute(int a, int b, Operation op) {
        int result;
        boolean overflow = false;
        boolean carry = false;
        
        switch (op) {
            case ADD:
                result = a + b;
                // Check for signed overflow
                overflow = ((a > 0 && b > 0 && result < 0) ||
                           (a < 0 && b < 0 && result > 0));
                // Check for unsigned carry
                carry = Integer.compareUnsigned(result, a) < 0;
                break;
                
            case SUB:
                result = a - b;
                overflow = ((a > 0 && b < 0 && result < 0) ||
                           (a < 0 && b > 0 && result > 0));
                carry = Integer.compareUnsigned(a, b) < 0;
                break;
                
            case AND:
                result = a & b;
                break;
                
            case OR:
                result = a | b;
                break;
                
            case XOR:
                result = a ^ b;
                break;
                
            case SLL:
                result = a << (b & 0x1F);  // Only use lower 5 bits of shift amount
                break;
                
            case SRL:
                result = a >>> (b & 0x1F);  // Logical right shift
                break;
                
            case SRA:
                result = a >> (b & 0x1F);   // Arithmetic right shift
                break;
                
            case SLT:
                result = (a < b) ? 1 : 0;   // Signed comparison
                break;
                
            case SLTU:
                result = (Integer.compareUnsigned(a, b) < 0) ? 1 : 0;
                break;
                
            case MUL:
                result = a * b;  // Lower 32 bits
                break;
                
            case MULH:
                result = (int) (((long) a * (long) b) >> 32);  // Upper 32 bits
                break;
                
            case DIV:
                result = (b == 0) ? -1 : a / b;
                break;
                
            case DIVU:
                result = (b == 0) ? -1 : Integer.divideUnsigned(a, b);
                break;
                
            case REM:
                result = (b == 0) ? a : a % b;
                break;
                
            case REMU:
                result = (b == 0) ? a : Integer.remainderUnsigned(a, b);
                break;
                
            case PASS_B:
                result = b;
                break;
                
            default:
                throw new IllegalArgumentException("Unknown ALU operation: " + op);
        }
        
        boolean zero = (result == 0);
        boolean negative = (result < 0);
        
        return new Result(result, zero, negative, overflow, carry);
    }
    
    /**
     * Simple execute that just returns the result value.
     */
    public int compute(int a, int b, Operation op) {
        return execute(a, b, op).value;
    }
    
    /**
     * Compare two values for branching.
     * Returns true if the branch should be taken.
     */
    public boolean compare(int a, int b, BranchCondition condition) {
        switch (condition) {
            case EQ:  return a == b;
            case NE:  return a != b;
            case LT:  return a < b;
            case GE:  return a >= b;
            case LTU: return Integer.compareUnsigned(a, b) < 0;
            case GEU: return Integer.compareUnsigned(a, b) >= 0;
            default:  return false;
        }
    }
    
    /**
     * Branch conditions for comparison operations.
     */
    public enum BranchCondition {
        EQ,   // Equal
        NE,   // Not equal
        LT,   // Less than (signed)
        GE,   // Greater than or equal (signed)
        LTU,  // Less than (unsigned)
        GEU   // Greater than or equal (unsigned)
    }
}

