package io.github.karino2.pngnote.booklist;

import android.graphics.Bitmap;

public class Thumbnail {
    private Bitmap page;
    private Bitmap bg;

    public Thumbnail(Bitmap page, Bitmap bg) {
        this.page = page;
        this.bg = bg;
    }

    public Bitmap getPage() {
        return this.page;
    }
    public Bitmap getBg() {
        return this.bg;
    }
    public Bitmap component1() {
        return this.page;
    }
    public Bitmap component2() {
        return this.bg;
    }

    //FIXME:
    public Thumbnail copy(Bitmap page, Bitmap bg) {
        return new Thumbnail(page, bg);
    }

    @Override
    public String toString() {
        return "Thumbnail(page=" + this.page + ", bg=" + this.bg + ')';
    }

    @Override
    public int hashCode() {
        int result = this.page.hashCode();
        result = result * 31 + (this.bg == null ? 0 : this.bg.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof Thumbnail)) {
            return false;
        } else {
            Thumbnail thumbnail = (Thumbnail)other;
            if (this.page != null && thumbnail.page != null &&
                    !this.page.equals(thumbnail.page)) {
                return false;
            } else {
                return this.bg != null && thumbnail.bg != null &&
                        this.bg.equals(thumbnail.bg);
            }
        }
    }
}
