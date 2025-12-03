/*
 * Arithmetic Overflow in Java
 * Java integers wrap around silently (like C)
 * No exception is thrown for integer overflow
 * 
 * Compile with: javac overflow_java.java
 * Run with: java OverflowJava
 */

public class OverflowJava {
    public static void main(String[] args) {
        System.out.println("=== Integer Overflow in Java ===\n");
        
        // Integer overflow (wraps around)
        System.out.println("--- Integer Overflow ---");
        int maxInt = Integer.MAX_VALUE;
        System.out.println("Integer.MAX_VALUE = " + maxInt);
        System.out.println("Integer.MAX_VALUE + 1 = " + (maxInt + 1) + " (wraps to negative)");
        System.out.println("Integer.MAX_VALUE + 100 = " + (maxInt + 100));
        
        int minInt = Integer.MIN_VALUE;
        System.out.println("\nInteger.MIN_VALUE = " + minInt);
        System.out.println("Integer.MIN_VALUE - 1 = " + (minInt - 1) + " (wraps to positive)");
        
        // Long overflow
        System.out.println("\n--- Long Overflow ---");
        long maxLong = Long.MAX_VALUE;
        System.out.println("Long.MAX_VALUE = " + maxLong);
        System.out.println("Long.MAX_VALUE + 1 = " + (maxLong + 1) + " (wraps)");
        
        // Multiplication overflow
        System.out.println("\n--- Multiplication Overflow ---");
        int a = 1000000;
        int b = 1000000;
        int result = a * b;
        System.out.println(a + " * " + b + " = " + result + " (overflow!)");
        System.out.println("Expected: " + ((long)a * (long)b));
        
        // Overflow in a loop
        System.out.println("\n--- Overflow in Loop ---");
        int counter = Integer.MAX_VALUE - 5;
        System.out.println("Starting counter at Integer.MAX_VALUE - 5 = " + counter);
        for (int i = 0; i < 10; i++) {
            counter++;
            System.out.println("  Iteration " + (i + 1) + ": counter = " + counter);
        }
        
        // Floating point overflow
        System.out.println("\n--- Floating Point Overflow ---");
        double maxDouble = Double.MAX_VALUE;
        System.out.println("Double.MAX_VALUE = " + maxDouble);
        System.out.println("Double.MAX_VALUE * 2 = " + (maxDouble * 2) + " (infinity)");
        System.out.println("Double.isInfinite(Double.MAX_VALUE * 2) = " + 
                          Double.isInfinite(maxDouble * 2));
        
        // Using Math methods to detect overflow
        System.out.println("\n--- Detecting Overflow with Math.addExact ---");
        try {
            int safe = Math.addExact(maxInt, 0);
            System.out.println("Math.addExact(MAX_VALUE, 0) = " + safe);
            int overflow = Math.addExact(maxInt, 1);
            System.out.println("Math.addExact(MAX_VALUE, 1) = " + overflow);
        } catch (ArithmeticException e) {
            System.out.println("Caught ArithmeticException: " + e.getMessage());
        }
        
        try {
            int overflow = Math.multiplyExact(1000000, 1000000);
            System.out.println("Math.multiplyExact(1000000, 1000000) = " + overflow);
        } catch (ArithmeticException e) {
            System.out.println("Caught ArithmeticException: " + e.getMessage());
        }
        
        System.out.println("\n=== Summary ===");
        System.out.println("Java handles overflow:");
        System.out.println("- Integers/Longs: Wrap around silently (like C)");
        System.out.println("- Floating point: Can overflow to infinity");
        System.out.println("- Math.addExact/multiplyExact: Throw ArithmeticException on overflow");
    }
}

