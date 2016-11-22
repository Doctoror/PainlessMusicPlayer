package com.doctoror.fuckoffmusicplayer.search;

import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Yaroslav Mytkalyk on 22.11.16.
 */

final class SearchResultsViewHolder extends WearableListView.ViewHolder {

    TextView textView;

    SearchResultsViewHolder(final View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(android.R.id.text1);
    }
}
