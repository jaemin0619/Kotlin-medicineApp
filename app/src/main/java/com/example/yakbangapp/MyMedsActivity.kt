package com.example.yakbangapp

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yakbangapp.databinding.FragmentMyMedsBinding
import com.example.yakbangapp.ui.mymeds.MedListAdapter
import com.example.yakbangapp.ui.mymeds.MyMedsViewModel

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
        super.onViewCreated(view, savedInstanceState)

        // 1) LayoutManager
        binding.medList.layoutManager = LinearLayoutManager(requireContext())
        binding.medList.setHasFixedSize(true)

        // 2) 카드 간격용 ItemDecoration
        val spacing = resources.getDimensionPixelSize(R.dimen.med_card_spacing)
        binding.medList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, v: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                val pos = parent.getChildAdapterPosition(v)
                outRect.left = spacing
                outRect.right = spacing
                outRect.top = if (pos == 0) spacing else spacing / 2
                outRect.bottom = spacing / 2
            }
        })

        // 3) Adapter
        adapter = MedListAdapter(
            onToggleTaken = { med, checked -> vm.toggleTaken(med, checked) },
            onDelete = { med -> vm.delete(med) }
        )
        binding.medList.adapter = adapter

        // 4) Observe
        vm.meds.observe(viewLifecycleOwner) { adapter.submitList(it) }

        // 5) FAB Add
        binding.fabAddMed.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val ctx = requireContext()
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        val etName = EditText(ctx).apply { hint = "약품명 (예: 타이레놀)" }
        val etDose = EditText(ctx).apply { hint = "1회 복용 개수" }
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
