package com.wei.forum.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description //万能包装类
 * create by weilei on 2018/12/13 10:27
 **/
public class ViewObject {
    private Map<String, Object> objects = new HashMap<String, Object>();
    public void set(String key, Object value) {
        objects.put(key, value);
    }

    public Object get(String key) {
        return objects.get(key);
    }
}
