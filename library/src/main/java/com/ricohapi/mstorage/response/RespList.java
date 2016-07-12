//
//  Copyright (c) 2016 Ricoh Company, Ltd. All Rights Reserved.
//  See LICENSE for more information
//

package com.ricohapi.mstorage.response;

import com.ricohapi.mstorage.entity.MediaIndex;
import com.ricohapi.mstorage.entity.Paging;

import java.util.List;

public class RespList {
    private List<MediaIndex> media;
    private Paging paging;

    public List<MediaIndex> getMedia() {
        return media;
    }

    public void setMedia(List<MediaIndex> media) {
        this.media = media;
    }

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

}
