/*
 * Copyright 2016 Kejun Xia
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

package com.shipdream.lib.android.mvc.service.dto;

public class VideoDTO extends VisualMediaDTO{
    private String album;
    private String artist;

    public String getAlbum(){
        return album;
    }

    public void setAlbum(String album){
        this.album = album;
    }

    public String getArtist(){
        return artist;
    }

    public void setArtist(String artist){
        this.artist = artist;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof VideoDTO)) return false;
        if (!super.equals(o)) return false;

        VideoDTO videoDTO = (VideoDTO) o;

        if (album != null ? !album.equals(videoDTO.album) : videoDTO.album != null) return false;
        return !(artist != null ? !artist.equals(videoDTO.artist) : videoDTO.artist != null);

    }

    @Override
    public int hashCode(){
        int result = super.hashCode();
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }
}
