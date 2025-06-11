package io.github.karino2.pngnote;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.github.karino2.pngnote.book.BookIO;
import io.github.karino2.pngnote.book.BookPage;
import io.github.karino2.pngnote.book.FastFile;
import io.github.karino2.pngnote.colorpicker.LineWidthDialog;
import io.github.karino2.pngnote.colorpicker.MaterialColorDialog;
import io.github.karino2.pngnote.colorpicker.PaintSelectDialog;
import io.github.karino2.pngnote.colorpicker.SimpleColorDialog;
import io.github.karino2.pngnote.ui.CanvasBoox;
import io.github.karino2.pngnotemod.R;

import org.json.JSONObject;

public class BookActivity extends AppCompatActivity {
    public final static boolean SAVING_ASYNC = true;

    public final static String EXTRA_DIRURLPATH = "EXTRA_DIRURLPATH";
    public final static String EXTRA_BACKTEXT = "EXTRA_BACKTEXT";
    public final static String PAGE_IDX = "PAGE_IDX";

    public String curPattern;
    private String backText; //背景图案的种类文本

    private Uri dirUrl;
    private String dirUrlPath;
    private int initialPageIdx;
    private FastFile _bookDir;// = bookDir_init();
    FastFile bookDir_init() {
        //FIXME:check this.dirUrlPath null
        return FastFile.fromTreeUri(this, this.dirUrl, this.dirUrlPath);
    }

    private BookIO _bookIO;// = bookIO_init();
    private BookIO bookIO_init() {
        return new BookIO(this.getContentResolver());
    }

    private Book _book;
    private int _pageIdx;// = pageIdx_init();
    private int pageIdx_init() {
        return BookActivity.this.initialPageIdx;
    }

    private void onCreateAct() {
        _bookDir = bookDir_init();
        _bookIO = bookIO_init();
        _pageIdx = pageIdx_init();
    }
    private void onPageIdxChange() {
        int idx = getPageIdx();
        if (idx < 0) {
            idx = this._pageIdx = 0;
        } else if (idx >= getBook().getPages().size()) {
            idx = this._pageIdx = getBook().getPages().size() - 1;
        }
        if (idx >= 0 && idx < getBook().getPages().size()) { //FIXME: null??
            canvas.onPageIdx(idx, new CanvasBoox.OnLoadBitmapListener() {
                @Override
                public Bitmap onLoadBitmap(int idx) {
                    BookIO bookIO = getBookIO();
                    BookPage page = getBook().getPage(idx);
                    pageBmp = bookIO.loadBitmapOrNull(page);
                    try {
                        String metaTxt = bookIO.loadMetaPng(page.getFile());
                        JSONObject item = new JSONObject(metaTxt);
                        if (item != null) {
                            curPattern = item.optString("pattern");
                        }
                    } catch (Throwable eee) {
                        eee.printStackTrace();
                    }
                    isDirty = false;
                    return pageBmp;
                }
            });
            onUpdatePidxPnum();
        }
    }

    private int pageNum = 0;
    private boolean canRedo = false;
    private boolean canUndo = false;
    private int undoCount = 0;
    private int redoCount = 0;
    private long lastWritten = -1L;
    private Bitmap emptyBmp;
    private Bitmap pageBmp;
    private boolean isDirty;
    private static final long SAVE_INTERVAL_MILL = 5000L;
    private static final ReentrantLock bitmapLock = new ReentrantLock();

    private FastFile getBookDir() {
        return this._bookDir;
    }

    private BookIO getBookIO() {
        return this._bookIO;
    }

    private void set_book(Book newbook) {
        this.pageNum = 0;
        this._book = newbook;
        if (newbook != null) {
            List<FastFile> pages = newbook.getPages();
            if (pages != null) {
                this.pageNum = pages.size();
            }
        }
        this.onUpdatePidxPnum();
    }

    private Book getBook() {
        if (this._book != null) {
            return this._book;
        } else {
            Book it = this.getBookIO().loadBook(this.getBookDir());
            this.set_book(it);
            return it;
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private int getPageIdx() {
        return this._pageIdx;
    }

    private void notifyBitmapUpdate(Bitmap newBmp) {
        this.isDirty = true;
        this.lastWritten = this.getCurrentMills();
        this.pageBmp = newBmp;
        this.lazySave();
    }

    private void notifyUndoStateChanged(boolean canUndo1, boolean canRedo1) {
        this.canUndo = canUndo1;
        this.canRedo = canRedo1;
    }

    private long getCurrentMills() {
        return (new Date()).getTime();
    }

    private void savePage(final int pageIdx, Bitmap pageBmp) {
        this.getBookIO().saveBitmap(this.getBook().getPage(pageIdx), pageBmp);
        new Thread(new Runnable() {
            @Override
            public void run() {
                BookActivity.this.set_book(BookActivity.this.getBook().assignNonEmpty(pageIdx));
            }
        }).start();
    }

    private void savePageInMain(int pageIdx, Bitmap pageBmp) {
        this.getBookIO().saveBitmap(this.getBook().getPage(pageIdx), pageBmp);
        this.set_book(this.getBook().assignNonEmpty(pageIdx));
    }

    private void lazySave() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(BookActivity.this.SAVE_INTERVAL_MILL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (BookActivity.this.isDirty &&
                        BookActivity.this.getCurrentMills() - BookActivity.this.lastWritten
                        >= SAVE_INTERVAL_MILL) {
                    BookActivity.this.isDirty = false;
                    Lock bitmapLock = BookActivity.getBitmapLock();
                    bitmapLock.lock();
                    Bitmap tempBmp = null;
                    try {
                        tempBmp = BookActivity.this.pageBmp.copy(BookActivity.this.pageBmp.getConfig(), false);
                    } catch (Throwable eee) {
                        eee.printStackTrace();
                    } finally {
                        bitmapLock.unlock();
                    }
                    savePage(getPageIdx(), tempBmp);
                }
            }
        }).start();
    }

    private void ensureSave() {
        if (this.isDirty) {
            this.isDirty = false;
            this.savePageInMain(this.getPageIdx(), this.pageBmp);
        }
    }

    protected void onStop() {
        if (true) {
            this.ensureSave();
        } else {
            this.savePageInMain(this.getPageIdx(), this.pageBmp);
        }
        super.onStop();
    }

    //FIXME: remove RequiresApi
    //@RequiresApi(26)
    private void share() {
        this.ensureSave();
        if (this.pageBmp != null) {
            FileOutputStream it = null;
            File path = null;
            try {
                path = File.createTempFile("share", ".png", this.getCacheDir());
                it = new FileOutputStream(path);
                this.pageBmp.compress(Bitmap.CompressFormat.PNG, 100, it);
                it.flush();
            } catch (Throwable e) {
                e.printStackTrace();
                path = null;
            } finally {
                try {
                    if (it != null) {
                        it.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (path != null) {
                Uri u = FileProvider.getUriForFile(this,
                        this.getApplicationContext().getPackageName() + ".provider",
                        path);
                this.shareImageUri(u);
            }
        }
    }

    private void shareImageUri(Uri uri) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.STREAM", (Parcelable)uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        this.startActivity(intent);
    }

    private void addNewPageAndGo(boolean needAsk) {
        if (needAsk) {
            PaintSelectDialog dialog = new PaintSelectDialog(this);
            dialog.show();
        } else {
            addNewPageAndGo_old();
        }
    }

    private void addNewPageAndGo_old() {
        this.ensureSave();
        if (this.emptyBmp == null) {
            if (this.pageBmp != null) {
                this.emptyBmp = Bitmap.createBitmap(this.pageBmp.getWidth(),
                        this.pageBmp.getHeight(), Bitmap.Config.ARGB_8888);
                if (BookIO.USE_META_TXT) {
                    this.emptyBmp.eraseColor(0x00000000);
                } else {
                    this.emptyBmp.eraseColor(0xFFFFFFFF);
                }
                if (!BookIO.USE_META_TXT) {
                    CanvasBoox.initBackText(this.backText, this.emptyBmp, 1);
                }
            }
        } else {
            if (BookIO.USE_META_TXT) {
                this.emptyBmp.eraseColor(0x00000000);
            } else {
                this.emptyBmp.eraseColor(0xFFFFFFFF);
            }
            if (!BookIO.USE_META_TXT) {
                CanvasBoox.initBackText(this.backText, this.emptyBmp, 1);
            }
        }
        this.set_book(this.getBook().addPage());
        if (false) {
            //FIXME:重复上一张,why????
            if (this.pageBmp != null) {
                this.savePageInMain(this.pageNum - 1, this.pageBmp);
            }
        } else {
            //FIXME:空白页？？？
            if (this.emptyBmp != null) {
                this.pageBmp = this.emptyBmp; //FIXME:???
            }
            if (this.pageBmp != null) {
                this.savePageInMain(this.pageNum - 1, this.pageBmp);
                if (BookIO.USE_META_TXT) {
                    getBookIO().saveMeta(backText, this.dirUrlPath, String.format("%04d", this.pageNum - 1) + ".meta");
                }
            }
        }
        this._pageIdx = this.pageNum - 1;
        onPageIdxChange();
    }

    //删除页面
    private void removeCurrentPageAndGo() {
        if (this.pageNum <= 1) {
            BookPage page = this.getBook().getPage(this._pageIdx);
            addNewPageAndGo(false);
            this.getBook().removePage(page.getFile(), _bookIO);
        } else if (this._pageIdx <= 0) { //0页的话直接清空就可以了
            this._pageIdx = 0; //FIXME:???
            this.ensureSave();
            if (this.pageBmp != null) {
                this.emptyBmp = Bitmap.createBitmap(this.pageBmp.getWidth(),
                        this.pageBmp.getHeight(), Bitmap.Config.ARGB_8888);
                if (BookIO.USE_META_TXT) {
                    this.emptyBmp.eraseColor(0x00000000);
                } else {
                    this.emptyBmp.eraseColor(0xFFFFFFFF);
                }
                if (!BookIO.USE_META_TXT) {
                    CanvasBoox.initBackText(this.backText, this.emptyBmp, 1);
                }
            }
            //FIXME:空白页？？？
            if (this.emptyBmp != null) {
                this.pageBmp = this.emptyBmp; //FIXME:???
            }
            if (this.pageBmp != null) {
                this.savePageInMain(this.pageNum - 1, this.pageBmp);
            }
            onPageIdxChange();
        } else {
            BookPage page = this.getBook().getPage(this._pageIdx);
            gotoPrevPage();
            this.getBook().removePage(page.getFile(), _bookIO);
        }

        //--------------
        this._book = null;
        set_book(getBook()); //FIXME:???重新加载
        //--------------

        onPageIdxChange();
    }

    private void gotoFirstPage() {
        this.ensureSave();
        this._pageIdx = 0;
        onPageIdxChange();
    }

    private void gotoLastPage() {
        this.ensureSave();
        this._pageIdx = this.pageNum - 1;
        onPageIdxChange();
    }

    private void gotoPrevPage() {
        this.ensureSave();
        int var1 = this.getPageIdx();
        if (var1 >= 1) {
            this._pageIdx = var1 - 1;
            onPageIdxChange();
        }
    }

    private void gotoNextPage() {
        this.ensureSave();
        int var1 = this.getPageIdx();
        this._pageIdx = var1 + 1;
        onPageIdxChange();
    }

    private void gotoGridPage() {
        Intent intent = new Intent(this, PageGridActivity.class);
        intent.setData(this.dirUrl);
        intent.putExtra(PageGridActivity.EXTRA_DIR_URL_PATH, this.dirUrlPath);
        this.startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            this.handlePageIdxArg(intent);
        }
    }

    private TextView textViewPageInfo;
    CanvasBoox canvas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newFixedThreadPool = Executors.newFixedThreadPool(6);
        Intent intent = this.getIntent();
        boolean isInitBackText = false;
        if (intent != null) {
            this.dirUrl = intent.getData();
            this.dirUrlPath = intent.getStringExtra(EXTRA_DIRURLPATH);
            this.backText = intent.getStringExtra(EXTRA_BACKTEXT);
            this.handlePageIdxArg(intent);
            isInitBackText = true;
        }

        setContentView(R.layout.activity_book);
        ActionBar topAppBar = getSupportActionBar();
        if (topAppBar != null) {
            topAppBar.setIcon(R.mipmap.ic_launcher);
            topAppBar.setHomeButtonEnabled(true);
            topAppBar.setDisplayHomeAsUpEnabled(true);
            if (!Config.USE_ACTIONBAR) {
                topAppBar.hide();
            }
        }
        findViewById(R.id.buttonBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FIXME:退出立即保存
                if (SAVING_ASYNC) {
                    if (task == null) {
                        task = new SavingTask(true);
                        task.executeOnExecutor(newFixedThreadPool);
                    }
                } else {
                    if (false) {
                        ensureSave();
                    } else {
                        savePageInMain(getPageIdx(), pageBmp);
                    }
                    finish();
                }
            }
        });
        findViewById(R.id.buttonPen2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPen2();
            }
        });
        findViewById(R.id.buttonEraser2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEraser2();
            }
        });

        findViewById(R.id.buttonPenColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPenColor();
            }
        });
        findViewById(R.id.buttonPaint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectPaint();
            }
        });
        findViewById(R.id.buttonPen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPen();
            }
        });
        findViewById(R.id.buttonBrush).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBrush();
            }
        });
        findViewById(R.id.buttonEraser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEraser();
            }
        });

//        selectPenOrEraser(true);

        findViewById(R.id.buttonUndo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUndo();
            }
        });
        findViewById(R.id.buttonRedo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRedo();
            }
        });
        findViewById(R.id.buttonGrid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SAVING_ASYNC) {
                    if (task == null) {
                        task = new SavingTask(false);
                        task.executeOnExecutor(newFixedThreadPool);
                    }
                } else {
                    gotoGridPage();
                }
            }
        });
        findViewById(R.id.buttonFirstPage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoFirstPage();
            }
        });
        findViewById(R.id.buttonPrevPage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoPrevPage();
            }
        });
        findViewById(R.id.buttonNextPage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNextPage();
            }
        });
        findViewById(R.id.buttonLastPage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoLastPage();
            }
        });
        findViewById(R.id.buttonAddPage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewPageAndGo(true);
            }
        });
        findViewById(R.id.buttonRemovePage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCurrentPageAndGo();
            }
        });
        findViewById(R.id.buttonShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    share();
                }
            }
        });
        textViewPageInfo = (TextView) findViewById(R.id.textViewPageInfo);
        onCreateAct();
        if (BookIO.USE_META_TXT) {
            if (isInitBackText && backText != null) {
                //如果是创建的才会走这里
                getBookIO().saveMeta(backText, this.dirUrlPath, "0000.meta");
            }
        }









        //FIXME:throw new RuntimeException("not implemented");
        canvas = (CanvasBoox) findViewById(R.id.canvas);
        try {
            BookPage page = getBook().getPage(getPageIdx());
            Bitmap initBmp = getBookIO().loadBitmapOrNull(page);
            Bitmap bgBmp = getBookIO().loadBgOrNull(getBook());
            try {
                String metaTxt = getBookIO().loadMetaPng(page.getFile());
                JSONObject item = new JSONObject(metaTxt);
                if (item != null) {
                    curPattern = item.optString("pattern");
                }
            } catch (Throwable eee) {
                eee.printStackTrace();
            }
            canvas.init(initBmp, bgBmp, initialPageIdx, this);
            canvas.setBackText(backText);
        } catch (Throwable eee) {
            eee.printStackTrace();
        }
        canvas.setClipToOutline(true);
        canvas.setOnUpdateListener(new CanvasBoox.OnUpdateBmpListener() {
            @Override
            public void onUpdateBmp(Bitmap bmp) {
                notifyBitmapUpdate(bmp);
            }
        });
        canvas.setOnUndoStateListener(new CanvasBoox.OnUndoStateListener() {
            @Override
            public void onUndoState(Boolean undo, Boolean redo) {
                notifyUndoStateChanged(undo, redo);
            }
        });

        //恢复画笔选择初始状态
        isPenEraserBrush = 1;
        selectPenOrEraser(1);
        canvas.setPenEraserBrush(1);
    }

    @Override
    public void onBackPressed() {
        //FIXME:
        //FIXME:退出立即保存
        if (SAVING_ASYNC) {
            if (task == null) {
                task = new SavingTask(true);
                task.executeOnExecutor(newFixedThreadPool);
            }
        } else {
            if (false) {
                ensureSave();
            } else {
                savePageInMain(getPageIdx(), pageBmp);
            }
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book, menu);
        return true;
    }

    int isPenEraserBrush = 1; //0:Eraser;1:Pen;2:Brush //初始状态是pen
//    boolean isEraser = false;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pen:
                onPen();
                break;

            case R.id.eraser:
                onEraser();
                break;

            case R.id.undo:
                onUndo();
                break;

            case R.id.redo:
                onRedo();
                break;

            case R.id.grid:
                if (SAVING_ASYNC) {
                    if (task == null) {
                        task = new SavingTask(false);
                        task.executeOnExecutor(newFixedThreadPool);
                    }
                } else {
                    gotoGridPage();
                }
                break;

            case R.id.firstPage:
                gotoFirstPage();
                break;

            case R.id.prevPage:
                gotoPrevPage();
                break;

            case R.id.nextPage:
                gotoNextPage();
                break;

            case R.id.lastPage:
                gotoLastPage();
                break;

            case R.id.addPage:
                addNewPageAndGo(true);
                break;

            case R.id.share:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    share();
                }
                break;
        }
        return true;
    }
    private void onPenColor() {
        if (false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (dialogInterface != null) {
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    if (dialogInterface != null) {
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.setView(R.layout.dialog_selectcolor);
            builder.setCancelable(true);
            final AlertDialog dialog = builder.create();
            //        dialog.setContentView(R.layout.dialog_loadpages); //don't use this
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {

                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {

                }
            });
            dialog.show();
        } else if (false) {
            MaterialColorDialog dialog = new MaterialColorDialog(this);
            dialog.show();
        } else {
            SimpleColorDialog dialog = new SimpleColorDialog(this);
            dialog.show();
        }
    }
    private void onSelectPaint() {
        PaintSelectDialog dialog = new PaintSelectDialog(this);
        dialog.show();
    }

    private void onPen2() {
        if (isPenEraserBrush != 1) {
            isPenEraserBrush = 1;
            selectPenOrEraser(1);
            canvas.setPenEraserBrush(1);
        }
        onLineThicknessButton();
    }
    private void onEraser2() {
        if (isPenEraserBrush != 0) {
            isPenEraserBrush = 0;
            selectPenOrEraser(0);
            canvas.setPenEraserBrush(0);
        }
    }

    private void onPen() {
//        if (!isEraser) {
//            isEraser = true;
//            selectPenOrEraser(false);
//            canvas.penOrEraser(false);
//        } else {
//            isEraser = false;
//            selectPenOrEraser(true);
//            canvas.penOrEraser(true);
//        }
        if (isPenEraserBrush != 1) {
            isPenEraserBrush = 1;
            //selectPenOrEraser(true);
            selectPenOrEraser(1);
            //canvas.penOrEraser(true);
            canvas.setPenEraserBrush(1);
        }
    }
    private void onBrush() {
        if (isPenEraserBrush != 2) {
            isPenEraserBrush = 2;
            //selectPenOrEraser(false);
            selectPenOrEraser(2);
            //canvas.penOrEraser(false);
            canvas.setPenEraserBrush(2);
        }
    }
    private void onEraser() {
//        if (isEraser) {
//            isEraser = false;
//            selectPenOrEraser(true);
//            canvas.penOrEraser(true);
//        } else {
//            isEraser = true;
//            selectPenOrEraser(false);
//            canvas.penOrEraser(false);
//        }
        if (isPenEraserBrush != 0) {
            isPenEraserBrush = 0;
            //selectPenOrEraser(false);
            selectPenOrEraser(0);
//            canvas.penOrEraser(false);
            canvas.setPenEraserBrush(0);
        }
    }
//    private void selectPenOrEraser(boolean selectPen) {
//        if (selectPen) {
//            findViewById(R.id.buttonPen).setEnabled(false);
//            findViewById(R.id.buttonEraser).setEnabled(true);
//            ((ImageButton)findViewById(R.id.buttonPen)).setImageResource(R.drawable.ic_baseline_edit_24);
//            ((ImageButton)findViewById(R.id.buttonEraser)).setImageResource(R.mipmap.eraser_button_selected);
//        } else {
//            findViewById(R.id.buttonPen).setEnabled(true);
//            findViewById(R.id.buttonEraser).setEnabled(false);
//            ((ImageButton)findViewById(R.id.buttonPen)).setImageResource(R.drawable.ic_baseline_edit_24_selected);
//            ((ImageButton)findViewById(R.id.buttonEraser)).setImageResource(R.mipmap.eraser_button);
//        }
//    }
    private void selectPenOrEraser(int v) {
        if (v == 1) {
            findViewById(R.id.buttonPen).setEnabled(false);
            findViewById(R.id.buttonEraser).setEnabled(true);
            findViewById(R.id.buttonBrush).setEnabled(true);
            ((ImageButton) findViewById(R.id.buttonPen)).setImageResource(R.drawable.ic_baseline_edit_24);
            ((ImageButton) findViewById(R.id.buttonEraser)).setImageResource(R.mipmap.eraser_button_selected);
            ((ImageButton)findViewById(R.id.buttonBrush)).setImageResource(R.drawable.ic_baseline_brush_24_selected);
        } else if (v == 2) {
            findViewById(R.id.buttonPen).setEnabled(true);
            findViewById(R.id.buttonEraser).setEnabled(true);
            findViewById(R.id.buttonBrush).setEnabled(false);
            ((ImageButton) findViewById(R.id.buttonPen)).setImageResource(R.drawable.ic_baseline_edit_24_selected);
            ((ImageButton) findViewById(R.id.buttonEraser)).setImageResource(R.mipmap.eraser_button_selected);
            ((ImageButton)findViewById(R.id.buttonBrush)).setImageResource(R.drawable.ic_baseline_brush_24);
        } else if (v == 0){
            findViewById(R.id.buttonPen).setEnabled(true);
            findViewById(R.id.buttonEraser).setEnabled(false);
            findViewById(R.id.buttonBrush).setEnabled(true);
            ((ImageButton)findViewById(R.id.buttonPen)).setImageResource(R.drawable.ic_baseline_edit_24_selected);
            ((ImageButton)findViewById(R.id.buttonEraser)).setImageResource(R.mipmap.eraser_button);
            ((ImageButton)findViewById(R.id.buttonBrush)).setImageResource(R.drawable.ic_baseline_brush_24_selected);
        }

        if (v == 1) {
            ((CardView)findViewById(R.id.buttonPen2Wrapper)).setCardBackgroundColor(0xFFCCCCCC);
            ((CardView)findViewById(R.id.buttonEraser2Wrapper)).setCardBackgroundColor(0xFFFFFFFF);
        } else {
            ((CardView)findViewById(R.id.buttonPen2Wrapper)).setCardBackgroundColor(0xFFFFFFFF);
            ((CardView)findViewById(R.id.buttonEraser2Wrapper)).setCardBackgroundColor(0xFFCCCCCC);
        }
    }

    private void onUndo() {
        undoCount = undoCount + 1;
        canvas.undo(undoCount);
    }
    private void onRedo() {
        redoCount = redoCount + 1;
        canvas.redo(redoCount);
    }
    private void onUpdatePidxPnum() {
        if (textViewPageInfo != null) {
            textViewPageInfo.post(new Runnable() {
                @Override
                public void run() {
                    textViewPageInfo.setText("" + (getPageIdx() + 1) + "/" + pageNum);
                };
            });
        }
    }


    private void handlePageIdxArg(Intent intent) {
        int argPageIdx = intent.getIntExtra(PAGE_IDX, -1);
        if (argPageIdx != -1) {
            this.initialPageIdx = argPageIdx;
        }
    }

    public static ReentrantLock getBitmapLock() {
        return BookActivity.bitmapLock;
    }

    public void setPenColor(int color) {
//        ImageButton buttonPenColor = (ImageButton) this.findViewById(R.id.buttonPenColor);
//
//        @SuppressLint("UseCompatLoadingForDrawables")
//        Drawable it = this.getDrawable(R.drawable.ic_baseline_color_lens_24);
//        DrawableCompat.setTintList(it, ColorStateList.valueOf(color));
//        buttonPenColor.setImageDrawable(it);
        CardView cvColorSel = this.findViewById(R.id.cvColorSel);
        cvColorSel.setCardBackgroundColor(0xff000000 | color);

        canvas.setPathPaintColor(color);
    }

    public void onPaintSelect(String backText) {
        this.backText = backText;
        addNewPageAndGo_old();
    }












    private class SavingTask extends AsyncTask<Void, Void, Void> {
        private boolean mIsBack = true;
        public SavingTask(boolean isBack) {
            this.mIsBack = isBack;
            createWaitingProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                savePageInMain(getPageIdx(), pageBmp);
            } catch (Throwable eee) {
                eee.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            cancelWaitingProgressDialog();
            task = null;
            if (this.mIsBack) {
                finish();
            } else {
                gotoGridPage();
            }
        }
    }

    private SavingTask task = null;
    private ExecutorService newFixedThreadPool;
    private ProgressDialog mProgressDialog = null; // 对话框对象
    protected void createWaitingProgressDialog() {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setTitle("");
            mProgressDialog.setMessage("Saving, please wait...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }
    protected void cancelWaitingProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    private float mCurrentInkLineThickness = 4.5f;
    public float getInkLineThickness() {
        return mCurrentInkLineThickness;
    }
    public void setInkLineThickness(float val) {
        mCurrentInkLineThickness=val;
        canvas.setThick(val);
    }
    private void onLineThicknessButton()  {
        float val = getInkLineThickness();
        ImageButton btn = (ImageButton)findViewById(R.id.buttonPen2);
        LineWidthDialog.show(this, btn, val,
            new LineWidthDialog.WidthChangedListener() {
                @Override
                public void onWidthChanged(float value) {
                    setInkLineThickness(value);
                }
        });
    }
}
