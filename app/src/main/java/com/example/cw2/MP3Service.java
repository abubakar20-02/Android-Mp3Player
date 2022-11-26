package com.example.cw2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MP3Service extends Service {

    private MP3Player mp3Player;
    public static MP3Service mp3Service;
    protected SongProgress SongProgress;
    private NotificationManager Notifications;
    private NotificationCompat.Builder BuildNotification;
    RemoteCallbackList<MyBinder> RemoteCallbackList = new RemoteCallbackList<>();
    int NotificationID = 1;

    //Constructor
    public MP3Service() {
        mp3Service = this;
    }

    //The instance getter used to access binder methods within an inner static class.
    public static MP3Service GetMP3Service() {
        return mp3Service;
    }

    public void PauseMusic() {
        mp3Player.pause();
    }

    public void PlayMusic() {
        mp3Player.play();
    }

    public void StopMusic() {
        if (mp3Player.getState() != MP3Player.MP3PlayerState.STOPPED) {
            mp3Player.stop();
            SongProgress = null;
            Notifications.cancel(NotificationID);
        }
    }

    public void LoadMusic(String uri, String SongName) {

        // if the user clicks stop or selects a different song, then stop current music and load new music.
        if (mp3Player.getState() == MP3Player.MP3PlayerState.STOPPED || !mp3Player.getFilePath().equals(uri)) {
            StopMusic();
            mp3Player.load(uri);
            SongProgress = new SongProgress();
            BuildNotification(SongName);
        }
    }



    // code to build notification with song name.
    private void BuildNotification(String SongName) {
        BuildNotification.setContentTitle("MP3 Player");
        BuildNotification.setContentText("Song playing: " + SongName);
        BuildNotification.setSmallIcon(R.drawable.outline_music_note_24);
        Notifications.notify(NotificationID, BuildNotification.build());
    }


    //Thread for tracking song time.
    protected class SongProgress extends Thread implements Runnable {

        public SongProgress() {
            this.start();
        }

        public void run() {
            long Duration;
            long Progress;

            do {
                // convert to nearest second
                Duration = 1000*(mp3Player.getDuration()/1000);
                Progress = 1000*(mp3Player.getProgress()/1000);
                UpdateProgress();
                Log.d("D", String.valueOf(Duration));
                Log.d("P", String.valueOf(Progress));
            } while (Progress < Duration);
            CloseProgress();
        }
    }

    private void CloseProgress() {
        StopMusic();
        doCallbacks(true, 0, 0);
        Notifications.cancel(NotificationID);
    }

    private void UpdateProgress() {
        doCallbacks(false, mp3Player.getProgress(), mp3Player.getDuration() );

    }

    // Reference: https://github.com/mdf/comp3018/blob/main/MartinBoundService/app/src/main/java/com/example/pszmdf/martinboundservice/CounterService.java
    public void doCallbacks(boolean finished,int CurrentTime, int TotalTime ) {
        final int n = RemoteCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            RemoteCallbackList.getBroadcastItem(i).Callback.SongProgress(finished,CurrentTime, TotalTime);
        }
        RemoteCallbackList.finishBroadcast();
    }



    // used https://developer.android.com/develop/ui/views/notifications/build-notification
    //Initialise notifications
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void CreateNotifications() {

        //Create new notification manager and cancel any notifications in the same ID slot
        Notifications = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notifications.cancel(NotificationID);
        String CHANNEL_ID = "1";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MP3Service";
            String description = "Music Player";
            //Create a new, high-priority notification channel so that notifications remain at the top of the list.
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            Notifications.createNotificationChannel(channel);
        }
        BuildNotification = new NotificationCompat.Builder(this, CHANNEL_ID).setSilent(true).setWhen(0)
                .setPriority(NotificationCompat.PRIORITY_MAX);
    }


    //Class that enables other apps to bind and establish connections.
    public class MyBinder extends Binder implements IInterface {

        ICallback Callback;

        //Defines interface that specifies how a client can communicate with the service
        @Override
        public IBinder asBinder() {
            return this;
        }

        void LoadMusic(String uri, String SongName) {MP3Service.this.LoadMusic(uri, SongName);}

        void PauseMusic() {
            MP3Service.this.PauseMusic();
        }

        void PlayMusic() {
            MP3Service.this.PlayMusic();
        }

        void StopMusic() {
            MP3Service.this.StopMusic();
        }

        public void RegisterCallback(ICallback Callback) {
            this.Callback = Callback;
            RemoteCallbackList.register(MyBinder.this);
        }

        public void UnregisterCallback(ICallback callback) {
            RemoteCallbackList.unregister(MyBinder.this);
        }
    }


    //On creation, create notification.
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onCreate");
        super.onCreate();
        mp3Player = new MP3Player();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CreateNotifications();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    //When service ends then reset the threads and cancel all notifications that are open.
    @Override
    public void onDestroy() {
        Log.d("Service", "onDestroy");
        super.onDestroy();
        Notifications.cancelAll();
        SongProgress = null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        SongProgress.interrupt();
        return super.onUnbind(intent);
    }
}