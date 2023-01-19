package vandy.mooc.prime.utils

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView

/**
 * TextView receiver extension that installs a text change listener that
 * will automatically scroll the TextView's ScrollView parent window so that
 * the last line of text is always visible in the parent view.
 */
fun TextView.autoScroll() {
    check(parent is ScrollView) {
        "TextView.autoscroll extension requires TextView parent to be a ScrollView"
    }

    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            (parent as? ScrollView)?.fullScroll(ScrollView.FOCUS_DOWN)
        }
    })
}

/**
 * Add EditText extension that enables the widget to clear
 * input with right button
 *
 * @param onIsNotEmpty callback invoked when input is completed and and is not empty
 * @param onCanceled callback which invoked when cancel button is clicked.
 * @param clearDrawable right drawable which is used as cancel/clear button
 */
fun EditText.makeClearEditText(
        onIsNotEmpty: (() -> Unit)?,
        onCanceled: (() -> Unit)?,
        clearDrawable: Drawable
) {
    updateRightDrawable(clearDrawable)

    afterTextChanged {
        if (it.isNotEmpty()) {
            onIsNotEmpty?.invoke()
        }

        updateRightDrawable(clearDrawable)
    }

    onRightDrawableClicked {
        text.clear()
        updateRightDrawable(null)
        onCanceled?.invoke()
        requestFocus()
    }

    setOnFocusChangeListener { v, focused ->
        updateRightDrawable(clearDrawable)
        if (!focused) {
            UiUtils.hideKeyboard(v.context, v.windowToken)
        }
    }
}

private fun EditText.updateRightDrawable(clearDrawable: Drawable?) {
    setCompoundDrawables(
            null,
            null,
            if (clearDrawable != null && hasFocus() && text.isNotEmpty()) clearDrawable else null,
            null)
}

/**
 *
 * Calculate right compound drawable and in case it exists calls
 * @see EditText.makeClearEditText
 *
 * Arguments:
 *  @param onIsNotEmpty - callback which is invoked when input is completed and is not empty. Is good for clearing error
 *  @param onCanceled - callbacks which is invoked when cancel button is clicked and input is cleared
 */
fun EditText.makeClearEditText(onIsNotEmpty: (() -> Unit)?, onCanceled: (() -> Unit)?) {
    compoundDrawables[COMPOUND_DRAWABLE_RIGHT_INDEX]?.let { clearDrawable ->
        makeClearEditText(onIsNotEmpty, onCanceled, clearDrawable)
    }
}

/**
 * Based on View.OnTouchListener. Be careful EditText replaces old View.OnTouchListener when setting new one
 */
@SuppressLint("ClickableViewAccessibility")
private fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
    setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is EditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}

/**
 * Private extension function that installs a text change listener on an
 * EditText view to invoke the passed lambda after each text change event.
 */
private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

/**
 * Index of right compound drawable in EditText view widget.
 */
private const val COMPOUND_DRAWABLE_RIGHT_INDEX = 2
