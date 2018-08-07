package com.itprogit.utils.gpstracker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class BoundingDotLine {
    private double x, y, z, xPoint, yPoint, relativePlaceX, relativePlaceY, lastX, lastY, angle;
    private float circleSize = 23.5f;
    private int countNumber;
    private int privateDotNumber, color;
    private boolean isStarted;
    private Paint paint;

    public BoundingDotLine (double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        lastX = x;
        lastY = y;
        angle = -1;

        color = Color.rgb((int)(Math.random()*255+1), (int)(Math.random()*255+1), (int)(Math.random()*255+1));

        countNumber++;
        privateDotNumber = countNumber;
    }

    public void init (Canvas canvas) {
        xPoint = canvas.getWidth() / 2;
        yPoint = canvas.getHeight() / 2;

        relativePlaceX = canvas.getWidth()/360;
        relativePlaceY = canvas.getHeight()/360;

        paint = new Paint ();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTextSize(20);

        isStarted = true;
    }

//    public void drawDot (Canvas canvas, float nowX, float nowY) {
//        if (!isStarted)
//            init(canvas);
//
//        paint.setColor(color);
//
////        if (nowX < x && nowX >= 0) {
////            xPoint += relativePlaceX * (x - nowX);
////        } else if (nowX > x && nowX >= 0) {
////            xPoint += relativePlaceX * (Math.abs (nowX) - Math.abs (x));
////        } else if (nowX < x && nowX <= 0) {
////            xPoint += relativePlaceX*(x + nowX);
////        } else if (nowX > x && nowX >= 0) {
////            xPoint += relativePlaceX*(Math.abs (x) - nowX);
////        }
//
//        if (nowX > x && nowX >= 0)
//            xPoint += Math.round(relativePlaceX * (nowX - x));
//        else if (nowX < x && nowX >= 0)
//            xPoint += Math.round(relativePlaceX * (x - nowX));
//        else if (nowX < x && nowX <= 0)
//            xPoint -= Math.round(relativePlaceX * (x - nowX));
//            //Fix this in complete
//        else if (nowX > x && nowX <= 0)
//            xPoint -= Math.round(relativePlaceX * (nowX - x));
//
////        if (nowY > y)
////            yPoint += relativePlaceY * (y - nowY);
////        else if (nowY < y)
////            yPoint -= relativePlaceY * (nowY - y);
////            else if (nowY < y && nowY <= 0)
////                yPoint -= relativePlaceY * (y - nowY);
////                //Fix this in complete
////            else if (nowY > y && nowY <= 0)
////                yPoint -= relativePlaceY * (nowY - y);
//
//        if (xPoint > relativePlaceX * 180 * 6)
//            xPoint -= relativePlaceX * 180 * 6;
//        else if (xPoint < -relativePlaceX * 180 * 6)
//            xPoint += relativePlaceX * 180 * 7;
//
//        if (yPoint > relativePlaceY * 180 * 4)
//            yPoint -= relativePlaceY * 180 * 4;
//        else if (yPoint < -relativePlaceY * 180 * 4)
//            yPoint += relativePlaceY * 180 * 5;
//
//
////        if (nowX > x - ACCELEROMETER_PUNISH && nowX < x + ACCELEROMETER_PUNISH && !inFocus) {
////            inFocusX = xPoint;
////            xPoint = canvas.getWidth() / 2;
////            inFocus = true;
////        } else {
////            xPoint = inFocusX;
////            inFocus = false;
////        }
////        if (nowY > y - ACCELEROMETER_PUNISH && nowY < y + ACCELEROMETER_PUNISH)
////            yPoint = y;
//
//        canvas.drawCircle((float)xPoint, (float)yPoint, 15, paint);
//        canvas.drawText(String.valueOf(x + " : " + y), (float)xPoint, (float)yPoint + 50, paint);
//        canvas.drawText(String.valueOf(xPoint + " : " + yPoint), (float)xPoint, (float)yPoint -50, paint);
//        paint.setColor(Color.BLACK);
//        canvas.drawText(String.valueOf(privateDotNumber), (float)xPoint, (float)yPoint, paint);
//    }

    //отрисовка графики точек
    public void drawDot (Canvas canvas) {
        if (!isStarted)
            init(canvas);

        paint.setColor(color);

        if (xPoint > relativePlaceX * 360 * 6)
            xPoint -= relativePlaceX * 360 * 6;
        else if (xPoint < -relativePlaceX * 360 * 6)
            xPoint += relativePlaceX * 360 * 7;

        if (yPoint > relativePlaceY * 360 * 6)
            yPoint -= relativePlaceY * 360 * 6;
        else if (yPoint < -relativePlaceY * 360 * 6)
            yPoint += relativePlaceY * 360 * 7;

        canvas.drawCircle((float)xPoint, (float)yPoint, circleSize, paint);
        canvas.drawText(String.valueOf((double)(Math.round (lastX * 10))/10 + " : " + (double)(Math.round (lastY * 10))/10), (float)xPoint, (float)yPoint + 50, paint);
        canvas.drawText(String.valueOf(xPoint + " : " + yPoint), (float)xPoint, (float)yPoint -50, paint);
        paint.setTextSize(50);
        canvas.drawText("∠" + String.valueOf(angle) + "°", (float)xPoint, (float)yPoint + 100, paint);
        paint.setTextSize(30);
        paint.setColor(Color.BLACK);
        canvas.drawText(String.valueOf(privateDotNumber), (float)xPoint, (float)yPoint, paint);
    }

    //сеттеры и геттеры
    public double getXPoint () {
        return xPoint;
    }
    public double getYPoint () {
        return yPoint;
    }
    public void setXPoint (double xPoint) {this.xPoint = xPoint;}
    public void setYPoint (double yPoint) {this.yPoint = yPoint;}

    public double getRelativePlaceX () {return relativePlaceX;}
    public double getRelativePlaceY () {return relativePlaceY;}

    public double getX () {return x;}
    public double getY () {return y;}
    public void setX (double x) {this.x = x;}
    public void setY (double y) {this.y = y;}

    public void setLastX (double lastX) {this.lastX = (double)(Math.round (lastX * 10))/10;}
    public void setLastY (double lastY) {this.lastY = (double)(Math.round (lastY * 10))/10;}
    public double getLastX () {return lastX;}
    public double getLastY () {return lastY;}

    public void setAngle (double angle) {
        this.angle = (double) Math.round (angle*1000)/10;
    }
    public double getAngle () {return angle;}

    public void setCircleSize (float circleSize) {
        this.circleSize = circleSize;
    }
}
