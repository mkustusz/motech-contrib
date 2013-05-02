package org.motechproject.crud.service;

import org.motechproject.crud.repository.CrudRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public abstract class CouchDBCrudEntity<T> extends CrudEntity<T>{

    protected CouchDBCrudEntity(CrudRepository crudRepository) {
        super(crudRepository);
    }

    public List<String> getHiddenFields(){
        return asList("_id", "_rev", "type");
    }

    public String getIdFieldName(){
        return "_id";
    }

    public Map<String, String> getDefaultValues(){
        HashMap<String, String> defaults = new HashMap<>();
        defaults.put("type", entityName());
        return defaults;
    }
}
