package metro.ourthingsee.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import metro.ourthingsee.OurContract;
import metro.ourthingsee.R;
import metro.ourthingsee.RESTObjects.Events;
import metro.ourthingsee.Utils;
import metro.ourthingsee.activities.MainActivity;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Usin on 22-Feb-17.
 * Display a widget with Humidity, temperature and luminance.
 * The widget is automatically update every 30min, which is stored in {@link metro.ourthingsee.R.xml#widget_myhome_info}
 * Each time the widget update, it will fetch data from the internet.
 */
public class MyHomeWidgetProviderSmall extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_myhome);
            SharedPreferences prefs = context.getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
            String authToken = prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "");
            String authId = prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "");
            if (!(authToken.equals("") || authId.equals(""))) {
                fetchData(context, remoteViews, appWidgetManager, widgetId);

                // Click on the widget will open the activity
                Intent myhomeIntent = new Intent(context, MainActivity.class);
                myhomeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent myhomePI = PendingIntent.getActivity(context, 0, myhomeIntent, 0);
                remoteViews.setOnClickPendingIntent(R.id.lnlWGMainLayout, myhomePI);

                Intent intent = new Intent(context, MyHomeWidgetProviderSmall.class);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.imgRefresh, pendingIntent);
            } else {
                setDefaultData(context, remoteViews);
            }
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private void setDefaultData(Context context, RemoteViews remoteViews) {
        remoteViews.setTextViewText(R.id.txtWGHumid, context.getString(R.string.no_data));
        remoteViews.setTextViewText(R.id.txtWGTemp, context.getString(R.string.no_data));
        remoteViews.setTextViewText(R.id.txtWGLight, context.getString(R.string.no_data));
        remoteViews.setTextViewText(R.id.txtWGTime, context.getString(R.string.no_data));
        remoteViews.setOnClickPendingIntent(R.id.lnlWGMainLayout, null);
        remoteViews.setOnClickPendingIntent(R.id.imgRefresh, null);
    }

    /**
     * Fetch Data from Thingsee cloud, we will fetch each data each time, and update accordingly.
     * If we fetch all data at the same time, we still need to put "appWidgetManager.updateAppWidget(widgetId, remoteViews);"
     * to the onResponse method so it can update (since the fetchData is not running on main thread,
     * it is impossible to know when it finish).
     * The advantage of up date each data each time, is that is take at least 0.5s, so user can sometime
     * see the progress bar show up.
     *
     * @param context          our app context
     * @param remoteViews      the widget views that get update
     * @param appWidgetManager The manager to update the widget views
     * @param widgetId         to know which widget is updated
     */

    private void fetchData(Context context, final RemoteViews remoteViews,
                           final AppWidgetManager appWidgetManager, final int widgetId) {
        remoteViews.setViewVisibility(R.id.imgRefresh, View.GONE);
        remoteViews.setViewVisibility(R.id.pgbWidget, View.VISIBLE);
        appWidgetManager.updateAppWidget(widgetId, remoteViews);

        fetchHumid(context, remoteViews, appWidgetManager, widgetId);
    }

    /**
     * Refer to {@link #fetchData(Context, RemoteViews, AppWidgetManager, int)}
     * We will update the value, and also timestamp in the widget is based on humid timestamp
     *
     * @param context          our app context
     * @param remoteViews      the widget views that get update
     * @param appWidgetManager The manager to update the widget views
     * @param widgetId         to know which widget is updated
     */

    private void fetchHumid(final Context context, final RemoteViews remoteViews,
                            final AppWidgetManager appWidgetManager, final int widgetId) {
        final SharedPreferences prefs = context.getSharedPreferences(OurContract.SHARED_PREF,
                Context.MODE_PRIVATE);
        APIService apiService = AppUtils.getAPIService();
        apiService.getUserEvents(
                "Bearer " + prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, ""),
                prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""),
                null, OurContract.SENSOR_ID_HUMIDITY, OurContract.MIN_FETCH_ITEM_TC, null, null).
                enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        Utils.handleOnResponse(response, prefs);
                        String strHumid = prefs.getString(
                                OurContract.PREF_HUMID_LATEST_VALUE, " ");

                        // Remove the unit
                        strHumid = strHumid.replaceAll("%", "");
                        try {
                            // Convert it back to double
                            Double dbHumid = Double.parseDouble(strHumid);
                        /*Subtitle text for Catalog display, price and quantity
                        http://stackoverflow.com/questions/3656371/dynamic-string-using-string-xml
                        String format replacement markers in xml are in the form of
                            %[parameter index]$[flags][width][.precision]conversion
                        format type: There are a lot of ways that you can format things (see the documentation).
                            http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html*/
                            remoteViews.setTextViewText(R.id.txtWGHumid,
                                    String.format(Locale.US, "%.0f", dbHumid) + "%");
                        } catch (Exception e) {
                            remoteViews.setTextViewText(R.id.txtWGHumid,
                                    context.getString(R.string.no_data));
                        }

                        String longDateString = prefs.getString(
                                OurContract.PREF_HUMID_LATEST_TIME, " ");
                        // Convert long date stored in prefs to short day
                        try {
                            Date date = Utils.dateFormat.parse(longDateString);
                            remoteViews.setTextViewText(R.id.txtWGTime,
                                    Utils.shortDateFormat.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        appWidgetManager.updateAppWidget(widgetId, remoteViews);

                        fetchTemp(context, remoteViews, appWidgetManager, widgetId);
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        Utils.handleFailure(context, t);
                        remoteViews.setViewVisibility(R.id.imgRefresh, View.VISIBLE);
                        remoteViews.setViewVisibility(R.id.pgbWidget, View.GONE);
                        appWidgetManager.updateAppWidget(widgetId, remoteViews);
                    }
                });
    }

    /**
     * Refer to {@link #fetchData(Context, RemoteViews, AppWidgetManager, int)}
     *
     * @param context          our app context
     * @param remoteViews      the widget views that get update
     * @param appWidgetManager The manager to update the widget views
     * @param widgetId         to know which widget is updated
     */
    private void fetchTemp(final Context context, final RemoteViews remoteViews,
                           final AppWidgetManager appWidgetManager, final int widgetId) {
        final SharedPreferences prefs = context.getSharedPreferences(OurContract.SHARED_PREF,
                Context.MODE_PRIVATE);
        APIService apiService = AppUtils.getAPIService();
        apiService.getUserEvents(
                "Bearer " + prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, ""),
                prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""),
                null, OurContract.SENSOR_ID_HUMIDITY, OurContract.MIN_FETCH_ITEM_TC, null, null).
                enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        Utils.handleOnResponse(response, prefs);

                        String strTemp = prefs.getString(
                                OurContract.PREF_TEMP_LATEST_VALUE, " ");
                        // Remove the unit
                        strTemp = strTemp.replaceAll("\u2103", "");
                        // Convert it back to double
                        try {
                            Double dbTemp = Double.parseDouble(strTemp);
                            remoteViews.setTextViewText(R.id.txtWGTemp,
                                    String.format(Locale.US, "%.0f", dbTemp) + "\u2103");
                        } catch (Exception e) {
                            remoteViews.setTextViewText(R.id.txtWGTemp,
                                    context.getString(R.string.no_data));
                        }
                        String longDateString = prefs.getString(
                                OurContract.PREF_TEMP_LATEST_TIME, " ");
                        // Convert long date stored in prefs to short day
                        try {
                            Date date = Utils.dateFormat.parse(longDateString);
                            remoteViews.setTextViewText(R.id.txtWGTime,
                                    Utils.shortDateFormat.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        appWidgetManager.updateAppWidget(widgetId, remoteViews);

                        fetchLight(context, remoteViews, appWidgetManager, widgetId);
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        Utils.handleFailure(context, t);
                        remoteViews.setViewVisibility(R.id.imgRefresh, View.VISIBLE);
                        remoteViews.setViewVisibility(R.id.pgbWidget, View.GONE);
                        appWidgetManager.updateAppWidget(widgetId, remoteViews);
                    }
                });
    }

    /**
     * Refer to {@link #fetchData(Context, RemoteViews, AppWidgetManager, int)}
     * After the luminance data updated, we need to make the progress bar gone, and display the
     * refresh image again.
     *
     * @param context          our app context
     * @param remoteViews      the widget views that get update
     * @param appWidgetManager The manager to update the widget views
     * @param widgetId         to know which widget is updated
     */
    private void fetchLight(final Context context, final RemoteViews remoteViews,
                            final AppWidgetManager appWidgetManager, final int widgetId) {
        final SharedPreferences prefs = context.getSharedPreferences(OurContract.SHARED_PREF,
                Context.MODE_PRIVATE);
        APIService apiService = AppUtils.getAPIService();
        apiService.getUserEvents(
                "Bearer " + prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, ""),
                prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""),
                null, OurContract.SENSOR_ID_HUMIDITY, OurContract.MIN_FETCH_ITEM_TC, null, null).
                enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        Utils.handleOnResponse(response, prefs);

                        String strLight = prefs.getString(
                                OurContract.PREF_LIGHT_LATEST_VALUE, " ");
                        // Remove the unit
                        strLight = strLight.replaceAll("lux", "");
                        try {
                            // Convert it back to double
                            Double dbLight = Double.parseDouble(strLight);
                            remoteViews.setTextViewText(R.id.txtWGLight,
                                    String.format(Locale.US, "%.0f", dbLight) + "lx");
                        } catch (Exception e) {
                            remoteViews.setTextViewText(R.id.txtWGLight,
                                    context.getString(R.string.no_data));
                        }
                        String longDateString = prefs.getString(
                                OurContract.PREF_LIGHT_LATEST_TIME, " ");
                        // Convert long date stored in prefs to short day
                        try {
                            Date date = Utils.dateFormat.parse(longDateString);
                            remoteViews.setTextViewText(R.id.txtWGTime,
                                    Utils.shortDateFormat.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        remoteViews.setViewVisibility(R.id.imgRefresh, View.VISIBLE);
                        remoteViews.setViewVisibility(R.id.pgbWidget, View.GONE);
                        appWidgetManager.updateAppWidget(widgetId, remoteViews);
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        Utils.handleFailure(context, t);
                        remoteViews.setViewVisibility(R.id.imgRefresh, View.VISIBLE);
                        remoteViews.setViewVisibility(R.id.pgbWidget, View.GONE);
                        appWidgetManager.updateAppWidget(widgetId, remoteViews);
                    }
                });
    }
}
