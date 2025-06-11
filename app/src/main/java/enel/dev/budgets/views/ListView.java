package enel.dev.budgets.views;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public abstract class ListView extends BaseAdapter {

    protected final Context context;
    protected final Object[] elements;
    protected final android.widget.ListView listView;
    protected final TextView textView;
    protected static LayoutInflater inflater = null;
    public ListView(Context context, Object[] elements, android.widget.ListView listView, TextView textView) {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.elements = elements;
        this.listView = listView;
        this.textView = textView;
    }

    @Override
    public int getCount() { return elements.length; } // número de filas

    @Override
    public Object getItem(int i) { return i; }

    @Override
    public long getItemId(int i) { return i; }

    // Other functions
    public void show() {
        if (textView != null)
            textView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }
    public void hide() {
        if (textView != null)
            textView.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    protected void fixHeight(android.widget.ListView listView, int heightOfItem) {
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = dpToPx(heightOfItem) * elements.length;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    protected void fixHeight() {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // No hay adaptador, no hay nada que ajustar
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    protected abstract void setAdapter();

}
