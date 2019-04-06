package com.ticsell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.roger.catloadinglibrary.CatLoadingView;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private Button mSend,mVerify;
    private TextView mResend,mChange;
    private EditText mMobile,mName;
    String mobile,name;
    FirebaseAuth mAuth;
    String verifID;
    CatLoadingView mView;
    DatabaseReference mDatabase;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/font_1.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_login);

        //init
        mView = new CatLoadingView();
        mSend=(Button)findViewById(R.id.start_btn_send);
        mResend=(TextView)findViewById(R.id.start_txt_resend);
        mChange=(TextView)findViewById(R.id.start_txt_change_number);
        mMobile=(EditText)findViewById(R.id.start_edt_number);
        mName=(EditText)findViewById(R.id.start_edt_name);
        mVerify=(Button)findViewById(R.id.start_btn_verify);
        mDatabase= FirebaseDatabase.getInstance().getReference().child("users");
        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reverseComponents();
            }
        });
        mResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_sms();
            }
        });
        mVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mView.setText("Loading..!");
                mView.setCanceledOnTouchOutside(false);
                mView.show(getSupportFragmentManager(), "");
                verify();
            }
        });
        // firebase init
        mAuth=FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                // Save the verification id somewhere
                // ...
                verifID = verificationId;
                showComponents();
                Toast.makeText(LoginActivity.this, "code sent", Toast.LENGTH_SHORT).show();
                // The corresponding whitelisted code above should be used to complete sign-in.

            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                // Sign in with the credential
                // ...
                Toast.makeText(getApplicationContext(), "Verified", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // ...
                Toast.makeText(LoginActivity.this, "Failed:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        };
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mobile=mMobile.getText().toString();
                name=mName.getText().toString();
                if(!TextUtils.isEmpty(mobile)&&!TextUtils.isEmpty(name)){
                    send_sms();
                }else{
                    Toast.makeText(LoginActivity.this, "Enter all the fields.!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void reverseComponents() {
        mMobile.setHint("Enter Mobile Number");
        mMobile.setVisibility(View.VISIBLE);
        mVerify.setVisibility(View.INVISIBLE);
        mResend.setVisibility(View.INVISIBLE);
        mSend.setVisibility(View.VISIBLE);
        mChange.setVisibility(View.INVISIBLE);
        mName.setVisibility(View.VISIBLE);
    }

    private void send_sms() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile, 30L, TimeUnit.SECONDS,
                this, mCallbacks);
        mMobile.setText("");

    }

    private void verify() {
        String input_Code = mMobile.getText().toString();
        if (!input_Code.equals("")) {
            verifyNumber(verifID, input_Code);
        } else {
            Toast.makeText(this, "Enter code", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyNumber(String verifID, String input_code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verifID, input_code);
        signInWithPhoneAuthCredential(credential);
    }

    private void showComponents() {
        mMobile.setHint("Enter OTP");
        mMobile.setVisibility(View.VISIBLE);
        mVerify.setVisibility(View.VISIBLE);
        mResend.setVisibility(View.VISIBLE);
        mSend.setVisibility(View.INVISIBLE);
        mChange.setVisibility(View.VISIBLE);
        mName.setVisibility(View.INVISIBLE);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("siva", "signInWithCredential:success");
                            mDatabase.child(mAuth.getCurrentUser().getUid()).child("mobile").setValue(mobile);
                            mDatabase.child(mAuth.getCurrentUser().getUid()).child("name").setValue(name);
                            mDatabase.child(mAuth.getCurrentUser().getUid()).child("image").setValue("default");

                            Toast.makeText(LoginActivity.this, "Signed IN", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("user_id",mAuth.getCurrentUser().getUid());
                            startActivity(intent);
                            finish();
                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            mView.dismiss();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(LoginActivity.this, "Invalid OTP entered", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            new FancyAlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setBackgroundColor(Color.parseColor("#800ddf"))  //Don't pass R.color.colorvalue
                    .setMessage("Do you really want to Exit ?")
                    .setNegativeBtnText("Cancel")
                    .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
                    .setPositiveBtnText("Exit")
                    .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
                    .setAnimation(Animation.POP)
                    .isCancellable(true)
                    .setIcon(R.drawable.ic_star_border_black_24dp,Icon.Visible)
                    .OnPositiveClicked(new FancyAlertDialogListener() {
                        @Override
                        public void OnClick() {
                            LoginActivity.this.finish();
                        }
                    })
                    .OnNegativeClicked(new FancyAlertDialogListener() {
                        @Override
                        public void OnClick() {
                            //do nothing
                        }
                    })
                    .build();
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }
//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }
}
