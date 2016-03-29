package com.common.app.ui.test;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.common.app.R;
import com.common.app.base.manager.DownloadManager;
import com.common.app.base.manager.NetworkChangeManager;
import com.common.app.ui.BaseActivity;
import com.common.network.HttpResponseError;
import com.common.network.HttpWorker;
import com.common.network.IHttpResponse;
import com.common.utils.AppLog;
import com.common.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by houlijiang on 16/1/20.
 * 
 * 测试网络库
 */
public class TestNetActivity extends BaseActivity implements View.OnClickListener,
    NetworkChangeManager.INetChangedListener {

    private static final String TAG = TestNetActivity.class.getSimpleName();

    private EditText mEtDownload;
    private EditText mEtUpload;
    private EditText mEtDownloadTimes;
    private TextView mTvStatus;

    private String[] images =
        new String[] {
            "http://down.360safe.com/360kan/360Video_ys4919.apk",
            "http://ww2.sinaimg.cn/mw600/bf559e10jw1e5zm8nn7xfj20e409wjsi.jpg",
            "http://imgsrc.baidu.com/forum/pic/item/72f082025aafa40fde0ca798ab64034f79f019a8.jpg",
            "http://imgsrc.baidu.com/forum/w%3D580/sign=f4d658fe9c3df8dca63d8f99fd1172bf/05378a82b9014a90721a10b4aa773912b31beeb7.jpg",
            "http://ww1.sinaimg.cn/mw600/bd992edbtw1e4ckol6tjqj20jg0gomyx.jpg",
            "http://ww1.sinaimg.cn/mw600/b58d5f62gw1e35tzlv01pj.jpg",
            "http://ww1.sinaimg.cn/mw600/62245359gw1e96n7buuhuj20c8086aag.jpg",
            "http://ww2.sinaimg.cn/mw600/682c28ecgw1e4r9oaaaqej20jg0cwdmy.jpg",
            "http://ww1.sinaimg.cn/mw600/a04e1fb1tw1e43dc2ymgwj20e609gq43.jpg",
            "http://imgsrc.baidu.com/forum/w%3D580/sign=f09bb261cfbf6c81f7372ce08c3eb1d7/c213c895d143ad4bbd0a10c981025aafa40f06b6.jpg" };

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_test_network);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.test_net_btn_download).setOnClickListener(this);
        findViewById(R.id.test_net_btn_upload).setOnClickListener(this);
        findViewById(R.id.test_net_btn_download_manager).setOnClickListener(this);
        mEtDownload = (EditText) findViewById(R.id.test_net_et_download);
        mEtUpload = (EditText) findViewById(R.id.test_net_et_upload);
        mEtDownloadTimes = (EditText) findViewById(R.id.test_net_et_download_times);
        mTvStatus = (TextView) findViewById(R.id.test_net_tv_status);

        mEtDownload.setText("http://b.hiphotos.baidu.com/image/pic/item/caef76094b36acafdfa7ddfb7bd98d1001e99c76.jpg");

        NetworkChangeManager.getInstance().registerNetChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        NetworkChangeManager.getInstance().unRegisterNetChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onNetWorkChanged(NetworkChangeManager.NetworkStatus status) {
        Toast.makeText(TestNetActivity.this, "网络变化：" + status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.test_net_btn_download) {
            String url = mEtDownload.getText().toString();
            File tempFile =
                FileUtils.createDirIfNotExists(FileUtils.tryGetGoodDiskCacheDir(this) + File.separator + "temp");
            HttpWorker.download(this, url, null, new File(tempFile, "testnet"), null, new IHttpResponse<File>() {
                @Override
                public void onSuccess(@NonNull File file, Object param) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvStatus.setText("下载完成");
                        }
                    });
                }

                @Override
                public void onFailed(@NonNull final HttpResponseError error, Object param) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvStatus.setText("下载失败：" + error.getReason());
                        }
                    });
                }

                @Override
                public void onProgress(final long donebytes, final long totalbytes, Object param) {
                    AppLog.d(TAG, "下载中：" + donebytes + "/" + totalbytes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvStatus.setText("下载中：" + donebytes + "/" + totalbytes);
                        }
                    });
                }

            }, 0);
        } else if (view.getId() == R.id.test_net_btn_upload) {

        } else if (view.getId() == R.id.test_net_btn_download_manager) {
            int times = Integer.parseInt(mEtDownloadTimes.getText().toString());
            if (times > images.length) {
                times = images.length;
            }
            final List<Integer> successList = new ArrayList<>();
            final List<Integer> failList = new ArrayList<>();
            for (int i = 0; i < times; i++) {
                DownloadManager.DownloadItem item = new DownloadManager.DownloadItem();
                item.origin = TestNetActivity.this;
                item.maxRetryTimes = 3;
                item.param = i;
                item.url = images[i];
                File tempFile =
                    FileUtils.createDirIfNotExists(FileUtils.tryGetGoodDiskCacheDir(this) + File.separator + "temp");
                item.target = new File(tempFile, "test" + i);
                item.callback = new DownloadManager.DownloadCallback() {
                    @Override
                    public void onFinish(boolean success, Object param) {
                        if (success) {
                            successList.add((int) param);
                        } else {
                            failList.add((int) param);
                        }
                        updateDownloadManagerStatus(successList, failList, -1, 0, 0);
                    }

                    @Override
                    public void progress(long done, long total, Object param) {
                        updateDownloadManagerStatus(successList, failList, (int) param, done, total);
                    }
                };
                DownloadManager.getInstance().addToDownloadQueue(item);
            }
        }
    }

    public void updateDownloadManagerStatus(List<Integer> success, List<Integer> fail, int curr, long done, long total) {
        StringBuffer buf = new StringBuffer();
        buf.append("成功：");
        for (Integer i : success) {
            buf.append(i).append(",");
        }
        buf.append("  失败：");
        for (Integer i : fail) {
            buf.append(i).append(",");
        }

        buf.append("  当前：").append(curr).append("  ").append(done).append("/").append(total);
        mTvStatus.setText(buf.toString());
    }
}
