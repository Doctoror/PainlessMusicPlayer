package com.doctoror.fuckoffmusicplayer.widget.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * View holder for single line item
 */
public final class SingleLineItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(android.R.id.text1) public TextView text;

    public SingleLineItemViewHolder(final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
