package com.example.mp3player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView titleView;

    ImageView playBtn;
    ImageView stopBtn;
    ProgressBar mProgressBar;

    // 재생할 음악 파일 경로
    String fileDir;

    boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleView = findViewById(R.id.lab1_title);

        playBtn = findViewById(R.id.lab1_play);
        stopBtn = findViewById(R.id.lab1_stop);
        mProgressBar = findViewById(R.id.lab1_progress);

        playBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        stopBtn.setEnabled(false);

        /*
         * AIDL 예제에서는 다른 모듈에서 권한을 수락받는다.
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 100);
        }

        // 인텐트에 'com.example.PLAY_TO_ACTIVITY'를 action으로 담으면 메인 액티비티의 브로트캐스트
        // 리시버가 작동한다.
        registerReceiver(mReceiver, new IntentFilter("com.example.PLAY_TO_ACTIVITY"));


        // 재생할 음악 파일 경로(내장 메모리)
        fileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Samsung/Music/Over_the_Horizon.mp3";

        Log.d("kkang", "Main Activity / onCreate(...) / 파일 경로: " + fileDir);

        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("file_dir", fileDir);
        /*
         * AIDL 예제에서는 이 부분에서 인텐트에 패키지를 설정했다.
         */

        Log.d("kkang", "Main Activity / onCreate(...) → My Service");
        // My Service를 실행하면서 음악을 재생하기 위해 파일 경로를 전송한다.
        startService(intent);

        Log.d("kkang", " ");
    }

    @Override
    public void onClick(View v) {
        if (v == playBtn) {
            // 인텐트에 'com.example.PLAY_TO_SERVICE'를 action으로 담으면 My Service의 브로트캐스트
            // 리시버가 작동한다.
            Intent intent = new Intent("com.example.PLAY_TO_SERVICE");
            intent.putExtra("mode", "start");

            Log.d("kkang", "Main Activity / onClick(...) / 재생 → My Service / BroadcastReceiver");
            // 서비스의 브로드캐스트 리시버에게 재생 모드(재생)를 전달하면 음악을 재생한다.
            sendBroadcast(intent);

            isPlaying = true;

            ProgressThread mThread = new ProgressThread();
            mThread.start();

            playBtn.setEnabled(false);
            stopBtn.setEnabled(true);

        } else if (v == stopBtn) {
            // 인텐트에 'com.example.PLAY_TO_SERVICE'를 action으로 담으면 My Service의 브로트캐스트
            // 리시버가 작동한다.
            Intent intent = new Intent("com.example.PLAY_TO_SERVICE");
            intent.putExtra("mode", "stop");

            Log.d("kkang", "Main Activity / onClick(...) / 중지 → My Service / BroadcastReceiver");
            // 서비스의 브로드캐스트 리시버에게 재생 모드(중지)를 전달하면 음악을 중지한다.
            sendBroadcast(intent);

            isPlaying = false;

            mProgressBar.setProgress(0);

            playBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        }
        Log.d("kkang", " ");
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mode = intent.getStringExtra("mode");

            Log.d("kkang", "Main Activity / BroadcastReceiver / 재생 모드: " + mode);

            if (mode != null) {
                switch (mode) {
                    case "start": {
                        int duration = intent.getIntExtra("duration", 0);

                        Log.d("kkang", "Main Activity / BroadcastReceiver / 재생 / int duration: " + duration);

                        mProgressBar.setMax(duration);
                        mProgressBar.setProgress(0);

                        break;
                    }
                    case "stop": {
                        isPlaying = false;

                        Log.d("kkang", "Main Activity / BroadcastReceiver / 중지");

                        break;
                    }
                    case "restart": {
                        int duration = intent.getIntExtra("duration", 0);
                        int current = intent.getIntExtra("current", 0);

                        Log.d("kkang", "Main Activity / BroadcastReceiver / 재시작 / int duration: " + duration);
                        Log.d("kkang", "Main Activity / BroadcastReceiver / 재시작 / int current: " + current);

                        mProgressBar.setMax(duration);
                        mProgressBar.setProgress(current);

                        isPlaying = true;

                        ProgressThread mThread = new ProgressThread();
                        mThread.start();

                        playBtn.setEnabled(false);
                        stopBtn.setEnabled(true);

                        break;
                    }
                }
            }
            Log.d("kkang", " ");
        }
    };

    class ProgressThread extends Thread {
        @Override
        public void run() {
            while (isPlaying) {
                Log.d("kkang", "Main Activity / 프로그래스 바 이전 진행도: " + mProgressBar.getProgress());
                // 음악 파일의 전체 길이를 얻으면 191242(191.242초) 처럼 나온다. 1000이 1초 즉, 전체
                // 길이는 191.242고, 1씩 오르는 것이다.
                mProgressBar.incrementProgressBy(1000);

                Log.d("kkang", "Main Activity / 프로그래스 바 현재 진행도: " + mProgressBar.getProgress());
                Log.d("kkang", " ");
                // 1초 마다 게이지가 찬다.
                SystemClock.sleep(1000);

                if (mProgressBar.getProgress() == mProgressBar.getMax()) {
                    isPlaying = false;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }
}