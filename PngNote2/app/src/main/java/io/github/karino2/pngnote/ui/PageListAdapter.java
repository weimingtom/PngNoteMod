package io.github.karino2.pngnote.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.github.karino2.pngnotemod.R;


public class PageListAdapter extends BaseAdapter {
    private Context context;
    private GridViewHolder gridholder;
    private List<Page> dataList;

    public PageListAdapter(Context context, List<Page> results) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(this.context, R.layout.pagelistviewitem, null);
            gridholder = new GridViewHolder();
            gridholder.tfBookName = (TextView) convertView.findViewById(R.id.bookgrid_name);
            gridholder.ivCoverImage = (ImageView) convertView.findViewById(R.id.bookgrid_pic);
            gridholder.ivCoverImageBack = (ImageView) convertView.findViewById(R.id.bookgrid_pic_backgroud);
            gridholder.imageButtonDelete = (ImageButton) convertView.findViewById(R.id.imageButtonDelete);
            convertView.setTag(gridholder);
        } else {
            gridholder = (GridViewHolder) convertView.getTag();
        }

        if (dataList != null) {
            Page page = dataList.get(position);
            if (page != null) {
                gridholder.tfBookName.setText(page.getTitle() != null ? page.getTitle() : "no title");
                gridholder.ivCoverImage.setImageBitmap(page.getThumbnail());
                gridholder.ivCoverImageBack.setImageBitmap(page.getBgThumbnail());
                gridholder.imageButtonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemDelete(position, page);
                    }
                });
            }
        }
        return convertView;
    }

    public void onItemDelete(int position, Page page) {

    }

    private final static class GridViewHolder {
        private TextView tfBookName;
        private ImageView ivCoverImage;
        private ImageView ivCoverImageBack;
        private ImageButton imageButtonDelete;
    }
}
