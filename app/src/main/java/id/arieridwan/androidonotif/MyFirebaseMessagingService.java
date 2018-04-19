package id.arieridwan.androidonotif;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.atomic.AtomicInteger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final static AtomicInteger c = new AtomicInteger(0);
    // hardcoded for temporary
    private final boolean sendImmediately = false;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (sendImmediately) {
            // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
            scheduleJob(remoteMessage);
        } else {
            // Handle message within 10 seconds
            handleNow(remoteMessage);
        }
    }

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob(RemoteMessage remoteMessage) {
        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString("title", remoteMessage.getData().get("title"));
        myExtrasBundle.putString("message", remoteMessage.getData().get("message"));

        FirebaseJobDispatcher dispatcher =
                new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .setRecurring(false)
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setTrigger(Trigger.executionWindow(0, 30))
                .setReplaceCurrent(false)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(
                        Constraint.ON_UNMETERED_NETWORK,
                        Constraint.DEVICE_CHARGING
                )
                .setExtras(myExtrasBundle)
                .build();
        dispatcher.mustSchedule(myJob);
    }

    /**
     * Perform and immediate, but quick, processing of the message.
     */
    private void handleNow(RemoteMessage remoteMessage) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String CHANNEL_ID_CHAT = "myapp-01";
        String CHANNEL_ID_GENERAL = "myapp-02";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Channel 1
            CharSequence chatChannel = "Chat";
            String chatChannelDesc = "Notifications from chat app";
            int chatImportance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChatChannel = new NotificationChannel(CHANNEL_ID_CHAT, chatChannel, chatImportance);
            mChatChannel.setDescription(chatChannelDesc);
            mChatChannel.enableLights(true);
            mChatChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(mChatChannel);

            // Channel 2
            CharSequence generalChannel = "General";
            String generalChannelDesc = "General notifications";
            int generalImportance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mGeneralChannel = new NotificationChannel(CHANNEL_ID_GENERAL, generalChannel, generalImportance);
            mGeneralChannel.setDescription(generalChannelDesc);
            mGeneralChannel.enableLights(true);
            mGeneralChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(mGeneralChannel);

        }

        // Sending push notification to spesific channel
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 123, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_CHAT)
                .setSmallIcon(R.drawable.xmen)
                .setBadgeIconType(R.drawable.xmen)
                .setChannelId(CHANNEL_ID_CHAT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setNumber(1)
                .setSound(defaultSoundUri)
                .setColor(ContextCompat.getColor(this, R.color.white))
                .setWhen(System.currentTimeMillis());

        if (remoteMessage.getData().size() > 0) {
            notificationBuilder.setContentTitle(remoteMessage.getData().get("title"));
            notificationBuilder.setContentText(remoteMessage.getData().get("message"));
        }

        notificationManager.notify(getID(), notificationBuilder.build());
    }

    public int getID() {
        return c.incrementAndGet();
    }

}
