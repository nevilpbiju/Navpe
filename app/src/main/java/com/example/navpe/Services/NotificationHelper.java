package com.example.navpe.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.navpe.MainActivity;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.Random;

public class NotificationHelper extends FirebaseMessagingService {

    private String CHANNEL_ID;
    String phoneNo,content;
    public NotificationHelper() {
    }
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
    private void createNotificationChannel() {
        CHANNEL_ID = getString(R.string.default_notification_channel_id);
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription(description);
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.img)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("message"))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setVibrate(new long[] { 1000, 1000})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);

        int notificationID = new Random().nextInt(3000);
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationID, builder.build());
    }
    public void getFirebaseMessagingToken ( ) {
        FirebaseMessaging.getInstance ().getToken ().addOnCompleteListener ( task -> {
                if (!task.isSuccessful ()) {
                    return;
                }
                if (null != task.getResult ()) {
                    String firebaseMessagingToken = Objects.requireNonNull ( task.getResult () );
                }
        });
    }

}