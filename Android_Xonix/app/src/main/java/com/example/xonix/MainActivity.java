package com.example.xonix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
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
    public static int PERCENT_OF_WATER_CAPTURE;
    public static Vibrator vibrator;

    SeekBar seekBar;
    SeekBar Percent_seekBar;
    Switch Bonus_switch;
    Switch Add_life_switch;
    Switch Photo_mode_switch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//Приложение во весь экран

        seekBar = (SeekBar)findViewById(R.id.REDRAW_seekBar);
        Percent_seekBar = (SeekBar)findViewById(R.id.Percent_seekBar);
        Bonus_switch = (Switch)findViewById(R.id.Bonus_switch);
        Add_life_switch = (Switch)findViewById(R.id.Add_life_switch);
        Photo_mode_switch = (Switch)findViewById(R.id.Photo_mode_switch);

        seekBar.setOnSeekBarChangeListener(this);

//BIND Music Service
        doBindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        startService(music);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        intent = new Intent(this, GameActivity.class);

    }

    public void onClick(View view) {
        bonus_flag = Bonus_switch.isChecked();
        add_life_flag = Add_life_switch.isChecked();
        photo_mode_flag = Photo_mode_switch.isChecked();
        REDRAW_TIME = valueOf(seekBar.getProgress());
        PERCENT_OF_WATER_CAPTURE = 5 * valueOf(Percent_seekBar.getProgress()) + 50;
        startActivity(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                Scon,Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(Scon);
            mIsBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Detect idle screen
        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //UNBIND music service
        doUnbindService();
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        stopService(music);

    }
}

