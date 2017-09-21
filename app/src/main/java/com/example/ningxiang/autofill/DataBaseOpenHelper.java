package com.example.ningxiang.autofill;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ningxiang on 4/6/17.
 */

public class DataBaseOpenHelper extends SQLiteOpenHelper {

    public static final String TAG = "DataBaseOpenHelper";

    public static final String DATABASE_NAME = "auto_fill.db";

    public static final String SCENE_TABLE = "scene";

    public DataBaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onUpgrade: +");
        onCreateSceneTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void onCreateSceneTable(SQLiteDatabase db) {
        Log.d(TAG, "onCreateSceneTable : +");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + SCENE_TABLE + "("
                + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "package" + " TEXT,"
                + "class" + " TEXT,"
                + "inputs_hashcode" + " INTEGER,"
                + "auto_fill" + " BOOLEAN DEFAULT 0,"
                + "no_ask" + " BOOLEAN DEFAULT 0,"
                + "inputs_name" + " TEXT,"
                + "inputs_data" + " TEXT,"
                + "UNIQUE (package, class, inputs_hashcode)"
                + ");"
        );

        Log.d(TAG, "onCreateSceneTable : -");
    }
}
