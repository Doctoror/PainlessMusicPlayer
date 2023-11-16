/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.presentation.util

object CollectionUtils {

    /**
     * Coverts List to ArrayList.
     *
     * If the [List] is already an [ArrayList] it will only cast it.
     * If the [List] is not an [ArrayList] will create an [ArrayList] which contains the input [List] elements.
     */
    fun <T> toArrayList(source: List<T>?): ArrayList<T>? {
        if (source is ArrayList<*>) {
            return source as ArrayList<T>?
        }
        return if (source != null) ArrayList(source) else null
    }
}
