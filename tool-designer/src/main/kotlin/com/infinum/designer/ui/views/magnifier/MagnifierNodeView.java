package com.infinum.designer.ui.views.magnifier;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.infinum.designer.R;

public class MagnifierNodeView extends View {
    private Paint mReticlePaint;
    private Paint mOutlinePaint;
    private Paint mFillPaint;
    private Paint mClearPaint;

    private float mCenterX;
    private float mCenterY;
    private float mRadius;
    private float mReticleRadius;
    private float mDensity;

    public MagnifierNodeView(Context context) {
        this(context, null);
    }

    public MagnifierNodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagnifierNodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MagnifierNodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float twoDp = 2f * dm.density;
        mReticlePaint = new Paint();
        mReticlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReticlePaint.setColor(ContextCompat.getColor(context, R.color.designer_colorPickerNodeReticleColor));
        mReticlePaint.setStrokeWidth(twoDp);
        mReticlePaint.setStyle(Paint.Style.STROKE);

        mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutlinePaint.setColor(ContextCompat.getColor(context, R.color.designer_colorPickerNodeOutlineColor));
        mOutlinePaint.setStrokeWidth(twoDp);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(ContextCompat.getColor(context, R.color.designer_colorPickerNodeFillColor));
        mFillPaint.setStrokeWidth(twoDp);
        mFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mFillPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));

        mClearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClearPaint.setColor(ContextCompat.getColor(context, R.color.designer_colorPickerNodeClearColor));
        mClearPaint.setStrokeWidth(twoDp);
        mClearPaint.setStyle(Paint.Style.FILL);
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mReticleRadius = getResources().getInteger(R.integer.designer_color_picker_sample_width) / 2.0f + twoDp;
        mDensity = dm.density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius = Math.min(w, h) / 2.0f - mDensity * 2f;
        mCenterX = w / 2.0f;
        mCenterY = h / 2.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mFillPaint);
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mOutlinePaint);
        canvas.drawCircle(mCenterX, mCenterY, mReticleRadius, mClearPaint);
        canvas.drawCircle(mCenterX, mCenterY, mReticleRadius, mReticlePaint);
    }
}
