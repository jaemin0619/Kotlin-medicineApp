package com.example.yakbangapp.ui.mymeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.yakbangapp.databinding.FragmentMyMedsBinding

class MyMedsFragment : Fragment() {

    private var _binding: FragmentMyMedsBinding? = null
    private val binding get() = _binding!!

    private val vm by viewModels<MyMedsViewModel>()
    private lateinit var adapter: MedListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyMedsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Recycler + Adapter
        adapter = MedListAdapter(
            onToggleTaken = { med, checked -> vm.toggleTaken(med, checked) },
            onDelete = { med -> vm.delete(med) }
        )
        binding.medList.adapter = adapter

        // Observe
        vm.meds.observe(viewLifecycleOwner) { adapter.submitList(it) }

        // FAB Add
        binding.fabAddMed.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val ctx = requireContext()
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        val etName = EditText(ctx).apply { hint = "약품명 (예: 타이레놀)" }
        val etDose = EditText(ctx).apply { hint = "용량/개수 (예: 500mg · 1정)" }
        val etSched = EditText(ctx).apply { hint = "스케줄 (예: 아침/저녁 · 식후 30분)" }
        container.addView(etName); container.addView(etDose); container.addView(etSched)

        androidx.appcompat.app.AlertDialog.Builder(ctx)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
