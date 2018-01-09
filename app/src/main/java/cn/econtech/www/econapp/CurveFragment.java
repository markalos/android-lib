package cn.econtech.www.econapp;

import android.app.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public class CurveFragment extends Fragment {
    private static final String TAG = CurveFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.walk_in_layout, container, false);
 /*       final MediaController mc = new MediaController(this);
        view.findViewById(R.id.walkInGif).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaController.show();
                    }
                }
        );*/
        return view;
    }

    @Override
    public void onStart() {


        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }
}
