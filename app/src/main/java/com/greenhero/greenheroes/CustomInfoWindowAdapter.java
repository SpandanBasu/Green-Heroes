package com.greenhero.greenheroes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContext;
    public static CircleImageView TreeImg;


    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow= LayoutInflater.from(context).inflate(R.layout.marker_info_window,null);
    }

    private void setWindowText(Marker marker,View view) {
        TextView TreeName = (TextView) view.findViewById(R.id.tree_name);
        TreeImg=(CircleImageView)view.findViewById(R.id.plant_image) ;
        TextView UserName = (TextView) view.findViewById(R.id.username);
        TextView Date = (TextView) view.findViewById(R.id.date);
        TextView Time = (TextView) view.findViewById(R.id.time);



        String title = marker.getTitle();
        String[] parts = marker.getSnippet().split(",");
        String date = parts[0];
        String time = parts[1];
        String userName = parts[2];
        String imgUrl = parts[3];
        String userId = parts[4];
        String emailId= parts[5];


        TreeName.setText(title);
        UserName.setText(userName);
        Date.setText(date);
        Time.setText(time);

        Picasso.get()
                .load(imgUrl)
                .placeholder(R.drawable.tree)
                .error(R.drawable.take_out_tree).resize(50, 50)
                .into(TreeImg, new MarkerCallback(marker));
    }

    public class MarkerCallback implements Callback {
        Marker marker=null;

        MarkerCallback(Marker marker) {
            this.marker=marker;
        }


        @Override
        public void onSuccess() {
            if (marker != null && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }

        @Override
        public void onError(Exception e) {
            Log.e(getClass().getSimpleName(), "Error loading thumbnail!");
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        setWindowText(marker,mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        setWindowText(marker,mWindow);
        return mWindow;
    }


}
