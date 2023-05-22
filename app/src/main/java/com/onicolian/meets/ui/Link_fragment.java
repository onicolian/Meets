package com.onicolian.meets.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.onicolian.meets.MainActivity;
import com.onicolian.meets.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

public class Link_fragment extends Fragment {
    com.google.api.services.calendar.Calendar mService;

    private EditText link;
    public Button add;
    public CheckBox checkBox;
    private ListView lv;
    private ArrayAdapter<String> adapter;

    public ArrayList<String> titleList = new ArrayList<>();
    public ArrayList<ArrayList<String[]>> listArrayList;
    public String[] Headers;
    public String[] Place;
    public Elements contentHeader;
    public Elements contentP;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_link,  container, false);

        mService = MainActivity.mService;
        link = (EditText) view.findViewById(R.id.link);
        lv = view.findViewById(R.id.listView);
        checkBox = view.findViewById(R.id.checkBox);
        add = view.findViewById(R.id.add);
        add.setOnClickListener(v -> {
            String enteredData = link.getText().toString();
            if (enteredData.isEmpty()) {
                Toast.makeText(getActivity().getApplicationContext(), "Please Enter the Data", Toast.LENGTH_SHORT).show();
            } else {
                new NewThread().execute();
                adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item, R.id.pro_item, titleList);
            }
        });

        return view;
    }

    public class NewThread extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... arg){
            Document doc;
            try {
                doc = Jsoup.connect(link.getText().toString()).get();

                contentHeader = doc.select("h2");
                Headers = new String[3];
                for (int j = 0; j < contentHeader.size(); j++) {
                    Element el = contentHeader.get(j);
                    String text = String.valueOf(el);
                    Headers[j] = text.substring(text.indexOf('>') + 1, text.indexOf("</"));
                }

                contentP = doc.select("p");
                Place = new String[3];
                for (int j = 0; j < contentP.size(); j++) {
                    Element el = contentP.get(j);
                    String text = String.valueOf(el);
                    Place[j] = text.substring(text.indexOf('>') + 1, text.indexOf("</"));
                }

                listArrayList = new ArrayList<>();
                for (Element contents: doc.select(".ttable")){

                    ArrayList<String[]> singleList = new ArrayList<>();
                    Elements rows = contents.select("tr");

                    for (int i = 0; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cols = row.select("td");
                        String[] arr = new String[3];
                        for (int j = 0; j < cols.size(); j++) {
                            Element el = cols.get(j);
                            String text = String.valueOf(el);
                            arr[j] = text.substring(text.indexOf('>') + 1, text.indexOf("</"));
                        }
                        singleList.add(arr);
                    }
                    listArrayList.add(singleList);
                }

                titleList.clear();
                for (ArrayList<String[]> contents: listArrayList){
                    for (String[] content: contents){
                        for (String con: content){
                            titleList.add(con);
                        }
                    }
                }
                for(int i = 0; i<listArrayList.size(); i++){
                    String day = "";
                    for(int j = 0; j < listArrayList.get(i).size(); j++){
                        if (listArrayList.get(i).get(j)[1] == null){
                            day = listArrayList.get(i).get(j)[0];
                        }
                        else{
                            Event event = new Event()
                                    .setSummary(listArrayList.get(i).get(j)[1])
                                    .setLocation(listArrayList.get(i).get(j)[2] + " " + Headers[i])
                                    .setDescription(Place[i]);

                            day = day.replace(",", "");
                            String[] sDay = day.split(" ");
                            String str = listArrayList.get(i).get(j)[0].replace(" ", "");
                            String[] time = str.split("[-–]");

                            java.util.Calendar calendar = new GregorianCalendar();
                            calendar.set(java.util.Calendar.YEAR, Integer.parseInt(sDay[3]) + 1);
                            calendar.set(java.util.Calendar.MONTH, 11);
                            calendar.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(sDay[2]));
                            calendar.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(time[0].split(":")[0]));
                            calendar.set(java.util.Calendar.MINUTE, Integer.parseInt(time[0].split(":")[1]));
                            Date d = calendar.getTime();

                            DateTime startDateTime = new DateTime(d);
                            EventDateTime start = new EventDateTime()
                                    .setDateTime(startDateTime)
                                    .setTimeZone("Europe/Moscow");
                            event.setStart(start);

                            if(time.length > 1) {
                                calendar = new GregorianCalendar();
                                calendar.set(java.util.Calendar.YEAR, Integer.parseInt(sDay[3]) + 1);
                                calendar.set(java.util.Calendar.MONTH, 11);
                                calendar.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(sDay[2]));
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(time[1].split(":")[0]));
                                calendar.set(java.util.Calendar.MINUTE, Integer.parseInt(time[1].split(":")[1]));
                                d = calendar.getTime();

                                DateTime endDateTime = new DateTime(d);
                                EventDateTime end = new EventDateTime()
                                        .setDateTime(endDateTime)
                                        .setTimeZone("Europe/Moscow");
                                event.setEnd(end);
                            }
                            else{
                                calendar = new GregorianCalendar();
                                calendar.set(java.util.Calendar.YEAR, Integer.parseInt(sDay[3]) + 1);
                                calendar.set(java.util.Calendar.MONTH, 11);
                                calendar.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(sDay[2]));
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(time[0].split(":")[0]));
                                calendar.set(java.util.Calendar.MINUTE, Integer.parseInt(time[0].split(":")[1]));
                                d = calendar.getTime();

                                DateTime endDateTime = new DateTime(d);
                                EventDateTime end = new EventDateTime()
                                        .setDateTime(endDateTime)
                                        .setTimeZone("Europe/Moscow");
                                event.setEnd(end);}

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
                        }
                    }
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        protected  void onPostExecute (String result){
            lv.setAdapter(adapter);
        }

    }
}
