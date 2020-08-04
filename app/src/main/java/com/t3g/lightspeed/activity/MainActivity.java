package com.t3g.lightspeed.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.t3g.lightspeed.R;
import com.t3g.lightspeed.app.Activity;
import com.t3g.lightspeed.dialog.ShareAppDialog;
import com.t3g.lightspeed.fragment.HomeFragment;
import com.t3g.lightspeed.object.NetworkDevice;
import com.t3g.lightspeed.service.CommunicationService;
import com.t3g.lightspeed.ui.callback.PowerfulActionModeSupport;
import com.t3g.lightspeed.util.AppUtils;
import com.t3g.lightspeed.util.UpdateUtils;
import com.genonbeta.android.framework.widget.PowerfulActionMode;
import com.google.android.material.navigation.NavigationView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;

public class MainActivity
        extends Activity
        implements NavigationView.OnNavigationItemSelectedListener, PowerfulActionModeSupport
{
    private AdView adView;
    public static final int REQUEST_PERMISSION_ALL = 1;
    private FirebaseAnalytics mFirebaseAnalytics;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private PowerfulActionMode mActionMode;
    private HomeFragment mHomeFragment;
    private MenuItem mTrustZoneToggle;
    private IntentFilter mFilter = new IntentFilter();
    private BroadcastReceiver mReceiver = null;

    private long mExitPressTime;
    private int mChosenMenuItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MyNotifications", "MyNotifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        FirebaseMessaging.getInstance().subscribeToTopic("5.4");

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        mHomeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.activitiy_home_fragment);
        mActionMode = findViewById(R.id.content_powerful_action_mode);
        mNavigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mTrustZoneToggle = mNavigationView.getMenu().findItem(R.id.menu_activity_trustzone);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.text_navigationDrawerOpen, R.string.text_navigationDrawerClose);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mFilter.addAction(CommunicationService.ACTION_TRUSTZONE_STATUS);
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener()
        {
            @Override
            public void onDrawerClosed(View drawerView)
            {
                applyAwaitingDrawerAction();
            }
        });

        mNavigationView.setNavigationItemSelectedListener(this);
        mActionMode.setOnSelectionTaskListener(new PowerfulActionMode.OnSelectionTaskListener()
        {
            @Override
            public void onSelectionTask(boolean started, PowerfulActionMode actionMode)
            {
//                toolbar.setVisibility(!started ? View.VISIBLE : View.GONE);
            }
        });

        if (UpdateUtils.hasNewVersion(this))
            highlightUpdater(getDefaultPreferences().getString("availableVersion", null));

        if (!AppUtils.isLatestChangeLogSeen(this)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.mesg_versionUpdatedChangelog)
                    .setNegativeButton(R.string.butn_ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            AppUtils.publishLatestChangelogSeen(MainActivity.this);
                        }
                    })
                    .show();
        }
// Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adView = new AdView(this, "362136061448257_362145731447290", AdSize.BANNER_HEIGHT_50);

        // Find the Ad Container
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);

        // Add the ad view to your activity layout
        adContainer.addView(adView);

        // Request an ad
        adView.loadAd();

        adView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
//                Toast.makeText(MainActivity.this, "Error: " + adError.getErrorMessage(),
//                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Ad loaded callback
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        });


//        // Request an ad
//        adView.loadAd();

    }
    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        createHeaderView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mReceiver = new ActivityReceiver(), mFilter);
        requestTrustZoneStatus();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (mReceiver != null)
            unregisterReceiver(mReceiver);

        mReceiver = null;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        mChosenMenuItemId = item.getItemId();

        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed()
    {
        if (mHomeFragment.onBackPressed())
            return;

        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else if ((System.currentTimeMillis() - mExitPressTime) < 2000)
            super.onBackPressed();
        else {
            mExitPressTime = System.currentTimeMillis();
            Toast.makeText(this, R.string.mesg_secureExit, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUserProfileUpdated()
    {
        createHeaderView();
    }

    private void applyAwaitingDrawerAction()
    {
        if (mChosenMenuItemId == 0) {
            // Do nothing
        } else if (R.id.menu_activity_main_manage_devices == mChosenMenuItemId) {
            startActivity(new Intent(this, ManageDevicesActivity.class));
        } else if (R.id.menu_activity_main_send_application == mChosenMenuItemId) {
            new ShareAppDialog(MainActivity.this)
                    .show();
        }else if (R.id.menu_activity_main_about == mChosenMenuItemId) {
            startActivity(new Intent(this, AboutActivity.class));
        }else if (R.id.menu_activity_main_web_share == mChosenMenuItemId) {
            startActivity(new Intent(this, WebShareActivity.class));
        } else if (R.id.menu_activity_main_preferences == mChosenMenuItemId) {
            startActivity(new Intent(this, PreferencesActivity.class));
        } else if (R.id.menu_activity_main_exit == mChosenMenuItemId) {
            exitApp();
        }  else if (R.id.menu_activity_privacy == mChosenMenuItemId) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://techno3gamma.in/privacy_policy.htm"));
            startActivity(i);
        }  else if (R.id.menu_activity_bug_report == mChosenMenuItemId) {
            try {
                String text = "LightSpeed bug: "; // Replace with your message.

                String toNumber = "918404032210"; // Replace with mobile phone number without +Sign or leading zeros.


                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + toNumber + "&text=" + text));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Install WhatsApp to report", Toast.LENGTH_LONG).show();
            }
        }

        mChosenMenuItemId = 0;
    }

    private void createHeaderView()
    {
        View headerView = mNavigationView.getHeaderView(0);
        Configuration configuration = getApplication().getResources().getConfiguration();



        if (headerView != null) {
            NetworkDevice localDevice = AppUtils.getLocalDevice(getApplicationContext());

            ImageView imageView = headerView.findViewById(R.id.layout_profile_picture_image_default);
            ImageView editImageView = headerView.findViewById(R.id.layout_profile_picture_image_preferred);
            TextView deviceNameText = headerView.findViewById(R.id.header_default_device_name_text);
            TextView versionText = headerView.findViewById(R.id.header_default_device_version_text);

            deviceNameText.setText(localDevice.nickname);
            versionText.setText(localDevice.versionName);
            loadProfilePictureInto(localDevice.nickname, imageView);

            editImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startProfileEditor();
                }
            });
        }
    }

    @Override
    public PowerfulActionMode getPowerfulActionMode()
    {
        return mActionMode;
    }

    private void highlightUpdater(String availableVersion)
    {
        MenuItem item = mNavigationView.getMenu().findItem(R.id.menu_activity_main_about);
        item.setTitle(R.string.text_newVersionAvailable);
    }

    public void requestTrustZoneStatus()
    {
        AppUtils.startForegroundService(this, new Intent(this, CommunicationService.class)
                .setAction(CommunicationService.ACTION_REQUEST_TRUSTZONE_STATUS));
    }

    public void toggleTrustZone()
    {
        AppUtils.startForegroundService(this, new Intent(this, CommunicationService.class)
                .setAction(CommunicationService.ACTION_TOGGLE_SEAMLESS_MODE));
    }

    private class ActivityReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (CommunicationService.ACTION_TRUSTZONE_STATUS.equals(intent.getAction())
                    && mTrustZoneToggle != null)
                mTrustZoneToggle.setTitle(intent.getBooleanExtra(
                        CommunicationService.EXTRA_STATUS_STARTED, false)
                        ? R.string.butn_turnTrustZoneOff : R.string.butn_turnTrustZoneOn);
        }
    }
}
