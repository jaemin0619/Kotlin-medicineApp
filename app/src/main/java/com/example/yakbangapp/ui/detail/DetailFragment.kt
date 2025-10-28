package com.example.yakbangapp.ui.detail

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
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
        // "yak" (권장) 또는 "yak_data" (하위 호환) 키 모두 지원
        yakData = arguments?.getParcelable("yak")
            ?: arguments?.getParcelable("yak_data")
                    ?: activity?.intent?.getParcelableExtra("yak")
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

        val data = yakData

        // 상단 카드
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
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 탭/페이지 데이터 (문자열은 HTML 그대로 전달)
        val pages = listOf(
            DetailPage("효능", data?.efficacy.orEmpty()),
            DetailPage("상호작용", data?.interactions.orEmpty()),
            DetailPage("주의사항", data?.precautions.orEmpty()),
            DetailPage("보관법", data?.storageMethod.orEmpty())
        )

        binding.viewPager.adapter = DetailPagerAdapter(this, pages)
        binding.viewPager.offscreenPageLimit = pages.size

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
        val raw = arguments?.getString(ARG_TEXT).orEmpty()
        val spanned = HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY)

        return TextView(requireContext()).apply {
            id = View.generateViewId()
            setTextColor(0xFF323537.toInt())
            textSize = 15f
            setPadding(32, 24, 32, 24)
            // 스크롤 + 링크 클릭 가능
            movementMethod = ScrollingMovementMethod()
            movementMethod = LinkMovementMethod.getInstance()
            text = spanned
        }
    }

    companion object {
        private const val ARG_TEXT = "text"
        fun newInstance(text: String) = DetailTextFragment().apply {
            arguments = Bundle().apply { putString(ARG_TEXT, text) }
        }
    }
}
