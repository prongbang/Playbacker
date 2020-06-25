package com.prongbang.playbacker.player.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.prongbang.playbacker.R
import com.prongbang.playbacker.core.exo.PlayerManager
import com.prongbang.playbacker.databinding.ActivityPlayerBinding
import com.prongbang.playbacker.main.domain.LiveStream
import kotlinx.android.synthetic.main.activity_player.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerActivity : AppCompatActivity() {

	private val playerManger by lazy { PlayerManager(this, playerView, debugTexView) }
	private val playerViewModel: PlayerViewModel by viewModel()
	private val liveStream by lazy {
		intent.getParcelableExtra<LiveStream>(LiveStream::class.java.simpleName)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = DataBindingUtil.setContentView<ActivityPlayerBinding>(this,
				R.layout.activity_player)
				.apply {
					data = liveStream
				}
		setContentView(binding.root)

		initView()
		initLoad()
	}

	private fun initView() {
		closeButton.setOnClickListener { finish() }
	}

	private fun initLoad() {
		playerManger.apply {
			createMediaHLS(liveStream.liveUrl)
			playerView().useController = liveStream.useController
			clearResumePosition()
			initial()
		}
	}

	override fun onResume() {
		super.onResume()
		playerManger.initial()
		playerManger.player?.playWhenReady = true
	}

	override fun onPause() {
		super.onPause()
		playerManger.releasePlayer()
	}

	override fun onStop() {
		super.onStop()
		playerManger.releasePlayer()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		playerViewModel.currentPosition = playerView?.player?.currentPosition ?: 0
		super.onSaveInstanceState(outState)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		playerManger.player?.seekTo(playerViewModel.currentPosition)
		playerManger.initial()
	}

	companion object {
		fun navigate(context: Context, liveStream: LiveStream) {
			val intent = Intent(context, PlayerActivity::class.java).apply {
				putExtras(Bundle().apply {
					putParcelable(LiveStream::class.java.simpleName, liveStream)
				})
			}
			context.startActivity(intent)
		}
	}
}
