package com.francisco.lsmproyect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ViewPagerAdapterWalkthrough extends PagerAdapter {

    Context context;
    int images[] = {
            R.drawable.undraw_completed,
            R.drawable.undraw_develop,
            R.drawable.undraw_editable
    };

    int headings[] = {
            R.string.heading_welcome,
            R.string.heading_courses,
            R.string.heading_profile
    };

    int descriptions[] = {
            R.string.desc_welcome,
            R.string.desc_courses,
            R.string.desc_profile
    };

    public ViewPagerAdapterWalkthrough(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_layout, container, false);

        ImageView slideTitleImage = (ImageView) view.findViewById(R.id.imgTitleImage);
        TextView slideHeading = (TextView) view.findViewById(R.id.tvTitle);
        TextView slideDescription = (TextView) view.findViewById(R.id.tvDescription);

        slideTitleImage.setImageResource(images[position]);
        slideHeading.setText(headings[position]);
        slideDescription.setText(descriptions[position]);

        container.addView(view);

        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }
}
