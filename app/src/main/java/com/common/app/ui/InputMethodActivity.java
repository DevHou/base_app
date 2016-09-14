package com.common.app.ui;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

/**
 * Created by houlijiang on 16/6/28.
 */
public abstract class InputMethodActivity extends BaseActivity {

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener =
        new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
                int contentViewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();

                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(InputMethodActivity.this);

                if (heightDiff <= contentViewTop) {
                    onHideKeyboard();

                    Intent intent = new Intent("KeyboardWillHide");
                    broadcastManager.sendBroadcast(intent);
                } else {
                    int keyboardHeight = heightDiff - contentViewTop;
                    onShowKeyboard(keyboardHeight);

                    Intent intent = new Intent("KeyboardWillShow");
                    intent.putExtra("KeyboardHeight", keyboardHeight);
                    broadcastManager.sendBroadcast(intent);
                }
            }
        };

    private boolean keyboardListenersAttached = false;
    private ViewGroup rootLayout;

    protected void onShowKeyboard(int keyboardHeight) {
    }

    protected void onHideKeyboard() {
    }

    protected void attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return;
        }
        // TODO: 16/6/28 获取根view
        rootLayout = null; // (ViewGroup) findViewById(R.id.rootLayout);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        keyboardListenersAttached = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (keyboardListenersAttached) {
            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(keyboardLayoutListener);
        }
    }

}
