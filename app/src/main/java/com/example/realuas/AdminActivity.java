package com.example.realuas;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    String[] daftar;
    ListView ListView01;
    protected Cursor cursor;
    DatabaseHelper dbCenter;
    public static AdminActivity ac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        ac = this;
        dbCenter = new DatabaseHelper(this);
        RefreshList();
    }

    public void RefreshList() {
        Cursor cursor = dbCenter.getUserList();
        if (cursor != null && cursor.moveToFirst()) {
            daftar = new String[cursor.getCount()];
            for (int cc = 0; cc < cursor.getCount(); cc++) {
                daftar[cc] = "Email: " + cursor.getString(1) + "\n" +
                        "Password: " + cursor.getString(2); // Mengambil password dari index kolom yang sesuai
                cursor.moveToNext();
            }
            cursor.close();
        } else {
            daftar = new String[0];
        }

        // Tampilkan data ke ListView dengan adapter kustom
        ListView01 = findViewById(R.id.listView);
        CustomAdapter adapter = new CustomAdapter(this, daftar);
        ListView01.setAdapter(adapter);
        ListView01.setSelected(true);

        ListView01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String selectedData = daftar[arg2];
                String email = selectedData.split("\n")[0].split(": ")[1]; // Ambil email dari string

                final CharSequence[] dialogitem = {"Read", "Update", "Delete"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
                builder.setTitle("Choose");
                builder.setItems(dialogitem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                Intent ir = new Intent(getApplicationContext(), ReadActivity.class);
                                ir.putExtra("email", email);
                                startActivity(ir);
                                break;
                            case 1:
                                Intent iu = new Intent(getApplicationContext(), UpdateActivity.class);
                                iu.putExtra("email", email);
                                startActivity(iu);
                                break;
                            case 2:
                                SQLiteDatabase db = dbCenter.getWritableDatabase();
                                int userId = dbCenter.getUserId(email);

                                db.execSQL("DELETE FROM profile WHERE user_id = " + userId);

                                db.execSQL("DELETE FROM user WHERE id = " + userId);

                                RefreshList();
                                break;
                        }
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}