package io.github.acashjos.compluxdriver;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class CoreService extends Service {
    private WebSocketClient socket;
    private HashMap<String, String> appMap = new HashMap<>();
    private boolean socketOpen = false;
    private boolean emitLock = false;
    private String appInForeGround;
    private BroadcastReceiver pongReceiver, stateReceiver;

    public CoreService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e(TAG, "core service started");

        final Intent nintent = new Intent("dev.edmt.todolist.compluxclient");
        sendBroadcast(nintent);
        final String address = intent.getStringExtra("address");
        Toast.makeText(this, "connection attempt to" + address, Toast.LENGTH_LONG).show();
        pongReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("#####", "receiving broadcast from" + intent.getStringExtra("appId") + intent.getStringExtra("name"));
                appMap.put(intent.getStringExtra("appId"), intent.getStringExtra("name"));

                socket = connectWebSocket(address);

            }
        };
        stateReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("#####", "receiving broadcast from" + intent.getStringExtra("appId") + intent.getStringExtra("context"));
                Log.e("#####", "state " + intent.getStringExtra("state"));

                JSONObject obj = new JSONObject();
                JSONObject state = null;
                try {
                    state = new JSONObject(intent.getStringExtra("state"));
                    obj.put("state", state);
                    obj.put("context", intent.getStringExtra("context"));
                    obj.put("appId", intent.getStringExtra("appId"));
                    respond(obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("io.github.acashjos.compluxdriver.register");
        registerReceiver(pongReceiver, filter);

        filter = new IntentFilter();
        filter.addAction("io.github.acashjos.compluxdriver.state_change");
        registerReceiver(stateReceiver, filter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private WebSocketClient connectWebSocket(String address) {
        URI uri;
        try {
            uri = new URI("ws://" + address);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        final WebSocketClient mWebSocketClient = new WebSocketClient(uri) {
            public static final String TAG = "WS-READ";

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                socketOpen = true;
                JSONObject msg = new JSONObject();
                try {
                    msg.put("device", Build.MANUFACTURER + " " + Build.MODEL);
                    socket.send(msg.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                socket.send("0");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                emitLock = true;
                Log.e(TAG, s);
                try {
                    JSONObject msg = new JSONObject(s);
                    actionRouter(msg.getString("action"), msg.optString("appId"), msg.optString("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // **********************************
//                    }
//                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
                socketOpen = false;

                unregisterReceiver(pongReceiver);
                stopSelf();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClient.connect();
        return mWebSocketClient;
    }

    private void sendStuff(String jsonString) {
        if (socketOpen && !emitLock) {
            socket.send(jsonString);
        }
    }

    private void sendStuff(byte[] data) {
        int MAX_LEN = 100000; // 100K
        if (socketOpen && !emitLock) {
            if (data.length < MAX_LEN)
                socket.send(data);
            else {
                sendStuff("TOO LARGE");
            }
        }
    }

    private void respond(String jsonString) {
        emitLock = false;
        sendStuff(jsonString);
    }

    private void respond(byte[] data) {
        emitLock = false;
        sendStuff(data);
    }


    private void actionRouter(String action, String appId, String data) {

        switch (action) {
            case "getWallpaper":
                getCurrentWallpaper();
                break;
            case "getIcon":
                getIcon(appId);
                break;
            case "listApps":
                listApps();
                break;
            case "launch":
                launchApp(appId);
                break;
            case "relay":
                relayData(appId, data);
                break;
            case "switchContext":
                switchContext(appId, data);
                break;
            case "getFile":
                returnSharedFile(appId,data);
                break;
        }

        emitLock = false;

    }

    private void returnSharedFile(String appId, String fdJSON) {
        JSONObject fd;
        String path;
        try {
            fd = new JSONObject(fdJSON);
            path = appId+'/'+fd.getString("filename");
        } catch (JSONException e) {
            respond("FAIL");
            e.printStackTrace();
            return;
        }
        File file = new File(Environment.getExternalStorageDirectory(), "/.complux/" + path);
        if (!file.exists()) {
            Log.e(TAG, "FILE does not exist - " + file.getAbsolutePath());
            respond("FAIL");
            return;
        }
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();

            respond(bytes);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void listApps() {
        JSONObject applist = new JSONObject();
        try {
            for (String id : appMap.keySet()) applist.put(id, appMap.get(id));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        respond(applist.toString());
    }

    private void getIcon(String appId) {
        try {
            byte[] picAsBytes = getBytesFromDrawable(getPackageManager().getApplicationIcon(appId));
            respond(picAsBytes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void getCurrentWallpaper() {
        // http://stackoverflow.com/a/9939358/2605574
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(CoreService.this);
        final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        byte[] bytes = getBytesFromDrawable(wallpaperDrawable);
        Log.e("DDDDD", "" + bytes.length);
        respond(bytes);
    }

    // http://stackoverflow.com/a/4830846/2605574
    private byte[] getBytesFromDrawable(Drawable pic) {
        Bitmap bitmap = ((BitmapDrawable) pic).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        return bitmapdata;
    }

    private void launchApp(String appId) {
        Intent nintent = new Intent();
        nintent.setAction(appId + ".FX_HOOK_UP");
        sendBroadcast(nintent);
        appInForeGround = appId;
    }

    private void relayData(String appId, String data) {
        Intent nintent = new Intent(appId + ".FX_UPDATE");
        nintent.putExtra("data", data);
        sendBroadcast(nintent);
    }

    private void switchContext(String appId, String data) {

        Intent intent = new Intent();
        intent.setAction(appId + ".FX_SWITCH_CONTEXT");
        intent.putExtra("context", data);
        sendBroadcast(intent);
    }

}
