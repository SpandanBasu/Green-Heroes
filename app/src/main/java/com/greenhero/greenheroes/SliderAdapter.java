package com.greenhero.greenheroes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.airbnb.lottie.LottieAnimationView;

public class SliderAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;


    public SliderAdapter(Context context) {
        mContext = context;
    }

    public int[] slideImages={
            R.drawable.dig_hole,
            R.drawable.take_out_tree,
            R.drawable.planting,
            R.drawable.take_out_tree,
            R.drawable.camera_circle
    };

    public String[] slideTitles={
            "Dig a Hole",
            "Remove Seedling",
            "Gently Nestle it",
            "Water it",
            "Capture Picture"
    };

    public String[] slideTips={
            "Tip: Choose a place with good wind protection",
            "Tip: Gently loosen the soil to help the roots spread out",
            "Tip: Make sure it's centered and upright",
            "Tip: Always water it as much required, more than necessary can be harmful",
            "Tip: The Image will be verified, Please be specific"
    };

    public String[] slideDescriptions={
            "Dig a hole 3 inches deeper than the length of the roots. Depth is more important than width in most cases, so be sure to dig properly",
            "Carefully remove the seedling from it's container. Exercise caution, because if you’re too rough with the roots, you’ll increase the risk of the tree going into transplant shock.",
            "Gently nestle it into the hole and backfill, Compressing as you go. Make sure the trunk flare remains partially aboveground.",
            "Water the plant in. It provides an important first drink if the soil is otherwise dry, and provides deeper moisture even if it is already damp.",
            "Capture a picture of your planted tree. To make the final arrangements for the whole World to see your tree, Take a clear shot."

    };

    @Override
    public int getCount() {
        return slideTitles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==(RelativeLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        mLayoutInflater=(LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view= mLayoutInflater.inflate(R.layout.slider_layout,container,false);

        ImageView slideImageView= (ImageView)view.findViewById(R.id.slide_img);
        TextView slideTitle= (TextView)view.findViewById(R.id.slide_title);
        TextView slideTip= (TextView)view.findViewById(R.id.slide_tips);
        TextView slideDescription= (TextView)view.findViewById(R.id.slide_small_desc);
        LottieAnimationView waterAnimation= (LottieAnimationView)view.findViewById(R.id.water_animation);
        if(position!=3)
        {
            slideImageView.setVisibility(View.VISIBLE);
            slideImageView.setImageResource(slideImages[position]);
        }
        else
        {
            slideImageView.setVisibility(View.INVISIBLE);
            waterAnimation.setVisibility(View.VISIBLE);
        }
        slideTitle.setText(slideTitles[position]);
        slideTip.setText(slideTips[position]);
        slideDescription.setText(slideDescriptions[position]);


        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((RelativeLayout)object);
    }
}
