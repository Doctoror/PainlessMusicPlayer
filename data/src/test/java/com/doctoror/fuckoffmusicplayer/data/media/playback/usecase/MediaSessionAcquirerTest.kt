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
package com.doctoror.fuckoffmusicplayer.data.media.playback.usecase

import com.doctoror.fuckoffmusicplayer.data.playback.usecase.MediaSessionAcquirer
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionHolder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

class MediaSessionAcquirerTest {

    private val mediaSessionHolder: MediaSessionHolder = mock()
    private val underTest = MediaSessionAcquirer(mediaSessionHolder)

    @Test
    fun opensMediaSessionOnCreate() {
        // When
        underTest.onCreate()

        // Then
        verify(mediaSessionHolder).openSession()
    }

    @Test
    fun closesMediaSessionOnDestroy() {
        // When
        underTest.onDestroy()

        // Then
        verify(mediaSessionHolder).closeSession()
    }
}
