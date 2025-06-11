package io.github.karino2.pngnote.colorpicker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.List;

import io.github.karino2.pngnotemod.R;

public class NoteListAdapter extends BaseAdapter {
    private Context context;
    private GridViewHolder gridholder;
    private List<FileMeta> dataList;

    public int selectIndex = 0;

    public String getSelectText() {
        if (selectIndex < 0 || dataList == null || selectIndex >= dataList.size()) {
            return "";
        }
        return dataList.get(selectIndex).name;
    }
    public void select(int pos) {
        this.selectIndex = pos;
        this.notifyDataSetChanged();
    }

    public NoteListAdapter(Context context, List<FileMeta> recentNoteList) {
        this.context = context;
        this.dataList = recentNoteList;
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

    //@see com.foobnix.ui2.adapter.FileMetaAdapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(this.context, R.layout.pagegridviewitem_note_grid, null);
            gridholder = new GridViewHolder();
            gridholder.tfBookName = (TextView) convertView.findViewById(R.id.bookgrid_name_library);
            gridholder.ivCoverImage = (ImageView) convertView.findViewById(R.id.browserItemIcon_library);
            //gridholder.tfBookTime = (TextView) convertView.findViewById(R.id.bookgrid_time_library);
            //gridholder.llWrapper = (LinearLayout) convertView.findViewById(R.id.llWrapper);
            gridholder.img_item_card = (CardView) convertView.findViewById(R.id.img_item_card);
            convertView.setTag(gridholder);
        } else {
            gridholder = (GridViewHolder) convertView.getTag();
        }

        if (dataList != null) {
            FileMeta fileMeta = dataList.get(position);
            if (fileMeta != null) {
                if (fileMeta.name != null) {
                    gridholder.tfBookName.setText(fileMeta.name);
                }
                if (fileMeta.drawable != 0) {
                    gridholder.ivCoverImage.setImageResource(fileMeta.drawable);
                } else {
                    gridholder.ivCoverImage.setImageResource(R.drawable.paint_empty);
                }
                if (position == this.selectIndex) {
                    gridholder.img_item_card.setCardBackgroundColor(0xFF2196F3); //0xFFFF0000
                } else {
                    gridholder.img_item_card.setCardBackgroundColor(0x00000000);
                }
            }
        }
        return convertView;
    }

    private final static class GridViewHolder {
        private TextView tfBookName;
        //private TextView tfBookTime;
        private ImageView ivCoverImage;
        //private ImageView ivCoverImageBack;
        //private LinearLayout llWrapper;
        private CardView img_item_card;
    }
}
