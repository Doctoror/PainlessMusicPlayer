/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.presentation.util;

import android.graphics.Rect;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewGroup;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * {@link CoordinatorLayout} utils
 */
public final class CoordinatorLayoutUtil {

    private CoordinatorLayoutUtil() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static AnchorParams getAnchorParams(@NonNull final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof CoordinatorLayout.LayoutParams) {
            final CoordinatorLayout.LayoutParams clp = (CoordinatorLayout.LayoutParams) lp;
            return new AnchorParams(clp.getAnchorId(),
                    clp.anchorGravity,
                    clp.leftMargin,
                    clp.topMargin,
                    clp.rightMargin,
                    clp.bottomMargin);
        }
        return null;
    }

    public static void applyAnchorParams(@NonNull final View view,
            @NonNull final AnchorParams ap) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof CoordinatorLayout.LayoutParams) {
            final CoordinatorLayout.LayoutParams clp = (CoordinatorLayout.LayoutParams) lp;
            clp.setMargins(ap.leftMargin, ap.topMargin, ap.rightMargin, ap.bottomMargin);
            clp.anchorGravity = ap.anchorGravity;
            clp.setAnchorId(ap.anchorId);
            view.setLayoutParams(clp);
        }
    }

    public static void clearAnchorGravityAndApplyMargins(@NonNull final View view) {
        final ViewGroup parent = (ViewGroup) view.getParent();
        if (parent instanceof CoordinatorLayout) {
            final Rect rect = new Rect();
            parent.getChildVisibleRect(view, rect, null);

            final ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof CoordinatorLayout.LayoutParams) {
                ((CoordinatorLayout.LayoutParams) lp).anchorGravity = 0;
                ((CoordinatorLayout.LayoutParams) lp).setAnchorId(0);
                ((CoordinatorLayout.LayoutParams) lp).setMargins(rect.left, rect.top,
                        ((CoordinatorLayout.LayoutParams) lp).rightMargin,
                        ((CoordinatorLayout.LayoutParams) lp).bottomMargin);
            }
        }
    }

    @Parcel
    public static final class AnchorParams {

        @IdRes
        final int anchorId;

        final int anchorGravity;

        final int leftMargin;
        final int topMargin;
        final int rightMargin;
        final int bottomMargin;

        @ParcelConstructor
        AnchorParams(@IdRes final int anchorId,
                final int anchorGravity,
                final int leftMargin,
                final int topMargin,
                final int rightMargin,
                final int bottomMargin) {
            this.anchorId = anchorId;
            this.anchorGravity = anchorGravity;
            this.leftMargin = leftMargin;
            this.topMargin = topMargin;
            this.rightMargin = rightMargin;
            this.bottomMargin = bottomMargin;
        }
    }
}
