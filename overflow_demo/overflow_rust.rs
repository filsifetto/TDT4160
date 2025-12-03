/*
 * Arithmetic Overflow in Rust
 * Rust panics on overflow in debug mode, wraps in release mode
 * Can use checked/wrapping/saturating operations for explicit behavior
 * 
 * Compile with: rustc overflow_rust.rs -o overflow_rust
 * Or: cargo build --release (for release mode - wrapping behavior)
 */

fn main() {
    println!("=== Integer Overflow in Rust ===\n");
    
    // In debug mode, this would panic. In release mode, it wraps.
    println!("--- Integer Overflow (Release Mode: Wraps) ---");
    let max_i32 = i32::MAX;
    println!("i32::MAX = {}", max_i32);
    
    // Using wrapping_add to explicitly wrap
    let wrapped = max_i32.wrapping_add(1);
    println!("i32::MAX.wrapping_add(1) = {} (wraps)", wrapped);
    
    let min_i32 = i32::MIN;
    println!("\ni32::MIN = {}", min_i32);
    println!("i32::MIN.wrapping_sub(1) = {} (wraps)", min_i32.wrapping_sub(1));
    
    // Checked operations return Option
    println!("\n--- Checked Operations ---");
    match max_i32.checked_add(1) {
        Some(result) => println!("checked_add(MAX, 1) = Some({})", result),
        None => println!("checked_add(MAX, 1) = None (overflow detected!)"),
    }
    
    match max_i32.checked_mul(2) {
        Some(result) => println!("checked_mul(MAX, 2) = Some({})", result),
        None => println!("checked_mul(MAX, 2) = None (overflow detected!)"),
    }
    
    // Saturating operations clamp at min/max
    println!("\n--- Saturating Operations ---");
    println!("saturating_add(MAX, 1) = {} (clamped to MAX)", max_i32.saturating_add(1));
    println!("saturating_add(MAX, 100) = {} (clamped to MAX)", max_i32.saturating_add(100));
    println!("saturating_sub(MIN, 1) = {} (clamped to MIN)", min_i32.saturating_sub(1));
    
    // Overflowing operations return (result, overflow_flag)
    println!("\n--- Overflowing Operations ---");
    let (result, overflowed) = max_i32.overflowing_add(1);
    println!("overflowing_add(MAX, 1) = ({}, {})", result, overflowed);
    
    let (result, overflowed) = max_i32.overflowing_mul(2);
    println!("overflowing_mul(MAX, 2) = ({}, {})", result, overflowed);
    
    // Unsigned overflow
    println!("\n--- Unsigned Integer Overflow ---");
    let max_u32 = u32::MAX;
    println!("u32::MAX = {}", max_u32);
    println!("u32::MAX.wrapping_add(1) = {} (wraps to 0)", max_u32.wrapping_add(1));
    
    // Floating point overflow
    println!("\n--- Floating Point Overflow ---");
    let max_f32 = f32::MAX;
    println!("f32::MAX = {}", max_f32);
    println!("f32::MAX * 2.0 = {} (infinity)", max_f32 * 2.0);
    println!("f32::MAX * 2.0 is infinite: {}", (max_f32 * 2.0).is_infinite());
    
    // Overflow in a loop
    println!("\n--- Overflow in Loop (using wrapping) ---");
    let mut counter = i32::MAX - 5;
    println!("Starting counter at i32::MAX - 5 = {}", counter);
    for i in 0..10 {
        counter = counter.wrapping_add(1);
        println!("  Iteration {}: counter = {}", i + 1, counter);
    }
    
    println!("\n=== Summary ===");
    println!("Rust handles overflow:");
    println!("- Debug mode: Panics on overflow");
    println!("- Release mode: Wraps silently (for performance)");
    println!("- Checked operations: Return Option (Some/None)");
    println!("- Wrapping operations: Explicitly wrap");
    println!("- Saturating operations: Clamp at min/max");
    println!("- Overflowing operations: Return (result, overflow_flag)");
}

