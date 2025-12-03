# Arithmetic Overflow Comparison Across Languages

This document compares how different programming languages handle arithmetic overflow.

## Summary Table

| Language | Integer Overflow Behavior | Detection | Notes |
|----------|--------------------------|-----------|-------|
| **C** | Wraps silently | No | Undefined behavior for signed, defined for unsigned |
| **Python** | No overflow (bigint) | N/A | Automatically promotes to arbitrary precision |
| **Java** | Wraps silently | Optional (Math.addExact) | Can use Math methods to detect |
| **Rust** | Panics (debug) / Wraps (release) | Yes (checked ops) | Multiple explicit overflow handling methods |

## Detailed Comparison

### C
- **Behavior**: Integer overflow wraps around silently
- **Signed integers**: Undefined behavior (but typically wraps)
- **Unsigned integers**: Well-defined wrapping behavior
- **Detection**: No built-in detection
- **Example**: `INT_MAX + 1` wraps to `INT_MIN`

### Python
- **Behavior**: No integer overflow possible!
- **Mechanism**: Integers automatically promote to arbitrary precision (bigint)
- **Performance**: Slower for very large numbers, but no overflow
- **Exception**: NumPy arrays use fixed-size types and can overflow
- **Example**: `sys.maxsize + 1` just works, no wrapping

### Java
- **Behavior**: Wraps silently (like C)
- **Detection**: Optional via `Math.addExact()`, `Math.multiplyExact()` which throw `ArithmeticException`
- **Floating point**: Can overflow to `Infinity`
- **Example**: `Integer.MAX_VALUE + 1` wraps to `Integer.MIN_VALUE`

### Rust
- **Debug mode**: Panics on overflow (safety-first)
- **Release mode**: Wraps silently (performance)
- **Explicit methods**:
  - `checked_add()`: Returns `Option<T>` (Some/None)
  - `wrapping_add()`: Explicitly wraps
  - `saturating_add()`: Clamps at min/max
  - `overflowing_add()`: Returns `(result, overflow_flag)`
- **Example**: `i32::MAX.checked_add(1)` returns `None`

## Running the Programs

### Individual Programs
```bash
# C
gcc overflow_c.c -o overflow_c && ./overflow_c

# Python
python3 overflow_python.py

# Java
javac OverflowJava.java && java OverflowJava

# Rust
rustc overflow_rust.rs -o overflow_rust && ./overflow_rust
```

### Run All
```bash
./run_all_overflows.sh
```

## Key Takeaways

1. **C/Java**: Silent wrapping - can lead to subtle bugs
2. **Python**: No overflow for integers - safe but potentially slower
3. **Rust**: Best of both worlds - safety in debug, performance in release, explicit control
4. **All languages**: Floating point can overflow to infinity

## Best Practices

- **C**: Use `-fsanitize=undefined` compiler flag to detect overflow
- **Java**: Use `Math.addExact()` when overflow detection is needed
- **Rust**: Use checked operations when safety is important
- **Python**: No special handling needed for integers, but be aware of NumPy arrays

