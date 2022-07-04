package com.greenhero.greenheroes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
public class AllMessagesAdapter extends RecyclerView.Adapter<AllMessagesAdapter.ViewHolder>
{
    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
    FirebaseUser myUser= FirebaseAuth.getInstance().getCurrentUser();
    String myUserIdHere;
    int type=5;
    private Context mContext;
    private List<Chat> mChat;
    FirebaseUser fuser;
    DatabaseReference otherUserId;
    String otherUserIdString;


    public AllMessagesAdapter(Context mContext, List<Chat> mChat) {
        this.mContext = mContext;
        this.mChat = mChat;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            myUserIdHere = myUser.getUid();
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.all_message_layout,parent,false);
        if(viewType!=-101)
        type=viewType;
        return new AllMessagesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Chat chat= mChat.get(position);

        if(chat.getReceiver().equals(myUserIdHere))
        {
            if(!chat.isIsseen())
            {
                holder.newMessageAlert.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.newMessageAlert.setVisibility(View.GONE);
            }
        }


        holder.message.setText(chat.getMessage());
        DateFormat df = new SimpleDateFormat("d MMM yyyy,HH:mm");
        String timeNow = df.format(Calendar.getInstance().getTime());
        String[] todayParts = timeNow.split(",");
        String todayDate= todayParts[0];
        String todayTime= todayParts[1];

        String[] timeParts= chat.getTime().split(",");
        String msgDate = timeParts[0];
        String msgTime= timeParts[1];

        if(msgDate.equals(todayDate))
        {
            holder.time.setText(msgTime);
        }
        else
        {
            holder.time.setText(msgDate);
        }

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        if(type==MSG_TYPE_LEFT)
        {

            otherUserId = ref.child(mChat.get(position).getSender());
            otherUserIdString = mChat.get(position).getSender();
        }
        else
        {

            otherUserId = ref.child(mChat.get(position).getReceiver());
            otherUserIdString = mChat.get(position).getReceiver();
        }

        otherUserId.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.userName.setText(snapshot.child("userName").getValue(String.class));
                if(snapshot.hasChild("profilePic"))
                {
                    if(!snapshot.child("profilePic").getValue(String.class).equals("default"))
                    {
                        Picasso.get()
                                .load(snapshot.child("profilePic").getValue(String.class))
                                .placeholder(R.color.black)
                                .error(R.drawable.groot)
                                .into(holder.userImage);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(type==MSG_TYPE_LEFT)
//                   otherUserIdString = mChat.get(position).getSender();
//                else
//                    otherUserIdString = mChat.get(position).getReceiver();

                if(mChat.get(position).getSender().equals(myUserIdHere))
                {
                    otherUserIdString = mChat.get(position).getReceiver();
                }
                else
                {
                    otherUserIdString = mChat.get(position).getSender();
                }

//                Toast.makeText(mContext, otherUserIdString, Toast.LENGTH_SHORT).show();
                Intent messageIntent= new Intent(mContext,ChatMessageActivity.class);
                messageIntent.putExtra("otherUserId",otherUserIdString);
                mContext.startActivity(messageIntent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if(type==MSG_TYPE_LEFT)
                    otherUserIdString = mChat.get(position).getSender();
                else
                    otherUserIdString = mChat.get(position).getReceiver();

                new AlertDialog.Builder(mContext)
                        .setTitle("Delete Chats")
                        .setMessage("Are you sure you want to delete the conversation ?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation

                                final DatabaseReference chatRef= FirebaseDatabase.getInstance().getReference("Chats");
                                chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot dataSnapshot: snapshot.getChildren())
                                        {
                                            Chat chat = dataSnapshot.getValue(Chat.class);
                                            if((chat.getReceiver().equals(otherUserIdString) && chat.getSender().equals(myUserIdHere)) || (chat.getReceiver().equals(myUserIdHere) && chat.getSender().equals(otherUserIdString)))
                                            {
                                                Log.e("Tag","deleted!!!!!!!!!!!!!!!!!!!"+ otherUserIdString);
                                                if(dataSnapshot.hasChild("deleted"))
                                                {
                                                    if(dataSnapshot.child("deleted").getValue(String.class).equals(otherUserIdString))
                                                    {
                                                        dataSnapshot.getRef().removeValue();
                                                    }
                                                }
                                                else
                                                {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("deleted",myUserIdHere);
                                                    dataSnapshot.getRef().updateChildren(hashMap);
                                                }

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
                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView userName, message;
        CircleImageView userImage;
        TextView time;
        RelativeLayout newMessageAlert;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = (TextView)itemView.findViewById(R.id.user_name_allmsg);
            userImage= (CircleImageView)itemView.findViewById(R.id.user_dp_allmsg);
            time= (TextView)itemView.findViewById(R.id.time_allmsg);
            message= (TextView)itemView.findViewById(R.id.message_allmsg);
            newMessageAlert= (RelativeLayout)itemView.findViewById(R.id.new_message_alert);
        }
    }

    @Override
    public int getItemViewType(int position) {

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        if(fuser!= null)
        {
            if (mChat.get(position).getSender().equals(fuser.getUid())) {
                return MSG_TYPE_RIGHT;
            } else {
                return MSG_TYPE_LEFT;
            }
        }
        else
        {
            return -101;
        }
    }
}
