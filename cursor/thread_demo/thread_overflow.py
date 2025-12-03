#!/usr/bin/env python3
"""
Thread Overflow Program
Creates threads continuously until the system limit is reached.
"""

import threading
import time
import sys

# Global counter for tracking thread creation
thread_count = 0
thread_count_lock = threading.Lock()

def worker_thread(thread_id):
    """Worker function that keeps the thread alive."""
    global thread_count
    try:
        # Keep thread alive indefinitely
        while True:
            time.sleep(1)
    except Exception as e:
        print(f"Thread {thread_id} encountered error: {e}")
    finally:
        with thread_count_lock:
            thread_count -= 1

def main():
    global thread_count
    
    print("Starting thread overflow program...")
    print("This will create threads until the system limit is reached.")
    print("Press Ctrl+C to stop early.\n")
    
    threads = []
    thread_id = 0
    
    try:
        while True:
            try:
                # Create a new thread
                thread = threading.Thread(
                    target=worker_thread,
                    args=(thread_id,),
                    daemon=True  # Daemon threads won't prevent program exit
                )
                thread.start()
                threads.append(thread)
                
                thread_id += 1
                with thread_count_lock:
                    thread_count += 1
                
                # Print progress every 100 threads
                if thread_id % 100 == 0:
                    print(f"Created {thread_id} threads... (Active: {thread_count})")
                    
            except RuntimeError as e:
                print(f"\nFailed to create thread {thread_id}: {e}")
                print(f"Total threads created before failure: {thread_id}")
                break
            except Exception as e:
                print(f"\nUnexpected error creating thread {thread_id}: {e}")
                print(f"Total threads created before failure: {thread_id}")
                break
                
    except KeyboardInterrupt:
        print(f"\n\nInterrupted by user.")
        print(f"Total threads created: {thread_id}")
        print(f"Active threads: {thread_count}")
    
    # Wait a bit to see the final count
    print("\nWaiting 2 seconds to check final thread count...")
    time.sleep(2)
    
    with thread_count_lock:
        print(f"Final active thread count: {thread_count}")
    
    print("\nProgram exiting. Threads will be cleaned up.")

if __name__ == "__main__":
    main()

