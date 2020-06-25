package com.prongbang.playbacker.main.data

import com.prongbang.playbacker.core.domain.ResponseData
import com.prongbang.playbacker.main.domain.LiveStream
import retrofit2.Response
import retrofit2.http.GET

interface LiveStreamApi {
	@GET("v1/livestream?page=1&limit=20000")
	suspend fun gerLiveStreamList(): Response<ResponseData<LiveStream>>
}