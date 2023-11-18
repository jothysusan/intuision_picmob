package com.picmob.android.utils;

import android.content.Context;
import android.graphics.Typeface;


public class General {
    private final Context context;

    public General(Context mContext) {
        this.context = mContext;
    }

   /* public static void statusbarColor(@NonNull Activity context2) {
        Window window = context2.getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.clearFlags(67108864);
        window.setStatusBarColor(ContextCompat.getColor(context2, R.color.colorPrimaryDark));
    }*/

    public Typeface mediumtypeface() {
        return Typeface.createFromAsset(this.context.getAssets(), "fonts/medium.ttf");
    }

    public Typeface italicTypeFace() {
        return Typeface.createFromAsset(this.context.getAssets(), "fonts/italic.ttf");
    }

    public Typeface regularTypeFace() {
        return Typeface.createFromAsset(this.context.getAssets(), "fonts/regular.ttf");
    }

    public Typeface boldTypeFace() {
        return Typeface.createFromAsset(this.context.getAssets(), "fonts/bold.ttf");
    }



}
