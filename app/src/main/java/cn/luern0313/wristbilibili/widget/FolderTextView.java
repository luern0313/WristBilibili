package cn.luern0313.wristbilibili.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import cn.luern0313.wristbilibili.R;

public class FolderTextView extends AppCompatTextView {
    private static final String ELLIPSIS = "...";
    private static final String FOLD_TEXT = "  折叠";
    private static final String UNFOLD_TEXT = "  全部";
    private boolean isFold;
    private boolean isLess;
    private boolean noFold;
    private boolean isDrawed;
    ClickableSpan clickSpan;
    private boolean isInner;
    private int foldLine;
    private String fullText;
    private float mSpacingMult;
    private float mSpacingAdd;
    private static final String TAGView = "FolderTextView";
    private String fold_txt;
    private String unFold_txt;
    private int unFold_color;
    private int fold_color;

    public FolderTextView(Context context) {
        this(context, (AttributeSet)null);
    }

    public FolderTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FolderTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.isFold = false;
        this.isLess = false;
        this.noFold = true;
        this.isDrawed = false;
        this.clickSpan = new ClickableSpan() {
            public void onClick(View widget) {
                FolderTextView.this.isFold = !FolderTextView.this.isFold;
                FolderTextView.this.isDrawed = false;
                FolderTextView.this.invalidate();
            }

            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);
            }
        };
        this.isInner = false;
        this.mSpacingMult = 1.0F;
        this.mSpacingAdd = 0.0F;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FolderTextView);
        this.foldLine = a.getInt(R.styleable.FolderTextView_fold_line, 2);
        this.noFold = a.getBoolean(R.styleable.FolderTextView_noFold, false);
        this.fold_txt = a.getString(R.styleable.FolderTextView_fold_text);
        if (TextUtils.isEmpty(this.fold_txt)) {
            this.fold_txt = "  折叠";
        }

        this.unFold_txt = a.getString(R.styleable.FolderTextView_unfold_text);
        if (TextUtils.isEmpty(this.unFold_txt)) {
            this.unFold_txt = "  全部";
        }

        this.unFold_color = a.getColor(R.styleable.FolderTextView_unfold_text_color, -130452);
        this.fold_color = a.getColor(R.styleable.FolderTextView_fold_text_color, -12627531);
        a.recycle();
    }

    private void setUpdateText(CharSequence text) {
        this.isInner = true;
        this.setText(text);
    }

    public void setText(CharSequence text, BufferType type) {
        if (TextUtils.isEmpty(this.fullText) || !this.isInner) {
            this.isDrawed = false;
            this.fullText = String.valueOf(text);
        }

        super.setText(text, type);
    }

    public void setLineSpacing(float add, float mult) {
        this.mSpacingAdd = add;
        this.mSpacingMult = mult;
        super.setLineSpacing(add, mult);
    }

    public int getFoldLine() {
        return this.foldLine;
    }

    public void setFoldLine(int foldLine) {
        this.foldLine = foldLine;
    }

    private Layout makeTextLayout(String text) {
        return new StaticLayout(text, this.getPaint(), this.getWidth() - this.getPaddingLeft() - this.getPaddingRight(), Alignment.ALIGN_NORMAL, this.mSpacingMult, this.mSpacingAdd, false);
    }

    protected void onDraw(Canvas canvas) {
        if (!this.isDrawed) {
            this.resetText();
        }

        super.onDraw(canvas);
        this.isDrawed = true;
        this.isInner = false;
    }

    private void resetText() {
        String spanText = this.fullText;
        SpannableString spanStr;
        if (this.isFold) {
            spanStr = this.createUnFoldSpan(spanText);
        } else {
            spanStr = this.createFoldSpan(spanText);
        }

        if (this.isLess) {
            this.setUpdateText(this.fullText);
        } else {
            this.setUpdateText(spanStr);
            //this.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }

    private SpannableString createUnFoldSpan(String text) {
        String destStr = text + this.fold_txt;
        int start;
        int end;
        if (this.noFold) {
            start = 0;
            end = 0;
            destStr = text;
        } else {
            start = destStr.length() - this.fold_txt.length();
            end = destStr.length();
        }

        SpannableString spanStr = new SpannableString(destStr);
        spanStr.setSpan(this.clickSpan, start, end, 33);
        spanStr.setSpan(new ForegroundColorSpan(this.fold_color), start, end, 33);
        return spanStr;
    }

    private SpannableString createFoldSpan(String text) {
        String destStr = "";
        Layout layout = this.makeTextLayout(text);
        if (layout.getLineCount() <= this.getFoldLine()) {
            destStr = text;
            this.isLess = true;
        } else {
            destStr = this.tailorText(text);
        }

        int start;
        int end;
        if (this.isLess) {
            start = 0;
            end = 0;
        } else {
            start = destStr.length() - this.unFold_txt.length();
            end = destStr.length();
        }

        SpannableString spanStr = new SpannableString(destStr);
        spanStr.setSpan(this.clickSpan, start, end, 33);
        spanStr.setSpan(new ForegroundColorSpan(this.unFold_color), start, end, 33);
        return spanStr;
    }

    private String tailorText(String text) {
        String destStrTemp = text + this.unFold_txt;
        Layout layout = this.makeTextLayout(destStrTemp);
        if (layout.getLineCount() < this.getFoldLine()) {
            this.isLess = true;
            return text;
        } else {
            this.isLess = false;
            String destStr = text + "..." + this.unFold_txt;
            if (layout.getLineCount() > this.getFoldLine()) {
                int index = layout.getLineEnd(this.getFoldLine());
                if (text.length() < index) {
                    index = text.length();
                }

                String subText = text.substring(0, index - 1);
                return this.tailorText(subText);
            } else {
                return destStr.substring(0, destStr.length() - "...".length() - this.unFold_txt.length() - 2) + destStr.substring(destStr.length() - "...".length() - this.unFold_txt.length());
            }
        }
    }
}

