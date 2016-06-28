# Ricoh Media Storage for Java

This open-source library allows you to integrate Ricoh Media Storage into your Android app.

Learn more at http://docs.ricohapi.com/

## Requirements

* java 1.8+

You'll also need

* Ricoh API Client Credentials (client_id & client_secret)
* Ricoh ID (user_id & password)

If you don't have them, please register yourself and your client from [THETA Developers Website](http://contest.theta360.com/).

## Installation
This section shows you to install Ricoh Media Storage for Java in your application.  
See [Media Storage Sample](https://github.com/ricohapi/media-storage-java/tree/master/sample#media-storage-sample) to try out a sample of Ricoh Media Storage for Java.

* Download: [`ricoh-api-mstorage.jar`](https://github.com/ricohapi/media-storage-java/blob/v1.0.0/lib/ricoh-api-mstorage.jar?raw=true)
* Drag `ricoh-api-mstorage.jar` into a directory (ex. `libs`) of your application.
* Edit your application's `build.gradle` as follows.
```java
dependencies {
    compile files('libs/ricoh-api-mstorage.jar')
}
```
* Click the `Sync Project with Gradle Files` icon to clean and build your application.
* Install completed! See [Sample Flow](https://github.com/ricohapi/media-storage-java#sample-flow) for a coding example.

## Sample Flow
```java
// import
import com.ricohapi.mstorage.MediaStorage;
import com.ricohapi.mstorage.entity.MediaInfo;
import com.ricohapi.auth.AuthClient;
import com.ricohapi.auth.AuthResult;
import com.ricohapi.auth.CompletionHandler;

// Set your Ricoh API Client Credentials
AuthClient authClient = new AuthClient("<your_client_id>", "<your_client_secret>");

// Set your resource owner credentials (Ricoh ID)
authClient.setResourceOwnerCreds("<your_user_id>", "<your_password>");

// Initialize a MediaStorage object with the AuthClient object
MediaStorage mstorage = new MediaStorage(authClient);

// Prepare a InputStream object in your way
File file = new File("upload.jpg");
final InputStream inputStream = new FileInputStream(file);

// Connect to the server
mstorage.connect(new CompletionHandler<AuthResult>(){

    // Success
    @Override
    public void onCompleted(AuthResult result) {

        // Upload
        mstorage.upload(inputStream, new CompletionHandler<MediaInfo>() {

            // Success
            @Override
            public void onCompleted(MediaInfo mediaInfo) {
                String id = mediaInfo.getId();
                String contentType = mediaInfo.getContentType();
                int bytes = mediaInfo.getBytes();
                String createdAt = mediaInfo.getCreatedAt();
                // Close InputStream.
            }

            // Error
            @Override
            public void onThrowable(Throwable t) {
                // Something wrong happened.
                // Close InputStream.
            }
        });
    }

    // Error
    @Override
    public void onThrowable(Throwable t) {
        // Something wrong happened.
    }
});
```

## SDK API Samples

### AuthClient
```java
AuthClient authClient = new AuthClient("<your_client_id>", "<your_client_secret>");
authClient.setResourceOwnerCreds("<your_user_id>", "<your_password>");
```

### Constructor
```java
MediaStorage mstorage = new MediaStorage(authClient);
```

### Connect to the server
```java
mstorage.connect(new CompletionHandler<AuthResult>() {
    @Override
    public void onCompleted(AuthResult result) {
        String accessToken = result.getAccessToken();
        // Do something.
    }

    @Override
    public void onThrowable(Throwable t) {
        // Something wrong happened.
    }
});
```

### Upload
```java
File file = ...;
InputStream inputStream = new FileInputStream(file);
mstorage.upload(inputStream, new CompletionHandler<MediaInfo>() {
    @Override
    public void onCompleted(MediaInfo mediaInfo) {
        String id = mediaInfo.getId();
        String contentType = mediaInfo.getContentType();
        int bytes = mediaInfo.getBytes();
        String createdAt = mediaInfo.getCreatedAt();
        // Do something.
        // Close InputStream.
    }

    @Override
    public void onThrowable(Throwable t) {
        // Something wrong happened.
        // Close InputStream.
    }
});
```

### Download
```java
mstorage.download("<media_id>", new CompletionHandler<MediaContent>() {
    @Override
    public void onCompleted(MediaContent mediaContent) {
        InputStream in = mediaContent.getInputStream();
        // Do something.
    }

    @Override
    public void onThrowable(Throwable t) {
        // Something wrong happened.
    }
});
```

### List media ids
You'll get a default list if you set an empty `Map` object or `null` on the first parameter.
```java
mstorage.list(null, new CompletionHandler<MediaList>() {
    @Override
    public void onCompleted(MediaList mediaList) {
        List<MediaIndex> list = mediaList.getMediaList();
        for (MediaIndex index : list) {
            String id = index.getId();
        }
        String pagingNext = mediaList.getPaging().getNext();
        String pagingPrevious = mediaList.getPaging().getPrevious();
        // Do something.
    }

    @Override
    public void onThrowable(Throwable t) {
        // Something wrong happened.
    }
});
```

You can also use listing options by setting values on `Map` object as follows.
The available options are `limit`, `after` and `before`.
```java
Map<String, String> params = new HashMap<String, String>();
params.put("limit", "25");
params.put("after", "<media_id>");
mstorage.list(params, new CompletionHandler<MediaList>() {
    @Override
    public void onCompleted(MediaList mediaList) {
        // Do something.
    }

    @Override
    public void onThrowable(Throwable t) {
        // Something wrong happened.
    }
});
```

### Delete
```java
mstorage.delete("<media_id>", new CompletionHandler<Object>() {
    @Override
    public void onCompleted(Object result) {
        // Do something.
        // result may be empty.
    }

    @Override
    public void onThrowable(Throwable t) {
        // Something wrong happened.
    }
});
```

### Get media information
```java
mstorage.info("<media_id>", new CompletionHandler<MediaInfo>() {
    @Override
    public void onCompleted(MediaInfo mediaInfo) {
        String id = mediaInfo.getId();
        String contentType = mediaInfo.getContentType();
        int bytes = mediaInfo.getBytes();
        String createdAt = mediaInfo.getCreatedAt();
        // Do something.
    }

    @Override
    public void onThrowable(Throwable t) {
        // Something wrong happened.
    }
});
```

### Get media metadata
```java
mstorage.meta("<media_id>", new CompletionHandler<MediaMeta>() {
    @Override
    public void onCompleted(MediaMeta mediaMeta) {
        Map<String, String> exif = mediaMeta.getExif();
        Map<String, String> gpano = mediaMeta.getGpano();
        // Do something.
    }

    @Override
    public void onThrowable(Throwable t) {
        // Something wrong happened.
    }
});
```

## References
* [Media Storage REST API](https://github.com/ricohapi/media-storage-rest/blob/master/media.md)
