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
package com.doctoror.fuckoffmusicplayer.data.playback.unit

import androidx.annotation.VisibleForTesting
import com.doctoror.fuckoffmusicplayer.data.util.CollectionUtils
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_IDLE
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_PAUSED
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer
import com.doctoror.fuckoffmusicplayer.domain.queue.Media

class PlaybackServiceUnitPlayMediaFromQueue(
        private val currentMediaProvider: CurrentMediaProvider,
        private val mediaPlayer: MediaPlayer,
        private val playbackData: PlaybackData,
        private val unitAudioFocus: PlaybackServiceUnitAudioFocus,
        private val unitReporter: PlaybackServiceUnitReporter) {

    @VisibleForTesting
    val remainingDurationForPlayWhereStopped = 100

    /**
     * Plays media from queue based on position.
     *
     * If queue position points to the same media, will continue playback.
     * If queue position points to different media, will replace current media and start playback of new media
     */
    fun play(
            queue: List<Media>?,
            position: Int) {
        if (queue == null || queue.isEmpty()) {
            throw IllegalArgumentException("Play queue is null or empty")
        }

        val currentState = playbackData.playbackState
        val currentMedia = currentMediaProvider.currentMedia
        val targetMedia = resolveTargetMedia(queue, position)

        if (queue == playbackData.queue &&
                isTheSameMediaPaused(currentState, currentMedia, targetMedia)) {
            requestFocusAndPlay()
            return
        }

        changeMediaAndPlay(
                queue,
                currentState,
                position,
                currentMedia,
                targetMedia)
    }

    private fun changeMediaAndPlay(
            queue: List<Media>,
            currentState: PlaybackState,
            position: Int,
            currentMedia: Media?,
            targetMedia: Media) {

        mediaPlayer.stop()
        playbackData.setPlayQueue(queue)
        playbackData.setPlayQueuePosition(position)

        val seekPosition = resolveSeekPosition(
                currentState = currentState,
                currentMedia = currentMedia,
                targetMedia = targetMedia)

        playbackData.setMediaPosition(seekPosition)
        playbackData.persistAsync()

        unitAudioFocus.requestAudioFocus()
        if (unitAudioFocus.focusGranted) {
            val uri = targetMedia.data
            if (uri != null) {
                mediaPlayer.load(uri)
                if (seekPosition != 0L) {
                    mediaPlayer.seekTo(seekPosition)
                }
                playAndReportCurrentState()
            }
        }
    }

    private fun resolveTargetMedia(
            queue: List<Media>,
            position: Int): Media {
        var media = CollectionUtils.getItemSafe(queue, position)
        if (media == null) {
            media = queue[0]
        }
        return media
    }

    private fun isTheSameMediaPaused(
            currentState: PlaybackState,
            currentMedia: Media?,
            targetMedia: Media) =
            currentState == STATE_PAUSED
                    && currentMedia != null
                    && targetMedia.id == currentMedia.id

    private fun resolveSeekPosition(
            currentState: PlaybackState,
            currentMedia: Media?,
            targetMedia: Media): Long {
        var seekPosition: Long = 0
        // If restoring from stopped state, set seek position to what it was
        if (currentState == STATE_IDLE && targetMedia == currentMedia) {
            seekPosition = playbackData.mediaPosition
            if (seekPosition > targetMedia.duration - remainingDurationForPlayWhereStopped) {
                seekPosition = 0
            }
        }
        return seekPosition
    }

    private fun requestFocusAndPlay() {
        unitAudioFocus.requestAudioFocus()
        if (unitAudioFocus.focusGranted) {
            playAndReportCurrentState()
        }
    }

    private fun playAndReportCurrentState() {
        mediaPlayer.play()
        unitReporter.reportCurrentMedia()
    }
}
