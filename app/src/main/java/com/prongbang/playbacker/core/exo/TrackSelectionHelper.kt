package com.prongbang.playbacker.core.exo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckedTextView
import android.widget.LinearLayout
import com.google.android.exoplayer2.RendererCapabilities
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.FixedTrackSelection
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.SelectionOverride
import com.google.android.exoplayer2.trackselection.RandomTrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.prongbang.playbacker.R
import java.util.*

class TrackSelectionHelper(
		selector: MappingTrackSelector,
		adaptiveTrackSelectionFactory: TrackSelection.Factory,
		private val exoPlayerUtility: ExoPlayerUtility
) : View.OnClickListener, DialogInterface.OnClickListener {

	private val defaultView: CheckedTextView? = null
	private var enableRandomAdaptationView: CheckedTextView? = null
	private val randomFactory = RandomTrackSelection.Factory()
	private var adaptiveTrackSelectionFactory: TrackSelection.Factory? = null
	private var selector: MappingTrackSelector? = null
	private var trackGroups: TrackGroupArray? = null
	private var trackGroupsAdaptive: BooleanArray? = null
	private var isDisabled: Boolean? = null
	private var override: SelectionOverride? = null
	private lateinit var disableView: CheckedTextView
	private var rendererIndex: Int? = 0
	private var trackViews: Array<Array<CheckedTextView>?>? = null
	private val fixedFactory = FixedTrackSelection.Factory()
	private var trackInfo: MappedTrackInfo? = null

	init {
		this.selector = selector
		this.adaptiveTrackSelectionFactory = adaptiveTrackSelectionFactory
	}

	fun showSelectionDialog(activity: Activity, title: CharSequence, trackInfo: MappedTrackInfo,
	                        rendererIndex: Int) {
		this.trackInfo = trackInfo
		this.rendererIndex = rendererIndex
		trackGroups = trackInfo.getTrackGroups(rendererIndex)
		trackGroupsAdaptive = BooleanArray(trackGroups?.length!!)

		for (i in 0 until trackGroups?.length!!) {
			trackGroupsAdaptive!![i] = (adaptiveTrackSelectionFactory != null
					&& trackInfo.getAdaptiveSupport(rendererIndex, i, false) != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED
					&& trackGroups?.get(i)!!.length > 1)
		}
		isDisabled = selector?.getRendererDisabled(rendererIndex)
		override = selector?.getSelectionOverride(rendererIndex, trackGroups)
		val builder = AlertDialog.Builder(activity)
		builder.setTitle(title)
				.setView(buildView(builder.context))
				.setPositiveButton(android.R.string.ok, this@TrackSelectionHelper)
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show()
	}

	private fun buildView(context: Context?): View {
		val inflater = LayoutInflater.from(context)
		val view = inflater.inflate(R.layout.track_selection_dialog, null)
		val root = view.findViewById<LinearLayout>(R.id.root)

		val attributeArray = context?.theme?.obtainStyledAttributes(
				intArrayOf(android.R.attr.selectableItemBackground))
		val selectableItemBackgroundResourceId = attributeArray?.getResourceId(0, 0)
		attributeArray?.recycle()

		// View for disabling the renderer.
		disableView = inflater.inflate(android.R.layout.simple_list_item_single_choice, root,
				false) as CheckedTextView
		disableView.setBackgroundResource(selectableItemBackgroundResourceId!!)
		disableView.setText(R.string.selection_disabled)
		disableView.isFocusable = true
		disableView.setOnClickListener(this)
		root.addView(disableView)

		// Per-track views.
		var haveAdaptiveTracks = false
		trackViews = arrayOfNulls(trackGroups?.length!!)
		for (groupIndex in 0 until trackGroups?.length!!) {
			val group = trackGroups!!.get(groupIndex)
			val groupIsAdaptive = trackGroupsAdaptive!![groupIndex]
			haveAdaptiveTracks = haveAdaptiveTracks or groupIsAdaptive

			trackViews!![groupIndex] = trackViews!![trackGroups?.length!!]
			for (trackIndex in 0 until group?.length!!) {
				if (trackIndex == 0) {
					root.addView(inflater.inflate(R.layout.list_divider, root, false))
				}
				val trackViewLayoutId = if (groupIsAdaptive)
					android.R.layout.simple_list_item_multiple_choice
				else
					android.R.layout.simple_list_item_single_choice
				val trackView = inflater.inflate(
						trackViewLayoutId, root, false) as CheckedTextView
				trackView.setBackgroundResource(selectableItemBackgroundResourceId)
				trackView.text = exoPlayerUtility.buildTrackName(group.getFormat(trackIndex))
				if (rendererIndex?.let {
							trackInfo?.getTrackFormatSupport(it, groupIndex, trackIndex)
						} == RendererCapabilities.FORMAT_HANDLED) {
					trackView.isFocusable = true
					trackView.tag = Pair(groupIndex, trackIndex)
					trackView.setOnClickListener(this)
				} else {
					trackView.isFocusable = false
					trackView.isEnabled = false
				}
				trackViews!![groupIndex]?.set(trackIndex, trackView)
				root.addView(trackView)
			}
		}
		if (haveAdaptiveTracks) {
			// View for using random adaptation.
			enableRandomAdaptationView = inflater.inflate(
					android.R.layout.simple_list_item_multiple_choice, root,
					false) as CheckedTextView
			enableRandomAdaptationView!!.apply {
				setBackgroundResource(selectableItemBackgroundResourceId)
				setText(R.string.enable_random_adaptation)
				setOnClickListener(this@TrackSelectionHelper)
			}
			root.addView(inflater.inflate(R.layout.list_divider, root, false))
			root.addView(enableRandomAdaptationView)
		}

		updateViews()
		return view
	}

	private fun updateViews() {
		disableView.isChecked = isDisabled!!
		defaultView?.isChecked = !isDisabled!! && override == null
		for (i in 0 until trackViews?.count()!!) {
			for (j in 0 until trackViews?.get(i)
					?.count()!!) {
				trackViews!![i]!![j].isChecked = (override != null && override!!.groupIndex == i
						&& override!!.containsTrack(j))
			}
		}
		if (enableRandomAdaptationView != null) {
			val enableView = !isDisabled!! && override != null && override?.length!! > 1
			enableRandomAdaptationView?.isEnabled = enableView
			enableRandomAdaptationView?.isFocusable = enableView
			if (enableView) {
				enableRandomAdaptationView!!.isChecked = !isDisabled!! && override!!.factory is RandomTrackSelection.Factory
			}
		}
	}

	override fun onClick(v: View?) {
		if (v === disableView) {
			isDisabled = true
			override = null
		} else if (v === defaultView) {
			isDisabled = false
			override = null
		} else if (v === enableRandomAdaptationView) {
			setOverride(override?.groupIndex, override?.tracks,
					!enableRandomAdaptationView?.isChecked!!)
		} else {
			val tag = v?.tag as Pair<Int, Int>
			isDisabled = false
			val groupIndex = tag.first
			val trackIndex = tag.second

			if (!trackGroupsAdaptive!![groupIndex] || override == null
					|| override?.groupIndex !== groupIndex) {
				override = SelectionOverride(fixedFactory, groupIndex, trackIndex)
			} else {
				// The group being modified is adaptive and we already have a non-null override.
				val isEnabled = (v as CheckedTextView).isChecked
				val overrideLength = override?.length
				if (isEnabled) {
					// Remove the track from the override.
					if (overrideLength == 1) {
						// The last track is being removed, so the override becomes empty.
						override = null
						isDisabled = true
					} else {
						setOverride(groupIndex, getTracksRemoving(override!!, trackIndex),
								enableRandomAdaptationView?.isChecked!!)
					}
				} else {
					// Add the track to the override.
					setOverride(groupIndex, getTracksAdding(override!!, trackIndex),
							enableRandomAdaptationView?.isChecked!!)
				}
			}
		}
		updateViews()
	}

	private fun getTracksRemoving(override: SelectionOverride,
	                              removedTrack: Any): IntArray? {
		val tracks = IntArray(override.length - 1)
		var trackCount = 0
		for (i in 0 until tracks.size + 1) {
			val track = override.tracks[i]
			if (track != removedTrack) {
				tracks[trackCount++] = track
			}
		}
		return tracks
	}

	private fun getTracksAdding(override: SelectionOverride,
	                            addedTrack: Int): IntArray? {
		var tracks = override.tracks
		tracks = Arrays.copyOf(tracks, tracks.size + 1)
		tracks[tracks.size - 1] = addedTrack
		return tracks
	}

	private fun setOverride(group: Int?, tracks: IntArray?, enableRandomAdaptation: Boolean) {
		val factory = when {
			tracks?.count() == 1 -> fixedFactory
			enableRandomAdaptation -> randomFactory
			else -> adaptiveTrackSelectionFactory
		}
		override = SelectionOverride(factory, group!!, tracks!![0])

	}

	override fun onClick(dialog: DialogInterface?, which: Int) {
		rendererIndex?.let { isDisabled?.let { it1 -> selector?.setRendererDisabled(it, it1) } }
		if (override != null) {
			rendererIndex?.let { selector?.setSelectionOverride(it, trackGroups, override) }
		} else {
			rendererIndex?.let { selector?.clearSelectionOverrides(it) }
		}
	}

}
