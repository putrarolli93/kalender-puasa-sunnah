package com.icaali.kalenderpuasasunnah.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import androidx.core.app.NotificationCompat;

import com.icaali.kalenderpuasasunnah.MainActivity;
import com.icaali.kalenderpuasasunnah.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {

    private PendingIntent pendingIntent;
    private static final int ALARM_REQUEST_CODE = 134;
    //set interval notifikasi 10 detik
    private int interval_seconds = 10;
    private NotificationManager alarmNotificationManager;
    String NOTIFICATION_CHANNEL_ID = "rasupe_channel_id";
    String NOTIFICATION_CHANNEL_NAME = "rasupe channel";
    private int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String quote ;
        sendNotification(context, intent);

//        long when = System.currentTimeMillis();
//        NotificationManager notificationManager = (NotificationManager) context
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//
//        Intent notificationIntent = new Intent(context, MainActivity.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
//                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // get your quote here
//        quote = doSomeMethod();


//        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
//                context).setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("My Quotes")
//                .setContentText("oke alarm cikur").setSound(alarmSound)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
//                .setStyle(new NotificationCompat.BigTextStyle().bigText("oke cihuy"))
//                .setContentIntent(pendingIntent)
//                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});  // Declair VIBRATOR Permission in AndroidManifest.xml
//        notificationManager.notify(5, mNotifyBuilder.build());

//        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
////        pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, alarmIntent, 0);
//        //set waktu sekarang berdasarkan interval
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT+07:00"));
//        cal.set(Calendar.HOUR_OF_DAY, 11);
//        cal.set(Calendar.MINUTE, 2);
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MILLISECOND, 0);
//        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        //set alarm manager dengan memasukkan waktu yang telah dikonversi menjadi milliseconds
//        if (android.os.Build.VERSION.SDK_INT >= 23) {
//            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
//        } else if (android.os.Build.VERSION.SDK_INT >= 19) {
//            manager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
//        } else {
//            manager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
//        }
//        //kirim notifikasi
//        sendNotification(context, intent);
    }
    //handle notification
    public void sendNotification(Context context, Intent itn) {
        String notif_title = "";
        String notif_content = "";
        String id = itn.getStringExtra("id");
        if (id.contains("100")) {
            notif_title = "Sholat Dhuha..";
            notif_content = "Halo..sudah masuk jam dhuha, yuk shalat dhuha :)";
        }else if(id.contains("101")) {
            notif_title = "Sholat Tahajud..";
            notif_content = "Halo..nanti malam tahajud yuk:)";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
        String datetimex = sdf.format(new Date());

        alarmNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent newIntent = new Intent(context,MainActivity.class);
        newIntent.putExtra("notifkey", "notifvalue");
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //cek jika OS android Oreo atau lebih baru
        //kalau tidak di set maka notifikasi tidak akan muncul di OS tersebut
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            alarmNotificationManager.createNotificationChannel(mChannel);
        }

        //Buat notification
        NotificationCompat.Builder alamNotificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        alamNotificationBuilder.setContentTitle(notif_title);
        alamNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        alamNotificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        alamNotificationBuilder.setContentText(notif_content);
        alamNotificationBuilder.setAutoCancel(true);
        alamNotificationBuilder.setContentIntent(contentIntent);
        //Tampilkan notifikasi
        alarmNotificationManager.notify(NOTIFICATION_ID, alamNotificationBuilder.build());
    }


}




