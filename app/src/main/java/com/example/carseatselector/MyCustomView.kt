package com.example.carseatselector

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.carseatselector.databinding.LayoutSomethingBinding

class MyCustomView : FrameLayout {

    private var textColor: Int? = null
    private var myCustomViewListener: MyCustomViewListener? = null

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
        val binding = LayoutSomethingBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyCustomView)
        textColor = typedArray.getColor(
            R.styleable.MyCustomView_myTextColor,
            context.getColor(R.color.black)
        )

        textColor?.let {
            binding.textTop.setTextColor(it)
        }

        binding.btnClick.setOnClickListener {
            myCustomViewListener?.onCustomClick()
        }

        typedArray.recycle()
    }

    fun setCustomClickListener(listener: MyCustomViewListener) {
        myCustomViewListener = listener
    }

    interface MyCustomViewListener {
        fun onCustomClick()
    }

}