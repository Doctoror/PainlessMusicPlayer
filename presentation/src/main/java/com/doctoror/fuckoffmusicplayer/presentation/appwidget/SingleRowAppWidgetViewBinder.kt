/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.presentation.appwidget

import android.widget.RemoteViews
import com.doctoror.fuckoffmusicplayer.R

class SingleRowAppWidgetViewBinder {

    fun bind(view: RemoteViews, model: SingleRowAppWidgetViewModel) {

        val thumb = model.albumThumb
        if (thumb != null) {
            view.setImageViewBitmap(R.id.appwidget_img_albumart, thumb)
        } else {
            view.setImageViewResource(R.id.appwidget_img_albumart, model.albumThumbPlaceholder)
        }

        view.setImageViewResource(R.id.appwidget_btn_play_pause, model.playPauseResId)

        view.setTextViewText(R.id.appwidget_text_artist, model.artistText)
        view.setTextViewText(R.id.appwidget_text_title, model.titleText)

        view.setOnClickPendingIntent(R.id.appwidget_btn_play_pause, model.playPauseAction)
        view.setOnClickPendingIntent(R.id.appwidget_btn_prev, model.prevAction)
        view.setOnClickPendingIntent(R.id.appwidget_btn_next, model.nextAction)
        view.setOnClickPendingIntent(R.id.appwidget_img_albumart, model.coverAction)
    }
}
