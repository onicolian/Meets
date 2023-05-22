package com.onicolian.meets;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateActivity extends AppCompatActivity {

    com.google.api.services.calendar.Calendar mService;

    EditText editTextName;
    EditText editTextDeck;
    EditText editTextPlace;
    TextView editTextTime;
    CheckBox checkBox;
    Button button;

    Calendar dateAndTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        mService = MainActivity.mService;

        editTextName = findViewById(R.id.editTextName);
        editTextDeck = findViewById(R.id.editTextDeck);
        editTextTime = findViewById(R.id.currentDateTime);
        editTextPlace = findViewById(R.id.editTextPlace);
        checkBox = findViewById(R.id.checkBox);
        button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            new NewThread().execute();
            finish();
        });
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();

        if (intent.getStringExtra("name") != null){

            editTextName.setText(intent.getStringExtra("name"));
            editTextDeck.setText(intent.getStringExtra("deck"));
            editTextPlace.setText(intent.getStringExtra("place"));
            editTextTime.setText(intent.getStringExtra("day") + " 2023 г., " + intent.getStringExtra("time"));

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
            try {
                dateAndTime.setTime(sdf.parse(intent.getStringExtra("day") + " 2023 " + intent.getStringExtra("time")));// all done
            } catch (ParseException e) {
                e.printStackTrace();
            }

            System.out.println( "input: " + dateAndTime );
        }
        else{
            setInitialDateTime();
        }
    }

    // отображаем диалоговое окно для выбора даты
    public void setDate(View v) {
        new DatePickerDialog(CreateActivity.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }
    // отображаем диалоговое окно для выбора времени
    public void setTime(View v) {
        new TimePickerDialog(CreateActivity.this, t,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }
    // установка начальных даты и времени
    private void setInitialDateTime() {
        editTextTime.setText(DateUtils.formatDateTime(this,
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
    }
    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener t= (view, hourOfDay, minute) -> {
        dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateAndTime.set(Calendar.MINUTE, minute);
        setInitialDateTime();
    };
    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d= (view, year, monthOfYear, dayOfMonth) -> {
        dateAndTime.set(Calendar.YEAR, year);
        dateAndTime.set(Calendar.MONTH, monthOfYear);
        dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        setInitialDateTime();
    };

    public class NewThread extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... arg){
            Event event = new Event()
                    .setSummary(String.valueOf(editTextName.getText()))
                    .setLocation(String.valueOf(editTextPlace.getText()))
                    .setDescription(String.valueOf(editTextDeck.getText()));

            Date d = dateAndTime.getTime();

            DateTime startDateTime = new DateTime(d);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("Europe/Moscow");
            event.setStart(start);

            d = dateAndTime.getTime();

            DateTime endDateTime = new DateTime(d);
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Europe/Moscow");
            event.setEnd(end);

            if (checkBox.isChecked()){
                EventReminder[] reminderOverrides = new EventReminder[] {
                        new EventReminder().setMethod("email").setMinutes(24 * 60),
                        new EventReminder().setMethod("popup").setMinutes(10),
                };
                Event.Reminders reminders = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminderOverrides));
                event.setReminders(reminders);
            }

            String calendarId = "primary";
            try {
                mService.events().insert(calendarId, event).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}