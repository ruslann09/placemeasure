package com.itprogit.utils.gpstracker;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    final int CAMERA_ID = 0;
    final boolean FULL_SCREEN = true;
    private Activity activity;

    public CameraView(Context context, Activity activity, Camera camera) {
        super(context);

        this.activity = activity;
        this.camera = camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        camera.stopPreview();
        setCameraDisplayOrientation(CAMERA_ID);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

//        void setPreviewSize(boolean fullScreen) {
//
//            // получаем размеры экрана
//            Display display = getWindowManager().getDefaultDisplay();
//            boolean widthIsMax = display.getWidth() > display.getHeight();
//
//            // определяем размеры превью камеры
//            Size size = camera.getParameters().getPreviewSize();
//
//            RectF rectDisplay = new RectF();
//            RectF rectPreview = new RectF();
//
//            // RectF экрана, соотвествует размерам экрана
//            rectDisplay.set(0, 0, display.getWidth(), display.getHeight());
//
//            // RectF первью
//            if (widthIsMax) {
//                // превью в горизонтальной ориентации
//                rectPreview.set(0, 0, size.width, size.height);
//            } else {
//                // превью в вертикальной ориентации
//                rectPreview.set(0, 0, size.height, size.width);
//            }
//
//            Matrix matrix = new Matrix();
//            // подготовка матрицы преобразования
//            if (!fullScreen) {
//                // если превью будет "втиснут" в экран (второй вариант из урока)
//                matrix.setRectToRect(rectPreview, rectDisplay,
//                        Matrix.ScaleToFit.START);
//            } else {
//                // если экран будет "втиснут" в превью (третий вариант из урока)
//                matrix.setRectToRect(rectDisplay, rectPreview,
//                        Matrix.ScaleToFit.START);
//                matrix.invert(matrix);
//            }
//            // преобразование
//            matrix.mapRect(rectPreview);
//
//            // установка размеров surface из получившегося преобразования
//            sv.getLayoutParams().height = (int) (rectPreview.bottom);
//            sv.getLayoutParams().width = (int) (rectPreview.right);
//        }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }
}