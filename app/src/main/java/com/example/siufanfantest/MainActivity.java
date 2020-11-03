package com.example.siufanfantest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

import static com.example.siufanfantest.GlobalConfig.SAMPLE_RATE_INHZ;
import static com.example.siufanfantest.GlobalConfig.CHANNEL_CONFIG;
import static com.example.siufanfantest.GlobalConfig.AUDIO_FORMAT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "main_test";

    private static final int MY_PERMISSIONS_REQUEST = 1001;

    private Button mBtnControl;
    private Button mBtnPlay;
    private Button mBtnConvert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Permission permission = new Permission(this, getApplicationContext());
        permission.checkPermissions();

        mBtnControl = (Button) findViewById(R.id.btn_control);
        mBtnControl.setOnClickListener(this);
        mBtnConvert = (Button) findViewById(R.id.btn_convert);
        mBtnConvert.setOnClickListener(this);
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, permissions[i] + " 权限被用户禁止！");
                }
            }
            // 运行时权限的申请不是本demo的重点，所以不再做更多的处理，请同意权限申请。
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        AudioRecordTest audioRecordTest = new AudioRecordTest(getApplicationContext());
        AudioPlayTest audioPlayTest = new AudioPlayTest(getApplicationContext());
        switch(v.getId()){
            case R.id.btn_control:
                Button button = (Button)v;
                if(button.getText().toString().equals(getString(R.string.start_record))) {
                    button.setText(getString(R.string.stop_record));
                    audioRecordTest.startRecord();
                } else{
                    button.setText(getString(R.string.start_record));
                    audioRecordTest.stopRecord();
                }
                break;
            case R.id.btn_convert:
                PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
                File pcmFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
                File wavFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.wav");
                if (!wavFile.mkdirs()) {
                    Log.e(TAG, "wavFile Directory not created");
                }
                if (wavFile.exists()) {
                    wavFile.delete();
                }
                pcmToWavUtil.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());

                break;
            case R.id.btn_play:
                Button btn = (Button) v;
                String string = btn.getText().toString();
                if (string.equals(getString(R.string.start_play))) {
                    btn.setText(getString(R.string.stop_play));
                    audioPlayTest.playInModeStream();
                    //playInModeStatic();
                } else {
                    btn.setText(getString(R.string.start_play));
                    audioPlayTest.stopPlay();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }
}
