#!/usr/bin/env python3
"""
Arithmetic Overflow in Python
Python automatically promotes integers to arbitrary precision (bigint)
So overflow doesn't occur for integers - they can grow indefinitely!
However, floating point can still overflow to infinity.
"""

import sys

def main():
    print("=== Integer Overflow in Python ===\n")
    
    # Python integers are arbitrary precision - no overflow!
    print("--- Integer Overflow Attempt ---")
    max_int = sys.maxsize
    print(f"sys.maxsize = {max_int}")
    print(f"sys.maxsize + 1 = {max_int + 1} (no overflow!)")
    print(f"sys.maxsize + 100 = {max_int + 100}")
    print(f"Type: {type(max_int + 1)}")
    
    # Very large numbers
    print("\n--- Very Large Numbers ---")
    huge = 10 ** 1000
    print(f"10 ** 1000 = {huge}")
    print(f"Type: {type(huge)}")
    print(f"10 ** 1000 + 1 = {huge + 1}")
    
    # Multiplication
    print("\n--- Large Multiplication ---")
    a = 10 ** 500
    b = 10 ** 500
    result = a * b
    print(f"10^500 * 10^500 = {result}")
    print(f"Length in digits: {len(str(result))}")
    
    # Negative overflow attempt
    print("\n--- Negative Overflow Attempt ---")
    min_int = -sys.maxsize - 1
    print(f"-sys.maxsize - 1 = {min_int}")
    print(f"(-sys.maxsize - 1) - 1 = {min_int - 1} (no overflow!)")
    
    # Floating point CAN overflow
    print("\n--- Floating Point Overflow ---")
    import math
    max_float = sys.float_info.max
    print(f"sys.float_info.max = {max_float}")
    print(f"max_float * 2 = {max_float * 2} (infinity)")
    print(f"math.isinf(max_float * 2) = {math.isinf(max_float * 2)}")
    
    # NumPy arrays DO have fixed-size integers (can overflow)
    print("\n--- NumPy Fixed-Size Integers (Can Overflow) ---")
    try:
        import numpy as np
        arr = np.array([2147483647], dtype=np.int32)  # INT_MAX
        print(f"NumPy int32 max: {arr[0]}")
        arr[0] += 1
        print(f"After +1: {arr[0]} (wraps like C!)")
        print(f"Type: {arr.dtype}")
    except ImportError:
        print("NumPy not available (install with: pip install numpy)")
    
    # Overflow in a loop
    print("\n--- Overflow in Loop ---")
    counter = sys.maxsize - 5
    print(f"Starting counter at sys.maxsize - 5 = {counter}")
    for i in range(10):
        counter += 1
        print(f"  Iteration {i+1}: counter = {counter}")
    
    print("\n=== Summary ===")
    print("Python handles overflow differently:")
    print("- Integers: No overflow! Automatically promoted to bigint")
    print("- Floating point: Can overflow to infinity")
    print("- NumPy arrays: Fixed-size types can overflow (like C)")

if __name__ == "__main__":
    main()

