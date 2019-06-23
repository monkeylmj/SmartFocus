package com.monkeyliu.smartfocus;

import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * @author baron
 * @bried
 * @date 2017/6/26 上午11:40 create
 * Created by baron on 2017/6/26.
 */

public class FocusHelper {

    private static final float FOCUSE_START_SCALE = 1.0f;
    private static final float FOCUSE_END_SCALE = 0.95f;
    private static final long FOCUSE_ANIMATION_DURATION = 60;
    private static long sLastHandledKeyTime = 0;
    private FocusHelper(){}

    public static boolean handleIconKeyEvent(View v, int keyCode, KeyEvent e, boolean isNeedHandleClick) {
        final int action = e.getAction();
        boolean wasHandled = false;
        if (SystemClock.elapsedRealtime() - sLastHandledKeyTime < 300) {
            sLastHandledKeyTime = SystemClock.elapsedRealtime();
            return wasHandled;
        }
        final boolean handleKeyEvent = action != KeyEvent.ACTION_UP;
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (handleKeyEvent) {
                    sLastHandledKeyTime = SystemClock.elapsedRealtime();
                    onFocusClick(v, isNeedHandleClick);
                }
                wasHandled = true;
                break;
            default:
                break;
        }
        return wasHandled;
    }

    public static void onFocusClick(final View view, final boolean isNeedHandleClick) {
        int width = view.getWidth();
        int height = view.getHeight();
        final ScaleAnimation downAnimation = new ScaleAnimation(FOCUSE_START_SCALE, FOCUSE_END_SCALE,
                FOCUSE_START_SCALE, FOCUSE_END_SCALE, width/2f, height/2f);
        downAnimation.setDuration(FOCUSE_ANIMATION_DURATION);
        downAnimation.setFillAfter(true);

        final ScaleAnimation upAnimation = new ScaleAnimation(FOCUSE_END_SCALE, FOCUSE_START_SCALE,
                FOCUSE_END_SCALE, FOCUSE_START_SCALE, width/2f, height/2f);
        upAnimation.setDuration(FOCUSE_ANIMATION_DURATION);
        upAnimation.setFillAfter(true);
        upAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // no need to do anything now
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isNeedHandleClick) {
                    view.performClick();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // no need to do anything now
            }
        });
        downAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // no need to do anything now
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(upAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // no need to do anything now
            }
        });
        view.startAnimation(downAnimation);
    }
}
