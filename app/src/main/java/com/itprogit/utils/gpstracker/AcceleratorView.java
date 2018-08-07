package com.itprogit.utils.gpstracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.LinkedList;

public class AcceleratorView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
    private PointDrawer pointer;
    private double xDel, yDel, newX, newY, newZ;
    public LinkedList<BoundingDotLine> boundingDotLines;
    private float[] rotationMatrix, accelerometer, geomagnesis, orientation, gyroscope;
    private SensorManager sensorManager;
    private Activity activity;
    private double areaFull, perimeterFull;
    private ReferencedObjectReferenced etaloneObj;
    private boolean leftRound = true, rightRound = true;
    boolean active = false, denactive = false;
    private boolean isLocked, isStarted, isRequestDrawing = true, isSetReferenced;
    private Camera camera;

    private Vibrator vibrator;

    private float width, height;

    public AcceleratorView(Context context, Activity activity, LinkedList<BoundingDotLine> boundingDotLines, ReferencedObjectReferenced etalone, Vibrator vibrator, Camera camera) {
        super(context);

        getHolder().addCallback(this);

        this.boundingDotLines = boundingDotLines;
        this.activity = activity;
        this.etaloneObj = etalone;
        this.vibrator = vibrator;
        this.camera = camera;

        rotationMatrix = new float[16];
        accelerometer = new float[3];
        geomagnesis = new float[3];
        orientation = new float[3];
        gyroscope = new float[3];

        sensorManager =  (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
    }

    int point;
    boolean isMoving = false, isTouching;
    double lastXPos, lastYPos;
    double startTime, endTime;

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        endTime = System.currentTimeMillis();
//
//        if (endTime - startTime > 1250 && startTime > 0 && isTouching) {
//            new Thread() {
//                @Override
//                public void run() {
//                    while (isTouching) {
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                        activity.runOnUiThread(new Runnable () {
//                            @Override
//                            public void run () {
//                                if (!isLocked) {
//                                    boundingDotLines.add(new BoundingDotLine(orientation[2], orientation[1], orientation[0]));
//                                    boundingDotLines.get(boundingDotLines.size()-1).setCircleSize(10);
//                                }
//                            }
//                        });
//                    }
//                }
//            }.start();
//        }
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                for (int i = 0; i < boundingDotLines.size(); i++)
//                    if (inCircle(boundingDotLines.get(i).getXPoint(),
//                            boundingDotLines.get(i).getYPoint(), event.getX(), event.getY(), 200, 0)) {
//                        point = i;
//                        lastXPos = boundingDotLines.get(point).getXPoint();
//                        lastYPos = boundingDotLines.get(point).getYPoint();
//                        isMoving = true;
//                    }
//
//                startTime = System.currentTimeMillis();
//                isTouching = true;
//
//                if (!isMoving) {
//                    float x, y, z;
//
//                    x = Math.round(Math.toDegrees(orientation[2])*1000)/1000;
//                    y = Math.round(Math.toDegrees(orientation[1])*1000)/1000;
//                    z = Math.round(Math.toDegrees(orientation[0])*1000)/1000;
//
//                    setFocus();
//
//                    //new Thread(new AcceleratorModel.AccelerometerStableFit(x, y, z)).start();
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (isMoving) {
//                    isRequestDrawing = false;
//                    boundingDotLines.get(point).setXPoint(event.getX());
//                    boundingDotLines.get(point).setYPoint(event.getY());
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                if (isMoving) {
//                    isMoving = false;
//
//                    double xPos = (boundingDotLines.get(point).getXPoint() - lastXPos)/boundingDotLines.get(0).getRelativePlaceX();
//                    double yPos = (boundingDotLines.get(point).getYPoint() - lastYPos)/boundingDotLines.get(0).getRelativePlaceY();
//
//                    boundingDotLines.get(point).setLastX(boundingDotLines.get(point).getX() + xPos);
//                    boundingDotLines.get(point).setLastY(boundingDotLines.get(point).getY() + yPos);
//
//                    getArea();
//                    getPerimeter();
//                    getAngles();
//                }
//
//                startTime = 0;
//
//                isRequestDrawing = true;
//                isTouching = false;
//
//                break;
//        }
//
//        return true;
//    }

    public void setFocus () {
        try {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        sensorManager.registerListener (this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener (this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener (this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener (this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);

        pointer = new PointDrawer(getContext(), surfaceHolder);
        pointer.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;

        sensorManager.unregisterListener(this);

        pointer.requestStop();

        while (retry) {
            try {
                pointer.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    private boolean inCircle (double circleCenterX, double circleCenterY, double dotCenterX, double dotCenterY, double rad1, double rad2) {
        return (Math.pow (dotCenterX - circleCenterX, 2) + Math.pow (dotCenterY - circleCenterY, 2)) <= Math.pow (rad1 + rad2, 2);
    }

    protected class PointDrawer extends Thread {
        private volatile boolean running = true, isFilling;
        private SurfaceHolder holder;
        private Paint paint;
        private long fillingTime, timeToFilling, startTime;

        public PointDrawer (Context context, SurfaceHolder holder) {
            this.holder = holder;
            paint = new Paint ();
            paint.setStrokeWidth (2);
            paint.setAntiAlias(true);
            paint.setSubpixelText(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            timeToFilling = 1;
        }

        @Override
        public void run () {
            while (running) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    try {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                        width = canvas.getWidth();
                        height = canvas.getHeight();

                        setAround(canvas);

                        if (boundingDotLines.size() > 0 && isRequestDrawing) {
                            paint.setStyle(Paint.Style.FILL_AND_STROKE);

                            boundingMoving(Math.round (Math.toDegrees(orientation[2])*1000)/1000, Math.round (Math.toDegrees(orientation[1])*1000)/1000, canvas);

                            paint.setStyle(Paint.Style.STROKE);
                            paint.setColor(Color.WHITE);

                            if (inCircle(boundingDotLines.get(0).getXPoint(), boundingDotLines.get(0).getYPoint(),
                                    canvas.getWidth()/2, canvas.getHeight()/2,   50, 0) && boundingDotLines.size() > 1) {
                                canvas.drawCircle((float) boundingDotLines.get(0).getXPoint(), (float) boundingDotLines.get(0).getYPoint(), 35, paint);

                                if (isFilling) {
                                    fillingTime = System.currentTimeMillis();

                                    if (fillingTime - startTime >= timeToFilling && !isLocked) {
                                        lockAndShowEnd();
                                        isLocked = true;

                                        getArea();
                                        getPerimeter();
                                    }
                                } else {
                                    isFilling = true;
                                    startTime = System.currentTimeMillis();
                                }
                            } else
                                isFilling = false;
                        }

                        if (!isLocked && boundingDotLines.size() > 0)
                            canvas.drawLine((float)boundingDotLines.get (boundingDotLines.size() - 1).getXPoint(), (float)boundingDotLines.get (boundingDotLines.size() - 1).getYPoint(),
                                    canvas.getWidth()/2, canvas.getHeight()/2, paint);

                        if (boundingDotLines.size () > 0)
                            for (int i = 0; i < boundingDotLines.size(); i++)
                                boundingDotLines.get(i).drawDot(canvas);

                        paint.setColor(Color.RED);
                        if (isLocked)
                            paint.setColor (Color.CYAN);
                        canvas.drawLine(canvas.getWidth()/2 - 50, canvas.getHeight()/2, canvas.getWidth()/2 + 50, canvas.getHeight()/2, paint);
                        canvas.drawLine(canvas.getWidth()/2, canvas.getHeight()/2 - 50, canvas.getWidth()/2, canvas.getHeight()/2 + 50, paint);
                        paint.setColor(Color.rgb(0, 191, 26));
                        paint.setStyle (Paint.Style.FILL_AND_STROKE);
                        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 12, paint);

                        paint.setTextSize(40);
                        canvas.drawText("X: " + String.valueOf(Math.round(Math.toDegrees(orientation[2]))), 0, 50, paint);
                        canvas.drawText("Y: " + String.valueOf(Math.round(Math.toDegrees(orientation[1]))), 0, 100, paint);
                        canvas.drawText("Z: " + String.valueOf(Math.round(Math.toDegrees(orientation[0]))), 0, 150, paint);
                        canvas.drawText("newX: " + String.valueOf(Math.round(Math.toDegrees(gyroscope[2]))), 0, 250, paint);
                        canvas.drawText("newY: " + String.valueOf(Math.round(Math.toDegrees(gyroscope[1]))), 0, 300, paint);
                        canvas.drawText("newZ: " + String.valueOf(Math.round(Math.toDegrees(gyroscope[0]))), 0, 350, paint);
                        canvas.drawText(String.valueOf(boundingDotLines.size()), 0, 200, paint);

                        canvas.drawText(activity.getString(R.string.square_is) + String.valueOf(areaFull) + " | meters", canvas.getWidth()/3f, canvas.getHeight()/8, paint);
                        canvas.drawText(activity.getString(R.string.perimeter_is) + String.valueOf(perimeterFull) + " | meters", canvas.getWidth()/3f, canvas.getHeight()/8+50, paint);
                        canvas.drawText("leftRound - " + leftRound, canvas.getWidth()/3f, canvas.getHeight()/8 + 100, paint);
                        canvas.drawText("rightRound - " + rightRound, canvas.getWidth()/3f, canvas.getHeight()/8 + 150, paint);
                    } catch (final Exception e){
                        activity.runOnUiThread(new Runnable () {
                            @Override
                            public void run () {
                                Toast.makeText(activity.getApplicationContext(), R.string.unknown_wrong + e.toString(), Toast.LENGTH_SHORT).show ();
                            }
                        });
                    }finally {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }

                try {
                    Thread.sleep (50);
                } catch (InterruptedException e) {}
            }
        }

        private void boundingMoving (double xPos, double yPos, Canvas canvas) {
            double xPoint = 0, yPoint = 0;

            if ((xPos > xDel + 1 || xPos < xDel - 1) || (yPos > yDel + 1 || yPos < yDel - 1)) {
                if (xPos > xDel + 1.1 || xPos < xDel - 1.1) {
                    xPoint = getXPointAxis(xPos);

                    xPoint *= 1.1d;

//                    xPoint += (!active ? -boundingDotLines.get(0).getRelativePlaceX() : boundingDotLines.get(0).getRelativePlaceX());
                    xDel = xPos;
                }

                if (yPos > yDel + 1.1 || yPos < yDel - 1.1) {

                    if (yPos <= 0 && (yPos > yDel && yPos < -75 && yPos > -90))
                        leftRound = !leftRound;
                    else if (yPos >= 0 && (yPos < yDel && yPos > 75 && yPos < 90))
                        rightRound = !rightRound;

                    yPoint = getYPointAxis(yPos);

                    yPoint *= 1.1d;

//                    yPoint += (!denactive ? -boundingDotLines.get(0).getRelativePlaceY() : boundingDotLines.get(0).getRelativePlaceY());
                    yDel = yPos;
                }

                for (int i = 0; i < boundingDotLines.size(); i++) {
                    boundingDotLines.get(i).setXPoint(boundingDotLines.get(i).getXPoint() + (!active ? xPoint:-xPoint));
                    boundingDotLines.get(i).setYPoint(boundingDotLines.get(i).getYPoint() + (!denactive ? yPoint:-yPoint));
                }

//                for (int i = 0; i < boundingDotLines.size(); i++) {
//                    yPoint = getYPointAxis(yPos, boundingDotLines.get(i).getY());
//                    boundingDotLines.get(i).setYPoint(boundingDotLines.get(i).getYPoint() + (!denactive ? yPoint:-yPoint));
//                }
            }

            paint.setColor(Color.WHITE);

            for (int i = 0; i < boundingDotLines.size () - 1; i++)
                canvas.drawLine ((float)boundingDotLines.get (i).getXPoint(), (float)boundingDotLines.get (i).getYPoint(),
                        (float)boundingDotLines.get(i + 1).getXPoint(), (float)boundingDotLines.get (i + 1).getYPoint(), paint);

            if (isLocked)
                canvas.drawLine((float) boundingDotLines.get(0).getXPoint(), (float) boundingDotLines.get(0).getYPoint(),
                        (float) boundingDotLines.get(boundingDotLines.size() - 1).getXPoint(), (float) boundingDotLines.get(boundingDotLines.size() - 1).getYPoint(), paint);
        }

        private void setAround(Canvas canvas) {
            paint.setColor (Color.GRAY);
            if (isLocked)
                paint.setColor (Color.GREEN);
            float relativePos = 5;
            for (int i = 1; i < relativePos; i++) {
                canvas.drawLine(canvas.getWidth()/relativePos * i, 0, canvas.getWidth()/relativePos * i, canvas.getHeight(), paint);
                canvas.drawLine(0, canvas.getHeight()/relativePos*i, canvas.getWidth(), canvas.getHeight()/relativePos*i, paint);
            }

        }

        public void requestStop () {
            running = false;
        }
    }

    private void lockAndShowEnd () {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), R.string.points_ready, Toast.LENGTH_SHORT).show();
            }
        });

//        if (vibrator.hasVibrator()) {
//            for (int i = 0; i < 3; i++) {
//                try {
//                    vibrator.vibrate(50);
//
//                    Thread.sleep(90);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    public void getArea () {
        if (isSetReferenced) {
            double plus = 0, minus = 0;

            for (int i = 0; i < boundingDotLines.size(); i++) {
                if (i == boundingDotLines.size() - 1)
                    plus += boundingDotLines.get(i).getLastX() * boundingDotLines.get(0).getLastY();
                else
                    plus += boundingDotLines.get(i).getLastX() * boundingDotLines.get(i + 1).getLastY();

                if (i == 0)
                    minus += boundingDotLines.get(i).getLastX() * boundingDotLines.get(boundingDotLines.size() - 1).getLastY();
                else
                    minus += boundingDotLines.get(i).getLastX() * boundingDotLines.get(i - 1).getLastY();
            }

            areaFull = (double) Math.round(Math.abs(plus - minus)) / 500;
            areaFull = etaloneObj.getSize(areaFull);
        }
    }

    public void getPerimeter () {
        if (isSetReferenced) {
            for (int i = 0; i < boundingDotLines.size(); i++) {
                perimeterFull = Math.random() * 30;

                if (i == boundingDotLines.size() - 1)
                    perimeterFull += Math.abs(Math.sqrt(boundingDotLines.get(boundingDotLines.size() - 1).getLastX() - Math.pow(boundingDotLines.get(0).getLastX(), 2)
                            + Math.pow(boundingDotLines.get(boundingDotLines.size() - 1).getLastY() - boundingDotLines.get(0).getLastY(), 2)));
                else
                    perimeterFull += Math.abs(Math.sqrt(Math.pow(boundingDotLines.get(i + 1).getLastX() - boundingDotLines.get(i).getLastX(), 2)
                            + Math.pow(boundingDotLines.get(i + 1).getLastY() - boundingDotLines.get(i).getLastY(), 2)));

                perimeterFull = (double) Math.round(Math.abs(perimeterFull));
            }

            perimeterFull = etaloneObj.getSize(perimeterFull);
        }
    }

    public void getAngles () {
        if (isSetReferenced) {
            if (boundingDotLines.size() > 2)
                for (int i = 1; i < boundingDotLines.size() - 1; i++) {
                    boundingDotLines.get(i).setAngle((Math.acos(
                            (Math.pow(getVectorLength(boundingDotLines.get(i - 1), boundingDotLines.get(i)), 2)
                                    + Math.pow(getVectorLength(boundingDotLines.get(i), boundingDotLines.get(i + 1)), 2)
                                    - Math.pow(getVectorLength(boundingDotLines.get(i - 1), boundingDotLines.get(i + 1)), 2))
                                    / (2 * getVectorLength(boundingDotLines.get(i - 1), boundingDotLines.get(i))
                                    * getVectorLength(boundingDotLines.get(i), boundingDotLines.get(i + 1))))) / 1.7);
                }

            if (isLocked) {
                boundingDotLines.get(0).setAngle((Math.acos(
                        (Math.pow(getVectorLength(boundingDotLines.get(boundingDotLines.size() - 2), boundingDotLines.get(0)), 2)
                                + Math.pow(getVectorLength(boundingDotLines.get(0), boundingDotLines.get(1)), 2)
                                - Math.pow(getVectorLength(boundingDotLines.get(boundingDotLines.size() - 2), boundingDotLines.get(1)), 2))
                                / (2 * getVectorLength(boundingDotLines.get(boundingDotLines.size() - 2), boundingDotLines.get(0))
                                * getVectorLength(boundingDotLines.get(0), boundingDotLines.get(1))))) / 1.7);
                boundingDotLines.get(boundingDotLines.size() - 1).setAngle((Math.acos(
                        (Math.pow(getVectorLength(boundingDotLines.get(boundingDotLines.size() - 2), boundingDotLines.get(boundingDotLines.size() - 1)), 2)
                                + Math.pow(getVectorLength(boundingDotLines.get(boundingDotLines.size() - 1), boundingDotLines.get(0)), 2)
                                - Math.pow(getVectorLength(boundingDotLines.get(boundingDotLines.size() - 2), boundingDotLines.get(0)), 2))
                                / (2 * getVectorLength(boundingDotLines.get(boundingDotLines.size() - 2), boundingDotLines.get(boundingDotLines.size() - 1))
                                * getVectorLength(boundingDotLines.get(boundingDotLines.size() - 1), boundingDotLines.get(0))))) / 1.7);
            }
        }
    }

    private double getVectorLength (BoundingDotLine first, BoundingDotLine second) {
        return Math.sqrt(Math.pow (second.getXPoint() - first.getXPoint(), 2) + Math.pow (second.getYPoint() - first.getYPoint(), 2));
    }

    private void setReferenceObjectOnMeasure () {
        final String[] mChooseCats = { activity.getString(R.string.et_card), activity.getString(R.string.et_pen), activity.getString(R.string.et_book) };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity.getApplicationContext());
        builder.setTitle(R.string.selectTheEthalone)
                .setCancelable(false)
                .setSingleChoiceItems(mChooseCats, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int item) {
                                dialog.cancel();

                                switch (item) {
                                    case 0:
                                        etaloneObj = new ReferencedObjectReferenced("debitCard", 1);
                                        break;
                                    case 1:
                                        etaloneObj = new ReferencedObjectReferenced("pen", 1);
                                        break;
                                    case 2:
                                        etaloneObj = new ReferencedObjectReferenced("book", 1);
                                        break;
                                    default:
                                        etaloneObj = new ReferencedObjectReferenced("avg", 1);
                                        break;
                                }
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show ();
    }

    private double getYPointAxis(double yPos) {
        double yPoint = 0.d;

        denactive = false;

        if (yPos <= 0) {
            if (!leftRound) {
                if (yPos < boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round((boundingDotLines.get(0).getRelativePlaceY() * (boundingDotLines.get(0).getY() - yPos)) * 10000) / 10000;
                    denactive = true;
                } else if (yPos > boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round((boundingDotLines.get(0).getRelativePlaceY() * (yPos - boundingDotLines.get(0).getY())) * 10000) / 10000;
//                                denactive = true;
                }
            } else if (leftRound) {
                if (yPos < boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round((boundingDotLines.get(0).getRelativePlaceY() * (yPos - boundingDotLines.get(0).getY())) * 10000) / 10000;
                    denactive = true;

                } else if (yPos > boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round((boundingDotLines.get(0).getRelativePlaceY() * (boundingDotLines.get(0).getY() - yPos)) * 10000) / 10000;
                }
            }
        } else if (yPos >= 0) {
            if (!rightRound) {
                if (yPos < boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round((boundingDotLines.get(0).getRelativePlaceY() * (boundingDotLines.get(0).getY() - yPos)) * 10000) / 10000;
                    denactive = true;
                } else if (yPos > boundingDotLines.get(0).getY() && yPos >= 0) {
                    yPoint = Math.round((boundingDotLines.get(0).getRelativePlaceY() * (yPos - boundingDotLines.get(0).getY())) * 10000) / 10000;
                }
            } else if (rightRound) {
                if (yPos < boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round((boundingDotLines.get(0).getRelativePlaceY() * (yPos - boundingDotLines.get(0).getY())) * 10000) / 10000;
                    denactive = true;

                } else if (yPos > boundingDotLines.get(0).getY() && yPos >= 0) {
                    yPoint = Math.round((boundingDotLines.get(0).getRelativePlaceY() * (boundingDotLines.get(0).getY() - yPos)) * 10000) / 10000;
                }
            }
        }

//        yPoint += yPoint > 0 ? boundingDotLines.get(0).getRelativePlaceY() : -boundingDotLines.get(0).getRelativePlaceY();

        return yPoint;
    }

    private double getXPointAxis(double xPos) {
        double xPoint = 0.d;

        active = false;

        if (xPos > boundingDotLines.get(0).getX() && xPos >= 0)
            xPoint = Math.round((boundingDotLines.get(0).getRelativePlaceX() * (xPos - boundingDotLines.get(0).getX())) * 10000) / 10000;
        else if (xPos < boundingDotLines.get(0).getX() && xPos >= 0)
            xPoint = Math.round((boundingDotLines.get(0).getRelativePlaceX() * (boundingDotLines.get(0).getX() - xPos)) * 10000) / 10000;
        else if (xPos < boundingDotLines.get(0).getX() && xPos <= 0) {
            xPoint = Math.round((boundingDotLines.get(0).getRelativePlaceX() * (boundingDotLines.get(0).getX() - xPos)) * 10000) / 10000;
            active = true;
        } else if (xPos > boundingDotLines.get(0).getX() && xPos <= 0) {
            xPoint = Math.round((boundingDotLines.get(0).getRelativePlaceX() * (xPos - boundingDotLines.get(0).getX())) * 10000) / 10000;
            active = true;
        }

//        xPoint += xPoint > 0 ? boundingDotLines.get(0).getRelativePlaceX() : -boundingDotLines.get(0).getRelativePlaceX();

        return xPoint;
    }

//    private double getXPointAxis (double xPos, double xDel) {
//        double xPoint = 0.d;
//
//        active = false;
//
//        if (xPos > xDel && xPos >= 0)
//            xPoint = Math.round((boundingDotLines.get(0).getRelativePlaceX() * (xPos - xDel))*10000)/10000;
//        else if (xPos < xDel && xPos >= 0)
//            xPoint = Math.round((boundingDotLines.get(0).getRelativePlaceX() * (xDel - xPos))*10000)/10000;
//        else if (xPos < xDel && xPos <= 0) {
//            xPoint = Math.round((boundingDotLines.get(0).getRelativePlaceX() * (xDel - xPos))*10000)/10000;
//            active = true;
//        } else if (xPos > xDel && xPos <= 0) {
//            xPoint = Math.round((boundingDotLines.get(0).getRelativePlaceX() * (xPos - xDel)) * 10000)/10000;
//            active = true;
//        }
//
////        xPoint += xPoint > 0 ? boundingDotLines.get(0).getRelativePlaceX() : -boundingDotLines.get(0).getRelativePlaceX();
//
//        return xPoint;
//    }

    public double getYPointAxis(double yPos, double yPoint) {
        denactive = false;

        if (yPos <= 0) {
            if (!leftRound) {
                if (yPos < boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round(boundingDotLines.get(0).getRelativePlaceY() * (yPoint - yPos));
                    denactive = true;
                } else if (yPos > boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round(boundingDotLines.get(0).getRelativePlaceY() * (yPos - yPoint));
//                                denactive = true;
                }
            } else if (leftRound) {
                if (yPos < boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round(boundingDotLines.get(0).getRelativePlaceY() * (yPos - yPoint));
                    denactive = true;

                } else if (yPos > boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round(boundingDotLines.get(0).getRelativePlaceY() * (yPoint - yPos));
                }
            }
        } else if (yPos >= 0) {
            if (!rightRound) {
                if (yPos < boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round(boundingDotLines.get(0).getRelativePlaceY() * (yPoint - yPos));
                    denactive = true;
                } else if (yPos > boundingDotLines.get(0).getY() && yPos >= 0) {
                    yPoint = Math.round(boundingDotLines.get(0).getRelativePlaceY() * (yPos - yPoint));
                }
            } else if (rightRound) {
                if (yPos < boundingDotLines.get(0).getY() && yPos <= 0) {
                    yPoint = Math.round(boundingDotLines.get(0).getRelativePlaceY() * (yPos - yPoint));
                    denactive = true;

                } else if (yPos > boundingDotLines.get(0).getY() && yPos >= 0) {
                    yPoint = Math.round(boundingDotLines.get(0).getRelativePlaceY() * (yPoint - yPos));
                }
            }
        }

        return yPoint * (1 / (90 / Math.abs(yPoint)));
    }

    private void stableFit () {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    double xRound, yRound;

                    xRound = getXPointAxis(Math.toDegrees(orientation[2]))/10;
                    yRound = getYPointAxis(Math.toDegrees(orientation[1]))/10;

                    for (int i = 0; i < boundingDotLines.size(); i++) {
                        boundingDotLines.get(i).setXPoint(boundingDotLines.get(i).getXPoint() + (!active ? xRound:-xRound));
                        boundingDotLines.get(i).setYPoint(boundingDotLines.get(i).getYPoint() + (!denactive ? yRound:-yRound));
                    }
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        loadSensorsData(sensorEvent);

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometer, geomagnesis);
        SensorManager.getOrientation(rotationMatrix, orientation);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void loadSensorsData (SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometer = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnesis = sensorEvent.values.clone ();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscope = sensorEvent.values.clone();
                break;
        }
    }
}