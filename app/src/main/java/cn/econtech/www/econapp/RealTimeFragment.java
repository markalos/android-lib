package cn.econtech.www.econapp;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * Created by Admin-01 on 2017/12/18.
 *
 */

public class RealTimeFragment extends Fragment {

    private static final String TAG = "RealTimeFragment";


    private Redrawer redrawer;

    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.xyseries_layout, container, false);
        initPlotCanvas(view);
        return view;
    }

    private void initPlotCanvas(View view) {

        XYPlot xyPlot = view.findViewById(R.id.plotCanvas);
        eegModel = new EEGModel(100);
        LineAndPointFormatter formatter = new LineAndPointFormatter(
                R.color.off_white, null, null, null);
        formatter.setLegendIconEnabled(false);
        xyPlot.addSeries(eegModel,formatter);
        xyPlot.setDomainBoundaries(0, 100, BoundaryMode.FIXED);
        eegModel.setRendererRef(new WeakReference<>(
                xyPlot.getRenderer(AdvancedLineAndPointRenderer.class)
        ));
        redrawer = new Redrawer(xyPlot, 50, true);
        isRefreshing = true;
    }
    EEGModel eegModel;
    private boolean isRefreshing = false;
    public void pauseRefresh() {
        if (isRefreshing)
            redrawer.pause();
        else
            redrawer.start();
        isRefreshing = !isRefreshing;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        /*if (redrawer != null) {
            redrawer.pause();
            redrawer.start();
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " + handler);
        if (handler == null) {
            handler = new Handler();
        }
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                eegModel.updateData(generateData(6));
                handler.postDelayed(this, 50);
            }
        }, 1000);
    }

    private final Random random = new Random();
    private double [] generateData(int range) {
        int size = random.nextInt(range + 1) + 5;
        double [] res = new double[size];
        for (int i = 0; i < size; ++i) {
            res[i] = random.nextDouble();
        }
        return res;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        redrawer.pause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ");
        redrawer.finish();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        if (null != handler)
            handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged: ");
        if (isRefreshing) { //on background pausing, to improve performance
            if (isHidden()) {
                redrawer.pause();
            } else
                redrawer.start();
        }

    }
}
