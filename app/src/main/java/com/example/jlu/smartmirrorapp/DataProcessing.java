package com.example.jlu.smartmirrorapp;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public String getMinimizedDateString() {
        // Returns date string in MM/DD/YYYY format
        Calendar sCalendar = Calendar.getInstance();
        SimpleDateFormat minimizedFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        String minimizedDate = minimizedFormat.format(sCalendar.getTime());
        return minimizedDate;
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

    public void updateMinimizedLockscreen(TextView minimized_date, TextView minimized_time, Context ctx) {
        // updates date and time in minimized format

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
        String am_pm = AMorPM(hourOfDayUnformatted, ctx);

        // finalize strings
        String minimizedTime = timeOfDay + " " + am_pm.toUpperCase();
        String minimizedDate = getMinimizedDateString();

        // set text values
        minimized_date.setText(minimizedDate);
        minimized_time.setText(minimizedTime);

    }

    public String[][] parseHeadlineJSON(String JSONString, Context ctx) {

        String[][] headlineArray = new String[][] { {"1", "headline", "date", "imageURL"} , {"2", "headline", "date", "imageURL"} , {"3", "headline", "date", "imageURL"} , {"4", "headline", "date", "imageURL"} , {"5", "headline", "date", "imageURL"} };

        try {

            JSONObject returnedData = new JSONObject(JSONString);
            JSONArray articles = returnedData.getJSONArray("articles");

            for (int i = 0; i < 5; i++) {

                // get article and set title and url
                JSONObject article = articles.getJSONObject(i);
                headlineArray[i][1] = article.getString("title");
                headlineArray[i][3] = article.getString("urlToImage");

                // format given date and time into 'published on MM/DD/YYYY at XX:XX UTC'
                String rawPublication = article.getString("publishedAt");
                String rawDate = rawPublication.substring(0, 10);
                String rawTime = rawPublication.substring(11, 16);

                String formattedDate = rawDate.substring(5, 7) + "/" + rawDate.substring(8) + "/" + rawDate.substring(0, 4);

                int unformattedHour = Integer.valueOf(rawTime.substring(0, 2));
                int formattedHour;

                if (unformattedHour == 0) {
                    formattedHour = 12;
                } else if (unformattedHour > 12) {
                    formattedHour = unformattedHour - 12;
                } else {
                    formattedHour = unformattedHour;
                }

                String period = AMorPM(unformattedHour, ctx);
                String formattedTime = Integer.toString(formattedHour) + rawTime.substring(2, 5) + " " + period.toUpperCase() + " UTC";

                String formattedPublication = "published on " + formattedDate + " at " + formattedTime;

                // insert formatted publication into array
                headlineArray[i][2] = formattedPublication;

            }

        } catch (JSONException e) {
            Log.e("ERROR", e.getMessage());
            return headlineArray;
        }


        return headlineArray;

    }

}
