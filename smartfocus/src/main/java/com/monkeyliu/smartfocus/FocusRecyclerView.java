package com.monkeyliu.smartfocus;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by monkey on 2017/9/22.
 * {@link AutoFocusFrameLayout}下使用此RecyclerView保证滚动时焦点正常。
 */

public class FocusRecyclerView extends RecyclerView {

    private Point mScrollPoint = new Point();

    public FocusRecyclerView(Context context) {
        super(context);
        init();
    }

    public FocusRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        setFocusable(false);
        setFocusableInTouchMode(false);
    }

    @Override
    public void smoothScrollBy(int dx, int dy) {
        setScrollValue(dx, dy);
        super.smoothScrollBy(dx, dy);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == SCROLL_STATE_IDLE) {
            setScrollValue(0, 0);
        }
        super.onScrollStateChanged(state);
    }

    private void setScrollValue(int x, int y) {
        mScrollPoint.set(x, y);
    }

    public Point getScrollValue() {
        return mScrollPoint;
    }
}
