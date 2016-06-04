package com.bupt.edison.scratchcard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ScratchView scratchView = (ScratchView) findViewById(R.id.scratchview);

        scratchView.setOnWipeListener(new ScratchView.OnWipeListener() {
            @Override
            public void onWipe(float progress) {
                if(progress>0.3){
                    scratchView.clearOverBitmap();
                }
            }
        });
    }

}
