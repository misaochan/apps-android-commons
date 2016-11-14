package fr.free.nrw.commons.category;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeSet;

import fr.free.nrw.commons.R;

public class CategoriesAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private Context context;
    private LayoutInflater mInflater;

    //FIXME: Might have issue here, headers need to be a String type so you can't just add them to an ArrayList of CategoryItem
    private ArrayList<CategorizationFragment.CategoryItem> items;

    public CategoriesAdapter(Context context, ArrayList<CategorizationFragment.CategoryItem> items) {
        this.context = context;
        this.items = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int i) {
        return items.get(i);
    }

    public ArrayList<CategorizationFragment.CategoryItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<CategorizationFragment.CategoryItem> items) {
        this.items = items;
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        CheckedTextView checkedView;

        if(view == null) {
            checkedView = (CheckedTextView) mInflater.inflate(R.layout.layout_categories_item, null);

        } else {
            checkedView = (CheckedTextView) view;
        }

        CategorizationFragment.CategoryItem item = (CategorizationFragment.CategoryItem) this.getItem(i);
        checkedView.setChecked(item.selected);
        checkedView.setText(item.name);
        checkedView.setTag(i);

        return checkedView;
    }
}