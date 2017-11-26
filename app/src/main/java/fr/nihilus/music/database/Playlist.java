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

package fr.nihilus.music.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaDescriptionCompat;

import java.util.Date;

import fr.nihilus.music.utils.MediaID;

/**
 * A class that groups informations associated with a playlist.
 * Each playlist has an unique identifier that its {@link PlaylistTrack} children must reference
 * to be included.
 */
@Entity(tableName = "playlist",
        indices = {@Index(value = "title", unique = true)})
public class Playlist {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "date_created")
    private Date created;

    @ColumnInfo(name = "date_last_played")
    private Date lastPlayed;

    @ColumnInfo(name = "art_uri")
    private Uri artUri;

    public Playlist() {
        // Default empty constructor
    }

    public static Playlist create(CharSequence title) {
        Playlist newPlaylist = new Playlist();
        newPlaylist.setTitle(title.toString());
        newPlaylist.setCreated(new Date());
        return newPlaylist;
    }

    /**
     * Returns the unique identifier of this playlist.
     * If this playlist has not been saved, this identifier will be {@code null}.
     * @return unique identifier or {@code null}
     */
    @Nullable
    public Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    /**
     * @return The title given by the user to this playlist
     */
    public String getTitle() {
        return title;
    }

    /**
     * Rename this playlist.
     * @param title The new title of this playlist
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the date at which this playlist has been created
     */
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @return the date at which this playlist has been last played
     */
    public Date getLastPlayed() {
        return lastPlayed;
    }

    /**
     * Update the date a which this playlist has been played for the last time.
     * @param lastPlayed date of last play
     */
    public void setLastPlayed(Date lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    /**
     * @return an URI pointing to a Bitmap featuring this playlist
     */
    public Uri getArtUri() {
        return artUri;
    }

    /**
     * Sets an URI pointing to a Bitmap that features this playlist.
     * @param artUri URI pointing to a Bitmap resource
     */
    public void setArtUri(Uri artUri) {
        this.artUri = artUri;
    }

    public MediaDescriptionCompat asMediaDescription(MediaDescriptionCompat.Builder builder) {
        String mediaId = MediaID.createMediaID(null, MediaID.ID_PLAYLISTS, id.toString());
        return builder.setMediaId(mediaId)
                .setTitle(title)
                .setIconUri(artUri)
                .build();
    }
}
