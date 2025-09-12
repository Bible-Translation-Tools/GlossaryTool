package org.bibletranslationtools.glossary.domain

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.toByteArray

interface GlossaryApi {
    suspend fun downloadGlossary(code: String): NetworkResult<ByteArray>
    suspend fun uploadGlossary(file: PlatformFile): NetworkResult<Boolean>
}

class GlossaryApiImpl(private val httpClient: HttpClient) : GlossaryApi {

    companion object {
        const val API_URL = "https://glossary.wycliffe-associates-account.workers.dev/api/glossary"
    }

    override suspend fun downloadGlossary(code: String): NetworkResult<ByteArray> {
        return ApiHelper.callApi {
            val response = httpClient.get("$API_URL/$code")
            val channel = response.bodyAsChannel()
            channel.toByteArray()
        }
    }

    override suspend fun uploadGlossary(file: PlatformFile): NetworkResult<Boolean> {
        return ApiHelper.callApi {
            val resp = httpClient.post(API_URL) {
                setBody(file.readBytes())
            }

            println(resp.bodyAsText())

            resp.body()
        }
    }
}