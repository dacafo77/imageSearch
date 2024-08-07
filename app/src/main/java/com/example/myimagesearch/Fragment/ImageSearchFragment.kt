package com.example.myimagesearch.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myimagesearch.MainActivity
import com.example.myimagesearch.R
import com.example.myimagesearch.adapter.SearchAdapter
import com.example.myimagesearch.api.ImageService
import com.example.myimagesearch.databinding.FragmentImagesearchBinding
import com.example.myimagesearch.model.ImageSearch
import com.example.myimagesearch.model.SearchModel
import com.example.myimagesearch.model.SearchListType
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
        sharedPreferences =
            requireContext().getSharedPreferences("saved_images", Context.MODE_PRIVATE)

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
                }
                return@setOnKeyListener true
            }
            false
        }
    }

    private fun fetchData(query: String) {
        // HttpLoggingInterceptor 설정
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 전체 요청과 응답을 로그로 출력
        }

        // OkHttpClient 설정
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "KakaoAK e9e0d93198726fed58205bcc2da07846") // API 키 변경
                    .header("User-Agent", "AppName") // 적절한 사용자 에이전트 값
                    .header("Referer", "https://yourappdomain.com") // 적절한 도메인 또는 앱 정보
                    .header("os", android.os.Build.VERSION.RELEASE) // 운영 체제 버전
                    .header("origin", "AppName") // 애플리케이션 이름 또는 도메인
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

        val imageService = retrofit.create(ImageService::class.java)

        val call = imageService.searchImages("KakaoAK e9e0d93198726fed58205bcc2da07846", query)
        call.enqueue(object : Callback<ImageSearch> {
            override fun onResponse(call: Call<ImageSearch>, response: Response<ImageSearch>) {
                if (response.isSuccessful) {
                    val imageSearchResult = response.body()
                    Log.d("API_RESPONSE", "Success: ${imageSearchResult.toString()}")

                    // 응답 데이터에서 thumbnailUrl 필드 확인
                    imageSearchResult?.documents?.forEach {
                        Log.d("API_RESPONSE", "Thumbnail URL: ${it.thumbnailUrl}")
                    }
                    // 이미지 갯수 제한
                    val imageList = imageSearchResult?.documents?.take(80)

                    // 데이터를 RecyclerView에 반영
                    val adapter = binding.imageRecyclerView.adapter as SearchAdapter
                    adapter.submitList(imageList?.map {
                        SearchModel(
                            id = it.collection ?: "Unknown",
                            thumbnailUrl = it.thumbnailUrl,
                            siteName = it.displaySiteName,
                            datetime = it.dateTime,
                            itemType = SearchListType.IMAGE
                        )
                    })
                    // 재 검색 시 리사이클러뷰 초기화
                    binding.imageRecyclerView.scrollToPosition(0)
                } else {
                    Log.e("API_ERROR", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ImageSearch>, t: Throwable) {
                Log.e("API_ERROR", "Failure: ${t.message}")
            }
        })
    }

    // 선택된 이미지를 SearchModel로 저장
    private fun saveSavedImages(savedImages: List<SearchModel>?) {
        val editor = sharedPreferences.edit()
        val imageUrlSet = savedImages?.map { it.thumbnailUrl ?: "" }?.toSet() ?: emptySet()
        editor.putStringSet("image_urls", imageUrlSet)
        editor.apply()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수를 방지하기 위해 뷰가 파괴될 때 바인딩을 해제
    }
}
