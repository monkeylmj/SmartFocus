package com.monkeyliu.smartfocus;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import java.util.Collections;
import java.util.List;

@SuppressLint("ViewConstructor")
public class DrawableFocusBorder extends AbsFocusBorder {
    private Drawable mBorderDrawable;

    private DrawableFocusBorder(Context context, RectF paddingOffsetRectF,
                                long animationDuration, float scaleX, float scaleY,
                                int shimmerColor, long shimmerDuration, boolean isShimmerAnim,
                                Drawable borderDrawable) {
        super(context, paddingOffsetRectF,
                animationDuration, scaleX, scaleY,
                shimmerColor, shimmerDuration, isShimmerAnim);

        mBorderDrawable = borderDrawable;

        Rect paddingRect = new Rect();
        mBorderDrawable.getPadding(paddingRect);
        mPaddingRectF.set(paddingRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mBorderDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        mBorderDrawable.draw(canvas);
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
        private Drawable mBorderDrawable;

        public Builder(Context context) {
            super(context);
        }

        public Builder borderDrawableRes(int drawableResId) {
            return borderDrawable(ContextCompat.getDrawable(mContext, drawableResId));
        }

        public Builder borderDrawable(Drawable drawable) {
            mBorderDrawable = drawable;
            return this;
        }

        @Override
        public FocusBorder build(Activity activity) {
            if (null == activity) {
                throw new NullPointerException("The activity cannot be null");
            }
            if (null == mBorderDrawable) {
                throw new NullPointerException("drawable cannot be null");
            }
            final ViewGroup parent = (ViewGroup) activity.findViewById(android.R.id.content);
            return build(parent);
        }

        @Override
        public FocusBorder build(ViewGroup parent) {
            if (null == parent) {
                throw new NullPointerException("The FocusBorder parent cannot be null");
            }
            DrawableFocusBorder border = new DrawableFocusBorder(
                    parent.getContext(), mPaddingOffsetRectF,
                    mAnimDuration, mScaleX, mScaleY,
                    mShimmerColor, mShimmerDuration, mIsShimmerAnim, mBorderDrawable);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(1, 1);
            parent.addView(border, lp);
            return border;
        }
    }
}
