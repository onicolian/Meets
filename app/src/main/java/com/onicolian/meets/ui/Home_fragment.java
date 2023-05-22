package com.onicolian.meets.ui;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.onicolian.meets.ApiAsyncTask;
import com.onicolian.meets.CreateActivity;
import com.onicolian.meets.MainActivity;
import com.onicolian.meets.R;

import java.util.List;

public class Home_fragment extends Fragment {

    public static com.google.api.services.calendar.Calendar mService;
    GoogleAccountCredential credential;

    private TextView mStatusText;
    private TextView mResultsText;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    public static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);

        credential = MainActivity.credential;
        mService = MainActivity.mService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,  container, false);

        mStatusText = view.findViewById(R.id.mStatusText);
        mResultsText = view.findViewById(R.id.mResultsText);

        Button button_text = view.findViewById(R.id.button_text);
        button_text.setOnClickListener(arg0 -> create());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            mStatusText.setText("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }
    }

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        MainActivity.credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        refreshResults();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    mStatusText.setText("Аккаунт не выбран.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void create() {
         Intent intent = new Intent(getActivity(), CreateActivity.class);
        startActivity(intent);
    }

    private void refreshResults() {
        if (MainActivity.credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new ApiAsyncTask(this).execute();
            } else {
                mStatusText.setText("Нет интернет соединения.");
            }
        }
    }

    public void clearResultsText() {
        getActivity().runOnUiThread(() -> {
            mStatusText.setText("Получение данных…");
            mResultsText.setText("");
        });
    }

    public void updateResultsText(final List<String> dataStrings) {
        getActivity().runOnUiThread(() -> {
            if (dataStrings == null) {
                mStatusText.setText("Ошибка при получении данных!");
            } else if (dataStrings.size() == 0) {
                mStatusText.setText("Данные не найдены.");
            } else {
                mStatusText.setText("Ближайшие события из вашего Google Calendar:");
                mResultsText.setText(TextUtils.join("\n\n", dataStrings));
            }
        });
    }

    public void updateStatus(final String message) {
        getActivity().runOnUiThread(() -> mStatusText.setText(message));
    }

    private void chooseAccount() {
        startActivityForResult(
                MainActivity.credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        getActivity().runOnUiThread(() -> {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                    connectionStatusCode,
                    getActivity(),
                    REQUEST_GOOGLE_PLAY_SERVICES);
            dialog.show();
        });
    }



}
