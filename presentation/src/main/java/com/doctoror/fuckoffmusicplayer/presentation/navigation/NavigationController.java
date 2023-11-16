/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.presentation.navigation;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.presentation.Henson;
import com.doctoror.fuckoffmusicplayer.presentation.settings.SettingsActivity;
import com.google.android.material.navigation.NavigationView;

public final class NavigationController {

    @NonNull
    private final Context context;

    @NonNull
    private final DrawerLayout drawerLayout;

    @NonNull
    private final NavigationView navigationView;

    private NavigationItem drawerClosedAction;

    public NavigationController(
            @NonNull final Context context,
            @NonNull final View view) {
        this.context = context;
        drawerLayout = view.findViewById(R.id.drawerLayout);
        navigationView = view.findViewById(R.id.navigationView);
    }

    public void bind() {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(@NonNull final View drawerView) {
                super.onDrawerClosed(drawerView);
                performDrawerClosedAction();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationListener());
    }

    private void performDrawerClosedAction() {
        if (drawerClosedAction != null) {
            switch (drawerClosedAction) {
                case SETTINGS:
                    context.startActivity(new Intent(context, SettingsActivity.class));
                    break;

                default:
                    final Intent intent = Henson.with(context)
                            .gotoHomeActivity()
                            .navigationAction(drawerClosedAction)
                            .build();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    break;
            }
            drawerClosedAction = null;
        }
    }

    private final class NavigationListener
            implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
            drawerClosedAction = NavigationItem.fromId(item.getItemId());
            drawerLayout.closeDrawers();
            return true;
        }
    }
}
