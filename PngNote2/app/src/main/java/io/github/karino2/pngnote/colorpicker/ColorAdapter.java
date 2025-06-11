package io.github.karino2.pngnote.colorpicker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.cardview.widget.CardView;


import java.util.List;

import io.github.karino2.pngnotemod.R;

public class ColorAdapter extends BaseAdapter {
    private Context context;
    private ColorViewHolder gridholder;
    private List<MaterialColor> dataList;

    public ColorAdapter(Context context, List<MaterialColor> results) {
        this.context = context;
        this.dataList = results;
    }

    @Override
    public int getCount() {
        if (dataList != null) {
            return dataList.size();
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
            convertView = View.inflate(this.context, R.layout.colorlistfirstviewitem, null);
            gridholder = new ColorViewHolder();
            gridholder.cvColor = (CardView) convertView.findViewById(R.id.cvColor);
            convertView.setTag(gridholder);
        } else {
            gridholder = (ColorViewHolder) convertView.getTag();
        }

        if (dataList != null) {
            MaterialColor it = dataList.get(position);
            if (it != null) {
                gridholder.cvColor.setCardBackgroundColor(it.primaryValue);
            }
        }
        return convertView;
    }

    private final static class ColorViewHolder {
        private CardView cvColor;
    }
}
