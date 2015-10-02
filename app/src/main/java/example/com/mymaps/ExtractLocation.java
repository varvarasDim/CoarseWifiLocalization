
package example.com.mymaps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.widget.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class ExtractLocation extends Activity
{

    protected XMLRPCClient client;
    protected String mac;
    protected WifiManager wifiManager;

    private String latitude;
    private String longitude;
    private static final String TAG = "LOCALIZATION";
    public static Context context;
    private GoogleMap map;
    private int registered=0;
    private MarkerOptions markerOptions;



    Intent intent;


    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ExtractLocation.context=getApplicationContext();
        Log.d(TAG, "onCreate ExtractLocation");
        setContentView(R.layout.extract_location);
        setTitle(R.string.app_name);
        Button startLocalization = (Button) findViewById(R.id.buttonStartLocalization);
        Button stopLocalization = (Button) findViewById(R.id.buttonStopLocalization);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        //initial area that will be shown on the map
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Float.parseFloat("39.17265867042994"), Float.parseFloat("22.8790283203125")),6));
        startLocalization.setOnClickListener(regReceiver);
        stopLocalization.setOnClickListener(unregReceiver);

        intent = new Intent(this, WiFiDataService.class);


    }

    private OnClickListener unregReceiver = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (registered==1)
            {
                registered=0;
                unregisterReceiver(broadcastReceiver);
                stopService(intent);
            }
            Log.d(TAG,"Receiver UnRegistered");
        }
    };
    private OnClickListener regReceiver = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (registered==0)
            {
                registered=1;
                startService(intent);
                registerReceiver(broadcastReceiver, new IntentFilter(WiFiDataService.BROADCAST_ACTION));
            }
            Log.d(TAG,"Receiver Registered");

        }
    };

    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(WiFiDataService.BROADCAST_ACTION));
    }

    protected void onPause() {
        super.onPause();
        if (registered==1)
            unregisterReceiver(broadcastReceiver);

    }

    protected void onDestroy() {
        super.onDestroy();
        if (registered==1)
            unregisterReceiver(broadcastReceiver);

    }

    protected void onStop() {
        super.onStop();
        if (registered==1)
            unregisterReceiver(broadcastReceiver);

    }


    //wifi data available
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateData(intent);
        }
    };

    private void updateData(Intent intent) {
        latitude = intent.getStringExtra("latitude");
        longitude = intent.getStringExtra("longitude");

        if (((latitude != null) && (latitude != "")) && ((longitude != null) && (longitude != "")))
        {
            Toast.makeText(context, "lat: "+latitude + " long: "+longitude  + "", Toast.LENGTH_LONG).show();
            map.clear(); ////Clear map from previous markers
            markerOptions = new MarkerOptions();
            markerOptions.draggable(true);
            markerOptions.position(new LatLng(Float.parseFloat(latitude), Float.parseFloat(longitude)));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow_64));
            //markerOptions.icon(BitmapDescriptorFactory.defaultMarker());
            map.addMarker(markerOptions); //add the specific marker
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Float.parseFloat(latitude), Float.parseFloat(longitude)),12));
            latitude = null;
            longitude = null;
        }

    }
}























    
    
    


	