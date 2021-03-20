package com.example.weather;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    String city;
    DecimalFormat df = new DecimalFormat("#.##");
    TextView address,update_at,temperature, min_temperature, max_temprature, status, wind, pressure, humidity, visibility, cloud;
    private final String  url = "http://api.openweathermap.org/data/2.5/weather";
    private final String  api = "72323379ed0fa9b0a04f39696af3d091";
    LocationManager locationManager;

    ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = new ProgressDialog(MainActivity.this);
        progressBar.show();
        progressBar.setContentView(R.layout.progress_dialog);
        progressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        grantPermission();
        check_location_enabled();
        get_location();
    }

    public void weather_updates(View view){

        weather_info();

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            city = addresses.get(0).getLocality();

        } catch (IOException e) {
            e.printStackTrace();
        }

        weather_info();

    }

    public void weather_info(){
        String tempUrl = url+"?q=" + city.toLowerCase().trim() + "&appid=" +api;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(String response) {

                try {
                    Log.d("Go", city);
//                    Log.d("Response", response);
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                    String description = jsonObjectWeather.getString("description");
                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                    Double temp = jsonObjectMain.getDouble("temp") - 273.15;
                    Double min_temp = jsonObjectMain.getDouble("temp_min") - 273.15;
                    Double max_temp = jsonObjectMain.getDouble("temp_max") - 273.15;
                    int press = jsonObjectMain.getInt("pressure");
                    int humid = jsonObjectMain.getInt("humidity");
                    int visible = (int) jsonResponse.getLong("visibility") / 1000;
                    JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                    int airSpeed = jsonObjectWind.getInt("speed");
                    JSONObject jsonObjectCloud = jsonResponse.getJSONObject("clouds");
                    int allClouds = jsonObjectCloud.getInt("all");


                    // break point or getting id
                    address = findViewById(R.id.address);
                    update_at = findViewById(R.id.update_at);
                    temperature = findViewById(R.id.temp);
                    min_temperature = findViewById(R.id.temp_min);
                    max_temprature = findViewById(R.id.temp_max);
                    status = findViewById(R.id.status);
                    pressure = findViewById(R.id.pressure);
                    humidity = findViewById(R.id.humidity);
                    visibility = findViewById(R.id.visibility);
                    wind = findViewById(R.id.wind);
                    cloud = findViewById(R.id.cloud);


                    // weather value

                    LocalDate date = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu");
                    String dat = date.format(formatter);
                    LocalDateTime dateTime = LocalDateTime.now();
                    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("h:m:s a");
                    String time = dateTime.format(formatter2);

                    progressBar.show();
                    update_at.setText(dat + " " + time);
                    address.setText(city.toUpperCase());
                    status.setText(description);
                    temperature.setText(df.format(temp) + "°C");
                    max_temprature.setText("Max: " + df.format(max_temp) + "°C");
                    min_temperature.setText("Min: " + df.format(min_temp) + "°C");
                    pressure.setText(press + "mb");
                    humidity.setText(humid + "%");
                    visibility.setText(visible + "km");
                    wind.setText(airSpeed + "m/s");
                    cloud.setText(allClouds + "%");
                    progressBar.dismiss();

                } catch (JSONException e) {
                    Log.d("Response", response + e);
                }
            }
        }, error -> Toast.makeText(this, "Something went Wrong", Toast.LENGTH_SHORT).show());

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
        }


    private void check_location_enabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnable = false;
        boolean networkEnable = false;

        try {
            gpsEnable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        }catch (Exception e){
            e.printStackTrace();
        }try {
            networkEnable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        }catch (Exception e){
            e.printStackTrace();
        }
        if(!gpsEnable){
            Toast.makeText(this, "Please Turn on Loctation To get Weather Updates", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));// redirect Location if gps not enabled
        }
        if (!networkEnable){
            Toast.makeText(this, "Please Turn on your Data", Toast.LENGTH_SHORT).show();

        }
    }

    private void grantPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);

        }
    }

    private void get_location() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,500, 5,  this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}