package com.genewarrior.daynightmap;

import java.util.GregorianCalendar;

public class Status {

    public enum ButtonChoice {
        MinuteOfDay, DayOfYear
    };

    public GregorianCalendar getDate() {
        return date;
    }

    public void setDate(GregorianCalendar date) {
        this.date = date;
    }

    public Status(double latitude, double longitude, double altitude, GregorianCalendar date, ButtonChoice buttonChoice) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.date = date;
        this.buttonChoice = buttonChoice;
    }

    double latitude = 0;
    double longitude = 0;
    double altitude = 0;

    public ButtonChoice getButtonChoice() {
        return buttonChoice;
    }

    public void setButtonChoice(ButtonChoice buttonChoice) {
        this.buttonChoice = buttonChoice;
    }

    ButtonChoice buttonChoice = null;
    GregorianCalendar date = null;
}
