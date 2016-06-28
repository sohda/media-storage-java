//
//  Copyright (c) 2016 Ricoh Company, Ltd. All Rights Reserved.
//  See LICENSE for more information
//

package com.ricohapi.mstorage.entity;

import java.util.Map;

public class MediaMeta {

    private Map<String, String> exif;
    private Map<String, String> gpano;

    public Map<String, String> getExif() {
        return exif;
    }

    public void setExif(Map<String, String> exif) { this.exif = exif; }

    public Map<String, String> getGpano() {
        return gpano;
    }

    public void setGpano(Map<String, String> gpano) { this.gpano = gpano; }
}
