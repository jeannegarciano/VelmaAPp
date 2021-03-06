package com.thesis.velma;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.thesis.velma.apiclient.MyEvent;
import com.thesis.velma.helper.DataBaseHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class LandingActivity extends AppCompatActivity implements CalendarPickerController, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, ResultCallback<People.LoadPeopleResult> {

    //WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener

    GoogleApiClient google_api_client;
    GoogleApiAvailability google_api_availability;
    private static final int SIGN_IN_CODE = 0;
    private static final int PROFILE_PIC_SIZE = 120;
    private ConnectionResult connection_result;
    private boolean is_intent_inprogress;


    private boolean is_signInBtn_clicked;
    private int request_code;
    private FloatingActionButton fabButton;

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private static final int CREATE_EVENT = 0;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    FloatingActionButton fab;
    AgendaCalendarView mAgendaCalendarView;

    Calendar startdate, enddate;
    Context mcontext;
    final int CALENDAR_PERMISSION = 42;
    AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    public static DataBaseHandler db;
    private static final String[] LOCATION_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    static double origlatitude = 0, origlongitude = 0;

    private static final int LOCATION_REQUEST = 3;
    LocationManager locationManager;

    MaterialDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //buildNewGoogleApiClient();
        setContentView(R.layout.activity_activity_landing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAgendaCalendarView = (AgendaCalendarView) findViewById(R.id.agenda_calendar_view);
        mcontext = this;

        db = new DataBaseHandler(mcontext);


        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);


        fab = (FloatingActionButton) findViewById(R.id.fabButton);


        fab.setOnClickListener(this);


        LoadEvents();


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();

//            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
//                    .getCurrentPlace(google_api_client, null);
//            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
//                @Override
//                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
//                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                        Log.i(TAG, String.format("Place '%s' has likelihood: %g",
//                                placeLikelihood.getPlace().getName(),
//                                placeLikelihood.getLikelihood()));
//                    }
//                    likelyPlaces.release();
//                }
//            });

        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                    },
                    LOCATION_REQUEST);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {


            case LOCATION_REQUEST:

                getCurrentLocation();
                break;
        }
    }

    public void LoadEvents() {
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();

        List<CalendarEvent> eventList = new ArrayList<>();
        mockList(eventList);

        minDate.set(Calendar.DAY_OF_YEAR, 1);
        maxDate.add(Calendar.YEAR, 1);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        mAgendaCalendarView.init(eventList, minDate, maxDate, Locale.getDefault(), this);
    }

    public void getCurrentLocation() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {


            // Acquire a reference to the system Location Manager


            origlatitude = 10.3157007;
            origlongitude = 123.88544300000001;


// Define a listener that responds to location updates
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    //makeUseOfNewLocation(location);
                    Log.d("Latlng", "" + location);
                    origlatitude = location.getLatitude();
                    origlongitude = location.getLongitude();

                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

// Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


        } else {
            Toast.makeText(getBaseContext(), "Location Failed", Toast.LENGTH_SHORT).show();
        }
    }


    // region Interface - CalendarPickerController
    @Override
    public void onDaySelected(DayItem dayItem) {

    }

    @Override
    public void onEventSelected(final CalendarEvent event) {
        //  Log.d(LOG_TAG, String.format("Selected event: %s", event));
        //Toast.makeText(getBaseContext(), "" + event.getId(), Toast.LENGTH_SHORT).show();

        String content = "";
        Cursor c = db.getEventDetails(event.getId());

        while (c.moveToNext()) {

            content = "Descrition: " + c.getString(c.getColumnIndex("EventDescription")) + "\n" +
                    "Location: " + c.getString(c.getColumnIndex("EventLocation")) + "\n" +
                    "StartDate: " + c.getString(c.getColumnIndex("StartDate")) + "\n" +
                    "EndDate: " + c.getString(c.getColumnIndex("EndDate")) + "\n" +
                    "StartTime: " + c.getString(c.getColumnIndex("StartTime")) + "\n" +
                    "EndTime: " + c.getString(c.getColumnIndex("EndTime")) + "\n";

        }


        dialog = new MaterialDialog.Builder(this)
                .title(event.getTitle())
                .content(content)
                .positiveText("Ok")
                .negativeText("Delete")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        db.deleteEvent(event.getId());
                        dialog.dismiss();
                        LoadEvents();
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                    }
                })
                .show();


    }

    @Override
    public void onScrollToDate(Calendar calendar) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
        }
    }

    // endregion


    private void buildNewGoogleApiClient() {

        google_api_client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }




    @Override
    public void onConnected(Bundle arg0) {
        is_signInBtn_clicked = false;
        // Get user's information and set it into the layout

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        prefs.edit().putBoolean("isLoggedIn", true).commit();
//        Intent i = new Intent(LandingActivity.this, LandingActivity.class);
//        startActivity(i);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        google_api_client.connect();

    }

    private void resolveSignInError() {
        if (connection_result.hasResolution()) {
            try {
                is_intent_inprogress = true;
                connection_result.startResolutionForResult(this, SIGN_IN_CODE);
                Log.d("resolve error", "sign in error resolved");
            } catch (IntentSender.SendIntentException e) {
                is_intent_inprogress = false;
                google_api_client.connect();

            }
        }
    }


    @Override
    public void onClick(View view) {

        // Toast.makeText(getBaseContext(), "" + view, Toast.LENGTH_LONG).show();

        if (view == fab) {
            Intent intent = new Intent(LandingActivity.this, OnboardingActivity.class);
            startActivityForResult(intent, CREATE_EVENT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        // Check which request we're responding to
//        if (requestCode == SIGN_IN_CODE) {
//            request_code = requestCode;
//            if (responseCode != RESULT_OK) {
//                is_signInBtn_clicked = false;
//
//            }
//
//            is_intent_inprogress = false;
//
//            if (!google_api_client.isConnecting()) {
//                google_api_client.connect();
//            }
//        }

        if (requestCode == CREATE_EVENT) {

            //Toast.makeText(getBaseContext(), "Here0", Toast.LENGTH_SHORT).show();
            MyEvent myevent = null;

            if (responseCode == RESULT_OK) {


                Calendar minDate = Calendar.getInstance();
                Calendar maxDate = Calendar.getInstance();

                List<CalendarEvent> eventList = new ArrayList<>();
                mockList(eventList);

                minDate.set(Calendar.DAY_OF_YEAR, 1);
                maxDate.add(Calendar.YEAR, 1);

                mAgendaCalendarView.init(eventList, minDate, maxDate, Locale.getDefault(), this);


                //HARDCODED VALUES 10:51
//                Calendar calNow = Calendar.getInstance();
//                Calendar calSet = (Calendar) calNow.clone();
//
//                calSet.set(Calendar.HOUR_OF_DAY, 3);
//                calSet.set(Calendar.MINUTE, 50);
//                calSet.set(Calendar.SECOND, 0);
//                calSet.set(Calendar.MILLISECOND, 0);
//
//                setAlarm(calSet);

            }

        }

    }

    //region FUNCTIONS

    private void setAlarm(Calendar targetcal) {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetcal.getTimeInMillis(), pendingIntent);
    }


    private void mockList(List<CalendarEvent> eventList) {

        Cursor cursor = db.getEvents();

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyy");//yyyy-MM-dd HH:mm:ss
        String dateString1 = "", dateString2;
        Calendar sdate = null, edate = null;


        while (cursor.moveToNext()) {

            sdate = Calendar.getInstance();
            edate = Calendar.getInstance();

            dateString1 = cursor.getString(cursor.getColumnIndex("StartDate"));
            dateString2 = cursor.getString(cursor.getColumnIndex("EndDate"));


            try {
                sdate.setTime(formatter.parse(dateString1));// all done
                edate.setTime(formatter.parse(dateString2));// all done


            } catch (ParseException e) {
                e.printStackTrace();
                Log.d("MyDate", "Err" + e.getMessage());
            }

            Log.d("MyDate", "" + sdate.get(Calendar.DATE));


//            public BaseCalendarEvent( long id, int color, String title,
//                    String description, String location,long dateStart, long dateEnd,
//            int allDay, String duration){

            Random rnd = new Random();

            int color = Color.argb(225, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

            BaseCalendarEvent event1 = new BaseCalendarEvent(
                    cursor.getInt(cursor.getColumnIndex("_id")),
                    color,
                    cursor.getString(cursor.getColumnIndex("EventName")),
                    cursor.getString(cursor.getColumnIndex("EventDescription")),
                    cursor.getString(cursor.getColumnIndex("EventLocation")),
                    sdate.getTimeInMillis(), edate.getTimeInMillis(), 0, "No");
            //BaseCalendarEvent event1 = new BaseCalendarEvent(cursor.getString(cursor.getColumnIndex("EventName")), cursor.getString(cursor.getColumnIndex("EventDescription")),
            //    "",
            //    ContextCompat.getColor(this, R.color.blue_selected), sdate, edate, true);
            eventList.add(event1);


        }

    }

    //endregion

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    protected String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
    }


    private void gPlusSignOut() {
        if (google_api_client.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(google_api_client);
            google_api_client.disconnect();


//                Auth.GoogleSignInApi.signOut(google_api_client).setResultCallback(
//                        new ResultCallback<Status>() {
//                            @Override
//                            public void onResult(Status status) {
////                                Plus.AccountApi.clearDefaultAccount(google_api_client);
////                                google_api_client.disconnect();
//                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//                                prefs.edit().putBoolean("isLoggedIn", false).commit();
//                                Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
//                                startActivity(intent);
//                            }
//                        });

        }
    }

//    private void gPlusSignOut() {
////        if (google_api_client.isConnected()) {
////            Plus.AccountApi.clearDefaultAccount(google_api_client);
////            google_api_client.disconnect();
////            google_api_client.connect();
////
////        }
////    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        if (!connectionResult.hasResolution()) {
            google_api_availability.getErrorDialog(this, connectionResult.getErrorCode(), request_code).show();
            return;
        }

        if (!is_intent_inprogress) {

//            connection_result = result;

//            if (is_signInBtn_clicked) {
//
//                resolveSignInError();
//            }
        }

    }


    @Override
    public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {

    }
}