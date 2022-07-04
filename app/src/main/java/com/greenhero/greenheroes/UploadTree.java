package com.greenhero.greenheroes;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.greenhero.greenheroes.MainActivity.treePlanted;
import static com.greenhero.greenheroes.PlantTreeSlidesFragment.imgUri;


public class UploadTree {
    Context mContext;
    FirebaseDatabase database;
    DatabaseReference ref;
    DatabaseReference refNew;
    private int totalTreesNum;
    private CreateNewTree mNewTree;
    StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();
    private String user_id = FirebaseAuth.getInstance().getUid();

    public UploadTree(CreateNewTree newTree, Context context) {
        mNewTree = newTree;
        mContext= context;
    }

    public void uploadTree() {

        database = FirebaseDatabase.getInstance();

        totalTreesNum= treePlanted;


        refNew = FirebaseDatabase.getInstance().getReference("Users").child(user_id).child("Trees").child(mNewTree.getDate() + " " + mNewTree.getTime());
        refNew.setValue(mNewTree);



        final StorageReference filePath = mStorageReference.child("Photos").child(user_id+" "+mNewTree.getTreeName() +" "+mNewTree.getDate() + " " + mNewTree.getTime());

        if(imgUri!=null)
        {
            filePath.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            refNew = FirebaseDatabase.getInstance().getReference("Users/" + user_id + "/Trees/" + mNewTree.getDate() + " " + mNewTree.getTime());

//                        HashMap<String, String> hashMap = new HashMap<>();
//                        hashMap.put("imageurl", String.valueOf(uri));
                            refNew.child("imageurl").setValue(String.valueOf(uri));
                            //SignUp.current_user_db.child("imageurl").setValue(hashMap);
                            totalTreesNum=totalTreesNum+1;
                            database = FirebaseDatabase.getInstance();
                            ref = database.getReference("Users/" + user_id);
                            ref.child("totalTrees").setValue(totalTreesNum);
                            Log.e("Upload Trees", "Total number of trees is "+ totalTreesNum);
                        }
                    });


                }
            });
        }
        else
        {
            Toast.makeText(mContext, "Image Not Found!!", Toast.LENGTH_SHORT).show();
        }



    }
}
