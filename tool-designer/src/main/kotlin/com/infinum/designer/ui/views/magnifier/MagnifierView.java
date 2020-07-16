package com.infinum.designer.ui.views.magnifier;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.textview.MaterialTextView;
import com.infinum.designer.R;

public class MagnifierView extends FrameLayout {

    private MaterialTextView mColorValueTextView;

    private Drawable mMagnifyingLens;
    private Bitmap mPixels;
    private Paint mBitmapPaint;
    private Paint mGridPaint;
    private Paint mPixelOutlinePaint;

    private Rect mSourcePreviewRect;
    private Rect mDestinationPreviewRect;
    private RectF mTargetPixelOutline;

    private Point mInsets;
    private Path mPreviewClipPath;

    private int mCenterPixelColor;

    public MagnifierView(Context context) {
        this(context, null);
    }

    public MagnifierView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagnifierView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MagnifierView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mMagnifyingLens = context.getDrawable(R.drawable.designer_loop_ring);

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(false);
        mBitmapPaint.setDither(true);
        mBitmapPaint.setFilterBitmap(false);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridPaint.setColor(ContextCompat.getColor(context, R.color.designer_colorPickerMagnifierGridColor));
        mGridPaint.setAlpha(128);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setStrokeWidth(1f * dm.density);
        mGridPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));

        mPixelOutlinePaint = new Paint();
        mPixelOutlinePaint.setColor(ContextCompat.getColor(context, R.color.designer_colorPickerMagnifierGridColor));
        mPixelOutlinePaint.setStyle(Paint.Style.STROKE);
        mPixelOutlinePaint.setStrokeWidth(2f * dm.density);
        mPixelOutlinePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));

        final Resources res = getResources();
        mInsets = new Point(res.getDimensionPixelSize(R.dimen.designer_magnified_image_horizontal_inset),
                res.getDimensionPixelSize(R.dimen.designer_magnified_image_vertical_inset));

        int previewSize = res.getDimensionPixelSize(R.dimen.designer_magnified_image_size);
        mDestinationPreviewRect = new Rect(mInsets.x, mInsets.y, mInsets.x + previewSize,
                mInsets.y + previewSize);
        mPreviewClipPath = new Path();
        mPreviewClipPath.addCircle(mDestinationPreviewRect.exactCenterX(),
                mDestinationPreviewRect.exactCenterY(), previewSize / 2f, Path.Direction.CCW);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mColorValueTextView = findViewById(R.id.color_value);
        mColorValueTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = getContext().getSystemService(ClipboardManager.class);
                CharSequence text = mColorValueTextView.getText();
                ClipData primaryClip = cm.getPrimaryClip();
                if (primaryClip == null || primaryClip.getItemAt(0) == null ||
                        !text.equals(cm.getPrimaryClip().getItemAt(0).coerceToText(getContext()))) {
                    ClipData clip = ClipData.newPlainText("color", text);
                    cm.setPrimaryClip(clip);
                    Toast.makeText(v.getContext(),
                            R.string.designer_color_copied_to_clipboard,
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMagnifyingLens.setBounds(0, 0, w, h);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mMagnifyingLens.draw(canvas);
        if (mPixels != null) {
            canvas.clipPath(mPreviewClipPath);
            canvas.drawBitmap(mPixels, mSourcePreviewRect, mDestinationPreviewRect, mBitmapPaint);
            drawGrid(canvas);
            canvas.drawRect(mTargetPixelOutline, mPixelOutlinePaint);
        }
        super.dispatchDraw(canvas);
    }

    public void setPixels(Bitmap pixels) {
        mPixels = pixels;
        mSourcePreviewRect = new Rect(0, 0, pixels.getWidth(), pixels.getHeight());
        mCenterPixelColor = pixels.getPixel(pixels.getWidth() / 2, pixels.getHeight() / 2);

        if (mTargetPixelOutline == null) {
            float pixelSize = (float) mDestinationPreviewRect.width() / pixels.getWidth();
            float x = (mPixels.getWidth() - 1) / 2f * pixelSize;
            float y = (mPixels.getHeight() - 1) / 2f * pixelSize;
            mTargetPixelOutline = new RectF(mDestinationPreviewRect.left + x,
                    mDestinationPreviewRect.top + y, mDestinationPreviewRect.left + x + pixelSize,
                    mDestinationPreviewRect.top + y + pixelSize);
        }

        if (mColorValueTextView != null) {
            mColorValueTextView.setText(String.format("#%06X", mCenterPixelColor & 0x00ffffff));
        }
        invalidate();
    }

    private void drawGrid(Canvas canvas) {
        final float stepSize = mDestinationPreviewRect.width() / (float) mSourcePreviewRect.width();

        for (float x = mDestinationPreviewRect.left + stepSize;
             x <= mDestinationPreviewRect.right; x += stepSize) {
            canvas.drawLine(x, mDestinationPreviewRect.top, x, mDestinationPreviewRect.bottom,
                    mGridPaint);
        }
        for (float y = mDestinationPreviewRect.top + stepSize;
             y <= mDestinationPreviewRect.bottom; y += stepSize) {
            canvas.drawLine(mDestinationPreviewRect.left, y, mDestinationPreviewRect.right, y,
                    mGridPaint);
        }
    }
}
