package com.example.condosched.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.condosched.R;
import com.example.condosched.domain.Forecast;
import com.example.condosched.ui.display.WeatherDisplay;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ForecastAdapter extends ArrayAdapter<Forecast> {
    private final DateTimeFormatter dateFormat;

    public ForecastAdapter(Context context, List<Forecast> forecasts) {
        super(context, R.layout.item_forecast, forecasts);
        dateFormat = DateTimeFormatter.ofPattern("EEEE, d MMM",
                context.getResources().getConfiguration().getLocales().get(0));
    }

    @Override
    public View getView(int position, View recycled, ViewGroup parent) {
        View row = recycled == null
                ? LayoutInflater.from(getContext()).inflate(R.layout.item_forecast, parent, false) : recycled;
        Forecast forecast = getItem(position);
        ((ImageView) row.findViewById(R.id.forecastIcon))
                .setImageResource(WeatherDisplay.icon(forecast.getConditionCode()));
        ((TextView) row.findViewById(R.id.forecastDate)).setText(forecast.getDate().format(dateFormat));
        ((TextView) row.findViewById(R.id.forecastDescription)).setText(forecast.getDescription());
        return row;
    }
}
