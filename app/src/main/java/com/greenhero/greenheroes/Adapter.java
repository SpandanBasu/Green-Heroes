package com.greenhero.greenheroes;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.greenhero.greenheroes.ChatMessageActivity.fragmentManagerChatActivity;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    public static double tree_latitude,tree_longitude;
    FirebaseUser fuser= FirebaseAuth.getInstance().getCurrentUser();
    String myUserId= fuser.getUid();
    List<String> mtitles;
    List<String> mimages;
    List<String> mdate;
    List<String> mtime;
    List<Double> mlat;
    List<Double> mlon;
    String theUserId;
    LayoutInflater minflater;
    StorageReference photoRef;
    Context mctx;
    Uri uri;
    FirebaseStorage mFirebaseStorage=FirebaseStorage.getInstance();

    public Adapter(Context ctx, List<String>titles, List<String>images, List<String>date, List<String>time,List<Double> lat, List<Double> lon, String userId)
    {
        try {
            mtitles = titles;
            mimages = images;
            mdate = date;
            mtime = time;
            mctx = ctx;
            mlat=lat;
            mlon=lon;
            minflater = LayoutInflater.from(mctx);
            theUserId = userId;

        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= minflater.inflate(R.layout.gridview_style,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if(!theUserId.equals(myUserId))
        {
            holder.delete_btn.setVisibility(View.GONE);
        }
        holder.treeName.setText(mtitles.get(position));
        holder.date.setText(mdate.get(position));
        holder.time.setText(mtime.get(position));
        Picasso.get()
                .load(mimages.get(position))
                .placeholder(R.drawable.tree)
                .error(R.drawable.take_out_tree).resize(50, 50)
                .into(holder.treeImg);
        holder.delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mctx)
                        .setTitle("Delete Tree")
                        .setMessage("Are you sure you want to delete this Tree?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation

                                try{
                                    photoRef = mFirebaseStorage.getReferenceFromUrl(mimages.get(position));
                                }catch(IllegalArgumentException e){
                                    e.printStackTrace();
                                }
                                try {
                                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // File deleted successfully
                                            Log.d("TAG", "onSuccess: deleted file");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Uh-oh, an error occurred!
                                            Log.d("TAG", "onFailure: did not delete file");
                                        }
                                    });
                                }
                                catch (NullPointerException e)
                                {
                                    e.printStackTrace();
                                }
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(myUserId).child("Trees").child(mdate.get(position)+" "+mtime.get(position));
                                ref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(mctx, "Tree removed", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                final DatabaseReference totalTreeRef= FirebaseDatabase.getInstance().getReference("Users").child(myUserId);
                                totalTreeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("totalTrees"))
                                        {
                                            int totalTree = snapshot.child("totalTrees").getValue(Integer.class);
                                            totalTree--;
                                            totalTreeRef.child("totalTrees").setValue(totalTree);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(R.drawable.ic_report)
                        .show();
            }
        });

        holder.treeGridBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tree_latitude = mlat.get(position);
                    tree_longitude = mlon.get(position);
                }catch(NullPointerException e)
                {
                    e.printStackTrace();
                }
                Log.e("TAG", "Location of tree!!!!!!!!!!!!!!!!!!! latitude is "+ mctx.getClass().getSimpleName());



                if(mctx.getClass().getSimpleName().equals("MainActivity")) {
                    MainActivity.fragmentManager.popBackStack();
                    MainActivity.fragmentManager.beginTransaction().replace(R.id.frame_container, new MapsForTree()).commit();
                }
                else if(mctx.getClass().getSimpleName().equals("ChatMessageActivity"))
                {
                    Log.e("TAG", "onClick: Trying to open intent!!!!!!" );
                    fragmentManagerChatActivity.beginTransaction().replace(R.id.frame_chat_profile, new MapsForTreeChatActivity()).addToBackStack(null).commit();
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return mtitles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView treeName;
        CircleImageView treeImg;
        TextView date;
        TextView time;
        ImageButton delete_btn;
        RelativeLayout treeGridBack;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            treeName= (TextView)itemView.findViewById(R.id.tree_name);
            treeImg= (CircleImageView)itemView.findViewById(R.id.grid_img);
            date= (TextView)itemView.findViewById(R.id.date);
            time= (TextView)itemView.findViewById(R.id.time);
            delete_btn=(ImageButton)itemView.findViewById(R.id.delete_tree);
            treeGridBack=(RelativeLayout)itemView.findViewById(R.id.tree_grid_back);
        }
    }

}
