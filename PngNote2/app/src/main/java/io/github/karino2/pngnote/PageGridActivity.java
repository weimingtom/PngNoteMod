package io.github.karino2.pngnote;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.karino2.pngnote.book.FastFile;
import io.github.karino2.pngnote.book.BookIO;
import io.github.karino2.pngnote.ui.CanvasBoox;
import io.github.karino2.pngnote.ui.Page;
import io.github.karino2.pngnote.ui.PageGridAdapter;
import io.github.karino2.pngnote.ui.PageGridData;
import io.github.karino2.pngnotemod.R;

import org.json.JSONObject;

public class PageGridActivity extends AppCompatActivity {
    public final static String EXTRA_DIR_URL_PATH = "EXTRA_DIR_URL_PATH";

    private Uri dirUrl;
    private String dirUrlPath;
    private FastFile _bookDir;// = bookDir_init();
    private FastFile bookDir_init() {
        return FastFile.fromTreeUri(this, this.dirUrl, this.dirUrlPath);
    }

    private BookIO _bookIO;// = bookIO_init();
    private BookIO bookIO_init() {
        return new BookIO(this.getContentResolver());
    }

    private Book _book;
    private Bitmap _bgImage;// = bgImage_init();
    private Bitmap bgImage_init() {
        return this.getBookIO().loadBgForGrid(this.getBook().getBookDir());
    }

    private List<Page> _pageList;// = pageList_init();
    private List<Page> pageList_init() {
        List<FastFile> pages = this.getBook().getPages();
        List<Page> result = new ArrayList<Page>(pages != null ? pages.size() : 10);
        int idxNext = 0;
        Iterator<FastFile> var8 = pages.iterator();
        while (var8.hasNext()) { //FIXME:
            var8.next();
            int idx = idxNext++;
            if (idx < 0) {
                throw new IndexOutOfBoundsException(); //FIXME:
            }
            Page page = new Page(String.valueOf(idx + 1), PageGridData.getBlankBitmap(), this.getBgImage());
            result.add(page);
        }
        return result;
    }

    private Pair<Double, Double> _pageSizeDP;// = pageSizeDP_init();
    private Pair<Double, Double> pageSizeDP_init() {
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return PageGridActivity.displayMetricsTo4GridSize(metrics);
    }

    private void onCreateAct() {
        _bookDir = bookDir_init();
        _bookIO = bookIO_init();
        _bgImage = bgImage_init();
        _pageList = pageList_init();
        _pageSizeDP = pageSizeDP_init();
    }

    private FastFile getBookDir() {
        return this._bookDir;
    }

    private BookIO getBookIO() {
        return this._bookIO;
    }

    private Book getBook() {
        if (this._book != null) {
            return this._book;
        } else {
            this._book = this.getBookIO().loadBook(this.getBookDir());
            return _book;
        }
    }

    private Bitmap getBgImage() {
        return this._bgImage;
    }

    private List<Page> getPageList() {
        return this._pageList;
    }

    private Pair getPageSizeDP() {
        return this._pageSizeDP;
    }

    private void requestLoadPages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int idx = 0; idx < getBook().getPages().size(); ++idx) {
                    FastFile bmpFile = getBook().getPages().get(idx);
                    Bitmap bitmap = getBookIO().loadPageThumbnail(bmpFile);
                    if (BookIO.USE_META_TXT) {
                        try {
                            String pattern = null;
                            String metaTxt = getBookIO().loadMetaPng(bmpFile);
                            JSONObject item = new JSONObject(metaTxt);
                            if (item != null) {
                                pattern = item.optString("pattern");
                            }
                            if (pattern != null) {
                                Bitmap emptyBmp = Bitmap.createBitmap(bitmap.getWidth(),
                                        bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                                if (BookIO.USE_META_TXT) {
                                    emptyBmp.eraseColor(0x00000000);
                                } else {
                                    emptyBmp.eraseColor(0x00000000);
                                }
                                CanvasBoox.initBackText(pattern, emptyBmp, BookIO.loadPageThumbnail_size);
                                Canvas canvas = new Canvas(emptyBmp);
                                Paint paint = new Paint();
                                canvas.drawBitmap(bitmap, 0, 0, paint);
                                bitmap = emptyBmp;
                            }
                        } catch (Throwable eee) {
                            eee.printStackTrace();
                        }
                    }
                    final int idx_ = idx;
                    final Bitmap bitmap_ = bitmap;
                    PageGridActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<Page> result = new ArrayList<Page>();
                            for (int idx2 = 0; idx2 < getPageList().size(); ++idx2) {
                                Page page = getPageList().get(idx2);
                                if (idx_ == idx2) {
                                    result.add(page.copy(null, bitmap_, null));
                                } else {
                                    result.add(page);
                                }
                            }
                            _pageList.clear();
                            _pageList.addAll(result);
                            bookGridAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }

    public void openPage(int pageIdx) {
        Intent it = new Intent(this, BookActivity.class);
        it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        it.setData(this.getBookDir().getUri());
        it.putExtra(BookActivity.EXTRA_DIRURLPATH, this.getBookDir().getFilePath());
        it.putExtra(BookActivity.PAGE_IDX, pageIdx);
        this.startActivity(it);
    }

    private GridView gridview;
    private PageGridAdapter bookGridAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        if (intent != null) {
            this.dirUrl = intent.getData();
            this.dirUrlPath = intent.getStringExtra(EXTRA_DIR_URL_PATH);
        }

        onCreateAct();

        setContentView(R.layout.activity_pagegrid);
        ActionBar topAppBar = getSupportActionBar();
        if (topAppBar != null) {
            topAppBar.setTitle(getBook().getName());
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

        gridview = (GridView) findViewById(R.id.bookgridview);
        gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridview.setBackgroundColor(Color.WHITE);
        bookGridAdapter = new PageGridAdapter(this, getPageList());
        gridview.setAdapter(bookGridAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pageIdx = position;
                openPage(pageIdx);
            }
        });
        this.requestLoadPages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //https://blog.csdn.net/aaLiweipeng/article/details/82948525
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    public static Pair<Double, Double> displayMetricsTo4GridSize(DisplayMetrics metrics) {
        double height = (metrics.heightPixels * 0.20 / metrics.density);
        double width = (metrics.widthPixels * 0.225 / metrics.density);
        return new Pair<Double, Double>(width, height);
    }
}