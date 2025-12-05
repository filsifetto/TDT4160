package computerdesign.theory;

/**
 * NumberSystems - Understanding number representation in computers.
 * 
 * Covers learning goals: T2.2, T3.3 (floating point)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * POSITIONAL NUMBER SYSTEMS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * A number in base B with digits d_n...d_1 d_0 has value:
 *   Value = d_n × B^n + ... + d_1 × B^1 + d_0 × B^0
 * 
 * DECIMAL (Base 10): Digits 0-9
 *   Example: 247 = 2×10² + 4×10¹ + 7×10⁰ = 200 + 40 + 7 = 247
 * 
 * BINARY (Base 2): Digits 0-1
 *   Example: 1101 = 1×2³ + 1×2² + 0×2¹ + 1×2⁰ = 8 + 4 + 0 + 1 = 13
 * 
 * HEXADECIMAL (Base 16): Digits 0-9, A-F (A=10, B=11, ..., F=15)
 *   Example: 0x2F = 2×16¹ + 15×16⁰ = 32 + 15 = 47
 *   
 *   Why hex? Compact representation of binary (4 bits = 1 hex digit)
 *   0000=0, 0001=1, 0010=2, 0011=3, 0100=4, 0101=5, 0110=6, 0111=7
 *   1000=8, 1001=9, 1010=A, 1011=B, 1100=C, 1101=D, 1110=E, 1111=F
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * DATA SIZE PREFIXES (Important: SI vs Binary!)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 *   ┌────────────────────┬────────────────────┬──────────────────────────────┐
 *   │ SI (Decimal)       │ Binary (IEC)       │ Difference                   │
 *   ├────────────────────┼────────────────────┼──────────────────────────────┤
 *   │ 1 KB = 10³ = 1000  │ 1 KiB = 2¹⁰ = 1024 │ KiB is 2.4% larger          │
 *   │ 1 MB = 10⁶         │ 1 MiB = 2²⁰        │ MiB is 4.9% larger          │
 *   │ 1 GB = 10⁹         │ 1 GiB = 2³⁰        │ GiB is 7.4% larger          │
 *   │ 1 TB = 10¹²        │ 1 TiB = 2⁴⁰        │ TiB is 10.0% larger         │
 *   └────────────────────┴────────────────────┴──────────────────────────────┘
 *   
 *   Memory/cache sizes typically use binary (KiB, MiB, GiB)
 *   Storage manufacturers typically use SI (KB, MB, GB) - makes drives seem bigger!
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * TWO'S COMPLEMENT - Signed Integer Representation
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * For an n-bit number, the range is: -2^(n-1) to 2^(n-1) - 1
 * 
 *   8-bit:  -128 to 127
 *   16-bit: -32,768 to 32,767
 *   32-bit: -2,147,483,648 to 2,147,483,647
 * 
 * INTERPRETATION:
 *   - MSB (leftmost bit) is the SIGN BIT: 0 = positive, 1 = negative
 *   - For negative numbers: Value = -2^(n-1) + (remaining bits as positive)
 * 
 * TO NEGATE (find -x):
 *   1. Flip all bits (one's complement)
 *   2. Add 1
 *   
 *   Example: -5 in 8-bit
 *     5 = 00000101
 *     Flip: 11111010
 *     Add 1: 11111011 = -5
 *   
 *   Verify: -128 + 64 + 32 + 16 + 8 + 0 + 2 + 1 = -128 + 123 = -5 ✓
 * 
 * WHY TWO'S COMPLEMENT?
 *   - Only one representation of zero (unlike sign-magnitude)
 *   - Addition works the same for positive and negative numbers!
 *   - Subtraction is just addition of the negated number
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * SIGN EXTENSION
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * When converting a smaller signed number to a larger size, copy the sign bit:
 * 
 *   8-bit to 16-bit:
 *     +5: 00000101 → 0000000000000101 (fill with 0s)
 *     -5: 11111011 → 1111111111111011 (fill with 1s)
 * 
 * WHY IT WORKS:
 *   For positive: Adding leading zeros doesn't change value
 *   For negative: -2^7 + x = -2^15 + (2^14 + 2^13 + ... + 2^7) + x
 *                 The extra 1s sum to exactly 2^15 - 2^7, preserving value
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * OVERFLOW
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Overflow occurs when the result is too large/small to fit in the available bits.
 * 
 * SIGNED OVERFLOW:
 *   - Adding two positives and getting negative
 *   - Adding two negatives and getting positive
 *   
 *   Example (8-bit): 100 + 50
 *     01100100 (100)
 *   + 00110010 (50)
 *   = 10010110 (-106 in two's complement!)  ← OVERFLOW!
 * 
 * UNSIGNED OVERFLOW:
 *   - Carry out of the MSB
 *   
 *   Example (8-bit): 200 + 100
 *     11001000 (200)
 *   + 01100100 (100)
 *   = 100101100 (9 bits, but we only have 8)
 *   = 00101100 (44)  ← OVERFLOW!
 * 
 * DETECTION:
 *   Signed: Overflow if both operands same sign and result different sign
 *   Unsigned: Overflow if carry out of MSB
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * LOGICAL AND ARITHMETIC OPERATIONS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * BITWISE OPERATIONS (operate on each bit independently):
 * 
 *   AND (&): 1 only if both bits are 1
 *     1010 & 1100 = 1000
 *     Use: Masking (extracting specific bits)
 * 
 *   OR (|): 1 if either bit is 1
 *     1010 | 1100 = 1110
 *     Use: Setting specific bits
 * 
 *   XOR (^): 1 if bits are different
 *     1010 ^ 1100 = 0110
 *     Use: Toggling bits, simple encryption
 * 
 *   NOT (~): Flip all bits
 *     ~1010 = 0101
 * 
 * SHIFT OPERATIONS:
 * 
 *   Logical Left Shift (<<): Shift bits left, fill with 0s
 *     00001010 << 2 = 00101000
 *     Effect: Multiply by 2^n
 * 
 *   Logical Right Shift (>>>): Shift bits right, fill with 0s
 *     10100000 >>> 2 = 00101000
 *     Effect: Unsigned divide by 2^n
 * 
 *   Arithmetic Right Shift (>>): Shift bits right, fill with SIGN BIT
 *     10100000 >> 2 = 11101000  (preserves sign!)
 *     Effect: Signed divide by 2^n
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * FLOATING POINT (IEEE 754)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Format: (-1)^S × (1.M) × 2^(E-bias)
 * 
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │ Single Precision (32-bit, "float"):                                    │
 *   │ ┌───┬──────────┬───────────────────────┐                               │
 *   │ │ S │ Exponent │      Mantissa         │                               │
 *   │ │1b │  8 bits  │       23 bits         │                               │
 *   │ └───┴──────────┴───────────────────────┘                               │
 *   │ Bias = 127, Range: ±1.18×10⁻³⁸ to ±3.4×10³⁸                            │
 *   ├─────────────────────────────────────────────────────────────────────────┤
 *   │ Double Precision (64-bit, "double"):                                   │
 *   │ ┌───┬───────────┬──────────────────────────────────────────┐           │
 *   │ │ S │ Exponent  │              Mantissa                    │           │
 *   │ │1b │  11 bits  │               52 bits                    │           │
 *   │ └───┴───────────┴──────────────────────────────────────────┘           │
 *   │ Bias = 1023, Range: ±2.2×10⁻³⁰⁸ to ±1.8×10³⁰⁸                          │
 *   └─────────────────────────────────────────────────────────────────────────┘
 * 
 * CONVERSION EXAMPLE: 12.375 to single precision
 *   1. Convert to binary: 12.375 = 1100.011
 *   2. Normalize: 1.100011 × 2³
 *   3. Sign: 0 (positive)
 *   4. Exponent: 3 + 127 = 130 = 10000010
 *   5. Mantissa: 10001100000000000000000 (implicit 1 not stored)
 *   Result: 0 10000010 10001100000000000000000
 * 
 * SPECIAL VALUES:
 *   - Zero: Exponent = 0, Mantissa = 0
 *   - Infinity: Exponent = all 1s, Mantissa = 0
 *   - NaN: Exponent = all 1s, Mantissa ≠ 0
 *   - Denormalized: Exponent = 0, Mantissa ≠ 0 (very small numbers)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class NumberSystems {
    
    // ==================== BASE CONVERSION ====================
    
    /**
     * Convert decimal to binary string.
     */
    public static String decimalToBinary(int decimal) {
        if (decimal == 0) return "0";
        StringBuilder binary = new StringBuilder();
        int num = Math.abs(decimal);
        while (num > 0) {
            binary.insert(0, num % 2);
            num /= 2;
        }
        return (decimal < 0 ? "-" : "") + binary.toString();
    }
    
    /**
     * Convert decimal to binary with fixed width (two's complement for negative).
     */
    public static String decimalToBinary(int decimal, int bits) {
        StringBuilder sb = new StringBuilder();
        for (int i = bits - 1; i >= 0; i--) {
            sb.append((decimal >> i) & 1);
        }
        return sb.toString();
    }
    
    /**
     * Convert binary string to decimal.
     */
    public static int binaryToDecimal(String binary) {
        return Integer.parseInt(binary, 2);
    }
    
    /**
     * Convert binary string to decimal (signed, two's complement).
     */
    public static int binaryToDecimalSigned(String binary) {
        int bits = binary.length();
        if (binary.charAt(0) == '0') {
            return Integer.parseInt(binary, 2);
        } else {
            // Negative number: -2^(n-1) + (remaining bits)
            return Integer.parseInt(binary, 2) - (1 << bits);
        }
    }
    
    /**
     * Convert decimal to hexadecimal.
     */
    public static String decimalToHex(int decimal) {
        return Integer.toHexString(decimal).toUpperCase();
    }
    
    /**
     * Convert hexadecimal to decimal.
     */
    public static int hexToDecimal(String hex) {
        return Integer.parseInt(hex.replace("0x", "").replace("0X", ""), 16);
    }
    
    // ==================== TWO'S COMPLEMENT ====================
    
    /**
     * Negate a number using two's complement.
     */
    public static int negate(int value) {
        return ~value + 1;  // Flip bits and add 1
    }
    
    /**
     * Demonstrate two's complement negation.
     */
    public static String demonstrateTwosComplement(int value, int bits) {
        StringBuilder sb = new StringBuilder();
        String original = decimalToBinary(value, bits);
        String flipped = decimalToBinary(~value, bits);
        int negated = negate(value);
        String negatedBinary = decimalToBinary(negated, bits);
        
        sb.append(String.format("Two's complement of %d (%d-bit):\n", value, bits));
        sb.append(String.format("  Original:    %s (%d)\n", original, value));
        sb.append(String.format("  Flip bits:   %s (%d)\n", flipped, ~value));
        sb.append(String.format("  Add 1:       %s (%d)\n", negatedBinary, negated));
        
        return sb.toString();
    }
    
    // ==================== SIGN EXTENSION ====================
    
    /**
     * Sign-extend a value from one bit width to another.
     */
    public static int signExtend(int value, int fromBits, int toBits) {
        // Check if sign bit is set
        boolean negative = (value & (1 << (fromBits - 1))) != 0;
        
        if (negative) {
            // Fill upper bits with 1s
            int mask = ((1 << toBits) - 1) ^ ((1 << fromBits) - 1);
            return value | mask;
        } else {
            // Upper bits already 0
            return value;
        }
    }
    
    /**
     * Demonstrate sign extension.
     */
    public static String demonstrateSignExtension(int value, int fromBits, int toBits) {
        StringBuilder sb = new StringBuilder();
        String original = decimalToBinary(value, fromBits);
        int extended = signExtend(value, fromBits, toBits);
        String extendedBinary = decimalToBinary(extended, toBits);
        
        sb.append(String.format("Sign extending %d from %d to %d bits:\n", 
                               binaryToDecimalSigned(original), fromBits, toBits));
        sb.append(String.format("  Original (%d-bit): %s = %d\n", 
                               fromBits, original, binaryToDecimalSigned(original)));
        sb.append(String.format("  Extended (%d-bit): %s = %d\n", 
                               toBits, extendedBinary, binaryToDecimalSigned(extendedBinary)));
        
        return sb.toString();
    }
    
    // ==================== OVERFLOW DETECTION ====================
    
    /**
     * Check for signed addition overflow.
     */
    public static boolean signedAddOverflow(int a, int b, int result) {
        // Overflow if both operands same sign and result different sign
        boolean aPositive = a >= 0;
        boolean bPositive = b >= 0;
        boolean resultPositive = result >= 0;
        
        return (aPositive == bPositive) && (aPositive != resultPositive);
    }
    
    /**
     * Demonstrate overflow.
     */
    public static String demonstrateOverflow() {
        StringBuilder sb = new StringBuilder();
        sb.append("Overflow demonstration (8-bit signed):\n\n");
        
        // Overflow case: 100 + 50 in 8-bit
        byte a = 100, b = 50;
        byte result = (byte)(a + b);  // Will overflow
        
        sb.append(String.format("  %d + %d = %d (in 8-bit: %d)\n", 
                               a, b, (int)a + (int)b, result));
        sb.append(String.format("    %s\n", decimalToBinary(a, 8)));
        sb.append(String.format("  + %s\n", decimalToBinary(b, 8)));
        sb.append(String.format("  = %s (OVERFLOW! MSB changed)\n", decimalToBinary(result, 8)));
        
        sb.append("\nNo overflow case: 50 + 30\n");
        byte c = 50, d = 30;
        byte result2 = (byte)(c + d);
        sb.append(String.format("  %d + %d = %d\n", c, d, result2));
        sb.append(String.format("    %s\n", decimalToBinary(c, 8)));
        sb.append(String.format("  + %s\n", decimalToBinary(d, 8)));
        sb.append(String.format("  = %s (OK)\n", decimalToBinary(result2, 8)));
        
        return sb.toString();
    }
    
    // ==================== FLOATING POINT ====================
    
    /**
     * Decompose a float into sign, exponent, and mantissa.
     */
    public static String floatComponents(float value) {
        int bits = Float.floatToRawIntBits(value);
        int sign = (bits >> 31) & 1;
        int exponent = (bits >> 23) & 0xFF;
        int mantissa = bits & 0x7FFFFF;
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Float %.6f decomposition:\n", value));
        sb.append(String.format("  Binary: %s\n", decimalToBinary(bits, 32)));
        sb.append(String.format("  Sign (1 bit):     %d (%s)\n", sign, sign == 0 ? "positive" : "negative"));
        sb.append(String.format("  Exponent (8 bit): %s = %d (unbiased: %d)\n", 
                               decimalToBinary(exponent, 8), exponent, exponent - 127));
        sb.append(String.format("  Mantissa (23 bit): %s\n", decimalToBinary(mantissa, 23)));
        sb.append(String.format("  Value = (-1)^%d × 1.%s × 2^%d\n", 
                               sign, decimalToBinary(mantissa, 23), exponent - 127));
        
        return sb.toString();
    }
    
    /**
     * Convert a simple decimal to IEEE 754 single precision.
     */
    public static String decimalToFloatBinary(double value) {
        float f = (float) value;
        int bits = Float.floatToRawIntBits(f);
        return decimalToBinary(bits, 32);
    }
    
    // ==================== BITWISE OPERATIONS DEMO ====================
    
    /**
     * Demonstrate bitwise operations.
     */
    public static String demonstrateBitwise() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bitwise Operations (8-bit):\n\n");
        
        int a = 0b10101010;  // 170
        int b = 0b11001100;  // 204
        
        sb.append(String.format("  A = %s (%d)\n", decimalToBinary(a, 8), a));
        sb.append(String.format("  B = %s (%d)\n\n", decimalToBinary(b, 8), b));
        
        sb.append(String.format("  A AND B = %s (%d)\n", decimalToBinary(a & b, 8), a & b));
        sb.append(String.format("  A OR  B = %s (%d)\n", decimalToBinary(a | b, 8), a | b));
        sb.append(String.format("  A XOR B = %s (%d)\n", decimalToBinary(a ^ b, 8), a ^ b));
        sb.append(String.format("  NOT A   = %s (%d)\n\n", decimalToBinary((~a) & 0xFF, 8), (~a) & 0xFF));
        
        sb.append("Shift Operations:\n");
        int x = 0b00010100;  // 20
        sb.append(String.format("  x         = %s (%d)\n", decimalToBinary(x, 8), x));
        sb.append(String.format("  x << 2    = %s (%d) [multiply by 4]\n", 
                               decimalToBinary((x << 2) & 0xFF, 8), (x << 2) & 0xFF));
        sb.append(String.format("  x >> 2    = %s (%d) [divide by 4]\n", 
                               decimalToBinary(x >> 2, 8), x >> 2));
        
        return sb.toString();
    }
    
    // ==================== MAIN DEMO ====================
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("  NUMBER SYSTEMS DEMONSTRATION");
        System.out.println("═══════════════════════════════════════════════════════════════\n");
        
        // Base conversion
        System.out.println("Base Conversion:");
        System.out.println("  42 decimal = " + decimalToBinary(42) + " binary");
        System.out.println("  42 decimal = 0x" + decimalToHex(42) + " hex");
        System.out.println("  0xFF hex = " + hexToDecimal("FF") + " decimal\n");
        
        // Two's complement
        System.out.println(demonstrateTwosComplement(5, 8));
        System.out.println(demonstrateTwosComplement(-5, 8));
        
        // Sign extension
        System.out.println(demonstrateSignExtension(0b00000101, 8, 16));  // +5
        System.out.println(demonstrateSignExtension(0b11111011, 8, 16));  // -5
        
        // Overflow
        System.out.println(demonstrateOverflow());
        
        // Bitwise
        System.out.println(demonstrateBitwise());
        
        // Floating point
        System.out.println(floatComponents(12.375f));
        System.out.println(floatComponents(-0.5f));
    }
}

