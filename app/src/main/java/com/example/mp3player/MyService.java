package com.example.mp3player;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MyService extends Service implements MediaPlayer.OnCompletionListener {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("kkang", "My Service / onCreate()");
        Log.d("kkang", " ");

        // 인텐트에 'com.example.PLAY_TO_SERVICE'를 action으로 담으면 서비스의 브로트캐스트 리시버가 작동한다.
        registerReceiver(mReceiver, new IntentFilter("com.example.PLAY_TO_SERVICE"));
    }

    MediaPlayer mPlayer;
    // 재생할 음악 파일 경로(내장 메모리)
    String fileDir;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 재생할 음악 파일 경로(내장 메모리)
        fileDir = intent.getStringExtra("file_dir");

        Log.d("kkang", "My Service / onStartCommand(...) / 파일 경로: " + fileDir);

        if (mPlayer != null) {
            // 인텐트에 'com.example.PLAY_TO_ACTIVITY'를 action으로 담으면 메인 액티비티의 브로트캐스트 리시버가 작동한다.
            Intent bIntent = new Intent("com.example.PLAY_TO_ACTIVITY");
            bIntent.putExtra("mode", "restart");
            bIntent.putExtra("duration", mPlayer.getDuration());
            bIntent.putExtra("current", mPlayer.getCurrentPosition());

            Log.d("kkang", "My Service / onStartCommand(...) / mPlayer != null / mPlayer.getDuration(): " + mPlayer.getDuration());
            Log.d("kkang", "My Service / onStartCommand(...) / mPlayer != null / mPlayer.getCurrentPosition(): " + mPlayer.getCurrentPosition());

            Log.d("kkang", "My Service / onStartCommand(...) / mPlayer != null → Main Activity / BroadcastReceiver");

            sendBroadcast(bIntent);
        }
        Log.d("kkang", "My Service / onStartCommand(...) / 음악을 재생할 준비가 되었습니다!");
        Log.d("kkang", " ");

        return super.onStartCommand(intent, flags, startId);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mode = intent.getStringExtra("mode");

            Log.d("kkang", "My Service / BroadcastReceiver / 재생 모드: " + mode);

            if (mode != null) {
                if (mode.equals("start")) {
                    if (mPlayer != null && mPlayer.isPlaying()) {
                        Log.d("kkang", "My Service / BroadcastReceiver / 재생 / mPlayer != null && mPlayer.isPlaying()");

                        mPlayer.stop();
                        mPlayer.release();
                        mPlayer = null;
                    }
                    mPlayer = new MediaPlayer();
                    try {
                        mPlayer.setDataSource(fileDir);
                        mPlayer.prepare();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("kkang", "My Service / BroadcastReceiver / 재생 / IOException e: " + e);
                    }
                    mPlayer.start();

                    // 인텐트에 'com.example.PLAY_TO_ACTIVITY'를 action으로 담으면 메인 액티비티의 브로트캐스트 리시버가 작동한다.
                    Intent bIntent = new Intent("com.example.PLAY_TO_ACTIVITY");
                    bIntent.putExtra("mode", "start");
                    bIntent.putExtra("duration", mPlayer.getDuration());

                    Log.d("kkang", "My Service / BroadcastReceiver / 재생 / mPlayer.getDuration(): " + mPlayer.getDuration());
                    Log.d("kkang", "My Service / BroadcastReceiver / 재생 → Main Activity / BroadcastReceiver");
                    // 메인 액티비티의 브로드캐스트 리시버에게 재생 모드(재생)와 길이를 전달하면, 프로그래스 바의 최대 길이를 설정하고 현재 진행도를 초기화한다.
                    sendBroadcast(bIntent);

                } else if (mode.equals("stop")) {
                    if (mPlayer != null && mPlayer.isPlaying()) {
                        Log.d("kkang", "My Service / BroadcastReceiver / 중지 / mPlayer != null && mPlayer.isPlaying()");

                        mPlayer.stop();
                        mPlayer.release();
                        mPlayer = null;
                    }
                    Log.d("kkang", "My Service / BroadcastReceiver / 중지");
                }
            }
            Log.d("kkang", " ");
        }
    };

    public MyService() { }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Intent intent = new Intent("com.example.PLAY_TO_ACTIVITY");
        intent.putExtra("mode", "stop");

        Log.d("kkang", "My Service / onCompletion(...) → Main Activity / BroadcastReceiver");
        Log.d("kkang", " ");

        sendBroadcast(intent);

        stopSelf();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }
}