package ru.netology.app_manager.utils

import android.os.SystemClock
import android.view.View

fun View.setDebouncedListener(debounceTime: Long = 600L, onClickListener: View.OnClickListener) {
    var lastClickTime: Long = 0
    val clickWithDebounce: (view: View) -> Unit = {

        if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) { /* do nothing */
        } else onClickListener.onClick(it)
    }
    lastClickTime = SystemClock.elapsedRealtime()
    this.setOnClickListener(clickWithDebounce)
}

fun View.setVisibility(visible: Boolean?) = when (visible) {
    true -> this.visibility = View.VISIBLE
    false -> this.visibility = View.GONE
    null -> this.visibility = View.INVISIBLE
}

val View.isVisible: Boolean
    get() = this.visibility == View.VISIBLE
