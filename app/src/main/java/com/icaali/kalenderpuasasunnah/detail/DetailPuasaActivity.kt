package com.icaali.kalenderpuasasunnah.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.icaali.kalenderpuasasunnah.R
import com.icaali.kalenderpuasasunnah.databinding.ActivityDetailPuasaBinding

class DetailPuasaActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityDetailPuasaBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlackImage)

//        val adRequest = AdRequest.Builder().build()
//        adView.loadAd(adRequest)
        binding.apply {
            var code: Int = intent.getIntExtra("code", 0)
            code?.let {
                when (code) {
                    1 -> {
                        webView.loadUrl("file:///android_asset/senin_kamis.html")
                    }

                    2 -> {
                        webView.loadUrl("file:///android_asset/ayyamul_bidh.html")
                    }

                    3 -> {
                        webView.loadUrl("file:///android_asset/ramadhan.html")
                    }

                    4 -> {
                        webView.loadUrl("file:///android_asset/arafah.html")
                    }

                    5 -> {
                        webView.loadUrl("file:///android_asset/tasua.html")
                    }

                    8 -> {
                        webView.loadUrl("file:///android_asset/haram_puasa.html")
                    }
                }
            }
        }
    }
}