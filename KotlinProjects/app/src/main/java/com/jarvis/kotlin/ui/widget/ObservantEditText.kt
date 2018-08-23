package com.jarvis.kotlin.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.jarvis.kotlin.ui.activity.BaseActivity

class ObservantEditText : EditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttrs: Int) : super(context, attributeSet, defStyleAttrs)


    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        (context as BaseActivity).onUserInteraction()
    }
}
