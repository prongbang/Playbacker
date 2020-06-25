package com.prongbang.playbacker.core.exo

import android.text.TextUtils
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.util.MimeTypes
import java.util.*

class ExoPlayerUtility {

	fun buildTrackName(format: Format): String {
		val trackName: String
		when {
			MimeTypes.isVideo(format.sampleMimeType) -> {
				trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(
						buildResolutionString(format), buildBitrateString(format)),
						buildTrackIdString(format)),
						buildSampleMimeTypeString(format))
			}
			MimeTypes.isAudio(format.sampleMimeType) -> {
				trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(joinWithSeparator(
						buildLanguageString(format), buildAudioPropertyString(format)),
						buildBitrateString(format)), buildTrackIdString(format)),
						buildSampleMimeTypeString(format))
			}
			else -> {
				trackName = joinWithSeparator(joinWithSeparator(
						joinWithSeparator(buildLanguageString(format), buildBitrateString(format)),
						buildTrackIdString(format)), buildSampleMimeTypeString(format))
			}
		}
		return if (trackName.isEmpty()) "unknown" else trackName
	}

	private fun buildResolutionString(format: Format): String {
		return if (format.width == Format.NO_VALUE || format.height == Format.NO_VALUE)
			""
		else
			format.width.toString() + "x" + format.height
	}

	private fun buildAudioPropertyString(format: Format): String {
		return if (format.channelCount == Format.NO_VALUE || format.sampleRate == Format.NO_VALUE)
			""
		else
			format.channelCount.toString() + "ch, " + format.sampleRate + "Hz"
	}

	private fun buildLanguageString(format: Format): String {
		return if (TextUtils.isEmpty(format.language) || "und" == format.language)
			""
		else
			format.language
	}

	private fun buildBitrateString(format: Format): String {
		return if (format.bitrate == Format.NO_VALUE)
			""
		else
			String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f)
	}

	private fun joinWithSeparator(first: String, second: String): String {
		return if (first.isEmpty()) second else if (second.isEmpty()) first else "$first, $second"
	}

	private fun buildTrackIdString(format: Format): String {
		return if (format.id == null) "" else "id:" + format.id
	}

	private fun buildSampleMimeTypeString(format: Format): String {
		return if (format.sampleMimeType == null) "" else format.sampleMimeType
	}
}