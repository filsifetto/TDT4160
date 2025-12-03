/*
 * Thread Overflow Program (C version)
 * Creates threads continuously until the system limit is reached.
 * 
 * Compile with: gcc -pthread thread_overflow.c -o thread_overflow
 */

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>

static volatile int thread_count = 0;
static pthread_mutex_t count_mutex = PTHREAD_MUTEX_INITIALIZER;

void* worker_thread(void* arg) {
    int thread_id = *(int*)arg;
    free(arg); // Free the allocated thread_id
    
    // Keep thread alive indefinitely
    while (1) {
        sleep(1);
    }
    
    return NULL;
}

int main() {
    int thread_id = 0;
    pthread_t thread;
    int result;
    
    printf("Starting thread overflow program...\n");
    printf("This will create threads until the system limit is reached.\n");
    printf("Press Ctrl+C to stop early.\n\n");
    
    while (1) {
        // Allocate memory for thread_id to pass to thread
        int* thread_id_ptr = malloc(sizeof(int));
        if (!thread_id_ptr) {
            fprintf(stderr, "Failed to allocate memory for thread_id\n");
            break;
        }
        *thread_id_ptr = thread_id;
        
        // Create a new thread
        result = pthread_create(&thread, NULL, worker_thread, thread_id_ptr);
        
        if (result != 0) {
            free(thread_id_ptr);
            fprintf(stderr, "\nFailed to create thread %d: %s (errno: %d)\n", 
                    thread_id, strerror(result), result);
            printf("Total threads created before failure: %d\n", thread_id);
            break;
        }
        
        // Detach thread so resources are freed when thread exits
        pthread_detach(thread);
        
        thread_id++;
        
        // Print progress every 100 threads
        if (thread_id % 100 == 0) {
            printf("Created %d threads...\n", thread_id);
            fflush(stdout);
        }
    }
    
    printf("\nProgram exiting. Threads will be cleaned up.\n");
    return 0;
}

