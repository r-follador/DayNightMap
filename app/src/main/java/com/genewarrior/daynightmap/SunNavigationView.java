package com.genewarrior.daynightmap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TimePicker;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SunNavigationView extends ConstraintLayout {
    ImageButton buttonChoice;
    ImageButton resetTime;
    ImageButton buttonPlay;
    ImageButton buttonSet;

    SunSeekBar sunSeekBar;
    Status status;
    boolean limitLiteVersion;


    public SunNavigationView(Context context) {
        super(context);
        init();
    }

    public SunNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SunNavigationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_sunnavigationview, this);
        buttonChoice = findViewById(R.id.buttonChoice);
        sunSeekBar = findViewById(R.id.sunSeekBar);
        resetTime = findViewById(R.id.buttonReset);
        buttonSet = findViewById(R.id.buttonSet);
        buttonPlay = findViewById(R.id.buttonPlay);

        buttonChoice.setImageResource(R.drawable.ic_time_of_day_button);

        buttonChoice.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (status.getButtonChoice() == Status.ButtonChoice.DayOfYear)
                    setSelection(Status.ButtonChoice.MinuteOfDay);
                else
                    setSelection(Status.ButtonChoice.DayOfYear);
            }
        });


        resetTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                stopSunUpdater();
                SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss S Z");
                //System.out.println("Set time to " + fmt.format(originalTime.getTime()));
                status.getDate().setTime((new GregorianCalendar()).getTime());
                //status.setDate((GregorianCalendar)originalTime.clone());
                sunSeekBar.update();
                callback.onSunNavigationChange();
                //System.out.println("Time is now " + fmt.format(status.getDate().getTime()));
            }
        });

        buttonSet.setOnClickListener(view -> {
            if (status.getButtonChoice() == Status.ButtonChoice.DayOfYear) {
                DialogFragment newFragment = new DatePickerFragment(this);
                newFragment.show(((Activity)getContext()).getFragmentManager(), "datePicker");
            } else { //TimeofDay is selected
                DialogFragment newFragment = new TimePickerFragment(this);
                newFragment.show(((Activity)getContext()).getFragmentManager(), "timePicker");
            }
        });


        sunSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar2, int progress,boolean fromUser) {
                if (fromUser)
                    stopSunUpdater();
                status.setDate(sunSeekBar.getDate());

                long t_diff = Math.abs(status.getDate().getTimeInMillis() - System.currentTimeMillis());
                if (t_diff < 60000) //less than one minute difference
                    resetTime.setVisibility(INVISIBLE);
                else
                    resetTime.setVisibility(VISIBLE);
                callback.onSunNavigationChange();
            }
        });

        buttonPlay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (sunupdaterIsRunning) {
                    stopSunUpdater();
                } else {
                    sunUpdater.run();
                    buttonPlay.setImageResource(R.drawable.ic_stop);
                }
            }
        });
    }

    Handler handler = new Handler();

    boolean sunupdaterIsRunning = false;

    public Runnable sunUpdater = new Runnable() {
        @Override
        public void run() {
            sunupdaterIsRunning = true;
            if (status.buttonChoice == Status.ButtonChoice.DayOfYear) {
                status.getDate().add(GregorianCalendar.DAY_OF_YEAR, 1);
            }
            else {
                int dayOfYear = status.getDate().get(GregorianCalendar.DAY_OF_YEAR);
                status.getDate().add(GregorianCalendar.MINUTE, 2);
            }
            sunSeekBar.update();
            callback.onSunNavigationChange();
            handler.postDelayed(sunUpdater,40);
        }
    };

    private void stopSunUpdater() {
        handler.removeCallbacks(sunUpdater);
        sunupdaterIsRunning = false;
        buttonPlay.setImageResource(R.drawable.ic_play_arrow);
    }

    public interface OnSunNavigationChangeListener {
        // TODO: Update argument type and name
        void onSunNavigationChange();
    }

    OnSunNavigationChangeListener callback;

    public void setOnFragmentInteractionListener(OnSunNavigationChangeListener callback, Status status, boolean limitLiteVersion) {
        this.limitLiteVersion = limitLiteVersion;
        this.callback = callback;
        this.status = status;
        sunSeekBar.setStatus(status);
        sunSeekBar.update();
    }

    protected void setSelection(Status.ButtonChoice choice) {
        if (choice == Status.ButtonChoice.DayOfYear) {
            status.setButtonChoice(choice);
            sunSeekBar.update();
            buttonChoice.setImageResource(R.drawable.ic_day_of_year_button);
        } else if (choice == Status.ButtonChoice.MinuteOfDay) {
            status.setButtonChoice(choice);
            sunSeekBar.update();
            buttonChoice.setImageResource(R.drawable.ic_time_of_day_button);
        }
        sunSeekBar.update();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopSunUpdater();
    }

    @SuppressLint("ValidFragment") //Shouldn't use custom constructor, but the alternative is a pain in the ass
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        SunNavigationView sunNavigationView;
        public TimePickerFragment(SunNavigationView sunNavigationView) {
            super();
            this.sunNavigationView = sunNavigationView;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            sunNavigationView.status.getDate().set(Calendar.HOUR_OF_DAY, hourOfDay);
            sunNavigationView.status.getDate().set(Calendar.MINUTE, minute);
            sunNavigationView.sunSeekBar.update();
            sunNavigationView.callback.onSunNavigationChange();
        }
    }

    @SuppressLint("ValidFragment") //Shouldn't use custom constructor, but the alternative is a pain in the ass
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        SunNavigationView sunNavigationView;
        public DatePickerFragment(SunNavigationView sunNavigationView) {
            super();
            this.sunNavigationView = sunNavigationView;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            sunNavigationView.status.getDate().set(Calendar.YEAR, year);
            sunNavigationView.status.getDate().set(Calendar.MONTH, month);
            sunNavigationView.status.getDate().set(Calendar.DAY_OF_MONTH, day);
            sunNavigationView.sunSeekBar.update();
            sunNavigationView.callback.onSunNavigationChange();
        }
    }
}
