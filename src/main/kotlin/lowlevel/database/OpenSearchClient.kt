package lowlevel.database

import org.apache.http.HttpHost
import org.opensearch.action.bulk.BulkRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.action.search.SearchRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.opensearch.common.xcontent.XContentType
import org.opensearch.index.query.QueryBuilders
import org.opensearch.search.builder.SearchSourceBuilder

class OpenSearchClient(
    private val hostname: String = "localhost",
    private val port: Int = 9200
) {
    private val client: RestHighLevelClient

    init {
        val restClientBuilder = RestClient.builder(
            HttpHost(hostname, port, "http")
        )
        client = RestHighLevelClient(restClientBuilder)
    }

    fun createIndex(index: String, settings: String? = null) {
        if (!indexExists(index)) {
            // Important: Fast fix for indexing everything and not throwing errors when udnerlying data changes.
            val dynamicMapping = """
        {
          "mappings": {
            "dynamic": true,
            "properties": {}
          }
        }"""
            val request = CreateIndexRequest(index)
            request.source(settings ?: dynamicMapping, XContentType.JSON)
            client.indices().create(request, RequestOptions.DEFAULT)
        }
    }

    fun indexDocument(index: String, document: String, xContentType: XContentType = XContentType.JSON, id: String?) {
        if (!indexExists(index)) {
            throw IllegalStateException("Index $index does not exist. Create it first.")
        }
        val request = IndexRequest(index).source(document, xContentType).id(id)
        client.index(request, RequestOptions.DEFAULT)
    }

    private fun indexExists(index: String): Boolean {
        val request = GetIndexRequest(index)
        return client.indices().exists(request, RequestOptions.DEFAULT)
    }

    fun bulkIndex(
        index: String,
        documents: List<Pair<String, String>>,
        xContentType: XContentType = XContentType.JSON
    ) {
        val bulkRequest = BulkRequest().add(
            documents.map { (id, doc) ->
                IndexRequest(index).source(doc, xContentType).id(id)
            }
        )
        client.bulk(bulkRequest, RequestOptions.DEFAULT)
    }

    fun searchDocuments(index: String, field: String, value: String): List<String> {
        val searchRequest = SearchRequest(index)
        val query = QueryBuilders.matchQuery(field, value)
        searchRequest.source(SearchSourceBuilder().query(query))

        val response = client.search(searchRequest, RequestOptions.DEFAULT)
        return response.hits.map { it.sourceAsString }
    }

    fun close() {
        client.close()
    }
}