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
package com.doctoror.fuckoffmusicplayer.data.media.compat

import android.support.annotation.IntDef
import kotlin.annotation.AnnotationRetention.SOURCE

const val USAGE_MEDIA = 1
const val CONTENT_TYPE_MUSIC = 2

@IntDef(value = [USAGE_MEDIA])
@Retention(SOURCE)
annotation class Usage

@IntDef(value = [CONTENT_TYPE_MUSIC])
@Retention(SOURCE)
annotation class ContentType

data class AudioAttributesCompat(
        @Usage val usage: Int,
        @ContentType val contentType: Int,
        val legacyStreamType: Int)
