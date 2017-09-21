package com.example.ningxiang.autofill;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfigActivity extends AppCompatActivity {

    @Bind(R.id.title)
    protected TextView mAppName;

    @Bind(R.id.app_icon)
    protected ImageView mIcon;

    @Bind(R.id.list_inputs)
    protected RecyclerView mInputs;

    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        int sceneId = getIntent().getIntExtra("scene_id", -1);
        Scene scene = SceneManager.getInstance(this).getSceneById(sceneId);

        PackageManager pm = getPackageManager();
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

            mIcon.setImageDrawable(appInfo.loadIcon(pm));
            mAppName.setText(appInfo.loadLabel(pm).toString());
        }

        mInputs.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyAdapter(this, scene);
        mInputs.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @OnClick(R.id.save)
    public void onClick(View v) {
        if (adapter.getItemCount() > 0) {
            adapter.mScene.setAutoFill(true);
            adapter.mScene.setData(adapter.mInputsData);
        }

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        private Context mContext;

        private Scene mScene;
        private List<Scene.InputNode> mInputs;
        private String[] mInputsData;

        public MyAdapter(Context context, Scene scene) {
            mContext = context;
            mScene = scene;
            mInputs = scene.getInputNodes();
            mInputsData = scene.peekData();
            if (mInputsData == null || mInputsData.length < 1) {
                mInputsData = new String[mInputs.size()];
            }
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.input_item, parent, false);
            final MyHolder holder = new MyHolder(v);
            holder.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (holder.position != -1) {
                        mInputsData[holder.position] = s.toString();
                    }
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            holder.position = position;
            holder.editText.setText(mInputsData[position]);
        }

        @Override
        public int getItemCount() {
            return mInputs != null ? mInputs.size() : 0;
        }
    }

    public static class MyHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.input_item_edit)
        EditText editText;

        int position = -1;

        public MyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
