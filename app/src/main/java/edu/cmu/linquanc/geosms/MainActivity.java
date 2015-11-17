package edu.cmu.linquanc.geosms;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import edu.cmu.linquanc.exception.MyExcaption;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected static final String TAG = "MainActivity";
    private static final String PHONE_NUMBER = "6505212598";
    private static final String NAME = "Linquan";

    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_SEND_SMS = 2;


    /** Provides the entry point to Google Play services.*/
    protected GoogleApiClient mGoogleApiClient;
    /** Provider the entry to SmsManager*/
    protected SmsManager smsManager;

    /** Store my location information*/
    protected Location mLastLocation;
    private String latitude, longitude;

    private Button getLocation;
    private TextView location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLocation = (Button) findViewById(R.id.button);
        location = (TextView)findViewById(R.id.location);
        
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                mGoogleApiClient.connect();
            }
        });

        buildGoogleApiClient();
        smsManager = SmsManager.getDefault();
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApiIfAvailable(LocationServices.API)
                .build();
    }

    /**
     * Get the current location and send message
     */
    private void getLocation() {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            try {
                latitude = String.valueOf(mLastLocation.getLatitude());
                longitude = String.valueOf(mLastLocation.getLongitude());
                location.setText("Latitude: " + latitude + ", Longitude: " + longitude);
                if (latitude == null || longitude == null) {
                    throw new MyExcaption(1);
                }
            }catch (MyExcaption e) {
                e.printStackTrace();
            }
            // After get the location, send the message.
            sendSMS();
        } else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }
    }

    private void sendSMS() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                // Send the message to PHONE_NUMBER
                send();
            } else {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.SEND_SMS)) {
                    // Explain to the user why we need to read the contacts
                    Toast.makeText(this, R.string.permission_SMS, Toast.LENGTH_LONG).show();
                }

                requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                        REQUEST_SEND_SMS);
            }
        else {
            send();
            //Toast.makeText(this, R.string.not_marshmallow, Toast.LENGTH_LONG).show();
        }
    }

    private void send() {
        String content = "Hello, I am " + NAME + ". Now, I am at this postion, Latitude: "
                + latitude + ", Longitude: " + longitude;
        smsManager.sendTextMessage(PHONE_NUMBER, null, content, null, null);

        Toast.makeText(this, ("Send SMS to " + PHONE_NUMBER + ": " + content), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // GET the current location
                getLocation();
            } else {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Explain to the user why we need to read the contacts
                    Toast.makeText(this, R.string.permission_rationale, Toast.LENGTH_LONG).show();
                }

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_FINE_LOCATION);
            }
        else {
            getLocation();
            //Toast.makeText(this, R.string.not_marshmallow, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    getLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                }
                return;
            }
            case REQUEST_SEND_SMS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    send();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }
}

