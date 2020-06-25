package com.prongbang.playbacker.main.domain

import com.prongbang.playbacker.core.domain.ResponseData
import com.prongbang.playbacker.core.domain.Results
import com.prongbang.playbacker.core.network.safeCallApi
import com.prongbang.playbacker.main.data.LiveStreamApi

class GetLiveStreamListUseCase(private val liveStreamApi: LiveStreamApi) {

	suspend fun execute(): Results<ResponseData<LiveStream>> {
		return safeCallApi {
			liveStreamApi.gerLiveStreamList()
		}
	}
}