/*
 * Copyright 2017 Thibault Seisel
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

package fr.nihilus.music.ui.albums

import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.bumptech.glide.RequestBuilder
import fr.nihilus.music.R
import fr.nihilus.music.glide.GlideApp
import fr.nihilus.music.glide.palette.PaletteBitmap
import fr.nihilus.music.ui.BaseAdapter
import fr.nihilus.music.ui.holder.AlbumHolder
import fr.nihilus.music.utils.MediaID
import fr.nihilus.music.utils.resolveThemeColor

internal class AlbumsAdapter(
        fragment: Fragment,
        private val listener: BaseAdapter.OnItemSelectedListener
) : BaseAdapter<AlbumHolder>() {

    private val glideRequest: RequestBuilder<PaletteBitmap>
    private val defaultColors: IntArray

    init {
        val ctx = checkNotNull(fragment.context) { "Fragment is not attached" }
        defaultColors = intArrayOf(
                ContextCompat.getColor(ctx, R.color.album_band_default),
                resolveThemeColor(ctx, R.attr.colorAccent),
                ContextCompat.getColor(ctx, android.R.color.white),
                ContextCompat.getColor(ctx, android.R.color.white)
        )

        val dummyAlbumArt = ContextCompat.getDrawable(ctx, R.drawable.ic_album_24dp)
        glideRequest = GlideApp.with(fragment).`as`(PaletteBitmap::class.java)
                .centerCrop()
                .error(dummyAlbumArt)
                .region(0f, .75f, 1f, 1f)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
        return AlbumHolder(parent, glideRequest, defaultColors).also { holder ->
            holder.onAttachListeners(listener)
        }
    }

    override fun getItemId(position: Int): Long {
        return if (hasStableIds()) {
            val item = items[position]
            MediaID.extractMusicID(item.mediaId)?.toLong() ?: RecyclerView.NO_ID
        } else RecyclerView.NO_ID
    }

}
