package lowlevel.consumer

import KafkaConsumerConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import lowlevel.database.OpenSearchClient
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class OpenSearchConsumer(
    kafkaConsumerConfig: KafkaConsumerConfig,
    val openSearchClient: OpenSearchClient,
) : AutoCloseable {
    private val gson = Gson()
    val consumer = KafkaConsumer<String, String>(kafkaConsumerConfig.toProperties())

    private val log: Logger = LoggerFactory.getLogger(javaClass)
    private var isRunning = true
    private var isClosed = false

    fun subscribe(topicNames: List<String>, openSearchIndex: String) {
        try {
            consumer.subscribe(topicNames)

            while (isRunning) {
                log.info("Polling")

                val records = consumer.poll(Duration.ofSeconds(2))
                log.info("Number of records read: ${records.count()}")
                processRecordsInBulk(records, openSearchIndex)
                Thread.sleep(2000)
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
            val id = extractId(record.value())
            val wikimediaData = gson.fromJson(record.value(), JsonObject::class.java)
            val indexDoc = JsonObject().apply {
                addProperty("title", wikimediaData.get("title")?.asString)
                addProperty("user", wikimediaData.get("user")?.asString)
                addProperty("timestamp", wikimediaData.get("timestamp")?.asString)
                addProperty("type", wikimediaData.get("type")?.asString)
                // Add other relevant fields if needed.
            }
            openSearchClient.indexDocument(openSearchIndex, gson.toJson(indexDoc), id = id)
        }
    }

    private fun processRecordsInBulk(
        records: ConsumerRecords<String, String>,
        openSearchIndex: String
    ) {
        records.takeUnless { it.isEmpty }?.let { validRecords ->
            validRecords.map { record ->
                extractId(record.value()) to record.value().let { value ->
                    gson.fromJson(value, JsonObject::class.java).toIndexDoc()
                }
            }.also { requests ->
                openSearchClient.bulkIndex(openSearchIndex, requests)
                consumer.commitAsync()
            }
        }
    }

    private fun JsonObject.toIndexDoc() = JsonObject().apply {
        addProperty("title", get("title")?.asString)
        addProperty("user", get("user")?.asString)
        addProperty("timestamp", get("timestamp")?.asString)
        addProperty("type", get("type")?.asString)
    }.toString()

    private fun extractId(json: String): String {
        return JsonParser.parseString(json)
            .asJsonObject
            .get("meta")
            .asJsonObject
            .get("id")
            .asString ?: "No value"
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
    val topicName = listOf("wikimedia.recentchange")
    val openSearchClient = OpenSearchClient()
    val elasticIndex = "wikimedia-recent-change"

    // create index if not already existing (Should not be done by the consumer).
    openSearchClient.createIndex(elasticIndex)

    try {
        OpenSearchConsumer(
            KafkaConsumerConfig(),
            openSearchClient
        ).use { consumer ->
            consumer.registerShutdownHook()
            consumer.subscribe(topicName, elasticIndex)
        }
    } catch (e: Exception) {
        throw e
    } finally {
        openSearchClient.close()
    }
}