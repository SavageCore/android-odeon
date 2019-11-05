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

package fr.nihilus.music.library.albums

import android.content.res.ColorStateList
import android.support.v4.media.MediaBrowserCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.ImageViewTarget
import fr.nihilus.music.R
import fr.nihilus.music.core.media.MediaItems
import fr.nihilus.music.glide.palette.AlbumArt
import fr.nihilus.music.ui.BaseAdapter

internal class AlbumHolder(
    parent: ViewGroup,
    private val glide: RequestBuilder<AlbumArt>,
    private val defaultPalette: AlbumPalette,
    private val isArtistAlbum: Boolean
) : BaseAdapter.ViewHolder(parent, R.layout.album_grid_item) {

    private val card: CardView = itemView.findViewById(R.id.card)
    private val textScrim: View = itemView.findViewById(R.id.text_scrim)
    private val albumArt: ImageView = itemView.findViewById(R.id.album_art_view)
    private val title: TextView = itemView.findViewById(R.id.title)
    private val subtitle: TextView = itemView.findViewById(R.id.artist)

    private val albumViewTarget = object : ImageViewTarget<AlbumArt>(albumArt) {

        override fun setResource(resource: AlbumArt?) {
            if (resource != null) {
                applyPalette(resource.palette)
                super.view.setImageBitmap(resource.bitmap)
            }
        }
    }

    inline val transitionView get() = albumArt

    private fun applyPalette(palette: AlbumPalette) {
        card.setCardBackgroundColor(palette.primary)
        textScrim.backgroundTintList = ColorStateList.valueOf(palette.primary)
        title.setTextColor(palette.bodyText)
        subtitle.setTextColor(palette.bodyText)
    }

    override fun onAttachListeners(client: BaseAdapter.OnItemSelectedListener) {
        itemView.setOnClickListener {
            client.onItemSelected(adapterPosition, R.id.action_browse_item)
        }
    }

    override fun onBind(item: MediaBrowserCompat.MediaItem) {
        val description = item.description
        title.text = description.title

        subtitle.text = if (isArtistAlbum) {
            val trackNb = description.extras!!.getInt(MediaItems.EXTRA_NUMBER_OF_TRACKS)
            subtitle.resources.getQuantityString(R.plurals.number_of_tracks, trackNb, trackNb)
        } else {
            description.subtitle
        }

        applyPalette(defaultPalette)

        glide.load(description.iconUri).into(albumViewTarget)
        albumArt.transitionName = description.mediaId
    }
}