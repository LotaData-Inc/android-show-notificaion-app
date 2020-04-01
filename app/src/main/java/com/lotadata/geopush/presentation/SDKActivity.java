package com.lotadata.geopush.presentation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.lotadata.geopush.R;
import com.lotadata.moments.ConnectionResult;
import com.lotadata.moments.Moments;
import com.lotadata.moments.MomentsClient;
import com.lotadata.moments.engagements.NotificationReceiver;

public class SDKActivity extends AppCompatActivity {

    private static final String TAG = "GEOPUSH";

    private Moments mMomentsClient;
    private TextView notificationCounterTextView;
    private Toolbar toolbar;

    protected String[] mPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    protected String[] mPermissionsForQ = {Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION};

    protected static final int PERMISSIONS_REQUEST = 100;
    protected int count = 0;
    protected Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdk_activity);

        //Initialize signal counter
        notificationCounterTextView = (TextView) findViewById(R.id.notificationCounterView);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mContext = this;
        notificationCounterTextView.setText(getString(R.string.notification_count_field_name) + count);

        checkPermissionsAndLaunch();
    }

    protected void checkPermissionsAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                for(int i = 0; i < mPermissionsForQ.length; i++) {
                    mPermissions = ArrayUtils.appendToArray(mPermissions, mPermissionsForQ[i]);
                }
            }

            if(!hasPermissions(this, mPermissions)){
                ActivityCompat.requestPermissions(this, mPermissions, PERMISSIONS_REQUEST);
            }
        }
        initializeLotaDataSDK();
    }

    private void initializeLotaDataSDK() {

        MomentsClient.getInstance(this, new MomentsClient.ConnectionListener() {
            @Override
            public void onConnected(Moments client) {
                Toast.makeText(mContext,"Initialization Successful",Toast.LENGTH_LONG).show();
                mMomentsClient = client;

                trackEngagements();
            }

            @Override
            public void onConnectionError(ConnectionResult result) {
                Toast.makeText(mContext,"Initialization Failed",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mMomentsClient != null) {
            untrackEngagements();
            Toast.makeText(this,"See you!",Toast.LENGTH_LONG).show();
            mMomentsClient.recordEvent("Stop SDK");
            mMomentsClient.disconnect();
        }
        super.onDestroy();
    }

    private static boolean hasPermissions(Activity activity, String... permissions) {
        if (activity != null && permissions != null) {
            for (String permission : permissions) {
                if ((ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) ||
                        (((ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)))){
                    return false;
                }
            }
        }
        return true;
    }

    protected void trackEngagements() {

        //Receives that handles notification
        NotificationReceiver notificationReceiver = new NotificationReceiver(
                101, R.drawable.lotadata_notification_icon, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) {
            @Override
            public void notificationClicked(Context context, String actionText, String actionLink, String message) {

                count++;
                Toast.makeText(mContext, "Notification displayed!", Toast.LENGTH_SHORT).show();
                notificationCounterTextView.setText(getString(R.string.notification_count_field_name) + count);

                //Compose intent to start activity that contains webview
                if(actionLink.contains("http")) {//Is it a url link?

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(actionLink));
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    //Launch a browser
                    context.startActivity(browserIntent);

                } else if (actionLink.contains(context.getPackageName())){//Is it an Activity class?
                    try {
                        Class activityClass = Class.forName(actionLink);
                        Intent myIntent = new Intent(context, activityClass);
                        myIntent.putExtra(Moments.LD_NOTIFICATION_ACTION_TEXT, actionText);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        //Launch an internal activity
                        context.startActivity(myIntent);

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //Track user engagements
        mMomentsClient.trackEngagements(notificationReceiver);
    }

    protected void untrackEngagements() {
        if(mMomentsClient != null) mMomentsClient.untrackEngagements();
    }
}
