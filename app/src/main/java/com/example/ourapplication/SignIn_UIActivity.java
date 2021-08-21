package com.example.ourapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

public class SignIn_UIActivity extends AppCompatActivity {
    private TextInputEditText tietUsername;
    private TextInputEditText tietPassword;
    private Button bSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signinui);

        this.tietUsername=findViewById(R.id.textUsername);
        this.tietPassword=findViewById(R.id.textPassword);
        this.bSignIn=findViewById(R.id.buttonSignIn);
        this.bSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {






            }
        });



    }





}