package example.com.mymaps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import org.apache.http.conn.HttpHostConnectException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WiFiScanReceiverClass extends BroadcastReceiver {

    public static final String BROADCAST_ACTION = "example.com.mymaps.getdata";
    Intent intentDataToActivity;

    private static final String TAG = "WiFiScanReceiver";
    private ExtractLocation extractLocation;
    private String latitude;
    private String longitude;

    public WiFiScanReceiverClass(ExtractLocation extLoc) {
        super();
        this.extractLocation = extLoc;
    }

    public void onReceive(Context c, Intent intent) {


        intentDataToActivity = new Intent(BROADCAST_ACTION);

        List<ScanResult> results = extractLocation.wifiManager.getScanResults();//receiver Scan Result objects

        Map<String, Object> wiFiDataMap = new HashMap<String, Object>();
        int count = 0;

        for (ScanResult result : results) { //collect data from scan
            String stringWiFiData = result.BSSID.toLowerCase() + " ; " + result.level + " ; " + result.SSID;
            Log.d(TAG, stringWiFiData);
            wiFiDataMap.put("network_" + count, stringWiFiData);
            count++;
        }
        wiFiDataMap.put("networksFound", count);
        wiFiDataMap.put("clientMac", extractLocation.mac.toLowerCase());
        wiFiDataMap.put("clientDate", android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()).toString());

        XMLRPCMethod method = new XMLRPCMethod("getCoordinates", new XMLRPCMethodCallback() {
            public void callFinished(Object result) {
                Map<String, Object> map = (Map<String, Object>) result;
                //extractLocation.latitude = map.get("latitude").toString();
                //extractLocation.longitude = map.get("longitude").toString();

                latitude = map.get("latitude").toString();
                longitude = map.get("longitude").toString();


            }
        });
        Object[] params = {
                wiFiDataMap
        };
        method.call(params);

        intentDataToActivity.putExtra("latitude", latitude);
        intentDataToActivity.putExtra("longitude", longitude);
        c.sendBroadcast(intentDataToActivity);

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
                final Object result = extractLocation.client.callEx(method, params);
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

