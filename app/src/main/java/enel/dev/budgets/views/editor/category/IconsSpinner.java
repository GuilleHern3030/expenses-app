package enel.dev.budgets.views.editor.category;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Color;
import enel.dev.budgets.objects.category.Icon;

public class IconsSpinner extends BaseAdapter {
    private final Context context;

    public IconsSpinner(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return Icon.icons().length;
    }

    @Override
    public Object getItem(int position) {
        return Icon.icon(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.spinner_icons, parent, false);
        }

        final int image = Icon.icon(position);
        ImageView icon = convertView.findViewById(R.id.icon);
        icon.setImageResource(image);

        return convertView;
    }
}