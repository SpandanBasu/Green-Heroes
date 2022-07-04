package com.greenhero.greenheroes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.greenhero.greenheroes.MapsFragment.otherUserId;

public class OtherUserTrees extends Fragment {

    private String user_id = FirebaseAuth.getInstance().getUid();
    private SwipeRefreshLayout refresh;
    RecyclerView dataList;
    List<String> titles;
    List<String> images;
    List<String> date;
    List<String> time;
    List<Double> lat;
    List<Double> lon;
    Adapter adapter;
    GridLayoutManager gridLayoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_my_trees, container, false);
        setHasOptionsMenu(true);

        dataList=rootview.findViewById(R.id.datalist);
        date = new ArrayList<>();
        time = new ArrayList<>();
        titles = new ArrayList<>();
        images = new ArrayList<>();
        lat = new ArrayList<>();
        lon = new ArrayList<>();
        refresh= (SwipeRefreshLayout)rootview.findViewById(R.id.refresh_my_trees) ;

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh.setRefreshing(false);
            }
        });

        dataQuery();

        return rootview;
    }

    public void dataQuery()
    {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(otherUserId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                titles.clear();
                images.clear();
                date.clear();
                time.clear();
                lat.clear();
                lon.clear();
                if(dataSnapshot.child("totalTrees").getValue(Integer.class)>0)
                {

                    //DataSnapshot treeSnapshot = dataSnapshot.child("Trees");
                    for(DataSnapshot snapshot: dataSnapshot.child("Trees").getChildren())
                    {

                        String titleTemp = snapshot.child("treeName").getValue(String.class);
                        titles.add(titleTemp);
                        images.add(snapshot.child("imageurl").getValue(String.class));
                        date.add(snapshot.child("date").getValue(String.class));
                        time.add(snapshot.child("time").getValue(String.class));
                        lat.add(snapshot.child("latitude").getValue(Double.class));
                        lon.add(snapshot.child("longitude").getValue(Double.class));

                    }
                    Log.e("Tag",titles.toString());
                    try {
                        adapter = new Adapter(getContext(), titles, images, date, time, lat, lon, user_id);
                    }catch (NullPointerException e)
                    {
                        e.printStackTrace();
                    }
                    gridLayoutManager = new GridLayoutManager(getContext(),2,GridLayoutManager.VERTICAL,false);
                    dataList.setLayoutManager(gridLayoutManager);
                    dataList.setAdapter(adapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.refresh);
        if(item!=null)
            item.setVisible(false);
    }
}
