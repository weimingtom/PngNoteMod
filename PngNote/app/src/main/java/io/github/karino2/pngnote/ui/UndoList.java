package io.github.karino2.pngnote.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UndoList {
    private List<UndoCommand> commandList = new ArrayList<UndoCommand>();
    private int currentPos = -1;
    private static final int COMMAND_MAX_SIZE = 0x100000;

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
        UndoCommand cmd;
        for (Iterator<UndoCommand> var2 = this.commandList.iterator();
            var2.hasNext();
            res += cmd.getSize()) {
            cmd = var2.next();
        }
        return res;
    }

    public boolean getCanUndo() {
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
            target.drawBitmap(this.undoBmp, this.x, this.y, null);
        }

        public void redo(Canvas target) {
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
