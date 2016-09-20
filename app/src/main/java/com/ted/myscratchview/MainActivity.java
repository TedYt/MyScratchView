package com.ted.myscratchview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ted.scratchview.MyScratchView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    /*@BindView(R.id.btn_reset)
    Button mBtnReset;*/

    @BindView(R.id.myscratchvew)
    MyScratchView mScratchView;

    @OnClick(R.id.btn_reset)
    void resetScratchView(){
        Log.d("tui","resetScratchView");
        mScratchView.reset();
    }

    @OnClick(R.id.btn_clean)
    void cleanScratchView(){
        mScratchView.clean();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
}
