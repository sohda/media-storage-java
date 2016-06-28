//
//  Copyright (c) 2016 Ricoh Company, Ltd. All Rights Reserved.
//  See LICENSE for more information.
//

package com.ricohapi.mstorage.sample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ricohapi.mstorage.MediaStorage;
import com.ricohapi.mstorage.entity.MediaIndex;
import com.ricohapi.mstorage.entity.MediaList;
import com.ricohapi.auth.AuthClient;
import com.ricohapi.auth.entity.AuthResult;
import com.ricohapi.auth.CompletionHandler;

public class MainActivity extends AppCompatActivity {

    private TextView connectStatusText;
    private TextView connectResultText;
    private TextView listStatusText;
    private TextView listResultText;
    private AuthClient authClient;
    private Button listBtn;

    private MediaStorage mstorage;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.connectStatusText = (TextView) findViewById(R.id.connectStatusText);
        this.connectResultText = (TextView) findViewById(R.id.connectResultText);
        this.listStatusText = (TextView) findViewById(R.id.listStatusText);
        this.listResultText = (TextView) findViewById(R.id.listResultText);
        this.listBtn = (Button) findViewById(R.id.listBtn);
    }

    public void onConnectBtnClick(View view){

        // Create and set an AuthClient object.
        authClient = new AuthClient("### enter your client ID ###", "### enter your client secret ###");
        authClient.setResourceOwnerCreds("### enter your user id ###", "### enter your user password ###");

        mstorage = new MediaStorage(authClient);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Start a session.
                mstorage.connect(new CompletionHandler<AuthResult>(){
                    // Success
                    @Override
                    public void onCompleted(AuthResult result) {
                        Log.i("Connection established", "Here is your access token: " + result.getAccessToken());
                        final String accessToken = result.getAccessToken();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateConnectTextView("Connected!", "Here is your access token: \n" + accessToken);
                                listBtn.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    // Error
                    @Override
                    public void onThrowable(final Throwable t) {
                        Log.i("Connection failed.", t.toString());

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateConnectTextView("Failed!", "Here is the error: \n" + t.toString());
                            }
                        });
                    }
                });
                return null;
            }
        }.execute();
    }

    public void onListBtnClick(View view){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Start a session.
                mstorage.list(null, new CompletionHandler<MediaList>() {

                    @Override
                    public void onCompleted(final MediaList mediaList) {
                        Log.i("List obtained", "You'll see a list on the app.");
                        Log.i("List obtained", "The next page is " + mediaList.getPaging().getNext());
                        Log.i("List obtained", "The previous page is " + mediaList.getPaging().getPrevious());

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder listedIds = new StringBuilder();
                                for (MediaIndex mediaIndex: mediaList.getMediaList()) {
                                    listedIds.append(mediaIndex.getId());
                                    listedIds.append("\n");
                                }
                                updateListTextView("You've got a list!", listedIds.toString());
                            }
                        });
                    }

                    @Override
                    public void onThrowable(final Throwable t) {
                        Log.i("Connection failed.", t.toString());

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateListTextView("Failed!", "Here is the error: \n" + t.toString());
                            }
                        });
                    }
                });
                return null;
            }
        }.execute();
    }

    private void updateConnectTextView(String status, String result) {
        if (connectStatusText != null && connectResultText != null) {
            connectStatusText.setText(status);
            connectResultText.setText(result);
        }
    }

    private void updateListTextView(String status, String result) {
        if (listStatusText != null && listResultText != null) {
            listStatusText.setText(status);
            listResultText.setText(result);
        }
    }
}
