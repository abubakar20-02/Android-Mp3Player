package com.example.cw2;
// Muhammad Abubakar
// 20314123
// hcyma5

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cw2.databinding.ActivityMainBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    String SongName;
    ActivityMainBinding activityMainBinding;
    private MP3Service.MyBinder mp3Service = null;
    @SuppressLint("SimpleDateFormat") SimpleDateFormat DateFormat = new SimpleDateFormat("mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Creates binding object and sets view
        activityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(activityMainBinding.getRoot());

        //Set song playing to none
        CleanPlayer();
        GetMusicList();

        // code for linking pause button with pause music.
        activityMainBinding.PauseButton.setOnClickListener(v -> mp3Service.PauseMusic());

        // code for linking stop button with stop music.
        activityMainBinding.StopButton.setOnClickListener(v -> mp3Service.StopMusic());

        //code for linking play button with play music.
        activityMainBinding.StartButton.setOnClickListener(v -> mp3Service.PlayMusic());

        //Link the main activity to the MP3 Service
        this.bindService(new Intent(this, MP3Service.class), ServiceConnection,
                Context.BIND_AUTO_CREATE);
        startService(new Intent(this, KillNotificationService.class));
    }

    @Nullable
    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }

    // Code from cw sheet
    @SuppressLint("SetTextI18n")
    private void GetMusicList() {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.IS_MUSIC + "!= 0",
                null,
                null);
        activityMainBinding.musicList.setAdapter(new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[]{MediaStore.Audio.Media.DATA},
                new int[]{android.R.id.text1}));
        activityMainBinding.musicList.setOnItemClickListener((myAdapter, myView, myItemInt, mylng) -> {
            Cursor c = (Cursor) activityMainBinding.musicList.getItemAtPosition(myItemInt);
            @SuppressLint("Range") String uri = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
            File File = new File(uri);
            SongName = File.getName();

            activityMainBinding.SongPlaying.setText(getString(R.string.SongPlayingText) + SongName);
            mp3Service.LoadMusic(uri, SongName);
        });
    }

    // settings for default player setting.
    @SuppressLint("SetTextI18n")
    private void CleanPlayer() {
        String SongName = "None";
        activityMainBinding.SongPlaying.setText(getString(R.string.SongPlayingText) + SongName);
        activityMainBinding.SongTime.setText(getString(R.string.DefaultTime));
        activityMainBinding.TotalTime.setText(getString(R.string.DefaultTime));
        activityMainBinding.songProgress.setProgress(0);
        activityMainBinding.songProgress.setMax(0);
    }

    // function to update player.
    @SuppressLint("SetTextI18n")
    private void UpdatePlayer(int CurrentTime, int TotalTime) {
        activityMainBinding.SongPlaying.setText(getString(R.string.SongPlayingText) + SongName);
        activityMainBinding.SongTime.setText(DateFormat.format(new Date(CurrentTime)));
        activityMainBinding.TotalTime.setText(DateFormat.format(new Date(TotalTime)));
        activityMainBinding.songProgress.setMax(TotalTime);
        activityMainBinding.songProgress.setProgress(CurrentTime);
    }

    //Used to monitor the connection between a service and an application
    private final ServiceConnection ServiceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName trackName, IBinder service) {
            mp3Service = (MP3Service.MyBinder) service;
            mp3Service.RegisterCallback(callback);
        }

        @Override
        public void onServiceDisconnected(ComponentName trackName) {
            mp3Service = null;
            assert false;
            mp3Service.UnregisterCallback(callback);
        }
    };



    //As long as a song is currently playing, this thread will provide the current track time and duration.
    ICallback callback = (SongCompleted, CurrentTime, TotalTime) -> runOnUiThread(() -> {
        if (SongCompleted) {
//            Log.d("Song","Complete");
            CleanPlayer();
        }
        else {
//            Log.d("Song","OnGoing");
            UpdatePlayer(CurrentTime, TotalTime);
        }
    });


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "MainActivity onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "MainActivity onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "MainActivity onResume");
        super.onResume();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "MainActivity onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "MainActivity onStop");
        super.onStop();
    }



    //Code to save instance when activity is destroyed.
    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("SongName", SongName);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SongName = savedInstanceState.getString("SongName");
        activityMainBinding.SongPlaying.setText(getString(R.string.SongPlayingText) + SongName);
    }
}