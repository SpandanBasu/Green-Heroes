package com.greenhero.greenheroes;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

import static com.greenhero.greenheroes.MainActivity.latitude;
import static com.greenhero.greenheroes.MainActivity.longitude;
import static com.greenhero.greenheroes.MainActivity.treePlanted;

public class ScanFragment extends Fragment {


    FirebaseAuth mAuth= FirebaseAuth.getInstance();
    private int REQUEST_CODE= 1;
    private int aqi=0;
    private TextView address;
    private TextView condition;
    private LinearLayout topContainer;
    private LinearLayout Container;
    private LottieAnimationView scan;
    private int reqTrees=2;
    private TextView co;
    private TextView no2;
    private TextView so2;
    private TextView o3;
    private TextView pm10;
    private TextView pm25;
    private String user_id = mAuth.getCurrentUser().getUid();
    public static double coData;
    public static double no2Data;
    public static double so2Data;
    public static double o3Data;
    public static double pm10Data;
    public static double pm25Data;
    private TextView scanEnv;
    AQIAsyncTask task;
    private static final String AQI_URL="https://api.waqi.info/feed/geo:"+latitude+";"+longitude+"/?token=1f1d110d1b1e0e8f388c2f89800ba7e5a02c8d8f";
    private TextView req_tree_num;
    private int area_radius;
    private FloatingActionButton fab_plant;
    private Spinner mradInSpinner;
    AdView adViewTop;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootview= inflater.inflate(R.layout.scan_fragment,container,false);
        MainActivity.navigationView.getMenu().getItem(1).setChecked(true);
        setHasOptionsMenu(true);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                        adViewTop =rootview.findViewById(R.id.adView_scan_top);

                        AdRequest adRequest = new AdRequest.Builder().build();
                        adViewTop.loadAd(adRequest);

                    }
                });

            }
        }, 1000);

        Handler handlerNetSlow = new Handler();
        handlerNetSlow.postDelayed(new Runnable() {
            public void run() {
               if(scan.getVisibility()==View.VISIBLE)
               {
                   AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                   builder1.setMessage("Its taking longer than usual to load the data, you can wait or recheck your internet connection!");
                   builder1.setCancelable(true);
                   builder1.setPositiveButton(
                           "Ok",
                           new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   dialog.cancel();
                               }
                           });

                   AlertDialog alert11 = builder1.create();
                   alert11.show();
               }
            }
        }, 10000);

        scanEnv = (TextView) rootview.findViewById(R.id.scan_text) ;
        AQIAsyncTask task= new AQIAsyncTask();
        task.execute(AQI_URL);

        co= (TextView)rootview.findViewById(R.id.co) ;
        no2= (TextView)rootview.findViewById(R.id.no2);
        so2= (TextView)rootview.findViewById(R.id.so2);
        o3= (TextView)rootview.findViewById(R.id.o3);
        pm10= (TextView)rootview.findViewById(R.id.pm10);
        pm25= (TextView)rootview.findViewById(R.id.pm25);
        req_tree_num= (TextView)rootview.findViewById(R.id.req_tree_num) ;
        scan= (LottieAnimationView) rootview.findViewById(R.id.progress_bar_scan) ;
        topContainer= (LinearLayout)rootview.findViewById(R.id.top_container) ;
        Container= (LinearLayout)rootview.findViewById(R.id.container) ;
        condition =(TextView)rootview.findViewById(R.id.condition);
        address = (TextView)rootview.findViewById(R.id.address) ;
        address.setText(MainActivity.addressLine);
        mradInSpinner = (Spinner)rootview.findViewById(R.id.radius_input) ;
        setupSpinner();
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)rootview.findViewById(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AQIAsyncTask task= new AQIAsyncTask();
                task.execute(AQI_URL);
                swipeRefreshLayout.setRefreshing(false);
            }
        });



        fab_plant= (FloatingActionButton)rootview.findViewById(R.id.fab_plant_tree);

        fab_plant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.fragmentManager.beginTransaction().replace(R.id.frame_container,new PlantTreeSlidesFragment()).addToBackStack(null).commit();
            }
        });



        return rootview;
    }

    private void setupSpinner() {
        ArrayAdapter radiusSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.radius_array, R.layout.layout_spinner);

        radiusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mradInSpinner.setAdapter(radiusSpinnerAdapter);

        mradInSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.distance_100m))) {
                        area_radius = 100;
                        AQIAsyncTask task= new AQIAsyncTask();
                        task.execute(AQI_URL);
                    }
                    else if (selection.equals(getString(R.string.distance_250m))) {
                        area_radius = 250;
                        AQIAsyncTask task= new AQIAsyncTask();
                        task.execute(AQI_URL);
                    }
                    else if (selection.equals(getString(R.string.distance_500m))) {
                        area_radius = 500;
                        AQIAsyncTask task= new AQIAsyncTask();
                        task.execute(AQI_URL);
                    }
                    else if (selection.equals(getString(R.string.distance_750m))) {
                        area_radius = 750;
                        AQIAsyncTask task= new AQIAsyncTask();
                        task.execute(AQI_URL);
                    }else if (selection.equals(getString(R.string.distance_1km))) {
                        area_radius = 1000;
                        AQIAsyncTask task= new AQIAsyncTask();
                        task.execute(AQI_URL);
                    }
                    else if (selection.equals(getString(R.string.distance_5km))) {
                        area_radius = 5000;
                        AQIAsyncTask task= new AQIAsyncTask();
                        task.execute(AQI_URL);
                    }
                    else if (selection.equals(getString(R.string.distance_10km))) {
                        area_radius = 10000;
                        AQIAsyncTask task= new AQIAsyncTask();
                        task.execute(AQI_URL);
                    }
                    else if (selection.equals(getString(R.string.distance_25km))) {
                        area_radius = 25000;
                        AQIAsyncTask task= new AQIAsyncTask();
                        task.execute(AQI_URL);
                    }

                    else {
                        area_radius = 100;
                        AQIAsyncTask task= new AQIAsyncTask();
                        task.execute(AQI_URL);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                area_radius = 100;
                AQIAsyncTask task= new AQIAsyncTask();
                task.execute(AQI_URL);
            }
        });

    }


   private class AQIAsyncTask extends AsyncTask<String, Void, Integer>
   {

       @Override
       protected Integer doInBackground(String... urls) {
           Integer result = null;
           try {
               result = AQI_Query.getAqi(urls[0]);
               AQI_Query.getPollutants(urls[0]);
           } catch (IOException e) {
               e.printStackTrace();
           }
           aqi= result;


           return result;

       }

       @Override
       protected void onPostExecute(Integer result) {

           scan.setVisibility(View.GONE);
           Container.setVisibility(View.VISIBLE);
           scanEnv.setText(""+result);
           if(result<=50)
           {
               condition.setText("Good");
               MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#008a00")));
               fab_plant.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#008a00")));
               MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#008a00")));
               if(getContext()!=null)
               topContainer.setBackgroundColor(getResources().getColor(R.color.colorDeepGreen));
               reqTrees =(int) (area_radius/100*2) - treePlanted;

           }
           else if(result>50 && result<=100)
           {
               condition.setText("Moderate");
               MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#CD9A00")));
               fab_plant.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CD9A00")));
               MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#CD9A00")));
               if(getContext()!=null)
               topContainer.setBackgroundColor(getResources().getColor(R.color.yellow));
               reqTrees =(int) (area_radius/100*3) - treePlanted;

           }
           else if(result>100 && result<=150)
           {
               condition.setText("Unhealthy for Sensitive Groups");
               MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#FF521B")));
               fab_plant.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF521B")));
               MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#FF521B")));
               if(getContext()!=null)
               topContainer.setBackgroundColor(getResources().getColor(R.color.orange));
               reqTrees =(int) (area_radius/100*4) - treePlanted;
               //Toast.makeText(getContext(),"!!!  "+treePlanted+"  !!!",Toast.LENGTH_LONG).show();
           }
           else if(result>150 && result<=200)
           {
               condition.setText("Unhealthy");
               MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#BC0000")));
               fab_plant.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BC0000")));
               MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#BC0000")));
               if(getContext()!=null)
               {
                   topContainer.setBackgroundColor(getResources().getColor(R.color.red));
               }

               reqTrees =(int) (area_radius/100*6) - treePlanted;

           }
           else if(result>200 && result<=300)
           {
               condition.setText("Very Unhealthy");
               MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#620097")));
               fab_plant.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#620097")));
               MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#620097")));
               if(getContext()!=null)
               topContainer.setBackgroundColor(getResources().getColor(R.color.purple));
               reqTrees =(int) (area_radius/100*8) - treePlanted;

           }
           else if(result>300)
           {
               condition.setText("Hazardous");
               MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#391111")));
               fab_plant.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#391111")));
               MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#391111")));
               if(getContext()!=null)
               topContainer.setBackgroundColor(getResources().getColor(R.color.brown));
               reqTrees =(int) (area_radius/100*10) - treePlanted;
           }



           if(reqTrees<=0)
           {
               switch(area_radius)
               {
                   case 100:
                       mradInSpinner.setSelection(1);
                       task= new AQIAsyncTask();
                       task.execute(AQI_URL);
                       break;

                   case 250:
                       mradInSpinner.setSelection(2);
                       task= new AQIAsyncTask();
                       task.execute(AQI_URL);
                       break;

                   case 500:
                       mradInSpinner.setSelection(3);
                       task= new AQIAsyncTask();
                       task.execute(AQI_URL);
                       break;

                   case 750:
                       mradInSpinner.setSelection(4);
                       task= new AQIAsyncTask();
                       task.execute(AQI_URL);
                       break;

                   case 1000:
                       mradInSpinner.setSelection(5);
                       task= new AQIAsyncTask();
                       task.execute(AQI_URL);
                       break;

                   case 5000:
                       mradInSpinner.setSelection(6);
                       task= new AQIAsyncTask();
                       task.execute(AQI_URL);
                       break;

                   case 10000:
                       mradInSpinner.setSelection(7);
                       task= new AQIAsyncTask();
                       task.execute(AQI_URL);
                       break;

                   case 25000:
                       area_radius=50000;
                       task= new AQIAsyncTask();
                       task.execute(AQI_URL);
                       break;

               }
           }

           if(reqTrees<0)
           {
               reqTrees=0;
           }

           req_tree_num.setText(""+reqTrees);

           co.setText(""+coData);
           no2.setText(""+no2Data);
           so2.setText(""+so2Data);
           o3.setText(""+o3Data);
           pm10.setText(""+pm10Data);
           pm25.setText(""+pm25Data);

       }
   }



    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.refresh);
        if(item!=null)
            item.setVisible(false);
    }

}
