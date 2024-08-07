package com.example.myimagesearch.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myimagesearch.MainActivity
import com.example.myimagesearch.R
import com.example.myimagesearch.adapter.SearchAdapter
import com.example.myimagesearch.databinding.FragmentImagelibraryBinding
import com.example.myimagesearch.model.SearchModel

class ImageLibraryFragment: Fragment(R.layout.fragment_imagelibrary) {

    private var _binding: FragmentImagelibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var savedImagesAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImagelibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        val savedImages = mainActivity.savedImageList

        savedImagesAdapter = SearchAdapter()
        binding.savedImageRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.savedImageRecyclerView.adapter = savedImagesAdapter
        savedImagesAdapter.submitList(savedImages)

        // 아이템 클릭 시 삭제 기능 추가
        savedImagesAdapter.setOnItemClickListener { searchModel ->
            removeImageFromList(searchModel)
        }
    }

    private fun removeImageFromList(searchModel: SearchModel) {
        val mainActivity = activity as MainActivity
        mainActivity.savedImageList.remove(searchModel)

        // SharedPreferences 업데이트
        val sharedPreferences = requireContext().getSharedPreferences("savedImages", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val updatedSet = mainActivity.savedImageList.map { it.thumbnailUrl ?: "" }.toSet()
        editor.putStringSet("imageUrls", updatedSet)
        editor.apply()

        // 어댑터 갱신
        savedImagesAdapter.submitList(mainActivity.savedImageList.toList())
        Toast.makeText(requireContext(), "이미지가 삭제되었습니다", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
