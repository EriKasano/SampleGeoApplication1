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
    final private GoogleMap map;
    //The current marker that this single-focused-marker is wrapping.
    private Marker marker;

    private OnMoveEndedListener onMoveEndedListener;

    public interface OnMoveEndedListener{
        void onMoveEnded(int reason);
    }

    public SingleFocusedMarker(GoogleMap map){
        this.map = map;
    }

    public OnMoveEndedListener getOnMoveEndedListener() {
        return onMoveEndedListener;
    }

    public void setOnMoveEndedListener(OnMoveEndedListener onMoveEndedListener) {
        this.onMoveEndedListener = onMoveEndedListener;
    }

    public void moveTo(LatLng latLng){
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
        graduallyMoveCamera(latLng);
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

            final long margin = 50;
            if(onMoveEndedListener != null){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onMoveEndedListener
                                .onMoveEnded(GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION);
                    }
                }, valueAnimator.getDuration() + margin);
            }
        }else{
            map.moveCamera(CameraUpdateFactory.newLatLng(to));
        }
    }

    public void setTitle(String title){
        marker.setTitle(title);
    }

    class LatLngInterpolator {
        public LatLng interpolate(float fraction, LatLng a, LatLng b) {
            double lat = (b.latitude - a.latitude) * fraction + a.latitude;
            double lngDelta = b.longitude - a.longitude;

            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360;
            }
            double lng = lngDelta * fraction + a.longitude;
            return new LatLng(lat, lng);
        }
    }
}
