package com.example.yakbangapp.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yakbangapp.R
import com.example.yakbangapp.databinding.FragmentHomeBinding
import com.example.yakbangapp.repository.Category
import com.example.yakbangapp.ui.detail.DetailFragment

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
            // RecyclerView 기본 셋업
            yakList.layoutManager = LinearLayoutManager(requireContext())
            yakList.adapter = adapter
            yakList.setHasFixedSize(true)

            chipProductName.isChecked = true

            // SearchView 설정
            searchView.apply {
                setIconifiedByDefault(false)
                isIconified = false
                queryHint = "제품명/업체/효능으로 검색"

                // 엔터(돋보기) 액션 강제
                val searchText =
                    findViewById<SearchView.SearchAutoComplete>(
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
                    override fun onQueryTextChange(newText: String?) = true
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

        // 초기 로드(원하지 않으면 지워도 됨)
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
        val queryCategory = categoryMap[binding.chipGroup.checkedChipId] ?: Category.ProductName
        viewModel.getYakList(queryCategory, q)
        binding.searchView.hideKeyboard()
        binding.searchView.clearFocus()
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
