package ru.netology.app_manager.ui.activities_list

import android.content.Context
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.netology.app_manager.R
import ru.netology.app_manager.core.apk.manager.ApkExtractor
import ru.netology.app_manager.core.apk.manager.ApkExtractor.launchActivity
import ru.netology.app_manager.core.apk.models.ActivityData
import ru.netology.app_manager.core.apk.models.Apk
import ru.netology.app_manager.core.helper.adapter_decorators.LinearVerticalSpacingDecoration
import ru.netology.app_manager.core.templates.ItemViewHolder
import ru.netology.app_manager.databinding.ActivityListItemBinding
import ru.netology.app_manager.ui.app_list.AppListAdapter
import ru.netology.app_manager.utils.AndroidUtils


class ParentActivityListAdapter(
    private val context: Context,
) : ListAdapter<Apk, ParentActivityListAdapter.ParentActivityListViewHolder>(Comparator) {

    inner class ParentActivityListViewHolder(
        private val binding: ActivityListItemBinding
    ) : ItemViewHolder<Apk>(binding.root) {
        override fun bind(item: Apk) = with(binding) {
            tvAppName.text = item.appName
            tvPackageName.text = item.packageName
            ivAppIcon.setImageDrawable(context.packageManager?.getApplicationIcon(item.appInfo))
            val adapter = ChildActivitiesListAppAdapter(
                context = context,
                callback = { item, anchor ->
                    showPopupMenu {
                        PopupMenu(anchor.context, anchor).apply {
                            menu.add(
                                0,
                                APP_LAUNCH_ID,
                                Menu.NONE,
                                context.getString(R.string.activity_launch)
                            )
                            setOnMenuItemClickListener { menuItem ->
                                if (menuItem.itemId == APP_LAUNCH_ID) {
                                    launchActivity(item)
                                }
                                return@setOnMenuItemClickListener true
                            }
                        }
                    }
                }
            )

            rvActivities.adapter = adapter
            rvActivities.addItemDecoration(
                LinearVerticalSpacingDecoration(INNER_SPACING_RC_VIEW.dpToPx())
            )
            adapter.submitList(getAllActivitiesFromApp(item.packageName))
        }

    }

    private fun Int.dpToPx(): Int = AndroidUtils.dpToPx(context, this)


    private fun showPopupMenu(inflater: () -> PopupMenu) {
        val popupMenu = inflater.invoke()
        popupMenu.show()
    }

    private fun getAllActivitiesFromApp(packageName: String): List<ActivityData> =
        ApkExtractor.getAllActivitiesFromApp(
            context,
            packageName
        )

    private fun launchActivity(packageName: String, activityName: String) {
        ApkExtractor.launchActivity(packageName, activityName, context)
    }

    private fun launchActivity(activity: ActivityData) {
        launchActivity(activity.packageName, activity.name)
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ParentActivityListViewHolder {
        return ParentActivityListViewHolder(
            ActivityListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ParentActivityListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private const val APP_LAUNCH_ID = 1

        private const val PAYLOAD_APP_NAME = 1
        private const val PAYLOAD_PACKAGE_NAME = 1
        private const val INNER_SPACING_RC_VIEW = 10
    }
}