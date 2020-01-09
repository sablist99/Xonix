package com.example.xonix;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    Surface surface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
            Особенность класса SurfaceView заключается в том, что он предоставляет отдельную
            область для рисования, действия с которой должны быть вынесены в отдельный поток
            приложения. Таким образом, приложению не нужно ждать, пока система будет готова
            к отрисовке всей иерархии view-элементов. Вспомогательный поток может использовать
            canvas нашего SurfaceView для отрисовки с той скоростью, которая необходима.

            Вся реализация сводится к двум основным моментам:
            Создание класса, унаследованного от SurfaceView
            и реализующего интерфейс SurfaceHolder.Callback
            Создание потока, который будет управлять отрисовкой.
         */
        surface = new Surface(this);
        setContentView(surface);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//Приложение во весь экран
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        surface = null;
    }
}
