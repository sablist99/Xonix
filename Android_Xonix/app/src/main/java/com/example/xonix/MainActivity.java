package com.example.xonix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Switch;

import static java.lang.Integer.valueOf;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    Intent intent;
    public static int REDRAW_TIME; //частота обновления экрана в мс
    public static boolean bonus_flag;
    public static boolean add_life_flag;
    public static boolean photo_mode_flag;
    public static Vibrator vibrator;

    SeekBar seekBar;// = (SeekBar)findViewById(R.id.REDRAW_seekBar);
    Switch Bonus_switch;// = (Switch)findViewById(R.id.Bonus_switch);
    Switch Add_life_switch;// = (Switch)findViewById(R.id.Add_life_switch);
    Switch Photo_mode_switch;// = (Switch)findViewById(R.id.Photo_mode_switch);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//Приложение во весь экран

        seekBar = (SeekBar)findViewById(R.id.REDRAW_seekBar);
        Bonus_switch = (Switch)findViewById(R.id.Bonus_switch);
        Add_life_switch = (Switch)findViewById(R.id.Add_life_switch);
        Photo_mode_switch = (Switch)findViewById(R.id.Photo_mode_switch);

        seekBar.setOnSeekBarChangeListener(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        intent = new Intent(this, GameActivity.class);
    }

    public void onClick(View view) {
        bonus_flag = Bonus_switch.isChecked();
        add_life_flag = Add_life_switch.isChecked();
        photo_mode_flag = Photo_mode_switch.isChecked();
        REDRAW_TIME = valueOf(seekBar.getProgress());
        startActivity(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { /*REDRAW_TIME = valueOf(seekBar.getProgress()); if (REDRAW_TIME == 0) REDRAW_TIME = 1;*/}

}

