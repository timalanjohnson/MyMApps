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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonRegisterUser;
    private EditText editTextEmailRegister;
    private EditText editTextPasswordRegister;
    private TextView textViewAlreadyRegistered;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonRegisterUser = (Button) findViewById(R.id.buttonRegisterUser);
        editTextEmailRegister = (EditText) findViewById(R.id.editTextEmailRegister);
        editTextPasswordRegister = (EditText) findViewById(R.id.editTextPasswordRegister);
        textViewAlreadyRegistered = (TextView) findViewById(R.id.textViewAlreadyRegistered);

        progressDialog = new ProgressDialog(this);

        buttonRegisterUser.setOnClickListener(this);
        textViewAlreadyRegistered.setOnClickListener(this);

    }

    private void registerUser(){
        String email = editTextEmailRegister.getText().toString().trim();
        String password = editTextPasswordRegister.getText().toString().trim();

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

        progressDialog.setMessage("Registering user...");
        progressDialog.show();

        // Register user with Firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            // User is registered successfully
                            Toast.makeText(RegisterActivity.this, "Registered Successfully.", Toast.LENGTH_SHORT).show();

                            // Open MapsActivity
                            finish();
                            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "Could Not Register. Please Try Again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view == buttonRegisterUser) {
            registerUser();
        }

        if (view == textViewAlreadyRegistered) {
            // Open LoginActivity
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
    }
}
