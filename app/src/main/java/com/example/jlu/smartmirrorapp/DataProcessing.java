package com.example.jlu.smartmirrorapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import  java.util.Calendar;

/**
 * Created by khanu263 on 3/13/2017.
 * File for all data processing functions.
 */
public class DataProcessing {

    // Log variables
    protected static final String TAG = "cal";

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

    public int stringToInt(String str) {
        // Converts string to an integer
        int number = (int) Math.round( Double.parseDouble(str) );
        return number;
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

    public String[] parseWeatherJSON(String JSONString, Context ctx) {
        // Parses weather string object returned by darksky API

        // Array
        String[] weatherArray = new String[3];

        // Parse JSON Object
        try {

            JSONObject returnedData = new JSONObject(JSONString);

            JSONObject currently = returnedData.getJSONObject("currently");
            JSONObject daily = returnedData.getJSONObject("hourly");

            String currentWeatherIcon = currently.getString("icon");
            String rawCurrentTemperature = currently.getString("temperature");
            String dailySummary = daily.getString("summary");

            weatherArray[0] = currentWeatherIcon;
            weatherArray[1] = rawCurrentTemperature;
            weatherArray[2] = dailySummary;

        }

        catch (JSONException e) {
            Log.e("noot", e.getMessage());
            return weatherArray;
        }

        return weatherArray;

    }

    public void displayHeadlines(String[][] headlineArray, LinearLayout fullscreen_content, TextView news_attribution, TextView news_default, TextView headline_1, TextView date_1, TextView headline_2, TextView date_2, TextView headline_3, TextView date_3, TextView headline_4, TextView date_4, TextView headline_5, TextView date_5, Context ctx) {

        // remove loading message
        fullscreen_content.removeView(news_default);

        // set new margin for attribution
        LinearLayout.LayoutParams attribution_margins = (LinearLayout.LayoutParams) news_attribution.getLayoutParams();
        attribution_margins.setMargins(attribution_margins.leftMargin, attribution_margins.topMargin, attribution_margins.rightMargin, 60);
        news_attribution.setLayoutParams(attribution_margins);

        // loop through array and set text
        for (int i = 0; i < 5; i++) {

            String headlineViewName = "headline_" + (i + 1);
            String dateViewName = "date_" + (i + 1);

            String headlineText = headlineArray[i][1];
            String dateText = headlineArray[i][2];

            if (ctx.getResources().getResourceEntryName(headline_1.getId()).equals(headlineViewName)) {
                headline_1.setText(headlineText);
            } else if (ctx.getResources().getResourceEntryName(headline_2.getId()).equals(headlineViewName)) {
                headline_2.setText(headlineText);
            } else if (ctx.getResources().getResourceEntryName(headline_3.getId()).equals(headlineViewName)) {
                headline_3.setText(headlineText);
            } else if (ctx.getResources().getResourceEntryName(headline_4.getId()).equals(headlineViewName)) {
                headline_4.setText(headlineText);
            } else if (ctx.getResources().getResourceEntryName(headline_5.getId()).equals(headlineViewName)) {
                headline_5.setText(headlineText);
            }

            if (ctx.getResources().getResourceEntryName(date_1.getId()).equals(dateViewName)) {
                date_1.setText(dateText);
            } else if (ctx.getResources().getResourceEntryName(date_2.getId()).equals(dateViewName)) {
                date_2.setText(dateText);
            } else if (ctx.getResources().getResourceEntryName(date_3.getId()).equals(dateViewName)) {
                date_3.setText(dateText);
            } else if (ctx.getResources().getResourceEntryName(date_4.getId()).equals(dateViewName)) {
                date_4.setText(dateText);
            } else if (ctx.getResources().getResourceEntryName(date_5.getId()).equals(dateViewName)) {
                date_5.setText(dateText);
            }

        }

    }

    static Cursor cursor;

    public String[][] readCalendar(Context context) {

        String[][] eventArray = new String[5][3];

        ContentResolver contentResolver = context.getContentResolver();

        // Selection Arguments
        String selection = CalendarContract.Events.DTSTART + " >= ?";
        String[] selectionArgs = new String[] {Long.toString(Calendar.getInstance().getTimeInMillis())};

        // Fetch a list of all calendars synced with the device, their display names and whether the

        cursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"),
                (new String[] {"calendar_id", "title", "dtstart"}), null, null, null);

        try {

            if (cursor.getCount() > 0) {

                for (int i = 13; i < 18; i++) {

                    cursor.moveToNext();

                    String eventTitle = cursor.getString(1);
                    String epochTime = cursor.getString(2);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH.mm");
                    String eventRawTime = dateFormat.format(new Date(Long.valueOf(epochTime)));

                    int hourUnformatted = Integer.valueOf(eventRawTime.substring(6, 8));
                    int hourFormatted = 0;

                    if (hourUnformatted == 0) {
                        hourFormatted = 12;
                    } else if (hourUnformatted > 12) {
                        hourFormatted = hourUnformatted - 12;
                    } else {
                        hourFormatted = hourUnformatted;
                    }

                    String period = AMorPM(hourUnformatted, context);

                    String eventTime = Integer.toString(hourFormatted) + eventRawTime.substring(8) + " " + period;
                    String eventDate = eventRawTime.substring(0, 6);

                    eventArray[i - 13][0] = eventTitle;
                    eventArray[i - 13][1] = eventTime;
                    eventArray[i - 13][2] = eventDate;

                }
            }

        } catch(AssertionError ex) {
            ex.printStackTrace();
            Log.d("ERROR", "Assertion Error");
        } catch(Exception e) {
            e.printStackTrace();
            Log.d("EXCEPTION", "Exception");
        }

        return eventArray;

    }

    public void updateNotifications(Context context, String[][] eventArray, String[] weatherArray, LinearLayout[] layouts, TextView[][] textViews, TextView notifications_default, TextView notifications_temperature, TextView notifications_summary) {

        // Delete Loading
        layouts[0].removeView(notifications_default);

        // Update layouts
        for (int i = 2; i <= 6; i++) {
            layouts[i].setVisibility(View.VISIBLE);
        }

        // Update Weather


        notifications_temperature.setText(weatherArray[1] + "° F");
        notifications_summary.setText(weatherArray[2]);

        // Update Events
        for (int event = 0; event <= 4; event++) {

            for (int i = 0; i <= 2; i++) {

                Log.d("INFO", "eventArray[event][i]: " + eventArray[event][i]);
                textViews[event][i].setText(eventArray[event][i]);
            }

        }

    }
}
