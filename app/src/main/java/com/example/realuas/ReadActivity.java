package com.example.realuas;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReadActivity extends AppCompatActivity {

    protected Cursor cursor;
    DatabaseHelper dbHelper;
    Button bt2;
    TextView text1, text2, text3, text4, text5, text6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        dbHelper = new DatabaseHelper(this);
        text1 = findViewById(R.id.textView1);
        text2 = findViewById(R.id.textView2);
        text3 = findViewById(R.id.textView3);
        text4 = findViewById(R.id.textView4);
        text5 = findViewById(R.id.textView5);
        text6 = findViewById(R.id.textView6);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String email = getIntent().getStringExtra("email");

        String query = "SELECT user.email, profile.fullName, profile.address, profile.DoB, profile.gender, profile.phoneNumber, profile.role " +
                "FROM user " +
                "INNER JOIN profile ON user.id = profile.user_id " +
                "WHERE user.email = ?";

        cursor = db.rawQuery(query, new String[]{email});

        if (cursor.moveToFirst()) {
            text1.setText(cursor.getString(1));
            text2.setText(cursor.getString(2));
            text3.setText(cursor.getString(3));
            text4.setText(cursor.getString(4));
            text5.setText(cursor.getString(5));
            text6.setText(cursor.getString(6));

        }

        bt2 = findViewById(R.id.button1);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}