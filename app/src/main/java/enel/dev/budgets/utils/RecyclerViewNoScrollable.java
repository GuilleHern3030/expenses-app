package enel.dev.budgets.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewNoScrollable extends RecyclerView {

    public RecyclerViewNoScrollable(Context context) {
        super(context);
    }

    public RecyclerViewNoScrollable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewNoScrollable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int heightSpecCustom = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, heightSpecCustom);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}
