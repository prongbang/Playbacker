package com.prongbang.playbacker.main

import com.prongbang.playbacker.core.network.LazyServiceGenerator
import com.prongbang.playbacker.main.data.LiveStreamApi
import com.prongbang.playbacker.main.domain.GetLiveStreamListUseCase
import com.prongbang.playbacker.main.presentation.LiveStreamAdapter
import com.prongbang.playbacker.main.presentation.LiveStreamViewModel
import org.koin.androidx.experimental.dsl.viewModel
import org.koin.dsl.module

// Mock base URL & Token
const val baseUrl = "https://api.domain.com/api/"
const val token = "Bearer YourToken"

val mainModule = module {
	single { get<LazyServiceGenerator>().create(baseUrl, token, LiveStreamApi::class.java) }
	single { GetLiveStreamListUseCase(get()) }
	single { LiveStreamAdapter() }
	viewModel<LiveStreamViewModel>()
}