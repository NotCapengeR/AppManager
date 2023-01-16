package ru.netology.app_manager.core.templates

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.netology.app_manager.utils.AndroidUtils

abstract class ItemViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(item: T)

    protected fun Int.dpToPx(): Int = AndroidUtils.dpToPx(itemView.context, this)
}