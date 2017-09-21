package com.example.ningxiang.autofill;

/**
 * Created by ningxiang on 3/30/17.
 */

public class DataPersistManager {
        Persistable persistable;

}

interface Persistable<DEST> {
    boolean save(DEST to);
    boolean load(DEST from);
}
