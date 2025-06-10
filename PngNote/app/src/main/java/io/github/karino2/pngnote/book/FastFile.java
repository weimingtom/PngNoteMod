package io.github.karino2.pngnote.book;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.github.karino2.pngnote.Book;

public class FastFile {
    private final static boolean D = true;
    private final static String TAG = "FastFile";

    private Uri uri;
    private String name;
    private long lastModified;
    private String mimeType;
    private long size;
    private ContentResolver resolver;
    private String filePath;

    public FastFile(Uri uri, String filePath, String name, long lastModified, String mimeType,
                    long size, ContentResolver resolver) {
        this.uri = uri;
        this.filePath = filePath;
        if (this.filePath != null && !this.filePath.startsWith("/")) {
            if (D) {
                Log.e(TAG, "this.filePath not good");
            }
        }
        this.name = name;
        this.lastModified = lastModified;
        this.mimeType = mimeType;
        this.size = size;
        this.resolver = resolver;
    }

    public String getFilePath() {
        return this.filePath;
    }
    public Uri getUri() {
        return this.uri;
    }
    public String getName() {
        return this.name;
    }
    public long getLastModified() {
        return this.lastModified;
    }
    public String getMimeType() {
        return this.mimeType;
    }
    public long getSize() {
        return this.size;
    }
    public ContentResolver getResolver() {
        return this.resolver;
    }

    public boolean isDirectory() {
        if (BookIO.USE_CONTENT_RESOLVER) {
            return "vnd.android.document/directory".equals(this.mimeType);
        } else {
            return new File(this.filePath).isDirectory();
        }
    }

    public boolean isFile() {
        return !this.isDirectory() && !"".equals(this.mimeType);
    }

    public FastFile createFile(String fileMimeType, String fileDisplayName) {
        FastFile result = null;
        if (BookIO.USE_CONTENT_RESOLVER) {
            Uri it = null;
            try {
                it = DocumentsContract.createDocument(this.resolver, this.uri, fileMimeType, fileDisplayName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (it != null) {
                result = new FastFile(it, null, fileDisplayName,
                        new Date().getTime(), fileMimeType, 0L, this.resolver);
            }
        } else {
            File file = null;
            boolean isOK = false;
            try {
                file = new File(this.filePath, fileDisplayName);
                if (file.exists() && file.canWrite() && !file.isDirectory()) {
                    isOK = true;
                } else {
                    isOK = file.createNewFile();
                }
            } catch (Throwable eee) {
                eee.printStackTrace();
            }
            if (isOK) {
                result = new FastFile(null, file.getAbsolutePath(), fileDisplayName,
                        new Date().getTime(), fileMimeType, 0L, this.resolver);
            } else {
                System.out.println("create failed!"); //FIXME:
            }
        }
        return result;
    }

    public void removeFile(BookIO bookIO) {
        if (BookIO.USE_CONTENT_RESOLVER) {
            //skip
        } else {
            File file = null;
            try {
                file = new File(this.filePath);
                if (file.exists() && file.canWrite() && !file.isDirectory()) {
                    boolean result = file.delete();
                } else {
                    if (D) {
                        Log.e(TAG, "<<<< removeFile failed! " + this.filePath);
                    }
                }
                //FIXME:还需要遍历所有文件，移动其他文件到前面
                if (bookIO != null) {
                    Book book = bookIO.loadBookParentNoCreate(this);
                }
            } catch (Throwable eee) {
                eee.printStackTrace();
            }
        }
    }

    public static void copyFile(String sourcePath, String destinationPath) {
        if (D) {
            Log.e(TAG, "copyFile " + sourcePath + " -> " + destinationPath);
        }
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(sourcePath);
            outputStream = new FileOutputStream(destinationPath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException eee) {
            eee.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<FastFile> listFiles() {
        return listFiles(this.resolver, this.uri, this.filePath);
    }

    public List<FastFile> listFilesParent() {
        return listFilesParent(this.resolver, this.uri, this.filePath);
    }

    public FastFile findFile(String targetDisplayName) {
        FastFile result = null;
        for (FastFile it : this.listFiles()) {
            if (it.name != null && targetDisplayName != null &&
                    !it.name.equals(targetDisplayName)) { //FIXME:???
                //continue;
            } else {
                result = it; //FIXME:???
                break;
            }
        }
        return result;
    }

    public FastFile createDirectory(String displayName) {
        if (BookIO.USE_CONTENT_RESOLVER) {
            Uri uri = null;
            try {
                uri = DocumentsContract.createDocument(this.resolver, this.uri,
                        "vnd.android.document/directory", displayName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return uri != null ? fromDocUri(this.resolver, uri, null) : null;
        } else {
            File folder = new File(this.filePath, displayName);
            if (folder.exists() && folder.isDirectory()) {
                return fromDocUri(this.resolver, null, folder.getAbsolutePath());
            }
            try {
                boolean result = folder.mkdirs();
            } catch (Throwable eee) {
                eee.printStackTrace();
            }
            if (folder.exists() && folder.isDirectory()) {
                return fromDocUri(this.resolver, null, folder.getAbsolutePath());
            } else {
                return null;
            }
        }
    }

    public boolean isEmpty() {
        if (!this.isFile()) {
            return false;
        } else {
            return 0L == this.size;
        }
    }

    public Uri component1() {
        return this.uri;
    }
    public String component2() {
        return this.name;
    }
    public long component3() {
        return this.lastModified;
    }
    public String component4() {
        return this.mimeType;
    }
    public long component5() {
        return this.size;
    }
    public ContentResolver component6() {
        return this.resolver;
    }

    public FastFile copy(Uri uri, String filePath, String name, long lastModified, String mimeType,
                               long size, ContentResolver resolver) {
        return new FastFile(uri, filePath, name, lastModified, mimeType, size, resolver);
    }

    //FIXME:
    public static FastFile copy(FastFile file, Uri uri_, String filePath_, String name_, long lastModified_,
                                String mimeType_, long size_, ContentResolver resolver_,
                                int mask, Object obj) {
        if ((mask & 1) != 0) {
            uri_ = file.uri;
            filePath_ = file.filePath;
        }
        if ((mask & 2) != 0) {
            name_ = file.name;
        }
        if ((mask & 4) != 0) {
            lastModified_ = file.lastModified;
        }
        if ((mask & 8) != 0) {
            mimeType_ = file.mimeType;
        }
        if ((mask & 16) != 0) {
            size_ = file.size;
        }
        if ((mask & 32) != 0) {
            resolver_ = file.resolver;
        }
        return file.copy(uri_, filePath_, name_, lastModified_, mimeType_, size_, resolver_);
    }

    @Override
    public String toString() {
        return "FastFile(uri=" + this.uri + ", name=" + this.name + ", lastModified=" + this.lastModified + ", mimeType=" + this.mimeType + ", size=" + this.size + ", resolver=" + this.resolver + ')';
    }

    @Override
    public int hashCode() {
        int result = (uri != null ? this.uri.hashCode() : 0);
        result = result * 31 + (this.filePath != null ? this.filePath.hashCode() : 0);
        result = result * 31 + (this.name != null ? this.name.hashCode() : 0);
        result = result * 31 + Long.hashCode(this.lastModified);
        result = result * 31 + this.mimeType.hashCode();
        result = result * 31 + Long.hashCode(this.size);
        result = result * 31 + (this.resolver != null ? this.resolver.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof FastFile)) {
            return false;
        } else {
            FastFile o2 = (FastFile)other;
            if (this.filePath != null && o2.filePath != null &&
                    !this.filePath.equals(o2.filePath)) {
                return false;
            } else if (this.uri != null && o2.uri != null &&
                    !this.uri.equals(o2.uri)) {
                return false;
            } else if (this.name != null && o2.name != null &&
                    !this.name.equals(o2.name)) {
                return false;
            } else if (this.lastModified != o2.lastModified) {
                return false;
            } else if (this.mimeType != null && o2.mimeType != null &&
                    !this.mimeType.equals(o2.mimeType)) {
                return false;
            } else if (this.size != o2.size) {
                return false;
            } else {
                return this.resolver != null && o2.resolver != null &&
                        this.resolver.equals(o2.resolver);
            }
        }
    }

    private static long getLong(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.isNull(index) ? 0L : cur.getLong(index);
    }

    private static String getString(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        if (cur.isNull(index)) {
            return "";
        } else {
            return cur.getString(index); //check null
        }
    }

    private static FastFile fromCursor(Cursor cur, Uri uri, ContentResolver resolver, String filePath) {
        if (BookIO.USE_CONTENT_RESOLVER) {
            String disp = getString(cur, "_display_name");
            long lm = getLong(cur, "last_modified");
            String mimeType = getString(cur, "mime_type");
            long size = getLong(cur, "_size");
            FastFile file = new FastFile(uri, null, disp, lm, mimeType, size, resolver);
            return file;
        } else {
            File file_ = new File(filePath);
            String disp = file_.getName();
            long lm = file_.lastModified();
            String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            long size = file_.length();
            return new FastFile(uri, filePath, disp, lm, mimeType, size, resolver);
        }
    }

    public List<FastFile> listFiles(ContentResolver resolver, Uri parent, String parentFilePath) {
        List<FastFile> result = new ArrayList<>();
        if (BookIO.USE_CONTENT_RESOLVER) {
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    parent, DocumentsContract.getDocumentId(parent));
            Cursor cur = resolver.query(childrenUri, null,
                    null, null, null,
                    null);
            while (cur.moveToNext()) {
                String docId = cur.getString(0);
                Uri uri = DocumentsContract.buildDocumentUriUsingTree(parent, docId);
                result.add(fromCursor(cur, uri, resolver, null));
            }
        } else {
            File parentFile = new File(parentFilePath);
            File[] files = parentFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    result.add(fromCursor(null, uri, resolver, file.getAbsolutePath()));
                }
            } else {
                if (D) {
                    Log.e(TAG, "parentFilePath listFiles null : " + parentFilePath);
                }
            }
        }
        return result;
    }

    public List<FastFile> listFilesParent(ContentResolver resolver, Uri parent, String parentFilePath) {
        List<FastFile> result = new ArrayList<>();
        if (BookIO.USE_CONTENT_RESOLVER) {

        } else {
            File file_ = new File(parentFilePath);
            File parentFile = file_.getParentFile();
            if (parentFile != null) {
                File[] files = parentFile.listFiles();
                if (files != null) {
                    for (File file : files) {
                        result.add(fromCursor(null, uri, resolver, file.getAbsolutePath()));
                    }
                } else {
                    if (D) {
                        Log.e(TAG, "parentFilePath listFilesParent null : " + parentFilePath);
                    }
                }
            }
        }
        return result;
    }

    public static FastFile fromTreeUri(Context context, Uri treeUri, String treePath) {
        if (BookIO.USE_CONTENT_RESOLVER) {
            String docId = DocumentsContract.isDocumentUri(context, treeUri) ? DocumentsContract.getDocumentId(treeUri) : DocumentsContract.getTreeDocumentId(treeUri);
            if (docId == null) {
                throw new IllegalArgumentException("Could not get documentUri from " + treeUri);
            } else {
                Uri treeDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
                if (treeDocUri == null) {
                    throw new NullPointerException("Failed to build documentUri from " + treeUri);
                } else {
                    ContentResolver resolver = context.getContentResolver(); //FIXME：check null
                    FastFile result = fromDocUri(resolver, treeDocUri, null);
                    if (result != null) {
                        return result;
                    } else {
                        throw new IllegalArgumentException("Could not query from " + treeUri);
                    }
                }
            }
        } else {
            return fromDocUri(null, null, treePath);
        }
    }

    //FIXME:???
    public static FastFile fromDocUri(ContentResolver resolver, Uri treeDocUri, String filePath) {
        if (BookIO.USE_CONTENT_RESOLVER) {
            Cursor cur = null;
            try {
                cur = resolver.query(treeDocUri, null,
                        null, null, null,
                        null);
                if (cur != null && cur.moveToFirst()) {
                    return fromCursor(cur, treeDocUri, resolver, filePath);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (cur != null) {
                    cur.close();
                }
            }
        } else {
            return fromCursor(null, treeDocUri, resolver, filePath);
        }
        return null;
    }
}
