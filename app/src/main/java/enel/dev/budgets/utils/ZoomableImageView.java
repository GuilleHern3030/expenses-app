package enel.dev.budgets.utils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {

    private final Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor = 1.0f;
    private float translateX = 0f;
    private float translateY = 0f;
    private boolean isInitialScaleSet = false;

    public ZoomableImageView(Context context) {
        super(context);
        init(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
        setScaleType(ScaleType.MATRIX);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isInitialScaleSet) {
            fitImageToView();
            isInitialScaleSet = true;
        }
    }

    private void fitImageToView() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        // Get dimensions of the image and view
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Calculate scale factor to fit the image to the view
        float scale = Math.min((float) viewWidth / drawableWidth, (float) viewHeight / drawableHeight);

        // Calculate the translation to center the image
        float dx = (viewWidth - drawableWidth * scale) / 2;
        float dy = (viewHeight - drawableHeight * scale) / 2;

        // Apply the initial scale and translation
        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dy);
        setImageMatrix(matrix);

        // Update the scaleFactor with the initial scale
        scaleFactor = scale;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));

            matrix.setScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            matrix.postTranslate(translateX, translateY);
            setImageMatrix(matrix);
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            translateX -= distanceX;
            translateY -= distanceY;

            matrix.postTranslate(-distanceX, -distanceY);
            setImageMatrix(matrix);
            return true;
        }
    }
}