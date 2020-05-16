package com.mertcaliskanyurek.sensormonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mertcaliskanyurek.sensormonitor.Service.SensorService;
import com.mertcaliskanyurek.sensormonitor.Service.ServiceListener;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ServiceListener {

    private boolean mServiceStarted = false;
    private SensorService mService;
    private Map<String,ProgressBar> mProgressBars;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {

    }

    private void updateViews() {
        Button btnService = findViewById(R.id.button_service_start_stop);
        btnService.setText(mServiceStarted?R.string.stop_service:R.string.start_service);
        LinearLayout llSensors = findViewById(R.id.linear_layout_sensors);
        llSensors.removeAllViews();
        if(mService != null)
        {
            List<Sensor> sensors = mService.getSensors();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mProgressBars = new Hashtable<>();
                for(final Sensor s:sensors)
                {
                    TextView tvName = new TextView(this);
                    tvName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1f));
                    ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
                    pb.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1f));

                    LinearLayout temp = new LinearLayout(this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0,10,0  ,10);
                    temp.setLayoutParams(layoutParams);
                    temp.setOrientation(LinearLayout.HORIZONTAL);
                    tvName.setText(s.getName());
                    tvName.setTextColor(Color.BLUE);
                    tvName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(MainActivity.this, MonitorActivity.class);
                            i.putExtra(MonitorActivity.EXTRA_SENSOR_TYPE,s.getType());
                            i.putExtra(MonitorActivity.EXTRA_SENSOR_NAME,s.getName());
                            startActivity(i);
                        }
                    });
                    mProgressBars.put(s.getName(),pb);
                    pb.setMin(0);
                    pb.setMax((int)s.getMaximumRange());
                    pb.setProgress((int)mService.getSensorValue(s.getName()));
                    temp.addView(tvName);
                    temp.addView(pb);
                    llSensors.addView(temp);
                }

            }
            else {
                for(Sensor s:sensors)
                {
                    TextView tvName = new TextView(this);
                    TextView tvValue = new TextView(this);
                    LinearLayout temp = new LinearLayout(this);
                    temp.setOrientation(LinearLayout.HORIZONTAL);

                    tvName.setText(s.getName());
                    tvValue.setText(""+mService.getSensorValue(s.getName()));
                    temp.removeAllViews();
                    temp.addView(tvName);
                    temp.addView(tvValue);
                    llSensors.addView(temp);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mService == null)
            bindService();
        else{
            mServiceStarted = mService.isServiceStarted();
            updateViews();
        }
    }

    private void bindService() {
        bindService(new Intent(this, SensorService.class), connection, Context.BIND_AUTO_CREATE);
    }

    public void onStartStopServiceClick(View view) {
        Intent intent= new Intent(this, SensorService.class);

        if(mServiceStarted)
            mService.stopService();
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent);
        else
            startService(intent);

        bindService(intent,connection,Context.BIND_AUTO_CREATE);
        mServiceStarted = !mServiceStarted;
        updateViews();
    }

    public ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.i("ServiceConnection","connected");
            mService = ((SensorService.ServiceBinder)binder).getService();
            mService.setListener(MainActivity.this);
            mServiceStarted = mService.isServiceStarted();
            updateViews();
        }
        //binder comes from server to communicate with method's of

        public void onServiceDisconnected(ComponentName className) {
            Log.i("ServiceConnection","disconnected");
            mService = null;
        }
    };

    //service on change method
    @Override
    public void onChanged() {
        updateViews();
    }
}
