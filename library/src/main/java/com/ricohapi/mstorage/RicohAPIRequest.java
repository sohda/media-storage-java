//
//  Copyright (c) 2016 Ricoh Company, Ltd. All Rights Reserved.
//  See LICENSE for more information
//

package com.ricohapi.mstorage;

import net.arnx.jsonic.JSON;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RicohAPIRequest {
    private HttpURLConnection connection;
    private URL url;
    private String responseBody = null;
    private String errorBody = null;

    public enum ParamType {
        URLENCODE,
        JSON
    }

    public RicohAPIRequest(String urlString) throws IOException {
        this.url = new URL(urlString);
        connection = (HttpURLConnection)url.openConnection();
    }

    public RicohAPIRequest(String urlString, Map<String, ?> queryParams) throws IOException {
        this.url = new URL(urlString + "?" + joinParameters(queryParams, ParamType.URLENCODE));
        connection = (HttpURLConnection)url.openConnection();
    }

    public void get(Map<String, String> header) throws IOException {
        connection.setRequestMethod("GET");
        connection.setDoOutput(false);
        for (String key : header.keySet()) {
            connection.setRequestProperty(key, header.get(key));
        }
    }

    public InputStream download(Map<String, String> header) throws IOException {
        connection.setRequestMethod("GET");
        connection.setDoOutput(false);
        for (String key : header.keySet()) {
            connection.setRequestProperty(key, header.get(key));
        }
        return connection.getInputStream();
    }

    public void post(Map<String, String> header, Map<String, ?> params, ParamType paramType) throws IOException {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        for (String key : header.keySet()) {
            connection.setRequestProperty(key, header.get(key));
        }
        if (ParamType.JSON == paramType) {
            connection.setRequestProperty("Content-Type", "application/json");
        }
        DataOutputStream os = new DataOutputStream(connection.getOutputStream());
        os.writeBytes(joinParameters(params, paramType));
    }

    public void delete(Map<String, String> header) throws IOException {
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(false);
        for (String key : header.keySet()) {
            connection.setRequestProperty(key, header.get(key));
        }
    }

    public void upload(Map<String, String> header, InputStream inputStream) throws IOException {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        for (String key : header.keySet()) {
            connection.setRequestProperty(key, header.get(key));
        }
        DataOutputStream os = new DataOutputStream(connection.getOutputStream());
        byte[] bytes = new byte[1024];
        while (true) {
            int read = inputStream.read(bytes);
            if (read <= 0) return;
            os.write(bytes, 0, read);
            os.flush();
        }
    }

    public void put(Map<String, String> header, String value) throws IOException {
        connection.setRequestMethod("PUT");
        for (String key : header.keySet()) {
            connection.setRequestProperty(key, header.get(key));
        }

        connection.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        out.write(value);
        out.flush();
        out.close();
    }

    public String getResponseBody() throws IOException {
        if (this.responseBody == null) {
            this.responseBody = getBody(connection.getInputStream());
        }
        return this.responseBody;
    }

    public String getErrorBody() throws IOException {
        if (this.errorBody == null) {
            this.errorBody = getBody(connection.getErrorStream());
        }
        return this.errorBody;
    }

    private static String getBody(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

    private static String joinParameters(Map<String, ?> paramMap, ParamType paramType) {
        switch (paramType) {
            case URLENCODE:
                List<String> paramList = new ArrayList<>();
                for (String key : paramMap.keySet()) {
                    Object value = paramMap.get(key);
                    paramList.add(StringUtils.join(new String[]{key, value.toString()}, "="));
                }
                return StringUtils.join(paramList, "&");
            case JSON:
                return JSON.encode(paramMap);
            default:
                throw new IllegalArgumentException("Invalid paramType.");
        }
    }

    public boolean isSucceeded() throws IOException {
        int responseCode = connection.getResponseCode();
        return responseCode >= 200 && responseCode < 300;
    }

    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

}