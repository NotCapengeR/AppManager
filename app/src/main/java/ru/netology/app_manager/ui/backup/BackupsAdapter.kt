package ru.netology.app_manager.ui.backup

import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.netology.app_manager.R
import ru.netology.app_manager.core.api.models.Backup
import ru.netology.app_manager.core.templates.ItemViewHolder
import ru.netology.app_manager.databinding.BackupFragmentItemBinding
import ru.netology.app_manager.utils.setDebouncedListener

interface BackupListener {
    fun onDelete(backup: Backup)

    fun onInstall(backup: Backup)
}

class BackupsAdapter(
    private val listener: BackupListener
) : ListAdapter<Backup, BackupsAdapter.BackupViewHolder>(Comparator), View.OnClickListener {

    override fun onClick(view: View) {
        if (view.id == R.id.ivMenu) {
            showPopup(view)
        }
    }


    inner class BackupViewHolder(
        private val binding: BackupFragmentItemBinding
    ) : ItemViewHolder<Backup>(binding.root) {

        override fun bind(item: Backup) = with(binding) {
            bindId(item)
            bindComment(item)
        }

        fun bindId(item: Backup) = with(binding) {
            tvId.text = item.id.toString()
            bindMenu(item)
        }

        private fun bindMenu(item: Backup) = with(binding) {
            ivMenu.tag = item
            ivMenu.setDebouncedListener(300L, this@BackupsAdapter)
        }

        fun bindComment(item: Backup) = with(binding) {
            tvComment.text = item.comment ?: "No comment"
            bindMenu(item)
        }
    }


    private fun showPopup(anchor: View) {
        val context = anchor.context
        val popup = PopupMenu(context, anchor)
        val backup = anchor.tag as Backup

        popup.menu.add(0, REMOVE_ID, Menu.NONE, context.getString(R.string.delete))
        popup.menu.add(0, INSTALL_ID, Menu.NONE, context.getString(R.string.download))
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                REMOVE_ID -> listener.onDelete(backup)
                INSTALL_ID -> listener.onInstall(backup)
            }
            return@setOnMenuItemClickListener true
        }
        popup.show()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupViewHolder {
        return BackupViewHolder(
            BackupFragmentItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: BackupViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            return super.onBindViewHolder(holder, position, payloads)
        }
        payloads.forEach { payload ->
            if (payload is List<*>) {
                val item = getItem(position)
                if (payload.contains(ID_CHANGED)) holder.bindId(item)
                if (payload.contains(COMMENT_CHANGED)) holder.bindComment(item)
            }
        }
    }

    override fun onBindViewHolder(holder: BackupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object Comparator : DiffUtil.ItemCallback<Backup>() {
        override fun areItemsTheSame(oldItem: Backup, newItem: Backup): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Backup, newItem: Backup): Boolean {
            return newItem == oldItem
        }

        override fun getChangePayload(oldItem: Backup, newItem: Backup): List<Int> {
            val payloads = mutableListOf<Int>()
            if (oldItem.id != newItem.id) payloads.add(ID_CHANGED)
            if (oldItem.comment != newItem.comment) payloads.add(COMMENT_CHANGED)

            return payloads
        }
    }

    private companion object {
        private const val ID_CHANGED = 1
        private const val COMMENT_CHANGED = 2

        private const val REMOVE_ID = 1
        private const val INSTALL_ID = 2
    }
}