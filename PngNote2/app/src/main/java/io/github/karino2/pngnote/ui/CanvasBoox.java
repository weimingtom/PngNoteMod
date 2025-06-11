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
import io.github.karino2.pngnote.book.BookIO;
import io.github.karino2.pngnote.colorpicker.Dips;
import io.github.karino2.pngnote.colorpicker.FileMeta;

public class CanvasBoox extends View {
    public final static float PEN_WIDTH1 = 3.0f;
    public final static float PEN_WIDTH2 = 30.0f;

    private BookActivity act;

    private Bitmap initialBmp;
    private Bitmap background;
    private Bitmap bitmap;
    private Canvas bmpCanvas;
    private float pencilWidth, pencilWidth2;
    private float eraserWidth;
    private Paint bmpPaint;
    private Paint bmpPaintWithBG;
    private Paint pathPaint, pathPaint2;
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

//    private boolean isPencil;
    private int isPen = 1;

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
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        this.pencilWidth = PEN_WIDTH1; //3.0F;
        this.pencilWidth2 = PEN_WIDTH2; //30.0F;
        this.eraserWidth = PEN_WIDTH2;//30.0F;
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

        this.pathPaint2 = new Paint();
        this.pathPaint2.setAntiAlias(true);
        this.pathPaint2.setDither(true);
        this.pathPaint2.setColor(0xFF000000);
        this.pathPaint2.setStyle(Style.STROKE);
        this.pathPaint2.setStrokeJoin(Join.ROUND);
        this.pathPaint2.setStrokeCap(Cap.ROUND);
        this.pathPaint2.setStrokeWidth(this.pencilWidth2);

        this.eraserPaint = new Paint();
        this.eraserPaint.setAntiAlias(false);
        if (BookIO.USE_META_TXT) {
            //this.eraserPaint.setColor(0x00000000);
            this.eraserPaint.setColor(0xFFFFFFFF);
        } else {
            this.eraserPaint.setColor(0xFFFFFFFF);
        }
        this.eraserPaint.setStyle(Style.STROKE);
        this.eraserPaint.setStrokeCap(Cap.ROUND);
        this.eraserPaint.setStrokeJoin(Join.ROUND);
        this.eraserPaint.setStrokeWidth(this.eraserWidth);
        if (BookIO.USE_META_TXT) {
            this.eraserPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
            //this.eraserPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_OUT));
        }

        this.undoList = new UndoList();
        this.tempRegion = new RectF();
        this.tempRect = new Rect();
        this.TOUCH_TOLERANCE = 4.0F;
        this.path = new Path();
//        this.isPencil = true;
        this.updateBmpListener = null;
        this.undoStateListener = null;
    }

    public void init(Bitmap initialBmp, Bitmap background, int initialPageIdx, BookActivity act) {
        this.initialBmp = initialBmp;
        this.background = background;
        this.pageIdx = initialPageIdx;
        this.act = act;
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
        if (this.tempBitmap == null) {
            this.tempBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);;
        }
    }
    Bitmap tempBitmap;

    private void setupNewCanvasBitmap(int w, int h) {
        this.createNewCanvas(w, h);
        this.drawBitmap(this.initialBmp);
        this.initialBmp = null;
    }

    private String backText;
    public void setBackText(String backText) {
        //设置背景图案
        this.backText = backText;
    }

    private void createNewCanvas(int w, int h) {
        Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888); //FIXME: check null
        if (bmp != null) {
            if (BookIO.USE_META_TXT) {
                bmp.eraseColor(0x00000000);
            } else {
                bmp.eraseColor(0xFFFFFFFF);
            }
            if (!BookIO.USE_META_TXT) {
                initBackText(this.backText, bmp, 1);
            }
        }
        this.bitmap = bmp;
        this.bmpCanvas = new Canvas(bmp);
    }

    //sampleSize=1原样， sampleSize=3 or 4，列表和表格中缩略图的下采样，所以间距也要除以这个数
    public static void initBackText(String backText, Bitmap bmp, int sampleSize) {
        if (bmp != null && backText != null) {
            if (backText.equals(FileMeta.DOTTED)) {
/*
final paint = Paint()
        ..color = Colors.grey[500].withOpacity(.3)
        ..strokeWidth = 1;
// 1 because no line at the top
for (int i = 1; i < size.height / XppPageSize.pt2mm(5); i++) {
    // 1 because no line at the beginning
    for (int j = 1; j < size.width / XppPageSize.pt2mm(5); j++) {
        canvas.drawCircle(
                Offset(j * XppPageSize.pt2mm(5).toDouble(),
                        i * XppPageSize.pt2mm(5).toDouble()),
                XppPageSize.pt2mm(.5).toDouble(),
                paint);
    }
}*/
                int w = bmp.getWidth();
                int h = bmp.getHeight();
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setColor(0xFFCCCCCC);
                paint.setAntiAlias(true);
                for (int i = 1; i < h / Dips.dpToPx(25/sampleSize) + 1; i++) {
                    // 1 because no line at the beginning
                    for (int j = 1; j < w / Dips.dpToPx(25/sampleSize) + 1; j++) {
                        canvas.drawCircle(j * Dips.dpToPx(25/sampleSize),
                                i * Dips.dpToPx(25/sampleSize),
                                Dips.dpToPx(2), paint); //Dips.dpToPx(2)
                    }
                }
            } else if (backText.equals(FileMeta.GRAPH)) {
/*
                final paint = Paint()
      ..color = Colors.grey[500].withOpacity(.3)
      ..strokeWidth = 1;
    // 1 because no line at the top
    for (int i = 1; i < size.height / XppPageSize.pt2mm(5); i++) {
      canvas.drawLine(Offset(0, i * XppPageSize.pt2mm(5).toDouble()),
          Offset(size.width, i * XppPageSize.pt2mm(5).toDouble()), paint);
    }
    // 1 because no line at the beginning
    for (int i = 1; i < size.width / XppPageSize.pt2mm(5); i++) {
      canvas.drawLine(Offset(i * XppPageSize.pt2mm(5).toDouble(), 0),
          Offset(i * XppPageSize.pt2mm(5).toDouble(), size.height), paint);
    }
                 */

                int w = bmp.getWidth();
                int h = bmp.getHeight();
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setColor(0xFFCCCCCC);
                paint.setStrokeWidth(1);
                paint.setAntiAlias(true);
                //1 because no line at the top
                for (int i = 1; i < h / Dips.dpToPx(25/sampleSize) + 1; i++) {
                    canvas.drawLine(0, i * Dips.dpToPx(25/sampleSize),
                            w, i * Dips.dpToPx(25/sampleSize), paint);
                }
                // 1 because no line at the beginning
                for (int i = 1; i < w / Dips.dpToPx(25/sampleSize) + 1; i++) {
                    canvas.drawLine(i * Dips.dpToPx(25/sampleSize), 0,
                            i * Dips.dpToPx(25/sampleSize), h, paint);
                }
            } else if (backText.equals(FileMeta.LINED)) {
/*
    final paint = Paint()
      ..color = Colors.grey[500].withOpacity(.3)
      ..strokeWidth = 1;
    // 1 because no line at the top
    for (int i = 1; i < size.height / 24; i++) {
      canvas.drawLine(Offset(0, i * 24.toDouble()),
          Offset(size.width, i * 24.toDouble()), paint);
    }
 */
                int w = bmp.getWidth();
                int h = bmp.getHeight();
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setColor(0xFFCCCCCC);
                paint.setStrokeWidth(1);
                paint.setAntiAlias(true);
                // 1 because no line at the top
                for (int i = 1; i < h / Dips.dpToPx(25/sampleSize); i++) {
                    canvas.drawLine(0, i * Dips.dpToPx(25/sampleSize),
                            w, i * Dips.dpToPx(25/sampleSize), paint);
                }
            }
        }
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
                    if (BookIO.USE_META_TXT) {
                        region.inset((int)-PEN_WIDTH2, (int)-PEN_WIDTH2);
                    }
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
        Paint paint = null;
        if (this.isPen == 1) {
            paint = this.pathPaint;
            canvas.drawPath(path, paint);
        } else if (this.isPen == 2) {
            paint = this.pathPaint2;
            canvas.drawPath(path, paint);
        } else if (this.isPen == 0){
            paint = this.eraserPaint;
            canvas.drawPath(path, paint);
        }

    }

    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFFFFFFF);
        if (BookIO.USE_META_TXT) {
            Bitmap bmp = this.bitmap;
            String backText = null;
            if (act != null) {
                backText = act.curPattern;
            }
            if (backText != null) {
                if (backText.equals(FileMeta.DOTTED)) {
/*
final paint = Paint()
        ..color = Colors.grey[500].withOpacity(.3)
        ..strokeWidth = 1;
// 1 because no line at the top
for (int i = 1; i < size.height / XppPageSize.pt2mm(5); i++) {
    // 1 because no line at the beginning
    for (int j = 1; j < size.width / XppPageSize.pt2mm(5); j++) {
        canvas.drawCircle(
                Offset(j * XppPageSize.pt2mm(5).toDouble(),
                        i * XppPageSize.pt2mm(5).toDouble()),
                XppPageSize.pt2mm(.5).toDouble(),
                paint);
    }
}*/
                    int w = bmp.getWidth();
                    int h = bmp.getHeight();
                    //Canvas canvas = new Canvas(bmp);
                    Paint paint = new Paint();
                    paint.setColor(0xFFCCCCCC);
                    paint.setAntiAlias(true);
                    for (int i = 1; i < h / Dips.dpToPx(25) + 1; i++) {
                        // 1 because no line at the beginning
                        for (int j = 1; j < w / Dips.dpToPx(25) + 1; j++) {
                            canvas.drawCircle(j * Dips.dpToPx(25), i * Dips.dpToPx(25), Dips.dpToPx(2), paint); //Dips.dpToPx(2)
                        }
                    }
                } else if (backText.equals(FileMeta.GRAPH)) {
/*
                final paint = Paint()
      ..color = Colors.grey[500].withOpacity(.3)
      ..strokeWidth = 1;
    // 1 because no line at the top
    for (int i = 1; i < size.height / XppPageSize.pt2mm(5); i++) {
      canvas.drawLine(Offset(0, i * XppPageSize.pt2mm(5).toDouble()),
          Offset(size.width, i * XppPageSize.pt2mm(5).toDouble()), paint);
    }
    // 1 because no line at the beginning
    for (int i = 1; i < size.width / XppPageSize.pt2mm(5); i++) {
      canvas.drawLine(Offset(i * XppPageSize.pt2mm(5).toDouble(), 0),
          Offset(i * XppPageSize.pt2mm(5).toDouble(), size.height), paint);
    }
                 */

                    int w = bmp.getWidth();
                    int h = bmp.getHeight();
                    //Canvas canvas = new Canvas(bmp);
                    Paint paint = new Paint();
                    paint.setColor(0xFFCCCCCC);
                    paint.setStrokeWidth(1);
                    paint.setAntiAlias(true);
                    //1 because no line at the top
                    for (int i = 1; i < h / Dips.dpToPx(25) + 1; i++) {
                        canvas.drawLine(0, i * Dips.dpToPx(25),
                                w, i * Dips.dpToPx(25), paint);
                    }
                    // 1 because no line at the beginning
                    for (int i = 1; i < w / Dips.dpToPx(25) + 1; i++) {
                        canvas.drawLine(i * Dips.dpToPx(25), 0,
                                i * Dips.dpToPx(25), h, paint);
                    }
                } else if (backText.equals(FileMeta.LINED)) {
/*
    final paint = Paint()
      ..color = Colors.grey[500].withOpacity(.3)
      ..strokeWidth = 1;
    // 1 because no line at the top
    for (int i = 1; i < size.height / 24; i++) {
      canvas.drawLine(Offset(0, i * 24.toDouble()),
          Offset(size.width, i * 24.toDouble()), paint);
    }
 */
                    int w = bmp.getWidth();
                    int h = bmp.getHeight();
                    //Canvas canvas = new Canvas(bmp);
                    Paint paint = new Paint();
                    paint.setColor(0xFFCCCCCC);
                    paint.setStrokeWidth(1);
                    paint.setAntiAlias(true);
                    // 1 because no line at the top
                    for (int i = 1; i < h / Dips.dpToPx(25); i++) {
                        canvas.drawLine(0, i * Dips.dpToPx(25),
                                w, i * Dips.dpToPx(25), paint);
                    }
                }
            }
        }
        if (isPen == 0 && this.tempBitmap != null) { //橡皮擦而且有图片缓存
            this.tempBitmap.eraseColor(0x00000000);
            Canvas canvas_ = new Canvas(this.tempBitmap);
            canvas_.drawBitmap(this.bitmap, 0, 0, this.bmpPaint);
            this.drawPathToCanvas(canvas_, this.path);
            canvas.drawBitmap(this.tempBitmap, 0, 0, this.bmpPaint);
        } else {
            canvas.drawBitmap(this.bitmap, 0, 0, this.bmpPaint); //FIXME: check null
            this.drawPathToCanvas(canvas, this.path);
        }
    }

//    private boolean isEraser() {
//        return !this.isPencil;
//    }
//
//    private void pencil() {
//        if (!this.isPencil) {
//            this.isPencil = true;
//        }
//    }
//
//    private void eraser() {
//        if (!this.isEraser()) {
//            this.isPencil = false;
//        }
//    }
//
//    public void penOrEraser(boolean isPen) {
//        if (isPen != this.isPencil) {
//            if (isPen) {
//                this.pencil();
//            } else {
//                this.eraser();
//            }
//        }
//    }
    public void setPenEraserBrush(int v) {
        this.isPen = v;
    }



    public void onPageIdx(int idx, OnLoadBitmapListener bitmapLoader) {
        if (this.pageIdx != idx) {
            this.pageIdx = idx;
            Bitmap newbmp = bitmapLoader.onLoadBitmap(idx);
            if (this.bitmap != null) {
                if (BookIO.USE_META_TXT) {
                    this.bitmap.eraseColor(0x00000000);
                } else {
                    this.bitmap.eraseColor(0xFFFFFFFF);
                }
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

    public void setPathPaintColor(int color) {
        if (pathPaint != null) {
            pathPaint.setColor(0xFF000000 | color);
        }
    }

    public void setThick(float val) {
        if (pathPaint != null) {
            pathPaint.setStrokeWidth(val);
        }
    }
}
