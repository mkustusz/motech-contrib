package org.motechproject.ivr.repository;

import com.github.ldriscoll.ektorplucene.CouchDbRepositorySupportWithLucene;
import com.github.ldriscoll.ektorplucene.CustomLuceneResult;
import com.github.ldriscoll.ektorplucene.LuceneAwareCouchDbConnector;
import com.github.ldriscoll.ektorplucene.LuceneQuery;
import com.github.ldriscoll.ektorplucene.designdocument.annotation.FullText;
import com.github.ldriscoll.ektorplucene.designdocument.annotation.Index;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.support.View;
import org.joda.time.DateTime;
import org.motechproject.ivr.domain.CallDetailRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.motechproject.commons.date.util.DateUtil.setTimeZoneUTC;

@Repository
public class AllCallDetailRecords extends CouchDbRepositorySupportWithLucene<CallDetailRecord> {

    private static final int MAX_CALL_DURATION = 1000;

    @Autowired
    protected AllCallDetailRecords(@Qualifier("callLogDbConnector") CouchDbConnector db) throws IOException {
        super(CallDetailRecord.class, new LuceneAwareCouchDbConnector(db.getDatabaseName(), new StdCouchDbInstance(db.getConnection())));
        initStandardDesignDocument();
    }


    @View(name = "countLogs", map = "function(doc){ emit(null, 1);}", reduce = "_count")
    public long countRecords(String phoneNumber, DateTime startTime, DateTime endTime, Integer minDurationInSeconds, Integer maxDurationInSeconds, List<String> dispositions) {
        StringBuilder queryString = generateQueryString(phoneNumber, startTime, endTime, minDurationInSeconds, maxDurationInSeconds, dispositions);
        return runQuery(queryString, 0, 0, null, false).size();
    }

    @View(name = "maxCallDuration", map = "function(doc){ emit(null, doc.duration);}",
            reduce = "function (key, values) {" +
                    "    var max = 0;" +
                    "    for(var i = 0; i < values.length; i++) {" +
                    "        max = Math.max(values[i], max);" +
                    "    }" +
                    "    return max;" +
                    "}")
    public long findMaxCallDuration() {
        ViewResult result = db.queryView(createQuery("maxCallDuration").reduce(true));
        if (!result.isEmpty() && result.iterator().hasNext()) {
            return Integer.valueOf(result.iterator().next().getValue());
        }
        return 0;
    }


    @View(name = "by_phoneNumber", map = "function(doc) { emit(doc.phoneNumber, doc.phoneNumber); }", reduce = "function() { return null; }")
    public List<String> getAllPhoneNumbers() {
        ViewQuery query = createQuery("by_phoneNumber").group(true);
        ViewResult r = db.queryView(query);
        List<String> allPhoneNumbers = new ArrayList<>();
        for (ViewResult.Row row : r.getRows()) {
            allPhoneNumbers.add(row.getKey());
        }
        return allPhoneNumbers;
    }


    @FullText({@Index(
            name = "search",
            index = "function(doc) { " +
                    "var ret=new Document(); " +
                    "ret.add(doc.phoneNumber,{'field':'phoneNumber'}); " +
                    "ret.add(doc.startDate, {'type':'date', 'field':'startDate'});" +
                    "ret.add(doc.duration, {'type':'int', 'field':'duration'}); " +
                    "ret.add(doc.disposition,{'field':'disposition'}); " +
                    "return ret " +
                    "}"
    )})
    public List<CallDetailRecord> search(String phoneNumber, DateTime startTime, DateTime endTime, Integer minDurationInSeconds, Integer maxDurationInSeconds, List<String> dispositions, int page, int pageSize, String sortby, boolean reverse) {

        StringBuilder queryString = generateQueryString(phoneNumber, setTimeZoneUTC(startTime), setTimeZoneUTC(endTime), minDurationInSeconds, maxDurationInSeconds, dispositions);

        return runQuery(queryString, page, pageSize, sortby, reverse);
    }


    @View(name = "by_call_id", map = "function(doc) { emit(doc.callId); }")
    public CallDetailRecord findByCallId(String callId) {
        return singleResult(queryView("by_call_id", callId));
    }


    public CallDetailRecord findOrCreate(String callId, String phoneNumber) {
        CallDetailRecord callDetailRecord = findByCallId(callId);
        if (callDetailRecord == null) {
            callDetailRecord = new CallDetailRecord(callId, phoneNumber);
            add(callDetailRecord);
        }
        return callDetailRecord;
    }

    private StringBuilder generateQueryString(String phoneNumber, DateTime startTime, DateTime endTime, Integer minDurationInSeconds, Integer maxDurationInSeconds, List<String> dispositions) {
        StringBuilder queryString = new StringBuilder();
        if (maxDurationInSeconds != null && minDurationInSeconds != null) {
            queryString.append(String.format("duration<int>:[%d TO %d]", minDurationInSeconds, maxDurationInSeconds));
        } else {
            queryString.append(String.format("duration<int>:[%d TO %d]", 0, MAX_CALL_DURATION));
        }
        if (StringUtils.isNotBlank(phoneNumber)) {
            if (queryString.length() > 0) {
                queryString.append(" AND ");
            }
            queryString.append(String.format(" phoneNumber:%s", phoneNumber));
        }
        if (startTime != null && endTime != null) {
            if (queryString.length() > 0) {
                queryString.append(" AND ");
            }
            queryString.append(String.format("startDate<date>:[%s TO %s]", startTime.toString("yyyy-MM-dd'T'HH:mm:ss"), endTime.toString("yyyy-MM-dd'T'HH:mm:ss")));
        }
        addDispositionFilter(dispositions, queryString);
        return queryString;
    }


    private void addDispositionFilter(List<String> dispositions, StringBuilder queryString) {
        if (isNotEmpty(dispositions)) {
            queryString.append(" AND (");
            for (int i = 0; i < dispositions.size(); i++) {
                if (i > 0) {
                    queryString.append(" OR ");
                }
                queryString.append("disposition:").append(dispositions.get(i));
            }
            queryString.append(")");
        }
    }

    private List<CallDetailRecord> runQuery(StringBuilder queryString, int page, int pageSize, String sortBy, boolean reverse) {
        LuceneQuery query = new LuceneQuery("CallDetailRecord", "search");
        query.setQuery(queryString.toString());
        query.setStaleOk(false);
        query.setIncludeDocs(true);
        if (pageSize > 0) {
            query.setLimit(pageSize);
            query.setSkip(page * pageSize);
        }
        if (!isBlank(sortBy)) {
            String sortString = reverse ? "\\" + sortBy : sortBy;
            query.setSort(sortString);
        }
        TypeReference<CustomLuceneResult<CallDetailRecord>> typeRef
                = new TypeReference<CustomLuceneResult<CallDetailRecord>>() {
        };
        CustomLuceneResult<CallDetailRecord> result = db.queryLucene(query, typeRef);
        return convert2Calllogs(result.getRows());
    }

    private List<CallDetailRecord> convert2Calllogs(List<CustomLuceneResult.Row<CallDetailRecord>> logRows) {
        List<CallDetailRecord> callLogs = new ArrayList<>();
        for (CustomLuceneResult.Row<CallDetailRecord> row : logRows) {
            callLogs.add(row.getDoc());
        }
        return callLogs;
    }

    protected CallDetailRecord singleResult(List<CallDetailRecord> resultSet) {
        return (resultSet == null || resultSet.isEmpty()) ? null : resultSet.get(0);
    }
}
