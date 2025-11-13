package org.bibletranslationtools.glossary.domain

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.toByteArray
import org.bibletranslationtools.glossary.data.api.GlossaryUpdate

interface GlossaryApi {
    suspend fun downloadGlossary(code: String): NetworkResult<ByteArray>
    suspend fun uploadGlossary(file: PlatformFile): NetworkResult<Int>
    suspend fun checkUpdates(glossaries: List<GlossaryUpdate>): NetworkResult<List<GlossaryUpdate>>
}

class GlossaryApiImpl(private val httpClient: HttpClient) : GlossaryApi {

    companion object {
        const val API_URL = "https://glossary.wycliffe-associates-account.workers.dev/api/glossary"
    }

    override suspend fun downloadGlossary(code: String): NetworkResult<ByteArray> {
        return ApiHelper.callApi {
            val response = httpClient.get("$API_URL/$code")

            if (response.status.value in 200..299) {
                val channel = response.bodyAsChannel()
                channel.toByteArray()
            } else {
                throw ServerResponseException(
                    response,
                    "Error downloading glossary"
                )
            }
        }
    }

    override suspend fun uploadGlossary(file: PlatformFile): NetworkResult<Int> {
        return ApiHelper.callApi {
            val response = httpClient.post(API_URL) {
                setBody(file.readBytes())
            }
            response.body()
        }
    }

    override suspend fun checkUpdates(
        glossaries: List<GlossaryUpdate>
    ): NetworkResult<List<GlossaryUpdate>> {
        return ApiHelper.callApi {
            val response = httpClient.post("$API_URL/check_updates") {
                setBody(glossaries)
                contentType(ContentType.Application.Json)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw ServerResponseException(
                    response,
                    "Error checking for updates"
                )
            }
        }
    }
}