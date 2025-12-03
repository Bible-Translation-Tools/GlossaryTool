package org.bibletranslationtools.glossary.domain

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.toByteArray
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.api.GlossaryUpdate
import org.bibletranslationtools.glossary.data.api.GlossaryUser
import org.bibletranslationtools.glossary.data.api.User
import org.bibletranslationtools.glossary.data.api.UserRole

interface GlossaryApi {
    suspend fun downloadGlossary(code: String): NetworkResult<ByteArray>
    suspend fun uploadGlossary(file: PlatformFile, token: String): NetworkResult<Int>
    suspend fun checkUpdates(glossaries: List<GlossaryUpdate>): NetworkResult<List<GlossaryUpdate>>
    suspend fun login(username: String, password: String): NetworkResult<User>
    suspend fun verifyLogin(token: String): NetworkResult<User>
    suspend fun getGlossaryUsers(code: String, token: String): NetworkResult<List<GlossaryUser>>
    suspend fun joinGlossary(code: String, token: String): NetworkResult<List<GlossaryUser>>
    suspend fun updateUserRole(
        code: String,
        username: String,
        role: UserRole,
        token: String
    ): NetworkResult<List<GlossaryUser>>
    suspend fun uploadPendingPhrases(
        code: String,
        phrases: List<Phrase>,
        token: String
    ): NetworkResult<Boolean>
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
                bearerAuth(token)
                setBody(file.readBytes())
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
                setBody(mapOf("username" to username, "password" to password))
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

    override suspend fun verifyLogin(token: String): NetworkResult<User> {
        return ApiHelper.callApi {
            val response = httpClient.get("$PRIVATE_API/verify") {
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

    override suspend fun getGlossaryUsers(
        code: String,
        token: String
    ): NetworkResult<List<GlossaryUser>> {
        return ApiHelper.callApi {
            val response = httpClient.get("$PRIVATE_API/glossary/$code/users") {
                bearerAuth(token)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw ServerResponseException(
                    response,
                    "Error getting glossary users"
                )
            }
        }
    }

    override suspend fun joinGlossary(
        code: String,
        token: String
    ): NetworkResult<List<GlossaryUser>> {
        return ApiHelper.callApi {
            val response = httpClient.get("$PRIVATE_API/glossary/$code/join") {
                bearerAuth(token)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw ServerResponseException(
                    response,
                    "Error joining glossary"
                )
            }
        }
    }

    override suspend fun updateUserRole(
        code: String,
        username: String,
        role: UserRole,
        token: String
    ): NetworkResult<List<GlossaryUser>> {
        return ApiHelper.callApi {
            val response = httpClient.post("$PRIVATE_API/glossary/$code/role") {
                bearerAuth(token)
                setBody(mapOf("username" to username, "role" to role.name.lowercase()))
                contentType(ContentType.Application.Json)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw ServerResponseException(
                    response,
                    "Error updating user role"
                )
            }
        }
    }

    override suspend fun uploadPendingPhrases(
        code: String,
        phrases: List<Phrase>,
        token: String
    ): NetworkResult<Boolean> {
        return ApiHelper.callApi {
            val response = httpClient.post(
                "$PRIVATE_API/glossary/$code/pending_phrases"
            ) {
                bearerAuth(token)
                setBody(phrases)
                contentType(ContentType.Application.Json)
            }
            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw ServerResponseException(
                    response,
                    "Error uploading pending phrases"
                )
            }
        }
    }
}