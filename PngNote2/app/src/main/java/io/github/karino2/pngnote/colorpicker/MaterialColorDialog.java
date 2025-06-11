package io.github.karino2.pngnote.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;

import io.github.karino2.pngnotemod.R;

public class MaterialColorDialog extends Dialog {
    private ColorAdapter adapter;
    private ListView listViewFirst, listViewSecond;
    private ColorSecondAdapter secondAdapter;
    public MaterialColorDialog(@NonNull Context context) {
        super(context);
        this.setContentView(R.layout.dialog_selectcolor);

        //final ImageView ivPenPreview = (ImageView) findViewById(R.id.ivPenPreview);
        listViewFirst = (ListView) findViewById(R.id.listViewFirst);
        adapter = new ColorAdapter(context, Colors.colorList);
        listViewFirst.setAdapter(adapter);
        listViewFirst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < Colors.colorList.size()) {
                    MaterialColor c = Colors.colorList.get(position);
                    secondAdapter = new ColorSecondAdapter(MaterialColorDialog.this.getContext(), c.colors);
                    listViewSecond.setAdapter(secondAdapter);
                    //secondAdapter.notifyDataSetChanged();
                }
            }
        });

        listViewSecond = (ListView) findViewById(R.id.listViewSecond);
        secondAdapter = new ColorSecondAdapter(context, Colors.colorList.get(0).colors);
        listViewSecond.setAdapter(secondAdapter);
        listViewSecond.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    public void onDialogCancel() {
        this.dismiss();
    }
    public void onDialogDel() {
        this.dismiss();
    }
}
