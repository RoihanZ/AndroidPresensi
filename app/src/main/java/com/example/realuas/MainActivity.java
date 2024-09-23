package com.example.realuas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;

    public static final String SHARED_PREF_NAME = "myPref";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.home) {
                    Toast.makeText(MainActivity.this, "Home Selected", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.admin) {
                    Toast.makeText(MainActivity.this, "Admin Selected", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(i);
                    return true;
                } else if(id == R.id.presensi){
                    Toast.makeText(MainActivity.this, "Presensi Selected", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, PresensiActivity.class);
                    startActivity(i);
                    return true;
                } else if (id == R.id.logout) {
                    Toast.makeText(MainActivity.this, "Logout Selected", Toast.LENGTH_SHORT).show();
                    Boolean updateSession = db.upgradeSession("kosong", 1);  // Menghapus sesi login di database
                    if(updateSession){
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(MainActivity.this, "Anda belum login!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (id == R.id.About) {
                    Toast.makeText(MainActivity.this, "About Selected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        db = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        Boolean checksession = db.checkSession("ada");
        if (!checksession) {
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(login);
            finish();
        }else{
            String email = sharedPreferences.getString("email", null);
            if (email == null) {
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
                finish();
                return;
            }
            int userId = db.getUserId(email);
            String role = db.getUserRole(userId);
            String fullname = db.getFullName(email);

            View headerView = navigationView.getHeaderView(0);
            TextView tvName = headerView.findViewById(R.id.tvName);
            TextView tvEmail = headerView.findViewById(R.id.tvEmail);

            tvName.setText(fullname);
            tvEmail.setText(email);

            if (role != null && role.equals("pegawai")) {
                Menu menu = navigationView.getMenu();
                MenuItem adminItem = menu.findItem(R.id.admin);
                if (adminItem != null) {
                    adminItem.setVisible(false);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
