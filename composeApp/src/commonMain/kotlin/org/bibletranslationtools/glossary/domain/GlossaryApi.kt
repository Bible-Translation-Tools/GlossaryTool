package org.bibletranslationtools.glossary.domain

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.toByteArray
import org.bibletranslationtools.glossary.data.api.GlossaryUpdate
import org.bibletranslationtools.glossary.data.api.User
import org.bibletranslationtools.glossary.data.api.UserAuth

interface GlossaryApi {
    suspend fun downloadGlossary(code: String): NetworkResult<ByteArray>
    suspend fun uploadGlossary(file: PlatformFile, token: String): NetworkResult<Int>
    suspend fun checkUpdates(glossaries: List<GlossaryUpdate>): NetworkResult<List<GlossaryUpdate>>
    suspend fun login(username: String, password: String): NetworkResult<User>
    suspend fun verifyLogin(token: String): NetworkResult<String>
}

class GlossaryApiImpl(private val httpClient: HttpClient) : GlossaryApi {

    companion object {
        const val BASE_URL = "https://glossary.wycliffe-associates-account.workers.dev"
        const val PUBLIC_API = "$BASE_URL/public/api"
        const val PRIVATE_API = "$BASE_URL/private/api"
    }

    override suspend fun downloadGlossary(code: String): NetworkResult<ByteArray> {
        return ApiHelper.callApi {
            val response = httpClient.get("$PUBLIC_API/glossary/$code")

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

    override suspend fun uploadGlossary(file: PlatformFile, token: String): NetworkResult<Int> {
        return ApiHelper.callApi {
            val response = httpClient.post("$PRIVATE_API/glossary") {
                setBody(file.readBytes())
                accept(ContentType.Application.Json)
                bearerAuth(token)
            }
            response.body()
        }
    }

    override suspend fun checkUpdates(
        glossaries: List<GlossaryUpdate>
    ): NetworkResult<List<GlossaryUpdate>> {
        return ApiHelper.callApi {
            val response = httpClient.post("$PUBLIC_API/glossary/check_updates") {
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

    override suspend fun login(username: String, password: String): NetworkResult<User> {
        return ApiHelper.callApi {
            val response = httpClient.post("$PUBLIC_API/login") {
                setBody(UserAuth(username, password))
                contentType(ContentType.Application.Json)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw ServerResponseException(
                    response,
                    "Authentication error"
                )
            }
        }
    }

    override suspend fun verifyLogin(token: String): NetworkResult<String> {
        return ApiHelper.callApi {
            val response = httpClient.get("$PRIVATE_API/verify") {
                accept(ContentType.Application.Json)
                bearerAuth(token)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw ServerResponseException(
                    response,
                    "Authentication verification error"
                )
            }
        }
    }
}