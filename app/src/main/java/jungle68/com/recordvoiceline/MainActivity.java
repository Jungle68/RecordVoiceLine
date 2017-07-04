package jungle68.com.recordvoiceline;


import android.Manifest;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import jungle68.com.library.core.RecordVoiceLineView;

public class MainActivity extends AppCompatActivity implements Runnable {
    private MediaRecorder mMediaRecorder;
    private boolean isAlive = true;
    private RecordVoiceLineView voiceLineView;
    private RecordVoiceLineView voicLine2;
    List<Integer> voiceDatas = new ArrayList<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mMediaRecorder == null) return;
            double ratio = (double) mMediaRecorder.getMaxAmplitude() / 100;
            double db = 0;// 分贝
            //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
            //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
            //同时，也可以配置灵敏度sensibility
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            //只要有一个线程，不断调用这个方法，就可以使波形变化
            //主要，这个方法必须在ui线程中调用
            int voiceValue = (int) (db + Math.random() * 100);
            voiceDatas.add(voiceValue);
            voiceLineView.setVolume(voiceValue);
//            voicLine2.setVolume(voiceValue);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voiceLineView = (RecordVoiceLineView) findViewById(R.id.voicLine);
//        voicLine2 = (RecordVoiceLineView) findViewById(R.id.voicLine2);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.RECORD_AUDIO)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        if (aBoolean) { // Always true pre-M
                            initRecord();
                        } else {
                            // 需要打开权限
                            Toast.makeText(MainActivity.this,"需要打开录音权限",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initRecord() {
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "hello.log");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        mMediaRecorder.setMaxDuration(1000 * 60 * 10);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaRecorder.start();

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    protected void onDestroy() {
        isAlive = false;
        mMediaRecorder.release();
        mMediaRecorder = null;
        super.onDestroy();
    }

    @Override
    public void run() {
        while (isAlive) {
            handler.sendEmptyMessage(0);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
