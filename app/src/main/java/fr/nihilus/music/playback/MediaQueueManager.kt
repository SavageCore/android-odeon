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

package fr.nihilus.music.playback

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import fr.nihilus.music.R
import fr.nihilus.music.assert
import fr.nihilus.music.command.MediaSessionCommand
import fr.nihilus.music.di.ServiceScoped
import fr.nihilus.music.media.repo.MusicRepository
import fr.nihilus.music.service.MusicService
import fr.nihilus.music.utils.MediaID
import javax.inject.Inject

@ServiceScoped
class MediaQueueManager
@Inject constructor(
        service: MusicService,
        private val repository: MusicRepository,
        private val player: ExoPlayer,
        private val commands: Map<String, @JvmSuppressWildcards MediaSessionCommand>
) : TimelineQueueNavigator(service.session), MediaSessionConnector.PlaybackPreparer {

    private val extractorsFactory = DefaultExtractorsFactory()
    private val dataSourceFactory: DataSource.Factory
    private val currentQueue = ArrayList<MediaDescriptionCompat>()

    init {
        val userAgent = Util.getUserAgent(service, service.getString(R.string.app_name))
        dataSourceFactory = DefaultDataSourceFactory(service, userAgent)
    }

    override fun getSupportedPrepareActions(): Long {
        // TODO Update supported action codes to include *_FROM_SEARCH
        return PlaybackStateCompat.ACTION_PREPARE or
                PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare() {
        // Should prepare playing the "current" media, which is the last played media id.
        // If not available, play all songs.
        // TODO Retrieve last playing Media ID
        onPrepareFromMediaId(MediaID.ID_MUSIC, null)
    }

    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        if (mediaId != null) {
            repository.getMediaItems(mediaId).subscribe { items ->
                currentQueue.clear()
                setupMediaSource(mediaId, items)
            }
        }
    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
        // Not supported at the time.
        throw UnsupportedOperationException()
    }

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
        TODO("Implement the searching API.")
    }

    override fun getMediaDescription(windowIndex: Int): MediaDescriptionCompat {
        assert(windowIndex in currentQueue.indices)

        // TODO Album art may be prefetch at this point
        return currentQueue[windowIndex]
    }

    override fun getCommands() = commands.keys.toTypedArray()

    override fun onCommand(player: Player?, command: String?, extras: Bundle?, cb: ResultReceiver?) {
        //
        commands[command]?.handle(extras, cb)
                ?: cb?.send(MediaSessionCommand.CODE_UNKNOWN_COMMAND, null)
    }

    private fun setupMediaSource(mediaId: String, queue: List<MediaBrowserCompat.MediaItem>) {
        val playableItems = queue.filterNot { it.isBrowsable }
                .mapTo(currentQueue) { it.description }

        val mediaSources = Array(playableItems.size) {
            val sourceUri = playableItems[it].mediaUri
                    ?: throw IllegalStateException("Every item should have an Uri.")
            ExtractorMediaSource(sourceUri, dataSourceFactory, extractorsFactory, null, null)
        }

        // Concatenate all media source to play them all in the same Timeline.
        val concatenatedSource = ConcatenatingMediaSource(*mediaSources)
        player.prepare(concatenatedSource)

        // Start at a given track if it is mentioned in the passed media id.
        val startIndex = indexOnQueue(playableItems, mediaId)
        if (startIndex != -1) {
            player.seekTo(startIndex, C.TIME_UNSET)
        }
    }
}

private fun indexOnQueue(queue: List<MediaDescriptionCompat>, mediaId: String) =
        queue.indexOfFirst { it.mediaId == mediaId }