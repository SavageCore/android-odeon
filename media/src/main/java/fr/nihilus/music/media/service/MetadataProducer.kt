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

package fr.nihilus.music.media.service

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import fr.nihilus.music.media.MediaItems
import fr.nihilus.music.media.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach

/**
 * The maximum width/height for the icon of the currently playing metadata.
 */
private const val ICON_MAX_SIZE = 320

/**
 * Starts a coroutine that transforms received queue items into [MediaMetadataCompat] instances
 * suitable for being set as the MediaSession's current metadata.
 *
 * If an item is received while the previous one is being processed,
 * then its processing is cancelled and the newly received item is processed instead.
 * This prevents from wasting resource preparing metadata for an item that is no longer valid.
 *
 * @param downloader The downloader used to load icons associated with each metadata.
 * @param metadata Channel to which metadata should be sent when ready.
 */
@ExperimentalMediaApi
@ObsoleteCoroutinesApi
internal fun CoroutineScope.metadataProducer(
    downloader: IconDownloader,
    metadata: SendChannel<MediaMetadataCompat>
): SendChannel<MediaDescriptionCompat> = actor(
    capacity = Channel.CONFLATED,
    start = CoroutineStart.LAZY
) {
    var currentlyPlayingItem = receive()
    var updateJob = scheduleMetadataUpdate(downloader, currentlyPlayingItem, metadata)

    consumeEach { description ->
        if (currentlyPlayingItem.mediaId != description.mediaId) {
            currentlyPlayingItem = description

            updateJob.cancel()
            updateJob = scheduleMetadataUpdate(downloader, description, metadata)
        }
    }
}

/**
 * Extract track metadata from the provided [media description][description] and load its icon.
 * The launched coroutine supports cancellation.
 *
 * @param downloader The downloader used to load the icon associated with the metadata.
 * @param description The description of the media from which metadata should be extracted.
 * @param output Send metadata to this channel when ready.
 */
private fun CoroutineScope.scheduleMetadataUpdate(
    downloader: IconDownloader,
    description: MediaDescriptionCompat,
    output: SendChannel<MediaMetadataCompat>
): Job = launch {
    val trackIcon = description.iconUri?.let {
        downloader.loadBitmap(it, ICON_MAX_SIZE, ICON_MAX_SIZE)
    }

    val metadata = MediaMetadataCompat.Builder().apply {
        id = checkNotNull(description.mediaId)
        displayTitle = description.title?.toString()
        displaySubtitle = description.subtitle?.toString()
        displayDescription = description.description?.toString()
        displayIconUri = description.iconUri?.toString()
        albumArt = trackIcon

        description.extras?.let {
            duration = it.getLong(MediaItems.EXTRA_DURATION, -1L)
            discNumber = it.getInt(MediaItems.EXTRA_DISC_NUMBER, 1).toLong()
            trackNumber = it.getInt(MediaItems.EXTRA_TRACK_NUMBER, 0).toLong()
        }
    }.build()

    output.send(metadata)
}