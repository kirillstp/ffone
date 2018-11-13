package com.example.kirill.ff_one;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SpeedometerView extends SurfaceView implements SurfaceHolder.Callback {
    private int height;
    private int width;
    private int rpm = 0;
    // yes i know its called speedometer, but with electric motor with one gear speed ~ constant * rpm
    private int numTicks = 12;
    private int maxRpm = 30000;
    private float maxKmh = 30;
    private int orangeTicksThresh = 7;
    private int redTicksThresh = 9;

    // surface view constructors
    public SpeedometerView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }
    // surface view constructors
    public SpeedometerView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
        getHolder().addCallback(this);

    }
    // surface view constructors
    public SpeedometerView(Context context, AttributeSet attributes) {
        super(context,attributes);
        getHolder().addCallback(this);

    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupDimensions();
        drawSpeedometer();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
    }
    // get height and width to split the speedometer bar
    void setupDimensions() {
        height = getHeight();
        width = getWidth();
    }
    private void drawSpeedometer() {
        float tickWidth = width / numTicks;
        float pix = width*rpm/maxRpm;
        if (getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint color = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            color.setARGB(255,255,255,255);
            for (int tick = 0; tick < numTicks; tick++) {
                if (tick > redTicksThresh) {
                    color.setARGB(255,255,0,0);
                }
                else if (tick > orangeTicksThresh) {
                    color.setARGB(255,255,153,0);
                }
                float x = tick*tickWidth+10; // offset to avoid edge clipping
                myCanvas.drawRect(x-tickWidth,0,x+tickWidth,5, color);
                myCanvas.drawRect(x-2, 5, x+2, 20, color);
            }
            color.setARGB(255,255,255,255);
            for (int needleTick = 0; needleTick < pix; needleTick+=10) {
                myCanvas.drawRect(needleTick - 2, 25, needleTick + 2, height - 25, color);
            }
            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }
    public void setRpm(int rpmVal) {
        rpm = rpmVal;
        drawSpeedometer();
    }

}
