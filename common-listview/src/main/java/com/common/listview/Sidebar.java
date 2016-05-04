package com.common.listview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * 右侧快速检索view
 */
public class Sidebar extends View {
    private Paint paint;
    private TextView header;// 中间大字
    private float height;// 每个字符占用高度
    private Context context;
    private MySectionIndexer indexer;
    private Handler mHandler;
    private String[] sections;// 右侧文字
    private int selected = -1;// 右侧选中的文字index

    public Sidebar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        sections =
            new String[] { "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
                "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.DKGRAY);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.common_list_sidebar_text_size));
        setBackgroundResource(R.drawable.common_list_shape_sidebar_normal_background);
        mHandler = new Handler();
    }

    public void setIndexer(MySectionIndexer index) {
        indexer = index;
        sections = indexer.getSections();
    }

    public void setHeader(TextView tv) {
        header = tv;
    }

    public void reloadSections() {
        if (indexer != null) {
            sections = indexer.getSections();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (sections == null || sections.length == 0) {
            return;
        }
        float center = getWidth() / 2;
        height = getHeight() / sections.length;
        for (int i = sections.length - 1; i > -1; i--) {
            if (i == selected) {
                paint.setColor(ContextCompat.getColor(context, R.color.common_list_sidebar_text_selected));
            } else {
                paint.setColor(ContextCompat.getColor(context, R.color.common_list_sidebar_text_normal));
            }
            canvas.drawText(sections[i], center, height * (i + 1) - height / 2, paint);
        }
    }

    /**
     * 计算手指滑到哪个位置
     * 
     * @param y 纵坐标
     * @return 位置index
     */
    private int sectionForPoint(float y) {
        if (sections == null || sections.length == 0) {
            return 0;
        }
        selected = (int) (y / height);
        if (selected < 0) {
            selected = 0;
        }
        if (selected > sections.length - 1) {
            selected = sections.length - 1;
        }
        return selected;
    }

    /**
     * 设置选择的字母显示
     */
    private void setHeaderTextAndScroll(MotionEvent event) {
        if (indexer == null) {
            return;
        }
        final float y = event.getY();
        // mHandler.post(new Runnable() {
        // @Override
        // public void run() {
        String headerString = sections[sectionForPoint(y)];
        if (header != null) {
            header.setText(headerString);
        }
        int position = indexer.getPositionForSection(sectionForPoint(y));
        indexer.setSelection(position);
        // }
        // });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (sections == null || sections.length == 0) {
                    return false;
                }
                setHeaderTextAndScroll(event);
                if (header != null) {
                    header.setVisibility(View.VISIBLE);
                }
                setBackgroundResource(R.drawable.common_list_shape_sidebar_pressed_background);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                setHeaderTextAndScroll(event);
                return true;
            }
            case MotionEvent.ACTION_UP:
                if (header != null) {
                    header.setVisibility(View.INVISIBLE);
                }
                setBackgroundResource(R.drawable.common_list_shape_sidebar_normal_background);
                return true;
            case MotionEvent.ACTION_CANCEL:
                if (header != null) {
                    header.setVisibility(View.INVISIBLE);
                }
                setBackgroundResource(R.drawable.common_list_shape_sidebar_normal_background);
                return true;
        }
        return super.onTouchEvent(event);
    }

}
