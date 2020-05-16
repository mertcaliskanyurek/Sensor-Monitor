package com.mertcaliskanyurek.sensormonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mertcaliskanyurek.sensormonitor.Data.DatabaseHelper;
import com.mertcaliskanyurek.sensormonitor.Sensor.SystemSensorManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class MonitorActivity extends AppCompatActivity {

    public static final String EXTRA_SENSOR_NAME = "SENSOR_NAME";
    public static final String EXTRA_SENSOR_TYPE = "SENSOR_TYPE";

    private Locale mLocale;
    private String mSensorName ;
    private int mSensorType;

    private Calendar mStartDate = null;
    private Calendar mFinishDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        mFinishDate = Calendar.getInstance();
        mSensorName = getIntent().getStringExtra(EXTRA_SENSOR_NAME);
        mLocale = getResources().getConfiguration().locale;
        if(mSensorName == null)
            mSensorName = "Sensor ";
        mSensorType = getIntent().getIntExtra(EXTRA_SENSOR_TYPE, SystemSensorManager.SENSOR_TYPES[0]);
        initView();
    }

    private void initView() {
        Map<Long,Integer> activityList;
        if(mStartDate == null) //get all activity
            activityList = new DatabaseHelper(this)
                    .getActivity(mSensorType);
        else //get activity filtered by date time
            activityList = new DatabaseHelper(this)
                    .getActivity(mSensorType, mStartDate.getTimeInMillis(), mFinishDate.getTimeInMillis());

        //init data view
        GraphView graph = (GraphView) findViewById(R.id.graph);
        DataPoint[] dataPoints = new DataPoint[activityList.size()];

        //init data list
        TextView tvActivityList = findViewById(R.id.textView_activity_list);
        String dateString;
        StringBuilder builder = new StringBuilder();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.SSS",mLocale);

        int i=0;
        for(Map.Entry<Long,Integer> entry: activityList.entrySet())
        {
            //set start date as first date if necessary
            if(mStartDate == null && i == 0) {
                mStartDate = Calendar.getInstance();
                mStartDate.setTimeInMillis(entry.getKey());
            }
            //init data list string

            dateString = formatter.format(entry.getKey());
            builder.append(i)
                    .append(" - ")
                    .append(getString(R.string.text_date)).append(" ")
                    .append(dateString).append("  |  ")
                    .append(getString(R.string.text_value))
                    .append(entry.getValue()).append("\n");

            //init data
            dataPoints[i] = new DataPoint(i,entry.getValue());
            i++;
        }

        tvActivityList.setText(builder.toString());

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);

        series.setAnimated(true);
        series.setDrawDataPoints(true);
        graph.setTitle(mSensorName);
        graph.removeAllSeries();
        graph.addSeries(series);
        //If I don't add this code, the graph cuts points after 8 on X axis
        graph.getGridLabelRenderer().setHumanRounding(false,true);

    }

    public void onSelectDateClick(final View viewBtn) {
        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mStartDate.set(Calendar.YEAR,year);
                mStartDate.set(Calendar.MONTH,month);
                mStartDate.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                mStartDate.set(Calendar.HOUR_OF_DAY,0);
                mStartDate.set(Calendar.MINUTE,0);

                mFinishDate.set(Calendar.YEAR,year);
                mFinishDate.set(Calendar.MONTH,month);
                mFinishDate.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                mFinishDate.set(Calendar.HOUR_OF_DAY,11);
                mFinishDate.set(Calendar.MINUTE,59);
                initView();

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", mLocale);
                Button btnFin = (Button) viewBtn;
                btnFin.setText(dateFormat.format(mStartDate.getTime()));
            }
        },year,month,day);
        dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.text_ok),dialog);
        dialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.text_cancel),dialog);
        dialog.show();
    }

    public void onSelectStartTimeClick(final View viewBtn) {
        showTimePickerDialog(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if(hourOfDay < mFinishDate.get(Calendar.HOUR_OF_DAY))
                {
                    mStartDate.set(Calendar.HOUR_OF_DAY,hourOfDay);
                    mStartDate.set(Calendar.MINUTE,minute);
                    initView();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", mLocale);
                    Button btnFin = (Button) viewBtn;
                    btnFin.setText(dateFormat.format(mStartDate.getTime()));
                }
                else
                    Toast.makeText(getApplicationContext(),R.string.err_invalid_start_time,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onSelectFinTimeClick(final View viewBtn) {
        showTimePickerDialog(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if(hourOfDay > mStartDate.get(Calendar.HOUR_OF_DAY))
                {
                    mFinishDate.set(Calendar.HOUR_OF_DAY,hourOfDay);
                    mFinishDate.set(Calendar.MINUTE,minute);
                    initView();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", mLocale);
                    Button btnFin = (Button) viewBtn;
                    btnFin.setText(dateFormat.format(mFinishDate.getTime()));
                }
                else
                    Toast.makeText(getApplicationContext(),R.string.err_invalid_finish_time,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTimePickerDialog(TimePickerDialog.OnTimeSetListener listener)
    {
        Calendar cal = Calendar.getInstance();
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(this, listener ,hourOfDay, minute,false);
        dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.text_ok),dialog);
        dialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.text_cancel),dialog);
        dialog.show();
    }

}
