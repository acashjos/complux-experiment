package dev.edmt.todolist.states;

import android.app.Application;
import android.util.Log;
import android.widget.ArrayAdapter;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import dev.edmt.todolist.DbHelper;
import dev.edmt.todolist.R;
import dev.edmt.todolist.util.AppState;

/**
 * Created by acashjos on 22/4/17.
 */

public class ListState extends AppState {
    DbHelper dbHelper;
    private final Application application;
    private ArrayList<String> taskList;
    private StateUpdateListener updateListener;

    public ArrayList<String> getTaskList() {
        return taskList;
    }

    public ListState(Application app) {
        super(app);
        application = app;
        dbHelper = new DbHelper(application);
        Log.e("QQQ","Starting task state");

    }


    public void loadTaskList() {
        taskList = dbHelper.getTaskList();
        notifyUpdates(new String[]{"taskList"});
    }
    public void addTask(String task){
        dbHelper.insertNewTask(task);
        loadTaskList();
    }
    public void deleteTask(String task){
        dbHelper.deleteTask(task);
        loadTaskList();
    }
    @Override
    public String[] performAction(String action, JSONObject data) {
        Log.e("HHH","received action "+action);
        switch (action){
            case "addTask":
                try {
                    addTask(data.getString("__"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case "deleteTask":
                try {
                    deleteTask(data.getString("__"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        return new String[0];
    }

    @Override
    public void setStateUpdateListenet(StateUpdateListener listener) {

        updateListener = listener;
        loadTaskList();
    }

    @Override
    protected void notifyUpdates(String... changes) {
        if(updateListener==null) return;
        updateListener.onStateChange(changes);
    }

    @Override
    public JSONObject JSONSerialize() {
//Log.e("$$$$",taskList.toString());
        JSONArray mJSONArray = new JSONArray(taskList);
        JSONObject stateObj = new JSONObject();
        try {
            stateObj.put("taskList",mJSONArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stateObj;

    }


}
