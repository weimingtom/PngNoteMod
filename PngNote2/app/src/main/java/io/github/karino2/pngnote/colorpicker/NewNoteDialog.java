package io.github.karino2.pngnote.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import io.github.karino2.pngnote.BookListActivity;
import io.github.karino2.pngnotemod.R;

import java.util.ArrayList;
import java.util.List;

public class NewNoteDialog extends Dialog {
    //FIXME:
    private final static int SINGLE_GRID_DP_WIDTH = 100; //FIXME:动态指定近期PDF文件的格子宽度
    //这个宽度参考pagegridviewitem_library的最大宽度，例如封面的dp宽度（可以稍微设置大一点）

    List<FileMeta> recentNoteList;
    private GridView recentNoteView;
    private NoteListAdapter recentNoteAdapter;
    EditText g_textState = null;
    Button btnCreate = null;

    public NewNoteDialog(@NonNull final Context context) {
        super(context);

        this.setContentView(R.layout.dialog_loadpages2);
        this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        g_textState = this.findViewById(R.id.textState);
        Button btnCancel = this.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewNoteDialog.this.dismiss();
            }
        });
        btnCreate = this.findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textState = "";
                if (g_textState != null) {
                    textState = g_textState.getText().toString();
                }
                if (textState.length() > 0) {
//                    if (onNewBook != null) {
//                        onNewBook.onNewBook(textState);
//                    }
                    if (context != null && context instanceof BookListActivity) {
                        BookListActivity act = (BookListActivity) context;
                        String backText = recentNoteAdapter.getSelectText();
                        if (act.checkText(textState)) {
                            NewNoteDialog.this.dismiss();
                            act.onNewBook(textState, backText);
                        } else {
                            Toast.makeText(context, "Duplicate note name, case insensitive", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(context, "Please input new note name", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
            }
        });
    }
}
