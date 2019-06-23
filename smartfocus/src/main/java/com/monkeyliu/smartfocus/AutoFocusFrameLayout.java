package com.monkeyliu.smartfocus;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * Created by monkey on 2017/9/1.
 * 焦点处理ViewGroup, 被此ViewGroup包含的focusable view获取焦点时会有统一的效果。
 */
public class AutoFocusFrameLayout extends FrameLayout {

    private static final String TAG = "AutoFocusFrameLayout";
    private FocusBorder mFocusBorder;

    public AutoFocusFrameLayout(Context context) {
        super(context);
        init();
    }

    public AutoFocusFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoFocusFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mFocusBorder = new ColorFocusBorder.Builder(getContext())
                .borderWidthRes(R.dimen.focus_border_width)
                .borderRadiusRes(R.dimen.focus_border_radius)
                .borderColorRes(R.color.colorFocusBorder)
                .paddingRes(R.dimen.focus_border_padding)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .build(this);
    }

    public void setFocusBorderBuilder(AbsFocusBorder.Builder focusBorderBuilder) {
        mFocusBorder = focusBorderBuilder.build(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setClip(this);
        startListenFocusChange();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopListenFocusChange();
    }

    //设置clip属性，保证焦点View放大之后不被父View裁剪. ps:只迭代了父ViewGroup设置了属性，子ViewGroup需要自己设置
    private void setClip(ViewGroup viewGroup) {
        viewGroup.setClipToPadding(false);
        viewGroup.setClipChildren(false);
        if (viewGroup.getParent() instanceof ViewGroup) {
            setClip((ViewGroup) viewGroup.getParent());
        }
    }

    private void startListenFocusChange() {
        if (mFocusBorder != null) {
            Log.d(TAG, "startListenFocusChange global focus:" + this.hashCode());
            mFocusBorder.bindGlobalFocusListener();
        }
    }

    private void stopListenFocusChange() {
        if (mFocusBorder != null) {
            Log.d(TAG, "stopListenFocusChange global focus:" + this.hashCode());
            mFocusBorder.unBindGlobalFocusListener();
        }
    }
}
