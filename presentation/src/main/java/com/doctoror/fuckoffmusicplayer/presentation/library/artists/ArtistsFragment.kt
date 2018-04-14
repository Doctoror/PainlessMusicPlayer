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
package com.doctoror.fuckoffmusicplayer.presentation.library.artists

import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.artists.ArtistsProvider
import com.doctoror.fuckoffmusicplayer.presentation.Henson
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryListFragment2
import com.doctoror.fuckoffmusicplayer.presentation.library.artistalbums.ArtistAlbumsActivity
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ArtistsFragment : LibraryListFragment2() {

    @Inject
    lateinit var artistsProvider: ArtistsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun obtainConfig() = Config(
            canShowEmptyView = true,
            dataSource = { artistsProvider.load(it) },
            emptyMessage = getText(R.string.No_artists_found),
            recyclerAdapter = createRecyclerAdapter()
    )

    private fun createRecyclerAdapter(): ArtistsRecyclerAdapter {
        val activity = activity ?: throw IllegalStateException("Activity is null")

        val adapter = ArtistsRecyclerAdapter(activity)
        adapter.setOnArtistClickListener { position, artistId, artist ->
            this.onArtistClick(position, artistId, artist)
        }

        return adapter
    }

    private fun onArtistClick(
            position: Int,
            artistId: Long,
            artist: String?) {
        activity?.let {

            val intent = Henson.with(it).gotoArtistAlbumsActivity()
                    .artist(artist)
                    .artistId(artistId)
                    .build()

            var options: Bundle? = null
            val itemView = getItemView(position)
            if (itemView != null) {
                options = ActivityOptionsCompat.makeSceneTransitionAnimation(it, itemView,
                        ArtistAlbumsActivity.TRANSITION_NAME_ROOT).toBundle()
            }

            startActivity(intent, options)
        }
    }
}
