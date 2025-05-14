package com.example.safelink;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationChannelHelper {
    public static final String CHANNEL_ID = "safe_link_alerts";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Link Scan Results",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("نتائج فحص الروابط");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
