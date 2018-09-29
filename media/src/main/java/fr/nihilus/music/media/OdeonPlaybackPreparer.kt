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

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.ShuffleOrder
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import fr.nihilus.music.media.command.MediaSessionCommand
import fr.nihilus.music.media.playback.AudioOnlyExtractorsFactory
import fr.nihilus.music.media.repo.MusicRepository
import fr.nihilus.music.media.service.MusicService
import javax.inject.Inject

/**
 * Handle requests to prepare media that can be played from the Odeon Media Player.
 * This fetches media information from the music library.
 */
internal class OdeonPlaybackPreparer
@Inject constructor(
    service: MusicService,
    private val player: ExoPlayer,
    private val repository: MusicRepository,
    private val settings: MediaSettings,
    private val commandHandlers: Map<String, @JvmSuppressWildcards MediaSessionCommand>
) : MediaSessionConnector.PlaybackPreparer {

    private val audioOnlyExtractors = AudioOnlyExtractorsFactory()
    private val appDataSourceFactory = DefaultDataSourceFactory(
        service,
        Util.getUserAgent(service, service.getString(R.string.app_name))
    )

    override fun getSupportedPrepareActions(): Long {
        // TODO Update supported action codes to include *_FROM_SEARCH
        return PlaybackStateCompat.ACTION_PREPARE or
                PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    /**
     * Handles generic requests to prepare playback.
     *
     * This prepares the last played queue, in the same order as the last time it was played.
     * If it is the first time a queue is built, this prepares to play all tracks.
     *
     * @see MediaSessionCompat.Callback.onPrepare
     */
    override fun onPrepare() {
        // Should prepare playing the "current" media, which is the last played media id.
        // If not available, play all songs.
        val lastPlayedMediaId = settings.lastPlayedMediaId ?: CATEGORY_MUSIC
        prepareFromMediaId(lastPlayedMediaId)
    }

    /**
     * Handles requests to prepare for playing a specific mediaId.
     *
     * This will built a new queue even if the current media id is the same, shuffling tracks
     * in a different order.
     * If no media id is provided, the player will be prepared to play all tracks.
     *
     * @param mediaId The media id of the track of set of tracks to prepare.
     * @param extras Optional parameters describing how the queue should be prepared.
     *
     * @see MediaSessionCompat.Callback.onPrepareFromMediaId
     */
    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        // A new queue has been requested. Increment the queue identifier.
        settings.queueCounter++
        prepareFromMediaId(mediaId ?: CATEGORY_MUSIC)
    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
        // Not supported at the time.
        throw UnsupportedOperationException()
    }

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
        // TODO: Implement searching for Google Assistant
        throw UnsupportedOperationException()
    }

    override fun getCommands(): Array<String> = commandHandlers.keys.toTypedArray()

    override fun onCommand(player: Player?, command: String?, extras: Bundle?, cb: ResultReceiver?) {
        commandHandlers[command]?.handle(extras, cb)
                ?: cb?.send(R.id.abc_error_unknown_command, null)
    }

    private fun prepareFromMediaId(mediaId: String) {
        repository.getMediaItems(mediaId).subscribe { items ->
            val playableItems = items.filterNot { it.isBrowsable }.map { it.description }
            val mediaSources = Array(playableItems.size) {
                val playableItem: MediaDescriptionCompat = playableItems[it]
                val sourceUri = checkNotNull(playableItem.mediaUri) {
                    "Every item should have an Uri."
                }

                ExtractorMediaSource.Factory(appDataSourceFactory)
                    .setExtractorsFactory(audioOnlyExtractors)
                    .setTag(playableItem)
                    .createMediaSource(sourceUri)
            }

            // Defines a shuffle order for the loaded media sources that is predictable.
            // It depends on the number of time a new queue has been built.
            val predictableShuffleOrder = ShuffleOrder.DefaultShuffleOrder(
                mediaSources.size,
                settings.queueCounter
            )

            // Concatenate all media source to play them all in the same Timeline.
            val concatenatedSource = ConcatenatingMediaSource(/* TODO Test with true */false,
                predictableShuffleOrder,
                *mediaSources
            )

            player.prepare(concatenatedSource)

            // Start at a given track if it is mentioned in the passed media id.
            val startIndex = playableItems.indexOfFirst { it.mediaId == mediaId }
            if (startIndex != -1) {
                player.seekTo(startIndex, C.TIME_UNSET)
            }
        }
    }
}