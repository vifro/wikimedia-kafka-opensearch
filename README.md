# Wikimedia Kafka Opensearch
Test project for running kafka cluster on the real time open source streaming 
data from wikimedia. So the main parts of the project is
1. **wikimedia** -> **Kafka Connect** SSE (Source Connector)
2. **Kafka Streams** Counter for statistics
3. **Kafka Connect** Elasticsearch Sink -> Opensearch
## Pre-req
1. Kotlin with a compatible gradle version
2. Docker installed (docker-compose in  conduktor platoform)

## Monitoring
When docker in running all the services. Go to  
* **localhost:8080** and login to the conduktor console. 

platform-config.yaml is included since this is a local versiona and 
tutorial project. You will see login there. 

## Wikimedia 
* **Source**: https://stream.wikimedia.org/v2/stream/recentchange
* **Inspect data stream**: https://esjewett.github.io/wm-eventsource-demo/ 

### Continue...
