package example.com.mymaps;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.conn.HttpHostConnectException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WiFiDataService extends Service {
    public static final String BROADCAST_ACTION = "example.com.mymaps.getdata";
    Intent intentDataToActivity;

    protected String mac;
    protected WifiManager wifiManager;
    private BroadcastReceiver receiver;
    private XMLRPCClient client;
    private String latitude;
    private String longitude;
    private URI uri;







    private Handler handler = new Handler();


    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String serverIpAddress = preferences.getString("serverIP","192.168.1.5");
        String serverPort = preferences.getString("serverPort","8080");

        uri = URI.create("http://"+serverIpAddress+":"+serverPort);
        client = new XMLRPCClient(uri);
        intentDataToActivity = new Intent(BROADCAST_ACTION);
        receiver = new WiFiScanReceiver();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        registerReceiver(receiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        Log.d("SERVICE","onStartCommand");
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();

        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();

        mac = info.getMacAddress();//Users mac Address
        wifiManager.startScan(); //Starts Scanning

        Thread thread = new Thread(null, sendUpdatesToActivity,"Background");
        thread.start();
        return START_STICKY;
    }



    private Runnable sendUpdatesToActivity = new Runnable() {
        public void run() {
            sendDataToActivity();
            handler.postDelayed(this, 5000); // 5 seconds
            wifiManager.startScan(); //Starts Scanning
        }
    };

    private void sendDataToActivity() {
        Log.d("SERVICE", "entered sendDataToActivity");
        Log.d("SERVICE", "data sent by intent: "+latitude+" "+longitude);
        intentDataToActivity.putExtra("latitude", latitude);
        intentDataToActivity.putExtra("longitude", longitude);
        sendBroadcast(intentDataToActivity);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdatesToActivity);
        if (receiver != null)
            unregisterReceiver(receiver);
        super.onDestroy();
    }





    public class WiFiScanReceiver extends BroadcastReceiver {
        private static final String TAG = "WiFiScanReceiver";
        private String ss = "";

        public void onReceive(Context c, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();//receiver Scan Result objects

            Map<String, Object> wiFiDataMap = new HashMap<String, Object>();
            int count = 0;

            for (ScanResult result : results) { //collect data from scan
                String stringWiFiData =  result.BSSID.toLowerCase()+" ; "+result.level+" ; "+result.SSID;
                Log.d(TAG, stringWiFiData);
                wiFiDataMap.put("network_"+count,stringWiFiData);
                count++;
            }
            wiFiDataMap.put("networksFound",count);
            wiFiDataMap.put("clientMac",mac.toLowerCase());
            wiFiDataMap.put("clientDate",android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()).toString());

            XMLRPCMethod method = new XMLRPCMethod("getCoordinates", new XMLRPCMethodCallback() {
                public void callFinished(Object result) {
                    Map<String, Object> map = (Map<String, Object>) result;
                    latitude = map.get("latitude").toString();
                    longitude = map.get("longitude").toString();
                }
            });
            Object[] params = {
                    wiFiDataMap
            };
            method.call(params);

           // intentDataToActivity.putExtra("latitude", latitude);
            //intentDataToActivity.putExtra("longitude", longitude);
            //c.sendBroadcast(intentDataToActivity);
        }
    }




    interface XMLRPCMethodCallback {
        void callFinished(Object result);
    }
    class XMLRPCMethod extends Thread {
        private String method;
        private Object[] params;
        private Handler handler;
        private XMLRPCMethodCallback callBack;
        public XMLRPCMethod(String method, XMLRPCMethodCallback callBack) {
            this.method = method;
            this.callBack = callBack;
            handler = new Handler();
        }
        public void call() {
            call(null);
        }
        public void call(Object[] params) {
            this.params = params;
            start();
        }
        @Override
        public void run() {
            try {
                final Object result = client.callEx(method, params);
                handler.post(new Runnable() {
                    public void run() {
                        callBack.callFinished(result);
                    }
                });
            } catch (final XMLRPCFault e) {
                handler.post(new Runnable() {
                    public void run() {
                        Log.d("Test", "error", e);
                    }
                });
            } catch (final XMLRPCException e) {
                handler.post(new Runnable() {
                    public void run() {
                        Throwable cause = e.getCause();
                        if (cause instanceof HttpHostConnectException) {
                            ;
                        } else {
                            ;
                        }
                        Log.d("Test", "error", e);
                    }
                });
            }
        }
    }



}
