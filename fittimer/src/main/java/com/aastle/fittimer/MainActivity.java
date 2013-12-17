package com.aastle.fittimer;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;




public class MainActivity extends Activity {
    StopWatch stopWatch;
    Button buttonStopWatch;
    Button resetButton;
    Button statsButton;
    Drawable shapePaused;
    Drawable shapeStart;
    Drawable shapeStats;
    TransitionDrawable pulse_start;
    boolean throb = true;
    int start;
    int end;
    private int interval;
    LinearLayout linearLayout;
    private static final String TAG = "SQL";
    private static final String DATABASE_NAME = "trimtimer.s3db";
    private static final String TABLE_NAME = "times";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        throb = checkThrobberPref();

        //TODO Delete the following before release!!!!
/*
        DatabaseHelper dbHelper = new DatabaseHelper(getBaseContext(),DATABASE_NAME,null,1);
        int rowsDeleted = dbHelper.DeleteAllRowsFromTable(TABLE_NAME);
        Log.e(TAG,"DELETED n rows, n = " + rowsDeleted);
*/

        stopWatch = (StopWatch) findViewById(R.id.stopwatch);
        shapePaused = getResources().getDrawable(R.drawable.shape_circle_stop_start_paused);
        shapeStart = getResources().getDrawable(R.drawable.shape_circle_stop_start);
        shapeStats = getResources().getDrawable(R.drawable.shape_stats_circle);
        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);
        pulse_start = (TransitionDrawable)getResources().getDrawable(R.drawable.pulse_color_start);
        interval = getLastInterval(getLastIntervalFromSqlite());
        buttonStopWatch = (Button) findViewById(R.id.buttonStartStop);
        buttonStopWatch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!stopWatch.running()) {
                    saveTime(TABLE_NAME,getDate(),getTime(),"running",interval);
                    stopWatch.startClock();
                    buttonStopWatch.setText("PAUSE");
                    buttonStopWatch.setTextSize(50);
                    buttonStopWatch.setBackground(shapeStart);

                    Log.e(TAG,"!stopWatch.running, interval = "+ interval);
                } else if (stopWatch.running()) {
                    saveTime(TABLE_NAME,getDate(),getTime(),"paused",interval);
                    stopWatch.pauseClock();
                    buttonStopWatch.setText("RESUME");
                    buttonStopWatch.setTextSize(40);
                    buttonStopWatch.setBackground(shapePaused);
                    Log.e(TAG,"stopWatch.paused, interval "+ interval);
                    getInterval();

                }

            }
        });
        resetButton = (Button) findViewById(R.id.buttonReset);
        resetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stopWatch.running()){
                    stopWatch.resetClock();
                    buttonStopWatch.setText("PAUSE");
                    buttonStopWatch.setTextSize(50);
                    buttonStopWatch.setBackground(shapeStart);
                    stopWatch.startClock();
                    //Log.e(TAG,getTimeFromSqlite());
                }else if(!stopWatch.running()){
                    stopWatch.resetClock();
                    buttonStopWatch.setText("START");
                    buttonStopWatch.setTextSize(50);
                    buttonStopWatch.setBackground(shapeStart);
                }
            }
        });

        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                if(throb){
                colorThrobber();
                }
            }
        });
        statsButton = (Button)findViewById(R.id.buttonStats);
        statsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogBox(getStats());
            }
        });
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
    @Override
    protected void onResume(){
        super.onResume();
    }
    @Override
    protected void onStop(){
        super.onStop();
        saveTime(TABLE_NAME,getDate(),getTime(),"stopped",interval);
        stopWatch.resetClock();
    }
    private int getInterval(){
        return interval++;
    }
    private void colorThrobber(){
        ValueAnimator va;
        va = ObjectAnimator.ofInt(findViewById(R.id.linearLayout), "backgroundColor", start, end);
        start = Color.WHITE;
        end = Color.LTGRAY;
        va.setDuration(500);
        va.setEvaluator(new ArgbEvaluator());
        va.setRepeatCount(1);
        va.setRepeatMode(ValueAnimator.REVERSE);
        va.start();
    }
    private String getDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        Log.e(TAG,"getDate() " + dateFormat.format(date));
        return dateFormat.format(date);
    }
    private String getTime(){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this,SettingsActivity.class));
                return true;
            case R.id.about:
                showDialogBox(buildAboutInfo());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private long saveTime(String table, String date, String time, String appState,int interval){
        DatabaseHelper dbHelper = new DatabaseHelper(getBaseContext(),DATABASE_NAME,null,1);
        return dbHelper.insertTime(table,date,time,appState,interval);
    }

    private String getStats(){
        Cursor statsCursor = getTimeFromSqlite();
        return buildJodaStats(statsCursor);
    }

    private Cursor getTimeFromSqlite(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT _id,appstate,date,time,interval FROM ");
        sqlBuilder.append(TABLE_NAME);
        sqlBuilder.append(" WHERE date >= date() ");
        sqlBuilder.append(" ORDER BY interval,date");
        DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext(),DATABASE_NAME,null,1);
        //Log.e(TAG,"Sqlite table times.rowCount = "+databaseHelper.getCountOfTableRows(TABLE_NAME));
        databaseHelper.setTableName(TABLE_NAME);
        return databaseHelper.getReadableDatabase().rawQuery(sqlBuilder.toString(), null);
    }

    private Cursor getLastIntervalFromSqlite(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT _id, appstate,date,time,interval FROM ");
        stringBuilder.append(TABLE_NAME);
        stringBuilder.append(" WHERE date >= date() ");
        stringBuilder.append(" ORDER BY date, interval DESC");
         DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext(),DATABASE_NAME,null,1);
        databaseHelper.setTableName(TABLE_NAME);
        return databaseHelper.getReadableDatabase().rawQuery(stringBuilder.toString(),null);
    }
    private int getLastInterval(Cursor cursor){
        int lastInterval = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            lastInterval = cursor.getInt(cursor.getColumnIndex("interval"));
            cursor.moveToNext();
        }
        if(lastInterval != 0){
            return lastInterval + 1;
        }
        return 0;
    }

    /**
     * Builds and displays exercise stats using the Jodatime library
     * @param cursor
     * @return string
     */
    private String buildJodaStats(Cursor cursor){
        ArrayList<DateTime> arrayList = new ArrayList<DateTime>();
        DateTime[] arrayOfTimes = null;
        int interval = 0;
        StringBuilder stringBuilder = new StringBuilder();
        DateTime datePart = new DateTime();
        DateTime timePart = new DateTime();
        DateTimeFormatter formatterDateSqlite = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatterTimeSqlite = DateTimeFormat.forPattern("HH:mm:ss");
        DateTimeFormatter simpleHumanDateFormat = DateTimeFormat.forPattern("MMMM d',' yyyy");
        DateTimeFormatter simpleHumanTimeFormat = DateTimeFormat.forPattern("H:mm:ss");

        cursor.moveToFirst();
        DateTime firstDate = formatterDateSqlite.parseDateTime(cursor.getString(cursor.getColumnIndex("date")));
        cursor.moveToLast();
        DateTime lastDate = formatterDateSqlite.parseDateTime(cursor.getString(cursor.getColumnIndex("date")));
        DateTime dateDiff = lastDate.minusHours(firstDate.get(DateTimeFieldType.hourOfDay()))
                .minusMinutes(firstDate.get(DateTimeFieldType.minuteOfHour()))
                .minusSeconds(firstDate.get(DateTimeFieldType.secondOfMinute()));
        stringBuilder.append("Date: ");
        stringBuilder.append(dateDiff.toString(simpleHumanDateFormat));
        stringBuilder.append("\n\n");
        stringBuilder.append("Total time exercised:\n");
        stringBuilder.append(dateDiff.toString(simpleHumanTimeFormat));
        stringBuilder.append("\n");
        stringBuilder
                .append(dateDiff.toString(DateTimeFormat.forPattern("H"))).append(" hours, ")
                        .append(dateDiff.toString(DateTimeFormat.forPattern("m"))).append(" minutes, ")
                        .append(dateDiff.toString(DateTimeFormat.forPattern("s"))).append(" seconds");

/*
        while (!cursor.isAfterLast())
        {
            try{
                datePart = formatterDateSqlite.parseDateTime(cursor.getString(cursor.getColumnIndex("date")));
                timePart = formatterTimeSqlite.parseDateTime(cursor.getString(cursor.getColumnIndex("time")));
                interval = cursor.getInt(cursor.getColumnIndex("interval"));
                arrayList.add(timePart);
                Log.e(TAG,datePart +", " + timePart +", " + " ,interval = " +interval);
            }catch (Exception pe){
                Log.e(TAG,pe.getMessage());
            }
            cursor.moveToNext();
        }

        arrayOfTimes = arrayList.toArray(new DateTime[arrayList.size()]);
        Log.e(TAG,"arrayOfTimes.length = " + arrayOfTimes.length);
        ArrayList<DateTime> stats = new ArrayList<DateTime>();

            for(int d=0;d<arrayOfTimes.length;d++){
                DateTime startTime = null;
                DateTime endTime = null;

                if(d%2==0){
                    endTime = arrayOfTimes[d];
                }else{
                    startTime = arrayOfTimes[d];
                }
                Log.e(TAG,"startTime " + startTime.toString(simpleHumanTimeFormat));
                Log.e(TAG,"endTime " + endTime.toString(simpleHumanTimeFormat));

                if(startTime != null && endTime != null){
                    stats.add(endTime.minusSeconds(startTime.get(DateTimeFieldType.minuteOfHour()))
                .minusSeconds(startTime.get(DateTimeFieldType.secondOfMinute())));
                }
            }
        for(DateTime dt:stats){
            stringBuilder.append(dt.toString(simpleHumanTimeFormat));
            stringBuilder.append("\n");
        }
*/

        return stringBuilder.toString();
    }

    private String buildAboutInfo(){
        String packageName = "";
        String versionName = "";

        Long time = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int year = calendar.get(Calendar.YEAR);

        int versionCode = 0;
        try{
            packageName = getPackageName();
            versionCode = getPackageManager().getPackageInfo(packageName, 0).versionCode;
            versionName = getPackageManager().getPackageInfo(packageName, 0).versionName;
        }catch (Exception e){
            Log.e(TAG,"Package Name not Found :(");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Trim Timer\n");
        stringBuilder.append("Version: ");
        stringBuilder.append(versionCode);
        stringBuilder.append("\nBuild: ");
        stringBuilder.append(versionName);
        stringBuilder.append("\nCopyright ");
        stringBuilder.append(year);
        stringBuilder.append(" by Alan W. Astle");
        return stringBuilder.toString();

    }
    private boolean checkThrobberPref(){
        SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(
                getBaseContext());
        return myPref.getBoolean("preference_throb", true);
    }
    private void showDialogBox(CharSequence about){
        new AlertDialog.Builder(this)
                .setMessage(about)
                .setPositiveButton("OK",null)
                .show();

    }


}
