package dev.edmt.todolist;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import dev.edmt.todolist.states.ListState;
import dev.edmt.todolist.util.Pipeline;

public class MainActivity extends AppCompatActivity implements Pipeline.OnStateChange {


    ArrayAdapter<String> mAdapter;
    ListView lstTask;
    Pipeline myService;
    ListState state = null;
    public ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.e("Activity","service connected");
            myService = ((Pipeline.PipelineBinder)binder).getService();
            state = (ListState) myService.switchContext("list");
            myService.setStateChangeListener(MainActivity.this);
        }
        //binder comes from server to communicate with method's of

        public void onServiceDisconnected(ComponentName className) {
            android.util.Log.d("ServiceConnection","disconnected");
            myService = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lstTask = (ListView)findViewById(R.id.lstTask);

//        loadTaskList();
    }

    private void loadTaskList(ArrayList<String> taskList) {
        if(mAdapter==null){
            mAdapter = new ArrayAdapter<String>(this,R.layout.row,R.id.task_title,taskList);
            lstTask.setAdapter(mAdapter);
        }
        else{
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);

        //Change menu icon color
        Drawable icon = menu.getItem(0).getIcon();
        icon.mutate();
        icon.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_task:
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add New Task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                if(state!=null)state.addTask(task);
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .create();
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteTask(View view){
        View parent = (View)view.getParent();
        TextView taskTextView = (TextView)parent.findViewById(R.id.task_title);
        Log.e("String", (String) taskTextView.getText());
        String task = String.valueOf(taskTextView.getText());
        state.deleteTask(task);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, Pipeline.class);
        intent.putExtra("state","list");
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        if (myService != null) {
            unbindService(myConnection);
        }

        super.onPause();
    }

    @Override
    public void remoteConnected() {

    }

    @Override
    public void remoteDisconnected() {

    }

    @Override
    public void onStateUpdate(String change) {
        switch(change){
            case "taskList":

                Log.d("ServiceConnection",state.JSONSerialize().toString());
                loadTaskList(state.getTaskList());
                break;
        }
    }
}
