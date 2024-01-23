package com.example.carseatselector

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
import com.example.carseatselector.databinding.LayoutCarBinding

private const val SELECTED_SEAT_TAGS = "SELECTED_SEAT_TAGS"

class CarSeatSelectorView : FrameLayout {

    private val seatViewMap = mutableMapOf<ImageView, Pair<ImageView, ImageView>>()
    private lateinit var animFadeOut: Animation
    private lateinit var animFadeIn: Animation
    private var selectedSeatTags: ArrayList<String> = arrayListOf()
    private var unavailableSeatTags: ArrayList<String> = arrayListOf()
    private var listener: CarSeatSelectorListener? = null

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
        val binding = LayoutCarBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)

        animFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        animFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)

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

//        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyCustomView)
//        textColor = typedArray.getColor(
//            R.styleable.MyCustomView_myTextColor,
//            context.getColor(R.color.black)
//        )

//        typedArray.recycle()
    }

    private fun onSeatClick(view: View, isRed: Boolean = false) {
        animFadeOut.reset()
        animFadeIn.reset()

        selectedSeatTags.forEach { selectedSeatTag ->
            seatViewMap.entries.filter { it.key.tag == selectedSeatTag }.getOrNull(0)
                ?.also { mutableEntry ->
                    mutableEntry.key.clearAnimation()
                    mutableEntry.value.first.clearAnimation()
                    mutableEntry.value.second.clearAnimation()
//                    mutableEntry.value.first.isVisible = false
//                    mutableEntry.value.second.isVisible = false
                }
        }

//        seatViewMap.forEach { entry ->
//            entry.key.clearAnimation()
//            entry.value.first.clearAnimation()
//            entry.value.second.clearAnimation()
////            entry.value.first.isVisible = false
////            entry.value.second.isVisible = false
//        }

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
                }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().also {
            it.putParcelable("superState", super.onSaveInstanceState())
            it.putStringArrayList(SELECTED_SEAT_TAGS, selectedSeatTags)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Parcelable? = null
        if (state is Bundle) {
            this.selectedSeatTags =
                state.getStringArrayList(SELECTED_SEAT_TAGS) as ArrayList<String>
            superState = state.customGetParcelable<Parcelable>("superState")
        }

        seatViewMap.forEach { seatView ->
            selectedSeatTags.forEach { selectedSeatTag ->
                if (seatView.key.tag == selectedSeatTag) {
                    onSeatClick(seatView.key)
                }
            }
        }

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