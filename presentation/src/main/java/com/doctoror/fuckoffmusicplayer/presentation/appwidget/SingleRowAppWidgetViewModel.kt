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

import android.app.PendingIntent
import android.graphics.Bitmap
import android.support.annotation.DrawableRes
import com.doctoror.fuckoffmusicplayer.R

class SingleRowAppWidgetViewModel {

    var albumThumb: Bitmap? = null
    val albumThumbPlaceholder = R.drawable.album_art_placeholder

    @DrawableRes
    var playPauseResId = R.drawable.ic_play_arrow_white_24dp
    var titleText: CharSequence? = null
    var artistText: CharSequence? = null

    var coverAction: PendingIntent? = null
    var playPauseAction: PendingIntent? = null
    var prevAction: PendingIntent? = null
    var nextAction: PendingIntent? = null
}
