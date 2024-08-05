package com.example.myimagesearch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myimagesearch.Fragment.ImageLibraryFragment
import com.example.myimagesearch.Fragment.ImageSearchFragment
import com.example.myimagesearch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 처음 시작할 때 보여줄 프래그먼트 설정
        replaceFragment(ImageSearchFragment())

        // BottomNavigationView 클릭 리스너 설정
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.imageSearch -> {
                    replaceFragment(ImageSearchFragment())
                    true
                }
                R.id.imageLibrary -> {
                    replaceFragment(ImageLibraryFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentView, fragment) // fragmentView에 프래그먼트 교체
        fragmentTransaction.commit()
    }
}
