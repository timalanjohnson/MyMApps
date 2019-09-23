package com.timalanjohnson.mymapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonLoginUser;
    private EditText editTextEmailLogin;
    private EditText editTextPasswordLogin;
    private TextView textViewNotRegistered;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonLoginUser = (Button) findViewById(R.id.buttonLoginUser);
        editTextEmailLogin = (EditText) findViewById(R.id.editTextEmailLogin);
        editTextPasswordLogin = (EditText) findViewById(R.id.editTextPasswordLogin);
        textViewNotRegistered = (TextView) findViewById(R.id.textViewNotRegistered);

        progressDialog = new ProgressDialog(this);

        buttonLoginUser.setOnClickListener(this);
        textViewNotRegistered.setOnClickListener(this);
    }

    private void loginUser(){
        String email = editTextEmailLogin.getText().toString().trim();
        String password = editTextPasswordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            // Email is empty
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return; // Prevents function executing further
        }

        if (TextUtils.isEmpty(password)) {
            // Password is empty
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return; // Prevents function executing further
        }

        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        // Login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            // User is registered successfully
                            Toast.makeText(LoginActivity.this, "Logged In Successfully.", Toast.LENGTH_SHORT).show();

                            // Open MapsActivity
                            finish();
                            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Could Not Login. Please Try Again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view == buttonLoginUser) {
           loginUser();
        }

        if (view == textViewNotRegistered) {
            // Open RegisterActivity
            finish();
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        }
    }
}
