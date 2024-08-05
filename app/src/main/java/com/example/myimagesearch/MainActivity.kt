package com.example.myimagesearch

import android.os.Bundle
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myimagesearch.api.ImageService
import com.example.myimagesearch.databinding.ActivityMainBinding
import com.example.myimagesearch.model.ImageSearch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                    .header("User-Agent", "YourAppName") // 적절한 사용자 에이전트 값
                    .header("Referer", "https://yourappdomain.com") // 적절한 도메인 또는 앱 정보
                    .header("os", Build.VERSION.RELEASE) // 운영 체제 버전
                    .header("origin", "YourAppName") // 애플리케이션 이름 또는 도메인
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()

        // Retrofit 설정
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com")
            .client(client) // OkHttpClient를 Retrofit에 추가
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val imageService = retrofit.create(ImageService::class.java)

        val apiKey = "KakaoAK e9e0d93198726fed58205bcc2da07846" // 여기도 API 키 변경
        val call = imageService.searchImages(apiKey, "cat")
        call.enqueue(object : Callback<ImageSearch> {
            override fun onResponse(call: Call<ImageSearch>, response: Response<ImageSearch>) {
                if (response.isSuccessful) {
                    val imageSearchResult = response.body()
                    Log.d("API_RESPONSE", "Success: ${imageSearchResult.toString()}")
                    // 데이터를 UI에 반영하는 로직 추가
                } else {
                    Log.e("API_ERROR", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ImageSearch>, t: Throwable) {
                Log.e("API_ERROR", "Failure: ${t.message}")
            }
        })
    }
}
