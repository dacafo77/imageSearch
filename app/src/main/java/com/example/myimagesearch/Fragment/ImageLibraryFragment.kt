package com.example.myimagesearch.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myimagesearch.MainActivity
import com.example.myimagesearch.R
import com.example.myimagesearch.adapter.SearchAdapter
import com.example.myimagesearch.databinding.FragmentImagelibraryBinding

class ImageLibraryFragment: Fragment(R.layout.fragment_imagelibrary) {

    private var _binding: FragmentImagelibraryBinding? = null
    private val binding get() = _binding!!

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

        val adapter = SearchAdapter()
        binding.savedImageRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.savedImageRecyclerView.adapter = adapter
        adapter.submitList(savedImages)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
