/* 
 * Copyright (c) 2011-2012 Yuichi Hirano
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.houlijiang.common.mp3recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import com.opensource.common.mp3rec.Lame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * mp3 音频录制
 */
public class RecMicToMp3 {

    static {
        System.loadLibrary("lamer");
    }

    /**
     * 文件路径
     */
    private String mFilePath;

    /**
     *
     */
    private int mSampleRate;

    /**
     * 是否在录音中
     */
    private boolean mIsRecording = false;

    /**
     * 是否发送语音大小消息
     */
    private boolean mIfSendVolume = false;

    private int mVoiceLen = 0;

    private Timer mTimer;

    /**
     * 录制中事件回调
     *
     * @see RecMicToMp3#MSG_REC_STARTED
     * @see RecMicToMp3#MSG_REC_STOPPED
     * @see RecMicToMp3#MSG_ERROR_GET_MIN_BUFFERSIZE
     * @see RecMicToMp3#MSG_ERROR_CREATE_FILE
     * @see RecMicToMp3#MSG_ERROR_REC_START
     * @see RecMicToMp3#MSG_ERROR_AUDIO_RECORD
     * @see RecMicToMp3#MSG_ERROR_AUDIO_ENCODE
     * @see RecMicToMp3#MSG_ERROR_WRITE_FILE
     * @see RecMicToMp3#MSG_ERROR_CLOSE_FILE
     */
    private Handler mHandler;

    /**
     * 录制开始
     */
    public static final int MSG_REC_STARTED = 0;

    /**
     * 录制结束
     */
    public static final int MSG_REC_STOPPED = 1;

    /**
     * 获取最小buffer错误
     */
    public static final int MSG_ERROR_GET_MIN_BUFFERSIZE = 2;

    /**
     * 创建文件错误
     */
    public static final int MSG_ERROR_CREATE_FILE = 3;

    /**
     * 开始录音错误
     */
    public static final int MSG_ERROR_REC_START = 4;

    /**
     * 录音错误
     */
    public static final int MSG_ERROR_AUDIO_RECORD = 5;

    /**
     * 编码错误
     */
    public static final int MSG_ERROR_AUDIO_ENCODE = 6;

    /**
     * 写文件错误
     */
    public static final int MSG_ERROR_WRITE_FILE = 7;

    /**
     * 关闭文件错误
     */
    public static final int MSG_ERROR_CLOSE_FILE = 8;

    /**
     * 声音大小
     */
    public static final int MSG_VOICE_VOLUME = 9;

    /**
     * @param filePath   文件路径
     * @param sampleRate 取样率
     */
    public RecMicToMp3(String filePath, int sampleRate) {
        this(filePath, sampleRate, false);
    }

    /**
     * @param filePath   文件路径
     * @param sampleRate 取样率
     */
    public RecMicToMp3(String filePath, int sampleRate, boolean sendVolume) {
        if (sampleRate <= 0) {
            throw new InvalidParameterException(
                    "Invalid sample rate specified.");
        }
        this.mFilePath = filePath;
        this.mSampleRate = sampleRate;
        this.mIfSendVolume = sendVolume;
    }

    private void startRecordTime() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mVoiceLen++;
            }
        }, 1000, 1000);
    }

    private void stopRecordTime() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 开始录音
     */
    public void start() {
        //
        if (mIsRecording) {
            return;
        }

        mVoiceLen = 0;
        //
        new Thread() {

            int mVolume = 0;

            @Override
            public void run() {
                android.os.Process
                        .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                //
                final int minBufferSize = AudioRecord.getMinBufferSize(
                        mSampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                //
                if (minBufferSize < 0) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR_GET_MIN_BUFFERSIZE);
                    }
                    return;
                }
                //
                // W/AudioFlinger(75): RecordThread: buffer overflow
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, mSampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);

                // PCM buffer size (5sec)
                short[] buffer = new short[mSampleRate * (16 / 8) * 1 * 5]; // SampleRate[Hz] * 16bit * Mono * 5sec
                byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(new File(mFilePath));
                } catch (FileNotFoundException e) {
                    //
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR_CREATE_FILE);
                    }
                    return;
                }

                // Lame init
                Lame.init(mSampleRate, 1, mSampleRate, 32);

                mIsRecording = true;
                try {
                    try {
                        audioRecord.startRecording();
                    } catch (IllegalStateException e) {
                        //
                        if (mHandler != null) {
                            mHandler.sendEmptyMessage(MSG_ERROR_REC_START);
                        }
                        return;
                    }

                    try {
                        //
                        if (mHandler != null) {
                            mHandler.sendEmptyMessage(MSG_REC_STARTED);
                        }

                        startRecordTime();
                        int readSize = 0;
                        while (mIsRecording) {
                            readSize = audioRecord.read(buffer, 0, minBufferSize);
                            if (readSize < 0) {
                                //
                                if (mHandler != null) {
                                    mHandler.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD);
                                }
                                break;
                            }
                            //
                            else if (readSize == 0) {
                                ;
                            }
                            //
                            else {
                                if (mIfSendVolume) {
                                    calculateRealVolume(buffer, readSize);
                                }
                                int encResult = Lame.encode(buffer,
                                        buffer, readSize, mp3buffer);
                                if (encResult < 0) {
                                    //
                                    if (mHandler != null) {
                                        mHandler.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE);
                                    }
                                    break;
                                }
                                if (encResult != 0) {
                                    try {
                                        output.write(mp3buffer, 0, encResult);
                                    } catch (IOException e) {
                                        //
                                        if (mHandler != null) {
                                            mHandler.sendEmptyMessage(MSG_ERROR_WRITE_FILE);
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        int flushResult = Lame.flush(mp3buffer);
                        if (flushResult < 0) {
                            //
                            if (mHandler != null) {
                                mHandler.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE);
                            }
                        }
                        if (flushResult != 0) {
                            try {
                                output.write(mp3buffer, 0, flushResult);
                            } catch (IOException e) {
                                //
                                if (mHandler != null) {
                                    mHandler.sendEmptyMessage(MSG_ERROR_WRITE_FILE);
                                }
                            }
                        }

                        try {
                            output.close();
                        } catch (IOException e) {
                            //
                            if (mHandler != null) {
                                mHandler.sendEmptyMessage(MSG_ERROR_CLOSE_FILE);
                            }
                        }
                        stopRecordTime();
                    } finally {
                        audioRecord.stop(); //
                        audioRecord.release();
                    }
                } finally {
                    Lame.close();
                    mIsRecording = false; //
                }

                //
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_REC_STOPPED);
                }
            }

            /**
             * 此计算方法来自samsung开发范例
             *
             * @param buffer
             * @param readSize
             */
            private void calculateRealVolume(short[] buffer, int readSize) {
                int sum = 0;
                for (int i = 0; i < readSize; i++) {
                    // 这里没有做运算的优化，为了更加清晰的展示代码
                    sum += buffer[i] * buffer[i];
                }
                if (readSize > 0) {
                    double amplitude = sum / readSize;
                    mVolume = (int) Math.sqrt(amplitude);
                }
                if (mHandler != null) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_VOICE_VOLUME, mVolume / 100, 0));
                }
            }

        }.start();
    }

    /**
     * 结束录音
     */
    public int stop() {
        mIsRecording = false;
        return mVoiceLen;
    }

    /**
     * 放弃录音
     */
    public void discardRecording() {
        mIsRecording = false;
        try {
            new File(mFilePath).deleteOnExit();
        } catch (Exception e) {
        }
    }

    /**
     * 是否在录音
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    /**
     * @param handler 事件回调处理
     * @see RecMicToMp3#MSG_REC_STARTED
     * @see RecMicToMp3#MSG_REC_STOPPED
     * @see RecMicToMp3#MSG_ERROR_GET_MIN_BUFFERSIZE
     * @see RecMicToMp3#MSG_ERROR_CREATE_FILE
     * @see RecMicToMp3#MSG_ERROR_REC_START
     * @see RecMicToMp3#MSG_ERROR_AUDIO_RECORD
     * @see RecMicToMp3#MSG_ERROR_AUDIO_ENCODE
     * @see RecMicToMp3#MSG_ERROR_WRITE_FILE
     * @see RecMicToMp3#MSG_ERROR_CLOSE_FILE
     */
    public void setHandle(Handler handler) {
        this.mHandler = handler;
    }
}
