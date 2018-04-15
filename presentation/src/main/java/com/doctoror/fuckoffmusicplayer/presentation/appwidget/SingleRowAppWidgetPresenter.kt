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
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.presentation.Henson
import com.doctoror.fuckoffmusicplayer.presentation.home.HomeActivity
import com.doctoror.fuckoffmusicplayer.presentation.playback.PlaybackServiceIntentFactory

class SingleRowAppWidgetPresenter(
        private val albumThumbHolder: AlbumThumbHolder,
        private val currentMediaProvider: CurrentMediaProvider,
        private val viewModel: SingleRowAppWidgetViewModel) {

    fun bindState(context: Context, state: PlaybackState) {
        bindAppearance(context, state)
        bindClickActions(context)
    }

    private fun bindAppearance(context: Context, state: PlaybackState) {
        val media = currentMediaProvider.currentMedia

        var artist: CharSequence? = media?.artist
        var title: CharSequence? = media?.title

        if (TextUtils.isEmpty(artist)) {
            artist = context.getText(R.string.Unknown_artist)
        }
        if (TextUtils.isEmpty(title)) {
            title = context.getText(R.string.Untitled)
        }

        viewModel.artistText = artist
        viewModel.titleText = title
        viewModel.albumThumb = albumThumbHolder.albumThumb

        viewModel.playPauseResId = if (state == PlaybackState.STATE_PLAYING)
            R.drawable.ic_pause_white_24dp
        else
            R.drawable.ic_play_arrow_white_24dp
    }

    private fun bindClickActions(context: Context) {
        val hasMedia = currentMediaProvider.currentMedia != null

        if (hasMedia) {
            setPlayPauseButtonAction(context)
            setPrevButtonAction(context)
            setNextButtonAction(context)
        } else {
            val playAnything = generatePlayAnythingIntent(context)
            viewModel.playPauseAction = playAnything
            viewModel.prevAction = playAnything
            viewModel.nextAction = playAnything
        }

        setCoverAction(context, hasMedia)
    }

    private fun setPlayPauseButtonAction(context: Context) {
        val intent = PlaybackServiceIntentFactory.intentPlayPause(context)
        val action = serviceIntent(context, intent)
        viewModel.playPauseAction = action
    }

    private fun setPrevButtonAction(context: Context) {
        val intent = PlaybackServiceIntentFactory.intentPrev(context)
        val action = serviceIntent(context, intent)
        viewModel.prevAction = action
    }

    private fun setNextButtonAction(context: Context) {
        val intent = PlaybackServiceIntentFactory.intentNext(context)
        val action = serviceIntent(context, intent)
        viewModel.nextAction = action
    }

    private fun setCoverAction(context: Context, hasMedia: Boolean) {
        val coverIntent = if (hasMedia) {
            Henson.with(context)
                    .gotoNowPlayingActivity()
                    .hasCoverTransition(true)
                    .hasListViewTransition(false)
                    .build()
        } else {
            Intent(context, HomeActivity::class.java)
        }

        viewModel.coverAction = PendingIntent.getActivity(
                context, 0, coverIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun generatePlayAnythingIntent(context: Context): PendingIntent {
        val intent = PlaybackServiceIntentFactory.intentPlayAnything(context)
        return serviceIntent(context, intent)
    }

    private fun serviceIntent(context: Context, intent: Intent): PendingIntent {
        return PendingIntent.getService(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
