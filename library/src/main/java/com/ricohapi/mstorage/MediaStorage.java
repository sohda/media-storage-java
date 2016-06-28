//
//  Copyright (c) 2016 Ricoh Company, Ltd. All Rights Reserved.
//  See LICENSE for more information
//

package com.ricohapi.mstorage;

import com.ricohapi.auth.AuthClient;
import com.ricohapi.auth.CompletionHandler;
import com.ricohapi.auth.RicohAPIException;
import com.ricohapi.auth.Scope;
import com.ricohapi.auth.entity.AuthResult;
import com.ricohapi.mstorage.entity.MediaContent;
import com.ricohapi.mstorage.entity.MediaInfo;
import com.ricohapi.mstorage.entity.MediaList;
import com.ricohapi.mstorage.entity.MediaMeta;
import com.ricohapi.mstorage.response.RespList;

import net.arnx.jsonic.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MediaStorage {
    private AuthClient authClient;
    private String accessToken;

    private static final String ENDPOINT = "https://mss.ricohapi.com/v1/media";
    private static final String GET_CONTENT_PATH = "/content";
    private static final String GET_META_PATH = "/meta";

    public MediaStorage(AuthClient authClient) {
        this.authClient = authClient;
    }

    public void connect(final CompletionHandler<AuthResult> handler) {
        authClient.session(Scope.MSTORAGE, new CompletionHandler<AuthResult>() {
            @Override
            public void onCompleted(AuthResult result) {
                MediaStorage.this.accessToken = result.getAccessToken();
                handler.onCompleted(result);
            }

            @Override
            public void onThrowable(Throwable t) {
                handler.onThrowable(t);
            }
        });
    }

    public void upload(InputStream inputStream, CompletionHandler<MediaInfo> handler) {
        try {
            if (accessToken == null) {
                throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
            }
            RicohAPIRequest request = new RicohAPIRequest(ENDPOINT);
            Map<String, String> header = new HashMap<>();
            header.put("Authorization", "Bearer " + accessToken);
            header.put("Content-Type", "image/jpeg");
            request.upload(header, inputStream);

            if (request.isSucceeded()) {
                MediaInfo mediaInfo = JSON.decode(request.getResponseBody(), MediaInfo.class);
                handler.onCompleted(mediaInfo);
            } else {
                throw new RicohAPIException(request.getResponseCode(), request.getErrorBody());
            }
        } catch (IOException e) {
            handler.onThrowable(e);
        } catch (RicohAPIException e) {
            handler.onThrowable(e);
        }
    }

    public void download(String mediaId, CompletionHandler<MediaContent> handler) {
        try {
            if (accessToken == null) {
                throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
            }

            Map<String, String> header = new HashMap<>();
            header.put("Authorization", "Bearer " + accessToken);

            RicohAPIRequest request = new RicohAPIRequest(ENDPOINT + "/" + mediaId + GET_CONTENT_PATH);
            InputStream inputStream = request.download(header);

            if (request.isSucceeded()) {
                handler.onCompleted(new MediaContent(inputStream));
            } else {
                throw new RicohAPIException(request.getResponseCode(), request.getErrorBody());
            }

        } catch (RicohAPIException e) {
            handler.onThrowable(e);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void list(Map<String, String> params, CompletionHandler<MediaList> handler) {
        try {
            if (accessToken == null) {
                throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
            }
            RicohAPIRequest request;
            if (params == null || params.isEmpty()) {
                request = new RicohAPIRequest(ENDPOINT);
            } else {
                request = new RicohAPIRequest(ENDPOINT, params);
            }
            Map<String, String> header = new HashMap<>();
            header.put("Authorization", "Bearer " + accessToken);

            request.get(header);

            if (request.isSucceeded()) {
                RespList list = JSON.decode(request.getResponseBody(), RespList.class);
                handler.onCompleted(new MediaList(list.getMedia(), list.getPaging()));
            } else {
                throw new RicohAPIException(request.getResponseCode(), request.getErrorBody());
            }
        } catch (IOException e) {
            handler.onThrowable(e);
        } catch (RicohAPIException e) {
            handler.onThrowable(e);
        }
    }

    public void delete(String mediaId, CompletionHandler<Object> handler) {
        try {
            if (accessToken == null) {
                throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
            }
            RicohAPIRequest request = new RicohAPIRequest(ENDPOINT + "/" + mediaId);
            Map<String, String> header = new HashMap<>();
            header.put("Authorization", "Bearer " + accessToken);

            request.delete(header);

            if (request.isSucceeded()) {
                handler.onCompleted(new Object());
            } else {
                throw new RicohAPIException(request.getResponseCode(), request.getErrorBody());
            }
        } catch (IOException e) {
            handler.onThrowable(e);
        } catch (RicohAPIException e) {
            handler.onThrowable(e);
        }

    }


    public void info(String mediaId, CompletionHandler<MediaInfo> handler) {
        try {
            if (accessToken == null) {
                throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
            }
            RicohAPIRequest request = new RicohAPIRequest(ENDPOINT + "/" + mediaId);
            Map<String, String> header = new HashMap<>();
            header.put("Authorization", "Bearer " + accessToken);

            request.get(header);

            if (request.isSucceeded()) {
                MediaInfo mediaInfo = JSON.decode(request.getResponseBody(), MediaInfo.class);
                handler.onCompleted(mediaInfo);
            } else {
                throw new RicohAPIException(request.getResponseCode(), request.getErrorBody());
            }
        } catch (IOException e) {
            handler.onThrowable(e);
        } catch (RicohAPIException e) {
            handler.onThrowable(e);
        }
    }

    public void meta(String mediaId, CompletionHandler<MediaMeta> handler) {
        try {
            if (accessToken == null) {
                throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
            }
            RicohAPIRequest request = new RicohAPIRequest(ENDPOINT + "/" + mediaId + GET_META_PATH);

            Map<String, String> header = new HashMap<>();
            header.put("Authorization", "Bearer " + accessToken);

            request.get(header);

            if (request.isSucceeded()) {
                MediaMeta mediaMeta = JSON.decode(request.getResponseBody(), MediaMeta.class);
                handler.onCompleted(mediaMeta);
            } else {
                throw new RicohAPIException(request.getResponseCode(), request.getErrorBody());
            }
        } catch (IOException e) {
            handler.onThrowable(e);
        } catch (RicohAPIException e) {
            handler.onThrowable(e);
        }

    }
}
