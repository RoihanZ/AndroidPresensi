package com.example.realuas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FinalProjectUAS.db";
    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        Log.d(TAG, "DatabaseHelper initialized");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");
        db.execSQL("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT NOT NULL, password TEXT NOT NULL)");
        db.execSQL("CREATE TABLE profile (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, fullName TEXT, address TEXT, DoB TEXT, gender TEXT, phoneNumber TEXT, role TEXT, FOREIGN KEY(user_id) REFERENCES user(id))");
        db.execSQL("CREATE TABLE presensi (id INTEGER PRIMARY KEY AUTOINCREMENT, nama_pegawai TEXT, keterangan TEXT, timestamp DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE session (id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT NOT NULL)");
        db.execSQL("INSERT INTO session (id, login) VALUES (1, 'kosong')");
        db.execSQL("INSERT INTO user (id, email, password) VALUES (1, 'ahmadroihanns@gmail.com', 'poliban123')");
        db.execSQL("INSERT INTO profile (id, user_id, fullName, address, DoB, gender, phoneNumber, role) VALUES (1, 1, 'Ahmad Roihan Nugraha', 'JL. KUIN UTARA GG. AL-MIZAN', '3 July 2003', 'L', '083150243054', 'admin')");
        Log.d(TAG, "Database tables created and initialized");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database...");
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS profile");
        db.execSQL("DROP TABLE IF EXISTS presensi");
        db.execSQL("DROP TABLE IF EXISTS session");
        onCreate(db);
    }

    // Check Session
    public Boolean checkSession(String value){
        SQLiteDatabase FPdb = this.getReadableDatabase();
        Cursor cursor = FPdb.rawQuery("SELECT * FROM session WHERE login = ? ", new String[]{value});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        Log.d(TAG, "checkSession: " + result);
        return result;
    }

    // Upgrade Session
    public Boolean upgradeSession(String value, int id){
        SQLiteDatabase FPdb = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("login", value);
        long update = FPdb.update("session", values, "id=" + id, null);
        Log.d(TAG, "upgradeSession: " + (update != -1));
        return update != -1;
    }

    // Input User
    public boolean simpanUser(String email, String password){
        Log.d(TAG, "Inserting user...");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        long insert = db.insert("user", null, values);
        Log.d(TAG, "simpanUser: " + (insert != -1));
        return insert != -1;
    }

    // Input Profile
    public boolean simpanProfile(int userId, String fullName, String address, String dob, String gender, String phoneNumber, String role) {
        Log.d(TAG, "Inserting profile...");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("fullName", fullName);
        values.put("address", address);
        values.put("DoB", dob);
        values.put("gender", gender);
        values.put("phoneNumber", phoneNumber);
        values.put("role", role);
        long insert = db.insert("profile", null, values);
        Log.d(TAG, "simpanProfile: " + (insert != -1));
        return insert != -1;
    }

    // Get User and Profile Data
    public Cursor getUserList() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, email, password FROM user", null);
        return cursor;
    }

    // Get User ID
    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM user WHERE email = ?", new String[]{email});
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(0);
            cursor.close();
            return userId;
        } else {
            return -1;
        }
    }

    // Get Role by User ID
    public String getUserRole(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT role FROM profile WHERE user_id = ?", new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            return role;
        } else {
            return null;
        }
    }

    // Check User
    public Boolean checkUser(String email){
        Log.d(TAG, "Checking user...");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM user WHERE email = ?", new String[]{email});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        Log.d(TAG, "checkUser: " + result);
        return result;
    }

    // Check Login
    public Boolean checkLogin(String email, String password){
        Log.d(TAG, "Checking login...");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM user WHERE email = ? AND password = ?", new String[]{email, password});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        Log.d(TAG, "checkLogin: " + result);
        return result;
    }

    // Get Full Name
    public String getFullName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String fullName = "";

        Cursor cursor = db.rawQuery("SELECT fullName FROM profile WHERE user_id = (SELECT id FROM user WHERE email = ?)", new String[]{email});

        if (cursor.moveToFirst()) {
            fullName = cursor.getString(cursor.getColumnIndex("fullName"));
        }

        cursor.close();
        return fullName;
    }

    // Insert Presensi
    public boolean simpanPresensi(String namaPegawai, String keterangan, String waktu) {
        Log.d(TAG, "Inserting presensi...");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nama_pegawai", namaPegawai);
        values.put("keterangan", keterangan);
        values.put("timestamp", waktu);

        long insert = db.insert("presensi", null, values);
        Log.d(TAG, "simpanPresensi: " + (insert != -1));
        return insert != -1;
    }

    // Get Presensi List
    public Cursor getPresensiList() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT nama_pegawai, keterangan, timestamp FROM presensi", null);
    }

}
