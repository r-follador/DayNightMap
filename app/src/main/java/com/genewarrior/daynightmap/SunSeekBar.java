package com.genewarrior.daynightmap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.SeekBar;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SunSeekBar extends SeekBar {
    static final int maximumPos = 10000;

    Status status;

    public SunSeekBar(Context context) {
        super(context);
        init();
    }

    public SunSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SunSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        setMax(maximumPos);
        Typeface font = ResourcesCompat.getFont(getContext(), R.font.lato);
        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackPaint.setTypeface(font);
        blackPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorSunseekbarFont));
        blackPaint.setStyle(Paint.Style.FILL);
        fontsize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,12, getResources().getDisplayMetrics());
        blackPaint.setTextSize(fontsize);
        drawDate = new GregorianCalendar();
        drawDate.set(GregorianCalendar.DAY_OF_YEAR, 1);
        drawDate.set(GregorianCalendar.HOUR, 0);
        drawDate.set(GregorianCalendar.MINUTE, 0);
    }

    Paint blackPaint;
    GregorianCalendar drawDate;
    int fontsize;

    public void setStatus(Status status)
    {
        this.status = status;
    }


    public void update() {
        this.setProgress(date2position(status.date, status.getButtonChoice()));
    }

    public void setDate(GregorianCalendar date) {

    }

    public GregorianCalendar getDate() {
        return position2date(this.getProgress(), status.buttonChoice, status.date);
    }



    private static int date2position(GregorianCalendar date, Status.ButtonChoice buttonChoice) {
        if (buttonChoice == Status.ButtonChoice.MinuteOfDay) {
            int minuteOfDay = date.get(Calendar.MINUTE) + (date.get(Calendar.HOUR_OF_DAY)*60);
            return Math.round((float)maximumPos*((float)minuteOfDay/(float)((23*60)+59)));
        } else if (buttonChoice == Status.ButtonChoice.DayOfYear) {
            return Math.round((float)maximumPos*((float)(date.get(Calendar.DAY_OF_YEAR)-1)/364.0f));
        }
        return -1; //error
    }

    private static GregorianCalendar position2date(int position, Status.ButtonChoice buttonChoice, GregorianCalendar date) {

        GregorianCalendar dateOut = (GregorianCalendar)(date.clone());

        if (buttonChoice == Status.ButtonChoice.MinuteOfDay) {
            int minuteOfDay = Math.round((((float)position/(float)maximumPos)*(float)((23*60)+59)));
            int hourOfDay = minuteOfDay/60;
            int minuteOfHour = minuteOfDay%60;



            dateOut.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateOut.set(Calendar.MINUTE, minuteOfHour);
        } else if (buttonChoice == Status.ButtonChoice.DayOfYear) {
            dateOut.set(Calendar.DAY_OF_YEAR, 1+Math.round(364.0f*(float)position/(float)maximumPos));
        }
        return dateOut;
    }



    //see https://stackoverflow.com/questions/15011144/how-to-make-custom-seek-bar-in-android
    @Override
    protected synchronized void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        // size of seek bar.
        float width = getWidth();
        float height = getHeight();
        float mPaddingLeft = getPaddingLeft();
        float mPaddingRight = getPaddingRight();
        float mPaddingTop = getPaddingTop();
        float mPaddingBottom = getPaddingBottom();
        float yMiddle = mPaddingTop + ((height-mPaddingTop-mPaddingBottom)/2);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM");

        float barWidth = width - mPaddingLeft-mPaddingRight;

        if (status.getButtonChoice()==Status.ButtonChoice.DayOfYear) {
            for (int month = 0; month < 12; month++) {
                drawDate.set(GregorianCalendar.MONTH, month);
                int pos = date2position(drawDate, Status.ButtonChoice.DayOfYear);
                float x = (barWidth * ((float) pos / (float) maximumPos)) + mPaddingRight;
                canvas.drawCircle(x, yMiddle, 5, blackPaint);
                float textY = (month % 2 == 0 ? yMiddle - 6 : yMiddle + fontsize + 2);
                canvas.drawText(sdf.format(drawDate.getTime()), x + 2, textY, blackPaint);
            }
        } else {
            for (int hour = 0; hour < 25; hour++) {

                float x = (barWidth * ((float)hour/24f)) + mPaddingRight;
                canvas.drawCircle(x, yMiddle, 5, blackPaint);
                float textY = (hour % 2 == 0 ? yMiddle - 12 : yMiddle + fontsize + 6);
                if (hour%3!=0)
                    continue;
                canvas.drawCircle(x, yMiddle, 8, blackPaint);
                if (hour==24)
                    continue;
                String txt = Integer.toString(hour)+(hour<12?"am":"pm");
                canvas.drawText(txt, x - 15, textY, blackPaint);
            }
        }

    }

}
