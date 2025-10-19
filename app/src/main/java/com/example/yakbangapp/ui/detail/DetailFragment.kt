package com.example.yakbangapp.ui.detail

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.yakbangapp.R
import com.example.yakbangapp.databinding.FragmentDetailBinding
import com.example.yakbangapp.ui.data.YakData
import com.google.android.material.tabs.TabLayoutMediator

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private var yakData: YakData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        yakData = arguments?.getParcelable("yak_data")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상단 카드 바인딩 (null-safe)
        val data = yakData
        binding.yakNameTv.text = data?.productName.orEmpty()
        binding.yakCode.text = data?.productCode.orEmpty()
        binding.yakVendor.text = data?.companyName.orEmpty()

        Glide.with(this).clear(binding.yakIv)
        val url = data?.imageUrl
        if (!url.isNullOrBlank()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_open_yak)
                .error(R.drawable.ic_open_yak)
                .into(binding.yakIv)
        } else {
            binding.yakIv.setImageResource(R.drawable.ic_open_yak)
        }

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 탭/페이지 데이터
        val pages = listOf(
            DetailPage("효능", data?.efficacy.orEmpty()),
            DetailPage("상호작용", data?.interactions.orEmpty()),
            DetailPage("주의사항", data?.precautions.orEmpty()),
            DetailPage("보관법", data?.storageMethod.orEmpty())
        )

        binding.viewPager.adapter = DetailPagerAdapter(this, pages)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = pages[pos].title
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class DetailPage(val title: String, val content: String)

private class DetailPagerAdapter(
    fragment: Fragment,
    private val pages: List<DetailPage>
) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = pages.size
    override fun createFragment(position: Int): Fragment =
        DetailTextFragment.newInstance(pages[position].content)
}

class DetailTextFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 간단한 스크롤 가능한 TextView
        return TextView(requireContext()).apply {
            id = View.generateViewId()
            setTextColor(0xFF323537.toInt())
            textSize = 15f
            setPadding(32, 24, 32, 24)
            movementMethod = ScrollingMovementMethod()
            // ⚠️ zero-length span 방지: 빈 문자열은 그대로 setText("")
            text = arguments?.getString(ARG_TEXT).orEmpty()
        }
    }

    companion object {
        private const val ARG_TEXT = "text"
        fun newInstance(text: String) = DetailTextFragment().apply {
            arguments = Bundle().apply { putString(ARG_TEXT, text ?: "") }
        }
    }
}
