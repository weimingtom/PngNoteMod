package io.github.karino2.pngnote.book;

import android.util.Log;

public class BookPage {
    private final static boolean D = true;
    private final static String TAG = "BookPage";


    private FastFile file;
    private int idx;

    public BookPage(FastFile file, int idx) {
        this.file = file;
        this.idx = idx;
    }

    public FastFile getFile() {
        return this.file;
    }
    public int getIdx() {
        return this.idx;
    }
    public FastFile component1() {
        return this.file;
    }
    public int component2() {
        return this.idx;
    }

    public BookPage copy(FastFile file, int idx) {
        return new BookPage(file, idx);
    }

    @Override
    public String toString() {
        return "BookPage(file=" + this.file + ", idx=" + this.idx + ')';
    }

    @Override
    public int hashCode() {
        int result = this.file.hashCode();
        result = result * 31 + Integer.hashCode(this.idx);
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof BookPage)) {
            return false;
        } else {
            BookPage o2 = (BookPage)other;
            if (this.file != null && o2.file != null &&
                    !this.file.equals(o2.file)) {
                return false;
            } else {
                return this.idx == o2.idx;
            }
        }
    }

    public static String newPageName(int pageIdx) {
        return String.format("%04d.png", pageIdx);
    }

    public static FastFile createEmptyFile(FastFile bookDir, int idx) {
        String fileName = newPageName(idx);
        FastFile result = bookDir.createFile("image/png", fileName);
        if (result != null) {
            return result;
        } else {
            //throw new RuntimeException("Can't create file " + fileName); //FIXME:???
            if (D) {
                Log.e(TAG, "Can't create file " + fileName);
            }
            return null;
        }
    }
}
