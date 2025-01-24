package extentedapi.config.streams

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import java.util.Properties


data class StreamsConf (
    private val bootstrapServersURL: String,
    private val applicationId: String
){
    fun toProperties() = Properties().apply {
        setProperty(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
        setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersURL)
        setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass.name)
        setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass.name)

    }
}