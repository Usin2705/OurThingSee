package metro.ourthingsee.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import metro.ourthingsee.OurContract;
import metro.ourthingsee.R;
import metro.ourthingsee.RESTObjects.Events;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static metro.ourthingsee.Utils.setUpDatePicker;
import static metro.ourthingsee.Utils.simpleDateFormat;
import static metro.ourthingsee.fragments.LocationFragment.sdfDate;

public class GraphActivity extends AppCompatActivity {

    private static final int SMA_PERIOD = 5;
    /**
     * 1 hour they have 4 value (15 min each).
     * So the default value is 4 * 24  = 96
     */
    private static final int DEFAULT_POINTVALUES_SIZE = 96;
    TextView tvDate, tvGraphName, tvGraphSMA;
    Spinner spData;
    String[] datas, units;
    String[] sensorIds;
    ArrayAdapter<String> arrayAdapter;
    Button btnGo;
    Calendar calendar = Calendar.getInstance();
    ProgressDialog progressDialog;
    //graph properties
    LineChartView line;
    List<PointValue> pointValues;
    private long startTime;
    private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        datas = new String[]{getString(R.string.temp), getString(R.string.humid)
                , getString(R.string.lumi)};
        units = new String[]{" (\u2103)", " (%)", "(lux)"};
        sensorIds = new String[]{OurContract.SENSOR_ID_TEMPERATURE, OurContract.SENSOR_ID_HUMIDITY
                , OurContract.SENSOR_ID_LUMINANCE};
        addControls();
        addEvents();
        drawGraph(spData.getSelectedItemPosition());
    }

    private void addEvents() {
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpDatePicker(tvDate, calendar, GraphActivity.this);
            }
        });
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawGraph(spData.getSelectedItemPosition());
            }
        });
    }

    private void drawGraph(int selectedItemPosition) {
        pointValues.clear();
        final String sensorID = sensorIds[selectedItemPosition];
        SharedPreferences prefs = this.getSharedPreferences(OurContract.SHARED_PREF,
                Context.MODE_PRIVATE);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        endTime = calendar.getTimeInMillis();
        String authToken = "Bearer " + prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "");
        String authId = prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "");
        APIService apiService = AppUtils.getAPIService();
        fetchData(authToken, authId, sensorID, startTime, endTime, apiService);
    }

    private void fetchData(final String authToken, final String authId, final String sensorID,
                           final Long startTime, final Long endTime, final APIService apiService) {
        progressDialog.show();
        apiService.getUserEvents(
                authToken, authId, "sense", sensorID, 50, startTime, endTime).
                enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        switch (response.code()) {
                            case 200:
                                for (int i = 0; i < response.body().getEvents().size(); i++) {
                                    for (int j = 0; j < response.body().getEvents().get(i).getCause()
                                            .getSenses().size(); j++) {
                                        if (response.body().getEvents().get(i).getCause()
                                                .getSenses().get(j).getSId().equals(sensorID)) {
                                            Date date = new Date(response.body().getEvents().get(i)
                                                    .getCause().getSenses().get(j).getTs());
                                            Calendar cal = Calendar.getInstance();
                                            cal.setTime(date);
                                            float time = (float) cal.get(Calendar.HOUR_OF_DAY) +
                                                    ((float) cal.get(Calendar.MINUTE)) / 60;
                                            double value = response.body().getEvents().get(i)
                                                    .getCause().getSenses().get(j).getVal();
                                            if (pointValues.size() == 0) {
                                                PointValue point = new PointValue(time, (float) value);
                                                pointValues.add(point);
                                            } else if (pointValues.size() > 0 && time <
                                                    pointValues.get(pointValues.size() - 1).getX()) {
                                                PointValue point = new PointValue(time, (float) value);
                                                pointValues.add(point);
                                            }
                                        }
                                    }
                                }
                                if (response.body().getEvents().size() == 50) {
                                    fetchData(authToken, authId, sensorID, startTime, response.body()
                                            .getEvents().get(49).getTimestamp() - 1, apiService);
                                }
                                if (response.body().getEvents().size() < 50) {
                                    if (pointValues.size() == 0)
                                        Toast.makeText(GraphActivity.this, R.string.nodatafound,
                                                Toast.LENGTH_SHORT).show();
                                    showGraph();
                                    progressDialog.dismiss();
                                }
                                break;
                            case 503:
                                fetchData(authToken, authId, sensorID, startTime, endTime, apiService);
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        Toast.makeText(GraphActivity.this,
                                getString(R.string.login_toast_login_failed_nointernet),
                                Toast.LENGTH_SHORT).show();
                        findViewById(R.id.labels).setVisibility(View.GONE);
                        progressDialog.dismiss();
                    }
                });
    }

    private void showGraph() {
        //reverse list to get pointValues in time order 0:00 -> 24:00
        Collections.reverse(pointValues);
        //continue processing the graph
        if (pointValues.size() > 0) {

            // Smooth the graph by using SMA
            if (pointValues.size() > SMA_PERIOD) {
                smoothGraphBySMA();
            }

            findViewById(R.id.labels).setVisibility(View.VISIBLE);
            //minX and maxX are used for better-looking graph
            float minX = pointValues.get(0).getX() - 0.25f;
            if (minX < 0)
                minX = 0;
            float maxX = pointValues.get(pointValues.size() - 1).getX() + 0.25f;
            if (maxX > 24)
                maxX = 24;
            // lineGraph is the line represent the value gotten from server
            Line lineGraph = new Line(pointValues).setColor(Color.parseColor("#5D4037"))
                    .setStrokeWidth(1);
            // if there is only one point gotten then show the point, else show the line of points
            if (pointValues.size() > 1)
                lineGraph.setHasPoints(false);
            else if (pointValues.size() == 1) {
                lineGraph.setHasPoints(true);
                Toast.makeText(this, R.string.oneValFound, Toast.LENGTH_SHORT).show();
            }
            //list of lines
            List<Line> lines = new ArrayList<>();
            lines.add(lineGraph);
            //data of graph
            LineChartData data = new LineChartData();
            data.setLines(lines);           //add list of lines to data
            //format the label of axis X to get the format of time
            List<AxisValue> axisValuesX = new ArrayList<>();
            for (float i = minX; i < maxX; i += 0.25f) {
                axisValuesX.add(new AxisValue(i).setLabel(formatTimes(i)));
            }
            // add 2 axis to the graph
            Axis axisX = new Axis(axisValuesX).setHasLines(true).setName("Hours of Day")
                    .setMaxLabelChars(4).setTextColor(Color.parseColor("#5D4037"));
            Axis axisY = new Axis().setHasLines(true).setName(datas
                    [spData.getSelectedItemPosition()] + units[spData.getSelectedItemPosition()])
                    .setMaxLabelChars(4).setTextColor(Color.parseColor("#daf7171b"));
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
            //draw graph
            line.setLineChartData(data);
            //custom viewport for better-looking graph
            Viewport v = line.getMaximumViewport();
            if (v.left - 0.25f < 0) v.left = 0;
            else v.left -= 0.25f;
            if (v.right + 0.25f > 24) v.right = 24;
            else v.right += 0.25f;
            setViewportTopBot(v, line);
            // add the minimum value to the graph
            if (spData.getSelectedItemPosition() != 0) {
                List<PointValue> minVal = new ArrayList<>();
                SharedPreferences prefs = this.getSharedPreferences(OurContract.SHARED_PREF,
                        Context.MODE_PRIVATE);
                switch (spData.getSelectedItemPosition()) {
                    case 1:
                        minVal.add(new PointValue(v.left, prefs.getInt(OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE, OurContract.DEFAULT_MIN_HUMIDITY_VALUE)));
                        minVal.add(new PointValue(v.right, prefs.getInt(OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE, OurContract.DEFAULT_MIN_HUMIDITY_VALUE)));
                        findViewById(R.id.min).setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        minVal.add(new PointValue(v.left, prefs.getInt(OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE, OurContract.DEFAULT_MIN_LIGHT_VALUE)));
                        minVal.add(new PointValue(v.right, prefs.getInt(OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE, OurContract.DEFAULT_MIN_LIGHT_VALUE)));
                        findViewById(R.id.min).setVisibility(View.VISIBLE);
                        break;
                }
                lines.add(new Line(minVal).setColor(Color.RED)
                        .setStrokeWidth(3).setFilled(true).setHasPoints(false));
                data.setLines(lines);
                line.setLineChartData(data);
                setViewportTopBot(v, line);
            } else findViewById(R.id.min).setVisibility(View.GONE);
        } else {
            Axis axisX = new Axis().setHasLines(true).setName("Hours of Day")
                    .setTextColor(Color.parseColor("#5D4037")).setMaxLabelChars(4);
            Axis axisY = new Axis().setHasLines(true).setName(datas
                    [spData.getSelectedItemPosition()] + units[spData.getSelectedItemPosition()])
                    .setTextColor(Color.parseColor("#daf7171b")).setMaxLabelChars(4);
            LineChartData data = new LineChartData();
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
            line.setLineChartData(data);
            findViewById(R.id.labels).setVisibility(View.GONE);
        }
        tvGraphName.setText("Graph of " + datas[spData.getSelectedItemPosition()].toLowerCase()
                + " in " + simpleDateFormat.format(calendar.getTime()));

        ((TextView) findViewById(R.id.tvLineName)).setText
                (datas[spData.getSelectedItemPosition()] + " graph");
    }

    /**
     * Smooth the map by using simple moving average method.
     * The period of the SMA is decided by {@link #SMA_PERIOD}
     * <p>
     * NOTICE: the data type here is float just so it can be the same as {@link PointValue}     *
     */
    private void smoothGraphBySMA() {
        Queue<Float> window = new LinkedList<>();
        float sum = 0;
        int smaPeriod = SMA_PERIOD;

        // If the user measure time is not 15 min but shorter (for example 1min)
        // then we increase the SMA PERIOD
        // - 1 as if the sample size just 49 time, it should not increase SMA period
        if (pointValues.size() > DEFAULT_POINTVALUES_SIZE) {
            smaPeriod = (pointValues.size() % DEFAULT_POINTVALUES_SIZE) + SMA_PERIOD - 1;
        }


        // Loop through all data
        for (PointValue pointValue : pointValues) {


            // Sum += new number in the queue
            sum += pointValue.getY();

            // Add that number to the queue
            window.add(pointValue.getY());

            // If by adding that number, the window size > SMA_PERIOD
            // then remove the old one in the queue.
            // Also sum -= the old one. So the new sum is just like the formula (SMA_PERIOD = 5):
            // Sum(old) = n1 + n2 + n3 + n4 + n5
            // Sum (new) = n1 + n2 + n3 + n4 + n5 + n6 - n1
            // Sum (new) = n2 + n3 + n4 + n5 + n6
            if (window.size() > smaPeriod) {
                sum -= window.remove();
            }

            // Also, since we have the five value for average, replace the raw value with
            //  the average result
            // (If the windows size is less than smaPeriod, it still be averaged with the total
            // current size (if they have 3 values then it will average by 3)
            pointValue.set(pointValue.getX(), sum / window.size());
        }

        tvGraphSMA.setText(getString(R.string.graph_sma_des, String.valueOf(smaPeriod)));
    }


    private void setViewportTopBot(Viewport v, LineChartView line) {
        switch (spData.getSelectedItemPosition()) {
            case 0:
                v.top = v.top + 1;
                v.bottom = v.bottom - 1;
                break;
            case 1:
                if (v.top + 1 > 100) v.top = 100;
                else v.top += 1;
                if (v.bottom - 1 < 0) v.bottom = 0;
                else v.bottom -= 1;
                break;
            case 2:
                if (v.bottom - 50 < 0) v.bottom = 0;
                else v.bottom -= 50;
                break;
        }
        v.set(v.left, v.top, v.right, v.bottom);
        line.setMaximumViewport(v);
        line.setCurrentViewport(v);
    }

    private String formatTimes(float value) {
        StringBuilder sb = new StringBuilder();
        int timeInMin = (int) (value * 60);
        int hour = timeInMin / 60;
        int min = timeInMin % 60;
        sb.append(String.valueOf(hour)).append(":");
        if (min < 10)
            sb.append("0");
        sb.append(String.valueOf(min));
        return sb.toString();
    }

    private void addControls() {
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvGraphName = (TextView) findViewById(R.id.tvGraphName);
        tvGraphSMA = (TextView) findViewById(R.id.tvGraphSMA);
        tvDate.setText(sdfDate.format(calendar.getTime()));
        btnGo = (Button) findViewById(R.id.btnGo);
        spData = (Spinner) findViewById(R.id.spData);
        arrayAdapter = new ArrayAdapter<>(GraphActivity.this,
                R.layout.support_simple_spinner_dropdown_item, datas);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spData.setAdapter(arrayAdapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.graphing));
        line = (LineChartView) findViewById(R.id.line);
        line.setZoomType(ZoomType.HORIZONTAL);
        pointValues = new ArrayList<>();
    }
}
