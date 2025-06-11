package io.github.karino2.pngnote.book;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.karino2.pngnote.Book;

public class BookIO {
    private final static boolean D = true;
    private final static String TAG = "BookIO";

    public final static boolean USE_CONTENT_RESOLVER = false;
    public static String rootPath = new File(Environment.getExternalStorageDirectory(),
            "pngnote"
        ).toString(); //FIXME:

    private ContentResolver resolver;
    private Pattern pageNamePat;

    public BookIO(ContentResolver resolver) {
        if (USE_CONTENT_RESOLVER) {
            this.resolver = resolver;
        }
        this.pageNamePat = Pattern.compile("([0-9][0-9][0-9][0-9])\\.png");
    }

    private Bitmap loadBitmap(FastFile file) {
        Bitmap result = null;
        if (USE_CONTENT_RESOLVER) {
            ParcelFileDescriptor it = null;
            try {
                it = this.resolver.openFileDescriptor(file.getUri(), "r");
                result = BitmapFactory.decodeFileDescriptor(it.getFileDescriptor());
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                try {
                    if (it != null) {
                        it.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            result = BitmapFactory.decodeFile(file.getFilePath());
        }
        return result; //FIXME: check null
    }

    private Bitmap loadThumbnail(FastFile bookDir, String displayName) {
        FastFile it = bookDir.findFile(displayName);
        Bitmap result = null;
        if (it != null) {
            result = this.loadBitmapThumbnail(it, 3);
        }
        return result;
    }

    public Bitmap loadThumbnail(FastFile bookDir) {
        return this.loadThumbnail(bookDir, "0000.png");
    }

    public Bitmap loadBgThumbnail(FastFile bookDir) {
        return this.loadThumbnail(bookDir, "background.png");
    }

    public Bitmap loadPageThumbnail(FastFile file) {
        return this.loadBitmapThumbnail(file, 4);
    }

    public Bitmap loadBgForGrid(FastFile bookDir) {
        FastFile it = bookDir.findFile("background.png");
        Bitmap result = null;
        if (it != null) {
            result = this.loadBitmapThumbnail(it, 4);
        }
        return result;
    }

    private Bitmap loadBitmapThumbnail(FastFile file, int sampleSize) {
        Bitmap result = null;
        if (BookIO.USE_CONTENT_RESOLVER) {
            ParcelFileDescriptor it = null;
            try {
                it = this.resolver.openFileDescriptor(file.getUri(), "r");
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inSampleSize = sampleSize;
                result = BitmapFactory.decodeFileDescriptor(it.getFileDescriptor(), null, option);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                try {
                    if (it != null) {
                        it.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = sampleSize;
            result = BitmapFactory.decodeFile(file.getFilePath(), option);
        }
        return result;
    }

    private boolean isEmpty(FastFile file) {
        return file.isEmpty();
    }

    public boolean isPageEmpty(BookPage page) {
        return this.isEmpty(page.getFile());
    }

    public Bitmap loadBitmap(BookPage page) {
        return this.loadBitmap(page.getFile());
    }

    public Bitmap loadBitmapOrNull(BookPage page) {
        return this.isPageEmpty(page) ? null : this.loadBitmap(page);
    }

    public Bitmap loadBgOrNull(Book book) {
        FastFile it = book.getBgImage();
        Bitmap result = null;
        if (it != null) {
            result = this.loadBitmap(it);
        }
        return result;
    }

    public void saveBitmap(BookPage page, Bitmap bitmap) {
        if (USE_CONTENT_RESOLVER) {
            OutputStream it = null;
            try {
                //FIXME:会创建新的文件导致最新图无法读取
                it = this.resolver.openOutputStream(page.getFile().getUri(), "w"); //"wt"
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                try {
                    if (it != null) {
                        it.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            OutputStream it = null;
            try {
                //FIXME:会创建新的文件导致最新图无法读取
                if (D) {
                    Log.e(TAG, "saving " + page.getFile().getFilePath());
                }
                it = new FileOutputStream(page.getFile().getFilePath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                try {
                    if (it != null) {
                        it.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            List<SimpleFileMeta> datas = loadRecent();
            if (datas == null) {
                datas = new ArrayList<SimpleFileMeta>();
            }
            File file = new File(page.getFile().getFilePath());
            File folder = file.getParentFile();
            String name = folder.getName();
            List<SimpleFileMeta> datas2 = new ArrayList<>();
            List<SimpleFileMeta> datas3 = new ArrayList<>();
            SimpleFileMeta itemFound = null;
            for (int i = 0; i < datas.size(); ++i) {
                SimpleFileMeta item = datas.get(i);
                if (name != null && name.length() > 0 && item != null && item.getName() != null) {
                    if (name.equals(item.getName()) && itemFound == null) {
                        itemFound = item;
                        datas3.add(item);
                    } else {
                        datas2.add(item);
                    }
                }
            }
            if (itemFound == null) {
                itemFound = new SimpleFileMeta();
                datas3.add(itemFound); //最新的记录移动到最前面
                itemFound.setCreateTime("" + new Date().getTime());
            }
            datas3.addAll(datas2);

            itemFound.setPath(name); //FIXME:可能不是读目录名称
            itemFound.setName(name);
            itemFound.setUpdateTime("" + new Date().getTime());

            //https://blog.csdn.net/ocean__yang/article/details/113740043
            if (bitmap != null) { //FIXME:可能不是这个bitmap，而是读第一个0001的png
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] imageByte = outputStream.toByteArray();
                itemFound.setPreview(Base64.encodeToString(imageByte, Base64.DEFAULT));
            }
            JSONArray arr = new JSONArray();
            for (SimpleFileMeta meta : datas3) {
                JSONObject obj = new JSONObject();
                obj.put("preview", meta.getPreview());
                obj.put("name", meta.getName());
                obj.put("path", meta.getPath());
                obj.put("createTime", meta.getCreateTime());
                obj.put("updateTime", meta.getUpdateTime());
                arr.put(obj);
            }
            saveRecent(arr.toString());
        } catch (Throwable eee) {
            eee.printStackTrace();
        }
    }

    public Book loadBook(FastFile bookDir) {
//        val pageMap = bookDir.listFiles()
//                .filter {file ->
//                pageNamePat.matches(file.name)
//        }.map {file ->
//                val res = pageNamePat.find(file.name)!!
//                val pageIdx = res.groupValues[1].toInt()
//            Pair(pageIdx, file)
//        }.toMap()

        Map<Integer, FastFile> pageMap = new HashMap<Integer, FastFile>();
        List<FastFile> files = bookDir.listFiles();
        if (files != null) {
            for (FastFile file : files) {
                try {
                    Matcher res = pageNamePat.matcher(file.getName());
                    if (res.matches()) {
                        //FIXME:java.lang.IllegalStateException: No successful match so far。
                        int pageIdx = Integer.parseInt(res.group(1));
                        pageMap.put(pageIdx, file);
                    }
                } catch (Throwable eee) {
                    eee.printStackTrace();
                }
            }
        }

        //val lastPageIdx = if(pageMap.isEmpty()) 0 else pageMap.maxOf { it.key }
        //FIXME：??
        int lastPageIdx = 0;
        if (!pageMap.isEmpty()) {
            int tempKey = 0;
            for (Integer curKey : pageMap.keySet()) {
                if (curKey != null && curKey > tempKey) {
                    tempKey = curKey;
                }
            }
            lastPageIdx = tempKey;
        }

        List<Integer> pagesTemp = new ArrayList<Integer>();
        for (int i = 0; i <= lastPageIdx; ++i) {
            pagesTemp.add(i);
        }
        List<FastFile> pages = new ArrayList<FastFile>(pagesTemp.size());
        for (Integer it : pagesTemp) {
            FastFile itemOld = pageMap.get(it);
            FastFile item = itemOld != null ? itemOld : BookPage.createEmptyFile(bookDir, it);
            pages.add(item);
        }
        FastFile bgFile = bookDir.findFile("background.png");
        return new Book(bookDir, pages, bgFile);
    }

    //FIXME:不插入空白页而是移动到新文件，可能会不正确
    public Book loadBookParentNoCreate(FastFile bookDir) {
        Map<Integer, FastFile> pageMap = new HashMap<Integer, FastFile>();
        List<FastFile> files = bookDir.listFilesParent();
        if (files != null) {
            for (FastFile file : files) {
                try {
                    Matcher res = pageNamePat.matcher(file.getName());
                    if (res.matches()) {
                        //FIXME:java.lang.IllegalStateException: No successful match so far。
                        int pageIdx = Integer.parseInt(res.group(1));
                        pageMap.put(pageIdx, file);
                    }
                } catch (Throwable eee) {
                    eee.printStackTrace();
                }
            }
        }

        //val lastPageIdx = if(pageMap.isEmpty()) 0 else pageMap.maxOf { it.key }
        //FIXME：??
        int lastPageIdx = 0;
        if (!pageMap.isEmpty()) {
            int tempKey = 0;
            for (Integer curKey : pageMap.keySet()) {
                if (curKey != null && curKey > tempKey) {
                    tempKey = curKey;
                }
            }
            lastPageIdx = tempKey;
        }

        List<Integer> pagesTemp = new ArrayList<Integer>();
        for (int i = 0; i <= lastPageIdx; ++i) {
            pagesTemp.add(i);
        }
        List<FastFile> pages = new ArrayList<FastFile>(pagesTemp.size());
        for (Integer it : pagesTemp) {
            FastFile itemOld = pageMap.get(it);
            if (itemOld == null) {
                continue;
            }
            pages.add(itemOld);
        }
        for (int i = 0; i < pages.size(); ++i) {
            FastFile item = pages.get(i);
            if (item != null && item.getName() != null) {
                try {
                    Matcher res = pageNamePat.matcher(item.getName());
                    if (res.matches()) {
                        //FIXME:java.lang.IllegalStateException: No successful match so far。
                        int pageIdx = Integer.parseInt(res.group(1));
                        if (pageIdx != i) {
                            String newName = BookPage.newPageName(i);
                            String oldFilePath = item.getFilePath();
                            String newFilePath = new File(
                                    new File(oldFilePath).getParent(), newName)
                                    .getAbsolutePath();
                            FastFile.copyFile(oldFilePath, newFilePath);
                            boolean r = new File(oldFilePath).delete();
                            //填充缺失的页面，然后删除
                        }
                   }
                } catch (Throwable eee) {
                    eee.printStackTrace();
                }
            }
        }
        FastFile bgFile = bookDir.findFile("background.png");
        return new Book(bookDir, pages, bgFile);
    }

















    private static final String KEY_RECENT_FILES = "recentFiles";
    //private final static String APPNAME = "pngnote";
    public void saveRecent(String value) {
        String key = "flutter." + KEY_RECENT_FILES;
        try {
            //String rootPath = new File(Environment.getExternalStorageDirectory(), APPNAME).toString();
            boolean kkk = new File(rootPath).mkdirs();
            if (true) {
                FileWriter fw = new FileWriter(new File(rootPath, key + ".txt"), false);
                fw.write(value);
                fw.flush();
                fw.close();
            }
        } catch (Throwable eee) {
            eee.printStackTrace();
        }
    }

    public void removeOrClearRecent() {
        String key = "flutter." + KEY_RECENT_FILES;
        try {
            //String rootPath = new File(Environment.getExternalStorageDirectory(), APPNAME).toString();
            boolean kkk = new File(rootPath).mkdirs();
            if (true) {
                FileWriter fw = new FileWriter(new File(rootPath, key + ".txt"), false);
                fw.write("");
                fw.flush();
                fw.close();
            }
        } catch (Throwable eee) {
            eee.printStackTrace();
        }
    }

    public List<SimpleFileMeta> loadRecent() {
        String recentFiles = "";
        if (true) {
            try {
                //String rootPath = new File(Environment.getExternalStorageDirectory(), APPNAME).toString();
                boolean kkk = new File(rootPath).mkdirs();
                if (new File(rootPath, "flutter." + KEY_RECENT_FILES + ".txt").exists()) {
                    InputStream fis = new FileInputStream(new File(rootPath, "flutter." + KEY_RECENT_FILES + ".txt"));
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    StringBuffer recentFilesBuffer = new StringBuffer();
                    while (true) {
                        String line = reader.readLine();
                        if (line != null) {
                            recentFilesBuffer.append(line);
                            recentFilesBuffer.append("\n");
                        } else {
                            break;
                        }
                    }
                    recentFiles = recentFilesBuffer.toString();
                    reader.close();
                    isr.close();
                    fis.close();
                }
            } catch (Throwable eee) {
                eee.printStackTrace();
            }
        }
        Log.e(TAG, "recentFiles: " + recentFiles);

        List<SimpleFileMeta> recentNoteList2 = new ArrayList<SimpleFileMeta>();
        try {
            JSONArray jsonArray = new JSONArray(recentFiles);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                if (item != null) {
                    String preview = item.optString("preview");
                    String name = item.optString("name");
                    String path = item.optString("path");
                    String createTime = item.optString("createTime");
                    String updateTime = item.optString("updateTime");

                    SimpleFileMeta meta = new SimpleFileMeta();
                    meta.setPreview(preview);
                    meta.setName(name);
                    meta.setPath(path);
                    meta.setCreateTime(createTime);
                    meta.setUpdateTime(updateTime);
                    recentNoteList2.add(meta);
                }
            }
        } catch (Throwable eee) {
            eee.printStackTrace();
        }
        return recentNoteList2;
    }

    public static boolean deleteFolder(String sPath) {
        try {
            boolean flag = false;
            File file = new File(sPath);
            // 判断目录或文件是否存在
            if (!file.exists()) {  // 不存在返回 false
                return flag;
            } else {
                // 判断是否为文件
                if (file.isFile()) {  // 为文件时调用删除文件方法
                    return deleteFile(sPath);
                } else {  // 为目录时调用删除目录方法
                    return deleteDirectory(sPath);
                }
            }
        } catch (Throwable eee) {
            eee.printStackTrace();
        }
        return true;
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     * @param   sPath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        //FIXME:不删除目录本身，只删除文件
	    if (dirFile.delete()) {
	        return true;
	    } else {
	        return false;
	    }
        /*
        return true;
        */
    }

    /**
     * 删除单个文件
     * @param   sPath    被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }
}
