# Wikimedia Kafka OpenSearch Pipeline

Real-time data pipeline processing Wikimedia stream data using Kafka and OpenSearch.

## Architecture
1. Wikimedia → Kafka Connect (Source)
2. Kafka Streams (Statistics)
3. Kafka Connect → OpenSearch (Sink)

## Prerequisites
- Kotlin & Gradle
- Docker with docker-compose
- Running services:
- Producer: `services/conduktor-platform/docker-compose.yaml`
- Consumer: `services/open-search/docker-compose.yaml`

## Environment Variables
```commandline
KAFKA_BOOTSTRAP_SERVERS
SCHEMA_REGISTRY_URL
WIKIMEDIA_STREAM_URL
```

Default values in config classes.

## Services

### Producer
- Data Source: [Wikimedia Stream](https://stream.wikimedia.org/v2/stream/recentchange)
- Demo: [Event Source Demo](https://esjewett.github.io/wm-eventsource-demo/)

### Monitoring
- Conduktor Console: `localhost:8080`
   - Credentials in platform-config.yaml
- OpenSearch: `localhost:9200`

### Consumer
[TBD]

## Project Structure
Single module approach chosen for tutorial simplicity.

## Setup & Running
1. Start required Docker services
2. Configure environment variables (optional)
3. Run producer/consumer applications

## Development
[Future development instructions]