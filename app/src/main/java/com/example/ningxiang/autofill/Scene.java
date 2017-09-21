package com.example.ningxiang.autofill;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ningxiang on 3/29/17.
 */

public class Scene implements Persistable<DataBaseOpenHelper> {

    public static final String TAG = "Scene";
    public static final String VALUE_DELIMITER = "\0";

    private int id = -1;

    private Context context;
    private ComponentName windowName;
    private List<InputNode> inputNodes;

    private SceneManager sceneManager;

    private boolean autoFill = false;
    private boolean noAsk = false;

    protected boolean dataReady = false;
    private String[] data;

    Scene(Context context, ComponentName componentName, List<InputNode> nodes) {
        this.context = context;
        this.windowName = componentName;
        this.inputNodes = nodes;

        sceneManager = SceneManager.getInstance(context);
    }

    public void setData(String[] data) {
        //replace null with empty String.
        if (data != null) {
            for (int i=0; i<data.length; i++) {
                if (data[i] == null) {
                    data[i] = "";
                }
            }
        }

        this.data = data;
        dataReady = true;
        syncToDb();
    }

    public boolean isDataReady() {
        return dataReady;
    }

    void setDataReady(boolean isReady) {
        dataReady = isReady;
    }

    public String[] useData() {
        dataReady = false;
        String[] data = new String[inputNodes.size()];
        for (int i=0; i<inputNodes.size(); i++) {
            data[i] = inputNodes.get(i).value.toString();
        }
        return data;
    }

    String[] peekData() {
        return data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ComponentName getWindowName() {
        return windowName;
    }

    public List<InputNode> getInputNodes() {
        return inputNodes;
    }

    public boolean isAutoFill() {
        return autoFill;
    }

    public void setAutoFill(boolean autoFill) {
        this.autoFill = autoFill;
        syncToDb();
    }

    public boolean isNoAsk() {
        return noAsk;
    }

    public void setNoAsk(boolean noAsk) {
        this.noAsk = noAsk;
        syncToDb();
    }

    public boolean syncToDb() {
        Log.d(TAG, "syncToDb: " + this);
        return sceneManager.updateSceneToDb(this);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{Scene ");
        s.append("- auto fill = ").append(autoFill);
        s.append("; no ask = ").append(noAsk);
        s.append("; activity name = ").append(windowName);
        s.append("; input nodes = ").append(inputNodes);
        s.append("; data = ").append(Arrays.toString(data));
        s.append("}");
        return s.toString();
    }

    @Override
    public int hashCode() {
        return windowName.hashCode() + inputNodes.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Scene)) {
            return false;
        }

        Scene that = (Scene) o;
        return windowName.equals(that.windowName)
                && inputNodes.equals(that.inputNodes);
    }

    @Override
    public boolean save(DataBaseOpenHelper dataBaseOpenHelper) {
        if (dataBaseOpenHelper == null) {
            return false;
        }

        SQLiteDatabase db = dataBaseOpenHelper.getWritableDatabase();
        if (db != null) {
            StringBuilder where = new StringBuilder();

            if (id != -1) {
                where.append("_id="+id);
            } else {
                where.append("package='"+windowName.getPackageName()+"'");
                where.append(" AND class='"+windowName.getClassName()+"'");
                where.append(" AND inputs_hashcode="+inputNodes.hashCode());
            }

            Cursor cursor = db.query(DataBaseOpenHelper.SCENE_TABLE, null, where.toString(), null, null, null, null);
            int count = cursor.getCount();
            cursor.close();

            ContentValues values = new ContentValues();
            values.put("auto_fill", autoFill);
            values.put("no_ask", noAsk);

            StringBuilder names = new StringBuilder();
            StringBuilder data = new StringBuilder();

            for (int i=0; i<inputNodes.size(); i++) {
                if (i != 0) {
                    names.append(VALUE_DELIMITER);
                    data.append(VALUE_DELIMITER);
                }

                names.append(inputNodes.get(i).name);
                data.append(inputNodes.get(i).value);
            }

            values.put("inputs_name", names.toString());
            values.put("inputs_data", encrypt(data.toString()));

            if (count < 1) {
                //insert
                values.put("package", windowName.getPackageName());
                values.put("class", windowName.getClassName());
                values.put("inputs_hashcode", inputNodes.hashCode());
                id = (int) db.insertWithOnConflict(DataBaseOpenHelper.SCENE_TABLE, null, values,
                        SQLiteDatabase.CONFLICT_NONE);
                return id != -1;
            } else {
                //update
                int affectCount = db.updateWithOnConflict(DataBaseOpenHelper.SCENE_TABLE, values, where.toString(), null, 0);
                return affectCount > 0;
            }
        }

        return false;
    }

    @Override
    public boolean load(DataBaseOpenHelper dataBaseOpenHelper) {
        if (dataBaseOpenHelper == null) {
            return false;
        }

        SQLiteDatabase db = dataBaseOpenHelper.getReadableDatabase();
        if (db != null) {
            StringBuilder where = new StringBuilder();

            if (id != -1) {
                where.append("_id="+id);
            } else {
                where.append("package='"+windowName.getPackageName()+"'");
                where.append(" AND class='"+windowName.getClassName()+"'");
                where.append(" AND inputs_hashcode="+inputNodes.hashCode());
            }

            Cursor cursor = db.query(DataBaseOpenHelper.SCENE_TABLE, null, where.toString(), null, null, null, null);
            if (cursor == null)
                return false;

            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
                autoFill = cursor.getInt(4) != 0;
                noAsk = cursor.getInt(5) != 0;

                String[] names = cursor.getString(6).split(VALUE_DELIMITER);
                String[] data = decrypt(cursor.getString(7)).split(VALUE_DELIMITER);
                this.data = data;
                cursor.close();

                if (inputNodes.size() != names.length || names.length != data.length) {
                    return false;
                }

                for (int i=0; i<inputNodes.size(); i++) {
                    inputNodes.get(i).name = names[i];
                    inputNodes.get(i).value = data[i];
                }
                dataReady = true;
                return true;
            }
        }
        return false;
    }

    protected String encrypt(String data) {
        return data;
    }

    protected String decrypt(String data) {
        return data;
    }

    public static class InputNode {
        public CharSequence name = "";
        public CharSequence cls;
        public CharSequence value = "";
        public Rect boundInParent;
        public String resId;
        public Bundle extra = new Bundle();

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("{InputNode ");
            s.append("- name = ").append(name);
            s.append("; class = ").append(cls);
            s.append("; resource id = ").append(resId);
            s.append("; bound in parent = ").append(boundInParent);
            s.append("; text = ").append(value);
            s.append("}");
            return s.toString();
        }

        @Override
        public int hashCode() {
            return identifyString().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof InputNode)) {
                return false;
            }

            InputNode that = (InputNode) o;
            return identifyString().equals(that.identifyString());
        }

        String identifyString() {
            return "" + cls + resId;
        }
    }
}
