// pagefault_benchmark.c
// Measure per-page access time for cold (page fault) vs hot (cache/resident) accesses.

#define _GNU_SOURCE
#include <errno.h>
#include <inttypes.h>
#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <time.h>
#include <unistd.h>

typedef struct Stats {
    double mean;
    double stddev;
    uint64_t min;
    uint64_t max;
} Stats;

static inline uint64_t now_ns(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (uint64_t)ts.tv_sec * 1000000000ull + (uint64_t)ts.tv_nsec;
}

static Stats compute_stats(const uint64_t *samples, size_t count) {
    Stats s = {0};
    if (count == 0) return s;
    uint64_t minv = UINT64_MAX;
    uint64_t maxv = 0;
    long double sum = 0.0L;
    for (size_t i = 0; i < count; ++i) {
        uint64_t v = samples[i];
        if (v < minv) minv = v;
        if (v > maxv) maxv = v;
        sum += (long double)v;
    }
    long double mean = sum / (long double)count;
    long double var = 0.0L;
    for (size_t i = 0; i < count; ++i) {
        long double d = (long double)samples[i] - mean;
        var += d * d;
    }
    var /= (long double)count;
    s.mean = (double)mean;
    s.stddev = (double)sqrt((double)var);
    s.min = minv;
    s.max = maxv;
    return s;
}

static void human_bytes(char *out, size_t out_sz, size_t bytes) {
    const char *units[] = {"B", "KB", "MB", "GB", "TB"};
    int u = 0;
    double val = (double)bytes;
    while (val >= 1024.0 && u < 4) {
        val /= 1024.0;
        ++u;
    }
    snprintf(out, out_sz, "%.1f %s", val, units[u]);
}

typedef struct Config {
    size_t size_bytes;
    int repeats;
    int randomize;
} Config;

static int cmp_size_t(const void *a, const void *b) {
    size_t aa = *(const size_t *)a;
    size_t bb = *(const size_t *)b;
    if (aa < bb) return -1;
    if (aa > bb) return 1;
    return 0;
}

static void shuffle_indices(size_t *idx, size_t n) {
    for (size_t i = n - 1; i > 0; --i) {
        size_t j = (size_t) (rand() % (int)(i + 1));
        size_t t = idx[i];
        idx[i] = idx[j];
        idx[j] = t;
    }
}

static void usage(const char *argv0) {
    fprintf(stderr,
            "Usage: %s [--mb N | --gb N] [--repeats R] [--random]\n"
            "  --mb N       Working set size in megabytes (default 1024)\n"
            "  --gb N       Working set size in gigabytes (overrides --mb)\n"
            "  --repeats R  Number of passes over pages per case (default 3)\n"
            "  --random     Access pages in random order (default sequential)\n",
            argv0);
}

int main(int argc, char **argv) {
    Config cfg;
    cfg.size_bytes = 1024ull * 1024ull * 1024ull; // 1 GiB default
    cfg.repeats = 3;
    cfg.randomize = 0;

    for (int i = 1; i < argc; ++i) {
        if (strcmp(argv[i], "--mb") == 0 && i + 1 < argc) {
            long mb = strtol(argv[++i], NULL, 10);
            if (mb <= 0) { usage(argv[0]); return 1; }
            cfg.size_bytes = (size_t)mb * 1024ull * 1024ull;
        } else if (strcmp(argv[i], "--gb") == 0 && i + 1 < argc) {
            double gb = strtod(argv[++i], NULL);
            if (gb <= 0) { usage(argv[0]); return 1; }
            cfg.size_bytes = (size_t)(gb * 1024.0 * 1024.0 * 1024.0);
        } else if (strcmp(argv[i], "--repeats") == 0 && i + 1 < argc) {
            int r = atoi(argv[++i]);
            if (r <= 0) { usage(argv[0]); return 1; }
            cfg.repeats = r;
        } else if (strcmp(argv[i], "--random") == 0) {
            cfg.randomize = 1;
        } else if (strcmp(argv[i], "-h") == 0 || strcmp(argv[i], "--help") == 0) {
            usage(argv[0]);
            return 0;
        } else {
            usage(argv[0]);
            return 1;
        }
    }

    const long page_size = sysconf(_SC_PAGESIZE);
    if (page_size <= 0) {
        fprintf(stderr, "Failed to get page size\n");
        return 1;
    }

    size_t num_pages = cfg.size_bytes / (size_t)page_size;
    if (num_pages == 0) {
        fprintf(stderr, "Working set too small (< one page)\n");
        return 1;
    }

    void *region = mmap(NULL, cfg.size_bytes, PROT_READ | PROT_WRITE,
                        MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (region == MAP_FAILED) {
        perror("mmap");
        return 1;
    }

    // Prepare page index order
    size_t *page_idx = (size_t *)malloc(num_pages * sizeof(size_t));
    if (!page_idx) {
        perror("malloc");
        munmap(region, cfg.size_bytes);
        return 1;
    }
    for (size_t i = 0; i < num_pages; ++i) page_idx[i] = i;
    if (cfg.randomize) {
        srand((unsigned)time(NULL));
        shuffle_indices(page_idx, num_pages);
    }

    // Buffers for per-page timings (ns)
    uint64_t *samples_cold = (uint64_t *)malloc(num_pages * sizeof(uint64_t));
    uint64_t *samples_hot = (uint64_t *)malloc(num_pages * sizeof(uint64_t));
    if (!samples_cold || !samples_hot) {
        perror("malloc samples");
        free(page_idx);
        munmap(region, cfg.size_bytes);
        return 1;
    }

    // Case A: Cold first-touch after discarding pages (should fault and zero-fill)
    int rv = madvise(region, cfg.size_bytes, MADV_DONTNEED);
    if (rv != 0) {
        perror("madvise DONTNEED");
        // Continue anyway; pages may still be unmapped lazily
    }

    volatile unsigned char *base = (volatile unsigned char *)region;
    for (size_t r = 0; r < (size_t)cfg.repeats; ++r) {
        for (size_t k = 0; k < num_pages; ++k) {
            size_t i = page_idx[k];
            size_t offset = i * (size_t)page_size;
            uint64_t t0 = now_ns();
            base[offset] ^= 1u; // write to ensure allocation
            uint64_t t1 = now_ns();
            samples_cold[k] = t1 - t0;
        }
        // For multiple repeats, evict again to ensure cold
        if (r + 1 < (size_t)cfg.repeats) {
            madvise(region, cfg.size_bytes, MADV_DONTNEED);
        }
    }

    // Case B: Hot access (pages resident)
    for (size_t r = 0; r < (size_t)cfg.repeats; ++r) {
        for (size_t k = 0; k < num_pages; ++k) {
            size_t i = page_idx[k];
            size_t offset = i * (size_t)page_size;
            uint64_t t0 = now_ns();
            base[offset] ^= 1u;
            uint64_t t1 = now_ns();
            samples_hot[k] = t1 - t0;
        }
    }

    Stats cold = compute_stats(samples_cold, num_pages);
    Stats hot  = compute_stats(samples_hot,  num_pages);

    char size_buf[64];
    human_bytes(size_buf, sizeof(size_buf), cfg.size_bytes);

    // Print table
    printf("%20s %12s %12s %16s %16s %16s %16s\n",
           "case", "pages", "page_KB", "mean_ns", "stddev_ns", "min_ns", "max_ns");
    printf("%20s %12zu %12ld %16.2f %16.2f %16" PRIu64 " %16" PRIu64 "\n",
           "cold_first_touch", num_pages, page_size / 1024, cold.mean, cold.stddev, cold.min, cold.max);
    printf("%20s %12zu %12ld %16.2f %16.2f %16" PRIu64 " %16" PRIu64 "\n",
           "hot_resident",     num_pages, page_size / 1024, hot.mean,  hot.stddev,  hot.min,  hot.max);
    printf("\nSummary: region=%s, repeats=%d, order=%s\n",
           size_buf, cfg.repeats, cfg.randomize ? "random" : "sequential");

    // Cleanup
    free(samples_hot);
    free(samples_cold);
    free(page_idx);
    munmap(region, cfg.size_bytes);
    return 0;
}