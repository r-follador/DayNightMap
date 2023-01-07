package com.genewarrior.daynightmap.Widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.widget.RemoteViews;


import com.genewarrior.daynightmap.MainActivity;
import com.genewarrior.daynightmap.R;
import com.genewarrior.daynightmap.ui.daynight.DayNightHelper;

import java.util.GregorianCalendar;


/**
 * Implementation of App Widget functionality.
 */
public class DayNightWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //(context.getPackageName()+".DAY_NIGHT_WIDGET_UPDATE_INTENT")
        if ((context.getPackageName()+".DAY_NIGHT_WIDGET_UPDATE_INTENT").equals(intent.getAction())) {
            // Get the widget manager and ids for this widget provider, then call the shared
            // clock update method.
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] ids = appWidgetManager.getAppWidgetIds(thisAppWidget);

            DayNightHelper dayNightHelper = new DayNightHelper(context);
            Bitmap outputBmp = dayNightHelper.getDayNightBmp(new GregorianCalendar());


            for (int appWidgetID: ids) {
                updateAppWidget(context, appWidgetManager, appWidgetID, outputBmp);
            }
        }
    }

    private PendingIntent createDayNightTickIntent(Context context) {
        Intent intent = new Intent(context, DayNightWidget.class);

        intent.setAction(context.getPackageName()+".DAY_NIGHT_WIDGET_UPDATE_INTENT");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return pendingIntent;
    }


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bitmap bitmap) {
        // Construct the RemoteViews object
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_day_night);
            views.setImageViewBitmap(R.id.daynightimageWidget, bitmap);

            Intent mainactivityIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainactivityIntent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.daynightimageWidget, pendingIntent);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            e.printStackTrace();
            //Bla
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        DayNightHelper dayNightHelper = null;

        try {
            dayNightHelper = new DayNightHelper(context);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            System.gc();
            try {
                dayNightHelper = new DayNightHelper(context);
            } catch (OutOfMemoryError e2) {
                e2.printStackTrace();
            }
        }

        if (dayNightHelper == null)
            return;

        Bitmap outputBmp = dayNightHelper.getDayNightBmp(new GregorianCalendar());

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, outputBmp);
        }

        dayNightHelper.destroy();
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }



    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+1000, 5*60*1000, createDayNightTickIntent(context));
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createDayNightTickIntent(context));

    }
}

