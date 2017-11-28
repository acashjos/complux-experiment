package io.github.acashjos.compluxsdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.webrtc.PeerConnectionFactory;

public abstract class CompluxListenerService extends Service {
    public CompluxListenerService() {
        PeerConnectionFactory pf;
    }

    @Override
    public abstract IBinder onBind(Intent intent);

    public void emit(String key, Object stream) {

    }
    public void emit(String key, String jsonData) {

    }
}
