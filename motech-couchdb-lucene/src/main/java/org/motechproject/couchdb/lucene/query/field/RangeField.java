package org.motechproject.couchdb.lucene.query.field;

import org.motechproject.couchdb.lucene.query.criteria.Criteria;
import org.motechproject.couchdb.lucene.query.criteria.RangeCriteria;

import java.util.Map;
import java.util.Properties;

public class RangeField extends Field {
    private String fromName;
    private String toName;

    public RangeField(String name, FieldType type, String fromName, String toName) {
        this.name = name;
        this.type = type;
        this.fromName = fromName;
        this.toName = toName;
    }

    @Override
    public boolean presentIn(Map<String, Object> filterParams) {
        return filterParams.containsKey(fromName) && filterParams.containsKey(toName);
    }

    @Override
    public Criteria createCriteria(Map<String, Object> filterParams) {
        Object from = filterParams.get(fromName);
        Object to = filterParams.get(toName);
        return new RangeCriteria(this, from, to);
    }

    @Override
    public String transform(String fieldValue) {
        return type.transform(fieldValue);
    }
}
