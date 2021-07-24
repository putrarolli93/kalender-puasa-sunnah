package com.muslimApp.kalenderpuasasunnah

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util

class LiveActivity : AppCompatActivity() {

    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var playerView: PlayerView
    private lateinit var exoFullScreenIcon: ImageView
    private lateinit var exoFullScreenBtn: FrameLayout

    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isFullscreen = false
    private var isPlayerPlaying = true
    private var HLS_STATIC_URL = ""

    companion object {
        const val STATE_RESUME_WINDOW = "resumeWindow"
        const val STATE_RESUME_POSITION = "resumePosition"
        const val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
        const val STATE_PLAYER_PLAYING = "playerOnPlay"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)
        var param: String = intent.getStringExtra("param")

        if (param == "mekah") {
            (this as? AppCompatActivity)?.supportActionBar?.title = "Live Mekah"
            HLS_STATIC_URL = "https://cdnamd-hls-globecast.akamaized.net/live/ramdisk/saudi_quran/hls1/saudi_quran.m3u8"
        }else {
            (this as? AppCompatActivity)?.supportActionBar?.title = "Live Madinah"
            HLS_STATIC_URL = "https://cdnamd-hls-globecast.akamaized.net/live/ramdisk/saudi_sunnah/hls1/saudi_sunnah-avc1_600000=4-mp4a_97200=2.m3u8"
        }


        playerView = findViewById(R.id.player_view)
        exoFullScreenBtn = playerView.findViewById(R.id.exo_fullscreen_button)
        exoFullScreenIcon = playerView.findViewById(R.id.exo_fullscreen_icon)
        dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "testapp"))

        initFullScreenButton()

        if (savedInstanceState != null) {
            currentWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
            playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            isFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
            isPlayerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING)
        }
    }

    private fun initPlayer() {
        exoPlayer = SimpleExoPlayer.Builder(this).build().apply {
            playWhenReady = isPlayerPlaying
            seekTo(currentWindow, playbackPosition)
            prepare(buildMediaSource(), false, false)
        }
        playerView.player = exoPlayer
        if(isFullscreen) openFullscreen()
    }

    private fun buildMediaSource():MediaSource{
        val userAgent = Util.getUserAgent(playerView.context, playerView.context.getString(R.string.app_name))

        val dataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
        val hlsMediaSource =  HlsMediaSource.Factory(dataSourceFactory).
        createMediaSource(Uri.parse(HLS_STATIC_URL))

        return  hlsMediaSource

    }

    private fun releasePlayer(){
        isPlayerPlaying = exoPlayer.playWhenReady
        playbackPosition = exoPlayer.currentPosition
        currentWindow = exoPlayer.currentWindowIndex
        exoPlayer.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_RESUME_WINDOW, exoPlayer.currentWindowIndex)
        outState.putLong(STATE_RESUME_POSITION, exoPlayer.currentPosition)
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initPlayer()
            playerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            initPlayer()
            playerView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onBackPressed() {
        if(isFullscreen){
            closeFullscreen()
            return
        }
        super.onBackPressed()
    }

    // FULLSCREEN PART

    private fun initFullScreenButton() {
        exoFullScreenBtn.setOnClickListener {
            if (!isFullscreen) {
                openFullscreen()
            } else {
                closeFullscreen()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun openFullscreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        exoFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_shrink))
        playerView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlack))
        val params: LinearLayout.LayoutParams = playerView.layoutParams as LinearLayout.LayoutParams
        params.width = LinearLayout.LayoutParams.MATCH_PARENT
        params.height = LinearLayout.LayoutParams.MATCH_PARENT
        playerView.layoutParams = params
        supportActionBar?.hide()
        hideSystemUi()
        isFullscreen = true
    }

    private fun closeFullscreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        exoFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_expand))
        playerView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
        val params: LinearLayout.LayoutParams = playerView.layoutParams as LinearLayout.LayoutParams
        params.width = LinearLayout.LayoutParams.MATCH_PARENT
        params.height = 0
        playerView.layoutParams = params
        supportActionBar?.show()
        playerView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        isFullscreen = false
    }

    private fun hideSystemUi() {
        playerView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

}