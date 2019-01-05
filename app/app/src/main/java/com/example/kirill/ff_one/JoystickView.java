package com.example.kirill.ff_one;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener{
    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;
    public JoystickListener joystickCallback;

    // surface view constructors
    public JoystickView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if (context instanceof JoystickListener) {
            joystickCallback = (JoystickListener) context;
        }
    }

    public JoystickView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if (context instanceof JoystickListener) {
            joystickCallback = (JoystickListener) context;
        }
    }

    public JoystickView (Context context, AttributeSet attributes) {
        super(context, attributes);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if (context instanceof JoystickListener) {
            joystickCallback = (JoystickListener) context;
        }
    }
    // callbacks from surface holder
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupDimensions();
        drawJoystick(centerX, centerY);
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){

    }

    // get dimensions of the view and assign some coordinates
    void setupDimensions(){
        centerX = getWidth()/2;
        centerY = getHeight()/2;
        baseRadius = Math.min(getWidth(), getHeight())/3;
        hatRadius = Math.min(getWidth(), getHeight())/5;
    }
    // draw an actual joystick
    private void drawJoystick(float newX, float newY){
        if (getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint color = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            color.setARGB(50, 200, 200, 200);
            myCanvas.drawCircle(centerX, centerY, baseRadius, color);
            color.setARGB(200, 255, 255, 255);
            myCanvas.drawCircle(newX, newY, hatRadius, color);
            getHolder().unlockCanvasAndPost(myCanvas);
        }

    }

    // listen for touches
    public boolean onTouch(View v, MotionEvent e) {
        long currentTimestamp = SystemClock.uptimeMillis();
        float jX = e.getX();
        float jY = e.getY();
        if (v.equals(this)) {
            if (e.getAction() != e.ACTION_UP) {
                float distFromCenter = (float) Math.sqrt(Math.pow(jX - centerX, 2)
                        + Math.pow(jY - centerY, 2));

                if (distFromCenter >= baseRadius) {
                    float radRatio = baseRadius / distFromCenter;
                    jX = centerX + (e.getX() - centerX) * radRatio;
                    jY = centerY + (e.getY() - centerY) * radRatio;
                }
            }
            else {
                jX = centerX;
                jY = centerY;
            }
            drawJoystick(jX, jY);
            joystickCallback.onJoystickMoved((jX - centerX) / baseRadius,
                    (jY - centerY) / baseRadius,
                    getId());
        }
        return true;
    }

    //interface for the main activity to hook onto
    public interface JoystickListener {
        void onJoystickMoved(float xPercent, float yPercent, int source);
    }


}
