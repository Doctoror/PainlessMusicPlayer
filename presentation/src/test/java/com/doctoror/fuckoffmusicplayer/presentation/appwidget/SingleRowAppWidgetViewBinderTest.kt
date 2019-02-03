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

import android.graphics.Bitmap
import android.widget.RemoteViews
import com.doctoror.fuckoffmusicplayer.R
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class SingleRowAppWidgetViewBinderTest {

    private val view: RemoteViews = mock()
    private val model = SingleRowAppWidgetViewModel()

    private val underTest = SingleRowAppWidgetViewBinder()

    @Test
    fun bindsAlbumThumbPlaceholder() {
        // When
        underTest.bind(view, model)

        // Then
        verify(view).setImageViewResource(
                R.id.appwidget_img_albumart, model.albumThumbPlaceholder)
    }

    @Test
    fun bindsAlbumThumb() {
        // Given
        val albumThumb: Bitmap = mock()
        model.albumThumb = albumThumb

        // When
        underTest.bind(view, model)

        // Then
        verify(view).setImageViewBitmap(R.id.appwidget_img_albumart, albumThumb)
    }

    @Test
    fun bindsPlayPauseButtonRes() {
        // When
        underTest.bind(view, model)

        // Then
        verify(view).setImageViewResource(R.id.appwidget_btn_play_pause, model.playPauseResId)
    }

    @Test
    fun bindsArtistText() {
        // Given
        model.artistText = "Artist"

        // When
        underTest.bind(view, model)

        // Then
        verify(view).setTextViewText(R.id.appwidget_text_artist, model.artistText)
    }

    @Test
    fun bindsTitleText() {
        // Given
        model.titleText = "Title"

        // When
        underTest.bind(view, model)

        // Then
        verify(view).setTextViewText(R.id.appwidget_text_title, model.titleText)
    }

    @Test
    fun bindsPlayPauseAction() {
        // Given
        model.playPauseAction = mock()

        // When
        underTest.bind(view, model)

        // Then
        verify(view).setOnClickPendingIntent(R.id.appwidget_btn_play_pause, model.playPauseAction)
    }

    @Test
    fun bindsPrevAction() {
        // Given
        model.prevAction = mock()

        // When
        underTest.bind(view, model)

        // Then
        verify(view).setOnClickPendingIntent(R.id.appwidget_btn_prev, model.prevAction)
    }

    @Test
    fun bindsNextAction() {
        // Given
        model.nextAction = mock()

        // When
        underTest.bind(view, model)

        // Then
        verify(view).setOnClickPendingIntent(R.id.appwidget_btn_next, model.nextAction)
    }

    @Test
    fun bindsCoverAction() {
        // Given
        model.coverAction = mock()

        // When
        underTest.bind(view, model)

        // Then
        verify(view).setOnClickPendingIntent(R.id.appwidget_img_albumart, model.coverAction)
    }
}
