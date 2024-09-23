package com.example.realuas;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText txtEmail, txtPass;
    private Button btnLogin;

    AlertDialog.Builder dialog;
    LayoutInflater inflaters;
    View dialogView;
    private TextView registerRedirectText;
    private EditText etEmail, etPassword, etRepeatPassword;
    private DatabaseHelper db;
    public static final String SHARED_PREF_NAME = "myPref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmail = findViewById(R.id.txtEmail);
        txtPass = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.login);
        registerRedirectText = findViewById(R.id.registerRedirectText);

        db = new DatabaseHelper(this);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        registerRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new AlertDialog.Builder(LoginActivity.this);
                inflaters = getLayoutInflater();
                dialogView = inflaters.inflate(R.layout.form_register, null);
                dialog.setView(dialogView);
                dialog.setCancelable(true);
                dialog.setTitle("Register");

                etEmail = dialogView.findViewById(R.id.etEmail);
                etPassword = dialogView.findViewById(R.id.etPassword);
                etRepeatPassword = dialogView.findViewById(R.id.etRepeatPassword);

                dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                dialog.setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = dialog.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();

                alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String inEmail = etEmail.getText().toString();
                        String inPassword = etPassword.getText().toString();
                        String inrePassword = etRepeatPassword.getText().toString();

                        // Validasi email harus mengandung karakter '@'
                        if (!isValidEmail(inEmail)) {
                            etEmail.setError("Email tidak valid");
                            return;
                        }

                        Boolean cekUser = db.checkUser(inEmail);
                        if (inEmail.isEmpty() || inPassword.isEmpty() || inrePassword.isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Form belum diisi semua", Toast.LENGTH_LONG).show();
                        } else if (cekUser) {
                            etEmail.setError("Email Sudah Ada");
                        } else if (!inrePassword.equals(inPassword)) {
                            etRepeatPassword.setError("Password Tidak Sama");
                        } else {
                            Boolean daftar = db.simpanUser(inEmail, inPassword);
                            if (daftar) {
                                int userId = db.getUserId(inEmail);
                                Toast.makeText(LoginActivity.this, "Daftar Berhasil", Toast.LENGTH_LONG).show();
                                alert.dismiss();
                                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                                i.putExtra("USER_ID", userId);
                                startActivity(i);
                            } else {
                                Toast.makeText(LoginActivity.this, "Daftar Gagal", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        });
        
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getEmail = txtEmail.getText().toString();
                String getPassword = txtPass.getText().toString();

                // Validasi email harus diisi
                if (getEmail.isEmpty()) {
                    txtEmail.setError("Email harus diisi");
                    return; // Menghentikan proses jika email kosong
                }

                // Validasi email harus valid (memiliki karakter '@')
                if (!isValidEmail(getEmail)) {
                    txtEmail.setError("Email tidak valid");
                    return; // Menghentikan proses jika email tidak valid
                }

                if (getPassword.isEmpty()) {
                    txtPass.setError("Password harus diisi");
                    return; // Menghentikan proses jika password kosong
                }

                // Lanjutkan proses login jika email dan password telah diisi
                Boolean masuk = db.checkLogin(getEmail, getPassword);
                if (masuk) {
                    Boolean updateSession = db.upgradeSession("ada", 1);
                    String fullName = db.getFullName(getEmail);
                    if (updateSession) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putBoolean("masuk", true);
                        editor.putString("email", getEmail);
                        editor.putString("fullname", fullName);
                        editor.apply();

                        Toast.makeText(getApplicationContext(), "Berhasil Masuk", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Gagal Masuk", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Method untuk validasi email
    private boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
