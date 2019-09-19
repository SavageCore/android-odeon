/*
 * Copyright 2019 Thibault Seisel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.nihilus.music.library.search

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import fr.nihilus.music.R
import fr.nihilus.music.common.media.MediaId
import fr.nihilus.music.common.media.toMediaId
import fr.nihilus.music.core.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : BaseFragment(R.layout.fragment_search) {
    private val viewModel by viewModels<SearchViewModel> { viewModelFactory }

    private lateinit var resultsAdapter: SearchResultsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = list_search_results
        recyclerView.setHasFixedSize(true)
        resultsAdapter = SearchResultsAdapter(this, ::onSuggestionSelected)
        recyclerView.adapter = resultsAdapter

        with(search_toolbar) {
            setNavigationOnClickListener { findNavController().navigateUp() }
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }

        search_input.requestFocus()
        search_input.doAfterTextChanged { text ->
            viewModel.search(text ?: "")
        }

        search_input.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    viewModel.search(v.text ?: "")
                    true
                }

                else -> false
            }
        }

        viewModel.searchResults.observe(this) { searchResults ->
            resultsAdapter.submitList(searchResults)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_clear -> {
            search_input.text = null
            search_input.requestFocus()
            resultsAdapter.submitList(emptyList())
            true
        }

        else -> false
    }

    private fun onSuggestionSelected(item: MediaBrowserCompat.MediaItem) {
        when {
            item.isBrowsable -> browseMedia(item)
            item.isPlayable -> viewModel.play(item)
        }
    }

    private fun browseMedia(item: MediaBrowserCompat.MediaItem) {
        val navController = findNavController()
        val (type, _, _) = item.mediaId.toMediaId()

        when (type) {
            MediaId.TYPE_ALBUMS -> {
                val toAlbumDetail = SearchFragmentDirections.browseAlbumDetail(item, null)
                navController.navigate(toAlbumDetail)
            }

            MediaId.TYPE_ARTISTS -> {
                val toArtistDetail = SearchFragmentDirections.browseArtistDetail(item)
                navController.navigate(toArtistDetail)
            }

            MediaId.TYPE_PLAYLISTS -> {
                val toPlaylistContent = SearchFragmentDirections.browsePlaylistContent(item)
                navController.navigate(toPlaylistContent)
            }
        }
    }
}