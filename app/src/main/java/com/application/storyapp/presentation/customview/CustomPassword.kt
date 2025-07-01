package com.application.storyapp.presentation.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.application.storyapp.R

class CustomPassword : AppCompatEditText {

    constructor(context: Context) : super(context) {

        init()

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        init()

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        init()

    }

    private fun init() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                error = when {
                    s.toString().isEmpty() -> {
                        context.getString(R.string.error_pass)
                    }

                    s.toString().length < 8 -> {
                        context.getString(R.string.error_pass_long)
                    }

                    else -> null
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }
}