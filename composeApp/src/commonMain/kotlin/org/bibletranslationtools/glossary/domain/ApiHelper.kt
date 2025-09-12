package org.bibletranslationtools.glossary.domain

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.io.IOException

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val status: Int, val message: String?) : NetworkResult<Nothing>()
}

object ApiHelper {
    suspend fun <T> callApi(block: suspend () -> T): NetworkResult<T> {
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