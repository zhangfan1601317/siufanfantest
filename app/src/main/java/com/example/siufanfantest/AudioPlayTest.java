package com.example.siufanfantest;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.siufanfantest.GlobalConfig.AUDIO_FORMAT;
import static com.example.siufanfantest.GlobalConfig.SAMPLE_RATE_INHZ;

public class AudioPlayTest {
    private final String TAG = "AudioPlayTest";
    private  AudioTrack mAudioTrack;
    private Context mContext;

    public AudioPlayTest(Context context){
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void playInModeStream(){
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        final int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ,channelConfig, AUDIO_FORMAT);

        mAudioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ)
                    .setEncoding(AUDIO_FORMAT)
                    .setChannelMask(channelConfig)
                    .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE);

        mAudioTrack.play();

        File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
        try{
            final FileInputStream fileInputStream = new FileInputStream(file);
            final Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    byte[] tempBuffer = new byte[minBufferSize];
                    while(true){
                        try {
                            if (!(fileInputStream.available() > 0)) break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int readCount = 0;
                        try {
                            readCount = fileInputStream.read(tempBuffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(readCount == AudioTrack.ERROR_INVALID_OPERATION ||
                        readCount == AudioTrack.ERROR_BAD_VALUE){
                            continue;
                        }

                        if(readCount != 0 && readCount != -1){
                            mAudioTrack.write(tempBuffer,0,readCount);
                        }
                    }
                }
            };
            new Thread(runnable).start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void playInModeStatic(){
        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            private byte[] audioData;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    InputStream in = mContext.getResources().openRawResource(R.raw.ding);
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        for(int b;(b = in.read())!= -1;){
                            out.write(b);
                            Log.e(TAG, "got the data");
                            audioData = out.toByteArray();
                        }
                    } finally {
                        in.close();
                    }
                } catch (Resources.NotFoundException | IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Log.i(TAG, "Creating track...audioData.length = " + audioData.length);

                // R.raw.ding铃声文件的相关属性为 22050Hz, 8-bit, Mono
                mAudioTrack = new AudioTrack(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build(),
                        new AudioFormat.Builder().setSampleRate(22050)
                                .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build(),
                        audioData.length,
                        AudioTrack.MODE_STATIC,
                        AudioManager.AUDIO_SESSION_ID_GENERATE);
                Log.d(TAG, "Writing audio data...");
                mAudioTrack.write(audioData, 0, audioData.length);
                Log.d(TAG, "Starting playback");
                mAudioTrack.play();
                Log.d(TAG, "Playing");
            }
        };

        asyncTask.execute();
    }

    public void stopPlay(){
        if (mAudioTrack != null) {
            Log.d(TAG, "Stopping");
            mAudioTrack.stop();
            Log.d(TAG, "Releasing");
            mAudioTrack.release();
            Log.d(TAG, "Nulling");
        }
    }
}
