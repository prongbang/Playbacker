package com.prongbang.playbacker.di

import com.prongbang.playbacker.core.network.networkModule
import com.prongbang.playbacker.main.mainModule
import com.prongbang.playbacker.player.playerModule

val contributorModule = listOf(
		networkModule,
		mainModule,
		playerModule
)