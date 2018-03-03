package com.doctoror.fuckoffmusicplayer.navigation;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.settings.SettingsActivity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;

public final class NavigationController {

    @NonNull
    private final Context mContext;

    @NonNull
    private final DrawerLayout mDrawerLayout;

    @NonNull
    private final NavigationView mNavigationView;

    private Integer mDrawerClosedAction;

    public NavigationController(
            @NonNull final Context context,
            @NonNull final View view) {
        mContext = context;
        mDrawerLayout = view.findViewById(R.id.drawerLayout);
        mNavigationView = view.findViewById(R.id.navigationView);
    }

    public void bind() {
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(@NonNull final View drawerView) {
                super.onDrawerClosed(drawerView);
                performDrawerClosedAction();
            }
        });

        mNavigationView.setNavigationItemSelectedListener(new NavigationListener());
    }

    private void performDrawerClosedAction() {
        if (mDrawerClosedAction != null) {
            switch (mDrawerClosedAction) {
                case R.id.navigationSettings:
                    mContext.startActivity(new Intent(mContext, SettingsActivity.class));
                    break;

                default:
                    final Intent intent = Henson.with(mContext)
                            .gotoHomeActivity()
                            .drawerClosedAction(mDrawerClosedAction)
                            .build();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(intent);
                    break;
            }
            mDrawerClosedAction = null;
        }
    }

    private final class NavigationListener
            implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
            mDrawerClosedAction = item.getItemId();
            mDrawerLayout.closeDrawers();
            return true;
        }
    }
}
