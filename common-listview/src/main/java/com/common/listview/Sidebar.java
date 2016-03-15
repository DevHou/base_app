package com.common.listview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.common.listview.R;

/**
 * 右侧快速检索view
 */
public class Sidebar extends View {
    private Paint paint;
    private TextView header;
    private float height;
    private Context context;
    private MySectionIndexer mIndexer;
    private Handler mHandler;
    private String[] sections;

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
        paint.setTextSize(context.getResources().getDisplayMetrics().density * 10);
        mHandler = new Handler();
    }

    public void setIndexer(MySectionIndexer index) {
        mIndexer = index;
        sections = mIndexer.getSections();
    }

    public void setHeader(TextView tv) {
        header = tv;
    }

    public void reloadSections() {
        if (mIndexer != null) {
            sections = mIndexer.getSections();
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
            canvas.drawText(sections[i], center, height * (i + 1) - height / 2, paint);
        }
    }

    private int sectionForPoint(float y) {
        if (sections == null || sections.length == 0) {
            return 0;
        }
        int index = (int) (y / height);
        if (index < 0) {
            index = 0;
        }
        if (index > sections.length - 1) {
            index = sections.length - 1;
        }
        return index;
    }

    private void setHeaderTextAndScroll(MotionEvent event) {
        if (mIndexer == null) {
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
        int position = mIndexer.getPositionForSection(sectionForPoint(y));
        mIndexer.setSelection(position);
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
                setBackgroundResource(R.drawable.common_list_shape_sidebar_background);
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
                setBackgroundColor(Color.TRANSPARENT);
                return true;
            case MotionEvent.ACTION_CANCEL:
                if (header != null) {
                    header.setVisibility(View.INVISIBLE);
                }
                setBackgroundColor(Color.TRANSPARENT);
                return true;
        }
        return super.onTouchEvent(event);
    }

}
