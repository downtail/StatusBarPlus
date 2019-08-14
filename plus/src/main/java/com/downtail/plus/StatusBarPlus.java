package com.downtail.plus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBarPlus {

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    private static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 设置状态栏颜色
     *
     * @param activity
     * @param color
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setColorAboveLollipop(activity, color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentAboveKitkat(activity);
            addStatusBar(activity, color);
        }
    }

    /**
     * 设置状态栏颜色API19(4.4)
     */
    private static void setTranslucentAboveKitkat(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    /**
     * 设置状态栏颜色API21(5.0)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setColorAboveLollipop(Activity activity, int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(color);
    }

    /**
     * 延伸到状态栏
     *
     * @param activity
     */
    public static void setTransparent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentAboveLollipop(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentAboveKitkat(activity);
            removeStatusBar(activity);
        }
    }

    /**
     * 延伸到状态栏(API5.0)
     *
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setTranslucentAboveLollipop(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 移除StatusBarView
     *
     * @param activity
     */
    private static void removeStatusBar(Activity activity) {
        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
        int childCount = viewGroup.getChildCount();
        if (childCount > 0 && viewGroup.getChildAt(childCount - 1) instanceof StatusBarView) {
            viewGroup.removeViewAt(childCount - 1);
        }
    }

    /**
     * 添加StatusBarView
     *
     * @param activity
     * @param color
     */
    private static void addStatusBar(Activity activity, int color) {
        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
        int childCount = viewGroup.getChildCount();
        if (childCount > 0 && viewGroup.getChildAt(childCount - 1) instanceof StatusBarView) {
            viewGroup.getChildAt(childCount - 1).setBackgroundColor(color);
        } else {
            StatusBarView statusBarView = createStatusBarView(activity, color);
            viewGroup.addView(statusBarView);
        }
        ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        if (rootView != null) {
            //rootview在原有得paddingTop上加上状态栏高度
            rootView.setPadding(rootView.getPaddingLeft(), getStatusBarHeight(activity) + rootView.getPaddingTop(), rootView.getPaddingRight(), rootView.getPaddingBottom());
        }
    }

    /**
     * 创建一个view代替状态栏
     *
     * @param activity
     * @param color
     * @return
     */
    private static StatusBarView createStatusBarView(Activity activity, int color) {
        StatusBarView statusBarView = new StatusBarView(activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
        statusBarView.setLayoutParams(layoutParams);
        statusBarView.setBackgroundColor(color);
        return statusBarView;
    }

    /**
     * StatusBarView
     */
    private static class StatusBarView extends View {
        public StatusBarView(Context context) {
            super(context);
        }
    }

    /**
     * 设置状态栏深浅色
     *
     * @param activity
     * @param darkMode
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setStatusBarMode(Activity activity, boolean darkMode) {
        setStatusBarMode(activity, darkMode, false);
    }

    /**
     * 设置状态栏深浅色
     *
     * @param activity
     * @param darkMode
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setStatusBarMode(Activity activity, boolean darkMode, boolean isUseFullScreenMode) {
        setStatusBarDarkModeNative(activity, darkMode, isUseFullScreenMode);
    }

    /**
     * 设置MIUI深浅模式
     *
     * @param darkMode
     * @param activity
     */
    private static void setStatusBarDarkModeMIUI(Activity activity, boolean darkMode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkMode ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Flyme深浅模式
     *
     * @param activity
     * @param darkMode
     * @return
     */
    private static boolean setStatusBarDarkModeFlyme(Activity activity, boolean darkMode) {
        boolean result = false;
        if (activity != null) {
            try {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (darkMode) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                activity.getWindow().setAttributes(lp);
                result = true;
            } catch (Exception e) {
            }
        }
        return result;
    }

    /**
     * API23(6.0)深浅模式
     *
     * @param darkMode
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static void setStatusBarDarkModeNative(Activity activity, boolean darkMode, boolean isUseFullScreenMode) {
        View decor = activity.getWindow().getDecorView();
        if (darkMode) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        ViewGroup rootView = (ViewGroup)((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        if (rootView != null && !isUseFullScreenMode) {
            //rootview在原有得paddingTop上加上状态栏高度
            rootView.setPadding(rootView.getPaddingLeft(), getStatusBarHeight(activity) + rootView.getPaddingTop(), rootView.getPaddingRight(), rootView.getPaddingBottom());
        }
    }
}
