package io.github.karino2.pngnote.colorpicker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.List;
import java.util.Locale;

import io.github.karino2.pngnotemod.R;

public class ColorSecondAdapter extends BaseAdapter {
    private Context context;
    private ColorViewHolder gridholder;
    private int[] colors;

    public ColorSecondAdapter(Context context, int[] colors) {
        this.context = context;
        this.colors = colors;
    }

    @Override
    public int getCount() {
        if (this.colors != null) {
            return this.colors.length;
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(this.context, R.layout.colorlistsecondviewitem, null);
            gridholder = new ColorViewHolder();
            gridholder.cvColor = (CardView) convertView.findViewById(R.id.cvColor);
            gridholder.tvColorText = (TextView) convertView.findViewById(R.id.tvColorText);
            convertView.setTag(gridholder);
        } else {
            gridholder = (ColorViewHolder) convertView.getTag();
        }

        if (colors != null) {
            if (position >= 0 && position < colors.length) {
                int color = colors[position];
                gridholder.cvColor.setCardBackgroundColor(color);
                String hexString = Integer.toHexString(color);
                int zeroLen = 0;
                if (hexString.length() < 8) {
                    zeroLen = 8 - hexString.length();
                }
                for (int i = 0; i < zeroLen; ++i) {
                    hexString = "0" + hexString;
                }
                hexString = hexString.toUpperCase();
                if (hexString.length() >= 2) {
                    hexString = hexString.substring(2);
                }
                gridholder.tvColorText.setText("#" + hexString);
            }
        }
        return convertView;
    }

    private final static class ColorViewHolder {
        private CardView cvColor;
        private TextView tvColorText;
    }
}
