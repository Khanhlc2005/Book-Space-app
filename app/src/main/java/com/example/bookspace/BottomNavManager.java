package com.example.bookspace;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class BottomNavManager {

    public static final int NAV_HOME = 0;
    public static final int NAV_LIBRARY = 1;
    public static final int NAV_READER = 2;

    private static int currentNav = NAV_HOME;

    public static void setCurrentNav(int nav) {
        currentNav = nav;
    }

    public static int getCurrentNav() {
        return currentNav;
    }

    public static void setupBottomNav(final Activity activity, int activeNav) {
        currentNav = activeNav;

        int activeColor = ContextCompat.getColor(activity, R.color.teal_600);
        int inactiveColor = ContextCompat.getColor(activity, R.color.nav_inactive);

        ViewGroup navHome = activity.findViewById(R.id.nav_home);
        ViewGroup navLibrary = activity.findViewById(R.id.nav_library);
        ViewGroup navReader = activity.findViewById(R.id.nav_reader);

        if (navHome == null || navLibrary == null || navReader == null) return;

        ImageView iconHome = getNavIcon(navHome);
        ImageView iconLibrary = getNavIcon(navLibrary);
        ImageView iconReader = getNavIcon(navReader);

        TextView textHome = getNavText(navHome);
        TextView textLibrary = getNavText(navLibrary);
        TextView textReader = getNavText(navReader);

        updateNavItem(activity, navHome, iconHome, textHome, activeNav == NAV_HOME, activeColor, inactiveColor);
        updateNavItem(activity, navLibrary, iconLibrary, textLibrary, activeNav == NAV_LIBRARY, activeColor, inactiveColor);
        updateNavItem(activity, navReader, iconReader, textReader, activeNav == NAV_READER, activeColor, inactiveColor);

        navHome.setOnClickListener(v -> {
            if (currentNav == NAV_HOME) return;
            setCurrentNav(NAV_HOME);
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.finish();
        });

        navLibrary.setOnClickListener(v -> {
            if (currentNav == NAV_LIBRARY) return;
            setCurrentNav(NAV_LIBRARY);
            Intent intent = new Intent(activity, CurrentlyReadingListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.finish();
        });

        navReader.setOnClickListener(v -> {
            if (currentNav == NAV_READER) return;
            setCurrentNav(NAV_READER);
            Intent intent = new Intent(activity, ReadingActivity.class);
            activity.startActivity(intent);
        });
    }

    private static ImageView getNavIcon(ViewGroup navContainer) {
        if (navContainer == null) return null;
        for (int i = 0; i < navContainer.getChildCount(); i++) {
            View child = navContainer.getChildAt(i);
            if (child instanceof ImageView) return (ImageView) child;
            if (child instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) child;
                for (int j = 0; j < ll.getChildCount(); j++) {
                    View sub = ll.getChildAt(j);
                    if (sub instanceof ImageView) return (ImageView) sub;
                }
            }
        }
        return null;
    }

    private static TextView getNavText(ViewGroup navContainer) {
        if (navContainer == null) return null;
        for (int i = 0; i < navContainer.getChildCount(); i++) {
            View child = navContainer.getChildAt(i);
            if (child instanceof TextView) return (TextView) child;
            if (child instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) child;
                for (int j = 0; j < ll.getChildCount(); j++) {
                    View sub = ll.getChildAt(j);
                    if (sub instanceof TextView) return (TextView) sub;
                }
            }
        }
        return null;
    }

    private static void updateNavItem(Activity activity, View container, ImageView icon, TextView text,
                                      boolean isActive, int activeColor, int inactiveColor) {
        if (container == null) return;
        if (isActive) {
            container.setBackground(ContextCompat.getDrawable(activity, R.drawable.bottom_nav_active_bg));
        } else {
            container.setBackground(null);
        }
        if (icon != null) {
            icon.setColorFilter(isActive ? activeColor : inactiveColor);
        }
        if (text != null) {
            text.setTextColor(isActive ? activeColor : inactiveColor);
        }
    }
}
