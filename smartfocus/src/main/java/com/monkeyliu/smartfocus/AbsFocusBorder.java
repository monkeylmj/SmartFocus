package com.monkeyliu.smartfocus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Keep;
import androidx.core.view.ViewCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsFocusBorder extends View implements FocusBorder, ViewTreeObserver.OnGlobalFocusChangeListener {
    protected static final String TAG = "FocusBorder";
    private static final String TAG_FOCUS = "focus";
    private static final String TAG_IGNORE_FOCUS = "ignore_focus";
    private static final int DEFAULT_ANIMATION_TIME = 200;
    private static final long DEFAULT_SHIMMER_DURATION_TIME = 1000;

    protected RectF mFrameRectF = new RectF();
    protected RectF mPaddingRectF = new RectF();
    protected RectF mPaddingOffsetRectF = new RectF();
    protected RectF mTempRectF = new RectF();

    private WeakReference<View> mOldFocusView;
    private ObjectAnimator mWidthAnimator;
    private ObjectAnimator mHeightAnimator;
    private ObjectAnimator mTranslationXAnimator;
    private ObjectAnimator mTranslationYAnimator;
    private ObjectAnimator mShimmerAnimator;
    private AnimatorSet mAnimatorSet;

    private LinearGradient mShimmerLinearGradient;
    private Matrix mShimmerGradientMatrix;
    private Paint mShimmerPaint;
    private int mShimmerColor = 0x66FFFFFF;
    protected long mShimmerDuration = DEFAULT_SHIMMER_DURATION_TIME;
    private float mShimmerTranslate;
    private boolean mShimmerAnimating;
    private boolean mIsShimmerAnim;

    private long mAnimationDuration;
    private float mScaleX;
    private float mScaleY;

    private int mWidth;
    private int mHeight;

    protected AbsFocusBorder(Context context, RectF paddingOffsetRectF,
                             long animationDuration, float scaleX, float scaleY,
                             int shimmerColor, long shimmerDuration, boolean isShimmerAnim) {
        super(context);
        if (null != paddingOffsetRectF) {
            this.mPaddingOffsetRectF.set(paddingOffsetRectF);
        }
        setLayerType(View.LAYER_TYPE_SOFTWARE, null); //关闭硬件加速
        setVisibility(INVISIBLE);
        setFocusable(false);
        ViewCompat.setTranslationZ(this, 1);

        mAnimationDuration = animationDuration;
        mScaleX = scaleX;
        mScaleY = scaleY;

        mShimmerColor = shimmerColor;
        mShimmerDuration = shimmerDuration;
        mIsShimmerAnim = isShimmerAnim;

        mShimmerPaint = new Paint();
        mShimmerGradientMatrix = new Matrix();
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (w != oldW || h != oldH) {
            mFrameRectF.set(mPaddingRectF.left, mPaddingRectF.top, w - mPaddingRectF.right, h - mPaddingRectF.bottom);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unBindGlobalFocusListener();
    }

    protected void setWidth(int width) {
        if (getLayoutParams().width != width) {
            mWidth = width;
            getLayoutParams().width = width;
            requestLayout();
        }
    }

    protected void setHeight(int height) {
        if (getLayoutParams().height != height) {
            mHeight = height;
            getLayoutParams().height = height;
            requestLayout();
        }
    }

    private void setVisible(boolean visible) {
        if (visible) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(INVISIBLE);
            unHighLightLastFocusView();
        }
    }

    private boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    @Override
    public void onFocus(View focusView) {
        View targetView = getTargetView(focusView);
        if (targetView == null) {
            return;
        }
        //取消高亮上一个焦点View
        unHighLightLastFocusView();
        //高亮焦点View
        highLightFocusView(targetView);
        //移动焦点框
        changeFocusPosition(targetView);
    }

    //基于FocusView获取要高亮的目标View
    private View getTargetView(View focusView) {
        View targetView = focusView;

        if (targetView == null || TAG_IGNORE_FOCUS.equals(targetView.getTag()) || isTargetViewOutOfRange(targetView)) {
            setVisible(false);
            return null;
        }

        if (targetView instanceof ViewGroup) {
            View newTargetView = findFocusTarget((ViewGroup) targetView);
            if (newTargetView != null) {
                targetView = newTargetView;
            }
        }
        return targetView;
    }

    private void unHighLightLastFocusView() {
        if (null != mOldFocusView && null != mOldFocusView.get()) {
            ViewCompat.setTranslationZ(mOldFocusView.get(), -1);
            mOldFocusView.get().animate().scaleX(1.0f).scaleY(1.0f).setDuration(DEFAULT_ANIMATION_TIME).start();
            mOldFocusView.clear();
        }
    }

    private void highLightFocusView(final View targetView) {
        mOldFocusView = new WeakReference<>(targetView);
        ViewCompat.setTranslationZ(targetView, 1);
        targetView.animate().scaleX(mScaleX).scaleY(mScaleY).setDuration(mAnimationDuration).start();
    }

    @Override
    public void bindGlobalFocusListener() {
        getViewTreeObserver().addOnGlobalFocusChangeListener(this);
    }

    @Override
    public void unBindGlobalFocusListener() {
        Log.d(TAG, "unBindGlobalFocusListener");
        getViewTreeObserver().removeOnGlobalFocusChangeListener(this);
        setVisible(false);
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        Log.d(TAG, "onGlobalFocusChanged:" + oldFocus + "," + newFocus);
        onFocus(newFocus);
    }

    private boolean isTargetViewOutOfRange(View targetView) {
        ViewGroup root = (ViewGroup) getParent();

        View descendant = targetView;
        while (descendant != null && descendant != root) {
            ViewParent theParent = descendant.getParent();
            if (theParent instanceof View) {
                descendant = (View) theParent;
            } else {
                break;
            }
        }
        return descendant != root;
    }

    private View findFocusTarget(ViewGroup parent) {
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if (TAG_FOCUS.equals(child.getTag())) {
                return child;
            }
            if (child instanceof ViewGroup) {
                View targetView = findFocusTarget((ViewGroup) child);
                if (targetView != null) {
                    return targetView;
                }
            }
        }
        return null;
    }

    private void changeFocusPosition(View targetView) {
        float paddingWidth = mPaddingRectF.left + mPaddingRectF.right + mPaddingOffsetRectF.left + mPaddingOffsetRectF.right;
        float paddingHeight = mPaddingRectF.top + mPaddingRectF.bottom + mPaddingOffsetRectF.top + mPaddingOffsetRectF.bottom;
        float offsetWidth = targetView.getMeasuredWidth() * (mScaleX - 1.0f) + paddingWidth;
        float offsetHeight = targetView.getMeasuredHeight() * (mScaleY - 1.0f) + paddingHeight;

        RectF fromRect = findLocationWithView(this);
        RectF toRect = findLocationWithView(targetView);

        if (!isVisible()) {
            //第一次可见时，初始化一下位置
            setVisible(true);
            setTranslationX((int) (toRect.left - fromRect.left));
            setTranslationY((int) (toRect.top - fromRect.top));
            setWidth((int) toRect.width());
            setHeight((int) toRect.height());
        }

        toRect.inset(-offsetWidth / 2, -offsetHeight / 2);
        int newWidth = round(toRect.right) - round(toRect.left);
        int newHeight = round(toRect.bottom) - round(toRect.top);
        int translationX = round(toRect.left) - round(fromRect.left);
        int translationY = round(toRect.top) - round(fromRect.top);

        moveFocusToTarget(newWidth, newHeight, translationX, translationY);
    }

    private void moveFocusToTarget(int newWidth, int newHeight, int translationX, int translationY) {
        //同时进行的动画
        List<Animator> together = new ArrayList<>();
        together.add(getTranslationXAnimator(translationX));
        together.add(getTranslationYAnimator(translationY));
        together.add(getWidthAnimator(newWidth));
        together.add(getHeightAnimator(newHeight));

        List<Animator> appendTogether = getTogetherAnimators(translationX, translationY, newWidth, newHeight);
        if (null != appendTogether && !appendTogether.isEmpty()) {
            together.addAll(appendTogether);
        }

        //顺序进行的动画
        List<Animator> sequentially = new ArrayList<>();
        if (mIsShimmerAnim) {
            sequentially.add(getShimmerAnimator());
        }
        List<Animator> appendSequentially = getSequentiallyAnimators(translationX, translationY, newWidth, newHeight);
        if (null != appendSequentially && !appendSequentially.isEmpty()) {
            sequentially.addAll(appendSequentially);
        }

        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.setInterpolator(new DecelerateInterpolator(1));
        mAnimatorSet.playTogether(together);
        mAnimatorSet.playSequentially(sequentially);
        mAnimatorSet.start();
    }

    //五舍六入
    private int round(float number) {
        return -Math.round(-number);
    }

    private RectF findLocationWithView(View targetFocusedView) {
        final RectF rect = new RectF();
        final ViewGroup root = (ViewGroup) getParent();
        if (targetFocusedView == root) {
            return rect;
        }

        View descendant = targetFocusedView;

        // search and offset up to the parent
        while (descendant != null && descendant != root) {
            rect.offset(descendant.getLeft() - descendant.getScrollX(),
                    descendant.getTop() - descendant.getScrollY());

            if (descendant instanceof FocusRecyclerView) {
                FocusRecyclerView rv = (FocusRecyclerView) descendant;
                Point offsetPoint = rv.getScrollValue();
                rect.offset(-offsetPoint.x, -offsetPoint.y);
            }

            ViewParent theParent = descendant.getParent();
            if (theParent instanceof View) {
                descendant = (View) theParent;
            } else {
                break;
            }
        }

        rect.right = rect.left + targetFocusedView.getMeasuredWidth();
        rect.bottom = rect.top + targetFocusedView.getMeasuredHeight();
        return rect;
    }

    private ObjectAnimator getHeightAnimator(int height) {
        if (null == mHeightAnimator) {
            mHeightAnimator = ObjectAnimator.ofInt(this, "height", mHeight, height)
                    .setDuration(DEFAULT_ANIMATION_TIME);
        } else {
            mHeightAnimator.setIntValues(mHeight, height);
        }
        return mHeightAnimator;
    }

    private ObjectAnimator getWidthAnimator(int width) {
        if (null == mWidthAnimator) {
            mWidthAnimator = ObjectAnimator.ofInt(this, "width", mWidth, width)
                    .setDuration(DEFAULT_ANIMATION_TIME);
        } else {
            mWidthAnimator.setIntValues(mWidth, width);
        }
        return mWidthAnimator;
    }

    private ObjectAnimator getTranslationXAnimator(float x) {
        if (null == mTranslationXAnimator) {
            mTranslationXAnimator = ObjectAnimator.ofFloat(this, "translationX", x)
                    .setDuration(DEFAULT_ANIMATION_TIME);
        } else {
            mTranslationXAnimator.setFloatValues(x);
        }
        return mTranslationXAnimator;
    }

    private ObjectAnimator getTranslationYAnimator(float y) {
        if (null == mTranslationYAnimator) {
            mTranslationYAnimator = ObjectAnimator.ofFloat(this, "translationY", y)
                    .setDuration(DEFAULT_ANIMATION_TIME);
        } else {
            mTranslationYAnimator.setFloatValues(y);
        }
        return mTranslationYAnimator;
    }

    private ObjectAnimator getShimmerAnimator() {
        if (null == mShimmerAnimator) {
            mShimmerAnimator = ObjectAnimator.ofFloat(this, "shimmerTranslate", -1f, 1f);
            mShimmerAnimator.setInterpolator(new LinearInterpolator());
            mShimmerAnimator.setDuration(mShimmerDuration);
            mShimmerAnimator.setStartDelay(400);
            mShimmerAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setShimmerAnimating(true);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setShimmerAnimating(false);
                }
            });
        }
        return mShimmerAnimator;
    }

    private void setShimmerAnimating(boolean shimmerAnimating) {
        mShimmerAnimating = shimmerAnimating;
        if (mShimmerAnimating) {
            mTempRectF.set(mFrameRectF);
            mTempRectF.left += mPaddingOffsetRectF.left;
            mTempRectF.top += mPaddingOffsetRectF.top;
            mTempRectF.right -= mPaddingOffsetRectF.right;
            mTempRectF.bottom -= mPaddingOffsetRectF.bottom;
            mShimmerLinearGradient = new LinearGradient(
                    0, 0, mTempRectF.width(), mTempRectF.height(),
                    new int[]{0x00FFFFFF, 0x1AFFFFFF, mShimmerColor, 0x1AFFFFFF, 0x00FFFFFF},
                    new float[]{0f, 0.2f, 0.5f, 0.8f, 1f}, Shader.TileMode.CLAMP);
            mShimmerPaint.setShader(mShimmerLinearGradient);
        }
    }

    @Keep
    protected void setShimmerTranslate(float shimmerTranslate) {
        mShimmerTranslate = shimmerTranslate;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Keep
    protected float getShimmerTranslate() {
        return mShimmerTranslate;
    }

    /**
     * 添加同时进行的动画集.
     *
     * @param translationX 焦点框X方向上移动的距离
     * @param translationY 焦点框Y方向上移动的距离
     * @param newWidth     焦点框的宽度
     * @param newHeight    焦点框的高度
     * @return 动画列表
     */
    protected abstract List<Animator> getTogetherAnimators(float translationX, float translationY, int newWidth, int newHeight);

    /**
     * 添加顺序进行的动画集.
     *
     * @param translationX 焦点框X方向上移动的距离
     * @param translationY 焦点框Y方向上移动的距离
     * @param newWidth     焦点框的宽度
     * @param newHeight    焦点框的高度
     * @return 动画列表
     */
    protected abstract List<Animator> getSequentiallyAnimators(float translationX, float translationY, int newWidth, int newHeight);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onDrawShimmer(canvas);
    }

    protected void onDrawShimmer(Canvas canvas) {
        if (mShimmerAnimating) {
            canvas.save();
            mTempRectF.set(mFrameRectF);
            mTempRectF.left += mPaddingOffsetRectF.left;
            mTempRectF.top += mPaddingOffsetRectF.top;
            mTempRectF.right -= mPaddingOffsetRectF.right;
            mTempRectF.bottom -= mPaddingOffsetRectF.bottom;
            float shimmerTranslateX = mTempRectF.width() * mShimmerTranslate;
            float shimmerTranslateY = mTempRectF.height() * mShimmerTranslate;
            mShimmerGradientMatrix.setTranslate(shimmerTranslateX, shimmerTranslateY);
            mShimmerLinearGradient.setLocalMatrix(mShimmerGradientMatrix);
            canvas.drawRoundRect(mTempRectF, 0, 0, mShimmerPaint);
            canvas.restore();
        }
    }

    public abstract static class Builder {
        protected int mShimmerColor = 0x66FFFFFF;
        protected boolean mIsShimmerAnim;
        protected long mShimmerDuration = AbsFocusBorder.DEFAULT_SHIMMER_DURATION_TIME;
        protected long mAnimDuration = AbsFocusBorder.DEFAULT_ANIMATION_TIME;

        protected RectF mPaddingOffsetRectF = new RectF();

        protected float mScaleX = 1.0f;
        protected float mScaleY = 1.0f;

        protected Context mContext;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder padding(float padding) {
            return padding(padding, padding, padding, padding);
        }

        public Builder paddingRes(@DimenRes int paddingResId) {
            return padding(mContext.getResources().getDimensionPixelSize(paddingResId));
        }

        public Builder padding(float left, float top, float right, float bottom) {
            mPaddingOffsetRectF.left = left;
            mPaddingOffsetRectF.top = top;
            mPaddingOffsetRectF.right = right;
            mPaddingOffsetRectF.bottom = bottom;
            return this;
        }

        public Builder shimmerColor(int color) {
            mShimmerColor = color;
            return this;
        }

        public Builder shimmerColorRes(@ColorRes int colorResId) {
            return shimmerColor(mContext.getResources().getColor(colorResId));
        }

        public Builder shimmerDuration(long duration) {
            mShimmerDuration = duration;
            return this;
        }

        public Builder enableShimmer() {
            mIsShimmerAnim = true;
            return this;
        }

        public Builder animDuration(long duration) {
            mAnimDuration = duration;
            return this;
        }

        public Builder scaleX(float scaleX) {
            mScaleX = scaleX;
            return this;
        }

        public Builder scaleY(float scaleY) {
            mScaleY = scaleY;
            return this;
        }

        public abstract FocusBorder build(Activity activity);

        public abstract FocusBorder build(ViewGroup viewGroup);
    }
}
