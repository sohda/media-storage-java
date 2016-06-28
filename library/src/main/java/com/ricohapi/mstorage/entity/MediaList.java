//
//  Copyright (c) 2016 Ricoh Company, Ltd. All Rights Reserved.
//  See LICENSE for more information
//

package com.ricohapi.mstorage.entity;

import java.util.List;

public class MediaList {

    private List<MediaIndex> mediaList;
    private Paging paging;

    public MediaList(List<MediaIndex> mediaList, Paging paging) {
        this.mediaList = mediaList;
        this.paging = paging;
    }


    public List<MediaIndex> getMediaList() {
        return mediaList;
    }

    public void setMediaList (List<MediaIndex> mediaList) { this.mediaList = mediaList; }

    public Paging getPaging() {
        return paging;
    }

    public void setPaging (Paging paging) { this.paging = paging; }
}
