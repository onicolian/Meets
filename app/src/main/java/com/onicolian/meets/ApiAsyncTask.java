package com.onicolian.meets;

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.onicolian.meets.ui.Home_fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private static Home_fragment mActivity;

    public ApiAsyncTask(Home_fragment activity) {
        this.mActivity = activity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            mActivity.clearResultsText();
            mActivity.updateResultsText(getDataFromApi());

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());

        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    Home_fragment.REQUEST_AUTHORIZATION);

        } catch (IOException e) {
            mActivity.updateStatus("The following error occurred: " +
                    e.getMessage());
        }
        return null;
    }

    private List<String> getDataFromApi() throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        List<String> eventStrings = new ArrayList<String>();
        Events events = MainActivity.mService.events().list("primary")
                //.setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            String dt = String.valueOf(start);

            String[] datetime = dt.split("T");

            String date = datetime[0];
            String time = datetime[1].split("\\.")[0];

            if (start == null) {
                start = event.getStart().getDate();
            }
            String place = event.getLocation();
            eventStrings.add(
                    String.format("%s at %s \n %s %s ", event.getSummary(), place, date, time));
        }
        return eventStrings;
    }
}