package producer.wikimedia

import KafkaConfig
import com.launchdarkly.eventsource.EventSource

import config.WikimediaConfig
import jdk.jfr.Event
import org.apache.kafka.clients.producer.KafkaProducer
import java.net.URI
import java.util.concurrent.TimeUnit


class WikimediaEventProducer(
    private val kafkaConfig: KafkaConfig,
    private val wikimediaConfig: WikimediaConfig,
    private val produceDurationMinutes: Long = 10
) {
    fun start() {
        val producer = KafkaProducer<String, String>(kafkaConfig.toProperties())
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
        kafkaConfig = KafkaConfig(),
        wikimediaConfig = WikimediaConfig()
    ).start()
}
