package com.vitorhilarioapps.autoscanner.ui.scanner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.vitorhilarioapps.autoscanner.ui.scanner.model.Filters
import com.squareup.picasso.Picasso
import com.vitorhilarioapps.autoscanner.databinding.FilterIconItemBinding

class ScannerPreviewAdapter (
    private val filterList: List<Filters>,
    private val actionApplyFilter: (Filters) -> Unit
) : Adapter<ScannerPreviewAdapter.FilterPreviewHolder>()
{
    inner class FilterPreviewHolder(binding: FilterIconItemBinding) : ViewHolder(binding.root) {
        private val cvFilterItemIcon = binding.cvFilterItemIcon
        private val tvFilterItemName = binding.tvFilterItemName
        private val ivFilterItemIcon = binding.ivFilterItemIcon

        fun bind(filter: Filters) {
            tvFilterItemName.text = filter.tittle
            Picasso.get().load(assets_path + filter.icon).into(ivFilterItemIcon)
            cvFilterItemIcon.setOnClickListener { actionApplyFilter(filter) }
        }
    }

    override fun onCreateViewHolder(group: ViewGroup, viewType: Int): FilterPreviewHolder {
        return FilterPreviewHolder(
            FilterIconItemBinding.inflate(LayoutInflater.from(group.context), group, false)
        )
    }

    override fun onBindViewHolder(holder: FilterPreviewHolder, position: Int) {
        holder.bind(filterList[position])
    }

    override fun getItemCount(): Int = filterList.size

    companion object {
        const val assets_path = "file:///android_asset/"
    }
}
