package io.github.karino2.pngnote;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import io.github.karino2.pngnote.book.BookIO;
import io.github.karino2.pngnote.book.BookPage;
import io.github.karino2.pngnote.book.FastFile;
import io.github.karino2.pngnote.ui.Page;

public class Book {
    private FastFile bookDir;
    private List<FastFile> pages;
    private FastFile bgImage;

    public Book(FastFile bookDir, List<FastFile> pages, FastFile bgImage) {
        this.bookDir = bookDir;
        this.pages = pages;
        this.bgImage = bgImage;
    }

    public FastFile getBookDir() {
        return this.bookDir;
    }
    public List<FastFile> getPages() {
        return this.pages;
    }
    public FastFile getBgImage() {
        return this.bgImage;
    }

    public Book addPage() {
        FastFile pngFile = BookPage.createEmptyFile(this.bookDir, this.pages.size());
        List<FastFile> result = new ArrayList<FastFile>(this.pages);
        result.add(pngFile);
        return new Book(this.bookDir, result, this.bgImage);
    }

    //FIXME:???
    public void removePage(FastFile page, BookIO bookIO) {
        if (page != null) {
            this.pages.remove(page);
            page.removeFile(bookIO);
        }
    }

    public BookPage getPage(int idx) {
        return new BookPage(this.pages.get(idx), idx);
    }

    //FIXME:????
    public Book assignNonEmpty(int pageIdx) {
        if (!this.getPage(pageIdx).getFile().isEmpty()) {
            return this;
        }

        List<FastFile> it = new ArrayList<FastFile>();
        for (int idx = 0; idx < this.pages.size(); ++idx) {
            FastFile file = this.pages.get(idx);
            if (idx != pageIdx) {
                it.add(file);
            } else {
                it.add(FastFile.copy(file, null, null, null, 0L,
                        null, 1000L, null, 47, null));
            }
        }
        return new Book(this.bookDir, it, this.bgImage);
    }

    public String getName() {
        return this.bookDir.getName();
    }
}
