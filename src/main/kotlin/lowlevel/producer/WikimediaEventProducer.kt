package lowlevel.producer

import KafkaProducerConfig
import com.launchdarkly.eventsource.EventSource

import lowlevel.config.WikimediaConfig
import org.apache.kafka.clients.producer.KafkaProducer
import java.net.URI
import java.util.concurrent.TimeUnit


class WikimediaEventProducer(
    private val kafkaProducerConfig: KafkaProducerConfig,
    private val wikimediaConfig: WikimediaConfig,
    private val produceDurationMinutes: Long = 10
) {
    fun start() {
        val producer = KafkaProducer<String, String>(kafkaProducerConfig.toProperties())
        val eventHandler = WikimediaChangeHandler(producer, TOPIC_NAME)

        val eventSource = EventSource.Builder(eventHandler, URI.create(wikimediaConfig.wikimediaStreamUrl))
            .build()
            .also { it.start() }

        TimeUnit.MINUTES.sleep(produceDurationMinutes)
        eventSource.close()
    }

    companion object {
        const val TOPIC_NAME = "wikimedia.recentchange"
    }
}


fun main() {
    WikimediaEventProducer(
        kafkaProducerConfig = KafkaProducerConfig(),
        wikimediaConfig = WikimediaConfig()
    ).start()
}
