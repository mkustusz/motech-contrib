package org.motechproject.decisiontree.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.decisiontree.domain.FlowSessionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationDecisionTree.xml")
public class AllCallSessionRecordsIT {

    @Autowired
    private AllFlowSessionRecords allFlowSessionRecords;

    @Test
    public void shouldCreateACallSessionRecordInDb() {
        FlowSessionRecord flowSessionRecord = new FlowSessionRecord("session1");
        ArrayList<String> value = new ArrayList<String>();
        value.add("value1");
        value.add("value2");
        value.add("value3");
        flowSessionRecord.set("key", value);

        allFlowSessionRecords.add(flowSessionRecord);

        List<FlowSessionRecord> flowSessionRecords = allFlowSessionRecords.getAll();
        assertThat(flowSessionRecords.size(), is(1));

        FlowSessionRecord actualRecord = flowSessionRecords.get(0);
        assertThat(actualRecord, is(flowSessionRecord));
        List<String> values = actualRecord.<ArrayList<String>>get("key");
        assertThat(values.size(), is(3));
        assertThat(values.get(0), is("value1"));
        assertThat(values.get(1), is("value2"));
        assertThat(values.get(2), is("value3"));
    }

    @Test
    public void shouldFindOrCreateACallSessionRecord() {
        assertThat(allFlowSessionRecords.getAll().size(), is(0));
        FlowSessionRecord newRecord = allFlowSessionRecords.findOrCreate("session1");
        assertThat(allFlowSessionRecords.getAll().size(), is(1));

        FlowSessionRecord existingRecord = allFlowSessionRecords.findOrCreate("session1");
        assertThat(allFlowSessionRecords.getAll().size(), is(1));
        assertThat(existingRecord, is(equalTo(newRecord)));
    }

    @Test
    public void shouldIgnoreCaseWhileFindOrCreateACallSessionRecord() {
        assertThat(allFlowSessionRecords.getAll().size(), is(0));
        allFlowSessionRecords.findOrCreate("Session1");
        assertThat(allFlowSessionRecords.getAll().size(), is(1));

        allFlowSessionRecords.findOrCreate("SESSION1");
        assertThat(allFlowSessionRecords.getAll().size(), is(1));
    }

    @Test
    public void shouldFindBySessionId() {
        FlowSessionRecord flowSessionRecord1 = new FlowSessionRecord("S1");
        FlowSessionRecord flowSessionRecord2 = new FlowSessionRecord("S2");

        allFlowSessionRecords.add(flowSessionRecord1);
        allFlowSessionRecords.add(flowSessionRecord2);

        FlowSessionRecord actualRecord = allFlowSessionRecords.findBySessionId("s1");

        assertNotNull(actualRecord);
        assertThat(actualRecord, is(flowSessionRecord1));

        FlowSessionRecord nonExistentRecord = allFlowSessionRecords.findBySessionId("s3");
        assertNull(nonExistentRecord);
    }

    @Test
    public void shouldIgnoreCaseWhileFindingBySessionId() {
        FlowSessionRecord flowSessionRecord = new FlowSessionRecord("IGNORE-CASE");

        allFlowSessionRecords.add(flowSessionRecord);

        FlowSessionRecord actualRecord = allFlowSessionRecords.findBySessionId("ignore-CASE");

        assertNotNull(actualRecord);
        assertThat(actualRecord, is(flowSessionRecord));
    }

    @Test
    public void shouldUpdateTheExistingRecord() {
        FlowSessionRecord flowSessionRecord1 = new FlowSessionRecord("S1");

        allFlowSessionRecords.add(flowSessionRecord1);
        FlowSessionRecord actualRecord = allFlowSessionRecords.findBySessionId("s1");
        assertNull(actualRecord.get("k1"));

        actualRecord.set("k1", "v1");
        allFlowSessionRecords.update(actualRecord);

        FlowSessionRecord updatedRecord = allFlowSessionRecords.findBySessionId("s1");
        assertNotNull(updatedRecord.get("k1"));
    }

    @Before
    public void setUp() {
        allFlowSessionRecords.removeAll();
    }

    @After
    public void tearDown() {
        allFlowSessionRecords.removeAll();
    }
}
