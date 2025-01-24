# Kafka Connect
We are doing the same thing as the previous low-level implementation,
but instead use pre-written connectors(libraries) which we invoke with the standalone connector
call.

remember to download the connectors and put them in oyu kafka/connectors folder
before referencing them in your properties, ie
```commandline
connector.class=io.conduktor.demos.kafka.connect.wikimedia.WikimediaConnector
```
or
````commandline
connector.class=io.aiven.kafka.connect.opensearch.OpensearchSinkConnector
````


## Wikimedia
````commandline
connect-standalone standalone.properties wikimedia.properties
````

which is starting a local cluster. CHeck console and see that the new topic has been
added. (wikimedia.recentchange.connect)


## ElasticSearch

````commandline
connect-standalone connect-standalone.properties elasticsearch.properties
````

> **_EXTRAS:_** With sink error, try the following.  https://github.com/dmathieu/kafka-connect-opensearch

# Streams - Check the subfolder 
for processes on streams.