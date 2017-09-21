package com.example.ningxiang.autofill;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Map;

/**
 * Created by ningxiang on 3/31/17.
 */

public class SceneManager {

    static void log(String... msg) {
        Log.d("SceneManager", TextUtils.join(" ", msg));
    }

    private static SceneManager sInstance;

    private Context mContext;

    private DataBaseOpenHelper mDataBaseOpenHelper;

    private SceneManager(Context context) {
        mContext = context;
        mDataBaseOpenHelper = new DataBaseOpenHelper(context);
    }

    public static SceneManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SceneManager.class) {
                if (sInstance == null) {
                    sInstance = new SceneManager(context.getApplicationContext());
                }
            }
        }

        return sInstance;
    }

    private LruCache<Integer, Scene> scenes = new LruCache<>(30);
    private Map<Integer, Scene> mapOfScenes = Utils.getLruCacheMap(scenes);

    public Scene getScene(ComponentName componentName, List<Scene.InputNode> nodes) {

        if (!fillIsEnableForPackage(componentName.getPackageName())) {
            return null;
        }

        Scene scene = new Scene(mContext, componentName, nodes);
        for (Scene item : mapOfScenes.values()) {
            if (scene.equals(item)) {
                log("found scene from memory!" + item);
                return item;
            }
        }

        //not found in memory, load info from persist storage.
        boolean succeed/* = updateSceneFromDb(scene)*/;
        succeed = scene.load(mDataBaseOpenHelper);

        if (!succeed) { //save to persist storage.
//            succeed = insertSceneToDb(scene);
            succeed = scene.save(mDataBaseOpenHelper);
        }

        if (succeed) {
            log("put scene to memory!" + scene);
            scenes.put(scene.getId(), scene);
            return scene;
        }

        return null;
    }

    public Scene getSceneById(int id) {
        return scenes.get(id);
    }

    public boolean insertSceneToDb(Scene scene) {
        SQLiteDatabase db = mDataBaseOpenHelper.getWritableDatabase();
        mDataBaseOpenHelper.getReadableDatabase();
        ComponentName window = scene.getWindowName();

        ContentValues values = new ContentValues();
        values.put("package", window.getPackageName());
        values.put("class", window.getClassName());
        values.put("inputs_hashcode", scene.getInputNodes().hashCode());
        values.put("auto_fill", scene.isAutoFill());
        values.put("no_ask", scene.isNoAsk());

        if (scene.peekData() != null) {
            values.put("data", TextUtils.join(" ", scene.peekData()));
        }

        long id = db.insertWithOnConflict(DataBaseOpenHelper.SCENE_TABLE, null, values,
                SQLiteDatabase.CONFLICT_NONE);
        scene.setId((int) id);
        log("inset scene data to db, row id = " + id);

        return id != -1;
    }

    public boolean updateSceneToDb(Scene scene) {
//        SQLiteDatabase db = mDataBaseOpenHelper.getWritableDatabase();

        return scene.save(mDataBaseOpenHelper);

       /* StringBuilder where = new StringBuilder();

        if (scene.getId() != -1) {
            where.append("_id="+scene.getId());
        } else {
            ComponentName window = scene.getWindowName();
            where.append("package='"+window.getPackageName()+"'");
            where.append(" AND class='"+window.getClassName()+"'");
            where.append(" AND inputs_hashcode="+scene.getInputNodes().hashCode());
        }

        log("where : " + where);
        Cursor cursor = db.query(DataBaseOpenHelper.SCENE_TABLE, null, where.toString(), null, null, null, null);
        if (cursor == null || cursor.getCount() < 1) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put("auto_fill", scene.isAutoFill());
        values.put("no_ask", scene.isNoAsk());

        if (scene.peekData() != null) {
            values.put("data", TextUtils.join("\\u0", scene.peekData()));
        }

        int affectCount = db.updateWithOnConflict(DataBaseOpenHelper.SCENE_TABLE, values, where.toString(), null, 0);
        return affectCount > 0;*/
    }

    private boolean updateSceneFromDb(Scene scene) {
        SQLiteDatabase db = mDataBaseOpenHelper.getWritableDatabase();

        StringBuilder where = new StringBuilder();
        ComponentName window = scene.getWindowName();
        where.append("package='"+window.getPackageName()+"'");
        where.append(" AND class='"+window.getClassName()+"'");
        where.append(" AND inputs_hashcode="+scene.getInputNodes().hashCode());
        log("where : " + where);
        Cursor cursor = db.query(DataBaseOpenHelper.SCENE_TABLE, null, where.toString(), null, null, null, null);
        if (cursor!= null && cursor.moveToFirst()) {
            log("fill scene data from db");
            scene.setId(cursor.getInt(0));
            scene.setAutoFill(cursor.getInt(4) != 0);
            scene.setNoAsk(cursor.getInt(5) != 0);
            String data = cursor.getString(6);
            scene.setData(data != null ? data.split(" ") : null);
            cursor.close();
            return true;
        }

        return false;
    }

    private final String[] ENABLE_PKG = {
            "com.android.mms",
            "com.android.settings"
    };

    public boolean fillIsEnableForPackage(String pkg) {
//        for (String s : ENABLE_PKG) {
//            if (s.equals(pkg)) {
//                return true;
//            }
//        }
//        return false;
        return true;
    }
}
