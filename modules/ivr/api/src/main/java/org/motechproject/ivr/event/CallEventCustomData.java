package org.motechproject.ivr.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extra data stored as key value pair along with call event.
 */
public class CallEventCustomData implements Serializable {

    private static final long serialVersionUID = -6688004334051287734L;

    private Map<String, List<String>> data = new HashMap<String, List<String>>();

    public void put(String key, String value) {
        if (data.get(key) == null) {
            data.put(key, new ArrayList<String>());
        }
        updateList(key, value);
    }

    private void updateList(String key, String value) {
        List<String> list = data.get(key);
        list.add(value);
    }

    public List<String> getAll(String key) {
        return data.get(key);
    }

    public String getFirst(String key) {
        List<String> all = getAll(key);
        return all == null ? null : all.get(0);
    }

    public Map<String, List<String>> getData() {
        return data;
    }

    public void setData(Map<String, List<String>> data) {
        this.data = data;
    }

    public void update(String key, String value) {
        data.put(key, new ArrayList<String>());
        updateList(key, value);
    }
}
