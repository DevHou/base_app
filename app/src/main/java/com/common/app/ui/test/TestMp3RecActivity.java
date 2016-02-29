package com.common.app.ui.test;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.common.app.R;
import com.common.app.ui.BaseActivity;
import com.common.permission.AppPermissions;
import com.common.utils.FileUtils;
import com.common.mp3recorder.RecMicToMp3;

import java.io.File;

import rx.functions.Action1;


/**
 * Created by houlijiang on 15/12/23.
 * 
 * 语音录制
 */
public class TestMp3RecActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = TestMp3RecActivity.class.getSimpleName();

    private RecMicToMp3 mRecMicToMp3;
    private String mVoiceFilePath;

    private TextView tv;

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_test_mp3rec);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 用来录音的工具类
        File temp = FileUtils.createDirIfNotExists(FileUtils.tryGetGoodDiskCacheDir(this));
        if (temp == null) {
            temp = Environment.getExternalStorageDirectory();
        }
        mVoiceFilePath = temp.getAbsolutePath() + File.separator + "test_rec.mp3";
        try {
            new File(mVoiceFilePath).deleteOnExit();
        } catch (Exception e) {
        }
        mRecMicToMp3 = new RecMicToMp3(mVoiceFilePath, 8000, true);
        mRecMicToMp3.setHandle(micImageHandler);

        findViewById(R.id.test_mp3rec_start).setOnClickListener(this);
        findViewById(R.id.test_mp3rec_stop).setOnClickListener(this);
        findViewById(R.id.test_mp3rec_play).setOnClickListener(this);
        tv = (TextView) findViewById(R.id.test_mp3rec_text);
    }

    private Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case RecMicToMp3.MSG_ERROR_AUDIO_ENCODE:
                case RecMicToMp3.MSG_ERROR_AUDIO_RECORD:
                case RecMicToMp3.MSG_ERROR_CLOSE_FILE:
                case RecMicToMp3.MSG_ERROR_CREATE_FILE:
                case RecMicToMp3.MSG_ERROR_GET_MIN_BUFFERSIZE:
                case RecMicToMp3.MSG_ERROR_REC_START:
                case RecMicToMp3.MSG_ERROR_WRITE_FILE: {
                    tv.append("\nerror!!! code:" + msg.what);
                    break;
                }
                case RecMicToMp3.MSG_REC_STARTED:
                    tv.append("\nstart record");
                    break;
                case RecMicToMp3.MSG_REC_STOPPED:
                    tv.append("\nstop record");
                    break;
                case RecMicToMp3.MSG_VOICE_VOLUME: {
                    int volume = msg.arg1;
                    tv.append("\nrecord volume:" + volume);
                    break;
                }
            }

        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test_mp3rec_start: {
                AppPermissions.getInstance(this).request(android.Manifest.permission.RECORD_AUDIO)
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean b) {
                            if (b) {
                                mRecMicToMp3.start();
                            } else {
                                Toast.makeText(TestMp3RecActivity.this, "请先同意录音权限", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                break;
            }
            case R.id.test_mp3rec_stop: {
                mRecMicToMp3.stop();
                break;
            }
            case R.id.test_mp3rec_play: {
                final MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(mVoiceFilePath);
                    mediaPlayer.prepare();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer.release();
                        }

                    });
                    mediaPlayer.start();
                } catch (Exception e) {
                    Log.e(TAG, "catch exception when play voice, e:" + e.getLocalizedMessage());
                    Toast.makeText(this, "播放音频失败", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
