package com.example.kirill.ff_one;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BatteryView extends SurfaceView implements SurfaceHolder.Callback{
    public double batteryVoltage = 0;
    private float width;
    private float height;
    private double maxVoltage = 8;
    private double minVoltage = 6.2;

    public BatteryView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }
    // surface view constructors
    public BatteryView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
        getHolder().addCallback(this);

    }
    // surface view constructors
    public BatteryView(Context context, AttributeSet attributes) {
        super(context,attributes);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupDimensions();
        drawBattery();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
    }

    private void setupDimensions() {
        width = getWidth();
        height = getHeight();
    }

    private void drawBattery() {
        double batteryValPix = (batteryVoltage - minVoltage)*(width/(maxVoltage - minVoltage));
        if (getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint color = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if ((batteryVoltage - minVoltage) / (maxVoltage - minVoltage) < 0.2) {
                color.setARGB(255, 255, 0, 0);
            }
            else {
                color.setARGB(255, 255, 255, 255);
            }
            // draw battery body
            myCanvas.drawRoundRect(0, 0, width - 10, height, 10, 10, color);
            // draw battery knob
            myCanvas.drawRect(width - 20, height / 2 - 10, width, height / 2 + 10, color);
            int bgColor = ContextCompat.getColor(getContext(), R.color.sexy_blue);
            color.setColor(bgColor);
            myCanvas.drawRoundRect(10, 10, width - 20, height - 10, 5, 5, color);
            if (((batteryVoltage - minVoltage) / (maxVoltage - minVoltage) < 0.2)) {
                color.setARGB(255, 255, 0, 0);
            }
            else if ((batteryVoltage - minVoltage) / (maxVoltage - minVoltage) < 0.4) {
                color.setARGB(255, 255, 153, 0);
            }
            else {
                color.setARGB(255, 2, 252, 31);
            }
            myCanvas.drawRoundRect(10, 10, (float) batteryValPix - 20, height - 10, 5, 5, color);
            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }

    void setVoltage(double val) {
        batteryVoltage = val;
        drawBattery();
    }
}
