package com.example.jlu.smartmirrorapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    /*
    public String[][] getCalendar(Context ctx) {

        // Initialize return array
        String[][] eventArray = new String[5][4];

        final String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };

        // The indices for the projection array above.
        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
        final int PROJECTION_DISPLAY_NAME_INDEX = 2;
        final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

        // Run query
        Cursor cursor = null;
        ContentResolver resolver = ctx.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";

        String[] selectionArgs = new String[]{"florence.soucy.321@gmail.com", "com.gmail",
                "florence.soucy.321@gmail.com"};

        // Submit the query and get a Cursor object back.
        try {

            cursor = resolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
            Log.v(TAG, "get cursor");

        } catch (Exception IllegalArgumentException) {
            Log.e("error", "Permission error");
            Log.v(TAG, "triggered permission error");
        }

        // Use the cursor to step through the returned records
        while (cursor.moveToNext()) {

            Log.v(TAG, "Got inside while loop");

            long calendarID = 0;
            Log.v(TAG, "Set calendar ID");
            String displayName = null;
            Log.v(TAG, "Set display name");
            String accountName = null;
            Log.v(TAG, "Set account name");
            String ownerName = null;
            Log.v(TAG, "Set owner name");

            // Get the field values
            calendarID = cursor.getLong(PROJECTION_ID_INDEX);
            Log.v(TAG, "Got calendar ID");
            displayName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX);
            Log.v(TAG, "Got display name");
            accountName = cursor.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            Log.v(TAG, "Got account name");
            ownerName = cursor.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
            Log.v(TAG, "Got owner name");

            Log.v(TAG, String.format("calendar ID = %d", calendarID));
            Log.v(TAG, displayName);
            Log.v(TAG, accountName);
            Log.v(TAG, ownerName);

            // Get Events
        }

        Log.v(TAG, "Made it past while loop");

        return eventArray;
    }
    */

    static Cursor cursor;

    public static void readCalendar(Context context) {

        ContentResolver contentResolver = context.getContentResolver();

        // Fetch a list of all calendars synced with the device, their display names and whether the

        cursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"),
                (new String[] {"calendar_id", "title", "dtstart"}), null, null, null);

        try {
            System.out.println("Count="+cursor.getCount());

            if (cursor.getCount() > 0) {

                while (cursor.moveToNext()) {

                    String _id = cursor.getString(0);
                    String eventTitle = cursor.getString(1);
                    String dateTimeStart = cursor.getString(2);

                    Log.d("ID", _id);
                    Log.d("Event Title", eventTitle);
                    Log.d("Date Time", dateTimeStart);
                }
            }

        } catch(AssertionError ex) {
            ex.printStackTrace();
            Log.d("ERROR", "Assertion Error");
        } catch(Exception e) {
            e.printStackTrace();
            Log.d("EXCEPTION", "Exception");
        }

    }
}
