package com.prongbang.playbacker.player

import com.prongbang.playbacker.player.presentation.PlayerViewModel
import org.koin.androidx.experimental.dsl.viewModel
import org.koin.dsl.module

val playerModule = module {
	viewModel<PlayerViewModel>()
}