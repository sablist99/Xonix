package com.example.xonix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class Surface extends SurfaceView implements SurfaceHolder.Callback{
    private Thread_for_game TFG;

    public static int WIDTH;
    public static int HEIGHT;
    public static int FIELD_HEIGHT;
    public static int FIELD_WIDTH;
    public static int POINT_SIZE;
    public static float xr, yr;
    private float xb, yb;
    public static Bitmap settings;
    private boolean is_running = true;
    public static Context mContext;


    public Surface(Context context) {
        super(context);

        mContext = context;
        getHolder().addCallback(this);
        initWidthAndHeight(context);
        settings = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.settings), 5 * POINT_SIZE, 4 * POINT_SIZE, false);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {//вызывается, когда surfaceView появляется на экране
        TFG = new Thread_for_game(getHolder());
        TFG.setRunning(true);
        TFG.start(); //запускает процесс в отдельном потоке
        //is_running = true;
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
        if (e.getAction() == MotionEvent.ACTION_UP && (e.getX() >= 3 * POINT_SIZE) && (e.getX() <= 8 * POINT_SIZE) && (e.getY() >= 3 * POINT_SIZE) && (e.getY() <= 7 * POINT_SIZE))
        {
            /*if (is_running) TFG.setRunning(false);
                else TFG.setRunning(true);
            is_running = !is_running;

            System.out.println("tut " + is_running);

             */
            /*MainActivity.setContenView()
            Intent intent = new Intent(mContext, MainActivity.class);
            mContext.startActivity(intent);
*/
            return true;
        }
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
