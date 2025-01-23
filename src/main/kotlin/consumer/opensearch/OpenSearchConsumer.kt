package consumer.opensearch

import KafkaConsumerConfig
import database.OpenSearchClient
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.opensearch.common.xcontent.XContentType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class OpenSearchConsumer(
    kafkaConsumerConfig: KafkaConsumerConfig,
    val openSearchClient: OpenSearchClient,
) : AutoCloseable {
    val consumer = KafkaConsumer<String, String>(kafkaConsumerConfig.toProperties())

    private val log: Logger = LoggerFactory.getLogger(javaClass)
    private var isRunning = true
    private var isClosed = false

    fun subscribe(topicNames: List<String>, openSearchIndex: String) {
        try {
            consumer.subscribe(topicNames)
            while (isRunning) {
                log.info("Polling")
                val records = consumer.poll(Duration.ofMillis(1000))
                processRecords(records, openSearchIndex)
            }
        } catch (e: WakeupException) {
            log.info("Consumer is starting to shut down")
        } catch (e: Exception) {
            log.error("Unexpected exception in the consumer", e)
        } finally {
            closeConsumer()
        }
    }

    private fun processRecords(
        records: ConsumerRecords<String, String>,
        openSearchIndex: String
    ) {
        records.forEach { record ->
            val response = openSearchClient.indexDocument(
                openSearchIndex,
                record.value(),
                XContentType.JSON
            )
        }
    }

    fun registerShutdownHook() {
        val mainThread = Thread.currentThread()
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Shutdown initiated, calling consumer.wakeup()")
            isRunning = false
            consumer.wakeup()
            try {
                mainThread.join()
            } catch (e: InterruptedException) {
                log.error("Shutdown hook interrupted", e)
            }
        })
        log.info("Shutdown hook registered")
    }

    private fun closeConsumer() {
        if (!isClosed) {  // Only close if not already closed
            try {
                isClosed = true
                consumer.close()
                log.info("Consumer closed successfully")
            } catch (e: Exception) {
                log.error("Error closing consumer", e)
            }
        }
    }

    override fun close() {
        isRunning = false
        closeConsumer()
    }

}

fun main() {
    println("Running")
    // First create openSearch Client
    val topicName = listOf("wikimedia.recentchange")

    val openSearchClient = OpenSearchClient()
    val elasticIndex = "wikimedia-recent-change"

    // create index and insert some data.
    openSearchClient.createIndex(elasticIndex)
    openSearchClient.indexDocument(elasticIndex, """{"Description": "This is a test"}""")

    try {
        val consumer = OpenSearchConsumer(
            KafkaConsumerConfig(),
            openSearchClient
        )
        consumer.subscribe(topicName, elasticIndex)

    } catch (e: Exception) {
        throw e
    } finally {
        openSearchClient.close()
    }
}