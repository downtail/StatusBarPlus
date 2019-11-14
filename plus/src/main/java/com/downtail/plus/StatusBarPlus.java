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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBarPlus {

    private static final String STATUS_BAR_VIEW_TAG = "StatusBarView";

    private static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    private static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            setColorAboveLollipop(activity,Color.TRANSPARENT);
            setTransparentAboveLollipop(activity, Color.TRANSPARENT);
            showStatusBarView(activity, color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTransparentAboveKitkat(activity);
            showStatusBarView(activity, color);
        }
        autoFitsSystemWindows(activity, true);
    }

    public static View setColor(View contentView, int color) {
        if (contentView == null) {
            throw new NullPointerException("contentView can't be null");
        }
        View statusBarView = contentView.findViewWithTag(STATUS_BAR_VIEW_TAG);
        if (statusBarView == null) {
            LinearLayout rootView = new LinearLayout(contentView.getContext());
            rootView.setOrientation(LinearLayout.VERTICAL);
            rootView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            statusBarView = new View(contentView.getContext());
            statusBarView.setTag(STATUS_BAR_VIEW_TAG);
            rootView.addView(statusBarView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(contentView.getContext())));
            statusBarView.setBackgroundColor(color);
            rootView.addView(contentView, rootView.getChildCount());
            return rootView;
        } else {
            statusBarView.setBackgroundColor(color);
            return contentView;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setColorAboveLollipop(Activity activity, int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(color);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void setTransparentAboveKitkat(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    private static void showStatusBarView(Activity activity, int color) {
        Window window = activity.getWindow();
        ViewGroup contentView = activity.findViewById(android.R.id.content);
//        ViewGroup decorView = (ViewGroup) window.getDecorView();
        View statusBarView = contentView.findViewWithTag(STATUS_BAR_VIEW_TAG);
        if (statusBarView == null) {
            statusBarView = new View(window.getContext());
            statusBarView.setTag(STATUS_BAR_VIEW_TAG);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(window.getContext()));
            layoutParams.gravity = Gravity.TOP;
            statusBarView.setLayoutParams(layoutParams);
            contentView.addView(statusBarView);
        }
        statusBarView.setBackgroundColor(color);
    }

    private static void autoFitsSystemWindows(Activity activity, boolean fitsSystemWindows) {
        ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        if (rootView != null) {
            rootView.setFitsSystemWindows(fitsSystemWindows);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTransparent(Activity activity) {
        setTransparent(activity, Color.argb(0, 0, 0, 0), true);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTransparent(Activity activity, boolean pure) {
        setTransparent(activity, Color.argb(0, 0, 0, 0), pure);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTransparent(Activity activity, int color) {
        setTransparent(activity, color, true);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTransparent(Activity activity, int color, boolean pure) {
        if (pure) {
            setTransparentWithoutInput(activity, color);
        } else {
            setTransparentWithInput(activity, color);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTranslucent(Activity activity) {
        setTranslucent(activity, true);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTranslucent(Activity activity, boolean pure) {
        setTranslucent(activity, Color.argb(127, 0, 0, 0), pure);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTranslucent(Activity activity, int color) {
        setTranslucent(activity, color, true);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setTranslucent(Activity activity, int color, boolean pure) {
        if (pure) {
            setTransparentWithoutInput(activity, color);
        } else {
            setTransparentWithInput(activity, color);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void setTransparentWithoutInput(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTransparentAboveLollipop(activity, Color.TRANSPARENT);
            showStatusBarView(activity, color);
            autoFitsSystemWindows(activity, false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTransparentAboveKitkat(activity);
            showStatusBarView(activity, color);
            AndroidBug5497Workaround.assistActivity(activity);
            autoFitsSystemWindows(activity, false);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void setTransparentWithInput(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTransparentAboveKitkat(activity);
            showStatusBarView(activity, color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTransparentAboveKitkat(activity);
            showStatusBarView(activity, color);
        }
        autoFitsSystemWindows(activity, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        AndroidBug5497Workaround.assistActivity(activity);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setTransparentAboveLollipop(Activity activity, int color) {
        Window window = activity.getWindow();
        window.setStatusBarColor(color);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setStatusBarMode(Fragment fragment, boolean darkMode) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            setStatusBarMode(activity, darkMode);
        }
    }

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

    @TargetApi(Build.VERSION_CODES.M)
    private static void setStatusBarDarkModeNative(Activity activity, boolean darkMode) {
        Window window = activity.getWindow();
        View decor = window.getDecorView();
        if (darkMode) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
