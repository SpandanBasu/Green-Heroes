package com.greenhero.greenheroes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.greenhero.greenheroes.MainActivity.latitude;
import static com.greenhero.greenheroes.MainActivity.longitude;

public class PlantTreeSlidesFragment extends Fragment {
    public static String PREFS_FIRST_TIME_SLIDE="fistTimeSlidePref";
    public static String NOT_FIRST_TIME_KEY="notFistTimeKey";
    private ViewPager viewPager;
    private LinearLayout belowPager;
    private SliderAdapter sliderAdapter;
    private List<ImageView> dots;
    private int NUM_PAGES=5;
    private TabLayout tabIndicator;
    private Button btnNext,btnCapture,btnSkip;
    private int position=0;
    private Animation capture_btn_anim;
    Dialog plantTreeDialog;
    Dialog captureDialog, noTreeDialog;
    Button okBtnCapture,okBtnNoTreeDialog;
    ImageView closeDialog,closeNoTreeDialog;
    ImageView closeDialogCapture,slide_img;
    public static LottieAnimationView successAnimation;
    CircleImageView plantImage;
    EditText plantName;
    Button completeBtn;
    public static Uri imgUri;
    private int REQUEST_CODE = 1;
    private boolean treeFound =false;
    SharedPreferences preferences;
    InputImage imageInputML;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        MainActivity.navigationView.getMenu().getItem(3).setChecked(true);
        setHasOptionsMenu(true);
        MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#008a00")));

        View rootView = inflater.inflate(R.layout.plant_tree_slides,container,false);
        preferences  = this.getActivity().getSharedPreferences(PREFS_FIRST_TIME_SLIDE, Context.MODE_PRIVATE);
        boolean NotFirstTime = preferences.getBoolean(NOT_FIRST_TIME_KEY,false);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(NOT_FIRST_TIME_KEY, true);
        editor.apply();


        btnNext= (Button)rootView.findViewById(R.id.nxt) ;
        viewPager= (ViewPager)rootView.findViewById(R.id.view_pager);
        belowPager= (LinearLayout)rootView.findViewById(R.id.below_pager);
        slide_img=(ImageView)rootView.findViewById(R.id.slide_img);
        btnCapture=(Button)rootView.findViewById(R.id.btn_capture);
        btnSkip=(Button)rootView.findViewById(R.id.skip) ;
        tabIndicator=(TabLayout)rootView.findViewById(R.id.tab_indicator) ;
        successAnimation=(LottieAnimationView)rootView.findViewById(R.id.success_animation);
        sliderAdapter=new SliderAdapter(getContext());
        viewPager.setAdapter(sliderAdapter);
        capture_btn_anim= AnimationUtils.loadAnimation(getContext(),R.anim.capture_btn_anim);
        tabIndicator.setupWithViewPager(viewPager);

        if(NotFirstTime)
        {
            position= viewPager.getCurrentItem();
            if(position<NUM_PAGES)
            {
                position=NUM_PAGES-1;
                viewPager.setCurrentItem(position);
                loadLastScreen();
                btnSkip.setVisibility(View.INVISIBLE);
            }
        }


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position= viewPager.getCurrentItem();
                if(position<NUM_PAGES)
                {
                    position++;
                    viewPager.setCurrentItem(position);
                }
                if(position == NUM_PAGES-1)
                {
                    loadLastScreen();
                }
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position= viewPager.getCurrentItem();
                if(position<NUM_PAGES)
                {
                    position=NUM_PAGES-1;
                    viewPager.setCurrentItem(position);

                }

            }
        });

        tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() != 0)
                {

                    btnSkip.setVisibility(View.INVISIBLE);
                }
                if(tab.getPosition() ==NUM_PAGES-1)
                {
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });































        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                treeFound=false;
                successAnimation.setVisibility(View.INVISIBLE);
                captureDialog = new Dialog(getContext());
                captureDialog.setContentView(R.layout.capture_dialog);
                closeDialogCapture = (ImageView) captureDialog.findViewById(R.id.close_dialog_plant_tree);
                okBtnCapture = (Button) captureDialog.findViewById(R.id.complete_plant_tree_btn);
                closeDialogCapture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        captureDialog.dismiss();
                    }
                });
                okBtnCapture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        captureDialog.dismiss();
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)+ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, REQUEST_CODE);
                        } else {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    }
                });
                captureDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                captureDialog.show();

            }
        });

        return rootView;
    }

    private void loadLastScreen() {

        btnNext.setVisibility(View.INVISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);
        btnCapture.setVisibility(View.VISIBLE);
        btnCapture.setAnimation(capture_btn_anim);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            if(data != null) {




                final Bitmap image = (Bitmap) data.getExtras().get("data");
                imgUri = getImageUri(getContext(), image);


                try {
                    imageInputML = InputImage.fromFilePath(getContext(), imgUri);
                    ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
                    labeler.process(imageInputML)
                            .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                                @Override
                                public void onSuccess(List<ImageLabel> labels) {
                                    // Task completed successfully
                                    for (ImageLabel label : labels) {

                                        String text = label.getText();

                                        float confidence = label.getConfidence();
                                        int index = label.getIndex();

                                        if(text.equals("Plants") || text.equals("Plant") || text.equals("Tree") || text.equals("Soil")|| text.equals("Green") || text.equals("Vegetable") || text.equals("Vegetables") || text.equals("Flower") || text.equals("Fruit") || text.equals("Leaf") || text.equals("Trunk"))
                                        {
                                            treeFound=true;
                                            if(imgUri!=null)
                                            {
                                                Log.v("uri of image", "uri of image is here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + imgUri.toString());
                                                plantTreeDialog = new Dialog(getContext());
                                                plantTreeDialog.setContentView(R.layout.popup_plant_tree);
                                                closeDialog = (ImageView) plantTreeDialog.findViewById(R.id.close_dialog_plant_tree);
                                                plantImage = (CircleImageView) plantTreeDialog.findViewById(R.id.plant_image);
                                                plantName = (EditText) plantTreeDialog.findViewById(R.id.plant_name);
                                                completeBtn = (Button) plantTreeDialog.findViewById(R.id.complete_plant_tree_btn);

                                                //plantImage.setImageURI(imgUri);

                                                plantImage.setImageBitmap(image);
                                                closeDialog.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        plantTreeDialog.dismiss();
                                                    }
                                                });

                                                completeBtn.setOnClickListener(new View.OnClickListener() {
                                                    @SuppressLint("NewApi")
                                                    @Override
                                                    public void onClick(View view) {
                                                        if (TextUtils.isEmpty(plantName.getText().toString().trim())) {
                                                            plantName.setError("Name is required");
                                                            plantName.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                                                            plantName.requestFocus();
                                                            return;
                                                        }
                                                        successAnimation.setVisibility(View.VISIBLE);
                                                        successAnimation.playAnimation();
                                                        plantTreeDialog.dismiss();
                                                        CreateNewTree newTree = new CreateNewTree(latitude, longitude, plantName.getText().toString().trim());
                                                        UploadTree newUploadTree = new UploadTree(newTree,getContext());
                                                        newUploadTree.uploadTree();
                                                        Toast.makeText(getContext(), "The data uploaded to server successfully going for verification...", Toast.LENGTH_LONG).show();
                                                        Log.e("TAG", "Data uploaded successfully for tree!!!!!");
                                                    }
                                                });
                                                plantTreeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                plantTreeDialog.show();
                                            }
                                            else
                                            {
                                                Toast.makeText(getContext(), "Could not find image uri!!!", Toast.LENGTH_SHORT).show();
                                            }
                                            break;

                                        }
                                        Log.e("TAG", "Labels are!!!!!!!!!! "+text+"     " );
                                    }
                                    if(!treeFound)
                                    {

                                        noTreeDialog = new Dialog(getContext());
                                        noTreeDialog.setContentView(R.layout.no_tree_found_dialog);
                                        closeNoTreeDialog = (ImageView) noTreeDialog.findViewById(R.id.close_dialog_no_tree);
                                        okBtnNoTreeDialog = (Button) noTreeDialog.findViewById(R.id.no_tree_found_ok_btn);
                                        closeNoTreeDialog.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                noTreeDialog.dismiss();
                                            }
                                        });
                                        okBtnNoTreeDialog.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                noTreeDialog.dismiss();

                                            }
                                        });
                                        noTreeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        noTreeDialog.show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                    // Task failed with an exception
                                    // ...
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }







            }
            else
            {
                Toast.makeText(getContext(), "Image Not Found!!", Toast.LENGTH_SHORT).show();
                plantTreeDialog = new Dialog(getContext());
                plantTreeDialog.setContentView(R.layout.popup_plant_tree);
                closeDialog = (ImageView) plantTreeDialog.findViewById(R.id.close_dialog_plant_tree);
                plantImage = (CircleImageView) plantTreeDialog.findViewById(R.id.plant_image);
                plantName = (EditText) plantTreeDialog.findViewById(R.id.plant_name);
                completeBtn = (Button) plantTreeDialog.findViewById(R.id.complete_plant_tree_btn);


                //plantImage.setImageURI(imgUri);
                imgUri = getImageUri(getContext(), BitmapFactory.decodeResource(getResources(), R.drawable.tree));
                plantImage.setImageResource(R.drawable.tree);
                closeDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        plantTreeDialog.dismiss();

                    }
                });

                completeBtn.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(plantName.getText().toString().trim())) {
                            plantName.setError("Name is required");
                            plantName.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                            plantName.requestFocus();
                            return;
                        }
                        plantTreeDialog.dismiss();

                        CreateNewTree newTree = new CreateNewTree(latitude, longitude, plantName.getText().toString().trim());
                        UploadTree newUploadTree = new UploadTree(newTree,getContext());
                        newUploadTree.uploadTree();
                        Toast.makeText(getContext(), "The data uploaded to server successfully going for verification...", Toast.LENGTH_LONG).show();
                    }
                });
                plantTreeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                plantTreeDialog.show();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.refresh);
        if(item!=null)
            item.setVisible(false);
    }


}
