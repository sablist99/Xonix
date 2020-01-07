package com.example.xonix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;

import static java.lang.Integer.valueOf;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    Intent intent;
    public static int REDRAW_TIME; //частота обновления экрана в мс
    public static Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//Приложение во весь экран

        final SeekBar seekBar = (SeekBar)findViewById(R.id.REDRAW_seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        REDRAW_TIME = valueOf(seekBar.getProgress());
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        intent = new Intent(this, GameActivity.class);
    }

    public void onClick(View view) {
        startActivity(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { REDRAW_TIME = valueOf(seekBar.getProgress()); if (REDRAW_TIME == 0) REDRAW_TIME = 1;}

}

