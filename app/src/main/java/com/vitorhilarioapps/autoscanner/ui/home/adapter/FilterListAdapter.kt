package com.vitorhilarioapps.autoscanner.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.vitorhilarioapps.autoscanner.ui.scanner.model.Filters
import com.squareup.picasso.Picasso
import com.vitorhilarioapps.autoscanner.databinding.FilterIconItemBinding

class FilterListAdapter (private val filterList: List<Filters>) : Adapter<FilterListAdapter.FilterItemHolder>() {

    inner class FilterItemHolder(binding: FilterIconItemBinding) : ViewHolder(binding.root) {
        private val tvFilterItemName = binding.tvFilterItemName
        private val ivFilterItemIcon = binding.ivFilterItemIcon

        fun bind(filter: Filters) {
            tvFilterItemName.text = filter.tittle
            Picasso.get().load(assets_path + filter.icon).into(ivFilterItemIcon)
        }
    }

    override fun onCreateViewHolder(group: ViewGroup, viewType: Int): FilterItemHolder {
        return FilterItemHolder(FilterIconItemBinding.inflate(LayoutInflater.from(group.context), group, false))
    }

    override fun getItemCount(): Int = filterList.size

    override fun onBindViewHolder(holder: FilterItemHolder, position: Int) {
        holder.bind(filterList[position])
    }

    companion object {
        const val assets_path = "file:///android_asset/"
    }
}