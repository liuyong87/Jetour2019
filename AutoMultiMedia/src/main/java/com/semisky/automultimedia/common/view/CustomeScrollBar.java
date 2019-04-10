package com.semisky.automultimedia.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.semisky.automultimedia.R;

/**
 * Created by xiong on 2017/6/2.
 * scrollview的scrollbar
 */

public class CustomeScrollBar extends View {
    Paint mPaint;
    private Context context;
    private float userNoticeCursorWidth;

    public CustomeScrollBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initRes();
    }

    private void initRes() {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        userNoticeCursorWidth = dp2px(context, 24);
    }

    public CustomeScrollBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomeScrollBar(Context context) {
        this(context, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scrollbar);
        canvas.drawBitmap(bitmap,userNoticeCursorWidth / 4, cursorMoveY,mPaint);
    }

    private float cursorMoveY;

    public void SetOffSet(float cursorMoveY) {
        this.cursorMoveY = cursorMoveY;
        invalidate();
    }

    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
