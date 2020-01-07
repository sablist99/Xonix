package com.example.xonix;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Random;

import static com.example.xonix.Surface.FIELD_HEIGHT;
import static com.example.xonix.Surface.FIELD_WIDTH;
import static com.example.xonix.Surface.POINT_SIZE;
import static com.example.xonix.Surface.getXr;
import static com.example.xonix.Surface.getYr;


public class Thread_for_game extends Thread {
    //Поток для прорисовки анимации

    private boolean tfg_Running; //запущен ли процесс
    private long tfg_StartTime; //время начала анимации
    private long tfg_PrevRedrawTime; //предыдущее время перерисовки
    private final SurfaceHolder tfg_SurfaceHolder; //нужен, для получения canvas
    private final int REDRAW_TIME    = 10; //частота обновления экрана - 10 мс
    private final int PERCENT_OF_WATER_CAPTURE = 75;
    private final int SLEEP_FOR_NEW_LVL = 1000;

    private Field field = new Field();
    private Random random = new Random();
    private Xonix xonix = new Xonix();
    private Balls balls = new Balls();
    private Cube cube = new Cube();
    private Bonuses bonuses = new Bonuses();

    final String COLOR_BONUS = "#a8ff00";
    final String COLOR_WATER = "#00012d";
    final String COLOR_LAND = "#17ab75";
    final String COLOR_TRACK = "#00c6ff";
    final String COLOR_WHITE = "#ffffff";
    final String COLOR_BALL = "#e81b3d";


    public Thread_for_game(SurfaceHolder holder) {
        tfg_SurfaceHolder = holder;
        tfg_Running = false;
    }

    public void setRunning(boolean running) { //запускает и останавливает процесс
        tfg_Running = running;
        tfg_PrevRedrawTime = getTime();
    }

    public long getTime() {
        return System.nanoTime() / 1_000_000;
    }

    @Override
    public void run() {
        Canvas canvas;
        tfg_StartTime = getTime();
            while (tfg_Running) {
                long curTime = getTime();
                long elapsedTime = curTime - tfg_PrevRedrawTime;
                if (elapsedTime < REDRAW_TIME) //проверяет, прошло ли 10 мс
                    continue;
                //если прошло, перерисовываем картинку
                canvas = null;
                try {
                    canvas = tfg_SurfaceHolder.lockCanvas(); //получаем canvas
                    synchronized (tfg_SurfaceHolder) {
                        //Игровой цикл
                        xonix.move();
                        balls.move();
                        cube.move();
                        bonuses.add();
                        bonuses.check_collect_bonus();
                        draw(canvas);
                        if (xonix.isSelfCrosed() || balls.isHitTrackOrXonix() || cube.isHitXonix()) {//Если Xonix ранен
                            xonix.decreaseCountLives();
                            if (xonix.getCountLives() > 0) { //Есди игра продолжается
                                xonix.init();
                                field.clearTrack();
                            } else new_game();//Если новая игра
                        }
                        if (field.getCurrentPercent() >= PERCENT_OF_WATER_CAPTURE) {
                            //Thread_for_game.sleep(SLEEP_FOR_NEW_LVL);
                            field.init();
                            xonix.init();
                            xonix.level_up();
                            cube.init();
                            balls.add();
                        }
                    }
                } catch (NullPointerException e) {
                //если canvas не доступен
                } finally {
                    if (canvas != null) {
                        tfg_SurfaceHolder.unlockCanvasAndPost(canvas); //освобождаем canvas
                        // Если добавить System.out.print(" "); , то почему-то перестает тормозить
                        System.out.print(" ");
                    }
                }
                tfg_PrevRedrawTime = curTime;
            }
    }

    class Field {
        /*
        1 - Земля
        2 - Вода
        3 - Трек
        4 - Temp
        */
        private final int WATER_AREA = (FIELD_WIDTH - 4)*(FIELD_HEIGHT - 4);
        private int[][] field = new int[FIELD_WIDTH][FIELD_HEIGHT];
        private float currentWaterArea;
        private int countScore = 0;

        Field() {
            init();
        }

        void init() {
            for (int y = 0; y < FIELD_HEIGHT; y++)
                for (int x = 0; x < FIELD_WIDTH; x++)
                    if(x < 2 || x > FIELD_WIDTH - 3 || y < 2 || y > FIELD_HEIGHT - 3)
                        field[x][y] = 1;
                    else field[x][y] = 2;
            currentWaterArea = WATER_AREA;
        }

        int getColor(int x, int y) {
            if (x >= 0 && x < FIELD_WIDTH && y >= 0 && y < FIELD_HEIGHT)
                return field[x][y];
            else return 0;
        }

        void setColor(int x, int y, int color) { field[x][y] = color; }

        void setCountScore(int c) { countScore = c; }

        int getCountScore() { return countScore; }
        float getCurrentPercent() { return ((float) Math.round((100f - currentWaterArea / WATER_AREA * 100) * 100) / 100); }

        void clearTrack() { // clear track of Xonix
            for (int y = 0; y < FIELD_HEIGHT; y++)
                for (int x = 0; x < FIELD_WIDTH; x++)
                    if (field[x][y] == 3) field[x][y] = 2;
        }

        void fillTemporary(int x, int y) {
            if (field[x][y] != 2) return;
            field[x][y] = 4; //Отделение области с шариком
            for (int dx = -1; dx < 2; dx++)
                for (int dy = -1; dy < 2; dy++) fillTemporary(x + dx, y + dy);
        }

        void tryToFill() {//Заполняем зону
            currentWaterArea = 0;
            for (Ball ball : balls.getBalls()) fillTemporary(ball.getX(), ball.getY());
            for (int y = 0; y < FIELD_HEIGHT; y++)
                for (int x = 0; x < FIELD_WIDTH; x++) {
                    if (field[x][y] == 3 || field[x][y] == 2) {
                        field[x][y] = 1;
                        countScore += 10;
                    }
                    if (field[x][y] == 4) {
                        field[x][y] = 2;
                        currentWaterArea++;
                    }
                }
        }

        void paint(Canvas canvas) {
            Rect myRect = new Rect();
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            for (int y = 0; y < FIELD_HEIGHT; y++)
                for (int x = 0; x < FIELD_WIDTH; x++) {
                    switch (field[x][y]){
                        case 0:
                            break;
                        case 1:
                            paint.setColor(Color.parseColor(COLOR_LAND));
                            break;
                        case 2:
                            paint.setColor(Color.parseColor(COLOR_WATER));
                            break;
                        case 3:
                            paint.setColor(Color.parseColor(COLOR_TRACK));
                            break;
                        default:
                            break;
                    }
                    myRect.set(x * POINT_SIZE, y * POINT_SIZE, x * POINT_SIZE + POINT_SIZE, y * POINT_SIZE + POINT_SIZE);
                    canvas.drawRect(myRect, paint);
                }
        }
    }

    class Xonix {

        private int x, y, countLives = 3, level = 1;
        private boolean isWater, isSelfCross;

        Xonix() {
            init();
        }

        void init() {
            y = 0;
            x = FIELD_WIDTH / 2;
            Surface.setXr(0);
            Surface.setYr(0);
            isWater = false;
        }

        int getX() { return x; }
        int getY() { return y; }
        int getCountLives() { return countLives; }
        int getLevel() {return level;}

        void decreaseCountLives() { countLives--; }

        void setCountLives(int c) { countLives = c;}

        void setLevel(int l) { level = l;};

        void level_up() {level++;}

        void addCountLives() {countLives++;}

        void move() {
            if (Math.abs(getXr()) > Math.abs(getYr()))  {
                if(getXr() > 0) x++;
                if(getXr() < 0) x--;
            } else {
                if(getYr() > 0) y++;
                if(getYr() < 0) y--;
            }
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (y > FIELD_HEIGHT - 1) y = FIELD_HEIGHT - 1;
            if (x > FIELD_WIDTH - 1) x = FIELD_WIDTH - 1;
            isSelfCross = field.getColor(x, y) == 3;
            if (field.getColor(x, y) == 1 && isWater) {
                Surface.setXr(0);
                Surface.setYr(0);
                isWater = false;
                field.tryToFill();
            }
            if (field.getColor(x, y) == 2) {
                isWater = true;
                field.setColor(x, y, 3);
            }
        }

        boolean isSelfCrosed() { return isSelfCross; }

        void paint(Canvas canvas) {
            Rect rect = new Rect();
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(POINT_SIZE / 3);
            paint.setColor(Color.parseColor((field.getColor(x, y) == 1) ? COLOR_TRACK : COLOR_WHITE));
            rect.set(x * POINT_SIZE, y * POINT_SIZE, x * POINT_SIZE + POINT_SIZE, y * POINT_SIZE + POINT_SIZE);
            canvas.drawRect(rect, paint);
            rect.set(x * POINT_SIZE + POINT_SIZE / 2, y * POINT_SIZE + POINT_SIZE / 2, x * POINT_SIZE + POINT_SIZE / 2, y * POINT_SIZE + POINT_SIZE  / 2);
            canvas.drawRect(rect, paint);
        }
    }

    class Balls {
        private ArrayList<Ball> balls = new ArrayList<Ball>();

        Balls() {
            add();
        }

        int sizeOfArray () {
            return balls.size();
        }

        void add() { balls.add(new Ball()); }

        void move() { for (Ball ball : balls) ball.move(); }

        void remove() {
            if (balls.size() > 0) {
                balls.subList(0, balls.size()).clear();
            }
        }

        void removeOne() {
            balls.remove(1);
        }

        ArrayList<Ball> getBalls() { return balls; }

        boolean isHitTrackOrXonix() {
            for (Ball ball : balls) if (ball.isHitTrackOrXonix()) return true;
            return false;
        }

        void paint(Canvas canvas) { for (Ball ball : balls) ball.paint(canvas); }
    }

    class Ball {
        private int x, y, dx, dy;

        Ball() {
            do {
                x = random.nextInt(FIELD_WIDTH);
                y = random.nextInt(FIELD_HEIGHT);
            } while (field.getColor(x, y) == 1);
            dx = random.nextBoolean()? 1 : -1;
            dy = random.nextBoolean()? 1 : -1;
        }

        void updateDXandDY() {
            if (field.getColor(x + dx, y) == 1) dx = -dx;
            if (field.getColor(x, y + dy) == 1) dy = -dy;
        }

        void move() {
            updateDXandDY();
            x += dx;
            y += dy;
        }

        int getX() { return x; }
        int getY() { return y; }

        boolean isHitTrackOrXonix() {
            updateDXandDY();
            if (field.getColor(x + dx, y + dy) == 3) return true;
            return x + dx == xonix.getX() && y + dy == xonix.getY();
        }

        void paint(Canvas canvas) {
            Paint paint = new Paint();
            paint.setColor(Color.parseColor(COLOR_BALL));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(POINT_SIZE / 4);
            canvas.drawCircle(x * POINT_SIZE + POINT_SIZE / 2, y * POINT_SIZE + POINT_SIZE / 2, POINT_SIZE / 2, paint);
            canvas.drawCircle(x * POINT_SIZE + POINT_SIZE / 2, y * POINT_SIZE + POINT_SIZE / 2, POINT_SIZE / 10, paint);
        }
    }

    class Cube {
        private int x, y, dx, dy;

        Cube() {
            init();
        }

        void init() { x = dx = dy = 1; }

        void updateDXandDY() {
            //System.out.println(" " + x + " " + (x + dx) + " " + y + " " + (y + dy));
            if (field.getColor(x + dx, y) != 1) dx = -dx;
            if (field.getColor(x, y + dy) != 1) dy = -dy;
        }

        void move() {
            updateDXandDY();
            x += dx;
            y += dy;
        }

        boolean isHitXonix() {
            updateDXandDY();
            return x + dx == xonix.getX() && y + dy == xonix.getY();
        }

        void paint (Canvas canvas) {
            Rect myRect = new Rect();
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(POINT_SIZE / 3);
            myRect.set(x * POINT_SIZE, y * POINT_SIZE, x * POINT_SIZE + POINT_SIZE, y * POINT_SIZE + POINT_SIZE);
            canvas.drawRect(myRect, paint);
            myRect.set(x * POINT_SIZE + POINT_SIZE / 2, y * POINT_SIZE + POINT_SIZE / 2, x * POINT_SIZE + POINT_SIZE / 2, y * POINT_SIZE + POINT_SIZE / 2);
            canvas.drawRect(myRect, paint);
        }
    }

    class Bonuses {
        /*
        type = 1 - добавление жизни
        type = 2 - удаление врага на море
         */
        private ArrayList<Bonus> bonuses = new ArrayList<Bonus>();
        private int step = 0;

        void add() {
            if (field.getCountScore() > 30000 * step + 30000) {
                int type, count = 1;
                for (Bonus bonus : bonuses) {
                    if (bonus.getType() == 2) {
                        count++;
                    }
                }
                if (balls.sizeOfArray() == count) {
                    type = 1;
                } else {
                    Random random = new Random();
                    type = random.nextInt(2) + 1;
                }
                bonuses.add(new Bonus(type));
                step++;
            }
        }

        void remove() {
            if (bonuses.size() > 0) {
                bonuses.subList(0, bonuses.size()).clear();
            }
            step = 0;
        }

        int sizeOfArray () {
            return bonuses.size();
        }

        void check_collect_bonus() {
            for (Bonus bonus : bonuses) {
                if (field.getColor(bonus.getX(),bonus.getY()) == 1) {
                    if (bonus.getType() == 1) {
                        xonix.addCountLives();
                        bonuses.remove(bonus);
                        break;
                    }
                    if (bonus.getType() == 2) {
                        balls.removeOne();
                        bonuses.remove(bonus);
                        break;
                    }
                }
            }
        }

        void paint(Canvas canvas) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor(COLOR_BONUS));
            paint.setStrokeWidth(POINT_SIZE / 4);
            for (Bonus bonus : bonuses) {
                int x = bonus.getX();
                int y = bonus.getY();
                switch (bonus.getType()) {
                    case 1:
                        canvas.drawLine(x * POINT_SIZE + POINT_SIZE / 2, y * POINT_SIZE, x* POINT_SIZE + POINT_SIZE / 2, (y + 1) * POINT_SIZE, paint);
                        canvas.drawLine(x * POINT_SIZE, y * POINT_SIZE + POINT_SIZE / 2, (x + 1) * POINT_SIZE, y * POINT_SIZE + POINT_SIZE / 2, paint);

                        break;
                    case 2:
                        canvas.drawLine(x * POINT_SIZE, y * POINT_SIZE + POINT_SIZE / 2, (x + 1) * POINT_SIZE, y * POINT_SIZE + POINT_SIZE / 2, paint);
                        break;
                }
            }
        }
    }

    class Bonus {
        private int x, y, type;

        Bonus (int type) {
            Random random = new Random();
            do {
                x = random.nextInt(FIELD_WIDTH);
                y = random.nextInt(FIELD_HEIGHT);
            } while (field.getColor(x, y) == 1);
            this.type = type;
        }

        int getX() {return x;}
        int getY() {return y;}
        int getType() {return type;}
    }

    private void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setStyle(Paint.Style.STROKE);
        field.paint(canvas);
        xonix.paint(canvas);
        balls.paint(canvas);
        cube.paint(canvas);
        bonuses.paint(canvas);

        canvas.drawBitmap(Surface.settings, 3 * POINT_SIZE, 3 * POINT_SIZE, paint);
        canvas.drawText("Level: " + xonix.getLevel() + "  Score: " + field.getCountScore() + "  Lives: " + xonix.getCountLives() + "  Full: " + field.getCurrentPercent() + "%", POINT_SIZE * 2, POINT_SIZE * (FIELD_HEIGHT - 2) - 3, paint);
    }

    private void new_game() {
        xonix.init();
        xonix.setCountLives(3);
        xonix.setLevel(1);
        balls.remove();
        balls.add();
        bonuses.remove();
        cube.init();
        field.init();
        field.setCountScore(0);
    }
}
