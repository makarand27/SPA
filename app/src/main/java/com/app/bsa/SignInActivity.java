package com.app.bsa;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class SignInActivity extends AppCompatActivity {


    private String mEmailId = "";
    private String mPassword = "";
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            startActivity(new Intent(this, BatchListActivity.class));
            finish();
        }

        Button tSignIn = findViewById(R.id.btn_sign_in);
        tSignIn.setOnClickListener(view -> {
            if(validateInputs()){
                if(signIn(mEmailId,mPassword)  ){
                    FirebaseUser currentUser1 = FirebaseAuth.getInstance().getCurrentUser();

                    if(currentUser1 != null ) {
                        startActivity(new Intent(getApplicationContext(), BatchListActivity.class));
                        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                }
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(mContext.getResources().getString(R.string.err_login_validation));
                builder.setNegativeButton(android.R.string.ok, (dialog, id) -> {
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        });

    }

    private boolean validateInputs(){

        mEmailId = ((EditText)findViewById(R.id.txt_email_id)).getText().toString();
        mPassword =((TextInputEditText)findViewById(R.id.txt_password)).getText().toString();
        if(mEmailId != null && mEmailId.contains(mContext.getResources().getString(R.string.login_domain))){
            if(mPassword != null && !mPassword.equals("")){
                return true;
            }
        }
        return false;
    }

    public boolean signIn(String vEmailId, String vPassword){

        FirebaseAuth tAuth =  FirebaseAuth.getInstance();

        tAuth.signInWithEmailAndPassword(vEmailId, vPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if(currentUser != null) {
                            startActivity(new Intent(getApplicationContext(), BatchListActivity.class));
                            //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        }

                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage(mContext.getResources().getString(R.string.err_login));
                        builder.setNegativeButton(android.R.string.ok, (dialog, id) -> {
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }

                });

        return false;
    }


    @Override
    public void onBackPressed() {
        // disable going back
    }

}
