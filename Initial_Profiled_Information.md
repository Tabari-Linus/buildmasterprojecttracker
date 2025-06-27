# Performance Profiling Data Collection Checklist

## Pre-Test Setup
- [ ] Application started with profiling configuration
- [ ] Database populated with sample data
- [ ] JMeter test plan configured
- [ ] Profiler (VisualVM) connected
- [ ] Actuator endpoints accessible at http://localhost:8080/actuator

## JMeter Load Test Metrics to Collect

### Test Configuration
- **Thread Count**: 100 users
- **Ramp-up Period**: 60 seconds
- **Test Duration**: 300 seconds (5 minutes)
- **Loop Count**: 10 per user

### Metrics to Record
- [ ] **Response Times**
    - [ ] Average response time per endpoint
    - [ ] 90th percentile response time
    - [ ] 95th percentile response time
    - [ ] 99th percentile response time
    - [ ] Min/Max response times

- [ ] **Throughput**
    - [ ] Requests per second (RPS)
    - [ ] Transactions per second (TPS)
    - [ ] Bytes per second

- [ ] **Error Rates**
    - [ ] HTTP error percentages
    - [ ] Failed requests count
    - [ ] Error types and messages

### Endpoint-Specific Metrics
- [ ] **GET /api/v1/projects**
    - Average response time: _____ ms
    - 95th percentile: _____ ms
    - Throughput: _____ RPS
    - Error rate: _____ %

- [ ] **GET /api/v1/tasks**
    - Average response time: _____ ms
    - 95th percentile: _____ ms
    - Throughput: _____ RPS
    - Error rate: _____ %

- [ ] **POST /api/v1/tasks**
    - Average response time: _____ ms
    - 95th percentile: _____ ms
    - Throughput: _____ RPS
    - Error rate: _____ %

## JVM & Memory Profiling

### Heap Memory Analysis
- [ ] **Initial heap usage**: _____ MB
- [ ] **Peak heap usage**: _____ MB
- [ ] **Heap utilization at end**: _____ MB
- [ ] **Object allocation rate**: _____ MB/sec
- [ ] **Memory leaks detected**: Yes/No

### Garbage Collection Analysis
- [ ] **GC Algorithm**: G1GC
- [ ] **Total GC time**: _____ ms
- [ ] **GC frequency**: _____ times during test
- [ ] **Average GC pause time**: _____ ms
- [ ] **Max GC pause time**: _____ ms
- [ ] **Young generation collections**: _____
- [ ] **Old generation collections**: _____

### Thread Analysis
- [ ] **Peak thread count**: _____
- [ ] **Active threads during load**: _____
- [ ] **Thread pools utilization**: _____ %
- [ ] **Deadlocks detected**: Yes/No
- [ ] **Thread contention issues**: Yes/No

### CPU Profiling
- [ ] **Average CPU utilization**: _____ %
- [ ] **Peak CPU utilization**: _____ %
- [ ] **Hot methods identified**:
    - Method 1: _____ (_____ % CPU time)
    - Method 2: _____ (_____ % CPU time)
    - Method 3: _____ (_____ % CPU time)

## Database Performance
- [ ] **Database connection pool**:
    - Active connections: _____
    - Max pool size: _____
    - Connection wait time: _____ ms

- [ ] **Query performance**:
    - Slowest queries identified: _____
    - Average query execution time: _____ ms

## Spring Boot Actuator Metrics

### Before Load Test
- [ ] **JVM Memory** (GET /actuator/metrics/jvm.memory.used):
    - Heap: _____ bytes
    - Non-heap: _____ bytes

- [ ] **HTTP Requests** (GET /actuator/metrics/http.server.requests):
    - Total requests: _____
    - Average duration: _____ seconds

### After Load Test
- [ ] **JVM Memory**:
    - Heap: _____ bytes
    - Non-heap: _____ bytes

- [ ] **HTTP Requests**:
    - Total requests: _____
    - Average duration: _____ seconds

- [ ] **Cache Metrics** (if applicable):
    - Cache hit ratio: _____ %
    - Cache miss count: _____

## Issues Identified

### Performance Bottlenecks
- [ ] **Database queries**: _____
- [ ] **N+1 query problems**: _____
- [ ] **Excessive object creation**: _____
- [ ] **Inefficient algorithms**: _____
- [ ] **Cache misses**: _____

### Memory Issues
- [ ] **Memory leaks**: _____
- [ ] **Large object allocations**: _____
- [ ] **String concatenation**: _____
- [ ] **Collection growth**: _____

### Threading Issues
- [ ] **Thread pool exhaustion**: _____
- [ ] **Synchronization bottlenecks**: _____
- [ ] **Context switching**: _____

## Screenshots/Exports to Capture
- [ ] JMeter Summary Report
- [ ] JMeter Response Time Graph
- [ ] JProfiler/VisualVM Memory Timeline
- [ ] JProfiler/VisualVM CPU Profiling Results
- [ ] GC Log Analysis
- [ ] Thread Dump (if issues found)
- [ ] Heap Dump (if memory issues found)

## Additional Notes
_______________________________________________
_______________________________________________
_______________________________________________