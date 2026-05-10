# Inventory Concurrency k6 Result

| Strategy | Success | Failure Rate | Avg(ms) | Med(ms) | P90(ms) | P95(ms) | Max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| JPA_OPTIMISTIC_LOCK | 1000 | 50.00% | 55.24 | 44.10 | 112.83 | 128.05 | 374.71 |
| JPA_PESSIMISTIC_LOCK | 1000 | 50.00% | 45.93 | 42.05 | 82.40 | 100.78 | 270.87 |
| CONDITIONAL_UPDATE | 1000 | 50.00% | 41.55 | 40.82 | 69.70 | 84.45 | 283.09 |
| REDIS_LUA | 1000 | 50.00% | 5.86 | 4.85 | 9.02 | 11.16 | 104.35 |

