package com.idyllic.car_seat_selector

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import com.idyllic.car_seat_selector.databinding.LayoutCarBinding

private const val SUPER_STATE = "SUPER_STATE"
private const val SELECTED_SEAT_TAGS = "SELECTED_SEAT_TAGS"
private const val UNAVAILABLE_SEAT_TAGS = "UNAVAILABLE_SEAT_TAGS"
private const val IS_MULTI_SELECT = "IS_MULTI_SELECT"

class CarSeatSelectorView : FrameLayout {

    private val seatViewMap = mutableMapOf<ImageView, Pair<ImageView, ImageView>>()
    private lateinit var animFadeOut: Animation
    private lateinit var animFadeIn: Animation
    private var selectedSeatTags: ArrayList<String> = arrayListOf()
    private var unavailableSeatTags: ArrayList<String> = arrayListOf()
    private var listener: CarSeatSelectorListener? = null
    private var _isMultiSelect = false
    val isMultiSelect get() = _isMultiSelect

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val binding = LayoutCarBinding.inflate(
            LayoutInflater.from(context)
        )
        addView(binding.root)

        animFadeOut =
            AnimationUtils.loadAnimation(context, R.anim.fade_out)
        animFadeIn =
            AnimationUtils.loadAnimation(context, R.anim.fade_in)

        binding.apply {
            seatViewMap[seatTopRight] = Pair(seatTopRightRed, seatTopRightGreen)
            seatViewMap[seatBottomLeft] = Pair(seatBottomLeftRed, seatBottomLeftGreen)
            seatViewMap[seatBottomMiddle] = Pair(seatBottomMiddleRed, seatBottomMiddleGreen)
            seatViewMap[seatBottomRight] = Pair(seatBottomRightRed, seatBottomRightGreen)

            seatViewMap.forEach { entry ->
                entry.key.setOnClickListener {
                    onSeatClick(it)
                }
                entry.value.second.setOnClickListener {
                    onSeatClick(it)
                }
            }
        }

        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CarSeatSelectorView
        )
        _isMultiSelect = typedArray.getBoolean(
            R.styleable.CarSeatSelectorView_multiSelect,
            false
        )

        typedArray.recycle()
    }

    private fun onSeatClick(view: View) {
        animFadeOut.reset()
        animFadeIn.reset()

        val seatsToRemove = arrayListOf<Any>()
        selectedSeatTags.forEach { selectedSeatTag ->
            seatViewMap.entries.filter { it.key.tag == selectedSeatTag }.getOrNull(0)
                ?.also { mutableEntry ->
                    mutableEntry.key.clearAnimation()
                    mutableEntry.value.first.clearAnimation()
                    mutableEntry.value.second.clearAnimation()

                    if (!_isMultiSelect) {
                        mutableEntry.value.first.isVisible = false
                        mutableEntry.value.second.isVisible = false
                        seatsToRemove.add(mutableEntry.key.tag)
                    }
                }
        }
        selectedSeatTags.removeAll(seatsToRemove.toSet())

        seatViewMap.forEach { entry ->
            if (entry.key == view) {
                entry.value.second.startAnimation(animFadeIn)
                entry.value.second.isVisible = true
                if (!selectedSeatTags.contains(entry.key.tag.toString())) {
                    selectedSeatTags.add(entry.key.tag.toString())
                }
                return@forEach
            } else if (entry.value.second == view) {
                entry.value.second.startAnimation(animFadeOut)
                entry.value.second.isVisible = false
                selectedSeatTags.remove(entry.key.tag)
                return@forEach
            }
        }

        val selectedSeatPositions: MutableList<Int> = arrayListOf()
        seatViewMap.entries.forEachIndexed { index, mutableEntry ->
            selectedSeatTags.forEach { tag ->
                if (tag == mutableEntry.key.tag?.toString()) {
                    selectedSeatPositions.add(index + 1)
                    return@forEachIndexed
                }
            }
        }

        listener?.onSeatClick(selectedSeatPositions)
    }

    fun setUnavailable(seatPositions: List<Int>) {
        seatPositions.forEach { seatPosition ->
            seatViewMap.entries.filter { it.key.tag == "seat_${seatPosition}" }
                .getOrNull(0)?.also { mutableEntry ->
                    mutableEntry.key.isEnabled = false
                    mutableEntry.value.first.isVisible = true
                    mutableEntry.value.first.isEnabled = false
                    mutableEntry.value.second.isEnabled = false
                    unavailableSeatTags.add(mutableEntry.key.tag.toString())
                    selectedSeatTags.remove(mutableEntry.key.tag.toString())
                }
        }
    }

    fun setIsMultiSelect(isMultiSelect: Boolean) {
        this._isMultiSelect = isMultiSelect
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().also {
            it.putParcelable(SUPER_STATE, super.onSaveInstanceState())
            it.putStringArrayList(SELECTED_SEAT_TAGS, selectedSeatTags)
            it.putStringArrayList(UNAVAILABLE_SEAT_TAGS, unavailableSeatTags)
            it.putBoolean(IS_MULTI_SELECT, isMultiSelect)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Parcelable? = null
        if (state is Bundle) {
            this.selectedSeatTags =
                state.getStringArrayList(SELECTED_SEAT_TAGS) as ArrayList<String>
            this.unavailableSeatTags =
                state.getStringArrayList(UNAVAILABLE_SEAT_TAGS) as ArrayList<String>
            this._isMultiSelect = state.getBoolean(IS_MULTI_SELECT)
            superState = state.customGetParcelable<Parcelable>(SUPER_STATE)
        }

        val newSelectedSeatTags = arrayListOf<Any>()
        newSelectedSeatTags.addAll(selectedSeatTags)
        seatViewMap.forEach { seatView ->
            newSelectedSeatTags.forEach { selectedSeatTag ->
                if (seatView.key.tag == selectedSeatTag) {
                    seatView.value.second.isVisible = true
//                    onSeatClick(seatView.key)
                }
            }
        }


        val selectedSeatPositions: MutableList<Int> = arrayListOf()
        seatViewMap.entries.forEachIndexed { index, mutableEntry ->
            selectedSeatTags.forEach { tag ->
                if (tag == mutableEntry.key.tag?.toString()) {
                    selectedSeatPositions.add(index + 1)
                    return@forEachIndexed
                }
            }
        }
        listener?.onSeatClick(selectedSeatPositions)

        superState?.let {
            super.onRestoreInstanceState(it)
        }
    }

    fun setCarSeatSelectorListener(listener: CarSeatSelectorListener) {
        this.listener = listener
    }

    @Suppress("DEPRECATION")
    private inline fun <reified T : Parcelable> Bundle.customGetParcelable(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(key, T::class.java)
        } else {
            getParcelable(key) as? T
        }
    }

    interface CarSeatSelectorListener {
        fun onSeatClick(selectedSeatPositions: List<Int>)
    }

}