package com.sunnymoon.samplegeoapplication1.maps.marker;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.animation.DecelerateInterpolator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Project SampleGeoApplication1
 * Working on SingleFocusedMarker
 * Created by Shion T. Fujie on 2017/11/03.
 */
public class SingleFocusedMarker {
    //The map that this single-focused-marker is on.
    private GoogleMap map;
    //The current marker that this single-focused-marker is wrapping.
    private Marker marker;

    private OnBindListener onBindListener;

    //Since binding a GoogleMap happens before substituting a LatLng for
    //the marker's location, we have to preserve a procedure so that
    //concatenating the map and location at once.
    public interface OnBindListener {
        void onBind(GoogleMap map1);
    }

    public OnBindListener getOnBindListener() {
        return onBindListener;
    }

    public void setOnBindListener(OnBindListener onBindListener) {
        this.onBindListener = onBindListener;
    }

    public void bindMap(GoogleMap map){
        if(this.map == null) {
            this.map = map;

            //If there is a binging already set, then apply it to the map.
            if(onBindListener != null){
                onBindListener.onBind(map);
            }
        }
    }

    public GoogleMap getMap() {
        return map;
    }

    public LatLng getLatLng(){
        return marker.getPosition();
    }

    public String getTitle(){
        return marker.getTitle();
    }

    public void moveTo(LatLng latLng){
        moveTo(latLng, true);
    }

    public void moveTo(final LatLng latLng, final boolean animate){
        if(marker != null) {
            final Marker preMarker = marker;
            final ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(preMarker, "alpha", 1,0);
            alphaAnimation.setDuration(1000);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    preMarker.remove();
                }
            }, alphaAnimation.getDuration());
            alphaAnimation.start();
        }
        //Create a new marker.
        final MarkerOptions options = new MarkerOptions().position(latLng);
        marker = map.addMarker(options);
        if(animate)
            graduallyMoveCamera(latLng);
        else
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //Apply a fading animation.
        final ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(marker, "alpha", 0,1);
        alphaAnimation.setDuration(1000);
        alphaAnimation.start();
    }

    private void graduallyMoveCamera(@NonNull final LatLng to){
        final LatLng from = map.getCameraPosition().target;

        if(from != null) {
            final ValueAnimator valueAnimator = new ValueAnimator();
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, from, to);
                    map.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
                }
            });
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.setFloatValues(0, 1);
            valueAnimator.setDuration(1000);
            valueAnimator.start();
        }else{
            map.moveCamera(CameraUpdateFactory.newLatLng(to));
        }
    }

    public void setTitle(String title){
        marker.setTitle(title);
    }

    class LatLngInterpolator {
        public LatLng interpolate(float fraction, LatLng from, LatLng to) {
            double lat = (to.latitude - from.latitude) * fraction + from.latitude;
            double lngDelta = to.longitude - from.longitude;

            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360;
            }
            double lng = lngDelta * fraction + from.longitude;
            return new LatLng(lat, lng);
        }
    }
}
