package dev.edmt.todolist.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"broadcast received",Toast.LENGTH_SHORT).show();
        Intent nintent = new Intent(context, Pipeline.class);
        nintent.putExtra("source","broadcast");
        context.startService(nintent);
    }
}
