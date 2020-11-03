package com.example.siufanfantest;

import android.app.Application;
import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.siufanfantest.GlobalConfig.AUDIO_FORMAT;
import static com.example.siufanfantest.GlobalConfig.CHANNEL_CONFIG;
import static com.example.siufanfantest.GlobalConfig.SAMPLE_RATE_INHZ;

/**
  * MediaRecorder 直接把手机麦克风录入的音频数据进行编码压缩，AMR,MP3保存文件，
 *  底层也是采用AudioRecord与Android FrameWork层的AudioFlinger进行交互，应用比如简单的录音机
 * AudioRecord 得到一帧原始的PCM裸流，对音频进行算法处理
 **/
public class AudioRecordTest {
    private final String TAG = "AudioRecordTest";

    private AudioRecord mAudioRecord = null;
    private int recordBufSize = 0;

    private Context mContext;

    private boolean mIsRecording = false;

    public AudioRecordTest(Context context){
        mContext = context;
    }

    public void startRecord(){
        recordBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, recordBufSize);

        final byte[] data = new byte[recordBufSize];

        final File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
        if(!file.mkdir()){
            Log.e(TAG, "file mkdir is wrong");
        }
        if(file.exists()){
            file.delete();
        }

        mAudioRecord.startRecording();
        mIsRecording = true;

        new Thread(new Runnable(){

            @Override
            public void run() {
                FileOutputStream os = null;
                try{
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                while(null != os){
                    while(mIsRecording){
                        int content = mAudioRecord.read(data, 0, recordBufSize);

                        if(content != AudioRecord.ERROR_INVALID_OPERATION){
                            try {
                                os.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    try {
                        Log.i(TAG, "run: close file output stream");
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stopRecord(){
        mIsRecording = false;
        if (mAudioRecord != null){
           mAudioRecord.stop();
           mAudioRecord.release();
           mAudioRecord = null;
        }
    }



}
