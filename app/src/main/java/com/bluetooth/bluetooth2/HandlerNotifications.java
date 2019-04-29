package com.bluetooth.bluetooth2;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import static com.bluetooth.bluetooth2.Utils.getRandomNumber;

public class HandlerNotifications {

    private static final int ONGOING_NOTIFICATION_ID = getRandomNumber();

    private static PendingIntent getStopServicePI(Service context) {
        Intent intentStopService = new Intent(context, BluetoothService.class);
        intentStopService.setAction("stop_service");

        Intent intentLaunchMainActivity = new Intent(context, MainActivity.class);
        PendingIntent.getActivity(context, getRandomNumber(), intentLaunchMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
        return PendingIntent.getService(context, getRandomNumber(), intentStopService, 0);
    }

    private static PendingIntent getLaunchActivityPI(Service context) {
        Intent intentLaunchMainActivity = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, getRandomNumber(), intentLaunchMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @TargetApi(25)
    public static class PreO {

        public static void createNotification(Service context) {
            // Create Pending Intents
            PendingIntent piLaunchMainActivity = getLaunchActivityPI(context);
            PendingIntent piStopService = getStopServicePI(context);

            // Action to stop the service
            NotificationCompat.Action stopAction =
                    new NotificationCompat.Action.Builder(
                            android.R.drawable.star_off,
                            "Stop service!!!",
                            piStopService
                    ).build();

            // Create a notification
            Notification mNotification =
                    new NotificationCompat.Builder(context)
                            .setContentTitle("BluetoothDevicesScanner")
                            .setContentText("Searching devices.")
                            .setSmallIcon(android.R.drawable.star_on)
                            .setContentIntent(piLaunchMainActivity)
                            .addAction(stopAction)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText("Click to stop service"))
                            .build();

            context.startForeground(ONGOING_NOTIFICATION_ID, mNotification);
        }
    }

    @TargetApi(26)
    public static class O {
        private static final String CHANNEL_ID = "Bluetooth_service_search_notificator";

        public static void createNotification(Service context) {
            String channelId = createChannel(context);
            Notification notification = buildNotification(context, channelId);
            context.startForeground(
                    ONGOING_NOTIFICATION_ID, notification);
        }

        private static Notification buildNotification(
                Service context, String channelId) {
            PendingIntent piLaunchMainActivity = getLaunchActivityPI(context);
            PendingIntent piStopService = getStopServicePI(context);

            Notification.Action stopAction = new Notification.Action.Builder(
                    android.R.drawable.star_off,
                    "Stop service!!!",
                    piStopService)
                    .build();

            return new Notification.Builder(context, channelId)
                    .setContentTitle("BluetoothDevicesScanner")
                    .setContentText("Searching devices.")
                    .setSmallIcon(android.R.drawable.star_on)
                    .setContentIntent(piLaunchMainActivity)
                    .setActions(stopAction)
                    .setStyle(new Notification.BigTextStyle())
                    .build();
        }

        @NonNull
        private static String createChannel(Service context) {
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            CharSequence channelName = "Playback channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
            return CHANNEL_ID;
        }
    }

    /*@NonNull
    private static String getNotificationStopActionText(Service context) {
        return context.getString(R.string.notification_stop_action_text);
    }*/
}
