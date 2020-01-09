package com.example.xonix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import static com.example.xonix.MainActivity.photo_mode_flag;

public class Surface extends SurfaceView implements SurfaceHolder.Callback{
    private Thread_for_game TFG;

    public static int WIDTH;
    public static int HEIGHT;
    public static int FIELD_HEIGHT;
    public static int FIELD_WIDTH;
    public static int POINT_SIZE;
    private static float xr, yr;
    private float xb, yb;

    static Bitmap[] bitmap = new Bitmap[10];

    public Surface(Context context) {
        super(context);

        getHolder().addCallback(this);
        initWidthAndHeight(context);

        if(photo_mode_flag) {
            bitmap[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_0), WIDTH, HEIGHT, false);
            bitmap[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_1), WIDTH, HEIGHT, false);
            bitmap[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_2), WIDTH, HEIGHT, false);
            bitmap[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_3), WIDTH, HEIGHT, false);
            bitmap[4] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_4), WIDTH, HEIGHT, false);
            bitmap[5] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_5), WIDTH, HEIGHT, false);
            bitmap[6] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_6), WIDTH, HEIGHT, false);
            bitmap[7] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_7), WIDTH, HEIGHT, false);
            bitmap[8] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_8), WIDTH, HEIGHT, false);
            bitmap[9] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_9), WIDTH, HEIGHT, false);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {//вызывается, когда surfaceView появляется на экране
        TFG = new Thread_for_game(getHolder());
        TFG.setRunning(true);
        TFG.start(); //запускает процесс в отдельном потоке
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        boolean retry = true;
        TFG.setRunning(false);
        while (retry) {
            try {
                TFG.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        float x;
        float y;
        int action = e.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: // нажатие
                xb = e.getX();
                yb = e.getY();
                break;
            case MotionEvent.ACTION_MOVE: // движение
                break;
            case MotionEvent.ACTION_UP: // отпускание
                x = e.getX();
                y = e.getY();
                xr = x - xb;
                yr = y - yb;
                break;
            case MotionEvent.ACTION_CANCEL:

                break;
        }
        return true;
    }

    public static void setXr(float x) {
        xr = x;
    }
    public static void setYr(float y) {
        yr = y;
    }

    public static float getXr() {return xr;}

    public static float getYr() {return yr;}


    private void initWidthAndHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert windowManager != null;
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        WIDTH = point.x;
        HEIGHT = point.y;
        POINT_SIZE = (int) WIDTH / 54;
        FIELD_WIDTH = WIDTH / POINT_SIZE;
        FIELD_HEIGHT = HEIGHT / POINT_SIZE;
    }
}
