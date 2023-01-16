package ru.netology.app_manager.ui.app_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.netology.app_manager.R
import ru.netology.app_manager.core.apk.models.Apk
import ru.netology.app_manager.core.templates.ItemViewHolder
import ru.netology.app_manager.databinding.AppListItemBinding
import ru.netology.app_manager.utils.setDebouncedListener

interface AppListListener {

    fun onDetails(apk: Apk)
}

class AppListAdapter(
    private val context: Context,
    private val listener: AppListListener
) : ListAdapter<Apk, AppListAdapter.AppListViewHolder>(Comparator), View.OnClickListener {

    override fun onClick(view: View) {
        when (view.id) {
            R.id.app_list_item -> {
                val apk = view.tag as Apk
                listener.onDetails(apk)
            }
            else -> {}
        }
    }

    inner class AppListViewHolder(
        private val binding: AppListItemBinding
    ) : ItemViewHolder<Apk>(binding.root) {
        override fun bind(item: Apk) = with(binding) {
            ivAppIcon.setImageDrawable(context.packageManager.getApplicationIcon(item.appInfo))
            appListItem.tag = item
            appListItem.setDebouncedListener(300L, this@AppListAdapter)
            bindAppName(item)
            bindPackageName(item)
        }

        fun bindAppName(item: Apk) = with(binding) {
            tvAppName.text = item.appName
        }
        fun bindPackageName(item: Apk) = with(binding) {
            tvPackageName.text = item.packageName
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppListViewHolder {
        return AppListViewHolder(
            AppListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: AppListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: AppListViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
          return  super.onBindViewHolder(holder, position, payloads)
        }
        val item = getItem(position)
        payloads.forEach { payload ->
            if (payload is List<*>) {
                if (payloads.contains(PAYLOAD_APP_NAME)) holder.bindAppName(item)
                if (payloads.contains(PAYLOAD_PACKAGE_NAME)) holder.bindPackageName(item)
            }
        }
    }

    private object Comparator : DiffUtil.ItemCallback<Apk>() {
        override fun areItemsTheSame(oldItem: Apk, newItem: Apk): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: Apk, newItem: Apk): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Apk, newItem: Apk): List<Int> {
            val payloads: MutableList<Int> = mutableListOf()
            if (oldItem.appName != newItem.appName) {
                payloads.add(PAYLOAD_APP_NAME)
            }
            if (oldItem.packageName != newItem.packageName) {
                payloads.add(PAYLOAD_PACKAGE_NAME)
            }
            return payloads
        }

    }

    private companion object {
        private const val PAYLOAD_APP_NAME = 1
        private const val PAYLOAD_PACKAGE_NAME = 1
    }
}