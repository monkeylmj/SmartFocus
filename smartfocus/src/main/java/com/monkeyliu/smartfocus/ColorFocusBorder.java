package com.monkeyliu.smartfocus;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;

import java.util.Collections;
import java.util.List;

@SuppressLint("ViewConstructor")
public class ColorFocusBorder extends AbsFocusBorder {
    private Paint mShadowPaint;
    private Paint mBorderPaint;
    private int mShadowColor;
    private float mShadowWidth;
    private int mBorderColor;
    private float mBorderWidth;
    private float mBorderRadius;

    private ColorFocusBorder(Context context, RectF paddingOffsetRectF,
                             long animationDuration, float scaleX, float scaleY,
                             int shimmerColor, long shimmerDuration, boolean isShimmerAnim,
                             int shadowColor, float shadowWidth, int borderColor, float borderWidth, float borderRadius) {
        super(context, paddingOffsetRectF,
                animationDuration, scaleX, scaleY,
                shimmerColor, shimmerDuration, isShimmerAnim);
        mShadowColor = shadowColor;
        mShadowWidth = shadowWidth;
        mBorderColor = borderColor;
        mBorderWidth = borderWidth;
        mBorderRadius = borderRadius;

        float padding = mShadowWidth + mBorderWidth;
        mPaddingRectF.set(padding, padding, padding, padding);
        initPaint();
    }

    private void initPaint() {
        if (mShadowWidth > 0) {
            mShadowPaint = new Paint();
            mShadowPaint.setColor(mShadowColor);
            mShadowPaint.setMaskFilter(new BlurMaskFilter(mShadowWidth, BlurMaskFilter.Blur.OUTER));
        }

        if (mBorderWidth > 0) {
            mBorderPaint = new Paint();
            mBorderPaint.setColor(mBorderColor);
            mBorderPaint.setStrokeWidth(mBorderWidth);
            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderPaint.setAntiAlias(true);
        }
    }

    /**
     * 绘制外发光阴影
     *
     * @param canvas canvas
     */
    private void onDrawShadow(Canvas canvas) {
        if (mShadowWidth > 0) {
            canvas.save();
            mTempRectF.set(mFrameRectF);
            mTempRectF.inset(-mBorderWidth, -mBorderWidth);
            //裁剪处理(使阴影矩形框内变为透明)
            if (mBorderRadius > 0) {
                canvas.clipRect(mTempRectF, Region.Op.DIFFERENCE);
            }
            //绘制外发光阴影效果
            canvas.drawRoundRect(mTempRectF, mBorderRadius, mBorderRadius, mShadowPaint);
            canvas.restore();
        }
    }

    /**
     * 绘制边框
     *
     * @param canvas canvas
     */
    private void onDrawBorder(Canvas canvas) {
        if (mBorderWidth > 0) {
            canvas.save();
            mTempRectF.set(mFrameRectF);
            mTempRectF.inset(-mBorderWidth / 2, -mBorderWidth / 2);
            canvas.drawRoundRect(mTempRectF, mBorderRadius, mBorderRadius, mBorderPaint);
            canvas.restore();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onDrawShadow(canvas);
        onDrawBorder(canvas);
        super.onDraw(canvas);
    }

    @Override
    protected List<Animator> getTogetherAnimators(float translationX, float translationY, int newWidth, int newHeight) {
        return Collections.emptyList();
    }

    @Override
    protected List<Animator> getSequentiallyAnimators(float translationX, float translationY, int newWidth, int newHeight) {
        return Collections.emptyList();
    }

    public static final class Builder extends AbsFocusBorder.Builder {
        private int mShadowColor;
        private float mShadowWidth;
        private int mBorderColor;
        private float mBorderWidth;
        private float mBorderRadius;

        public Builder(Context context) {
            super(context);
        }

        public Builder shadowColor(int color) {
            mShadowColor = color;
            return this;
        }

        public Builder shadowColorRes(@ColorRes int colorRes) {
            mShadowColor = Resources.getSystem().getColor(colorRes);
            return this;
        }

        public Builder shadowWidth(float pxWidth) {
            mShadowWidth = pxWidth;
            return this;
        }

        public Builder shadowWidth(int unit, float width) {
            return shadowWidth(TypedValue.applyDimension(
                    unit, width, Resources.getSystem().getDisplayMetrics()));
        }

        public Builder shadowWidthRes(@DimenRes int widthRes) {
            return shadowWidth(mContext.getResources().getDimensionPixelSize(widthRes));
        }

        public Builder borderColor(int color) {
            mBorderColor = color;
            return this;
        }

        public Builder borderColorRes(@ColorRes int colorResId) {
            return borderColor(mContext.getResources().getColor(colorResId));
        }

        public Builder borderWidth(float pxWidth) {
            mBorderWidth = pxWidth;
            return this;
        }

        public Builder borderWidth(int unit, float width) {
            mBorderWidth = TypedValue.applyDimension(
                    unit, width, Resources.getSystem().getDisplayMetrics());
            return this;
        }

        public Builder borderWidthRes(@DimenRes int widthResId) {
            return borderWidth(mContext.getResources().getDimensionPixelSize(widthResId));
        }

        public Builder borderRadius(float borderRadius) {
            mBorderRadius = borderRadius;
            return this;
        }

        public Builder borderRadius(int unit, float borderRadius) {
            return borderRadius(TypedValue.applyDimension(
                    unit, borderRadius, Resources.getSystem().getDisplayMetrics()));
        }

        public Builder borderRadiusRes(@DimenRes int borderRadiusResId) {
            return borderRadius(mContext.getResources().getDimensionPixelSize(borderRadiusResId));
        }

        @Override
        public FocusBorder build(Activity activity) {
            if (null == activity) {
                throw new NullPointerException("The activity cannot be null");
            }
            ViewGroup parent = (ViewGroup) activity.findViewById(android.R.id.content);
            return build(parent);
        }

        @Override
        public FocusBorder build(ViewGroup parent) {
            if (null == parent) {
                throw new NullPointerException("The FocusBorder parent cannot be null");
            }
            ColorFocusBorder borderView = new ColorFocusBorder(parent.getContext(), mPaddingOffsetRectF,
                    mAnimDuration, mScaleX, mScaleY,
                    mShimmerColor, mShimmerDuration, mIsShimmerAnim,
                    mShadowColor, mShadowWidth, mBorderColor, mBorderWidth, mBorderRadius);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(1, 1);
            parent.addView(borderView, lp);
            return borderView;
        }
    }
}
