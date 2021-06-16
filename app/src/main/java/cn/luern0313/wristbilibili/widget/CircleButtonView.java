package cn.luern0313.wristbilibili.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.king.view.circleprogressview.CircleProgressView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;

/**
 * 被 luern0313 创建于 2021/1/20.
 */

public class CircleButtonView extends RelativeLayout
{
    private final Context ctx;
    private final View rootView;
    private final ImageView img;
    private final ImageView imgBackground;
    private final TextView name;
    private final CircleProgressView progressView;

    private final GradientDrawable fillShapeDrawable;

    private String nameDefault;
    private int nameNumber;
    private boolean isChecked;
    private int srcUnchecked;
    private int srcChecked;
    private boolean isFillBG;

    public CircleButtonView(Context context)
    {
        this(context, null);
    }

    public CircleButtonView(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public CircleButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }

    public CircleButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        ctx = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleButtonView);

        boolean isProgress = typedArray.getBoolean(R.styleable.CircleButtonView_is_progress, false);
        if(!isProgress)
        {
            rootView = View.inflate(context, R.layout.widget_circle_button, this);
            imgBackground = img = rootView.findViewById(R.id.circle_button_img);
            name = rootView.findViewById(R.id.circle_button_text);
            progressView = null;
        }
        else
        {
            rootView = View.inflate(context, R.layout.widget_circle_button_progress, this);
            img = rootView.findViewById(R.id.circle_button_progress_img);
            name = rootView.findViewById(R.id.circle_button_progress_text);
            imgBackground = rootView.findViewById(R.id.circle_button_progress_img_bg);
            progressView = rootView.findViewById(R.id.circle_button_progress_progress);
        }

        fillShapeDrawable = (GradientDrawable) ContextCompat.getDrawable(ctx, R.drawable.shape_bg_circle_burron_fill);

        initStyle(typedArray);

        typedArray.recycle();
    }

    public void setChecked(boolean isChecked)
    {
        this.isChecked = isChecked;
        if(!isChecked)
        {
            img.setImageResource(srcUnchecked);
            if(!isFillBG)
                imgBackground.setBackgroundResource(R.drawable.icon_bg_circle_button);
            else
                imgBackground.setBackground(fillShapeDrawable);
        }
        else
        {
            img.setImageResource(srcChecked);
            if(!isFillBG)
                imgBackground.setBackgroundResource(R.drawable.icon_bg_circle_button_checked);
            else
                imgBackground.setBackground(fillShapeDrawable);
        }
    }

    public void setDefaultName(String text)
    {
        this.nameDefault = text;
        if(nameNumber <= 0 && nameDefault != null)
            name.setText(nameDefault);
    }

    public void setNameColor(int color)
    {
        name.setTextColor(color);
    }

    public void setNameNumber(int number)
    {
        this.nameNumber = number;
        if(nameNumber <= 0 && nameDefault != null)
            name.setText(nameDefault);
        else
            name.setText(DataProcessUtil.getView(nameNumber));
    }

    public void setSrcUnchecked(int resId)
    {
        this.srcUnchecked = resId;
        setChecked(isChecked);
    }

    public void setSrcChecked(int resId)
    {
        this.srcChecked = resId;
        setChecked(isChecked);
    }

    public void setFillBGColor(int color)
    {
        fillShapeDrawable.setColor(color);
    }

    public CircleProgressView getProgress()
    {
        return progressView;
    }

    public ImageView getImg()
    {
        return img;
    }

    private void initStyle(TypedArray typedArray)
    {
        int resourceId = typedArray.getResourceId(R.styleable.CircleButtonView_name_default, -1);
        setDefaultName(resourceId != -1 ? getResources().getString(resourceId) : typedArray.getString(R.styleable.CircleButtonView_name_default));
        if(typedArray.hasValue(R.styleable.CircleButtonView_name_color))
            setNameColor(typedArray.getColor(R.styleable.CircleButtonView_name_color, ColorUtil.getColor(R.attr.colorTitle, ctx)));
        isFillBG = typedArray.getBoolean(R.styleable.CircleButtonView_is_fill_bg, false);
        setSrcUnchecked(typedArray.getResourceId(R.styleable.CircleButtonView_src_unchecked, 0));
        setSrcChecked(typedArray.getResourceId(R.styleable.CircleButtonView_src_checked, 0));
        setChecked(typedArray.getBoolean(R.styleable.CircleButtonView_is_checked, false));
        setFillBGColor(typedArray.getColor(R.styleable.CircleButtonView_fill_bg_color, ctx.getResources().getColor(R.color.gray_80)));
    }
}
