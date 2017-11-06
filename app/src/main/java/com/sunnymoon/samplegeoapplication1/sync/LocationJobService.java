package com.sunnymoon.samplegeoapplication1.sync;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sunnymoon.samplegeoapplication1.MapsActivity;
import com.sunnymoon.samplegeoapplication1.R;

import java.util.Random;

/**
 * Project SampleGeoApplication1
 * Working on LocationJobService
 * Created by Shion T. Fujie on 2017/11/05.
 */
public class LocationJobService extends JobService {
    private static final String NOTIFICATION_CHANNEL = "Location retrieval";
    private static final int NOTIFICATION_ID = 001;

    //Assumed to be true for the testing purpose.
    private boolean mLocationPermissionGranted = true;
    @Override
    public boolean onStartJob(final JobParameters params) {
        final FusedLocationProviderClient fusedLocationProviderClient
                = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                final Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            final Location location = task.getResult();
                            sendNotification("lat: " + location.getLatitude() + ", lng: " + location.getLongitude(), location);
                        } else {
                            sendNotification("Obtaining a fused location has been failed.", null);
                        }
                        LocationJobService.this.jobFinished(params, false);
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
            sendNotification(e.getMessage(), null);
            return false;
        }
        return true;
    }

    private void sendNotification(String message, Location location){
        final Intent resultIntent = new Intent(this, MapsActivity.class);
        if(location != null) {
            resultIntent.putExtra(MapsActivity.BUNDLE_LOCATION_IS_SET, true);
            resultIntent.putExtra(MapsActivity.BUNDLE_LATITUDE, location.getLatitude());
            resultIntent.putExtra(MapsActivity.BUNDLE_LONGITUDE, location.getLongitude());
        }
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        final PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_maps)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notifyMgr.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
