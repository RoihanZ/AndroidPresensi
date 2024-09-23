package com.example.realuas;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private EditText etFullName, etAddress, etDoB, etPhoneNumber;
    private Button btnSaveProfile;
    private DatabaseHelper db;
    private int userId;
    private Spinner etGender;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);

        etFullName = findViewById(R.id.etFullName);
        etAddress = findViewById(R.id.etAddress);
        etDoB = findViewById(R.id.etDoB);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        etGender = findViewById(R.id.etGender);

        userId = getIntent().getIntExtra("USER_ID", -1);

        // Inisialisasi Calendar untuk DatePickerDialog
        calendar = Calendar.getInstance();

        // Set onClickListener untuk etDoB (EditText Date of Birth)
        etDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Atur Spinner (etGender) dengan adapter ArrayAdapter
        String[] genderOptions = {"L", "P"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etGender.setAdapter(genderAdapter);

        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString();
                String address = etAddress.getText().toString();
                String dob = etDoB.getText().toString();
                String gender = etGender.getSelectedItem().toString();
                String phoneNumber = etPhoneNumber.getText().toString();

                boolean isInserted = db.simpanProfile(userId, fullName, address, dob, gender, phoneNumber, "pegawai");
                if (isInserted) {
                    Toast.makeText(ProfileActivity.this, "Profile Saved", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(ProfileActivity.this, "Profile Save Failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Method untuk menampilkan DatePickerDialog
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDoB();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Method untuk memperbarui tampilan EditText Date of Birth (etDoB)
    private void updateDoB() {
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
        etDoB.setText(simpleDateFormat.format(calendar.getTime()));
    }
}

