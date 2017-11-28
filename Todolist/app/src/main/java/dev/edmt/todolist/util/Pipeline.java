package dev.edmt.todolist.util;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dev.edmt.todolist.BuildConfig;
import dev.edmt.todolist.states.ListState;

import static android.content.ContentValues.TAG;

public class Pipeline extends Service {


    private static final String APP_ID = BuildConfig.APPLICATION_ID;
    private static final String FX_UPDATE = APP_ID + ".FX_UPDATE";
    private static final String FX_SWITCH_CONTEXT = APP_ID + ".FX_SWITCH_CONTEXT";
    private static final String FX_LOST = APP_ID + ".FX_LOST";
    private static final String FX_HOOK_UP = APP_ID + ".FX_HOOK_UP";
    private static final String DEFAULT_CTX = "list";
    private String boundState;
    private String remoteState;
    private OnStateChange changeListener;
    private BroadcastReceiver receiver;
    private boolean isRemoteActive;
    private Map<String, AppState> stateMap = new HashMap<>();

    private long time;

    public Pipeline() {

    }

    public class PipelineBinder extends Binder {

        public Pipeline getService() {
            return Pipeline.this;
        }
    }

    ;

    @Override
    public void onCreate() {
        super.onCreate();
        time = new Date().getTime();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e(TAG, "service started");
        File exportPath = new File(Environment.getExternalStorageDirectory(), "/.complux/" + APP_ID);
        if (!exportPath.exists()) {
            Log.e(TAG, "FILE does not exist");
            moveAssets();
        }

        Log.e(TAG, exportPath.getAbsolutePath());
        if ("broadcast".equals(intent.getStringExtra("source"))) {
            Intent nintent = new Intent("io.github.acashjos.compluxdriver.register");
            nintent.putExtra("appId", APP_ID);
            nintent.putExtra("name", "My ToDo App");
            sendBroadcast(nintent);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(FX_HOOK_UP);
        filter.addAction(FX_LOST);
        filter.addAction(FX_SWITCH_CONTEXT);
        filter.addAction(FX_UPDATE);

        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                AppState remoteStateCtx = stateMap.get(remoteState);
                switch (intent.getAction()) {
                    case FX_LOST:
                        if (changeListener != null) changeListener.remoteDisconnected();
                        isRemoteActive = false;
                        break;
                    case FX_HOOK_UP:
                        if (changeListener != null) changeListener.remoteConnected();
                        isRemoteActive = true;
//                        break;
                    case FX_SWITCH_CONTEXT:
                        remoteState = intent.getStringExtra("context");
                        remoteState = remoteState == null ? DEFAULT_CTX : remoteState;
                        switchContext(remoteState);
                        stateMap.get(remoteState).setStateUpdateListenet(new AppState.StateUpdateListener() {
                            @Override
                            public void onStateChange(String[] changes) {
                                pushStateToRemote();
                            }
                        });
                        break;
                    case FX_UPDATE:
                        JSONObject params, obj = null;
                        String action = null;
                        String data = intent.getStringExtra("data");
                        Log.e(remoteState, "receiving " + data);

                        try {
                            obj = new JSONObject(data);
                            action = obj.getString("action");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }
                        try {
                            params = obj.getJSONObject("params");
                        } catch (JSONException e) {
//                            e.printStackTrace();
                            params = new JSONObject();
                            try {
                                params.put("__", obj.getString("params"));
                            } catch (JSONException e1) {
//                                e1.printStackTrace();
                            }
                        }
                        remoteStateCtx.performAction(action, params);

                        break;
                }
                //do something based on the intent's action
            }
        };
        registerReceiver(receiver, filter);
        return super.onStartCommand(intent, flags, startId);
    }

    private void pushStateToRemote() {

        JSONObject state = stateMap.get(remoteState).JSONSerialize();
//        try {
//            state.put("appId", APP_ID);
        Intent intnt = new Intent("io.github.acashjos.compluxdriver.state_change");
        Log.e("&&&&", state.toString());
        intnt.putExtra("state", state.toString());
        intnt.putExtra("appId", APP_ID);
        intnt.putExtra("context", remoteState);
        sendBroadcast(intnt);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    private void moveAssets() {
        File outPath = new File(Environment.getExternalStorageDirectory(), ".complux");
        if (!outPath.exists()) {
            outPath.mkdir();
        }
        outPath = new File(outPath, APP_ID);
        if (!outPath.exists()) {
            outPath.mkdir();
        }
        copyFile("bundle.js", outPath);
        copyFile("style.css", outPath);

    }

    private void copyFile(String filename, File outPath) {
        AssetManager assetManager = this.getAssets();

        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(filename);
            File newFileName = new File(outPath, filename);
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        String state = intent.getStringExtra("state");
        if (state == null)
            throw new UnsupportedOperationException("Cannot bind without specifying a state");
        switchContext(state);
        boundState = state;
        return new PipelineBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (remoteState != boundState) {
            stateMap.remove(stateMap.get(boundState));
        }
        boundState = null;
//        if(remoteState == null) stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    public AppState switchContext(String state) {
        if (stateMap.containsKey(state))
            return stateMap.get(state);

        AppState currentState = null;
        Application app = getApplication();
        switch (state) {
            case "list":
                Log.e("QQQ", "Calling constructor " + time);
                currentState = new ListState(getApplication());
                break;
        }

        stateMap.put(state, currentState);
        return currentState;
    }

    public void setStateChangeListener(final OnStateChange listener) {

        changeListener = listener;
        if (boundState != null)
            stateMap.get(boundState).setStateUpdateListenet(new AppState.StateUpdateListener() {
                @Override
                public void onStateChange(String[] changes) {
                    for (String change : changes) {
                        listener.onStateUpdate(change);
                    }

                    if (remoteState != null)
                        pushStateToRemote();
                }
            });
    }


    public interface OnStateChange {
        void remoteConnected();

        void remoteDisconnected();

        void onStateUpdate(String change);
    }
}
