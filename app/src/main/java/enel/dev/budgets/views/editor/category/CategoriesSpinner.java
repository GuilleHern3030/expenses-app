package enel.dev.budgets.views.editor.category;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.category.Categories;

public class CategoriesSpinner extends BaseAdapter {

    private Context context;
    private final Categories categories;

    public CategoriesSpinner(Context context, final Categories categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.spinner_category, parent, false);
        }

        final int image = categories.get(position).getImage();
        final int color = categories.get(position).getColor();
        final String name = categories.get(position).getName();

        FrameLayout iconBorder = convertView.findViewById(R.id.icon_container);
        ImageView icon = convertView.findViewById(R.id.icon);
        TextView text = convertView.findViewById(R.id.text);

        iconBorder.setBackgroundResource(color);
        icon.setImageResource(image);
        text.setText(name);

        return convertView;
    }
}
