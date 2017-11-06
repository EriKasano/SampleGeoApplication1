package com.sunnymoon.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;
import com.sunnymoon.samplegeoapplication1.R;

/**
 * Project SampleGeoApplication1
 * Working on AttrRetrieval
 * Created by Shion T. Fujie on 2017/11/04.
 */
public class AttrRetrieval {
    private AttrRetrieval(){}

    public static int fetchAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    public static int fetchPrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimary });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
}
