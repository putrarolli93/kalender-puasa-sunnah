package com.icaali.kalenderpuasasunnah.detail

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.icaali.kalenderpuasasunnah.R
import com.icaali.kalenderpuasasunnah.databinding.ActivityDetailPuasaBinding
import java.util.Locale

class DetailPuasaActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityDetailPuasaBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // Tambah ini ↓
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        val adRequest = AdRequest.Builder().build()
//        adView.loadAd(adRequest)
        binding.apply {
            var code: Int = intent.getIntExtra("code", 0)
            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
            val lang = prefs.getString("language", "en") ?: "en"
            code.let {
                when (code) {
                    1 -> {
                        val fileName = if (lang == "in" || lang == "id") {
                            "senin_kamis_id.html"
                        } else {
                            "senin_kamis.html"
                        }

                        webView.loadUrl("file:///android_asset/$fileName")
                    }

                    2 -> {
                        val fileName = if (lang == "in" || lang == "id") {
                            "ayyamul_bidh_id.html"
                        } else {
                            "ayyamul_bidh.html"
                        }

                        webView.loadUrl("file:///android_asset/$fileName")
                    }

                    3 -> {
                        val fileName = if (lang == "in" || lang == "id") {
                            "ramadhan_id.html"
                        } else {
                            "ramadhan.html"
                        }

                        webView.loadUrl("file:///android_asset/$fileName")
                    }

                    4 -> {
                        val fileName = if (lang == "in" || lang == "id") {
                            "arafah_id.html"
                        } else {
                            "arafah.html"
                        }

                        webView.loadUrl("file:///android_asset/$fileName")
                    }

                    5 -> {
                        val fileName = if (lang == "in" || lang == "id") {
                            "tasua_id.html"
                        } else {
                            "tasua.html"
                        }

                        webView.loadUrl("file:///android_asset/$fileName")
                    }
                    6 -> {
                        val fileName = if (lang == "in" || lang == "id") {
                            "syawal_id.html"
                        } else {
                            "syawal.html"
                        }

                        webView.loadUrl("file:///android_asset/$fileName")
                    }

                    99 -> {
                        val fileName = if (lang == "in" || lang == "id") {
                            "haram_puasa_id.html"
                        } else {
                            "haram_puasa.html"
                        }

                        webView.loadUrl("file:///android_asset/$fileName")
                    }
                }
            }
        }
    }
}