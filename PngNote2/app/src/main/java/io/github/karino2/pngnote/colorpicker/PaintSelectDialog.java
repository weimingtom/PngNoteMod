package io.github.karino2.pngnote.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import io.github.karino2.pngnote.BookActivity;
import io.github.karino2.pngnotemod.R;

import java.util.ArrayList;
import java.util.List;

public class PaintSelectDialog extends Dialog {
    //FIXME:
    private final static int SINGLE_GRID_DP_WIDTH = 100; //FIXME:动态指定近期PDF文件的格子宽度
    //这个宽度参考pagegridviewitem_library的最大宽度，例如封面的dp宽度（可以稍微设置大一点）

    List<FileMeta> recentNoteList;
    private GridView recentNoteView;
    private NoteListAdapter recentNoteAdapter;

    public PaintSelectDialog(@NonNull final Context context) {
        super(context);

        this.setContentView(R.layout.dialog_paintselect);
        this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        recentNoteList = new ArrayList<FileMeta>();
        recentNoteList.add(new FileMeta(FileMeta.NONE, 0));
        recentNoteList.add(new FileMeta(FileMeta.LINED, R.drawable.paint_lined));
        recentNoteList.add(new FileMeta(FileMeta.GRAPH, R.drawable.paint_graph));
        recentNoteList.add(new FileMeta(FileMeta.DOTTED, R.drawable.paint_dotted));
//        for (int i = 0; i < 100; ++i) {
//            recentNoteList.add(new FileMeta("None" + i, 0));
//        }
        recentNoteView = (GridView) findViewById(R.id.notegridview_home);
        recentNoteView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        recentNoteView.setBackgroundColor(Color.WHITE);
        recentNoteAdapter = new NoteListAdapter(this.getContext(), recentNoteList);
        recentNoteAdapter.select(-1);
        recentNoteView.setAdapter(recentNoteAdapter);
        int size = 3;
        if (recentNoteAdapter.getCount() > 0) {
            size = recentNoteAdapter.getCount();
            recentNoteView.setNumColumns(size);
        } else {
            recentNoteView.setNumColumns(3);
        }
        int gridviewWidth = size * Dips.dpToPx(SINGLE_GRID_DP_WIDTH);
        //https://blog.csdn.net/zhuwentao2150/article/details/70211610
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridviewWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        recentNoteView.setLayoutParams(params);
        recentNoteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                recentNoteAdapter.select(position);

                PaintSelectDialog.this.dismiss();
                if (context != null && context instanceof BookActivity) {
                    BookActivity act = (BookActivity) context;
                    String backText = recentNoteAdapter.getSelectText();
                    act.onPaintSelect(backText);
                }
            }
        });
    }
}
