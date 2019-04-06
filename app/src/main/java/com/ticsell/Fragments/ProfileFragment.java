package com.ticsell.Fragments;



import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.ticsell.LoginActivity;
import com.ticsell.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mMobile;
    private StorageReference mImageStorage;
    private ProgressDialog mProgressDialog,mImageProgress;
    private Button mLogout,mImagebtn;
    private View mView;
    private static final int GALLERY_PICK=1;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_profile, container, false);
        //begins
        mDisplayImage=(CircleImageView)mView.findViewById(R.id.profile_image);
        mName=(TextView)mView.findViewById(R.id.profile_name);
        mMobile=(TextView)mView.findViewById(R.id.profile_mobile);
        mLogout=(Button)mView.findViewById(R.id.btn_logout);
        mImagebtn=(Button)mView.findViewById(R.id.btn_change_picture);
        mImageStorage= FirebaseStorage.getInstance().getReference();
        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());
        mAuth=FirebaseAuth.getInstance();
        mProgressDialog=new ProgressDialog(getActivity(),R.style.AppCompatAlertDialogStyle);
        mImageProgress=new ProgressDialog(getActivity(),R.style.AppCompatAlertDialogStyle);
        mProgressDialog.setTitle("Please wait");
        mImageProgress.setTitle("Loading...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mImageProgress.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mName.setText(dataSnapshot.child("name").getValue().toString());
                mMobile.setText(dataSnapshot.child("mobile").getValue().toString());
                final String image=dataSnapshot.child("image").getValue().toString();
                if(!image.equals("default")){
                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);

                    Picasso.with(getActivity()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.user).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(getActivity()).load(image).placeholder(R.drawable.user).into(mDisplayImage);

                        }

                    });


                }
                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                mAuth=null;
                sendToLogin();

            }
        });
        mImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent galleryIntent=new Intent();
//                galleryIntent.setType("image/*");
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(galleryIntent,"Select Profile image"),GALLERY_PICK);

                CropImage.activity()
                        .start(getContext(),ProfileFragment.this);
            }
        });
        //ends
        return mView;
    }

    private void sendToLogin() {
        Intent intent=new Intent(getActivity(),LoginActivity.class);
        startActivity(intent);
        getActivity().finish();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageProgress.show();
                Uri resultUri = result.getUri();
                File thumb_filePath=new File(resultUri.getPath());
                String current_user_id=mCurrentUser.getUid();
                Bitmap thumb_bitmap= null;
                try {
                    thumb_bitmap = new Compressor(getActivity())
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte=baos.toByteArray();
                StorageReference filePath = mImageStorage.child("profile_images").child(current_user_id+".jpg");
                final StorageReference thum_filepath=mImageStorage.child("profile_images").child("thumbs").child(current_user_id+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            final String download_url=task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask=thum_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot>thumb_task) {
                                    String thumb_downloadUrl=thumb_task.getResult().getDownloadUrl().toString();
                                    if(thumb_task.isSuccessful()) {

                                        Map update_hashMap=new HashMap();
                                        update_hashMap.put("image",download_url);
                                        update_hashMap.put("thumb_image",thumb_downloadUrl);
                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mImageProgress.dismiss();
                                                    Toast.makeText(getActivity(), "Successfully uploaded  ", Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        });
                                    }
                                    else{ Toast.makeText(getActivity(), "Error in thumbnails  ", Toast.LENGTH_LONG).show();
                                        mImageProgress.dismiss();
                                    }


                                }
                            });

                        }else
                        {
                            Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}


