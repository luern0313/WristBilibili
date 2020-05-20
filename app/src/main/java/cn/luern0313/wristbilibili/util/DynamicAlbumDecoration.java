package cn.luern0313.wristbilibili.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 被 luern0313 创建于 2020/5/20.
 */

public class DynamicAlbumDecoration extends RecyclerView.ItemDecoration
{

    private static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    private static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private int mSpace = 1;
    private Rect mRect = new Rect(0, 0, 0, 0);
    private Paint mPaint = new Paint();

    private int mOrientation;

    private DynamicAlbumDecoration(Context context, int orientation, @ColorInt int color, int space)
    {
        mOrientation = orientation;
        if(space > 0)
            mSpace = space;
        mPaint.setColor(color);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
    {
        super.onDraw(c, parent, state);
        drawHorizontal(c, parent);
    }

    private void drawHorizontal(Canvas c, RecyclerView parent)
    {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mSpace;
            mRect.set(left, top, right, bottom);
            c.drawRect(mRect, mPaint);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);
        if(mOrientation == VERTICAL_LIST)
        {
            outRect.set(0, 0, 0, mSpace);
        }
        else
        {
            outRect.set(0, 0, mSpace, 0);
        }
    }

    public static DynamicAlbumDecoration createHorizontal(Context context, @ColorInt int color, int width)
    {
        return new DynamicAlbumDecoration(context, HORIZONTAL_LIST, color, width);
    }
}
