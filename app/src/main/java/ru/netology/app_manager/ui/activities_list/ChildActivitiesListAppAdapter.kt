package ru.netology.app_manager.ui.activities_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.netology.app_manager.R
import ru.netology.app_manager.core.apk.models.ActivityData
import ru.netology.app_manager.core.templates.ItemViewHolder
import ru.netology.app_manager.databinding.AppListItemBinding
import ru.netology.app_manager.utils.setDebouncedListener

class ChildActivitiesListAppAdapter(
    private val context: Context,
    private val callback: (item: ActivityData, anchor: View) -> Unit
) : ListAdapter<ActivityData, ChildActivitiesListAppAdapter.ActivitiesListAppViewHolder>(Comparator),
    View.OnClickListener {

    override fun onClick(view: View) {
        if (view.id == R.id.app_list_item) {
            val data = view.tag as ActivityData
            callback.invoke(data, view)
        }
    }

    inner class ActivitiesListAppViewHolder(
        private val binding: AppListItemBinding
    ) : ItemViewHolder<ActivityData>(binding.root) {

        override fun bind(item: ActivityData) = with(binding) {
            appListItem.tag = item
            appListItem.setDebouncedListener(200L, this@ChildActivitiesListAppAdapter)
            tvPackageName.text = ""
            bindName(item)
            ivAppIcon.setImageDrawable(context.packageManager.getApplicationIcon(item.appInfo))
        }

        fun bindName(item: ActivityData) = with(binding) {
            tvAppName.text = item.name
        }

    }

    private object Comparator : DiffUtil.ItemCallback<ActivityData>() {
        override fun areItemsTheSame(oldItem: ActivityData, newItem: ActivityData): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ActivityData, newItem: ActivityData): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: ActivityData, newItem: ActivityData): Int? {
            if (oldItem.name != newItem.name) return PAYLOAD_NAME

            return null
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesListAppViewHolder {
        return ActivitiesListAppViewHolder(
            AppListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ActivitiesListAppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: ActivitiesListAppViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            return super.onBindViewHolder(holder, position, payloads)
        }
        payloads.forEach { payload ->
            if (payload is Int && payload == PAYLOAD_NAME) {
                holder.bindName(getItem(position))
            }
        }
    }

    private companion object {
        private const val PAYLOAD_NAME = 1
    }
}