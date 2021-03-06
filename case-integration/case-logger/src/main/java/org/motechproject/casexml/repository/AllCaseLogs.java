package org.motechproject.casexml.repository;

import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.View;
import org.motechproject.casexml.domain.CaseLog;
import org.motechproject.dao.MotechBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AllCaseLogs extends MotechBaseRepository<CaseLog> {

    @Autowired
    public AllCaseLogs(@Qualifier("caseLogConnector") CouchDbConnector db) {
        super(CaseLog.class, db);
    }

    @View(name = "all_case_logs", map = "function(doc) {if (doc.type ==='CaseLog') {emit(null, doc._id);}}")
    public List<CaseLog> getLatestLogs(int limit) {
        ViewQuery q = createQuery("all_case_logs").limit(limit).includeDocs(true);
        return db.queryView(q, CaseLog.class);
    }

    @View(name = "find_by_entity_id_and_request_type", map = "function(doc) {if (doc.type ==='CaseLog') {emit([doc.entityId, doc.requestType], doc._id);}}")
    public List<CaseLog> filterByEntityIdAndRequestType(String entityId, String requestType, int limit) {
        ViewQuery q = createQuery("find_by_entity_id_and_request_type").startKey(ComplexKey.of(entityId, requestType)).endKey(ComplexKey.of(entityId, requestType)).limit(limit).includeDocs(true);
        return db.queryView(q, CaseLog.class);
    }

    @View(name = "find_by_entity_id", map = "function(doc) {if (doc.type ==='CaseLog') {emit(doc.entityId, doc._id);}}")
    public List<CaseLog> filterByEntityId(String entityId, int limit) {
        ViewQuery q = createQuery("find_by_entity_id").key(entityId).limit(limit).includeDocs(true);
        return db.queryView(q, CaseLog.class);
    }

    @View(name = "find_by_request_type", map = "function(doc) {if (doc.type ==='CaseLog') {emit(doc.requestType, doc._id);}}")
    public List<CaseLog> filterByRequestType(String requestType, int limit) {
        ViewQuery q = createQuery("find_by_request_type").key(requestType).limit(limit).includeDocs(true);
        return db.queryView(q, CaseLog.class);
    }
}
