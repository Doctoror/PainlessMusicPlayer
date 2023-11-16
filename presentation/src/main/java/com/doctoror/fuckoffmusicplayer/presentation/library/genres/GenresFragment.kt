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
package com.doctoror.fuckoffmusicplayer.presentation.library.genres

import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.genres.GenresProvider
import com.doctoror.fuckoffmusicplayer.presentation.Henson
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryListFragment
import com.doctoror.fuckoffmusicplayer.presentation.library.genrealbums.GenreAlbumsActivity
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class GenresFragment : LibraryListFragment() {

    @Inject
    lateinit var genresProvider: GenresProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun obtainConfig() = Config(
            canShowEmptyView = true,
            dataSource = { genresProvider.load(it) },
            emptyMessage = getText(R.string.No_genres_found),
            recyclerAdapter = createRecyclerAdapter()
    )

    private fun createRecyclerAdapter(): GenresRecyclerAdapter {
        val context = context ?: throw IllegalStateException("Context is null")

        val adapter = GenresRecyclerAdapter(context)
        adapter.setOnGenreClickListener { position, genreId, genre ->
            onGenreClick(position, genreId, genre)
        }
        return adapter
    }

    private fun onGenreClick(position: Int, genreId: Long,
                             genre: String?) {
        activity?.let {
            val intent = Henson.with(it).gotoGenreAlbumsActivity()
                    .genre(genre)
                    .genreId(genreId)
                    .build()

            var options: Bundle? = null
            val itemView = getItemView(position)
            if (itemView != null) {
                options = ActivityOptionsCompat.makeSceneTransitionAnimation(it, itemView,
                        GenreAlbumsActivity.TRANSITION_NAME_ROOT).toBundle()
            }

            startActivity(intent, options)
        }
    }
}
