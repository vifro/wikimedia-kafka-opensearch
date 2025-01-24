package extentedapi.streams

import extentedapi.config.streams.AppConfig
import extentedapi.config.streams.StreamsConf
import extentedapi.streams.processor.BotCountStreamBuilder
import extentedapi.streams.processor.EventCountTimeseriesBuilder
import extentedapi.streams.processor.WebsiteCountStreamBuilder
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WikimediaStreamsApp(
    topology: Topology,
    streamsConf: StreamsConf
) {
    private val streamer: KafkaStreams = KafkaStreams(topology, streamsConf.toProperties())
    fun start() = streamer.start()
}

fun main() {
    val LOGGER = LoggerFactory.getLogger(WikimediaStreamsApp::class.java)

    val config = AppConfig(
        inputTopic = "wikimedia.recentchange",
        bootstrapURL = "localhost:9092",
        appId = "wikimedia-stats-app"
    )

    val changeJsonStream = StreamsBuilder().apply {
        stream<String, String>(config.inputTopic).also { stream ->
            listOf(
                BotCountStreamBuilder(stream),
                WebsiteCountStreamBuilder(stream),
                EventCountTimeseriesBuilder(stream)
            ).forEach { it.setup() } // Since all Builders implement the StreamBuilderInterface.
        }
    }.build().also {
        LOGGER.info("Topology: ${it.describe()}")
    }

    WikimediaStreamsApp(
        topology = changeJsonStream,
        streamsConf = StreamsConf(config.bootstrapURL, config.appId)
    ).start()


}

