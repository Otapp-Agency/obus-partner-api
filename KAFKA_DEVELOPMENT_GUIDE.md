# Kafka Development Guide for OBUS Partners API

## Overview
This guide provides comprehensive instructions for setting up and using Kafka in the OBUS Partners API for event-driven operations.

## Environment Requirements

### Development Environment
- **Kafka**: Single broker with 1 replication factor
- **Topics**: Auto-created with minimal partitions (1-3)
- **Performance**: Optimized for fast development cycles
- **Monitoring**: Basic logging and Kafka UI
- **Security**: None (PLAINTEXT)

### Staging Environment
- **Kafka**: Multi-broker cluster with 2 replication factor
- **Topics**: Pre-created with moderate partitions (3-5)
- **Performance**: Production-like settings with monitoring
- **Monitoring**: Enhanced logging and metrics
- **Security**: Optional SASL/SSL

### Production Environment
- **Kafka**: High-availability cluster with 3+ replication factor
- **Topics**: Pre-created with high partitions (5+)
- **Performance**: Optimized for high throughput and low latency
- **Monitoring**: Full observability with alerts
- **Security**: SASL/SSL with authentication

## Development Setup

### Prerequisites
1. **Docker** and **Docker Compose** installed
2. **Java 21** and **Maven** for the application
3. **Git** for version control

### Quick Start (Windows)
```bash
# 1. Start Kafka services
setup-kafka-dev.bat

# 2. Start the application
mvnw.cmd spring-boot:run

# 3. Access Kafka UI
# Open http://localhost:8080 in your browser
```

### Quick Start (Linux/Mac)
```bash
# 1. Start Kafka services
./setup-kafka-dev.sh

# 2. Start the application
./mvnw spring-boot:run

# 3. Access Kafka UI
# Open http://localhost:8080 in your browser
```

### Manual Setup
If you prefer manual setup:

```bash
# 1. Start Kafka services
docker-compose -f docker-compose.kafka.yml up -d

# 2. Wait for services to be ready (30 seconds)
sleep 30

# 3. Start your application (topics will be auto-created)
mvnw spring-boot:run
```

## Available Services

### Kafka Services
- **Kafka Broker**: `localhost:9092`
- **Kafka UI**: `http://localhost:8083`
- **Schema Registry**: `http://localhost:8084`
- **Zookeeper**: `localhost:2181`

### Application Services
- **OBUS Partners API**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **Actuator Health**: `http://localhost:8080/api/actuator/health`

## Kafka Topics

### Partner Management
- `obus.partner.registered` - Partner registration events
- `obus.partner.updated` - Partner profile updates
- `obus.partner.apikey.generated` - API key generation
- `obus.partner.apikey.revoked` - API key revocation

### Agent Management
- `obus.agent.registered` - Agent registration events
- `obus.agent.authenticated` - Agent authentication events
- `obus.agent.verification.completed` - Agent verification completion

### Booking Management
- `obus.booking.created` - New booking creation
- `obus.booking.updated` - Booking updates
- `obus.booking.cancelled` - Booking cancellations

### Payment Management
- `obus.payment.initiated` - Payment initiation
- `obus.payment.completed` - Payment completion
- `obus.payment.failed` - Payment failures

### Audit and Security
- `obus.audit.user.action` - User action logging
- `obus.security.event` - Security-related events

### System
- `obus.notification` - General notifications
- `obus.dlq` - Dead letter queue for failed messages

## Development Workflow

### 1. Start Development Environment
```bash
# Start Kafka
setup-kafka-dev.bat  # Windows
./setup-kafka-dev.sh # Linux/Mac

# Start application
mvnw spring-boot:run
```

### 2. Monitor Events
- Open Kafka UI: `http://localhost:8083`
- Navigate to topics to see messages
- Use the message browser to inspect event data

### 3. Test Event Publishing
```bash
# Publish a test message
docker exec obus-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic obus.partner.registered
# Type your message and press Enter
```

### 4. Test Event Consumption
```bash
# Consume messages
docker exec obus-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic obus.partner.registered --from-beginning
```

## Configuration Files

### Application Configuration
- `application.yml` - Base configuration
- `application-dev.yml` - Development overrides
- `application-staging.yml` - Staging configuration
- `application-prod.yml` - Production configuration

### Kafka Configuration
- `KafkaConfig.java` - Spring Kafka configuration
- `KafkaTopicsConfig.java` - Topic definitions
- `docker-compose.kafka.yml` - Local Kafka services

## Environment Variables

### Development
```bash
# No environment variables needed
# Uses localhost:9092 by default
```

### Staging
```bash
KAFKA_BOOTSTRAP_SERVERS=staging-kafka-cluster:9092
```

### Production
```bash
KAFKA_BOOTSTRAP_SERVERS=prod-kafka-cluster:9092
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=PLAIN
KAFKA_SASL_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule required username="user" password="pass";
```

## Troubleshooting

### Common Issues

#### 1. Kafka Not Starting
```bash
# Check Docker status
docker info

# Check container logs
docker-compose -f docker-compose.kafka.yml logs

# Restart services
docker-compose -f docker-compose.kafka.yml down
docker-compose -f docker-compose.kafka.yml up -d
```

#### 2. Application Cannot Connect to Kafka
```bash
# Check if Kafka is running
docker ps | grep kafka

# Test connection
docker exec obus-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Check application logs
tail -f logs/application.log
```

#### 3. Topics Not Created
```bash
# List existing topics
docker exec obus-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Topics should be auto-created by Spring Boot application
# If not, check application logs for errors
```

#### 4. Port Conflicts
```bash
# Check port usage
netstat -an | grep :9092
netstat -an | grep :8080
netstat -an | grep :8081
netstat -an | grep :8083
netstat -an | grep :8084

# Stop conflicting services or change ports in docker-compose.kafka.yml
```

### Performance Tuning

#### Development
- Single consumer thread
- Small batch sizes
- Fast acknowledgment
- Minimal retries

#### Production
- Multiple consumer threads
- Large batch sizes
- Optimized acknowledgment
- Comprehensive retry logic

## Next Steps

1. **Implement Event Models** - Define event schemas
2. **Create Event Producers** - Add event publishing to business logic
3. **Build Event Consumers** - Process events for notifications, audit, etc.
4. **Add Monitoring** - Implement metrics and alerting
5. **Test Integration** - End-to-end testing with real events

## Resources

- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka UI Documentation](https://docs.kafka-ui.provectus.io/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
