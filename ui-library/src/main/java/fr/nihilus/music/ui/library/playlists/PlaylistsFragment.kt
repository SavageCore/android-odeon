/*
 * Copyright 2020 Thibault Seisel
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

package fr.nihilus.music.ui.library.playlists

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import fr.nihilus.music.core.ui.LoadRequest
import fr.nihilus.music.core.ui.ProgressTimeLatch
import fr.nihilus.music.core.ui.base.BaseFragment
import fr.nihilus.music.ui.library.HomeFragmentDirections
import fr.nihilus.music.ui.library.HomeViewModel
import fr.nihilus.music.ui.library.R
import kotlinx.android.synthetic.main.fragment_playlist.*

internal class PlaylistsFragment : BaseFragment(R.layout.fragment_playlist) {

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var adapter: PlaylistsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = PlaylistsAdapter(this, ::onPlaylistSelected)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressIndicator = view.findViewById<View>(R.id.progress_indicator)
        val progressBarLatch = ProgressTimeLatch { shouldShow ->
            progressIndicator.isVisible = shouldShow
        }

        playlist_recycler.adapter = adapter
        playlist_recycler.setHasFixedSize(true)

        viewModel.playlists.observe(viewLifecycleOwner) { playlistsRequest ->
            when (playlistsRequest) {
                is LoadRequest.Pending -> progressBarLatch.isRefreshing = true
                is LoadRequest.Success -> {
                    progressBarLatch.isRefreshing = false
                    adapter.submitList(playlistsRequest.data)
                    group_empty_view.isVisible = playlistsRequest.data.isEmpty()
                }
                is LoadRequest.Error -> {
                    progressBarLatch.isRefreshing = false
                    adapter.submitList(emptyList())
                    group_empty_view.isVisible = true
                }
            }
        }
    }

    private fun onPlaylistSelected(position: Int) {
        val selectedPlaylist = adapter.getItem(position)
        val toPlaylistTracks = HomeFragmentDirections.browsePlaylistContent(
            playlistId = selectedPlaylist.mediaId!!
        )

        findNavController().navigate(toPlaylistTracks)
    }
}
