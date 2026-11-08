package com.example.condosched.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;
import androidx.core.util.Consumer;

/** Obtém coordenadas do dispositivo; não consulta APIs meteorológicas. */
public final class WeatherLocation {
    private CancellationSignal pending;

    public static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission") // A permissão é verificada antes de usar LocationManager.
    public void request(Context context, Consumer<Location> callback) {
        cancel();
        if (!hasPermission(context)) {
            callback.accept(null);
            return;
        }
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            callback.accept(null);
            return;
        }
        try {
            String provider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    ? LocationManager.NETWORK_PROVIDER
                    : manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ? LocationManager.GPS_PROVIDER : null;
            if (provider == null) {
                callback.accept(null);
                return;
            }
            Location last = manager.getLastKnownLocation(provider);
            if (last != null) {
                callback.accept(last);
                return;
            }
            pending = new CancellationSignal();
            LocationManagerCompat.getCurrentLocation(manager, provider, pending,
                    ContextCompat.getMainExecutor(context), callback);
        } catch (SecurityException | IllegalArgumentException error) {
            callback.accept(null);
        }
    }

    public void cancel() {
        if (pending != null) pending.cancel();
    }
}
