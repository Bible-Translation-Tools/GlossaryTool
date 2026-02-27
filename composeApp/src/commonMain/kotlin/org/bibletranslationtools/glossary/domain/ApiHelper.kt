package org.bibletranslationtools.glossary.domain

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import kotlinx.io.IOException
import org.bibletranslationtools.glossary.data.api.ErrorDetails
import org.bibletranslationtools.glossary.logE

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val status: Int, val message: ErrorDetails) : NetworkResult<Nothing>()
}

object ApiHelper {
    suspend fun <T> callApi(block: suspend () -> T): NetworkResult<T> {
        return try {
            NetworkResult.Success(block())
        } catch (e: ClientRequestException) {
            this.logE("Client request failed: ${e.response.status.value}", e)
            NetworkResult.Error(
                status = e.response.status.value,
                message = tryGetErrorDetails(e.response)
            )
        } catch (e: ServerResponseException) {
            this.logE("Server error: ${e.response.status.value}", e)
            NetworkResult.Error(
                status = e.response.status.value,
                message = tryGetErrorDetails(e.response)
            )
        } catch (e: IOException) {
            this.logE("Network I/O error", e)
            NetworkResult.Error(
                status = -1,
                message = ErrorDetails(
                    error = "Network Error.",
                    details = e.message
                )
            )
        } catch (e: Exception) {
            this.logE("Unexpected API error", e)
            NetworkResult.Error(
                status = -1,
                message = ErrorDetails(
                    error = "Network Error.",
                    details = e.message
                )
            )
        }
    }

    private suspend fun tryGetErrorDetails(response: HttpResponse): ErrorDetails {
        return try {
            response.body()
        } catch (e: Exception) {
            this.logE("Failed to parse error response", e)
            ErrorDetails(response.body())
        }
    }
}