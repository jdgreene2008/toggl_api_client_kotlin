package com.jarvis.kotlin.ui.widget

import android.content.Context
import android.support.design.widget.TextInputEditText
import android.util.AttributeSet
import com.jarvis.kotlin.ui.activity.BaseActivity


class ObservantTextInputEditText : TextInputEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttrs: Int) : super(context, attributeSet, defStyleAttrs)


    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        (context as BaseActivity).onUserInteraction()
    }
}
