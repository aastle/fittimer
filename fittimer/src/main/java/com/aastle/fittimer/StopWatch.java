package com.aastle.fittimer;

/**
 * Created by prometheus on 11/26/13.
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Chronometer;

/**
 * A StopWatch with start, pause, reset functionality.
 *
 * @author Viktor Lindgren
 *
 */
public class StopWatch extends Chronometer {

    private Long currentTimeLastStop;
    private boolean isRunning = false;
    private boolean clockReseted = true;

    /**
     * A StopWatch with start, pause, reset functionality.
     *
     * Example usage: stopWatch = (StopWatch) findViewById(R.id.chronometer);
     *
     * @param context
     */
    public StopWatch(Context context) {
        super(context);
    }

    /**
     * A StopWatch with start, pause, reset functionality.
     *
     * Example usage: stopWatch = (StopWatch) findViewById(R.id.chronometer);
     *
     * @param context
     * @param attr
     */
    public StopWatch(Context context, AttributeSet attr) {
        super(context, attr);
    }
    private void init(AttributeSet attrs) {
        TypedArray a=getContext().obtainStyledAttributes(
                attrs,
                R.styleable.StopWatch);

        //Use a
        Log.i("test", a.getString(
                R.styleable.StopWatch_android_text));
        Log.i("test",""+a.getColor(
                R.styleable.StopWatch_android_textColor, Color.BLACK));
        Log.i("test",a.getString(
                R.styleable.StopWatch_extraInformation));

        //Don't forget this
        a.recycle();
    }
    /**
     *
     * @return the displayed time in seconds
     */
    public Long getTime() {
        if (!isRunning) {
            return currentTimeLastStop - this.getBase() / 1000;
        } else {
            return (time() - this.getBase()) / 1000;
        }
    }

    /**
     * Sets the time that the display should show. Call this method will not
     * change state(if it is running or paused.).
     *
     * @param time
     *            in seconds
     */
    public void setTime(int time) {
        Long time2 = time();
        this.setBase(time2 - time * 1000);
        clockReseted = false;
        currentTimeLastStop = time2;
    }

    /**
     * Start the clock to tick.
     *
     * Can be called multiple times, it just set the state to running.
     */
    public void startClock() {
        if (isRunning)
            return;

        if (clockReseted) {
            this.setBase(time());
        } else {
            long time = getRestoreTime();
            this.setBase(time);
        }

        this.start();
        isRunning = true;
        clockReseted = false;
    }

    /**
     * Get restore time for clock, used for set base time.
     *
     * @return base time for clock.
     */
    private long getRestoreTime() {
        return this.getBase() + time() - currentTimeLastStop;
    }

    /**
     * Can be called multiple times, sets the state to paused
     */
    public void pauseClock() {
        if (!isRunning) {
            return;
        }
        this.stop();
        setClockIsNotRunning();
    }

    /**
     * Reset the clock to time 00:00 and stop the clock. Do not ignore multiple
     * reset.
     */
    public void resetClock() {
        this.setBase(time());
        this.refreshDrawableState();
        this.stop();
        setClockIsNotRunning();
        clockReseted = true;
    }

    /**
     * Sets currentTimeLastStop to time.
     *
     * @param time
     */
    private void setClockIsNotRunning(Long time) {
        currentTimeLastStop = time;
        isRunning = false;
    }

    /**
     * Sets currentTimeLastStop to the time now.
     */
    private void setClockIsNotRunning() {
        setClockIsNotRunning(time());
    }

    /**
     * Relative time since something. It just have to be constant.
     *
     * @return
     */
    private Long time() {
        return SystemClock.elapsedRealtime();
    }

    /**
     * Tells if the clock is running or not, true is running.
     *
     * @return
     */
    public boolean running() {
        return isRunning;
    }
}
