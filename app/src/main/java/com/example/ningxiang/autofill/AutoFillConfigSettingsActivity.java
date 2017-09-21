package com.example.ningxiang.autofill;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by ningxiang on 4/17/17.
 */

public class AutoFillConfigSettingsActivity extends SettingsActivity{

    @Override
    public Intent getIntent() {
        Intent intent = super.getIntent();
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, intent.getExtras());

        intent.putExtra(EXTRA_NO_HEADERS, true);
        intent.putExtra(EXTRA_SHOW_FRAGMENT, AutoFillConfigPreferenceFragment.class.getName());

        return intent;
    }

    @Override
    protected void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayUseLogoEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageManager pm = getPackageManager();

        android.app.ActionBar actionBar = getActionBar();

        int sceneId = getIntent().getIntExtra("scene_id", -1);
        Scene scene = SceneManager.getInstance(this).getSceneById(sceneId);
        //get title info
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageInfo(
                    scene.getWindowName().getPackageName(),
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;

            actionBar.setLogo(appInfo.loadIcon(pm));
            actionBar.setTitle(appInfo.loadLabel(pm).toString());
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return AutoFillConfigPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class AutoFillConfigPreferenceFragment extends PreferenceFragment {

        private Scene scene;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);
            if (v instanceof LinearLayout) {
                ViewGroup contentParent = (ViewGroup) v;
                View buttonBar = inflater.inflate(R.layout.center_button, contentParent, true);
                buttonBar.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSave();
                    }
                });
            }

            return v;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            Context prefContext = getPreferenceManager().getContext();
            /*getPreferenceManager().setPreferences(new PreferenceScreen(getActivity(), null));
            setPreferenceScreen(getPreferenceManager()
                    .createPreferenceScreen(prefContext));*/
            addPreferencesFromResource(R.xml.pref_config);
            PreferenceScreen prefScreen = getPreferenceScreen();

            Bundle args = getArguments();
            if (args != null) {
                int sceneId = args.getInt("scene_id", -1);
                scene = SceneManager.getInstance(getActivity()).getSceneById(sceneId);
                //get title info
                /*Preference title = findPreference("title");
                PackageManager pm = prefContext.getPackageManager();
                PackageInfo pkgInfo = null;
                try {
                    pkgInfo = pm.getPackageInfo(
                            scene.getWindowName().getPackageName(),
                            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                if (pkgInfo != null) {
                    ApplicationInfo appInfo = pkgInfo.applicationInfo;

                    title.setIcon(appInfo.loadIcon(pm));
                    title.setTitle(appInfo.loadLabel(pm).toString());
                }*/
                //get input info
                if (scene != null && scene.getInputNodes() != null) {
                    List<Scene.InputNode> nodes = scene.getInputNodes();
                    for (int i=0; i<nodes.size(); i++) {
//                    for (Scene.InputNode node : nodes) {
                        Scene.InputNode node = nodes.get(i);
                        TitleEditPreference preference = new TitleEditPreference(prefContext);
                        preference.setPersistent(false);

                        node.name = TextUtils.isEmpty(node.name) ? "input_" + i : node.name;
                        preference.setTitle(node.name);
                        preference.setKey(node.name.toString());

                        CharSequence value = node.value != null ? node.value : "";
                        preference.setText(value.toString());

                        preference.getEditText().setInputType(node.extra.getInt("input_type", 0));
                        prefScreen.addPreference(preference);
                    }
                }
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if ("ok".equals(preference.getKey())) {
                onSave();
                return true;
            }
            return super.onPreferenceTreeClick(preference);
        }

        private void onSave() {
            if (checkAndSave()) {
                scene.setAutoFill(true);
                scene.setDataReady(true);
                scene.syncToDb();
                getActivity().finish();
            }
        }

        private boolean checkAndSave() {
            List<Scene.InputNode> nodes = scene.getInputNodes();
            for (Scene.InputNode node : nodes) {
                TitleEditPreference pref = (TitleEditPreference) findPreference(node.name);
                if (pref == null) {
                    continue;
                }

                node.name = pref.getTitle();
                node.value = pref.getText();
            }
            return true;
        }
    }
}
