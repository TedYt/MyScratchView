package com.ted.scratchview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Copyright (C) 2008 The Android Open Source Project
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Created by Ted.Yt on 9/20/16.
 *
 * 几个方法的执行顺序
 * onFinishInflate ->
 * onMeasure ->
 * onSizeChanged ->
 * onLayout ->
 * onMeasure ->
 * onLayout ->
 * onDraw
 */
public class MyScratchView extends View {

    int mMaskColor;
    int mErasePainSize;

    private Paint mMaskPaint;
    private Paint mErasePaint;
    private Bitmap mMaskBitmap;
    private Canvas mMaskCanvas;
    private Path mErasePath;

    private float mMotionX;
    private float mMotionY;

    private int mTouchSLop;

    private onEraseListner mEraseListner;

    private int[] mPixels;

    //擦除70%即算是擦除完成
    private final int mPercentComplete = 70;

    private Boolean isCompleted = false;

    public MyScratchView(Context context) {
        this(context,null);
    }

    public MyScratchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyScratchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs,R.styleable.MyScratchView);
        mMaskColor = ta.getColor(R.styleable.MyScratchView_maskColor, Color.BLACK);
        mErasePainSize = ta.getInt(R.styleable.MyScratchView_erasePainSize,10);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        initMaskPaint();
        initErasePaint();
        initPath();
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSLop = vc.getScaledTouchSlop();//若移动小于这个值，则不算是移动
    }

    private void initPath() {
        mErasePath = new Path();
    }

    private void initErasePaint() {

        mErasePaint = new Paint();
        mErasePaint.setAntiAlias(true);
        mErasePaint.setDither(true);
        mErasePaint.setStyle(Paint.Style.STROKE);
        mErasePaint.setStrokeCap(Paint.Cap.ROUND);
        mErasePaint.setStrokeWidth(mErasePainSize);
        mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private void initMaskPaint() {
        mMaskPaint = new Paint();
        mMaskPaint.setAntiAlias(true);
        mMaskPaint.setColor(mMaskColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //这里画bitmap，是因为bitmap比其他的drawXXX更简单
        canvas.drawBitmap(mMaskBitmap,0,0,mMaskPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createMask(w,h);
    }

    private void createMask(int w, int h) {
        mMaskBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        mMaskCanvas = new Canvas(mMaskBitmap);
        Rect rect = new Rect(0,0,w,h);
        mMaskCanvas.drawRect(rect,mMaskPaint);

        mPixels = new int[w * h];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                beforeErase(event.getX(),event.getY());
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                erase(event.getX(),event.getY());
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                endErase(event.getX(),event.getY());
                invalidate();
                break;
        }

        return true;
    }

    private void beforeErase(float x, float y) {
        mErasePath.reset();
        mErasePath.moveTo(x,y);
        mMotionX = x;
        mMotionY = y;
    }

    private void endErase(float x, float y) {
        mErasePath.reset();
        mMotionX = 0;
        mMotionY = 0;
    }

    private void erase(float x, float y) {
        int dx = (int) Math.abs(x - mMotionX);
        int dy = (int) Math.abs(y - mMotionY);
        if (dx >= mTouchSLop || dy >= mTouchSLop){
            mErasePath.lineTo(x,y);
            mMaskCanvas.drawPath(mErasePath, mErasePaint);

            mMotionX = x;
            mMotionY = y;

            mErasePath.reset();
            mErasePath.moveTo(x, y);

            new EraseTask().execute(getWidth(),getHeight());
        }
    }

    /**
     * 用mErasePaint来画整个布局
     */
    public void clean(){
        int w = getWidth();
        int h = getHeight();
        mMaskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Rect rect = new Rect(0,0,w,h);
        mMaskCanvas = new Canvas(mMaskBitmap);
        mMaskCanvas.drawRect(rect,mErasePaint);

        invalidate();
    }

    public void reset(){
        createMask(getWidth(),getHeight());
        invalidate();
    }

    public void setOnEraseListner(onEraseListner listner){
        mEraseListner = listner;
    }

    public interface onEraseListner {
        /**
         * 擦除中
         */
        void onProgress(float pert);

        /**
         * 擦除完成
         */
        void onComplete();

    }

    class EraseTask extends AsyncTask<Object, Float, Boolean>{

        @Override
        protected Boolean doInBackground(Object... params) {

            int width = (int)params[0];
            int height = (int)params[1];

            int erasedPixels = 0;//被擦除的像素
            int totalPixels = width * height;//像素总数

            //第三个参数表示一行有多少个像素
            mMaskBitmap.getPixels(mPixels,0,width,0,0,width,height);

            for (int pos = 0; pos < totalPixels; pos ++){
                if (mPixels[pos] == 0){ //透明的像素的值是0
                    erasedPixels++;
                }
            }

            float percent = Math.round(erasedPixels * 100/ totalPixels) ;

            publishProgress(percent);

            return percent >= mPercentComplete;
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
            if (mEraseListner != null){
                float per = values[0];
                mEraseListner.onProgress(per);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result && !isCompleted){
                isCompleted = true;
                if (mEraseListner != null){
                    mEraseListner.onComplete();
                }
            }
        }
    }
}
