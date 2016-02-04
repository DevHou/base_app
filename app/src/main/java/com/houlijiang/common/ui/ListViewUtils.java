package com.houlijiang.common.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.houlijiang.app.base.error.ErrorConst;
import com.houlijiang.app.base.error.ErrorModel;
import com.houlijiang.common.R;

/**
 * Created by houlijiang on 16/2/4.
 * 
 * 列表页通用处理工具类
 */
public class ListViewUtils {

    /**
     * activity, fragment共用错误页面显示
     */
    public static void showErrorView(Context context, View mListErrorView, ErrorModel result,
        View.OnClickListener listener) {
        if (result == null)
            return;
        if (mListErrorView == null)
            return;

        ImageView errorImage = (ImageView) mListErrorView.findViewById(R.id.layout_listview_error_image_iv);
        TextView errorNote = (TextView) mListErrorView.findViewById(R.id.layout_listview_error_note_tv);
        Button errorButton = (Button) mListErrorView.findViewById(R.id.layout_listview_error_button_bt);

        if (result.code == ErrorConst.ERROR_CODE_NETWORK_DISCONNECTION) {
            errorImage.setImageResource(R.drawable.ic_error_no_network);
            errorNote.setText(R.string.error_no_network);
            errorButton.setText(R.string.error_no_network_btn_refresh);
            errorButton.setVisibility(View.VISIBLE);
            errorButton.setOnClickListener(listener);
        } else {
            errorImage.setImageResource(R.drawable.ic_error_server);
            String message = result.message;
            if (TextUtils.isEmpty(message)) {
                message = context.getString(R.string.error_server);
            }
            errorNote.setText(message);
            errorButton.setVisibility(View.GONE);
        }
    }
}
