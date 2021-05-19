package com.muslimApp.kalenderpuasasunnah.fcm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.muslimApp.kalenderpuasasunnah.MainActivity
import com.muslimApp.kalenderpuasasunnah.R
import com.muslimApp.kalenderpuasasunnah.detail.DetailPuasaActivity
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseToken"
    private lateinit var notificationManager: NotificationManager
    private val ADMIN_CHANNEL_ID = "Android4Dev"

    @SuppressLint("LongLogTag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Dikirim dari: ${remoteMessage.from}")

        remoteMessage?.let { message ->
//            Log.i(TAG, message.getData().get("message"))

            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            //Setting up Notification channels for android O and above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                setupNotificationChannels()
            }
            val resultIntent = Intent(this, DetailPuasaActivity::class.java)
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(resultIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val notificationId = Random().nextInt(60000)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)  //a resource for your custom small icon
                .setContentTitle(message.notification?.title) //the "title" value you sent in your notification
                .setContentText(message.notification?.body) //ditto
                .setAutoCancel(true)  //dismisses the notification on click
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent)

            notificationManager.notify(notificationId, notificationBuilder.build())

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupNotificationChannels() {
        val adminChannelName = "MuslimApp"
        val adminChannelDescription = "Kalender Puasa Apps"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager.createNotificationChannel(adminChannel)
    }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)

    }

}