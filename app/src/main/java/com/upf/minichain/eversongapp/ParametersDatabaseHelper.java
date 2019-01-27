package com.upf.minichain.eversongapp;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ParametersDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Eversong.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ParametersColumns.PARAMETERS_TABLE_NAME + " (" +
                    ParametersColumns._ID + " INTEGER PRIMARY KEY," +
                    ParametersColumns.COLUMN_PARAMETER + " TEXT," +
                    ParametersColumns.COLUMN_VALUE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ParametersColumns.PARAMETERS_TABLE_NAME;

    public ParametersDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /*******************
     * PARAMETERS TABLE
     ******************/
    public static class ParametersColumns implements BaseColumns {
        public static final String PARAMETERS_TABLE_NAME = "ParametersTable";
        public static final String COLUMN_PARAMETER = "Parameter";
        public static final String COLUMN_VALUE = "Value";
    }
}