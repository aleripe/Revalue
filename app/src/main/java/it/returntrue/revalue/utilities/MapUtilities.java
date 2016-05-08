package it.returntrue.revalue.utilities;

import android.graphics.Color;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class MapUtilities {
    public static Circle getCenteredCircle(GoogleMap map, LatLng center, int radius) {
        return map.addCircle(new CircleOptions().center(center).radius(radius).strokeColor(Color.TRANSPARENT));
    }

    public static int getCircleZoomLevel(Circle circle) {
        if (circle != null){
            double radius = circle.getRadius();
            double scale = radius / 500;
            return (int) (16 - Math.log(scale) / Math.log(2));
        }
        return 0;
    }
}