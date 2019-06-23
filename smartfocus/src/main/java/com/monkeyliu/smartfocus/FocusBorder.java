package com.monkeyliu.smartfocus;

import android.view.View;

public interface FocusBorder {

    void onFocus(View focusView);
    
    void bindGlobalFocusListener();
    
    void unBindGlobalFocusListener();
}
