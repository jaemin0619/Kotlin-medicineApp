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
import com.example.yakbangapp.ui.detail.DetailFragment
import com.google.android.material.chip.ChipGroup

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HomeViewModel>()
    private val detailFragment by lazy { DetailFragment() }

    private val adapter by lazy {
        DataListAdapter { data ->
            detailFragment.arguments = Bundle().apply { putParcelable("yak_data", data) }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private val categoryMap = mapOf(
        R.id.chip_product_name to Category.ProductName,
        R.id.chip_company to Category.CompanyName,
        R.id.chip_effect to Category.Efficacy,
        R.id.chip_side_effect to Category.SideEffects,
        R.id.chip_interaction to Category.Interactions
    )

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

            // ✅ ChipGroup 기본 선택(그룹 API로 설정해야 checkedChipId가 유효)
            chipGroup.check(R.id.chip_product_name)
            updateHintForCategory(categoryFrom(chipGroup.checkedChipId))

            // ✅ ChipGroup 선택 변경 리스너
            chipGroup.setOnCheckedStateChangeListener(
                ChipGroup.OnCheckedStateChangeListener { group, checkedIds ->
                    val id = checkedIds.firstOrNull() ?: return@OnCheckedStateChangeListener
                    val cat = categoryFrom(id)
                    updateHintForCategory(cat)

                    // 현재 입력값이 있으면 즉시 재검색
                    val q = searchView.query?.toString()?.trim().orEmpty()
                    if (q.isNotEmpty()) {
                        viewModel.getYakList(cat, q)
                    }
                }
            )

            // SearchView 설정
            searchView.apply {
                setIconifiedByDefault(false)
                isIconified = false
                // 처음 힌트는 chipGroup.check에서 이미 설정됨

                // 엔터(돋보기) 액션 강제
                val searchText = binding.searchView.findViewById<TextView>(
                    androidx.appcompat.R.id.search_src_text
                )
                searchText.imeOptions = EditorInfo.IME_ACTION_SEARCH
                searchText.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        submitSearch()
                        true
                    } else false
                }

                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        submitSearch()
                        return true
                    }

                    // 필요 시 여기서 실시간 검색 가능: return true → 소비 / false → 기본 처리
                    override fun onQueryTextChange(newText: String?) = false
                })

                // 클릭 시 확장 + 포커스 + 키보드
                setOnClickListener {
                    isIconified = false
                    requestFocus()
                    post { showKeyboard() }
                }

                // 닫기(X) 시 리스트 비우기 (잔상 방지)
                setOnCloseListener {
                    adapter.submitList(emptyList())
                    false
                }

                setOnQueryTextFocusChangeListener { v, hasFocus ->
                    if (hasFocus) v.showKeyboard() else v.hideKeyboard()
                }
            }
        }

        // (선택) 초기 로드 — 필요 없다면 주석 처리하세요
        viewModel.getYakList(Category.Efficacy, "감기")

        // 단 1회 submit + 새 리스트 인스턴스 전달
        viewModel.yakDataList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list?.toList() ?: emptyList())
        }

        return binding.root
    }

    // SearchView의 현재 query를 읽어 검색 호출
    private fun submitSearch() {
        val q = binding.searchView.query?.toString()?.trim().orEmpty()
        if (q.isEmpty()) return

        val queryCategory = categoryFrom(binding.chipGroup.checkedChipId)
        viewModel.getYakList(queryCategory, q)

        binding.searchView.hideKeyboard()
        binding.searchView.clearFocus()
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
            else                  -> "키워드로 검색" // ✅ 나머지 분기 커버
        }
        binding.searchView.queryHint = hint
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ===== 키보드 헬퍼 =====
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requestFocus()
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}
