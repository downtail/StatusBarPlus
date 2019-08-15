package com.downtail.plus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBarPlus {

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 获取导航栏高度
     *
     * @param context
     * @return
     */
    public int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 设置状态栏颜色
     * 此方法只能在Activity中调用
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
     * 此方法只能在Activity中调用
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.flags = attributes.flags & ~WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        }
        AndroidBug5497Workaround.assistActivity(activity);
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
            viewGroup.getChildAt(childCount - 1).setVisibility(View.GONE);
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
            viewGroup.getChildAt(childCount - 1).setVisibility(View.VISIBLE);
            viewGroup.getChildAt(childCount - 1).setBackgroundColor(color);
        } else {
            StatusBarView statusBarView = createStatusBarView(activity, color);
            viewGroup.addView(statusBarView);
        }
        autoFitsSystemWindows(activity, true);
    }

    private static void autoFitsSystemWindows(Activity activity, boolean fitsSystemWindows) {
        ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        if (rootView != null) {
            rootView.setFitsSystemWindows(fitsSystemWindows);
            //rootview在原有得paddingTop上加上状态栏高度
//            rootView.setPadding(rootView.getPaddingLeft(), getStatusBarHeight(activity) + rootView.getPaddingTop(), rootView.getPaddingRight(), rootView.getPaddingBottom());
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
        if (isMIUIV6OrAbove()) {
            setStatusBarDarkModeMIUI(activity, darkMode);
        } else if (isFlymeV4OrAbove()) {
            setStatusBarDarkModeFlyme(activity, darkMode);
        } else if (Build.MANUFACTURER.equalsIgnoreCase("OPPO")) {
            setStatusBarDarkModeOPPO(activity, darkMode);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setStatusBarDarkModeNative(activity, darkMode);
        }
    }

    /**
     * 设置状态栏深浅色
     *
     * @param fragment
     * @param darkMode
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setStatusBarMode(Fragment fragment, boolean darkMode) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            setStatusBarMode(activity, darkMode);
        }
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setStatusBarDarkModeNative(activity, darkMode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //MIUI V6对应的versionCode是4
    //MIUI V7对应的versionCode是5
    private static boolean isMIUIV6OrAbove() {
        String miuiVersionCodeStr = getSystemProperty("ro.miui.ui.version.code");
        if (!TextUtils.isEmpty(miuiVersionCodeStr)) {
            try {
                int miuiVersionCode = Integer.parseInt(miuiVersionCodeStr);
                if (miuiVersionCode >= 4) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    /**
     * 设置Flyme深浅模式
     *
     * @param activity
     * @param darkMode
     * @return
     */
    private static void setStatusBarDarkModeFlyme(Activity activity, boolean darkMode) {
        StatusbarColorUtils.setStatusBarDarkIcon(activity, darkMode);
    }

    //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
    //Flyme V5的displayId格式为 [Flyme 5.x.x.x beta]
    private static boolean isFlymeV4OrAbove() {
        String displayId = Build.DISPLAY;
        if (!TextUtils.isEmpty(displayId) && displayId.contains("Flyme")) {
            String[] displayIdArray = displayId.split(" ");
            for (String temp : displayIdArray) {
                //版本号4以上，形如4.x.
                if (temp.matches("^[4-9]\\.(\\d+\\.)+\\S*")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置OPPO深浅模式
     *
     * @param activity
     * @param lightMode
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setStatusBarDarkModeOPPO(Activity activity, boolean lightMode) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int vis = window.getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (lightMode) {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT = 0x00000010;
            if (lightMode) {
                vis |= SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
            } else {
                vis &= ~SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
            }
        }
        window.getDecorView().setSystemUiVisibility(vis);
    }

    /**
     * API23(6.0)深浅模式
     *
     * @param darkMode
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static void setStatusBarDarkModeNative(Activity activity, boolean darkMode) {
        Window window = activity.getWindow();
        View decor = window.getDecorView();
        if (darkMode) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }
}
