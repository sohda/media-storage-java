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
import com.ricohapi.mstorage.response.RespMeta;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaStorage {
    private AuthClient authClient;

    private static final String ENDPOINT = "https://mss.ricohapi.com/v1/media";
    private static final String SEARCH_PATH = "/search";
    private static final String GET_CONTENT_PATH = "/content";
    private static final String GET_META_PATH = "/meta";
    private static final String USER_META_PATH = "/meta/user";
    private static final String REPLACE_USER_META_REGEX = "^user\\.([A-Za-z0-9_\\-]{1,256})$";
    private static final Integer FIRST = 1;
    private static final Integer MAX_USER_META_LENGTH = 1024;
    private static final Integer MIN_USER_META_LENGTH = 1;

    public static final String LIST_PARAM_KEY_FILTER = "filter";
    public static final String META_EXIF = "exif";
    public static final String META_GPANO = "gpano";
    public static final String META_USER = "user";

    public MediaStorage(AuthClient authClient) {
        this.authClient = authClient;
    }

    public void connect(final CompletionHandler<AuthResult> handler) {
        authClient.session(Scope.MSTORAGE, new CompletionHandler<AuthResult>() {
            @Override
            public void onCompleted(AuthResult result) {
                handler.onCompleted(result);
            }

            @Override
            public void onThrowable(Throwable t) {
                handler.onThrowable(t);
            }
        });
    }

    public void upload(final InputStream inputStream, final CompletionHandler<MediaInfo> handler) {
        authClient.getAccessToken(new CompletionHandler<AuthResult>(){
            @Override
            public void onCompleted(AuthResult result) {
                try {
                    if (result.getAccessToken() == null) {
                        throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
                    }
                    RicohAPIRequest request = new RicohAPIRequest(ENDPOINT);
                    Map<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + result.getAccessToken());
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

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                handler.onThrowable(t);
            }
        });
    }

    public void download(final String mediaId, final CompletionHandler<MediaContent> handler) {
        authClient.getAccessToken(new CompletionHandler<AuthResult>(){
            @Override
            public void onCompleted(AuthResult result) {
                try {
                    if (result.getAccessToken() == null) {
                        throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
                    }

                    Map<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + result.getAccessToken());

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

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                handler.onThrowable(t);
            }
        });
    }

    public void list(final Map<String, ?> params, final CompletionHandler<MediaList> handler) {
        authClient.getAccessToken(new CompletionHandler<AuthResult>(){
            @Override
            public void onCompleted(AuthResult result) {
                try {
                    if (result.getAccessToken() == null) {
                        throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
                    }
                    RicohAPIRequest request;
                    Map<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + result.getAccessToken());
                    if (params == null || params.isEmpty()) {
                        // GET /media
                        request = new RicohAPIRequest(ENDPOINT);
                        request.get(header);
                    } else {
                        if (!params.containsKey(LIST_PARAM_KEY_FILTER) || params.get(LIST_PARAM_KEY_FILTER) == null) {
                            // GET /media
                            request = new RicohAPIRequest(ENDPOINT, params);
                            request.get(header);
                        } else {
                            // POST /media/search
                            Map<String, Object> searchParams = new HashMap<>();
                            searchParams.put("search_version", "2016-07-08");
                            Map<String, Object> paging = new HashMap<>();
                            for (String key : params.keySet()) {
                                if (LIST_PARAM_KEY_FILTER.equals(key)) {
                                    searchParams.put("query", params.get(LIST_PARAM_KEY_FILTER));
                                } else {
                                    paging.put(key, params.get(key));
                                }
                            }
                            searchParams.put("paging", paging);

                            request = new RicohAPIRequest(ENDPOINT + SEARCH_PATH);
                            request.post(header, searchParams, RicohAPIRequest.ParamType.JSON);
                        }
                    }

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

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                handler.onThrowable(t);
            }
        });
    }

    public void delete(final String mediaId, final CompletionHandler<Object> handler) {
        authClient.getAccessToken(new CompletionHandler<AuthResult>(){
            @Override
            public void onCompleted(AuthResult result) {
                try {
                    if (result.getAccessToken() == null) {
                        throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
                    }
                    RicohAPIRequest request = new RicohAPIRequest(ENDPOINT + "/" + mediaId);
                    Map<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + result.getAccessToken());

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

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                handler.onThrowable(t);
            }
        });
    }

    public void info(final String mediaId, final CompletionHandler<MediaInfo> handler) {
        authClient.getAccessToken(new CompletionHandler<AuthResult>(){
            @Override
            public void onCompleted(AuthResult result) {
                try {
                    if (result.getAccessToken() == null) {
                        throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
                    }
                    RicohAPIRequest request = new RicohAPIRequest(ENDPOINT + "/" + mediaId);
                    Map<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + result.getAccessToken());

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

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                handler.onThrowable(t);
            }
        });
    }

    public void meta(final String mediaId, final CompletionHandler<MediaMeta> handler) {
        authClient.getAccessToken(new CompletionHandler<AuthResult>(){
            @Override
            public void onCompleted(AuthResult result) {
                try {
                    if (result.getAccessToken() == null) {
                        throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
                    }
                    RicohAPIRequest request = new RicohAPIRequest(ENDPOINT + "/" + mediaId + GET_META_PATH);

                    Map<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + result.getAccessToken());

                    request.get(header);

                    if (request.isSucceeded()) {
                        RespMeta respMeta = JSON.decode(request.getResponseBody(), RespMeta.class);
                        handler.onCompleted(new MediaMeta(respMeta));
                    } else {
                        throw new RicohAPIException(request.getResponseCode(), request.getErrorBody());
                    }
                } catch (IOException e) {
                    handler.onThrowable(e);
                } catch (RicohAPIException e) {
                    handler.onThrowable(e);
                }
            }

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                handler.onThrowable(t);
            }
        });
    }

    public void meta(final String mediaId, final String fieldName, final CompletionHandler<Map<String, String>> handler) {
        authClient.getAccessToken(new CompletionHandler<AuthResult>(){
            @Override
            public void onCompleted(AuthResult result) {
                try {
                    if (result.getAccessToken() == null) {
                        throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
                    }
                    Map<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + result.getAccessToken());
                    if (fieldName == null) {
                        throw new RicohAPIException(0, "invalid fieldName: null");
                    } else if (META_EXIF.equals(fieldName) || META_GPANO.equals(fieldName) || META_USER.equals(fieldName)) {
                        // GET /media/{id}/meta/exif, /media/{id}/meta/gpano, /media/{id}/meta/user
                        RicohAPIRequest request = new RicohAPIRequest(ENDPOINT + "/" + mediaId + GET_META_PATH + "/" + fieldName);
                        request.get(header);

                        if (request.isSucceeded()) {
                            Map<String, String> respMap = JSON.decode(request.getResponseBody(), new TypeReference<Map<String, String>>() {});
                            handler.onCompleted(respMap);
                        } else {
                            throw new RicohAPIException(request.getResponseCode(), request.getErrorBody());
                        }
                    } else {
                        String userMetaKey = replaceUserMeta(fieldName);
                        if (userMetaKey == null) {
                            throw new RicohAPIException(0, "invalid fieldName: " + fieldName);
                        }
                        // GET /media/{id}/meta/user/{key}
                        RicohAPIRequest request = new RicohAPIRequest(ENDPOINT + "/" + mediaId + USER_META_PATH + "/" + userMetaKey);
                        request.get(header);

                        if (request.isSucceeded()) {
                            Map<String, String> respMap = new HashMap<>();
                            respMap.put(userMetaKey, request.getResponseBody());
                            handler.onCompleted(respMap);
                        } else {
                            throw new RicohAPIException(request.getResponseCode(), request.getErrorBody());
                        }
                    }
                } catch (IOException e) {
                    handler.onThrowable(e);
                } catch (RicohAPIException e) {
                    handler.onThrowable(e);
                }
            }

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                handler.onThrowable(t);
            }
        });
    }

    public void addMeta(final String mediaId, final Map<String, String> userMeta, final CompletionHandler<Object> handler) {
        authClient.getAccessToken(new CompletionHandler<AuthResult>(){
            @Override
            public void onCompleted(AuthResult result) {
                try {
                    if (result.getAccessToken() == null) {
                        throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
                    }
                    RicohAPIRequest request;

                    for(String userMetaKey : userMeta.keySet()) {
                        String requestUserMetaKey = replaceUserMeta(userMetaKey);
                        String value = userMeta.get(userMetaKey);

                        if (requestUserMetaKey == null || !isValidValue(value)) {
                            throw new RicohAPIException(0, "invalid parameter: " + "{" + userMetaKey + "=" + value + "}");
                        }
                        request = new RicohAPIRequest(ENDPOINT + "/" + mediaId + USER_META_PATH + "/" + requestUserMetaKey);
                        Map<String, String> header = new HashMap<>();
                        header.put("Authorization", "Bearer " + result.getAccessToken());
                        header.put("Content-Type", "text/plain");

                        request.put(header, value);

                        if (request.isSucceeded()) {
                            handler.onCompleted(new Object());
                        } else {
                            throw new RicohAPIException(request.getResponseCode(), request.getErrorBody());
                        }
                    }
                } catch (IOException e) {
                    handler.onThrowable(e);
                } catch (RicohAPIException e) {
                    handler.onThrowable(e);
                }
            }

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                handler.onThrowable(t);
            }
        });
    }

    public void removeMeta(final String mediaId, final String key, final CompletionHandler<Object> handler) {
        authClient.getAccessToken(new CompletionHandler<AuthResult>(){
            @Override
            public void onCompleted(AuthResult result) {
                try {
                    if (result.getAccessToken() == null) {
                        throw new RicohAPIException(0, "wrong usage: use the connect method to get an access token.");
                    }
                    RicohAPIRequest request;
                    if (key == null) {
                        throw new RicohAPIException(0, "invalid parameter: null");
                    } else if (META_USER.equals(key)) {
                        // DELETE /media/{id}/meta/user
                        request = new RicohAPIRequest(ENDPOINT + "/" + mediaId + USER_META_PATH);
                    } else {
                        String userMetaKey = replaceUserMeta(key);
                        if (userMetaKey == null) {
                            throw new RicohAPIException(0, "invalid parameter: " + key);
                        }
                        // DELETE /media/{id}/meta/user/{key}
                        request = new RicohAPIRequest(ENDPOINT + "/" + mediaId + USER_META_PATH + "/" + userMetaKey);
                    }
                    Map<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + result.getAccessToken());

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

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                handler.onThrowable(t);
            }
        });
    }

    private String replaceUserMeta(String userMeta){
        Pattern pattern = Pattern.compile(REPLACE_USER_META_REGEX);
        Matcher match = pattern.matcher(userMeta);

        return match.matches() ? match.group(FIRST) : null ;
    }

    private boolean isValidValue(String value) {
        return (value.length() >= MIN_USER_META_LENGTH && value.length() <= MAX_USER_META_LENGTH);
    }
}
