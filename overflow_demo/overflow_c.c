/*
 * Arithmetic Overflow in C
 * C does NOT check for overflow - it wraps around silently
 * This is undefined behavior for signed integers, but defined for unsigned
 * 
 * Compile with: gcc overflow_c.c -o overflow_c
 */

#include <stdio.h>
#include <limits.h>
#include <stdint.h>

int main(void) {
    printf("=== Integer Overflow in C ===\n\n");
    
    // Signed integer overflow (undefined behavior, but typically wraps)
    printf("--- Signed Integer Overflow ---\n");
    int max_int = INT_MAX;
    printf("INT_MAX = %d\n", max_int);
    printf("INT_MAX + 1 = %d (wraps to negative)\n", max_int + 1);
    printf("INT_MAX + 100 = %d\n", max_int + 100);
    
    int min_int = INT_MIN;
    printf("\nINT_MIN = %d\n", min_int);
    printf("INT_MIN - 1 = %d (wraps to positive)\n", min_int - 1);
    
    // Unsigned integer overflow (well-defined: wraps around)
    printf("\n--- Unsigned Integer Overflow ---\n");
    unsigned int max_uint = UINT_MAX;
    printf("UINT_MAX = %u\n", max_uint);
    printf("UINT_MAX + 1 = %u (wraps to 0)\n", max_uint + 1);
    printf("UINT_MAX + 100 = %u\n", max_uint + 100);
    
    unsigned int zero = 0;
    printf("\n0 - 1 = %u (wraps to UINT_MAX)\n", zero - 1);
    
    // Multiplication overflow
    printf("\n--- Multiplication Overflow ---\n");
    int a = 1000000;
    int b = 1000000;
    int result = a * b;
    printf("%d * %d = %d (overflow!)\n", a, b, result);
    printf("Expected: %lld\n", (long long)a * (long long)b);
    
    // 64-bit integer overflow
    printf("\n--- 64-bit Integer Overflow ---\n");
    int64_t max_int64 = INT64_MAX;
    printf("INT64_MAX = %lld\n", (long long)max_int64);
    printf("INT64_MAX + 1 = %lld (wraps)\n", (long long)(max_int64 + 1));
    
    // Overflow in a loop
    printf("\n--- Overflow in Loop ---\n");
    int counter = INT_MAX - 5;
    printf("Starting counter at INT_MAX - 5 = %d\n", counter);
    for (int i = 0; i < 10; i++) {
        counter++;
        printf("  Iteration %d: counter = %d\n", i + 1, counter);
    }
    
    // Floating point overflow (infinity)
    printf("\n--- Floating Point Overflow ---\n");
    float max_float = 3.4028235e38f;  // Approx FLT_MAX
    printf("Large float: %e\n", max_float);
    printf("Large float * 10 = %e (infinity)\n", max_float * 10.0f);
    
    float inf = max_float * 10.0f;
    printf("inf == inf: %d\n", inf == inf);
    printf("inf > 0: %d\n", inf > 0);
    
    printf("\n=== Summary ===\n");
    printf("C does NOT detect integer overflow\n");
    printf("- Signed overflow: Undefined behavior (typically wraps)\n");
    printf("- Unsigned overflow: Well-defined (wraps around)\n");
    printf("- Floating point overflow: Results in infinity\n");
    
    return 0;
}

