/*
 * Copyright 2018 Thibault Seisel
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

package fr.nihilus.music.media

import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.LayoutRes
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.nihilus.music.media.extensions.*
import java.lang.ref.WeakReference

/**
 * Convert this [MediaMetadataCompat] into its [MediaDescriptionCompat] equivalent.
 * @param categories the Media ID to use a prefix for this item's Media ID
 * @param builder an optional builder for reuse
 * @return a media description created from this track metadata
 */
internal fun MediaMetadataCompat.asMediaDescription(
    builder: MediaDescriptionCompat.Builder,
    vararg categories: String
): MediaDescriptionCompat {
    val musicId = this.id
    val extras = Bundle(4)
    extras.putString(MediaItems.EXTRA_TITLE_KEY, this.titleKey)
    extras.putLong(MediaItems.EXTRA_DURATION, this.duration)
    extras.putLong(MediaItems.EXTRA_TRACK_NUMBER, this.trackNumber)
    extras.putLong(MediaItems.EXTRA_DISC_NUMBER, this.discNumber)

    val mediaId = mediaIdOf(*categories, musicId = musicId.toLong())
    builder.setMediaId(mediaId)
        .setTitle(this.title)
        .setSubtitle(this.artist)
        .setIconBitmap(this.albumArt)
        .setIconUri(this.albumArtUri)
        .setMediaUri(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon()
                .appendEncodedPath(musicId)
                .build()
        )
        .setExtras(extras)

    return builder.build()
}

/**
 * Inflate the given layout as a child of this view group.
 *
 * @receiver the view parent in which inflate this layout
 * @param resource id for an XML resource to load
 * @param attach whether the inflated layout should be attached to this view group.
 * If false, the view group will be used to create the correct layout params.
 * @return the root view of the inflated hierarchy. If [attach] is `true`,
 * this will be this view group, otherwise the root of the inflated XML file.
 */
fun ViewGroup.inflate(@LayoutRes resource: Int, attach: Boolean = false): View =
    LayoutInflater.from(context).inflate(resource, this, attach)

/**
 * Helper extension function to execute a block of instructions only if
 * weak reference to an object is still valid.
 */
inline fun <T> WeakReference<T>.doIfPresent(action: (T) -> Unit) {
    get()?.let(action)
}

inline fun MediaDescriptionCompat.copy(
    reWriter: MediaDescriptionCompat.Builder.() -> Unit
): MediaDescriptionCompat {
    val builder = MediaDescriptionCompat.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setDescription(description)
        .setIconUri(iconUri)
        .setMediaUri(mediaUri)
        .setIconBitmap(iconBitmap)
        .setExtras(extras)
    reWriter(builder)
    return builder.build()
}

/**
 * Helper extension function that creates an [android.util.Pair] the same way Kotlin does for
 * [kotlin.Pair].
 * @receiver The first object of the pair
 * @param other The second object of the pair
 * @return A pair made of this object and the one passed in parameter.
 */
infix fun <F, S> F.to(other: S): android.util.Pair<F, S> = Pair(this, other)