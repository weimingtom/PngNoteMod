package io.github.karino2.pngnote;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.github.karino2.pngnote.book.BookIO;
import io.github.karino2.pngnote.book.BookPage;
import io.github.karino2.pngnote.book.FastFile;
import io.github.karino2.pngnote.ui.CanvasBoox;
import io.github.karino2.pngnotemod.R;

public class BookActivity extends AppCompatActivity {
    public final static String EXTRA_DIRURLPATH = "EXTRA_DIRURLPATH";
    public final static String PAGE_IDX = "PAGE_IDX";

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
                    pageBmp = bookIO.loadBitmapOrNull(getBook().getPage(idx));
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
        this.ensureSave();
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

    private void addNewPageAndGo() {
        this.ensureSave();
        if (this.emptyBmp == null) {
            if (this.pageBmp != null) {
                this.emptyBmp = Bitmap.createBitmap(this.pageBmp.getWidth(),
                        this.pageBmp.getHeight(), Bitmap.Config.ARGB_8888);
                this.emptyBmp.eraseColor(0xFFFFFFFF);
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
            }
        }
        this._pageIdx = this.pageNum - 1;
        onPageIdxChange();
    }

    //删除页面
    private void removeCurrentPageAndGo() {
        if (this.pageNum <= 1) {
            BookPage page = this.getBook().getPage(this._pageIdx);
            addNewPageAndGo();
            this.getBook().removePage(page.getFile(), _bookIO);
        } else if (this._pageIdx <= 0) { //0页的话直接清空就可以了
            this._pageIdx = 0; //FIXME:???
            this.ensureSave();
            if (this.pageBmp != null) {
                this.emptyBmp = Bitmap.createBitmap(this.pageBmp.getWidth(),
                        this.pageBmp.getHeight(), Bitmap.Config.ARGB_8888);
                this.emptyBmp.eraseColor(0xFFFFFFFF);
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
        Intent intent = this.getIntent();
        if (intent != null) {
            this.dirUrl = intent.getData();
            this.dirUrlPath = intent.getStringExtra(EXTRA_DIRURLPATH);
            this.handlePageIdxArg(intent);
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
                finish();
            }
        });
        findViewById(R.id.buttonPen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPen();
            }
        });
        findViewById(R.id.buttonEraser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEraser();
            }
        });
        selectPenOrEraser(true);
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
                gotoGridPage();
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
                addNewPageAndGo();
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










        //FIXME:throw new RuntimeException("not implemented");
        canvas = (CanvasBoox) findViewById(R.id.canvas);
        try {
            Bitmap initBmp = getBookIO().loadBitmapOrNull(getBook().getPage(getPageIdx()));
            Bitmap bgBmp = getBookIO().loadBgOrNull(getBook());
            canvas.init(initBmp, bgBmp, initialPageIdx);
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


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book, menu);
        return true;
    }

    boolean isEraser = false;
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
                gotoGridPage();
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
                addNewPageAndGo();
                break;

            case R.id.share:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    share();
                }
                break;
        }
        return true;
    }
    private void onPen() {
//        if (item.isChecked()) {
//            item.setChecked(false);
//            isEraser = true;
//            canvas.penOrEraser(!isEraser);
//        } else {
//            item.setChecked(true);
//            isEraser = false;
//            canvas.penOrEraser(!isEraser);
//        }
        //FIXME:
        if (!isEraser) {
            isEraser = true;
            selectPenOrEraser(false);
            canvas.penOrEraser(false);
        } else {
            isEraser = false;
            selectPenOrEraser(true);
            canvas.penOrEraser(true);
        }
    }
    private void onEraser() {
//        if (item.isChecked()) {
//            item.setChecked(false);
//            isEraser = false;
//            canvas.penOrEraser(!isEraser);
//        } else {
//            item.setChecked(true);
//            isEraser = true;
//            canvas.penOrEraser(!isEraser);
//        }
        //FIXME:
        if (isEraser) {
            isEraser = false;
            selectPenOrEraser(true);
            canvas.penOrEraser(true);
        } else {
            isEraser = true;
            selectPenOrEraser(false);
            canvas.penOrEraser(false);
        }
    }
    private void selectPenOrEraser(boolean selectPen) {
        if (selectPen) {
            findViewById(R.id.buttonPen).setEnabled(false);
            findViewById(R.id.buttonEraser).setEnabled(true);
            ((ImageButton)findViewById(R.id.buttonPen)).setImageResource(R.drawable.ic_baseline_edit_24);
            ((ImageButton)findViewById(R.id.buttonEraser)).setImageResource(R.mipmap.eraser_button_selected);
        } else {
            findViewById(R.id.buttonPen).setEnabled(true);
            findViewById(R.id.buttonEraser).setEnabled(false);
            ((ImageButton)findViewById(R.id.buttonPen)).setImageResource(R.drawable.ic_baseline_edit_24_selected);
            ((ImageButton)findViewById(R.id.buttonEraser)).setImageResource(R.mipmap.eraser_button);
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
}
