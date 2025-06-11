package io.github.karino2.pngnote.ui;

import android.graphics.Bitmap;

public class Page {
    private String title;
    private Bitmap thumbnail;
    private Bitmap bgThumbnail;
    private boolean isEmpty;

    public Page(String title, Bitmap thumbnail, Bitmap bgThumbnail) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.bgThumbnail = bgThumbnail;
        this.isEmpty = this.title == null || "".equals(this.title);
    }

    public String getTitle() {
        return this.title;
    }
    public Bitmap getThumbnail() {
        return this.thumbnail;
    }
    public Bitmap getBgThumbnail() {
        return this.bgThumbnail;
    }
    public boolean isEmpty() {
        return this.isEmpty;
    }
    public String component1() {
        return this.title;
    }
    public Bitmap component2() {
        return this.thumbnail;
    }
    public Bitmap component3() {
        return this.bgThumbnail;
    }

    public Page copy(String title, Bitmap thumbnail, Bitmap bgThumbnail) {
        return new Page(title != null ? title : this.title,
                thumbnail != null ? thumbnail : this.thumbnail,
                bgThumbnail != null ? bgThumbnail : this.bgThumbnail);
    }

    @Override
    public String toString() {
        return "Page(title=" + this.title +
                ", thumbnail=" + this.thumbnail +
                ", bgThumbnail=" + this.bgThumbnail +
                ')';
    }

    @Override
    public int hashCode() {
        int result = this.title.hashCode();
        result = result * 31 + this.thumbnail.hashCode();
        result = result * 31 + (this.bgThumbnail == null ? 0 : this.bgThumbnail.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof Page)) {
            return false;
        } else {
            Page o2 = (Page)other;
            if (this.title != null && o2.title != null &&
                    !this.title.equals(o2.title)) {
                return false;
            } else if (this.thumbnail != null && o2.thumbnail != null &&
                    !this.thumbnail.equals(o2.thumbnail)) {
                return false;
            } else {
                return this.bgThumbnail != null && o2.bgThumbnail != null &&
                        !this.bgThumbnail.equals(o2.bgThumbnail);
            }
        }
    }
}
