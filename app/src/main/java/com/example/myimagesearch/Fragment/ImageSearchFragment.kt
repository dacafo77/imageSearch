package com.example.myimagesearch.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myimagesearch.MainActivity
import com.example.myimagesearch.R
import com.example.myimagesearch.adapter.SearchAdapter
import com.example.myimagesearch.api.MediaService
import com.example.myimagesearch.databinding.FragmentImagesearchBinding
import com.example.myimagesearch.model.ImageSearch
import com.example.myimagesearch.model.SearchModel
import com.example.myimagesearch.model.SearchListType
import com.example.myimagesearch.model.VideoSearch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ImageSearchFragment : Fragment(R.layout.fragment_imagesearch) {

    private var _binding: FragmentImagesearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImagesearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SharedPreferences 초기화
        sharedPreferences = requireContext().getSharedPreferences("saved_images", Context.MODE_PRIVATE)

        // 검색어 불러오기
        val searchWord = sharedPreferences.getString("searchWord", "")
        binding.searchEditText.setText(searchWord)

        initImageRecyclerView()
        setupSearchListener()

        // 이미지 클릭 시 데이터 저장 및 전달
        (binding.imageRecyclerView.adapter as SearchAdapter).setOnItemClickListener { searchModel ->
            val mainActivity = activity as? MainActivity
            val savedImageList = mainActivity?.savedImageList
            savedImageList?.add(searchModel)
            saveSavedImages(savedImageList)
        }

        // 포커스시에 글자 삭제
        binding.searchEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.searchEditText.text.clear()
            }
        }

        // 포커스 활성화
        binding.searchEditText.setOnClickListener {
            binding.searchEditText.isFocusableInTouchMode = true
            binding.searchEditText.isFocusable = true
            binding.searchEditText.requestFocus()
            // 키보드 활성화
            val keyBoard = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyBoard.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        // searchButton 클릭 리스너 설정
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                fetchData(query) // 사용자가 입력한 쿼리를 fetchData로 전달

                // 키보드 비활성화
                val keyBoard = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyBoard.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
            }
        }
    }

    private fun initImageRecyclerView() {
        val adapter = SearchAdapter()
        binding.imageRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.imageRecyclerView.adapter = adapter
    }

    private fun setupSearchListener() {
        binding.searchEditText.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == MotionEvent.ACTION_DOWN) {
                val query = binding.searchEditText.text.toString()
                if (query.isNotEmpty()) {
                    fetchData(query) // 사용자가 입력한 쿼리를 fetchData로 전달
                    // 키보드 비활성화
                    val keyBoard = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    keyBoard.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                }
                return@setOnKeyListener true
            }
            false
        }
    }

    private fun fetchData(query: String) {
        // 검색어 저장
        val editor = sharedPreferences.edit()
        editor.putString("searchWord", query)
        editor.apply()

        // HttpLoggingInterceptor 설정
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttpClient 설정
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "KakaoAK e9e0d93198726fed58205bcc2da07846")
                    .header("User-Agent", "AppName")
                    .header("Referer", "https://yourappdomain.com")
                    .header("os", android.os.Build.VERSION.RELEASE)
                    .header("origin", "AppName")
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()

        // Retrofit 설정
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val mediaService = retrofit.create(MediaService::class.java)

        // 이미지 검색 API 호출
        val imageCall = mediaService.searchImages("KakaoAK e9e0d93198726fed58205bcc2da07846", query)
        imageCall.enqueue(object : Callback<ImageSearch> {
            override fun onResponse(call: Call<ImageSearch>, response: Response<ImageSearch>) {
                if (response.isSuccessful) {
                    val imageSearchResult = response.body()
                    val imageList = imageSearchResult?.documents?.take(80)?.map { document ->
                        SearchModel(
                            id = document.collection ?: "Unknown",
                            thumbnailUrl = document.thumbnailUrl,
                            siteName = document.displaySiteName,
                            datetime = document.dateTime,
                            itemType = SearchListType.IMAGE
                        )
                    } ?: emptyList()

                    // 동영상 검색 API 호출
                    val videoCall = mediaService.searchVideos("KakaoAK e9e0d93198726fed58205bcc2da07846", query)
                    videoCall.enqueue(object : Callback<VideoSearch> {
                        override fun onResponse(call: Call<VideoSearch>, response: Response<VideoSearch>) {
                            if (response.isSuccessful) {
                                val videoSearchResult = response.body()
                                val videoList = videoSearchResult?.documents?.take(80)?.map { document ->
                                    SearchModel(
                                        id = document.collection ?: "Unknown",
                                        thumbnailUrl = document.thumbnailUrl,
                                        siteName = document.displaySiteName,
                                        datetime = document.dateTime,
                                        itemType = SearchListType.VIDEO
                                    )
                                } ?: emptyList()

                                // 이미지와 동영상 리스트를 합쳐서 표시
                                val adapter = binding.imageRecyclerView.adapter as SearchAdapter
                                adapter.submitList(imageList + videoList)
                            }
                        }

                        override fun onFailure(call: Call<VideoSearch>, t: Throwable) {
                            // 실패 처리
                        }
                    })
                } else {
                    // 실패 처리
                }
            }

            override fun onFailure(call: Call<ImageSearch>, t: Throwable) {
                // 실패 처리
            }
        })
    }

    // 선택된 이미지를 SearchModel로 저장
    private fun saveSavedImages(savedImages: List<SearchModel>?) {
        val editor = sharedPreferences.edit()
        val imageUrlSet = savedImages?.map { it.thumbnailUrl ?: "" }?.toSet() ?: emptySet()
        editor.putStringSet("image_urls", imageUrlSet)
        editor.apply()

        Toast.makeText(requireContext(), "이미지가 저장되었습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수를 방지하기 위해 뷰가 파괴될 때 바인딩을 해제
    }
}
