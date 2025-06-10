package io.github.karino2.pngnote.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.locks.Lock;

import io.github.karino2.pngnote.BookActivity;

public class CanvasBoox extends View {
    private Bitmap initialBmp;
    private Bitmap background;
    private Bitmap bitmap;
    private Canvas bmpCanvas;
    private float pencilWidth;
    private float eraserWidth;
    private Paint bmpPaint;
    private Paint bmpPaintWithBG;
    private Paint pathPaint;
    private Paint eraserPaint;
    private UndoList undoList;
    private int undoCount;
    private int redoCount;
    private int initCount;
    private RectF tempRegion;
    private Rect tempRect;
    private boolean downHandled;
    private float prevX;
    private float prevY;
    private float TOUCH_TOLERANCE;
    private Path path;
    private boolean isPencil;
    private int pageIdx;
    private OnUpdateBmpListener updateBmpListener;
    private OnUndoStateListener undoStateListener;

    public CanvasBoox(Context context) {
        super(context);
        CanvasBooxInit(context);
    }

    public CanvasBoox(Context context, AttributeSet attrs) {
        super(context, attrs);
        CanvasBooxInit(context);
    }

    public CanvasBoox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        CanvasBooxInit(context);
    }

    private void CanvasBooxInit(Context context) {
        this.pencilWidth = 3.0F;
        this.eraserWidth = 30.0F;
        this.bmpPaint = new Paint(Paint.DITHER_FLAG);

        this.bmpPaintWithBG = new Paint(Paint.DITHER_FLAG);
        this.bmpPaintWithBG.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));

        this.pathPaint = new Paint();
        this.pathPaint.setAntiAlias(true);
        this.pathPaint.setDither(true);
        this.pathPaint.setColor(0xFF000000);
        this.pathPaint.setStyle(Style.STROKE);
        this.pathPaint.setStrokeJoin(Join.ROUND);
        this.pathPaint.setStrokeCap(Cap.ROUND);
        this.pathPaint.setStrokeWidth(this.pencilWidth);

        this.eraserPaint = new Paint();
        this.eraserPaint.setAntiAlias(false);
        this.eraserPaint.setColor(0xFFFFFFFF);
        this.eraserPaint.setStyle(Style.STROKE);
        this.eraserPaint.setStrokeCap(Cap.ROUND);
        this.eraserPaint.setStrokeJoin(Join.ROUND);
        this.eraserPaint.setStrokeWidth(this.eraserWidth);

        this.undoList = new UndoList();
        this.tempRegion = new RectF();
        this.tempRect = new Rect();
        this.TOUCH_TOLERANCE = 4.0F;
        this.path = new Path();
        this.isPencil = true;
        this.updateBmpListener = null;
        this.undoStateListener = null;
    }

    public void init(Bitmap initialBmp, Bitmap background, int initialPageIdx) {
        this.initialBmp = initialBmp;
        this.background = background;
        this.pageIdx = initialPageIdx;
    }

    public Bitmap getInitialBmp() {
        return this.initialBmp;
    }
    public void setInitialBmp(Bitmap var1) {
        this.initialBmp = var1;
    }
    public Bitmap getBitmap() {
        return this.bitmap;
    }
    public void setBitmap(Bitmap var1) {
        this.bitmap = var1;
    }

    public void undo(int count) {
        if (this.undoCount != count) {
            this.undoCount = count;
            Lock lock = BookActivity.getBitmapLock();
            lock.lock();
            try {
                this.undoList.undo(this.bmpCanvas);
            } finally {
                lock.unlock();
            }
            this.refreshAfterUndoRedo();
        }
    }

    public void redo(int count) {
        if (this.redoCount != count) {
            this.redoCount = count;
            Lock lock = BookActivity.getBitmapLock();
            lock.lock();
            try {
                this.undoList.redo(this.bmpCanvas);
            } finally {
                lock.unlock();
            }
            this.refreshAfterUndoRedo();
        }
    }

    private void notifyUndoStateChanged() {
        if (this.undoStateListener != null) {
            this.undoStateListener.onUndoState(this.getCanUndo(), this.getCanRedo());
        }
    }

    private void refreshAfterUndoRedo() {
        if (this.updateBmpListener != null) {
            this.updateBmpListener.onUpdateBmp(this.bitmap);
        }
        this.notifyUndoStateChanged();
        this.invalidate();
    }

    public boolean getCanUndo() {
        return this.undoList.getCanUndo();
    }

    public boolean getCanRedo() {
        return this.undoList.getCanRedo();
    }

    private Rect pathBound(Path path) {
        path.computeBounds(this.tempRegion, false);
        this.tempRegion.roundOut(this.tempRect);
        this.widen(this.tempRect, 5);
        return this.tempRect;
    }

    private void widen(Rect tmpInval, int margin) {
        int newLeft = coerceAtLeast(tmpInval.left - margin, 0);
        int newTop = coerceAtLeast(tmpInval.top - margin, 0);
        int newRight = coerceAtMost(tmpInval.right + margin, this.getWidth());
        int newBottom = coerceAtMost(tmpInval.bottom + margin, this.getHeight());
        tmpInval.set(newLeft, newTop, newRight, newBottom);
    }
    private static int coerceAtLeast(int this_, int minimumValue) {
        if (this_ < minimumValue) {
            return minimumValue;
        } else {
            return this_;
        }
    }
    private static int coerceAtMost(int this_, int maximumValue) {
        if (this_ > maximumValue) {
            return maximumValue;
        } else {
            return this_;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.bitmap != null) {
            Bitmap oldbmp = this.bitmap;
            this.createNewCanvas(w, h);
            this.drawBitmap(oldbmp);
        } else {
            this.setupNewCanvasBitmap(w, h);
        }
        if (updateBmpListener != null) {
            this.updateBmpListener.onUpdateBmp(this.bitmap);
        }
    }

    private void setupNewCanvasBitmap(int w, int h) {
        this.createNewCanvas(w, h);
        this.drawBitmap(this.initialBmp);
        this.initialBmp = null;
    }

    private void createNewCanvas(int w, int h) {
        Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888); //FIXME: check null
        if (bmp != null) {
            bmp.eraseColor(0xFFFFFFFF);
        }
        this.bitmap = bmp;
        this.bmpCanvas = new Canvas(bmp);
    }

    private void drawBitmap(Bitmap srcBitmap) {
        if (srcBitmap != null && this.bmpCanvas != null) {
            this.bmpCanvas.drawBitmap(srcBitmap,
                new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight()),
                new Rect(0, 0, this.bitmap.getWidth(), this.bitmap.getHeight()),
                this.bmpPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.downHandled = true;
                this.path.reset();
                this.path.moveTo(x, y);
                this.prevX = x;
                this.prevY = y;
                this.invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (this.downHandled) {
                    this.downHandled = false;
                    this.path.lineTo(x, y);
                    Rect region = this.pathBound(this.path);
                    Bitmap redo = Bitmap.createBitmap(this.bitmap, //check null
                            region.left, region.top,
                            region.width(), region.height());
                    Bitmap undo = redo; //FIXME:check null
                    this.drawPathToCanvas(this.bmpCanvas, this.path);
                    redo = Bitmap.createBitmap(this.bitmap, //FIXME: check null
                            region.left, region.top,
                            region.width(), region.height()); //FIXME: check null
                    this.undoList.pushUndoCommand(region.left, region.top, undo, redo);
                    this.notifyUndoStateChanged();
                    if (this.updateBmpListener != null) {
                        this.updateBmpListener.onUpdateBmp(this.bitmap);//FIXME: check null
                    }
                    this.path.reset();
                    this.invalidate();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (this.downHandled) {
                    float dx = Math.abs(x - this.prevX);
                    float dy = Math.abs(y - this.prevY);
                    if (dx >= this.TOUCH_TOLERANCE || dy >= this.TOUCH_TOLERANCE) {
                        this.path.quadTo(this.prevX, this.prevY, (x + this.prevX) / (float)2, (y + this.prevY) / (float)2);
                        this.prevX = x;
                        this.prevY = y;
                        this.invalidate();
                    }
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void drawPathToCanvas(Canvas canvas, Path path) {
        Paint paint = this.isPencil ? this.pathPaint : this.eraserPaint;
        canvas.drawPath(path, paint);
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFFFFFFF);
        canvas.drawBitmap(this.bitmap, 0, 0, this.bmpPaint); //FIXME: check null
        this.drawPathToCanvas(canvas, this.path);
    }

    private boolean isEraser() {
        return !this.isPencil;
    }

    private void pencil() {
        if (!this.isPencil) {
            this.isPencil = true;
        }
    }

    private void eraser() {
        if (!this.isEraser()) {
            this.isPencil = false;
        }
    }

    public void penOrEraser(boolean isPen) {
        if (isPen != this.isPencil) {
            if (isPen) {
                this.pencil();
            } else {
                this.eraser();
            }
        }
    }

    public void onPageIdx(int idx, OnLoadBitmapListener bitmapLoader) {
        if (this.pageIdx != idx) {
            this.pageIdx = idx;
            Bitmap newbmp = bitmapLoader.onLoadBitmap(idx);
            if (this.bitmap != null) {
                this.bitmap.eraseColor(0xFFFFFFFF);
            }
            if (newbmp != null) {
                if (this.bmpCanvas != null) {
                    this.bmpCanvas.drawBitmap(newbmp,
                        new Rect(0, 0, newbmp.getWidth(), newbmp.getHeight()),
                        new Rect(0, 0, this.getWidth(), this.getHeight()),
                        this.bmpPaint);
                }
            }
            this.undoList.clear();
            this.notifyUndoStateChanged();
            this.invalidate();
        }
    }

    public void setOnUpdateListener(OnUpdateBmpListener updateBmpListener) {
        this.updateBmpListener = updateBmpListener;
    }

    public void setOnUndoStateListener(OnUndoStateListener undoStateListener) {
        this.undoStateListener = undoStateListener;
    }

    public interface OnUpdateBmpListener {
        void onUpdateBmp(Bitmap bmp);
    }

    public interface OnUndoStateListener {
        void onUndoState(Boolean undo, Boolean redo);
    }

    public interface OnLoadBitmapListener {
        Bitmap onLoadBitmap(int bmp);
    }
}
