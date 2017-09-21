package com.example.ningxiang.autofill;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class ForegroundWindowDetectionService extends AccessibilityService {

    public static final String TAG = "ForegroundWindowDetectionService";

    private LruCache<Integer, ComponentName> mRecentWindows;
    private int mForegroundId = -1;
    public ForegroundWindowDetectionService() {
        mRecentWindows = new LruCache<>(50);
    }

    @Override
    public void onCreate() {
        log("on create!");
    }

    @Override
    public void onInterrupt() {
        log("on interrupt!");
    }

    @Override
    public void onDestroy() {
        log("on destroy!");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        log("on unbind!");
        return super.onUnbind(intent);
    }

    public static void log(CharSequence... msg){
        Log.i(TAG, TextUtils.join(" ", msg));
    }

    void printEventInfo(AccessibilityEvent accessibilityEvent) {
        log( "---------------------------------------------------");
        log( "onAccessibilityEvent : "+ AccessibilityEvent.eventTypeToString(accessibilityEvent.getEventType()));
        log( "Text :" + accessibilityEvent.getText());
        log( "Content description :" + accessibilityEvent.getContentDescription());
        log( "event class = " + accessibilityEvent.getPackageName() + "/" + accessibilityEvent.getClassName());
        log( "event window id = " + accessibilityEvent.getWindowId());
        AccessibilityNodeInfo source = accessibilityEvent.getSource();
        if (source != null) {
            log( "source class = " + source.getPackageName() + "/" + source.getClassName());
            log( "source window id = " + source.getWindowId());
        } else {
            log( "source is null");
        }

        AccessibilityNodeInfo active = getRootInActiveWindow();
        if (active != null) {
            log( "active window id = " + active.getWindowId());
            log( "active node class = " + active.getPackageName() + "/" + active.getClassName());
        } else {
            log( "active is null");
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                onWindowStateChanged(event);
        }
    }

    private void onWindowStateChanged(AccessibilityEvent e) {
        log("-----------------onWindowStateChanged start--------------------");
        int windowId = e.getWindowId();
        if (windowId == -1) {
            log("window id is -1, break update window!");
            return;
        }

        if (TextUtils.isEmpty(e.getPackageName()) || TextUtils.isEmpty(e.getClassName())) {
            log("pkg or cls is empty, break update window!");
            return;
        }

        String pkg = e.getPackageName().toString();
        String cls = e.getClassName().toString();

        /*if (cls.startsWith("android.")) {
            log("replace cls ", cls, " with window_" + windowId);
            cls = "window_" + windowId;
        }*/

        ComponentName cm = new ComponentName(pkg, cls);
        ComponentName oldCompoent = mRecentWindows.get(windowId);
        if (!cm.equals(oldCompoent)) { // not exist, put in the new window.
            log("put new window : "+ windowId +"\n" + cm);
            mRecentWindows.put(windowId, cm);
        }

        AccessibilityNodeInfo active = getRootInActiveWindow();
        log("active root node info :" + toString(active));
        if (active != null) {
            int activeId = active.getWindowId();
            if (activeId == windowId){
                log("mark foreground id : " + windowId);
                mForegroundId = windowId;
            } else {
                log("This window is not active window : " + cm);
            }

            log("invoke onForegroundWindowUpdate : " + activeId);
            onForegroundWindowUpdate(active);
        }
    }

    public ComponentName getForegroundWindow() {
        return mRecentWindows.get(mForegroundId);
    }

    public ComponentName getWindow(int windowId) {
        return mRecentWindows.get(windowId);
    }

    public void onForegroundWindowUpdate(AccessibilityNodeInfo activeRootNode) {
    }

    public String toString(AccessibilityNodeInfo node) {
        StringBuilder s = new StringBuilder("\n{");
        if (node != null) {
            s.append("class = ").append(node.getPackageName()).append("/").append(node.getClassName());
            s.append("\nwindow id = ").append(node.getWindowId());
            s.append("\nresource id = ").append(node.getViewIdResourceName());
            s.append("\ncontent description = ").append(node.getContentDescription());
            s.append("\ntext = ").append(node.getText());
        }
        s.append("}");
        return s.toString();
    }
}
