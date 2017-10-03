package fr.nihilus.music.media.builtin

import android.support.v4.media.MediaBrowserCompat
import io.reactivex.Single

/**
 * A static media item defined by the application.
 *
 * Built-in items should be associated with a unique media id that must not change once assigned.
 * Most of the time, built-in items are attached to the root of the media browser
 * and their children have a specific id.
 */
interface BuiltinItem {
    /**
     * Returns a representation of this item as a media item used for browsing.
     *
     * The description associated with the media item must be static,
     * for example stored as resources. Note that this media item *might* be playable;
     * but since it represents a built-in element, most of the time it is only browsable.
     *
     * @return a media item representing this built-in item
     */
    fun asMediaItem(): MediaBrowserCompat.MediaItem

    /**
     * Provides the children media items of this built-in element.
     *
     * Those children media id must be composed of the same root media id
     * plus an extra part specific to each child, except if children are built-in items themselves.
     * In this case the have their own root id.
     *
     * @return an observable list of media item children of this built-in item
     */
    fun getChildren(): Single<List<MediaBrowserCompat.MediaItem>>
}