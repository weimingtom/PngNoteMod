package io.github.karino2.pngnote.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;

import io.github.karino2.pngnote.book.BookIO;

import java.util.ArrayList;
import java.util.List;

public class UndoList {
    private final static boolean D = true;
    private final static String TAG = "UndoList";

    private List<UndoCommand> commandList = new ArrayList<UndoCommand>();
    private int currentPos = -1;
    private static final int COMMAND_MAX_SIZE = 0x100000 * 1000; //最大位图内存占用数是256K

    public void pushUndoCommand(int x, int y, Bitmap undo, Bitmap redo) {
        this.discardLaterCommand();
        this.commandList.add(new UndoCommand(x, y, undo, redo));
        this.currentPos++;
        this.discardUntilSizeFit();
    }

    private void discardLaterCommand() {
        int i = this.commandList.size() - 1;
        int var2 = this.currentPos + 1;
        if (var2 <= i) {
            while (true) {
                this.commandList.remove(i);
                if (i == var2) {
                    break;
                }
                --i;
            }
        }
    }

    private void discardUntilSizeFit() {
        while(this.currentPos > 0 && this.getCommandsSize() > COMMAND_MAX_SIZE) {
            this.commandList.remove(0);
            this.currentPos--;
        }
    }

    private int getCommandsSize() {
        int res = 0;
        for (UndoCommand cmd : this.commandList) {
            res += cmd.getSize();
        }
        return res; //计算总的位图占用内存量（像素*4字节）
    }

    public boolean getCanUndo() {
        if (D) {
            Log.e(TAG, "<< currentPos == " + this.currentPos +
                    ", getCanUndo == " + (this.currentPos >= 0));
        }
        return this.currentPos >= 0;
    }

    public boolean getCanRedo() {
        return this.currentPos < this.commandList.size() - 1;
    }

    public void redo(Canvas target) {
        if (this.getCanRedo()) {
            this.currentPos++;
            this.commandList.get(this.currentPos).redo(target);
        }
    }

    public void undo(Canvas target) {
        if (this.getCanUndo()) {
            this.commandList.get(this.currentPos).undo(target);
            this.currentPos--;
        }
    }

    public void clear() {
        this.commandList.clear();
        this.currentPos = -1;
    }

    public static class UndoCommand {
        private int x;
        private int y;
        private Bitmap undoBmp;
        private Bitmap redoBmp;

        public UndoCommand(int x, int y, Bitmap undoBmp, Bitmap redoBmp) {
            this.x = x;
            this.y = y;
            this.undoBmp = undoBmp;
            this.redoBmp = redoBmp;
        }

        public void undo(Canvas target) {
            if (BookIO.USE_META_TXT) {
                //透明色背景，所以需要清除
                Paint p = new Paint();
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.BLACK);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                Rect rect = new Rect(
                        this.x,
                        this.y,
                        this.x + this.undoBmp.getWidth(),
                        this.y + this.undoBmp.getHeight());
                target.drawRect(rect, p);
            }
            target.drawBitmap(this.undoBmp, this.x, this.y, null);
        }

        public void redo(Canvas target) {
            if (BookIO.USE_META_TXT) {
                //透明色背景，所以需要清除
                Paint p = new Paint();
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.BLACK);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                Rect rect = new Rect(
                        this.x,
                        this.y,
                        this.x + this.undoBmp.getWidth(),
                        this.y + this.undoBmp.getHeight());
                target.drawRect(rect, p);
            }
            target.drawBitmap(this.redoBmp, this.x, this.y, null);
        }

        private int getBitmapSize(Bitmap bmp) {
            return 4 * bmp.getWidth() * bmp.getHeight();
        }

        public int getSize() {
            return this.getBitmapSize(this.undoBmp) + this.getBitmapSize(this.redoBmp);
        }
    }
}
