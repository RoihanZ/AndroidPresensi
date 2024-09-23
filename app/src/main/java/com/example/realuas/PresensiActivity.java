package com.example.realuas;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PresensiActivity extends AppCompatActivity {

    private TextView textHari, textTanggal, textJam, textPosisi, textKoordinat, textJarak;
    private TableLayout tableKehadiran;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitudeKantor = -3.295616;
    private double longitudeKantor = 114.582169;
    private static final int YOUR_RADIUS_IN_METERS = 100;
    private Location lastKnownLocation; // Variabel untuk menyimpan lokasi terakhir

    private Button buttonDatang;
    private Button buttonPulang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presensi);

        textHari = findViewById(R.id.textHari);
        textTanggal = findViewById(R.id.textTanggal);
        textJam = findViewById(R.id.textJam);
        textPosisi = findViewById(R.id.textPosisi);
        textKoordinat = findViewById(R.id.textKoordinat);
        textJarak = findViewById(R.id.textJarak);
        tableKehadiran = findViewById(R.id.tableKehadiran);

        buttonDatang = findViewById(R.id.btnDatang);
        buttonPulang = findViewById(R.id.btnPulang);

        updateDateTime();

        // Setup location manager and listener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastKnownLocation = location; // Simpan lokasi terbaru
                checkDistanceFromOffice(location.getLatitude(), location.getLongitude());

                // Update textKoordinat secara real-time
                updateKoordinatTextView(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // Start location updates
        startLocationUpdates();

        // Update date and time every second
        runnable = new Runnable() {
            @Override
            public void run() {
                updateDateTime();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        // Button click listener for 'Datang'
        buttonDatang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastKnownLocation != null && isInsideOffice(lastKnownLocation)) {
                    // Check if presensi 'Datang' already done today
                    SharedPreferences sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
                    String lastPresensiDate = sharedPreferences.getString("last_presensi_date", "");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String currentDate = dateFormat.format(new Date());

                    if (!currentDate.equals(lastPresensiDate)) {
                        // Save presensi and update lastPresensiDate
                        simpanPresensi();
                        sharedPreferences.edit().putString("last_presensi_date", currentDate).apply();
                        sharedPreferences.edit().putBoolean("datang_done", true).apply(); // Mark datang as done

                        // Enable button Pulang
                        buttonPulang.setEnabled(true);

                        // Reload presensi data after saving
                        loadPresensiData();
                    } else {
                        Toast.makeText(PresensiActivity.this, "Anda sudah melakukan presensi Datang hari ini.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PresensiActivity.this, "Anda harus berada di kantor untuk melakukan presensi Datang.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Button click listener for 'Pulang'
        buttonPulang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastKnownLocation != null && isInsideOffice(lastKnownLocation)) {
                    // Check if presensi 'Pulang' already done today
                    SharedPreferences sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
                    String lastPresensiPulangDate = sharedPreferences.getString("last_presensi_pulang_date", "");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String currentDate = dateFormat.format(new Date());

                    if (!currentDate.equals(lastPresensiPulangDate)) {
                        // Save presensi pulang and update lastPresensiPulangDate
                        simpanPresensiPulang();
                        sharedPreferences.edit().putString("last_presensi_pulang_date", currentDate).apply();
                        sharedPreferences.edit().putBoolean("datang_done", false).apply(); // Reset datang done

                        // Reload presensi data after saving
                        loadPresensiData();
                    } else {
                        Toast.makeText(PresensiActivity.this, "Anda sudah melakukan presensi Pulang hari ini.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PresensiActivity.this, "Anda harus berada di kantor untuk melakukan presensi Pulang.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load existing attendance data
        loadPresensiData();

        // Check datang status on create
        checkDatangStatus();
    }

    private void checkDatangStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        boolean datangDone = sharedPreferences.getBoolean("datang_done", false);
        buttonPulang.setEnabled(datangDone);
    }

    private void simpanPresensi() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String namaPegawai = sharedPreferences.getString("fullname", "");

        String keterangan = "Datang";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String waktu = dateFormat.format(new Date());

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        boolean insertSuccess = dbHelper.simpanPresensi(namaPegawai, keterangan, waktu);

        if (insertSuccess) {
            Toast.makeText(this, "Presensi berhasil disimpan", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gagal menyimpan presensi", Toast.LENGTH_SHORT).show();
        }
    }

    private void simpanPresensiPulang() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String namaPegawai = sharedPreferences.getString("fullname", "");

        String keterangan = "Pulang";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String waktu = dateFormat.format(new Date());

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        boolean insertSuccess = dbHelper.simpanPresensi(namaPegawai, keterangan, waktu);

        if (insertSuccess) {
            Toast.makeText(this, "Presensi Pulang berhasil disimpan", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gagal menyimpan presensi Pulang", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPresensiData() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getPresensiList();

        // Clear the table before adding new rows
        tableKehadiran.removeViews(1, tableKehadiran.getChildCount() - 1);

        while (cursor.moveToNext()) {
            String namaPegawai = cursor.getString(cursor.getColumnIndex("nama_pegawai"));
            String keterangan = cursor.getString(cursor.getColumnIndex("keterangan"));
            String waktu = cursor.getString(cursor.getColumnIndex("timestamp"));

            TableRow row = new TableRow(this);
            TextView namaText = new TextView(this);
            TextView keteranganText = new TextView(this);
            TextView waktuText = new TextView(this);

            // Set properties for each TextView
            namaText.setText(namaPegawai);
            keteranganText.setText(keterangan);
            waktuText.setText(waktu);

            // Set layout parameters for each TextView
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
            namaText.setLayoutParams(params);
            keteranganText.setLayoutParams(params);
            waktuText.setLayoutParams(params);

            // Set gravity and padding for each TextView
            namaText.setGravity(Gravity.CENTER);
            keteranganText.setGravity(Gravity.CENTER);
            waktuText.setGravity(Gravity.CENTER);
            namaText.setPadding(8, 8, 8, 8);
            keteranganText.setPadding(8, 8, 8, 8);
            waktuText.setPadding(8, 8, 8, 8);

            // Add TextViews to TableRow
            row.addView(namaText);
            row.addView(keteranganText);
            row.addView(waktuText);

            // Add TableRow to TableLayout
            tableKehadiran.addView(row);
        }
        cursor.close();
    }

    private void updateDateTime() {
        Locale locale = new Locale("id", "ID");
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", locale);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", locale);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", locale);

        // Set time zone to device's local time
        TimeZone timeZone = TimeZone.getDefault();
        dayFormat.setTimeZone(timeZone);
        dateFormat.setTimeZone(timeZone);
        timeFormat.setTimeZone(timeZone);

        Date now = new Date();
        String day = dayFormat.format(now);
        String date = dateFormat.format(now);
        String time = timeFormat.format(now);

        textHari.setText(day + ", ");
        textTanggal.setText(date);
        textJam.setText(time);
    }

    private boolean isInsideOffice(Location location) {
        if (location != null) {
            float[] distance = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), latitudeKantor, longitudeKantor, distance);
            double distanceInMeters = distance[0];

            return distanceInMeters <= YOUR_RADIUS_IN_METERS;
        }
        return false;
    }

    private void checkDistanceFromOffice(double latitude, double longitude) {
        float[] distance = new float[1];
        Location.distanceBetween(latitude, longitude, latitudeKantor, longitudeKantor, distance);
        double distanceInMeters = distance[0];

        // Update text on TextView based on distance
        String distanceText = String.format(Locale.getDefault(), "%.2f meter", distanceInMeters);
        textJarak.setText(distanceText);

        if (distanceInMeters <= YOUR_RADIUS_IN_METERS) {
            textPosisi.setText("Anda berada di kantor");
        } else {
            textPosisi.setText("Anda tidak berada di kantor");
        }
    }

    private void updateKoordinatTextView(double latitude, double longitude) {
        String coordinates = String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
        textKoordinat.setText(coordinates);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
