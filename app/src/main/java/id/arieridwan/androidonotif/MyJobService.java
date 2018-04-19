package id.arieridwan.androidonotif;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.concurrent.atomic.AtomicInteger;

import static android.text.TextUtils.isEmpty;

public class MyJobService extends JobService {

    private final static AtomicInteger c = new AtomicInteger(0);

    @Override
    public boolean onStartJob(JobParameters job) {
        String title = job.getExtras().getString("title");
        String message = job.getExtras().getString("message");
        handleNow(title, message);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }

    /**
     * Perform and immediate, but quick, processing of the message.
     */
    private void handleNow(String title, String message) {
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

        if (isEmpty(title) && isEmpty(message)) {
            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(message);
        }

        notificationManager.notify(getID(), notificationBuilder.build());
    }

    public int getID() {
        return c.incrementAndGet();
    }

}
