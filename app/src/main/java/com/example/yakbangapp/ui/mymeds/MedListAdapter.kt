package com.example.yakbangapp.ui.mymeds

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbangapp.databinding.ItemMedBinding
import com.example.yakbangapp.model.MyMed

class MedListAdapter(
    private val onToggleTaken: (MyMed, Boolean) -> Unit,
    private val onDelete: (MyMed) -> Unit
) : ListAdapter<MyMed, MedListAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MyMed>() {
            override fun areItemsTheSame(o: MyMed, n: MyMed) = o.id == n.id
            override fun areContentsTheSame(o: MyMed, n: MyMed) = o == n
        }
    }

    inner class VH(private val binding: ItemMedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyMed) = with(binding) {
            tvName.text = item.name
            tvDose.text = item.doseText
            tvSchedule.text = item.scheduleText

            cbTakenToday.setOnCheckedChangeListener(null)
            cbTakenToday.isChecked = item.takenToday
            cbTakenToday.setOnCheckedChangeListener { _, checked ->
                onToggleTaken(item, checked)
            }

            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
