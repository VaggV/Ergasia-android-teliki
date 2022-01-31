package com.ergasia_android_teliki;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class Variables {
    LinearLayout.LayoutParams btnLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    LinearLayout.LayoutParams imgLayoutParams = new LinearLayout.LayoutParams(650, 650);
    GradientDrawable shape = new GradientDrawable();

    public LinearLayout.LayoutParams getBtnLayoutParams() {
        btnLayoutParams.bottomMargin = 40;
        btnLayoutParams.topMargin = 10;

        return btnLayoutParams;
    }

    public LinearLayout.LayoutParams getImgLayoutParams() {
        imgLayoutParams.topMargin = 20;

        return imgLayoutParams;
    }

    public GradientDrawable getShape() {
        shape.setCornerRadius(100);
        shape.setColor(Color.rgb(103, 80, 164));

        return shape;
    }
}
