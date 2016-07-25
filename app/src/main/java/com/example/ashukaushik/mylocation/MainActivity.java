package com.example.ashukaushik.mylocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener, SensorEventListener {

    GoogleApiClient mClient;

    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b = (Button) findViewById(R.id.button);
        if (mClient == null) {
            mClient = new GoogleApiClient.Builder((MainActivity.this)).addConnectionCallbacks(MainActivity.this).addOnConnectionFailedListener(MainActivity.this).addApi(LocationServices.API).build();
        }
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationUpdate();
                startSensorUpdate();
            }
        });

    }

    public boolean checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(!checkPermission()){
            return;
        }
//        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mClient);
//        if(lastLocation == null){
//            Toast.makeText(MainActivity.this,"Null",Toast.LENGTH_LONG).show();
//            return;
//        }
//        startLocationUpdate();


    }

    SensorManager mSensorManager;
    Sensor proximity;
    private void startSensorUpdate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
        for (Sensor s: sensors) {
            Log.i("sensor", s.toString());
        }

        proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximity != null) {
            mSensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == proximity) {
            Log.i("proxomity", event.values[0] + "");
        } else {

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

//    @Override
//    protected void onResume() {
//        mSensorManager.registerListener(this,msensor,SensorManager.SENSOR_DELAY_NORMAL);
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        mSensorManager.unregisterListener(this);
//        super.onPause();
//    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(MainActivity.this,"Suspended State",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this,"failed",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        mClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                startLocationUpdate();
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    public void startLocationUpdate(){
        final LocationRequest lr = LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).setInterval(5000).setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(lr);
        LocationServices.SettingsApi.checkLocationSettings(mClient, builder.build()).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                int statusCode = locationSettingsResult.getStatus().getStatusCode();
                if(statusCode == LocationSettingsStatusCodes.SUCCESS){
                    if(!checkPermission()){
                        return;
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(mClient, lr, MainActivity.this);
                }else if(statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED){
                    try {
                        locationSettingsResult.getStatus().startResolutionForResult(MainActivity.this, 1);
                    } catch (IntentSender.SendIntentException e) {

                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"ELSE ELSE",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    Location bestLocation = null;
    int i=0;
    @Override
    public void onLocationChanged(Location location) {
        if(bestLocation == null || (location.getAccuracy() < bestLocation.getAccuracy()))
            bestLocation= location;
        i++;

        if(bestLocation.getAccuracy() <100 || i>10){
            i=0;
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient,MainActivity.this);
            useLocation();
        }
    }

    public void useLocation() {
        Toast.makeText(MainActivity.this,"latitude :" + bestLocation.getLatitude() +"longitude :" +bestLocation.getLongitude(),Toast.LENGTH_LONG).show();
    }


}
