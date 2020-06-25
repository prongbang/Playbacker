package com.prongbang.playbacker.core.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LazyServiceGenerator {

	fun <T> create(baseUrl: String, token: String, clazz: Class<T>): T {

		val loggerInterceptor = HttpLoggingInterceptor().apply {
			this.level = HttpLoggingInterceptor.Level.BODY
		}

		val authorizationInterceptor = object : Interceptor {
			override fun intercept(chain: Interceptor.Chain): Response {
				val request = chain.request()
						.newBuilder()
						.addHeader("Authorization", token)
						.build()
				return chain.proceed(request)
						.newBuilder()
						.build()
			}
		}

		val okHttpClient: OkHttpClient = OkHttpClient.Builder()
				.addInterceptor(loggerInterceptor)
				.addInterceptor(authorizationInterceptor)
				.build()

		val retrofit = Retrofit.Builder()
				.baseUrl(baseUrl)
				.client(okHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build()
		return retrofit.create(clazz)
	}
}