package ru.netology.app_manager.ui.virustotal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.netology.app_manager.R
import ru.netology.app_manager.core.api.models.VirusTotalFileResponse
import ru.netology.app_manager.core.templates.ItemViewHolder
import ru.netology.app_manager.databinding.VirusTotalResultItemBinding

typealias VirusTotalItem = VirusTotalFileResponse.AnalysisResult

class VirusTotalAdapter :
    ListAdapter<VirusTotalItem, VirusTotalAdapter.VirusTotalViewHolder>(Comparator) {

    inner class VirusTotalViewHolder(
        private val binding: VirusTotalResultItemBinding
    ) : ItemViewHolder<VirusTotalItem>(binding.root) {

        override fun bind(item: VirusTotalItem): Unit = with(binding) {
            bindText(item)
            bindEngine(item)
        }

        fun bindText(item: VirusTotalItem) = with(binding) {
            val context = binding.root.context
            val analysis = item.result
            tvDetected.text = analysis ?: "Undetected"
            if (analysis == null) {
                tvDetected.setTextColor(context.getColor(R.color.green))
            } else {
                tvDetected.setTextColor(context.getColor(R.color.red))
            }
            if (analysis != null) {
                ivDetected.setImageResource(R.drawable.alert_circle_outline)
            } else {
                ivDetected.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
            }
        }

        fun bindEngine(item: VirusTotalItem) = with(binding) {
            tvEngine.text = item.engineName
        }

    }

    private object Comparator : DiffUtil.ItemCallback<VirusTotalItem>() {
        override fun areItemsTheSame(oldItem: VirusTotalItem, newItem: VirusTotalItem): Boolean {
            return oldItem.result == newItem.result
        }

        override fun areContentsTheSame(oldItem: VirusTotalItem, newItem: VirusTotalItem): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: VirusTotalItem, newItem: VirusTotalItem): List<Int> {
            val payloads = mutableListOf<Int>()
            if (oldItem.result != newItem.result) {
                payloads.add(RESULT_CHANGED)
            }
            if (oldItem.engineName != newItem.engineName) {
                payloads.add(ENGINE_CHANGED)
            }
            return payloads
        }

    }

    override fun onBindViewHolder(
        holder: VirusTotalViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            return super.onBindViewHolder(holder, position, payloads)
        }
        val item = getItem(position)
        payloads.forEach { payload ->
            if (payload is List<*>) {
                if (payload.contains(ENGINE_CHANGED)) holder.bindEngine(item)
                if (payload.contains(RESULT_CHANGED)) holder.bindText(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VirusTotalViewHolder {
        return VirusTotalViewHolder(
            VirusTotalResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: VirusTotalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        private const val ENGINE_CHANGED = 1
        private const val RESULT_CHANGED = 2
    }
}