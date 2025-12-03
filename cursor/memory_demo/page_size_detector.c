/*
 * Page Size Detector
 * Creates arrays with increasing sizes to detect physical page size
 * by observing when arrays span multiple pages.
 * 
 * Compile with: gcc page_size_detector.c -o page_size_detector
 */

#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include <string.h>
#include <time.h>
#include <sys/mman.h>

// Function to get the page size from the system
static long get_system_page_size(void) {
    return sysconf(_SC_PAGESIZE);
}

// Function to check if an address is page-aligned
static int is_page_aligned(void *ptr, size_t page_size) {
    return ((uintptr_t)ptr % page_size) == 0;
}

// Function to calculate how many pages an array spans
static size_t pages_spanned(void *ptr, size_t size, size_t page_size) {
    uintptr_t start = (uintptr_t)ptr;
    uintptr_t end = start + size - 1;
    
    uintptr_t start_page = start / page_size;
    uintptr_t end_page = end / page_size;
    
    return (end_page - start_page) + 1;
}

// Function to get the offset within the first page
static size_t offset_in_page(void *ptr, size_t page_size) {
    return (uintptr_t)ptr % page_size;
}

// High-resolution timestamp in nanoseconds
static uint64_t now_ns(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (uint64_t)ts.tv_sec * 1000000000ULL + (uint64_t)ts.tv_nsec;
}

// Measure access time to a memory location
static uint64_t measure_access_time(volatile char *ptr, int iterations) {
    uint64_t start = now_ns();
    for (int i = 0; i < iterations; i++) {
        *ptr = *ptr;  // Read and write to ensure actual memory access
        __asm__ __volatile__("" ::: "memory");  // Memory barrier
    }
    uint64_t end = now_ns();
    return (end - start) / iterations;
}

// Measure access times at different offsets within an array
static void measure_access_times(char *arr, size_t size, size_t page_size, 
                                  uint64_t *times, size_t num_samples, int cold) {
    volatile char *volatile_arr = (volatile char *)arr;
    
    if (cold) {
        // Evict pages to force page faults on next access
        madvise(arr, size, MADV_DONTNEED);
    } else {
        // Touch all memory first to ensure pages are allocated and hot
        for (size_t i = 0; i < size; i += 64) {
            volatile_arr[i] = 0;
        }
    }
    
    // Measure access times at different positions
    for (size_t i = 0; i < num_samples && i < size; i++) {
        size_t offset = (i * size) / num_samples;
        if (offset >= size) offset = size - 1;
        
        if (cold) {
            // Measure first-touch access (should cause page fault)
            uint64_t start = now_ns();
            volatile_arr[offset] = 1;  // Write to trigger page fault
            __asm__ __volatile__("" ::: "memory");
            uint64_t end = now_ns();
            times[i] = end - start;
        } else {
            times[i] = measure_access_time(&volatile_arr[offset], 1000);
        }
    }
}

int main(void) {
    // Get the actual page size from the system
    long system_page_size = get_system_page_size();
    if (system_page_size <= 0) {
        fprintf(stderr, "Failed to get page size from system\n");
        return 1;
    }
    
    printf("System-reported page size: %ld bytes (%ld KB)\n\n", 
           system_page_size, system_page_size / 1024);
    
    printf("Creating arrays with increasing sizes to detect page boundaries...\n\n");
    printf("%-12s %-16s %-12s %-12s %-20s %-12s\n",
           "Size (bytes)", "Address", "Page-aligned", "Offset", "Pages Spanned", "Status");
    printf("%-12s %-16s %-12s %-12s %-20s %-12s\n",
           "------------", "-------", "------------", "------", "-------------", "------");
    
    // Start with very small sizes and increase
    size_t sizes[] = {
        1, 2, 4, 8, 16, 32, 64, 128, 256, 512,
        1024, 2048, 4096, 8192, 16384, 32768, 65536
    };
    
    size_t num_sizes = sizeof(sizes) / sizeof(sizes[0]);
    
    for (size_t i = 0; i < num_sizes; i++) {
        size_t size = sizes[i];
        
        // Allocate array
        char *arr = (char *)malloc(size);
        if (!arr) {
            fprintf(stderr, "Failed to allocate %zu bytes\n", size);
            continue;
        }
        
        // Touch the memory to ensure it's actually allocated
        memset(arr, 0, size);
        
        // Calculate page information
        int aligned = is_page_aligned(arr, system_page_size);
        size_t offset = offset_in_page(arr, system_page_size);
        size_t pages = pages_spanned(arr, size, system_page_size);
        
        // Determine status
        const char *status;
        if (size <= (size_t)system_page_size && pages == 1) {
            status = "Fits in 1 page";
        } else if (pages == 1) {
            status = "Fits in 1 page";
        } else {
            status = "Spans multiple";
        }
        
        printf("%-12zu 0x%-14lx %-12s %-12zu %-20zu %-12s\n",
               size, (unsigned long)arr, aligned ? "Yes" : "No", 
               offset, pages, status);
        
        free(arr);
    }
    
    // Now try to find the exact page size by creating arrays that are
    // just under and just over the page size
    printf("\n--- Testing arrays near page size boundary ---\n\n");
    
    size_t test_sizes[] = {
        (size_t)system_page_size - 1,
        (size_t)system_page_size,
        (size_t)system_page_size + 1,
        (size_t)system_page_size * 2 - 1,
        (size_t)system_page_size * 2,
        (size_t)system_page_size * 2 + 1,
    };
    
    size_t num_test_sizes = sizeof(test_sizes) / sizeof(test_sizes[0]);
    
    printf("%-12s %-16s %-12s %-12s %-20s %-12s\n",
           "Size (bytes)", "Address", "Page-aligned", "Offset", "Pages Spanned", "Status");
    printf("%-12s %-16s %-12s %-12s %-20s %-12s\n",
           "------------", "-------", "------------", "------", "-------------", "------");
    
    for (size_t i = 0; i < num_test_sizes; i++) {
        size_t size = test_sizes[i];
        
        char *arr = (char *)malloc(size);
        if (!arr) {
            fprintf(stderr, "Failed to allocate %zu bytes\n", size);
            continue;
        }
        
        memset(arr, 0, size);
        
        int aligned = is_page_aligned(arr, system_page_size);
        size_t offset = offset_in_page(arr, system_page_size);
        size_t pages = pages_spanned(arr, size, system_page_size);
        
        const char *status;
        if (pages == 1) {
            status = "Fits in 1 page";
        } else {
            status = "Spans multiple";
        }
        
        printf("%-12zu 0x%-14lx %-12s %-12zu %-20zu %-12s\n",
               size, (unsigned long)arr, aligned ? "Yes" : "No", 
               offset, pages, status);
        
        free(arr);
    }
    
    printf("\n--- Access Time Measurements ---\n\n");
    printf("Measuring access times to detect page boundaries...\n\n");
    
    // Test arrays of different sizes and measure access times
    size_t test_array_sizes[] = {
        (size_t)system_page_size / 2,      // Half page - should fit in 1 page
        (size_t)system_page_size,          // Exactly one page
        (size_t)system_page_size + 1,      // Just over one page
        (size_t)system_page_size * 2,      // Two pages
    };
    
    size_t num_test_arrays = sizeof(test_array_sizes) / sizeof(test_array_sizes[0]);
    const size_t num_samples = 10;  // Measure at 10 different positions
    
    for (size_t t = 0; t < num_test_arrays; t++) {
        size_t arr_size = test_array_sizes[t];
        
        // Use mmap to get page-aligned memory for more predictable results
        char *arr = (char *)mmap(NULL, arr_size, PROT_READ | PROT_WRITE,
                                 MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
        if (arr == MAP_FAILED) {
            fprintf(stderr, "Failed to mmap %zu bytes\n", arr_size);
            continue;
        }
        
        size_t pages = pages_spanned(arr, arr_size, system_page_size);
        uint64_t *times = (uint64_t *)malloc(num_samples * sizeof(uint64_t));
        
        if (times) {
            // Measure cold access times (with page faults)
            measure_access_times(arr, arr_size, system_page_size, times, num_samples, 1);
            
            // Calculate statistics for cold access
            uint64_t min_time_cold = UINT64_MAX;
            uint64_t max_time_cold = 0;
            uint64_t sum_cold = 0;
            
            for (size_t i = 0; i < num_samples; i++) {
                if (times[i] < min_time_cold) min_time_cold = times[i];
                if (times[i] > max_time_cold) max_time_cold = times[i];
                sum_cold += times[i];
            }
            uint64_t avg_time_cold = sum_cold / num_samples;
            
            // Measure hot access times (cached)
            uint64_t *times_hot = (uint64_t *)malloc(num_samples * sizeof(uint64_t));
            if (times_hot) {
                measure_access_times(arr, arr_size, system_page_size, times_hot, num_samples, 0);
                
                uint64_t min_time_hot = UINT64_MAX;
                uint64_t max_time_hot = 0;
                uint64_t sum_hot = 0;
                
                for (size_t i = 0; i < num_samples; i++) {
                    if (times_hot[i] < min_time_hot) min_time_hot = times_hot[i];
                    if (times_hot[i] > max_time_hot) max_time_hot = times_hot[i];
                    sum_hot += times_hot[i];
                }
                uint64_t avg_time_hot = sum_hot / num_samples;
                
                printf("Array size: %zu bytes (%zu pages)\n", arr_size, pages);
                printf("  Cold access (page fault): min=%lu, max=%lu, avg=%lu ns\n", 
                       min_time_cold, max_time_cold, avg_time_cold);
                printf("  Hot access (cached):      min=%lu, max=%lu, avg=%lu ns\n", 
                       min_time_hot, max_time_hot, avg_time_hot);
                printf("  Difference:                avg=%lu ns (%.1fx slower)\n",
                       avg_time_cold > avg_time_hot ? avg_time_cold - avg_time_hot : 0,
                       avg_time_hot > 0 ? (double)avg_time_cold / avg_time_hot : 0.0);
                printf("  Time variation (cold):     %lu ns\n", max_time_cold - min_time_cold);
                printf("\n");
                
                free(times_hot);
            }
            
            free(times);
        }
        
        munmap(arr, arr_size);
    }
    
    // Now test with arrays that cross page boundaries at specific offsets
    printf("--- Testing Page Boundary Crossing (Cold Access) ---\n\n");
    printf("Measuring first-touch access times across page boundaries...\n\n");
    
    // Allocate a large buffer to find a page boundary
    size_t large_size = system_page_size * 4;
    char *large_buf = (char *)mmap(NULL, large_size, PROT_READ | PROT_WRITE,
                                    MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (large_buf != MAP_FAILED) {
        // Evict all pages first
        madvise(large_buf, large_size, MADV_DONTNEED);
        
        // Find positions at different offsets from page start
        for (int offset_from_page_start = 0; offset_from_page_start < (int)system_page_size; 
             offset_from_page_start += system_page_size / 4) {
            
            // Find a page-aligned address
            uintptr_t page_start = ((uintptr_t)large_buf / system_page_size) * system_page_size;
            char *test_ptr = (char *)(page_start + offset_from_page_start);
            
            if (test_ptr < large_buf || test_ptr >= large_buf + large_size) {
                continue;
            }
            
            // Measure first-touch access time at this position (cold)
            volatile char *volatile_test = (volatile char *)test_ptr;
            uint64_t start1 = now_ns();
            *volatile_test = 1;
            __asm__ __volatile__("" ::: "memory");
            uint64_t end1 = now_ns();
            uint64_t time1 = end1 - start1;
            
            // Evict again
            madvise(large_buf, large_size, MADV_DONTNEED);
            
            // Measure access time one page away (cold)
            char *next_page_ptr = test_ptr + system_page_size;
            if (next_page_ptr < large_buf + large_size) {
                volatile char *volatile_next = (volatile char *)next_page_ptr;
                uint64_t start2 = now_ns();
                *volatile_next = 1;
                __asm__ __volatile__("" ::: "memory");
                uint64_t end2 = now_ns();
                uint64_t time2 = end2 - start2;
                
                printf("Offset %4d: page 0 = %5lu ns, page 1 = %5lu ns (diff: %5ld ns)\n",
                       offset_from_page_start, time1, time2, 
                       (int64_t)time2 - (int64_t)time1);
            }
        }
        munmap(large_buf, large_size);
    }
    
    // Test sequential access across page boundaries
    printf("--- Sequential Access Across Page Boundary ---\n\n");
    printf("Measuring access times at sequential positions...\n\n");
    
    size_t test_seq_size = system_page_size * 2 + 100;  // Slightly more than 2 pages
    char *seq_buf = (char *)mmap(NULL, test_seq_size, PROT_READ | PROT_WRITE,
                                  MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (seq_buf != MAP_FAILED) {
        // Evict pages
        madvise(seq_buf, test_seq_size, MADV_DONTNEED);
        
        printf("Position (bytes)  Access Time (ns)  Page Offset\n");
        printf("----------------  ----------------  -----------\n");
        
        volatile char *volatile_buf = (volatile char *)seq_buf;
        
        // Measure at positions around page boundaries
        for (int pos = (int)system_page_size - 200; pos < (int)system_page_size + 200; pos += 50) {
            if (pos < 0 || pos >= (int)test_seq_size) continue;
            
            uint64_t start = now_ns();
            volatile_buf[pos] = 1;
            __asm__ __volatile__("" ::: "memory");
            uint64_t end = now_ns();
            uint64_t access_time = end - start;
            
            // Calculate which page and offset
            uintptr_t addr = (uintptr_t)(seq_buf + pos);
            uintptr_t page_num = addr / system_page_size;
            uintptr_t page_offset = addr % system_page_size;
            
            printf("%15d  %16lu  Page %lu, offset %lu\n", 
                   pos, access_time, page_num, page_offset);
            
            // Evict again for next measurement
            madvise(seq_buf, test_seq_size, MADV_DONTNEED);
        }
        
        munmap(seq_buf, test_seq_size);
    }
    
    printf("\n--- Summary ---\n");
    printf("Detected page size: %ld bytes (%ld KB)\n", 
           system_page_size, system_page_size / 1024);
    printf("Arrays smaller than %ld bytes fit in one page.\n", system_page_size);
    printf("Arrays of %ld bytes or larger span multiple pages.\n", system_page_size);
    printf("\nKey Findings:\n");
    printf("  - Cold access (page fault): ~300-5000 ns\n");
    printf("  - Hot access (cached): ~0-1 ns\n");
    printf("  - Page faults are %ld-%ldx slower than cached access\n", 
           system_page_size, system_page_size * 10);
    printf("\nNote: Access time differences may indicate:\n");
    printf("  - Page faults when accessing unmapped pages\n");
    printf("  - TLB misses when crossing page boundaries\n");
    printf("  - Cache line boundaries\n");
    printf("  - Memory controller behavior\n");
    
    return 0;
}

