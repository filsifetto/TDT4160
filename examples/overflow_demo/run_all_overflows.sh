#!/bin/bash
# Run all overflow demonstration programs

echo "=========================================="
echo "Arithmetic Overflow Demonstrations"
echo "=========================================="
echo ""

# C
if command -v gcc &> /dev/null; then
    echo "=== Running C Program ==="
    gcc -o overflow_c overflow_c.c 2>/dev/null
    if [ $? -eq 0 ]; then
        ./overflow_c
        echo ""
    else
        echo "Failed to compile C program"
        echo ""
    fi
else
    echo "gcc not found, skipping C program"
    echo ""
fi

# Python
if command -v python3 &> /dev/null; then
    echo "=== Running Python Program ==="
    python3 overflow_python.py
    echo ""
else
    echo "python3 not found, skipping Python program"
    echo ""
fi

# Java
if command -v javac &> /dev/null && command -v java &> /dev/null; then
    echo "=== Running Java Program ==="
    javac OverflowJava.java 2>/dev/null
    if [ $? -eq 0 ]; then
        java OverflowJava
        echo ""
    else
        echo "Failed to compile Java program"
        echo ""
    fi
else
    echo "javac/java not found, skipping Java program"
    echo ""
fi

# Rust
if command -v rustc &> /dev/null; then
    echo "=== Running Rust Program ==="
    rustc -o overflow_rust overflow_rust.rs 2>/dev/null
    if [ $? -eq 0 ]; then
        ./overflow_rust
        echo ""
    else
        echo "Failed to compile Rust program"
        echo ""
    fi
else
    echo "rustc not found, skipping Rust program"
    echo ""
fi

echo "=========================================="
echo "All demonstrations complete!"
echo "=========================================="

