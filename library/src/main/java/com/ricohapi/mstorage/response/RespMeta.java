//
//  Copyright (c) 2016 Ricoh Company, Ltd. All Rights Reserved.
//  See LICENSE for more information
//

package com.ricohapi.mstorage.response;

import java.util.Map;

public class RespMeta {
    private Map<String, String> exif;
    private Map<String, String> gpano;
    private Map<String, String> user;

    public Map<String, String> getExif() {
        return exif;
    }

    public void setExif(Map<String, String> exif) { this.exif = exif; }

    public Map<String, String> getGpano() {
        return gpano;
    }

    public void setGpano(Map<String, String> gpano) { this.gpano = gpano; }

    public Map<String, String> getUser() {
        return user;
    }

    public void setUser(Map<String, String> user) { this.user = user; }
}
