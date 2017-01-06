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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * View holder for media in playlist
 */
final class PlaylistItemViewHolder extends RecyclerView.ViewHolder implements
        ItemTouchHelperViewHolder {

    @BindView(R.id.textTitle) TextView textTitle;
    @BindView(R.id.textArtist) TextView textArtist;
    @BindView(R.id.textDuration) TextView textDuration;

    @Nullable
    private final Drawable mDefaultBackground;
    private final float mDefaultElevation;

    @BindDimen(R.dimen.list_drag_selected_item_elevation)
    float mElevationSelected;

    private Drawable mSelectedBackground;

    PlaylistItemViewHolder(@NonNull final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        mDefaultBackground = itemView.getBackground();
        mDefaultElevation = ViewCompat.getElevation(itemView);
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
