package com.hi.appskin_v40.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.hi.appskin_v40.R;
import com.hi.appskin_v40.utils.GradientHelper;

public class ImagePagerAdapter extends PagerAdapter {
    private Context context;
    private int[] ids;

    public ImagePagerAdapter(Context context, int[] ids) {
        this.context = context;
        this.ids = ids;
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_viewpager, null);
        ImageView imageView = view.findViewById(R.id.image);
        imageView.setImageDrawable(GradientHelper.generateFundsTopGradient(
                context,
                context.getResources(),
                ids[position]));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() { return ids == null ? 0 : ids.length; }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }
}
