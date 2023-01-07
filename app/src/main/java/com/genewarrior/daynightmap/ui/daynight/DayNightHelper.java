package com.genewarrior.daynightmap.ui.daynight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;
import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.genewarrior.daynightmap.R;
import com.genewarrior.sunlocator.app.ScriptC_daynightscript;
import com.sunlocator.solarpositioning.AzimuthZenithAngle;
import com.sunlocator.solarpositioning.DeltaT;

import java.util.GregorianCalendar;

import static com.sunlocator.solarpositioning.Grena3.calcT;
import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public class DayNightHelper {

    RenderScript rs;
    ScriptC_daynightscript daynightscript;

    Allocation mapBmpInput;

    Bitmap background;
    Drawable sunIcon;
    int sunIconSize;
    int width;
    int height;
    public DayNightHelper(Context context) {
        background = BitmapFactory.decodeResource(context.getResources(), R.drawable.map_of_the_world);
        sunIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.icon_sun, null);

        width = background.getWidth();
        height = background.getHeight();
        sunIconSize = height/5;
        rs = RenderScript.create(context, RenderScript.ContextType.NORMAL);
        daynightscript = new ScriptC_daynightscript(rs);

        mapBmpInput = Allocation.createFromBitmap(rs, background);
    }

    public Bitmap getDayNightBmp(GregorianCalendar date) {

        if (isDestroyed) {
            return null;
        }

        SolarElevationPreCalc preCalc = calculateSolarElevation_Step1(date);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmpOutput = Bitmap.createBitmap(background.getWidth(), background.getHeight(), conf);


        Allocation outputBmpAlloc = Allocation.createFromBitmap(rs, bmpOutput);

        daynightscript.set_alpha((float)preCalc.alpha); //right ascension
        daynightscript.set_delta((float)preCalc.delta); //declination
        daynightscript.set_h_pre((float)preCalc.H_pre);
        daynightscript.set_height(height);
        daynightscript.set_width(width);

        daynightscript.forEach_root(mapBmpInput, outputBmpAlloc);
        rs.finish();
        outputBmpAlloc.copyTo(bmpOutput);

        Bitmap overlay = background.copy(background.getConfig(), true);
        Canvas canvas = new Canvas(overlay);
        canvas.drawBitmap(bmpOutput, new Matrix(), null);


        double pseudoH = preCalc.H_pre-preCalc.alpha; //the hour angle for 0 longitude
        pseudoH = ((pseudoH + PI) % (2 * PI)) - PI;
        if (pseudoH < -PI) {
            pseudoH += 2 * PI;
        }

        int longitudePixel = (width/2)-(int)((pseudoH/(Math.PI))*(double)width/2);
        int latitudePixel = (height/2)-(int)((preCalc.delta/(Math.PI/2d))*(double)height/2);
        sunIcon.setBounds(longitudePixel-(sunIconSize/2), latitudePixel-(sunIconSize/2), longitudePixel+(sunIconSize/2), latitudePixel+(sunIconSize/2));
        sunIcon.draw(canvas);

        if (longitudePixel-(sunIconSize/2)<0) {
            sunIcon.setBounds(width+longitudePixel-(sunIconSize/2), latitudePixel-(sunIconSize/2), width+longitudePixel+(sunIconSize/2), latitudePixel+(sunIconSize/2));
            sunIcon.draw(canvas);
        } else if (longitudePixel+(sunIconSize/2)>width) {
            int bla = ((width-longitudePixel)*-1)-(sunIconSize/2);
            sunIcon.setBounds(bla, latitudePixel-(sunIconSize/2), bla+(sunIconSize), latitudePixel+(sunIconSize/2));

            sunIcon.draw(canvas);
        }
        outputBmpAlloc.destroy();

        return overlay;
    }

    boolean isDestroyed = false;
    public void destroy() {
        isDestroyed = true;
        mapBmpInput.destroy();
        daynightscript.destroy();
        rs.destroy();
    }


    /**
     * Modified from Grena3; no refraction correction
     * Step 1: non lat/lon dependent calculations
     *
     * @param date        Observer's local date and time.
     * @return Topocentric solar position (azimuth measured eastward from north)
     * @see AzimuthZenithAngle
     */
    public static SolarElevationPreCalc calculateSolarElevation_Step1(final GregorianCalendar date) {
        final double deltaT = DeltaT.estimate(date);
        final double t = calcT(date);
        final double tE = t + 1.1574e-5 * deltaT;
        final double omegaAtE = 0.0172019715 * tE;

        final double lambda = -1.388803 + 1.720279216e-2 * tE + 3.3366e-2 * sin(omegaAtE - 0.06172)
                + 3.53e-4 * sin(2.0 * omegaAtE - 0.1163);

        final double epsilon = 4.089567e-1 - 6.19e-9 * tE;

        final double sLambda = sin(lambda);
        final double cLambda = cos(lambda);
        final double sEpsilon = sin(epsilon);
        final double cEpsilon = sqrt(1 - sEpsilon * sEpsilon);

        double alpha = atan2(sLambda * cEpsilon, cLambda);
        if (alpha < 0) {
            alpha += 2 * PI;
        }

        final double delta = asin(sLambda * sEpsilon);

        double H_pre = 1.7528311 + 6.300388099 * t;


        return new SolarElevationPreCalc(alpha, delta, H_pre);
    }

    private static class SolarElevationPreCalc {
        double alpha, delta, H_pre;
        public SolarElevationPreCalc(double alpha, double delta, double H_pre) {
            this.alpha = alpha;
            this.delta = delta;
            this.H_pre = H_pre;
        }
    }
}
