package com.doctoror.fuckoffmusicplayer.widget;

import com.doctoror.aspectratiolayout.AspectRatioDelegate;
import com.doctoror.aspectratiolayout.AspectRatioInterface;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Yaroslav Mytkalyk on 11.11.16.
 */

public class AspectRatioImageView extends ImageView implements AspectRatioInterface {

    private final AspectRatioDelegate mDelegate;

    public AspectRatioImageView(final Context context) {
        super(context);
        mDelegate = new AspectRatioDelegate(this);
    }

    public AspectRatioImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mDelegate = new AspectRatioDelegate(this, attrs);
    }

    public AspectRatioImageView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDelegate = new AspectRatioDelegate(this, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AspectRatioImageView(final Context context, final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mDelegate = new AspectRatioDelegate(this, attrs);
    }

    @Override
    public void setAspect(final float aspect) {
        this.mDelegate.setAspect(aspect);
    }

    @Override
    public void setAspectType(final int aspectType) {
        this.mDelegate.setAspectType(aspectType);
    }

    @SuppressLint("WrongCall")
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        this.mDelegate.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @SuppressLint("WrongCall")
    @Override
    public void superOnMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
