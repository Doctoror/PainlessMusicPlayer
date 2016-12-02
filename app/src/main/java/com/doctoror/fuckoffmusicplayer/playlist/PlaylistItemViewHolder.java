package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.util.ThemeUtils;
import com.doctoror.fuckoffmusicplayer.widget.ItemTouchHelperViewHolder;
import com.doctoror.fuckoffmusicplayer.widget.TwoLineItemViewHolder;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Created by Yaroslav Mytkalyk on 02.12.16.
 */

final class PlaylistItemViewHolder extends TwoLineItemViewHolder implements
        ItemTouchHelperViewHolder {

    @Nullable
    private final Drawable mDefaultBackground;
    private final float mDefaultElevation;

    private final float mElevationSelected;
    private Drawable mSelectedBackground;

    PlaylistItemViewHolder(final View itemView) {
        super(itemView);
        mDefaultBackground = itemView.getBackground();
        mDefaultElevation = ViewCompat.getElevation(itemView);
        mElevationSelected = itemView.getResources()
                .getDimension(R.dimen.list_drag_selected_item_elevation);
    }

    @Override
    public void onItemSelected() {
        itemView.setBackground(getSelectedBackground(itemView.getContext()));
        ViewCompat.setElevation(itemView, mElevationSelected);
    }

    @Override
    public void onItemClear() {
        itemView.setBackground(mDefaultBackground);
        ViewCompat.setElevation(itemView, mDefaultElevation);
    }

    @NonNull
    private Drawable getSelectedBackground(@NonNull final Context context) {
        if (mSelectedBackground != null) {
            return mSelectedBackground;
        }

        mSelectedBackground = new LayerDrawable(new Drawable[]{
                new ColorDrawable(ThemeUtils.getColor(context.getTheme(),
                        android.R.attr.windowBackground)),
                new ColorDrawable(ContextCompat.getColor(context, R.color.dividerBackground))
        });

        return mSelectedBackground;
    }
}
