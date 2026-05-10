import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

export const options = {
  scenarios: {
    optimistic_lock: {
      executor: 'shared-iterations',
      vus: Number(__ENV.VUS || 50),
      iterations: Number(__ENV.ITERATIONS || 200),
      maxDuration: '30s',
      exec: 'reserveWithOptimisticLock',
    },
    pessimistic_lock: {
      executor: 'shared-iterations',
      vus: Number(__ENV.VUS || 50),
      iterations: Number(__ENV.ITERATIONS || 200),
      maxDuration: '30s',
      exec: 'reserveWithPessimisticLock',
    },
    conditional_update: {
      executor: 'shared-iterations',
      vus: Number(__ENV.VUS || 50),
      iterations: Number(__ENV.ITERATIONS || 200),
      maxDuration: '30s',
      exec: 'reserveWithConditionalUpdate',
    },
    redis_lua: {
      executor: 'shared-iterations',
      vus: Number(__ENV.VUS || 50),
      iterations: Number(__ENV.ITERATIONS || 200),
      maxDuration: '30s',
      exec: 'reserveWithRedisLua',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.6'],
    http_req_duration: ['p(95)<1000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const INITIAL_STOCK = Number(__ENV.INITIAL_STOCK || 100);
const RESULT_NAME = __ENV.RESULT_NAME || 'latest';
const STRATEGIES = [
  {
    key: 'optimisticLock',
    label: 'JPA_OPTIMISTIC_LOCK',
    path: 'optimistic-lock',
  },
  {
    key: 'pessimisticLock',
    label: 'JPA_PESSIMISTIC_LOCK',
    path: 'pessimistic-lock',
  },
  {
    key: 'conditionalUpdate',
    label: 'CONDITIONAL_UPDATE',
    path: 'conditional-update',
  },
  {
    key: 'redisLua',
    label: 'REDIS_LUA',
    path: 'redis-lua',
  },
];

const optimisticDuration = new Trend('optimistic_lock_duration', true);
const optimisticFailures = new Rate('optimistic_lock_failed');
const optimisticSuccesses = new Counter('optimistic_lock_success');

const pessimisticDuration = new Trend('pessimistic_lock_duration', true);
const pessimisticFailures = new Rate('pessimistic_lock_failed');
const pessimisticSuccesses = new Counter('pessimistic_lock_success');

const conditionalUpdateDuration = new Trend('conditional_update_duration', true);
const conditionalUpdateFailures = new Rate('conditional_update_failed');
const conditionalUpdateSuccesses = new Counter('conditional_update_success');

const redisLuaDuration = new Trend('redis_lua_duration', true);
const redisLuaFailures = new Rate('redis_lua_failed');
const redisLuaSuccesses = new Counter('redis_lua_success');

const METRICS = {
  optimisticLock: {
    duration: optimisticDuration,
    failures: optimisticFailures,
    successes: optimisticSuccesses,
  },
  pessimisticLock: {
    duration: pessimisticDuration,
    failures: pessimisticFailures,
    successes: pessimisticSuccesses,
  },
  conditionalUpdate: {
    duration: conditionalUpdateDuration,
    failures: conditionalUpdateFailures,
    successes: conditionalUpdateSuccesses,
  },
  redisLua: {
    duration: redisLuaDuration,
    failures: redisLuaFailures,
    successes: redisLuaSuccesses,
  },
};

export function setup() {
  const createdAt = Date.now();
  const stocks = {};

  STRATEGIES.forEach((strategy) => {
    const payload = JSON.stringify({
      sku: `K6-${strategy.label}-${createdAt}`,
      totalQuantity: INITIAL_STOCK,
    });

    const response = http.post(`${BASE_URL}/api/stocks`, payload, {
      headers: { 'Content-Type': 'application/json' },
    });

    check(response, {
      [`created stock for ${strategy.label}`]: (res) => res.status === 201,
    });

    stocks[strategy.key] = {
      id: response.json('id'),
      label: strategy.label,
      path: strategy.path,
    };
  });

  return { stocks };
}

function reserve(data, key) {
  const stock = data.stocks[key];
  const response = http.post(
    `${BASE_URL}/api/stocks/${stock.id}/reservations/${stock.path}`,
    JSON.stringify({ quantity: 1 }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { strategy: stock.label },
    },
  );
  const success = response.status === 200;
  const metrics = METRICS[key];

  metrics.duration.add(response.timings.duration);
  metrics.failures.add(!success);

  if (success) {
    metrics.successes.add(1);
  }

  check(response, {
    'reserved or rejected': (res) => res.status === 200 || res.status >= 400,
  });
}

export function reserveWithOptimisticLock(data) {
  reserve(data, 'optimisticLock');
}

export function reserveWithPessimisticLock(data) {
  reserve(data, 'pessimisticLock');
}

export function reserveWithConditionalUpdate(data) {
  reserve(data, 'conditionalUpdate');
}

export function reserveWithRedisLua(data) {
  reserve(data, 'redisLua');
}

export function teardown(data) {
  const report = STRATEGIES.map((strategy) => {
    const stockRef = data.stocks[strategy.key];
    const response = http.get(`${BASE_URL}/api/stocks/${stockRef.id}`);
    const stock = response.json();

    return {
      strategy: strategy.label,
      stockId: stock.id,
      totalQuantity: stock.totalQuantity,
      allocatedQuantity: stock.allocatedQuantity,
      availableQuantity: stock.availableQuantity,
    };
  });

  console.log(JSON.stringify({ report }, null, 2));
}

function metricValue(data, metricName, valueName) {
  return data.metrics[metricName]?.values?.[valueName] ?? null;
}

function buildStrategySummary(data, key, label) {
  const durationMetric = `${key}_duration`;
  const failedMetric = `${key}_failed`;
  const successMetric = `${key}_success`;

  return {
    strategy: label,
    successCount: metricValue(data, successMetric, 'count'),
    failureRate: metricValue(data, failedMetric, 'rate'),
    avgMs: metricValue(data, durationMetric, 'avg'),
    minMs: metricValue(data, durationMetric, 'min'),
    medMs: metricValue(data, durationMetric, 'med'),
    p90Ms: metricValue(data, durationMetric, 'p(90)'),
    p95Ms: metricValue(data, durationMetric, 'p(95)'),
    maxMs: metricValue(data, durationMetric, 'max'),
  };
}

function formatNumber(value) {
  if (value === null || value === undefined) {
    return '-';
  }

  return Number(value).toFixed(2);
}

function buildMarkdownReport(strategySummaries) {
  const lines = [
    '# Inventory Concurrency k6 Result',
    '',
    '| Strategy | Success | Failure Rate | Avg(ms) | Med(ms) | P90(ms) | P95(ms) | Max(ms) |',
    '| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |',
  ];

  strategySummaries.forEach((summary) => {
    lines.push(
      `| ${summary.strategy} | ${summary.successCount ?? '-'} | ${formatNumber((summary.failureRate ?? 0) * 100)}% | ${formatNumber(summary.avgMs)} | ${formatNumber(summary.medMs)} | ${formatNumber(summary.p90Ms)} | ${formatNumber(summary.p95Ms)} | ${formatNumber(summary.maxMs)} |`,
    );
  });

  lines.push('');

  return `${lines.join('\n')}\n`;
}

export function handleSummary(data) {
  const strategySummaries = [
    buildStrategySummary(data, 'optimistic_lock', 'JPA_OPTIMISTIC_LOCK'),
    buildStrategySummary(data, 'pessimistic_lock', 'JPA_PESSIMISTIC_LOCK'),
    buildStrategySummary(data, 'conditional_update', 'CONDITIONAL_UPDATE'),
    buildStrategySummary(data, 'redis_lua', 'REDIS_LUA'),
  ];

  return {
    stdout: buildMarkdownReport(strategySummaries),
    [`load-test/results/${RESULT_NAME}-summary.json`]: JSON.stringify(
      {
        generatedAt: new Date().toISOString(),
        initialStock: INITIAL_STOCK,
        vusPerStrategy: Number(__ENV.VUS || 50),
        iterationsPerStrategy: Number(__ENV.ITERATIONS || 200),
        strategies: strategySummaries,
      },
      null,
      2,
    ),
    [`load-test/results/${RESULT_NAME}-summary.md`]: buildMarkdownReport(strategySummaries),
  };
}
