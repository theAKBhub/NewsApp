package com.example.android.newsapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.DatePicker;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class SettingsActivity extends AppCompatActivity {

    public static final String LOG_TAG = SettingsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class NewsPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener, DatePickerDialog.OnDateSetListener {

        SharedPreferences preferences;
        Calendar mCalendar;
        private int mCurrentYear;
        private int mCurrentMonth;
        private int mCurrentDayofMonth;
        private String mToday;
        private String mYesterday;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Preference orderByPref = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderByPref);

            Preference fromDatePref = findPreference(getString(R.string.settings_from_date_key));

            // Get today's Date
            mCalendar = Calendar.getInstance();
            mCurrentYear = mCalendar.get(Calendar.YEAR);
            mCurrentMonth = mCalendar.get(Calendar.MONTH);
            mCurrentDayofMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
            mToday = dateFormat.format(mCalendar.getTime());

            // Get Yesterday's date
            mCalendar.add(Calendar.DATE, -1);
            mYesterday = dateFormat.format(mCalendar.getTime());

            // Always set default date to Yesterday's date when app is launched
            if (preferences.getLong(getString(R.string.settings_from_date_key), 0) == 0) {
                fromDatePref.setSummary(mYesterday);
            } else {
                long longPrefDate = preferences.getLong(
                        getString(R.string.settings_from_date_key), 0
                );

                Date dateObject = new Date(longPrefDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateObject);
                fromDatePref.setSummary(dateFormat.format(calendar.getTime()));
            }

            /** Set date picked from calendar as preferred date */
            fromDatePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

                            String datePickedFormatted = "";

                            // Set picked date on calendar; if launching for first time then it's set to today's date
                            mCalendar = Calendar.getInstance();
                            mCalendar.set(Calendar.YEAR, year);
                            mCalendar.set(Calendar.MONTH, monthOfYear);
                            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            Date datePicked = mCalendar.getTime();

                            datePickedFormatted = dateFormat.format(mCalendar.getTime());

                            if (datePickedFormatted.compareTo(mToday) < 0) {
                                Toast.makeText(getActivity(), getString(R.string.setting_older_date), Toast.LENGTH_LONG).show();
                            } else if (datePickedFormatted.compareTo(mToday) > 0) {
                                Toast.makeText(getActivity(), getString(R.string.setting_future_date), Toast.LENGTH_LONG).show();
                            }

                            preference.setSummary(datePickedFormatted);
                            preferences.edit().putLong(getString(R.string.settings_from_date_key), datePicked.getTime()).commit();

                        }
                    }, mCurrentYear, mCurrentMonth, mCurrentDayofMonth);

                    datePickerDialog.show();
                    return true;
                }
            });
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }


        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            // Default method overriden from parent class
        }

    }
}
