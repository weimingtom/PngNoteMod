package io.github.karino2.pngnote.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import io.github.karino2.pngnote.BookActivity;
import io.github.karino2.pngnotemod.R;

public class SimpleColorDialog extends Dialog {
    private RelativeLayout cbutton1, cbutton2, cbutton3, cbutton4, cbutton5;
    private RelativeLayout cbutton6, cbutton7, cbutton8, cbutton9, cbutton10;

    public SimpleColorDialog(@NonNull Context context) {
        super(context);
        //this.setContentView(R.layout.dialog_simplecolor);
        this.setContentView(R.layout.dialog_simplecolor2);

        cbutton1 = (RelativeLayout) this.findViewById(R.id.cbutton1);
        cbutton2 = (RelativeLayout) this.findViewById(R.id.cbutton2);
        cbutton3 = (RelativeLayout) this.findViewById(R.id.cbutton3);
        cbutton4 = (RelativeLayout) this.findViewById(R.id.cbutton4);
        cbutton5 = (RelativeLayout) this.findViewById(R.id.cbutton5);
        cbutton6 = (RelativeLayout) this.findViewById(R.id.cbutton6);
        cbutton7 = (RelativeLayout) this.findViewById(R.id.cbutton7);
        cbutton8 = (RelativeLayout) this.findViewById(R.id.cbutton8);
        cbutton9 = (RelativeLayout) this.findViewById(R.id.cbutton9);
        cbutton10 = (RelativeLayout) this.findViewById(R.id.cbutton10);
        final RelativeLayout[] buttons = new RelativeLayout[]{
                cbutton1, cbutton2, cbutton3, cbutton4, cbutton5,
                cbutton6, cbutton7, cbutton8, cbutton9, cbutton10,
        };
        final int[] colors = new int[]{
                0x2c988c, 0x3073aa, 0x8b1a63, 0x706559, 0x282425,
                0xe2e2e2, 0xdb5832, 0x81b845, 0xff0000, 0x00ff00
        };
        for (int i = 0; i < buttons.length; ++i) {
//            if (colors[i] == this.m_penColor) {
//                buttons[i].setBackgroundResource(R.drawable.com_facebook_button_blue);
//            }
            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int selectIndex = -1;
                    if (view == cbutton1) {
                        selectIndex = 0;
                    } else if (view == cbutton2) {
                        selectIndex = 1;
                    } else if (view == cbutton3) {
                        selectIndex = 2;
                    } else if (view == cbutton4) {
                        selectIndex = 3;
                    } else if (view == cbutton5) {
                        selectIndex = 4;
                    } else if (view == cbutton6) {
                        selectIndex = 5;
                    } else if (view == cbutton7) {
                        selectIndex = 6;
                    } else if (view == cbutton8) {
                        selectIndex = 7;
                    } else if (view == cbutton9) {
                        selectIndex = 8;
                    } else if (view == cbutton10) {
                        selectIndex = 9;
                    }
                    for (int i = 0; i < buttons.length; ++i) {
                        buttons[i].setBackgroundColor(0x00000000);
                    }
                    SimpleColorDialog.this.dismiss();
                    if (context != null && context instanceof BookActivity) {
                        BookActivity act = (BookActivity) context;
                        act.setPenColor(colors[selectIndex]);
                    }
//                    view.setBackgroundResource(R.drawable.com_facebook_button_blue);
//                    if (base != null) {
//                        base.setPenColor(colors[selectIndex]);
//
//                        final ImageView ivPenPreview = (ImageView) findViewById(R.id.ivPenPreview);
//                        //ivPenPreview.setImageDrawable(new ColorDrawable(0xff000000 | colors[selectIndex]));
//                        ivPenPreview.setImageDrawable(new CircleDrawable(0xff000000 | colors[selectIndex]));
//                    }
                }
            });
        }
    }
}
