package io.github.acashjos.compluxdriver;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAINACT";
    private static final int LISTENPORT = 41234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    //http://stackoverflow.com/questions/17308729/send-broadcast-udp-but-not-receive-it-on-other-android-devices
    public HashMap<String, String> sendBroadcast(String messageStr) {
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(), LISTENPORT);
            socket.send(sendPacket);
            System.out.println(getClass().getName() + "Broadcast packet sent to: " + getBroadcastAddress().getHostAddress());
            return readResponses(socket);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException: " + e.getMessage());
        }
        return new HashMap<>();
    }

    private HashMap<String, String> readResponses(DatagramSocket socket)  {
        long startAt = new Date().getTime();
        int TTL = 1000; //  1 sec

        HashMap<String,String > map = new HashMap<>();
        while(new Date().getTime() - startAt < TTL){
            //Receive a packet
            byte[] recvBuf = new byte[100];
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);

            try {
                socket.setSoTimeout(TTL/3);
                socket.receive(packet);
            } catch (Exception e){
                // do nothing. timeout can throw IOException.
                continue;
            }
            //Packet received
            Log.i(TAG, "Packet received from: " + packet.getAddress().getHostAddress());
            String data = new String(packet.getData()).trim();
            JSONObject obj = null;
            try {
                obj = new JSONObject(data);
                map.put(obj.optString("name"), obj.optString("address"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Packet received; data: " + data);
        }

        return map;
    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        String ip = "";
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            ip += ((broadcast >> k * 8) & 0xFF)+".";
        }
        Log.e(TAG,"broadcasting ip: "+ ip);
//        quads[2]= (byte) 1;
        return InetAddress.getByAddress(quads);
    }

    public void udpbroadcast(View view) {
        AsyncTask<Void, Void, HashMap<String, String>> task = new AsyncTask<Void, Void, HashMap<String, String>>() {
            @Override
            protected HashMap<String, String> doInBackground(Void... voids) {
                return sendBroadcast("ping");
            }

            @Override
            protected void onPostExecute(final HashMap<String, String> addressMap) {
                super.onPostExecute(addressMap);
                final ArrayList<String> deviceList = new ArrayList<String>(addressMap.keySet());
                for(String name: deviceList){
                    Log.e(TAG,name+':'+addressMap.get(name));
                }
                ListView listView = (ListView) findViewById(R.id.devicelist);
                ArrayAdapter adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,android.R.id.text1,deviceList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(MainActivity.this, CoreService.class);
                        intent.putExtra("address",addressMap.get(deviceList.get(i)));
                        startService(intent);
                    }
                });
            }
        };

        task.execute();

    }
    public void serviceStarter(View view) {

    }
}
