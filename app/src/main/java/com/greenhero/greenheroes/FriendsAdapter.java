package com.greenhero.greenheroes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
    private String myUserId =myUser.getUid();

    private List<User> mUser;
    private Context mContext;

    public FriendsAdapter(List<User> mUser, Context mContext) {
        this.mUser = mUser;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friends_grid,parent,false);
        return new FriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user= mUser.get(position);
        holder.userName.setText(user.getUserName());
        holder.email.setText(user.getEmailId());
        if(!user.getProfilePic().equals("default"))
        {

            Picasso.get()
                    .load(user.getProfilePic())
                    .placeholder(R.drawable.groot)
                    .error(R.drawable.groot)
                    .into(holder.userImage);
        }
        else
        {
            Log.e("TAG", "imageUrlis Here!!!!! default  !!!" );
        }

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Remove Friend")
                        .setMessage("Are you sure you want to remove this friend ?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(myUserId).child("Friends");
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        for(DataSnapshot dataSnapshot: snapshot.getChildren())
                                        {
                                            if(dataSnapshot.child("mEmailId").getValue(String.class).equals(user.getEmailId()))
                                            {
                                                dataSnapshot.getRef().removeValue();
                                            }
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
                        .setIcon(R.drawable.delete)
                        .show();

            }
        });

        holder.friendBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messageIntent= new Intent(mContext,ChatMessageActivity.class);
                messageIntent.putExtra("otherUserId",user.getUserId());
                mContext.startActivity(messageIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout friendBack;
        TextView userName, email;
        CircleImageView userImage;
        ImageButton remove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = (TextView)itemView.findViewById(R.id.friend_name);
            userImage= (CircleImageView)itemView.findViewById(R.id.friend_img);
            email= (TextView)itemView.findViewById(R.id.friend_email);
            remove= (ImageButton) itemView.findViewById(R.id.remove_friend);
            friendBack= (LinearLayout)itemView.findViewById(R.id.friend_back);
        }
    }
}
