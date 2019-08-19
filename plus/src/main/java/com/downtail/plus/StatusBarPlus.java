package com.downtail.plus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
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
    private static int getStatusBarHeight(Context context) {
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
    private static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 计算状态栏颜色
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的状态栏颜色
     */
    private static int calculateStatusColor(@ColorInt int color, int alpha) {
        if (alpha == 0) {
            return color;
        }
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
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
        setColor(activity, color, 0);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setColor(Activity activity, int color, int alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setColorAboveLollipop(activity, color, alpha);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentAboveKitkat(activity);
            addStatusBar(activity, color, alpha);
            autoFitsSystemWindows(activity, true);
        }
    }

    public static void setColor(View statusBarView, int color) {
        setColor(statusBarView, color, 0);
    }

    public static void setColor(View statusBarView, int color, int alpha) {
        statusBarView.getLayoutParams().height = getStatusBarHeight(statusBarView.getContext());
        statusBarView.setBackgroundColor(calculateStatusColor(color, alpha));
    }

    /**
     * setTranslucent API19(4.4)
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void setTranslucentAboveKitkat(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    /**
     * setTranslucent API21(5.0)
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
     * 设置状态栏颜色API21(5.0)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setColorAboveLollipop(Activity activity, int color, int alpha) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(calculateStatusColor(color, alpha));
    }

    /**
     * 延伸到状态栏
     * 此方法只能在Activity中调用
     *
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTransparent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentAboveLollipop(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentAboveKitkat(activity);
            removeStatusBar(activity);
            autoFitsSystemWindows(activity, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        AndroidBug5497Workaround.assistActivity(activity);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTransparentIgnoreNavigation(Activity activity) {
        setTransparentIgnoreNavigation(activity, 0);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTransparentIgnoreNavigation(Activity activity, int alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            setTransparentAboveLollipop(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentAboveKitkat(activity);
            removeStatusBar(activity);
            autoFitsSystemWindows(activity, false);
        }
        addTranslucentView(activity, alpha);
        AndroidBug5497Workaround.assistActivity(activity);
    }

    private static void addTranslucentView(Activity activity, int alpha) {
        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
        int childCount = viewGroup.getChildCount();
        if (childCount > 0 && viewGroup.getChildAt(childCount - 1) instanceof StatusBarView) {
            viewGroup.getChildAt(childCount - 1).setVisibility(View.VISIBLE);
            viewGroup.getChildAt(childCount - 1).setBackgroundColor(calculateStatusColor(Color.argb(alpha, 0, 0, 0), alpha));
        } else {
            StatusBarView statusBarView = createStatusBarView(activity, Color.argb(alpha, 0, 0, 0), 0);
            viewGroup.addView(statusBarView);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTransparentWithSystemBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentAboveKitkat(activity);
        }
    }

    private static final int TAG_KEY_HAVE_SET_OFFSET = -123;

    /**
     * 延伸到状态栏(API5.0)
     *
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setTransparentAboveLollipop(Activity activity, View needOffsetView) {

        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//                .getDecorView()
//                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        addStatusBar(activity, Color.WHITE, 0);
        if (needOffsetView != null) {
            Object haveSetOffset = needOffsetView.getTag(TAG_KEY_HAVE_SET_OFFSET);
            if (haveSetOffset != null && (Boolean) haveSetOffset) {
                return;
            }
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) needOffsetView.getLayoutParams();
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + getStatusBarHeight(activity),
                    layoutParams.rightMargin, layoutParams.bottomMargin);
            needOffsetView.setTag(TAG_KEY_HAVE_SET_OFFSET, true);

        }
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
    private static void addStatusBar(Activity activity, int color, int alpha) {
        ViewGroup viewGroup = activity.findViewById(android.R.id.content);
        int childCount = viewGroup.getChildCount();
        if (childCount > 0 && viewGroup.getChildAt(childCount - 1) instanceof StatusBarView) {
            viewGroup.getChildAt(childCount - 1).setVisibility(View.VISIBLE);
            viewGroup.getChildAt(childCount - 1).setBackgroundColor(calculateStatusColor(color, alpha));
        } else {
            StatusBarView statusBarView = createStatusBarView(activity, color, alpha);
            viewGroup.addView(statusBarView);
        }
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
    private static StatusBarView createStatusBarView(Activity activity, int color, int alpha) {
        StatusBarView statusBarView = new StatusBarView(activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
        statusBarView.setLayoutParams(layoutParams);
        statusBarView.setBackgroundColor(calculateStatusColor(color, alpha));
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
