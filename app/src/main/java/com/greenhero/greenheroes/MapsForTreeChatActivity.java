package com.greenhero.greenheroes;

import android.annotation.SuppressLint;
import android.app.Dialog;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;


import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


import static com.greenhero.greenheroes.Adapter.tree_latitude;
import static com.greenhero.greenheroes.Adapter.tree_longitude;
import static com.greenhero.greenheroes.MainActivity.latitude;
import static com.greenhero.greenheroes.MainActivity.longitude;


public class MapsForTreeChatActivity extends Fragment {

    Toolbar toolbar;
    MenuItem menuItem;
    Dialog plantTreeDialog;
    Dialog markerDialog;
    Button okBtnCapture;
    ImageView closeDialog;
    ImageView closeDialogCapture;
    private CircleImageView plantImage;
    EditText plantName;
    private String my_userId;
    public static String otherUserId;
    Button completeBtn;
    private LatLng currentLocation;
    public static FloatingActionButton fab_scan;
    public static FloatingActionButton fab_plant;
    private int REQUEST_CODE = 1;
    public static ImageView image_frame;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private String userId,userName,emailId;
    private TextView usernameTextView,treenameTextView,date,time,emailTextView;

    View view;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {


        @SuppressLint("MissingPermission")
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mapAPI = googleMap;
            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                boolean success = mapAPI.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getContext(), R.raw.map_style));

                if (!success) {
                    Log.e("TAG", "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e("TAG", "Can't find style. Error: ", e);
            }
            currentLocation = new LatLng(tree_latitude, tree_longitude);
            mapAPI.setMyLocationEnabled(true);
//            Circle circle = mapAPI.addCircle(new CircleOptions()
//                    .center(currentLocation)
//                    .radius(50)
//                    .strokeWidth(3f)
//                    .strokeColor(Color.argb(90, 72, 203, 255))
//                    .fillColor(Color.argb(55, 72, 203, 255)));
            if(latitude!=0 && longitude!=0)
            {
                CameraPosition cameraPosition = new CameraPosition.Builder().
                        target(currentLocation).
                        tilt(60).
                        zoom(16).
                        bearing(0).
                        build();

                mapAPI.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


//                mapAPI.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
//                mapAPI.animateCamera(CameraUpdateFactory.zoomIn());
//                mapAPI.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);


            }
            else
            {
                mapAPI.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 2));
            }


            getTrees(mapAPI);
            mapAPI.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {


                    markerDialog = new Dialog(getContext());
                    markerDialog.setContentView(R.layout.map_marker_popup);
                    usernameTextView =(TextView) markerDialog.findViewById(R.id.user_name_popup);
                    treenameTextView =(TextView) markerDialog.findViewById(R.id.tree_name_popup);
                    date =(TextView) markerDialog.findViewById(R.id.date_popup);
                    time =(TextView) markerDialog.findViewById(R.id.time_popup);
                    emailTextView =(TextView) markerDialog.findViewById(R.id.email_popup);
                    closeDialogCapture = (ImageView) markerDialog.findViewById(R.id.close_dialog_marker_popup);
                    okBtnCapture = (Button) markerDialog.findViewById(R.id.check_profile_btn);
                    plantImage=(CircleImageView)markerDialog.findViewById(R.id.plant_image);

                    String[] parts = marker.getSnippet().split(",");
                    String mdate = parts[0];
                    String mtime = parts[1];
                    String userName = parts[2];
                    String imgUrl = parts[3];
                    String userId = parts[4];
                    String emailId= parts[5];

                    treenameTextView.setText(marker.getTitle());
                    usernameTextView.setText(userName);
                    time.setText(mtime);
                    date.setText(mdate);
                    otherUserId=userId;
                    if(otherUserId.equals(my_userId))
                    {
                        okBtnCapture.setVisibility(View.GONE);
                    }
                    emailTextView.setText(emailId);
                    Picasso.get()
                            .load(imgUrl)
                            .placeholder(R.drawable.tree)
                            .error(R.drawable.tree)
                            .into(plantImage);


                    closeDialogCapture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            markerDialog.dismiss();
                        }
                    });
                    okBtnCapture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            markerDialog.dismiss();

                            try {
                                MainActivity.fragmentManager.popBackStack();
                            }catch (IllegalStateException ignored){

                            }
                            MainActivity.fragmentManager.beginTransaction().replace(R.id.frame_container,new OtherUserProfile()).addToBackStack(null).commit();
                        }
                    });
                    markerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


                    markerDialog.show();
                    return true;
                }
            });

        }
    };




    GoogleMap mapAPI;

    public void getTrees(final GoogleMap mapAPI)
    {
        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try{


                    for(DataSnapshot snapshot: dataSnapshot.getChildren())
                    {
                        if(snapshot.child("totalTrees").getValue(Integer.class)>0)
                        {
                            userId=snapshot.child("userId").getValue(String.class);
                            userName=snapshot.child("userName").getValue(String.class);
                            emailId=snapshot.child("emailId").getValue(String.class);

                            DataSnapshot allTrees = snapshot.child("Trees");
                            for(DataSnapshot tree: allTrees.getChildren())
                            {
                                Tree newTree = new Tree(userId,userName,emailId,tree.child("treeName").getValue(String.class),tree.child("date").getValue(String.class),tree.child("time").getValue(String.class),tree.child("imageurl").getValue(String.class),tree.child("latitude").getValue(Double.class),tree.child("longitude").getValue(Double.class));
                                LatLng treeLocation =new LatLng(newTree.getlat(),newTree.getlng());
                                String snippet= newTree.getDate()+","+newTree.getTime()+","+newTree.getUserName()+","+newTree.getImgUrl()+","+newTree.getUserId()+","+newTree.getEmailId();


                                Marker marker =  mapAPI.addMarker(new MarkerOptions()
                                        .position(treeLocation)
                                        .title(newTree.getTreeName())
                                        .snippet(snippet)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tree_marker_logo))
                                );

                                //mapAPI.setInfoWindowAdapter(new CustomInfoWindowAdapter(getContext()));

                            }
                        }
                    }
                }
                catch (NullPointerException e)
                {
                    e.printStackTrace();
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    public MapsForTreeChatActivity() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        try {
            my_userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }
        View rootview = inflater.inflate(R.layout.fragment_maps, container, false);
        MainActivity.navigationView.getMenu().getItem(2).setChecked(true);
        MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            toolbar = (Toolbar)getActivity().findViewById(R.id.toolbar);
            //view= (toolbar).findViewById(R.id.refresh).getRootView();
            //menuItem = toolbar.getMenu().getItem(1);
        }

        if(latitude==0 && longitude==0)
        {

            Toast.makeText(getContext(), "Latitude is zero", Toast.LENGTH_SHORT).show();

            MenuItem menuItem = MainActivity.toolbar.getMenu().findItem(R.id.refresh); // get the menu item

            try {

                TapTargetView.showFor(getActivity(),                 // `this` is an Activity
                        TapTarget.forToolbarMenuItem(MainActivity.toolbar, R.id.refresh, "Load Map", "Location is not accessed correctly, So, Please refresh the Map")

                                // All options below are optional
                                .outerCircleColor(R.color.colorDeepGreen)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                .targetCircleColor(R.color.white)   // Specify a color for the target circle
                                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.white)      // Specify the color of the title text
                                .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                .descriptionTextColor(R.color.light_green3)  // Specify the color of the description text
                                .textColor(R.color.white)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                .icon(getResources().getDrawable(R.drawable.ic_refresh))                     // Specify a custom drawable to draw as the target
                                .targetRadius(60),                  // Specify the target radius (in dp)
                        new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                            @Override
                            public void onTargetClick(TapTargetView view) {

                                super.onTargetClick(view);      // This call is optional
                                Toast.makeText(getContext(), "Map Loaded Successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("frgToLoad", 3);
                                startActivity(intent);

                            }

                            @Override
                            public void onOuterCircleClick(TapTargetView view) {
                                super.onOuterCircleClick(view);
                                Toast.makeText(getContext(), "Outside Clicked", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("frgToLoad", 3);
                                startActivity(intent);
                            }
                        });
            }catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
        }

        MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        fab_scan = (FloatingActionButton) rootview.findViewById(R.id.fab_scan);
        fab_plant = (FloatingActionButton) rootview.findViewById(R.id.fab_plant_tree);

        fab_plant.setVisibility(View.INVISIBLE);
        fab_scan.setVisibility(View.INVISIBLE);



        return rootview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }




}






