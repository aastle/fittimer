package com.aastle.fittimer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by prometheus on 10/19/13.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "";
    private static String DB_PATH = "";
    private static String TABLE_NAME = "";
    private static String NEW_COLUMN = "";
    private SQLiteDatabase thisDataBase;
    private final Context thisContext;
    private StringBuilder sb = null;

    public static final String COL_ROW_ID = "_id";
    public static final String COL_DATE = "date";
    public static final String COL_TIME = "time";
    public static final String COL_APPSTATE = "appstate";
    public static final String COL_INTERVAL = "interval";
    public static final String COL_SESSION = "session";

    /**
     * constuctor
     * @param context
     * @param databaseName
     * @param cursorFactory
     * @param version
     */
    public DatabaseHelper (Context context,String databaseName, SQLiteDatabase.CursorFactory cursorFactory,int version){
        super(context,databaseName,null,version);
        DB_NAME = databaseName;
        sb = new StringBuilder();

        thisContext = context;
        DB_PATH = "/data/data/" + thisContext.getApplicationContext().getPackageName() + "/databases/";
        boolean databaseExists = checkDataBase();
        if(databaseExists)
        {
            openDatabase();
            //Log.e("SQL","DatabaseHelp constructor, database exists, Database opened");
        }
        else
        {
            //Log.e("SQL","DatabaseHelper constructor: Database doesn't exist");
            try{
            createDatabase();
            //Log.e("SQL","DataBaseHelper constructor, Database Created");
            openDatabase();
            } catch (Exception e){
                //Log.e("SQL",">>>>>>>>> Create database failed: " + e.fillInStackTrace());
            }
        }
    }

    public void createDatabase() throws IOException {

            this.getReadableDatabase();
            try{
                copyDatabase();
            }catch (IOException io){

                throw new Error(">>>>>>>>>>>>>>>>>>>>>> " + io.getMessage() + " fill in stack trace: " + io.fillInStackTrace() + " stack trace: " + io.getStackTrace());
            }


    }
    private boolean checkDataBase(){
        SQLiteDatabase checkDB = null;

        try{

            sb.append(DB_PATH).append(DB_NAME);

            checkDB = SQLiteDatabase.openDatabase(sb.toString(),null,SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e){}
        if (checkDB != null){

            checkDB.close();
        }
        else {
            //android.util.Log.e("DBHELPER",">>>>>>>>>>>>> checkDataBase checkDB IS NULL!");
        }
        return checkDB != null ? true : false;
    }
    private void copyDatabase() throws IOException {
        close();
        InputStream inputStream = thisContext.getAssets().open(DB_NAME);
        sb.delete(0,sb.length());
        sb.append(DB_PATH).append(DB_NAME);
        OutputStream outputStream = new FileOutputStream(sb.toString());

        byte[] buffer = new byte[1024];
        int length;
        while((length=inputStream.read(buffer))>0){

            outputStream.write(buffer,0,length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        getWritableDatabase().close();
    }
    public void openDatabase()throws SQLException {
        sb.delete(0,sb.length());
        sb.append(DB_PATH).append(DB_NAME);
        thisDataBase = SQLiteDatabase.openDatabase(sb.toString(),null,SQLiteDatabase.OPEN_READWRITE);
    }

    /**
     *
     * @param tableName
     * @return Cursor
     */
    public Cursor getCursorForTable(String tableName){
        //DatabaseHelper dbHelper = new DatabaseHelper(thisContext,tableName,null,1);
        Cursor thisCursor = this.getReadableDatabase().rawQuery("select * from " + tableName, null);
        return thisCursor;
    }

    /**
     *
     * @param tableName
     * @param date
     * @param time
     * @param appState
     * @return
     */
    public long insertTime(String tableName, String date, String time, String appState,int interval, int session) {
        long id;
        thisDataBase = getWritableDatabase();
        //Log.e("SQL","insertTime(), thisDataBase.toString() = "+thisDataBase.toString() + ", tableName = " + tableName);
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_DATE, date);
        initialValues.put(COL_TIME, time);
        initialValues.put(COL_APPSTATE,appState);
        initialValues.put(COL_INTERVAL,interval);
        initialValues.put(COL_SESSION,session);
        id = thisDataBase.insert(tableName, null, initialValues);
        thisDataBase.close();
        return id;
    }


    public String setTableName(String tablename){
        return TABLE_NAME = tablename;
    }

    public String[] getColumnsArray(String csvColumns){
        return csvColumns.split(",");
    }
    public String[] getFilterParams(String csvParameters){
        return csvParameters.split(",");
    }

    public Long getCountOfTableRows(String tableName){
        thisDataBase = getReadableDatabase();
//        Log.e("SQL","getCountOfTableRows(), thisDataBase.toString() = " + thisDataBase.toString() +", tableName = " + tableName);
//        Log.e("SQL","thisDataBase.isOpen() = " + thisDataBase.isOpen());
        Long results = null;
        try{
        SQLiteStatement sqLiteStatement = thisDataBase.compileStatement("SELECT COUNT(*) FROM " + tableName);
        results = sqLiteStatement.simpleQueryForLong();
        }catch(SQLiteDoneException de)
        {
            Log.e("SQL",de.getMessage());
        }

        return results;
    }
    public int DeleteAllRowsFromTable(String table){

        thisDataBase = getWritableDatabase();
        return thisDataBase.delete(table,null,null);
    }
    public Cursor showAllTables(){
        String mySql = " SELECT name FROM sqlite_master " + " WHERE type='table'"
                + "   AND name LIKE 'PR_%' ";
        return thisDataBase.rawQuery(mySql, null);
    }
    @Override
    public synchronized void close(){
        if(thisDataBase != null)
            thisDataBase.close();
        super.close();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion){

        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE " +
                        TABLE_NAME + " ADD COLUMN " + COL_INTERVAL + " INTEGER NULL");
                break;
            case 2:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_SESSION + " INTEGER NULL");
                break;
        }
    }
}
