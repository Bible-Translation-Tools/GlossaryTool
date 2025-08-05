package org.bibletranslationtools.glossary.domain

import glossary.composeapp.generated.resources.Res
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.Utils

@Serializable
data class Catalog(
    val languages: List<CatalogLanguage>
)

@Serializable
data class CatalogLanguage(
    val identifier: String,
    val resources: List<CatalogResource>
)

@Serializable
data class CatalogResource(
    val identifier: String,
    val issued: String,
    val modified: String,
    val version: String? = "v1",
    val subject: String,
    val formats: List<CatalogFormat>
)

@Serializable
data class CatalogFormat(
    val format: String,
    val url: String
)

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val status: Int, val message: String?) : NetworkResult<Nothing>()
}

interface CatalogApi {
    suspend fun updateCatalog()
    suspend fun getCatalog(): NetworkResult<Catalog>
    suspend fun getCatalog(asset: String): Catalog
    suspend fun downloadResource(url: String): NetworkResult<ByteArray>
}

class CatalogApiImpl(
    private val httpClient: HttpClient
): CatalogApi {

    companion object {
        const val CATALOG_URL = "https://api.bibletranslationtools.org/v3/catalog.json"
    }

    override suspend fun updateCatalog() {
        val catalog = getCatalog()
    }

    override suspend fun getCatalog(): NetworkResult<Catalog> {
        return callApi {
            httpClient.get(CATALOG_URL).body()
        }
    }

    override suspend fun getCatalog(asset: String): Catalog {
        val bytes = Res.readBytes(asset)
        val json = String(bytes)
        return Utils.JsonLenient.decodeFromString<Catalog>(json)
    }

    override suspend fun downloadResource(url: String): NetworkResult<ByteArray> {
        return callApi {
            httpClient.get(url).body()
        }
    }

    private suspend fun <T> callApi(block: suspend () -> T): NetworkResult<T> {
        return try {
            NetworkResult.Success(block())
        } catch (e: ClientRequestException) {
            NetworkResult.Error(
                status = e.response.status.value,
                message = e.response.body()
            )
        } catch (e: ServerResponseException) {
            NetworkResult.Error(
                status = e.response.status.value,
                message = e.response.body()
            )
        } catch (e: IOException) {
            NetworkResult.Error(
                status = -1,
                message = e.message
            )
        } catch (e: Exception) {
            NetworkResult.Error(
                status = -1,
                message = e.message
            )
        }
    }
}