package com.example.ningxiang.autofill;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ningxiang on 3/30/17.
 */

public class AutoFillService extends ForegroundWindowDetectionService {

    private SceneManager sceneManager;

    @Override
    public void onCreate() {
        sceneManager = SceneManager.getInstance(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        super.onAccessibilityEvent(event);
//        printEventInfo(event);
//        AccessibilityNodeInfo activeRoot = getRootInActiveWindow();
//        if (activeRoot != null) {
//            findInputNode(activeRoot);
//        }

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                onWindowContentChanged(event);
        }
    }

    /**
     * record if the scene has been filled for the specific window.<br/>
     * the key present the window id.<br/>
     * the value present the scenes of current window.
     */
    LruCache<Integer, List<Scene>> records = new LruCache<>(30);

    private void onWindowContentChanged(AccessibilityEvent e) {
        log("@@@@@@@@@@@@@@@@@@@onWindowContentChanged start@@@@@@@@@@@@@@@@");
        int windowId = e.getWindowId();
        log("current window : "  + getWindow(windowId));

        AccessibilityNodeInfo activeRootNode = getRootInActiveWindow();
        if (activeRootNode == null) {
            log("active root node is null");
            return;
        }
        int activeWindowId = activeRootNode.getWindowId();
        if (windowId != activeWindowId) {
            log("current source window is not active");
        }

        ComponentName activeWindow = getWindow(activeWindowId);
        if (activeWindow == null) {
            log("current active window is not ready!");
            return;
        }

        if (!sceneManager.fillIsEnableForPackage(activeWindow.getPackageName())) {
            log("current active package don't require auto fill!");
            return;
        }

        if (getPackageName().equals(activeRootNode.getPackageName())) {
            log("current active window is self pkg!");
            return;
        }

        List<AccessibilityNodeInfo> inputs = findInputNode(activeRootNode);
        if (inputs == null || inputs.size() == 0) {
            log("current active window has no input node.");
            return;
        }

        List<Scene.InputNode> sceneNodes = new ArrayList<>(inputs.size());
        for (AccessibilityNodeInfo input : inputs) {
            Scene.InputNode node = new Scene.InputNode();
            node.cls = input.getClassName().toString();
            node.resId = input.getViewIdResourceName();
            node.name = input.getText();

            node.boundInParent = new Rect();
            input.getBoundsInParent(node.boundInParent);

            node.extra.putInt("input_type", input.getInputType());
            sceneNodes.add(node);
        }

        Scene scene = sceneManager.getScene(getWindow(activeWindowId), sceneNodes);

        if (!scene.isAutoFill() && scene.isNoAsk()) {
            //配置为始终关闭自动填写
            log("current scene disable auto fill.");
            return;
        }

        if (scene.isDataReady()) { //data has been ready.
            //fill data. args : input nodes, data.
            log("fill info for scene data is ready!");
            fillInfo(inputs, scene.useData());
            return;
        }

        List<Scene> scenesOfWindow = records.get(activeWindowId);
        if (scenesOfWindow == null) {
            scenesOfWindow = new ArrayList<>();
            records.put(activeWindowId, scenesOfWindow);
        }

        log("current scenes of window :" + activeWindowId);
        log(scenesOfWindow.toString());
        if (scenesOfWindow.contains(scene)) {
            /* || 对于当前窗口已经显示过了  */
            log("current scene has been filled.");
            return;
        }

        //第一次出现该窗口的这个场景
        scenesOfWindow.add(scene);

        if (scene.isAutoFill()) { //scene has been configured.
            //show confirm window.
            //set scene.data
            log("show confirm dialog for scene has been config!");
            ConfirmDialog dialog = new ConfirmDialog(this);
            dialog.setScene(scene);
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
            dialog.show();
        } else if (!scene.isNoAsk()) { //scene configured not been disable.
            //show config window.
            //set origin data.
            log("show config dialog for scene has not been config!");
            ConfigDialog dialog = new ConfigDialog(this);
            dialog.setScene(scene);
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
            dialog.show();
        }
    }

    void showDialog(Scene scene) {
        ConfirmDialog dialog = new ConfirmDialog(this);
        dialog.setScene(scene);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
        dialog.show();
    }

    void fillInfo(List<AccessibilityNodeInfo> nodes, String[] infos) {


        if (infos == null || infos.length == 0) {
            log("fill inputs empty data!");
            return;
        }

        if (nodes == null || nodes.size() == 0) {
            log("fill inputs no input!");
            return;
        }

        Bundle data = new Bundle();
        for (int i=0; i<infos.length && i<nodes.size(); i++) {
            if (TextUtils.isEmpty(infos[i])) {
                log("fill input, skip empty node!");
                continue;
            }

            data.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, infos[i]);

            AccessibilityNodeInfo node = nodes.get(i);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, data);
            node.recycle();
            log("fill input node of : " + (i+1) + " ==== " + infos[i]);
        }
    }

    static final String EDIT_CLASS_NAME = "android.widget.EditText";

    void findEditTextRecursive(AccessibilityNodeInfo parent, List<AccessibilityNodeInfo> nodeInfoList) {

        if (parent == null || nodeInfoList == null) {
            return;
        }

        if (EDIT_CLASS_NAME.equals(parent.getClassName())) {
            log("add EditText input for window " + parent.getWindowId(), toString(parent));
            nodeInfoList.add(parent);
        }

        int childCount = parent.getChildCount();

        for (int i=0; i<childCount; i++) {
            findEditTextRecursive(parent.getChild(i), nodeInfoList);
        }
    }

    List<AccessibilityNodeInfo> findInputNode(AccessibilityNodeInfo root) {
        log("find input for window start ********** : " + root.getWindowId());
        List<AccessibilityNodeInfo> inputNodes = new ArrayList<>();

        if (root == null)
            return inputNodes;

        List<AccessibilityNodeInfo> editTextNodes = new ArrayList<>();
        findEditTextRecursive(root, editTextNodes);

        log("find input for window filter begin ////////");
        for (AccessibilityNodeInfo node : editTextNodes) {
            if (!node.isFocusable()) {
                log("skip node for not focusable : " + toString(node));
            } else if (!node.isEditable()) {
                log("skip node for not editable : " + toString(node));
            } else if (!node.isEnabled()) {
                log("skip node for not enable : " + toString(node));
            } else if (!node.isVisibleToUser()) {
                log("skip node for not visible to user : " + toString(node));
            } else {
                log("add input for window " + root.getWindowId(), toString(node));
                inputNodes.add(node);
            }
        }
        log("find input for window end ********** : " + root.getWindowId());
        return  inputNodes;
    }
}
