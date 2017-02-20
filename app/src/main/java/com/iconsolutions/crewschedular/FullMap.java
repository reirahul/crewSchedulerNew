package com.iconsolutions.crewschedular;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;

import org.w3c.dom.Document;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.iconsolutions.helper.GPSTracker;

import java.util.ArrayList;

import crewschedular.fragmentinterface.BaseBackPressedListener;
import rolustech.helper.AlertHelper;

public class FullMap extends Fragment implements View.OnClickListener, DirectionCallback{

    View view;
    FragmentActivity fm;

    private Button btnDirectionFrom, btnDirectionTo;
    private String serverKey = "AIzaSyAMg-A1YMWkkKJrjQovm3457KA76DT9j3w";
    LatLng destination;

    private SupportMapFragment mapView;
    private GoogleMap map;
    GPSTracker gpsTracker;
    Location myLocation, addressLocation;

    String address;
    Boolean isValidAddress = true;

    public FullMap()
    {
        this.fm = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup v,Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.full_map_findapps, v, false);
        initUI();

        Geocoder coder = new Geocoder(getActivity());
        try {
            ArrayList<Address> adresses = (ArrayList<Address>) coder.getFromLocationName(address, 50);
            for(Address add : adresses){
                double longitude = add.getLongitude();
                double latitude = add.getLatitude();
                destination = new LatLng(latitude, longitude);
                addressLocation = new Location("Testing");
                addressLocation.setLatitude(latitude);
                addressLocation.setLongitude(longitude);
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mapView = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.full_map);

                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            gpsTracker = new GPSTracker(fm);
                            myLocation = gpsTracker.m_Location;
                            map = googleMap;
                            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                            if(addressLocation != null) {
                                addMarker(addressLocation);
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(addressLocation), 12));
                            }
                            else {
                                isValidAddress = false;
                                AlertHelper.showAlert(fm, "Error", "Invalid address");
                            }

                        }
                    });

                } catch (Exception e) {
                    Log.d("exception because ", "");
                }
            }
        },3000);

        return view;
    }

    private void initUI(){
        btnDirectionFrom = (Button) view.findViewById(R.id.directions_from);
        btnDirectionFrom.setOnClickListener(this);

        btnDirectionTo = (Button) view.findViewById(R.id.directions_to);
        btnDirectionTo.setOnClickListener(this);

        address = getArguments().getString("WorkLocation");
//        address = "Barkat Market, Main Boulevard Garden Town, Lahore, Punjab, Pakistan";
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.directions_from) {
            map.clear();
            requestDirectionFrom();
        }

        else if (id == R.id.directions_to) {
            map.clear();
            requestDirectionTo();
        }
    }

    public void requestDirectionTo() {
        if(isValidAddress) {
            GoogleDirection.withServerKey(serverKey)
                    .from(getLatLng(myLocation))
                    .to(destination)
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);
        }
    }

    public void requestDirectionFrom() {
        if(isValidAddress) {
            GoogleDirection.withServerKey(serverKey)
                    .from(destination)
                    .to(getLatLng(myLocation))
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);
        }
    }


    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            map.addMarker(new MarkerOptions().position(getLatLng(myLocation)));
            map.addMarker(new MarkerOptions().position(destination));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            map.addPolyline(DirectionConverter.createPolyline(this.getContext(), directionPositionList, 5, Color.RED));

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(myLocation), 12));
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Log.d("FAILED: ", t.getMessage());
    }

    void addMarker(Location location)
    {
        if(map != null && location != null)
        {
            LatLng position = new LatLng(location.getLatitude(),location.getLongitude());
            map.addMarker(new MarkerOptions().position(position));
        }
    }

    @Override
    public void onResume() {
        MainActivity mainActivity = (MainActivity) this.fm;

        super.onResume();
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.fm = getActivity();
        if(this.fm != null)
        {
            ((MainActivity) this.fm).setOnBackPressedListener(new BaseBackPressedListener(this.fm));
        }
    }
    private LatLng getLatLng(Location location)
    {
        LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
        return latlng;
    }
    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}