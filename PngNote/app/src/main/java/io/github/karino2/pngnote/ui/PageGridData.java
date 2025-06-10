package io.github.karino2.pngnote.ui;

import android.graphics.Bitmap;

import java.util.List;

public class PageGridData {
    private List<Page> pages;
    private int colNum;
    private static final Bitmap blankBitmap;

    public PageGridData(List<Page> pages) {
        this.pages = pages;
        this.colNum = 4;
    }

    public int getColNum() {
        return this.colNum;
    }

    public int getRowNum() {
        return (this.pages.size() + 3) / this.colNum;
    }

    private Page makeBlankPage() {
        return new Page("", blankBitmap, null);
    }

    public Page getPage(int row, int col) {
        int index = this.toIndex(row, col);
        return index < this.pages.size() ? this.pages.get(index) : this.makeBlankPage();
    }

    public int toIndex(int row, int col) {
        return row * this.colNum + col;
    }

    static {
        blankBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        if (blankBitmap != null) {
            blankBitmap.eraseColor(0xFFCCCCCC);
        }
    }

    public static Bitmap getBlankBitmap() {
        return blankBitmap;
    }
}
