package com.lotadata.geopush.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.TextView;

import com.lotadata.geopush.R;
import com.lotadata.moments.Moments;

public class EngagementActivity extends AppCompatActivity {

    private static final String TAG = "EngagementActivity";

    private TextView mActionText;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.engagement_view);

        //Get web view
        mWebView = (WebView) findViewById(R.id.webviewID);

        //Get action text view
        mActionText = (TextView) findViewById(R.id.action_textID);

        Intent intent = getIntent();

        //Sanity check
        if((intent == null) || (intent.getExtras() == null)) return;

        //Get the engagement ID
        String actionText = intent.getExtras().getString(Moments.LD_NOTIFICATION_ACTION_TEXT);
        String actionLink = intent.getExtras().getString(Moments.LD_NOTIFICATION_ACTION_LINK);
        String message = intent.getExtras().getString(Moments.LD_NOTIFICATION_MESSAGE);


        //Set text
        mActionText.setText(actionText);

        //Load url
        mWebView.loadUrl(actionLink);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
