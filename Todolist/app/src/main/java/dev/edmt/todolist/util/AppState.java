package dev.edmt.todolist.util;

import android.app.Application;
import android.content.Context;

import org.json.JSONObject;

/**
 * Created by acashjos on 23/4/17.
 */
public abstract class AppState {

    private AppState(){}
    public AppState(Application application){
    };
    public abstract String[] performAction(String action, JSONObject data);
    public  abstract void setStateUpdateListenet(StateUpdateListener listener);
    protected abstract void notifyUpdates(String... changes);
    public abstract JSONObject JSONSerialize();
    public interface StateUpdateListener{
        public void onStateChange(String[] changes);
    }
}
