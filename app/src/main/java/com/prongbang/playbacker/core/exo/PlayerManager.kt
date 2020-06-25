package com.prongbang.playbacker.core.exo

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackPreparer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.DebugTextViewHelper
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import com.prongbang.playbacker.R
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import kotlin.math.max

class PlayerManager(
		private val content: Context,
		private val playerView: PlayerView,
		private val debugTextView: TextView
) : PlayerControlView.VisibilityListener, PlaybackPreparer, View.OnClickListener {

	var onError: (Boolean) -> Unit = {}
	private var inErrorState: Boolean = false
	private var debugRootView: LinearLayout? = null
	private var debugViewHelper: DebugTextViewHelper? = null
	private var shouldAutoPlay: Boolean = false
	private var defaultTrackSelection: DefaultTrackSelector? = null
	private var trackSelectionHelper: TrackSelectionHelper? = null
	private var eventLogger: EventLogger? = null
	private var lastSeenTrackGroupArray: Nothing? = null
	internal var player: SimpleExoPlayer? = null

	companion object {
		private var mediaSource: MediaSource? = null
		private var resumePosition: Long = 0
		private var resumeWindow: Int = 0

		private val BANDWIDTH_METER = DefaultBandwidthMeter()
		private val DEFAULT_COOKIE_MANAGER: CookieManager = CookieManager()

		init {
			DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
		}

		private fun isBehindLiveWindow(e: ExoPlaybackException): Boolean {
			if (e.type != ExoPlaybackException.TYPE_SOURCE) {
				return false
			}
			var cause = e.sourceException
			while (cause != null) {
				if (cause is BehindLiveWindowException) {
					return true
				}
				cause = cause.cause as IOException?
			}
			return false
		}
	}

	fun initial() {
		if (player == null) {
			val exoPlayerUtility = ExoPlayerUtility()
			val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory(BANDWIDTH_METER)
			defaultTrackSelection = DefaultTrackSelector(adaptiveTrackSelectionFactory)
			trackSelectionHelper = TrackSelectionHelper(
					defaultTrackSelection!!,
					adaptiveTrackSelectionFactory, exoPlayerUtility)
			lastSeenTrackGroupArray = null
			eventLogger = EventLogger(defaultTrackSelection)

			lastSeenTrackGroupArray = null
			resumePosition
			player = ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(content),
					defaultTrackSelection, DefaultLoadControl())
					.apply {
						addListener(PlayerEventListener())
						addListener(eventLogger)
						addMetadataOutput(eventLogger)
						addAudioDebugListener(eventLogger)
						addVideoDebugListener(eventLogger)
						playWhenReady = shouldAutoPlay
					}
			this.playerView.player = player
			this.playerView.setPlaybackPreparer(this)
		}

		debugViewHelper = DebugTextViewHelper(player, debugTextView)
		debugViewHelper?.start()
		val haveResumePosition = resumeWindow != C.INDEX_UNSET

		if (haveResumePosition) {
			player?.seekTo(resumeWindow, resumePosition)
		}
		inErrorState = false
		player?.prepare(mediaSource, !haveResumePosition, false)
	}

	fun playerView() = this.playerView

	fun createMediaHLS(hisUrl: String?): MediaSource {
		val userAgent = Util.getUserAgent(content,
				content.getString(R.string.exo_controls_play_description))
		val mediaHLS = HlsMediaSource(Uri.parse(hisUrl),
				DefaultDataSourceFactory(content, userAgent, BANDWIDTH_METER), 1, null, null)
		val loopingMediaSource = LoopingMediaSource(mediaHLS)
		mediaSource = loopingMediaSource
		return loopingMediaSource
	}

	fun createMediaSource(videoUrl: String): MediaSource {
		val userAgent = Util.getUserAgent(content,
				content.getString(R.string.exo_controls_play_description))

		mediaSource = ExtractorMediaSource(Uri.parse(videoUrl),
				DefaultHttpDataSourceFactory(userAgent),
				DefaultExtractorsFactory(), null, null)
		return mediaSource as ExtractorMediaSource
	}

	fun releasePlayer() {
		if (player != null) {
			debugViewHelper?.stop()
			debugViewHelper = null
			shouldAutoPlay = player?.playWhenReady ?: false
			updateResumePosition()
			player?.release()
			defaultTrackSelection = null
			trackSelectionHelper = null
			eventLogger = null
		}
	}

	private fun updateResumePosition() {
		resumeWindow = player?.currentWindowIndex ?: 0
		resumePosition = max(0, player?.contentPosition ?: 0)
	}

	private fun updateButtonVisibilities() {
		if (player == null) {
			return
		}
		val mappedTrackInfo = defaultTrackSelection?.currentMappedTrackInfo ?: return
		loop@ for (i in 0 until mappedTrackInfo.length) {
			val trackGroups = mappedTrackInfo.getTrackGroups(i)
			if (trackGroups.length != 0) {
				val button = Button(content)
				val label: String = when (player?.getRendererType(i)) {
					C.TRACK_TYPE_AUDIO -> "audio"
					C.TRACK_TYPE_VIDEO -> "video"
					C.TRACK_TYPE_TEXT -> "text"
					else -> continue@loop
				}
				button.text = label
				button.tag = i
				button.setOnClickListener(this)
			}
		}
	}

	override fun onClick(v: View?) {
		if (v?.parent === debugRootView) {
			val mappedTrackInfo = defaultTrackSelection?.currentMappedTrackInfo
			if (mappedTrackInfo != null) {
				trackSelectionHelper?.showSelectionDialog(content as Activity, (v as Button).text,
						mappedTrackInfo, v.getTag() as Int)
			}
		}
	}

	override fun preparePlayback() {
		initial()
	}

	override fun onVisibilityChange(visibility: Int) {
		debugRootView?.visibility = visibility
	}

	private inner class PlayerEventListener : Player.DefaultEventListener() {


		override fun onPositionDiscontinuity(reason: Int) {
			super.onPositionDiscontinuity(reason)
			if (!inErrorState) {
				updateResumePosition()
			}
		}

		override fun onPlayerError(e: ExoPlaybackException?) {
			super.onPlayerError(e)
			var errorString: String? = null
			if (e?.type == ExoPlaybackException.TYPE_RENDERER) {
				val cause = e.rendererException
				if (cause is MediaCodecRenderer.DecoderInitializationException) {
					// Special case for decoder initialization failures.
					errorString = if (cause.decoderName == null) {
						when {
							cause.cause is MediaCodecUtil.DecoderQueryException -> {
								"Unable to query device decoders"
							}
							cause.secureDecoderRequired -> {
								"This device does not provide a secure decoder for" + cause.mimeType
							}
							else -> {
								"This device does not provide a decoder for" + cause.mimeType
							}
						}
					} else {
						"Unable to instantiate decoder" + cause.decoderName
					}
				}
			}

			onError.invoke(true)
			if (errorString != null) {
				showToast(errorString)
			}

			inErrorState = true
			e?.let {
				if (isBehindLiveWindow(e)) {
					clearResumePosition()
					initial()
				} else {
					updateResumePosition()
					updateButtonVisibilities()
				}
			}
		}

		override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
			super.onPlayerStateChanged(playWhenReady, playbackState)
			if (playbackState == Player.STATE_ENDED) {
				showControls()
			}
			updateButtonVisibilities()
		}
	}

	private fun showToast(message: String?) {
		Toast.makeText(content, message, Toast.LENGTH_LONG)
				.show()
	}

	fun clearResumePosition() {
		resumeWindow = C.INDEX_UNSET
		resumePosition = C.TIME_UNSET
	}

	private fun showControls() {
		debugRootView?.visibility = View.VISIBLE
	}

	abstract class CallBack {
		abstract fun onError(status: Boolean)
	}

}