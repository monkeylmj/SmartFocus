package com.monkeyliu.smartfocusdemo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.monkeyliu.smartfocus.AutoFocusFrameLayout;
import com.monkeyliu.smartfocus.ColorFocusBorder;

/**
 * @author monkey_liu
 * @date 2019-06-23
 */
public class FocusDemoActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);

        //自定义焦点框的效果
        AutoFocusFrameLayout autoFocusFrameLayout = findViewById(R.id.focus_framelayout);
        autoFocusFrameLayout.setFocusBorderBuilder(new ColorFocusBorder.Builder(this)
                .borderWidth(4) //border宽度
                .borderColor(Color.RED) //border颜色
                .borderRadius(6) //border圆角半径
                .shadowWidth(20) //shadow半径
                .shadowColor(Color.BLUE) //shadow颜色
                .padding(3) //内边距
                .scaleX(1.1f) //X方向缩放倍数
                .scaleY(1.1f) //Y方向缩放倍数
                .enableShimmer()); //使用闪光特效
    }
}
