package com.example.yakbangapp

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.yakbangapp.databinding.ActivityMyMedsBinding
import com.example.yakbangapp.ui.mymeds.MedListAdapter
import com.example.yakbangapp.ui.mymeds.MyMedsViewModel

class MyMedsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyMedsBinding
    private val vm by viewModels<MyMedsViewModel>()
    private lateinit var adapter: MedListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyMedsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recycler + Adapter
        adapter = MedListAdapter(
            onToggleTaken = { med, checked -> vm.toggleTaken(med, checked) },
            onDelete = { med -> vm.delete(med) }
        )
        binding.medList.adapter = adapter

        // Observe
        vm.meds.observe(this) { adapter.submitList(it) }

        // FAB Add
        binding.fabAddMed.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val ctx = this
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        val etName = EditText(ctx).apply { hint = "약품명 (예: 타이레놀)" }
        val etDose = EditText(ctx).apply { hint = "용량/개수 (예: 500mg · 1정)" }
        val etSched = EditText(ctx).apply { hint = "스케줄 (예: 아침/저녁 · 식후 30분)" }
        container.addView(etName); container.addView(etDose); container.addView(etSched)

        AlertDialog.Builder(ctx)
            .setTitle("복약 추가")
            .setView(container)
            .setPositiveButton("추가") { _, _ ->
                val name = etName.text.toString().trim()
                val dose = etDose.text.toString().trim().ifEmpty { "-" }
                val sched = etSched.text.toString().trim().ifEmpty { "-" }
                if (name.isNotEmpty()) vm.addMed(name, dose, sched)
                else Toast.makeText(ctx, "약품명을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
