package com.example.wbdemo.info.maindata;

import java.io.Serializable;

/**
 * Created by zhoujunyu on 2019/5/23.
 */
public class VisibleBean implements Serializable {
    /**
     * type : 0
     * list_id : 0
     */

    private int type;
    private int list_id;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getList_id() {
        return list_id;
    }

    public void setList_id(int list_id) {
        this.list_id = list_id;
    }
}
