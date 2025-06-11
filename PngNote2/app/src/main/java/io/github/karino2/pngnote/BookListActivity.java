package io.github.karino2.pngnote;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.karino2.pngnote.book.BookIO;
import io.github.karino2.pngnote.book.FastFile;
import io.github.karino2.pngnote.book.SimpleFileMeta;
import io.github.karino2.pngnote.booklist.BookList;
import io.github.karino2.pngnote.colorpicker.NewNoteDialog;
import io.github.karino2.pngnote.ui.CanvasBoox;
import io.github.karino2.pngnote.ui.Page;
import io.github.karino2.pngnote.ui.PageGridAdapter;
import io.github.karino2.pngnote.ui.PageGridData;
import io.github.karino2.pngnote.ui.PageListAdapter;
import io.github.karino2.pngnotemod.R;

import org.json.JSONObject;

public class BookListActivity extends AppCompatActivity {
    private final static boolean D = true;
    private final static String TAG = "BookListActivity";

    private static String APP_OPEN;
    private static String APP_FILE;


    private Uri _url;
    private String _urlPath;

    //if (BookIO.USE_CONTENT_RESOLVER)
//    private ActivityResultLauncher<Uri> getRootDirUrl;
    private Bitmap blankBitmap;
    private List<FastFile> files;
    private List<Page> pageList;
    private Pair<Double, Double> _bookSizeDP;
    private BookIO _bookIO;

    public BookListActivity() {

    }

    public void onActCreate() {
        if (BookIO.USE_CONTENT_RESOLVER) {
//        this.getRootDirUrl = this.registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
//                new ActivityResultCallback<Uri>() {
//                    @Override
//                    public void onActivityResult(Uri o) {
//                        BookListActivity.this.getRootDirUrl(o);
//                    }
//                });
        }
        this.blankBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        this.blankBitmap.eraseColor(PageGridData.THUMB_BACK_COLOR);
        this.files = new ArrayList<FastFile>(); //CollectionsKt.emptyList();
        this.pageList = new ArrayList<Page>();//CollectionsKt.emptyList();
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            this._bookSizeDP = PageGridActivity.displayMetricsTo4GridSize(metrics);
        } catch (Throwable eee) {
            eee.printStackTrace();
        }
        this._bookIO = new BookIO(this.getContentResolver());
    }

    private Uri getLastUri() {
        String it = BookList.lastUriStr(this);
        Uri result = null;
        if (it != null) {
            result = Uri.parse(it);
        }
        return result;
    }

    private boolean writeLastUri(Uri uri) {
        return BookList.writeLastUriStr(this, uri.toString());
    }

    private void showMessage(String msg) {
        BookList.showMessage(this, msg);
    }

    private void openRootDir(Uri url, String urlPath) {
        this._url = url;
        this._urlPath = urlPath;
        this.reloadBookList(url, urlPath);
    }

    public Bitmap getBlankBitmap() {
        return this.blankBitmap;
    }

    private boolean firstNew = true;
    private int stateStarted_ = 0;
    private void updateFiles(List<FastFile> newFiles) {
        this.files.clear();
        this.files.addAll(newFiles);
        List<Page> tempList = new ArrayList<Page>(newFiles.size() > 0 ? newFiles.size() : 10);
        for (FastFile file : newFiles) {
            tempList.add(new Page(file.getName(), this.blankBitmap, this.blankBitmap));
        }
        List<SimpleFileMeta> datas = null;
        try {
            datas = getBookIO().loadRecent();
        } catch (Throwable eee) {
            eee.printStackTrace();
        }
        if (datas == null) {
            datas = new ArrayList<SimpleFileMeta>();
        }
        Map<String, Long> updateTimeMap = new HashMap<String, Long>();
        for (SimpleFileMeta meta : datas) {
            if (meta != null && meta.getName() != null &&
                    meta.getUpdateTime() != null &&
                    meta.getUpdateTime().length() > 0) {
                try {
                    updateTimeMap.put(meta.getName(), Long.parseLong(meta.getUpdateTime()));
                } catch (Throwable eee) {
                    eee.printStackTrace();
                }
            }
        }
        //根据更新时间倒序
        Collections.sort(tempList, new Comparator<Page>() {
            @Override
            public int compare(Page o1, Page o2) {
                if (o1 == null) {
                    return -(-1);
                } else if (o2 == null) {
                    return -(1);
                }
                long updateTime1 = 0L;
                long updateTime2 = 0L;
                if (o1.getTitle() != null) {
                    Long updateTime1Val = updateTimeMap.get(o1.getTitle());
                    updateTime1 = updateTime1Val != null ? updateTime1Val : 0L;
                }
                if (o2.getTitle() != null) {
                    Long updateTime2Val = updateTimeMap.get(o2.getTitle());
                    updateTime2 = updateTime2Val != null ? updateTime2Val : 0L;
                }
                if (updateTime1 < updateTime2) {
                    return -(-1);
                } else if (updateTime1 > updateTime2) {
                    return -(1);
                }
                return 0;
            }
        });
        this.pageList.clear();
        this.pageList.addAll(tempList);
        if (D) {
            for (int i = 0; i < this.pageList.size(); ++i) {
                Page o1 = this.pageList.get(i);
                Log.e(TAG, "<<< " + i + ", o1.getTitle() : " + o1.getTitle() + ", " + updateTimeMap.get(o1.getTitle()));
            }
        }
        bookGridAdapter.notifyDataSetChanged();
        bookListAdapter.notifyDataSetChanged();
        this.requestLoadPages();

        if (APP_OPEN != null && APP_OPEN.equals("NEW")) {
            //findViewById(R.id.buttonNewBook).performClick();
            if (firstNew && stateStarted_ == 0) {
                firstNew = false;
                onNewBook();
            }
        } else if (APP_FILE != null && !APP_FILE.equals("")) {
            for (int i = 0; i < files.size(); ++i) {
                FastFile f = files.get(i);
                if (f != null && f.getName() != null && f.getName().equals(APP_FILE)) {
                    int pageIdx = i;
                    Intent intent = new Intent(BookListActivity.this, BookActivity.class);
                    intent.setData(files.get(pageIdx).getUri());
                    intent.putExtra(BookActivity.EXTRA_DIRURLPATH, files.get(pageIdx).getFilePath());
                    startActivity(intent);
                    finish();
                    break;
                }
            }
        }
    }

    private void requestLoadPages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int idx = 0; idx < files.size(); ++idx) {
                    FastFile oneBookDir = files.get(idx);
                    //FIXME: thumbnailBitmap可能会抛异常导致null
                    try {
                        Bitmap thumbnailBitmap = getBookIO().loadThumbnail(oneBookDir);
                        if (BookIO.USE_META_TXT) {
                            try {
                                String pattern = null;
                                String metaTxt = getBookIO().loadMeta(oneBookDir);
                                JSONObject item = new JSONObject(metaTxt);
                                if (item != null) {
                                    pattern = item.optString("pattern");
                                }
                                if (pattern != null) {
                                    Bitmap emptyBmp = Bitmap.createBitmap(thumbnailBitmap.getWidth(),
                                            thumbnailBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                                    if (BookIO.USE_META_TXT) {
                                        emptyBmp.eraseColor(0x00000000);
                                    } else {
                                        emptyBmp.eraseColor(0x00000000);
                                    }
                                    CanvasBoox.initBackText(pattern, emptyBmp, BookIO.loadThumbnail_size);
                                    Canvas canvas = new Canvas(emptyBmp);
                                    Paint paint = new Paint();
                                    canvas.drawBitmap(thumbnailBitmap, 0, 0, paint);
                                    thumbnailBitmap = emptyBmp;
                                }
                            } catch (Throwable eee) {
                                eee.printStackTrace();
                            }
                        }
                        final Bitmap thumbnail = thumbnailBitmap != null ? thumbnailBitmap : blankBitmap;
                        final Bitmap bg = getBookIO().loadBgThumbnail(oneBookDir);
                        final int idx_ = idx;
                        final String name_ = oneBookDir.getName();
                        BookListActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                List<Page> result = new ArrayList<Page>();
                                for (int idx2 = 0; idx2 < pageList.size(); ++idx2) {
                                    Page page = pageList.get(idx2);
                                    if (page != null && page.getTitle() != null &&
                                        page.getTitle().equals(name_)) {
                                        result.add(page.copy(null, thumbnail, bg));
                                    } else {
                                        result.add(page);
                                    }
                                }
                                pageList.clear();
                                pageList.addAll(result);
                                bookGridAdapter.notifyDataSetChanged();
                                bookListAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (Throwable eee) {
                        eee.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void reloadBookList(Uri url, String urlPath) {
        this.updateFiles(this.listFiles(url, urlPath));
    }

    private List<FastFile> listFiles(Uri url, String urlPath) {
        FastFile rootDir = FastFile.fromTreeUri(this, url, urlPath);
        if (!rootDir.isDirectory()) {
            //throw new RuntimeException("Not directory");
            if (D) {
                Log.e(TAG, "Not directory or not exits or no permission : " + urlPath);
            }
            return new ArrayList<FastFile>();
        } else {
            List<FastFile> result = new ArrayList<FastFile>();
            for (FastFile obj : rootDir.listFiles()) {
                if (obj.isDirectory()) {
                    result.add(obj);
                }
            }
            Collections.sort(result, new Comparator<FastFile>() {
                @Override
                public int compare(FastFile item0, FastFile item1) {
                    String name0 = item0.getName();
                    String name1 = item1.getName();
                    if (name0 == null) name0 = "";
                    if (name1 == null) name1 = "";
                    return -name0.compareTo(name1);
                }
            });
            return result;
        }
    }

    private Pair getBookSizeDP() {
        return this._bookSizeDP;
    }

    private BookIO getBookIO() {
        return this._bookIO;
    }

    protected void onRestart() {
        super.onRestart();
        if (!this.files.isEmpty()) {
            this.reloadBookList(this._url, this._urlPath);
        }
    }

    private GridView gridview;
    private PageGridAdapter bookGridAdapter;
    private ListView listview;
    private PageListAdapter bookListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onActCreate();

        setContentView(R.layout.activity_booklist);
        ActionBar topAppBar = getSupportActionBar();
        if (topAppBar != null) {
            topAppBar.setTitle("Note List");
            topAppBar.setIcon(R.mipmap.ic_launcher);
            if (!Config.USE_ACTIONBAR) {
                topAppBar.hide();
            }
        }
        findViewById(R.id.buttonNewBook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNewBook();
            }
        });
        findViewById(R.id.buttonSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSettings();
            }
        });

        ImageButton imageButtonBack = (ImageButton) findViewById(R.id.imageButtonBack);
        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        gridview = (GridView) findViewById(R.id.bookgridview);
        gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridview.setBackgroundColor(Color.WHITE);
        bookGridAdapter = new PageGridAdapter(this, pageList);
        gridview.setAdapter(bookGridAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO: see below
//                //int bookType = bookInfoList.get(position).getBookType();
//                int pageIdx = position;
//                Intent intent = new Intent(BookListActivity.this, BookActivity.class);
//                intent.setData(files.get(pageIdx).getUri());
//                intent.putExtra(BookActivity.EXTRA_DIRURLPATH, files.get(pageIdx).getFilePath());
//                startActivity(intent);
            }
        });
        listview = (ListView) findViewById(R.id.booklistview);
        //listview.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listview.setBackgroundColor(Color.WHITE);
        bookListAdapter = new PageListAdapter(this, pageList) {
            @Override
            public void onItemDelete(int position_, final Page page) {
                super.onItemDelete(position_, page);
                showOkDialog(BookListActivity.this,
                        "Delete this note \"" + page.getTitle() + "\" ? ",
                        "OK",
                        new Runnable() {
                            @Override
                            public void run() {
                                //FIXME:
                                int position = -1;
                                Page p = pageList.get(position_);
                                for (int k = 0; k < files.size(); ++k) {
                                    FastFile f = files.get(k);
                                    if (f != null && f.getName().equals(p.getTitle())) {
                                        position = k;
                                        break;
                                    }
                                }
                                if (position >= 0) {
                                    FastFile f = files.get(position);
                                    if (f != null) {
                                        BookIO.deleteFolder(f.getFilePath());
                                    }
//                                  if (bookListAdapter != null) {
//                                      bookListAdapter.notifyDataSetChanged();
//                                  }
                                    getBookIO().removeSaveRecent(f.getName());
                                    reloadBookList(_url, _urlPath);
                                }
                            }
                        }, null);
            }
        };
        listview.setAdapter(bookListAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position_, long id) {
                //int bookType = bookInfoList.get(position).getBookType();
                //FIXME:
                int position = -1;
                Page p = pageList.get(position_);
                for (int k = 0; k < files.size(); ++k) {
                    FastFile f = files.get(k);
                    if (f != null && f.getName().equals(p.getTitle())) {
                        position = k;
                        break;
                    }
                }
                if (position >= 0) {
                    int pageIdx = position;
                    Intent intent = new Intent(BookListActivity.this, BookActivity.class);
                    intent.setData(files.get(pageIdx).getUri());
                    String filePath = files.get(pageIdx).getFilePath();
                    //FIXME:可能乱序了
                    if (false) {
                        Toast.makeText(BookListActivity.this,
                                "filePath == " + filePath,
                                Toast.LENGTH_SHORT).show();
                    }
                    intent.putExtra(BookActivity.EXTRA_DIRURLPATH, filePath);
                    startActivity(intent);
                }
            }
        });

        Intent intent = this.getIntent();
        if (intent != null) {
            APP_OPEN = intent.getStringExtra("APP_OPEN");
            APP_FILE = intent.getStringExtra("APP_FILE");
            if (D) {
                Log.e(TAG, "APP_OPEN:" + APP_OPEN);
                Log.e(TAG, "APP_FILE:" + APP_FILE);
            }
        }
        int stateStarted = 0;
        if (savedInstanceState != null) {
            stateStarted = savedInstanceState.getInt(STATE_STARTED, 0);
            stateStarted_ = stateStarted;
        }

        if (stateStarted == 0) {
            checkPermissioin();
        }

        if (BookIO.USE_CONTENT_RESOLVER) {
            boolean openGood = false;
            try {
                Uri it = this.getLastUri();
                if (it != null) {
                    this.openRootDir(it, null);
                    openGood = true;
                }
            } catch (Exception eee) {
                this.showMessage("Can't open dir. Please re-open.");
            }
            if (!openGood) {
                if (BookIO.USE_CONTENT_RESOLVER) {
//            this.getRootDirUrl.launch(null);
                }
            }
        } else {
            this.openRootDir(null, BookIO.rootPath);
        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_STARTED, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.booklist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newBook:
                onNewBook();
                break;

            case R.id.settings:
                onSettings();
                break;
        }
        return true;
    }
    private void onNewBook() {
        NewBookPopup(new OnNewBookListener() {
            @Override
            public void onNewBook(String bookName) {
                addNewBook(bookName, "");
            }
        }, new OnPopDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
    }
    private void onSettings() {
        if (BookIO.USE_CONTENT_RESOLVER) {
//            getRootDirUrl.launch(null);
        }
    }

    private void addNewBook(String newBookName, String backText) {
        FastFile rootDir = null;
        if (BookIO.USE_CONTENT_RESOLVER && this._url != null) {
            rootDir = FastFile.fromTreeUri(this, this._url, this._urlPath);
        } else if (!BookIO.USE_CONTENT_RESOLVER && this._urlPath != null) {
            rootDir = FastFile.fromTreeUri(this, this._url, this._urlPath);
        }
        if (rootDir != null) {
            try {
                rootDir.createDirectory(newBookName);
                this.openRootDir(this._url, this._urlPath);
                this.bookListAdapter.notifyDataSetChanged();
                this.listview.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < files.size(); ++i) {
                            FastFile f = files.get(i);
                            if (f != null && f.getName() != null && f.getName().equals(newBookName)) {
                                int pageIdx = i;
                                Intent intent = new Intent(BookListActivity.this, BookActivity.class);
                                intent.setData(files.get(pageIdx).getUri());
                                intent.putExtra(BookActivity.EXTRA_DIRURLPATH, files.get(pageIdx).getFilePath());
                                if (backText != null) {
                                    intent.putExtra(BookActivity.EXTRA_BACKTEXT, backText);
                                }
                                startActivity(intent);
                                break;
                            }
                        }
                    }
                }, 500);
            } catch (Exception var8) {
                this.showMessage("Can't create book directory (" + newBookName + ").");
            }
            return;
        }

        if (false) {
            throw new RuntimeException("Can't open dir"); //FIXME:
        } else {
            Toast.makeText(this, "Can't open dir", Toast.LENGTH_SHORT).show();
        }
    }

    //FIXME:
    private void getRootDirUrl(Uri uri) {
        if (BookIO.USE_CONTENT_RESOLVER) {
            if (uri != null) {
                this.getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                this.writeLastUri(uri);
                this.openRootDir(uri, null);
            }
        } else {
            throw new IllegalArgumentException("not implemented");
        }
    }


    EditText g_textState = null;
    private void NewBookPopup(OnNewBookListener onNewBook, OnPopDismissListener onDismiss) {
        if (false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (onDismiss != null) {
                        onDismiss.onDismiss();
                    }
                    String textState = "";
                    if (g_textState != null) {
                        textState = g_textState.getText().toString();
                    }
                    if (textState != "") {
                        if (onNewBook != null) {
                            onNewBook.onNewBook(textState);
                        }
                    }
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (onDismiss != null) {
                        onDismiss.onDismiss();
                    }
                    if (dialogInterface != null) {
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    if (onDismiss != null) {
                        onDismiss.onDismiss();
                    }
                    if (dialogInterface != null) {
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.setView(R.layout.dialog_loadpages);
            builder.setCancelable(true);
            final AlertDialog dialog = builder.create();
//        dialog.setContentView(R.layout.dialog_loadpages); //don't use this
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    g_textState = dialog.findViewById(R.id.textState);
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    g_textState = null;
                }
            });
            dialog.show();
        } else {
            NewNoteDialog dialog = new NewNoteDialog(this);
            dialog.show();
        }
    }

    public interface OnNewBookListener {
        void onNewBook(String bookName);
    }

    public interface OnPopDismissListener {
        void onDismiss();
    }

    private static final String STATE_STARTED = "STATE_STARTED";
    //原文链接：https://blog.csdn.net/zuo_er_lyf/article/details/82659426
    //https://www.dev2qa.com/android-read-write-external-storage-file-example/
    private final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 100;
    private void checkPermissioin(){
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If do not grant write external storage permission.
        if (writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED) {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(getApplicationContext(), "You grant write external storage permission. Please click original button again to continue.", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(getApplicationContext(), "You denied write external storage permission.", Toast.LENGTH_LONG).show();
            }
        }
    }


    public static AlertDialog showOkDialog(final Activity c,
                                           final String message,
                                           final String actionString,
                                           final Runnable action,
                                           Runnable onDismiss) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(actionString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                if (action != null) {
                    action.run();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                if (onDismiss != null) {
                    onDismiss.run();
                }
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (onDismiss != null) {
                    onDismiss.run();
                }
                //Keyboards.hideNavigation(c);
            }
        });
        create.show();
        return create;
    }

    public void onNewBook(String textState, String backText) {
        if (textState != null && textState.length() > 0) {
            addNewBook(textState, backText);
        }
    }

    public boolean checkText(String textState) {
        if (textState == null) {
            return false;
        }
        if (pageList != null) {
            for (Page page : pageList) {
                if (page != null && page.getTitle() != null &&
                    page.getTitle().equalsIgnoreCase(textState)) {
                    return false; //重复名称不允许
                }
            }
        }
        return true;
    }
}