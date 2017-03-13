package com.example.jlu.smartmirrorapp;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by khanu263 on 3/13/2017.
 * File for all data processing functions.
 */
public class DataProcessing {

    public String AMorPM(int hourOfDay, Context ctx) {
        // Returns string "am" or "pm"
        String am_pm;

        if (hourOfDay >= 12) {
            am_pm = ctx.getResources().getString(R.string.period_pm);
        }

        else {
            am_pm = ctx.getResources().getString(R.string.period_am);
        }

        return am_pm;

    }

    public String getGreeting(int hourOfDay, Context ctx) {
        // Returns string "morning", "afternoon", or "evening"
        String greeting;

        if (hourOfDay >= 4 && hourOfDay < 12) {
            greeting = ctx.getResources().getString(R.string.greeting_morning);
        }

        else if (hourOfDay >= 12 && hourOfDay < 17) {
            greeting = ctx.getResources().getString(R.string.greeting_afternoon);
        }

        else {
            greeting = ctx.getResources().getString(R.string.greeting_evening);
        }

        return greeting;

    }

    public String getDateString() {
        // Returns date string
        Calendar sCalendar = Calendar.getInstance();
        String dayOfWeek = sCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

        SimpleDateFormat month_name = new SimpleDateFormat("MMMM");
        String monthName = month_name.format(sCalendar.getTime());

        SimpleDateFormat month_day = new SimpleDateFormat("dd");
        String dayOfMonth = month_day.format(sCalendar.getTime());

        String dateString = dayOfWeek + ", " + monthName + " " + dayOfMonth;

        return dateString;
    }

    public void updateLockscreen(TextView greeting_text, TextView period_text, TextView date_text, TextView time_text, Context ctx) {
        // updates screen with latest time/date info

        // get specific time of day info
        int hourOfDayUnformatted = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int hourOfDayFormatted = 0;

        if (hourOfDayUnformatted == 0) {
            hourOfDayFormatted = 12;
        }

        else if (hourOfDayUnformatted > 12) {
            hourOfDayFormatted = hourOfDayUnformatted - 12;
        }

        else {
            hourOfDayFormatted = hourOfDayUnformatted;
        }

        int minuteOfDay = Calendar.getInstance().get(Calendar.MINUTE);
        String timeOfDay = Integer.toString(hourOfDayFormatted) + "." + String.format("%02d", minuteOfDay);
        Log.d("test", Integer.toString(hourOfDayFormatted));
        Log.d("test", Integer.toString(hourOfDayUnformatted));

        // get general time of day info
        String am_pm = AMorPM(hourOfDayUnformatted, ctx);
        String greeting = getGreeting(hourOfDayUnformatted, ctx);
        String dateString = getDateString();

        // set text values
        period_text.setText(am_pm);
        greeting_text.setText(greeting);
        date_text.setText(dateString);
        time_text.setText(timeOfDay);

    }

}
