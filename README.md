# Wikimedia Kafka OpenSearch Project

This project is a Kotlin-based implementation of a real-time data pipeline that processes Wikimedia stream data using Kafka and OpenSearch. It is inspired by the "Kafka for Beginners" tutorial on Udemy-> [Link](https://www.udemy.com/course/apache-kafka/learn/lecture/31409100#overview) <-

## Architecture Overview

The data pipeline consists of the following components:

1. **Wikimedia Stream**: Acts as the data source, providing real-time updates from Wikimedia projects.
2. **Kafka Connect (Source)**: Captures data from the Wikimedia Stream and publishes it to Kafka topics.
3. **Kafka Streams**: Processes the data for generating statistics or transformations as needed.
4. **Kafka Connect (Sink)**: Transfers processed data from Kafka topics to OpenSearch for indexing and search capabilities.

## Prerequisites

Before setting up and running the project, ensure you have the following installed:

- **Kotlin**: Programming language used for the application.
- **Gradle**: Build automation tool for managing dependencies and building the project.
- **Docker**: Containerization platform to run services in isolated environments.
- **Docker Compose**: Tool for defining and running multi-container Docker applications.

## Project Structure

The repository is organized as follows:

- `src/main/kotlin`: Contains the Kotlin source code for the application.
- `services/conduktor-platform/docker-compose.yaml`: Docker Compose file to set up and run the Kafka producer services.
- `services/open-search/docker-compose.yaml`: Docker Compose file to set up and run the OpenSearch consumer services.
- `schema-registry`: Directory containing schema definitions for data serialization and deserialization.
- `.gitignore`: Specifies files and directories to be ignored by Git.
- `build.gradle.kts`: Gradle build script in Kotlin DSL.
- `gradle.properties`: Configuration properties for the Gradle build.
- `settings.gradle.kts`: Gradle settings script in Kotlin DSL.

## Environment Variables

The application uses the following environment variables, with default values specified in the configuration classes:

- `KAFKA_BOOTSTRAP_SERVERS`: Specifies the Kafka server addresses.
- `SCHEMA_REGISTRY_URL`: URL of the schema registry service.
- `WIKIMEDIA_STREAM_URL`: URL of the Wikimedia stream to be consumed.

## Setting Up and Running the Services

Follow these steps to set up and run the project:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/vifro/wikimedia-kafka-opensearch.git
   cd wikimedia-kafka-opensearch
   ```

2. **Start the Producer Services**:
   Navigate to the `services/conduktor-platform` directory and start the Kafka producer services using Docker Compose:
   ```bash
   cd services/conduktor-platform
   docker-compose up -d
   ```

3. **Start the Consumer Services**:
   Navigate to the `services/open-search` directory and start the OpenSearch consumer services using Docker Compose:
   ```bash
   cd ../open-search
   docker-compose up -d
   ```

4. **Build and Run the Application**:
   Return to the root directory of the project, build the application using Gradle, and run it:
   ```bash
   cd ../../
   ./gradlew build
   ./gradlew run
   ```

Ensure that Docker services are running and accessible before starting the application.

## Data Source

- **Wikimedia Stream**: The application consumes real-time data from the [Wikimedia EventStreams](https://stream.wikimedia.org/).
- **Demo**: For a demonstration of the event source, visit the [Event Source Demo](https://esjewett.github.io/).