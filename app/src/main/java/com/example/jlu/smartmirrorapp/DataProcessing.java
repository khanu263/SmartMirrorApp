package com.example.jlu.smartmirrorapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thbs.skycons.library.CloudFogView;
import com.thbs.skycons.library.CloudHvRainView;
import com.thbs.skycons.library.CloudMoonView;
import com.thbs.skycons.library.CloudRainView;
import com.thbs.skycons.library.CloudSnowView;
import com.thbs.skycons.library.CloudSunView;
import com.thbs.skycons.library.CloudThunderView;
import com.thbs.skycons.library.CloudView;
import com.thbs.skycons.library.MoonView;
import com.thbs.skycons.library.SunView;
import com.thbs.skycons.library.WindView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
            String dailySummary = daily.getString("summary");

            String rawCurrentTemperature = currently.getString("temperature");
            String formattedCurrentTemperature = Integer.toString((int) Math.round(Double.valueOf(rawCurrentTemperature)));

            weatherArray[0] = currentWeatherIcon;
            weatherArray[1] = formattedCurrentTemperature;
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
        String selection = CalendarContract.Instances.BEGIN + " >= ?";
        String[] selectionArgs = new String[] {Long.toString(Calendar.getInstance().getTimeInMillis())};

        // Fetch a list of all calendars synced with the device, their display names and whether the
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(eventsUriBuilder, Long.MIN_VALUE);
        ContentUris.appendId(eventsUriBuilder, Long.MAX_VALUE);

        Uri eventsUri = eventsUriBuilder.build();
        cursor = contentResolver.query(eventsUri,
                (new String[] { CalendarContract.Instances.CALENDAR_ID, CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN }), selection, selectionArgs, CalendarContract.Instances.BEGIN + " ASC");

        try {

            if (cursor.getCount() > 0) {

                for (int i = 0; i < 5; i++) {

                    cursor.moveToPosition(i);

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

                    eventArray[i][0] = eventTitle;
                    eventArray[i][1] = eventTime;
                    eventArray[i][2] = eventDate;

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

    void createCloudView(Context context, LinearLayout weatherLayout) {

        CloudView cloudView = new CloudView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        cloudView.setLayoutParams(params);
        weatherLayout.addView(cloudView, 0);

    }

    void createSunView(Context context, LinearLayout weatherLayout) {

        SunView sunView = new SunView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        sunView.setLayoutParams(params);
        weatherLayout.addView(sunView, 0);

    }

    void createMoonView(Context context, LinearLayout weatherLayout) {

        MoonView moonView = new MoonView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        moonView.setLayoutParams(params);
        weatherLayout.addView(moonView, 0);

    }

    void createCloudSunView(Context context, LinearLayout weatherLayout) {

        CloudSunView cloudSunView = new CloudSunView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        cloudSunView.setLayoutParams(params);
        weatherLayout.addView(cloudSunView, 0);

    }

    void createCloudMoonView(Context context, LinearLayout weatherLayout) {

        CloudMoonView cloudMoonView = new CloudMoonView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        cloudMoonView.setLayoutParams(params);
        weatherLayout.addView(cloudMoonView, 0);

    }

    void createCloudHvRainView(Context context, LinearLayout weatherLayout) {

        CloudHvRainView cloudHvRainView = new CloudHvRainView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        cloudHvRainView.setLayoutParams(params);
        weatherLayout.addView(cloudHvRainView, 0);

    }

    void createCloudSnowView(Context context, LinearLayout weatherLayout) {

        CloudSnowView cloudSnowView = new CloudSnowView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        cloudSnowView.setLayoutParams(params);
        weatherLayout.addView(cloudSnowView, 0);

    }

    void createCloudRainView(Context context, LinearLayout weatherLayout) {

        CloudRainView cloudRainView = new CloudRainView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        cloudRainView.setLayoutParams(params);
        weatherLayout.addView(cloudRainView, 0);

    }

    void createCloudFogView(Context context, LinearLayout weatherLayout) {

        CloudFogView cloudFogView = new CloudFogView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        cloudFogView.setLayoutParams(params);
        weatherLayout.addView(cloudFogView, 0);

    }

    void createWindView(Context context, LinearLayout weatherLayout) {

        WindView windView = new WindView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        windView.setLayoutParams(params);
        weatherLayout.addView(windView, 0);

    }

    void createCloudThunderView(Context context, LinearLayout weatherLayout) {

        CloudThunderView thunderView = new CloudThunderView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.width = 175;
        params.height = 175;
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(90, 0 , 40, 0);

        thunderView.setLayoutParams(params);
        weatherLayout.addView(thunderView, 0);

    }

    public void updateNotifications(Context context, String[][] eventArray, String[] weatherArray, LinearLayout[] layouts, TextView[][] textViews, TextView notifications_default, TextView notifications_temperature, TextView notifications_summary) {

        // Delete Loading
        layouts[0].removeView(notifications_default);

        // Update layouts
        for (int i = 2; i <= 6; i++) {
            layouts[i].setVisibility(View.VISIBLE);
        }

        // Update Weather
        switch (weatherArray[0]) {
            case "clear-day":
                createSunView(context, layouts[1]);
                break;

            case "clear-night":
                createMoonView(context, layouts[1]);
                break;

            case "rain":
                createCloudRainView(context, layouts[1]);
                break;

            case "snow":
                createCloudSnowView(context, layouts[1]);
                break;

            case "sleet":
                createCloudHvRainView(context, layouts[1]);
                break;

            case "wind":
                createWindView(context, layouts[1]);
                break;

            case "fog":
                createCloudFogView(context, layouts[1]);
                break;

            case "cloudy":
                createCloudView(context, layouts[1]);
                break;

            case "partly-cloudy-day":
                createCloudSunView(context, layouts[1]);
                break;

            case "partly-cloudy-night":
                createCloudMoonView(context, layouts[1]);
                break;
        }

        notifications_temperature.setText(weatherArray[1] + "° F");
        notifications_summary.setText(weatherArray[2]);

        // Update Events
        for (int event = 0; event <= 4; event++) {

            for (int i = 0; i <= 2; i++) {
                textViews[event][i].setText(eventArray[event][i]);
            }

        }

    }
}
