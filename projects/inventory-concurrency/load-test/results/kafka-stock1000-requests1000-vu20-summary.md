# Inventory Concurrency k6 Result

| Strategy | Success | Failure Rate | Avg(ms) | Med(ms) | P90(ms) | P95(ms) | Max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| JPA_OPTIMISTIC_LOCK | 727 | 27.30% | 63.00 | 45.89 | 118.35 | 132.95 | 250.62 |
| JPA_PESSIMISTIC_LOCK | 1000 | 0.00% | 58.00 | 52.39 | 83.93 | 96.77 | 205.52 |
| CONDITIONAL_UPDATE | 1000 | 0.00% | 48.44 | 43.95 | 68.80 | 78.10 | 132.02 |
| REDIS_LUA | 1000 | 0.00% | 5.07 | 4.61 | 6.96 | 8.29 | 80.64 |

