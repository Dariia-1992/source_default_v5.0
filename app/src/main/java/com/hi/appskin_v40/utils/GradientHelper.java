package com.hi.appskin_v40.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.hi.appskin_v40.R;

public class GradientHelper {

    public static Drawable generateFundsTopGradient(Context context, Resources resources, @DrawableRes int imageId) {
        Drawable imageDrawable = ContextCompat.getDrawable(context, imageId);

        int[] shadowColors = resources.getIntArray(R.array.shadow_gradient);
        GradientDrawable shadowDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, shadowColors);
        shadowDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shadowDrawable.setShape(GradientDrawable.RECTANGLE);

        return new LayerDrawable(new Drawable[] { imageDrawable, shadowDrawable });
    }

    public static Drawable getGradient(Resources resources) {
        int[] shadowColors = resources.getIntArray(R.array.shadow_gradient);
        GradientDrawable shadowDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, shadowColors);
        shadowDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shadowDrawable.setShape(GradientDrawable.RECTANGLE);

        return shadowDrawable;
    }
}
