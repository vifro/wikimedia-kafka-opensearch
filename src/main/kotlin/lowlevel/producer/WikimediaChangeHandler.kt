package lowlevel.producer

import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.MessageEvent
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class WikimediaChangeHandler(
    private val kafkaProducer: KafkaProducer<String, String>,
    private val topic: String
) : EventHandler {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun onOpen() {}

    override fun onMessage(event: String?, messageEvent: MessageEvent?) {
        log.info("Event Data: ${messageEvent?.data}")
        // Asynch code
        val record = ProducerRecord<String, String>(
            topic,
            messageEvent?.data ?: "empty_message"
        )
        kafkaProducer.send(record)
    }

    override fun onComment(comment: String?) {}

    override fun onError(t: Throwable?) {
        log.error("Error in streams: ${t.toString()}")
        throw t ?: Throwable("Not specified")
    }

    override fun onClosed() {
        kafkaProducer.close()
    }

}