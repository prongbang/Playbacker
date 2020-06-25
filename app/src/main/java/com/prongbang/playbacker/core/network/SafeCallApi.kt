package com.prongbang.playbacker.core.network

import com.prongbang.playbacker.core.domain.Results
import retrofit2.Response

suspend fun <T : Any> safeCallApi(call: suspend () -> Response<T>): Results<T> = try {
	val response = call.invoke()
	if (response.isSuccessful) {
		response.body()
				?.let { Results.Success(it) } ?: run {
			Results.Error(Throwable(response.message()))
		}
	} else {
		Results.Error(Throwable(response.message()))
	}
} catch (exception: Exception) {
	Results.Error(exception)
}