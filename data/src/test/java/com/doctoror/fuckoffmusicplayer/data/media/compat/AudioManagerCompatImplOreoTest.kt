package com.doctoror.fuckoffmusicplayer.data.media.compat

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.O])
@RunWith(RobolectricTestRunner::class)
class AudioManagerCompatImplOreoTest {

    private val onAudioFocusChangeListener = { _: Int -> }

    private val audioAttributes = AudioAttributesCompat(
        usage = USAGE_MEDIA,
        contentType = CONTENT_TYPE_MUSIC,
        legacyStreamType = AudioManager.STREAM_MUSIC)

    private val audioFocusRequest = AudioFocusRequestCompat(
        audioAttributes = audioAttributes,
        acceptsDelayedFocusGain = false,
        onAudioFocusChangeListener = onAudioFocusChangeListener,
        willPauseWhenDucked = false)

    private val audioManager: AudioManager = Mockito.mock(AudioManager::class.java)

    private val underTest = AudioManagerCompat.Impl.ImplOreo()

    @Test
    fun requestsAudioFocus() {
        // When
        underTest.requestAudioFocus(audioManager, audioFocusRequest)

        // Then
        val captor = ArgumentCaptor.forClass(AudioFocusRequest::class.java)
        Mockito.verify(audioManager).requestAudioFocus(captor.capture())

        val expectedAudioFocusRequest = toAudioFocusRequest(audioFocusRequest)
        Assert.assertTrue(audioFocusRequestsEqual(expectedAudioFocusRequest, captor.value))
    }

    @Test
    fun abandonsRequestedAudioFocus() {
        // When
        underTest.requestAudioFocus(audioManager, audioFocusRequest)
        underTest.abandonAudioFocus(audioManager)

        // Then
        val captor = ArgumentCaptor.forClass(AudioFocusRequest::class.java)
        Mockito.verify(audioManager).abandonAudioFocusRequest(captor.capture())

        val expectedAudioFocusRequest = toAudioFocusRequest(audioFocusRequest)
        Assert.assertTrue(audioFocusRequestsEqual(expectedAudioFocusRequest, captor.value))
    }

    @Test
    fun doesNotAbandonRequestedAudioFocusTwice() {
        // When
        underTest.requestAudioFocus(audioManager, audioFocusRequest)
        underTest.abandonAudioFocus(audioManager)
        underTest.abandonAudioFocus(audioManager)

        // Then
        Mockito.verify(audioManager, Mockito.times(1))
            .abandonAudioFocusRequest(ArgumentMatchers.any())
    }

    @Test
    fun doesNothingWhenAbandonRequestedWithoutPriorRequest() {
        // When
        underTest.abandonAudioFocus(audioManager)

        // Then
        Mockito.verify(audioManager, Mockito.never())
            .abandonAudioFocusRequest(ArgumentMatchers.any())
    }

    private fun audioFocusRequestsEqual(
        a: AudioFocusRequest,
        b: AudioFocusRequest
    ): Boolean {
        return (a.acceptsDelayedFocusGain() == b.acceptsDelayedFocusGain()) and
                audioAttributesEqual(a.audioAttributes, b.audioAttributes) and
                (a.willPauseWhenDucked() == b.willPauseWhenDucked())
    }

    private fun audioAttributesEqual(
        a: AudioAttributes,
        b: AudioAttributes
    ): Boolean {
        return (a.contentType == b.contentType) and
                (a.flags == b.flags) and
                (a.usage == b.usage) and
                (a.volumeControlStream == b.volumeControlStream)
    }

    private fun toAudioAttributes(attributes: AudioAttributesCompat) = AudioAttributes
        .Builder()
        .setContentType(attributes.contentType)
        .setLegacyStreamType(attributes.legacyStreamType)
        .setUsage(attributes.usage)
        .build()

    private fun toAudioFocusRequest(request: AudioFocusRequestCompat) = AudioFocusRequest
        .Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(toAudioAttributes(request.audioAttributes))
        .setAcceptsDelayedFocusGain(request.acceptsDelayedFocusGain)
        .setOnAudioFocusChangeListener(request.onAudioFocusChangeListener)
        .setWillPauseWhenDucked(request.willPauseWhenDucked)
        .build()
}
