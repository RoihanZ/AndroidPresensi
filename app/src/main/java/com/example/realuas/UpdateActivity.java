package com.example.realuas;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class UpdateActivity extends AppCompatActivity {

    protected Cursor cursor;
    DatabaseHelper dbHelper;
    Button btnUpdate, btnBack;
    EditText etFullName, etAddress, etDoB, etPhoneNumber;
    Spinner etGender;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        dbHelper = new DatabaseHelper(this);

        etFullName = findViewById(R.id.etFullName);
        etAddress = findViewById(R.id.etAddress);
        etDoB = findViewById(R.id.etDoB);
        etGender = findViewById(R.id.etGender);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String email = getIntent().getStringExtra("email");

        String query = "SELECT user.id, profile.fullName, profile.address, profile.DoB, profile.gender, profile.phoneNumber, profile.role " +
                "FROM user " +
                "INNER JOIN profile ON user.id = profile.user_id " +
                "WHERE user.email = ?";

        cursor = db.rawQuery(query, new String[]{email});

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
            etFullName.setText(cursor.getString(1));
            etAddress.setText(cursor.getString(2));
            etDoB.setText(cursor.getString(3));
            setSpinnerValue(etGender, cursor.getString(4));
            etPhoneNumber.setText(cursor.getString(5));
        }

        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);

        etDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("UPDATE profile SET fullName='" +
                        etFullName.getText().toString() + "', address='" +
                        etAddress.getText().toString() + "', DoB='" +
                        etDoB.getText().toString() + "', gender='" +
                        etGender.getSelectedItem().toString() + "', phoneNumber='" +
                        etPhoneNumber.getText().toString() + "' WHERE user_id=" + userId);
                Toast.makeText(getApplicationContext(), "Berhasil", Toast.LENGTH_LONG).show();
                AdminActivity.ac.RefreshList();
                finish();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etGender.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                etDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (value != null) {
            int spinnerPosition = adapter.getPosition(value);
            spinner.setSelection(spinnerPosition);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
