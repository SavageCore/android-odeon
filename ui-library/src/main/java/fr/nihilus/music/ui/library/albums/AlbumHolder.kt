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

package fr.nihilus.music.ui.library.albums

import android.graphics.drawable.Drawable
import android.support.v4.media.MediaBrowserCompat
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.ImageViewTarget
import fr.nihilus.music.core.media.MediaItems
import fr.nihilus.music.core.ui.base.BaseHolder
import fr.nihilus.music.core.ui.glide.palette.AlbumArt
import fr.nihilus.music.core.ui.glide.palette.AlbumPalette
import fr.nihilus.music.ui.library.R

internal class AlbumHolder(
    parent: ViewGroup,
    private val glide: RequestBuilder<AlbumArt>,
    private val defaultPalette: AlbumPalette,
    private val isArtistAlbum: Boolean,
    onAlbumSelected: (position: Int) -> Unit
) : BaseHolder<MediaBrowserCompat.MediaItem>(parent, R.layout.album_grid_item) {

    private val card: CardView = itemView.findViewById(R.id.card)
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

        override fun onLoadFailed(errorDrawable: Drawable?) {
            super.onLoadFailed(errorDrawable)
            applyPalette(defaultPalette)
        }
    }

    inline val transitionView get() = albumArt

    init {
        itemView.setOnClickListener {
            onAlbumSelected(adapterPosition)
        }
    }

    private fun applyPalette(palette: AlbumPalette) {
        card.setCardBackgroundColor(palette.primary)
        title.setTextColor(palette.titleText)
        subtitle.setTextColor(palette.bodyText)
    }

    override fun bind(data: MediaBrowserCompat.MediaItem) {
        val description = data.description
        title.text = description.title

        subtitle.text = if (isArtistAlbum) {
            val trackNb = description.extras!!.getInt(MediaItems.EXTRA_NUMBER_OF_TRACKS)
            subtitle.resources.getQuantityString(R.plurals.number_of_tracks, trackNb, trackNb)
        } else {
            description.subtitle
        }

        glide.load(description.iconUri).into(albumViewTarget)
        albumArt.transitionName = description.mediaId
    }
}