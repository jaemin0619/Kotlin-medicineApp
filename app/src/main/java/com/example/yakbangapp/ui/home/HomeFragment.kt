package com.example.yakbangapp.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yakbangapp.R
import com.example.yakbangapp.databinding.FragmentHomeBinding
import com.example.yakbangapp.repository.Category
import com.example.yakbangapp.ui.data.YakData
import com.example.yakbangapp.ui.detail.DetailFragment
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HomeViewModel>()
    private val detailFragment by lazy { DetailFragment() }

    private val adapter by lazy {
        DataListAdapter { data ->
            openDetail(data)
        }
    }

    private val categoryMap = mapOf(
        R.id.chip_product_name to Category.ProductName,
        R.id.chip_company to Category.CompanyName,
        R.id.chip_effect to Category.Efficacy,
        R.id.chip_side_effect to Category.SideEffects,
        R.id.chip_interaction to Category.Interactions
    )

    // ✅ 검색 제출 후 결과 도착 시 상세로 이동할지 여부
    private var navigateOnSubmit = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        with(binding) {
            // RecyclerView
            yakList.layoutManager = LinearLayoutManager(requireContext())
            yakList.adapter = adapter
            yakList.setHasFixedSize(true)

            // 기본 Chip 선택 및 힌트 반영
            chipGroup.check(R.id.chip_product_name)
            updateHintForCategory(categoryFrom(chipGroup.checkedChipId))

            // ChipGroup 선택 변경 → 힌트 갱신 + 현재 쿼리로 즉시 재검색
            chipGroup.setOnCheckedStateChangeListener(
                ChipGroup.OnCheckedStateChangeListener { _, checkedIds ->
                    val id = checkedIds.firstOrNull() ?: return@OnCheckedStateChangeListener
                    val cat = categoryFrom(id)
                    updateHintForCategory(cat)

                    val raw = searchView.query?.toString().orEmpty()
                    val q = cleaned(raw)
                    if (q.isNotEmpty()) {
                        viewModel.getYakList(cat, q)
                    }
                }
            )

            // SearchView
            searchView.apply {
                setIconifiedByDefault(false)
                isIconified = false

                // 엔터 액션
                val searchText = findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
                searchText.imeOptions = EditorInfo.IME_ACTION_SEARCH
                searchText.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        submitSearch(navigate = true)
                        true
                    } else false
                }

                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        submitSearch(navigate = true) // ✅ 제출 → 상세로 진입 예약
                        return true
                    }
                    override fun onQueryTextChange(newText: String?) = false
                })

                setOnClickListener {
                    isIconified = false
                    requestFocus()
                    post { showKeyboard() }
                }

                setOnCloseListener {
                    adapter.submitList(emptyList())
                    false
                }

                setOnQueryTextFocusChangeListener { v, hasFocus ->
                    if (hasFocus) v.showKeyboard() else v.hideKeyboard()
                }
            }
        }

        // (옵션) 초기 데이터
        viewModel.getYakList(Category.Efficacy, "감기")

        // ✅ 결과 관찰: 리스트 반영 + 제출 후엔 자동 상세 진입 (0건 가드)
        viewModel.yakDataList.observe(viewLifecycleOwner) { list ->
            val safe = list?.toList() ?: emptyList()
            adapter.submitList(safe)

            if (safe.isEmpty()) {
                navigateOnSubmit = false
                view?.let { Snackbar.make(it, "해당하는 항목이 없습니다.", Snackbar.LENGTH_SHORT).show() }
                return@observe
            }

            if (navigateOnSubmit) {
                navigateOnSubmit = false
                val q = cleaned(binding.searchView.query?.toString().orEmpty())
                val target = safe.firstOrNull {
                    it.productName?.contains(q, ignoreCase = true) == true
                } ?: safe.first()
                openDetail(target)
            }
        }

        return binding.root
    }

    private fun submitSearch(navigate: Boolean = false) {
        val raw = binding.searchView.query?.toString().orEmpty()
        val q = cleaned(raw)
        if (q.isEmpty()) return
        val queryCategory = categoryFrom(binding.chipGroup.checkedChipId)
        navigateOnSubmit = navigate
        viewModel.getYakList(queryCategory, q)
        binding.searchView.hideKeyboard()
        binding.searchView.clearFocus()
    }

    private fun openDetail(data: YakData) {
        detailFragment.arguments = Bundle().apply { putParcelable("yak_data", data) }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun categoryFrom(id: Int): Category {
        return categoryMap[id] ?: Category.ProductName
    }

    private fun updateHintForCategory(category: Category) {
        val hint = when (category) {
            Category.ProductName  -> "제품명으로 검색"
            Category.CompanyName  -> "업체명으로 검색"
            Category.Efficacy     -> "효능으로 검색"
            Category.SideEffects  -> "부작용으로 검색"
            Category.Interactions -> "상호작용으로 검색"
            else                  -> "키워드로 검색"
        }
        binding.searchView.queryHint = hint
    }

    // 검색어 전처리: 앞뒤 공백 제거, 연속 공백 축약, 일부 상표 기호 제거 등
    private fun cleaned(text: String): String =
        text.trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[®™Ⓡ]"), "")

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ===== 키보드 헬퍼 =====
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (windowToken != null) imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requestFocus()
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}
