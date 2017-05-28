package com.rahul.shrofiledemo.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by rahul on 11-03-2017.
 */

public class CustomTypefaceTextView extends android.support.v7.widget.AppCompatTextView{
    public CustomTypefaceTextView(Context context) {
        super(context);
        init();
    }

    public CustomTypefaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(),"fonts/COLONNA.TTF");
        setTypeface(typeface,Typeface.BOLD);
    }
}
