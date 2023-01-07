package com.genewarrior.daynightmap.ui.daynight;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.genewarrior.daynightmap.R;
import com.genewarrior.daynightmap.Status;
import com.genewarrior.daynightmap.SunNavigationView;
import com.github.chrisbanes.photoview.PhotoView;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DayNightFragment extends Fragment implements SunNavigationView.OnSunNavigationChangeListener {

    TextView timeView;
    boolean isRunning = false;
    GregorianCalendar queue = null;
    Status status = null;
    DayNightHelper dayNightHelper;
    PhotoView dayNightImage;
    SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
    SunNavigationView sunNavigationView;
    DayLightImageTask dayLightImageTask;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_daynight, container, false);
        dayNightHelper = new DayNightHelper(getContext());

        status = new Status(0, 0, 0, new GregorianCalendar(), Status.ButtonChoice.MinuteOfDay);
        fmt.setTimeZone(status.getDate().getTimeZone());
        timeView = (TextView) root.findViewById(R.id.timeView);
        dayNightImage = (PhotoView) root.findViewById(R.id.daynightimage);
        sunNavigationView = root.findViewById(R.id.sunnavigationview);
        sunNavigationView.setOnFragmentInteractionListener(this, status, false);

        drawNewDate();

        return root;
    }

    protected String getStringFromDate() {
        String tz = status.getDate().getTimeZone().getDisplayName(status.getDate().getTimeZone().inDaylightTime(status.getDate().getTime()), TimeZone.SHORT);
        return fmt.format(status.getDate().getTime()) + "\n" +tz;
    }

    protected void setTimeView(String str) {
        timeView.setText(str);
    }

    protected void drawNewDate() {
        setTimeView(getStringFromDate());
        if (isRunning) {
            queue = status.getDate();
            return;
        }

        dayLightImageTask = new DayLightImageTask();
        dayLightImageTask.execute(status.getDate());

    }



    @Override
    public void onSunNavigationChange() {
        drawNewDate();
    }

    protected class DayLightImageTask extends AsyncTask<GregorianCalendar, Integer, Integer> {

        Bitmap outputBmp;
        @Override
        protected Integer doInBackground(GregorianCalendar... dates) {
            GregorianCalendar date = dates[0];
            outputBmp = dayNightHelper.getDayNightBmp(date);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            //do something
        }

        protected void onPreExecute() {
            isRunning = true;
        }

        protected void onPostExecute(Integer result) {
            Matrix previousImageMatrix = new Matrix();
            dayNightImage.getSuppMatrix(previousImageMatrix);
            dayNightImage.setImageBitmap(outputBmp);
            dayNightImage.setDisplayMatrix(previousImageMatrix);
            isRunning = false;

            if (queue != null) {
                new DayLightImageTask().execute((GregorianCalendar)queue.clone());
                queue = null;
            }
        }
    }

    @Override
    public void onDestroyView() {
        dayLightImageTask.cancel(true);
        dayNightHelper.destroy();
        super.onDestroyView();
    }
}