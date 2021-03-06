package org.motechproject.couchdb.lucene.repository;

import com.github.ldriscoll.ektorplucene.CustomLuceneResult;
import com.github.ldriscoll.ektorplucene.LuceneAwareCouchDbConnector;
import com.github.ldriscoll.ektorplucene.LuceneQuery;
import org.codehaus.jackson.type.TypeReference;
import org.ektorp.support.TypeDiscriminator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.couchdb.lucene.query.QueryDefinition;
import org.motechproject.couchdb.lucene.query.field.Field;
import org.motechproject.couchdb.lucene.query.field.QueryField;
import org.motechproject.couchdb.lucene.query.field.RangeField;
import org.motechproject.couchdb.lucene.util.WhiteSpaceEscape;
import org.motechproject.model.MotechBaseDataObject;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.couchdb.lucene.query.field.FieldType.STRING;

public class LuceneAwareMotechBaseRepositoryTest {

    @Mock
    LuceneAwareCouchDbConnector connector;

    @Mock
    WhiteSpaceEscape whiteSpaceEscape;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldFilterResultsBasedOnQueryDefinition() {
        Map<String, Object> filterParams = buildFilterParameters();

        LuceneAwareMotechBaseRepositoryImpl repository = new LuceneAwareMotechBaseRepositoryImpl(connector, whiteSpaceEscape);

        Map<String, Object> expectedFilterParams = new HashMap<>();
        expectedFilterParams.put("field1", "value1");
        expectedFilterParams.put("field2From", "value2");
        expectedFilterParams.put("field2To", "value3");

        QueryDefinitionImpl queryDefinition = new QueryDefinitionImpl();
        LuceneQuery expectedQuery = expectedLuceneQuery(queryDefinition.viewName(), queryDefinition.searchFunctionName());
        Entity entity = new Entity("value1", "value2");
        List<Entity> expectedResult = createExpectedResult(entity);
        CustomLuceneResult<Entity> luceneResult = createLuceneResult(entity);

        when(connector.queryLucene(expectedQuery, repository.getTypeReference())).thenReturn(luceneResult);
        when(whiteSpaceEscape.escape(filterParams)).thenReturn(expectedFilterParams);

        assertEquals(expectedResult, repository.filter(queryDefinition, filterParams, null, 0, 10));
        verify(connector).queryLucene(expectedQuery, repository.getTypeReference());
        verify(whiteSpaceEscape).escape(filterParams);
    }

    @Test
    public void shouldFilterAndSortResultsBasedOnQueryDefinition() {
        Map<String, Object> filterParams = buildFilterParameters();

        LuceneAwareMotechBaseRepositoryImpl repository = new LuceneAwareMotechBaseRepositoryImpl(connector, whiteSpaceEscape);
        repository.whiteSpaceEscape = whiteSpaceEscape;

        LinkedHashMap<String, Object> sortParams = new LinkedHashMap<>();
        sortParams.put("field1", "ASC");

        QueryDefinitionImpl queryDefinition = new QueryDefinitionImpl();
        LuceneQuery expectedQuery = expectedLuceneQuery(queryDefinition.viewName(), queryDefinition.searchFunctionName());
        expectedQuery.setSort("/field1<string>");

        Entity entity = new Entity("value1", "value2");
        List<Entity> expectedResult = createExpectedResult(entity);
        CustomLuceneResult<Entity> luceneResult = createLuceneResult(entity);

        when(connector.queryLucene(expectedQuery, repository.getTypeReference())).thenReturn(luceneResult);
        when(whiteSpaceEscape.escape(filterParams)).thenReturn(filterParams);

        assertEquals(expectedResult, repository.filter(queryDefinition, filterParams, sortParams, 0, 10));
        verify(connector).queryLucene(expectedQuery, repository.getTypeReference());
    }

    @Test
    public void shouldGetTotalRecordsForGivenFilter(){
        Map<String, Object> filterParams = buildFilterParameters();

        LuceneAwareMotechBaseRepositoryImpl repository = new LuceneAwareMotechBaseRepositoryImpl(connector, whiteSpaceEscape);
        repository.whiteSpaceEscape = whiteSpaceEscape;

        QueryDefinitionImpl queryDefinition = new QueryDefinitionImpl();
        LuceneQuery expectedQuery = expectedLuceneQuery(queryDefinition.viewName(), queryDefinition.searchFunctionName());
        expectedQuery.setLimit(1);

        Entity entity = new Entity("value1", "value2");
        CustomLuceneResult<Entity> luceneResult = createLuceneResult(entity);
        luceneResult.setTotalRows(1);

        when(connector.queryLucene(expectedQuery, repository.getTypeReference())).thenReturn(luceneResult);
        when(whiteSpaceEscape.escape(filterParams)).thenReturn(filterParams);

        assertEquals(1, repository.count(queryDefinition, filterParams));
        verify(connector).queryLucene(expectedQuery, repository.getTypeReference());

    }

    private Map<String, Object> buildFilterParameters() {
        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("field1", "value1");
        filterParams.put("field2From", "value2");
        filterParams.put("field2To", "value3");
        return filterParams;
    }

    private List<Entity> createExpectedResult(Entity entity) {
        List<Entity> expectedResult = new ArrayList<>();
        expectedResult.add(entity);
        return expectedResult;
    }

    private LuceneQuery expectedLuceneQuery(String viewName, String searchFunctionName) {
        LuceneQuery expectedQuery = new LuceneQuery(viewName, searchFunctionName);
        String queryString = "field1:value1 AND field2<string>:[value2 TO value3]";
        expectedQuery.setQuery(queryString.toString());
        expectedQuery.setIncludeDocs(true);
        expectedQuery.setLimit(10);
        expectedQuery.setSkip(0);
        return expectedQuery;
    }

    private CustomLuceneResult<Entity> createLuceneResult(Entity entity) {
        CustomLuceneResult<Entity> luceneResult = new CustomLuceneResult<Entity>();
        CustomLuceneResult.Row<Entity> row = new CustomLuceneResult.Row<>();
        row.setDoc(entity);
        List<CustomLuceneResult.Row<Entity>> rows = new ArrayList<>();
        rows.add(row);
        luceneResult.setRows(rows);
        return luceneResult;
    }
}

@TypeDiscriminator("doc.type == 'Entity'")
class Entity extends MotechBaseDataObject {
    public String field1;
    public String field2;

    Entity(String field1, String field2) {
        this.field1 = field1;
        this.field2 = field2;
    }
}

class LuceneAwareMotechBaseRepositoryImpl extends LuceneAwareMotechBaseRepository<Entity> {


    protected LuceneAwareMotechBaseRepositoryImpl(LuceneAwareCouchDbConnector db, WhiteSpaceEscape whiteSpaceEscape) {
        super(Entity.class, db, whiteSpaceEscape);
    }

    @Override
    protected TypeReference getTypeReference() {
        return new TypeReference<CustomLuceneResult<Entity>>() {
            public boolean equals(Object obj) {
                return this.getClass().equals(obj.getClass());
            }
        };
    }
}

class QueryDefinitionImpl implements QueryDefinition {

    @Override
    public List<Field> fields() {
        List<Field> queryFields = new ArrayList<>();
        queryFields.add(new QueryField("field1", STRING));
        queryFields.add(new RangeField("field2", STRING, "field2From", "field2To"));
        return queryFields;
    }

    @Override
    public String viewName() {
        return "VIEW_NAME";
    }

    @Override
    public String searchFunctionName() {
        return "SEARCH_NAME";
    }

    @Override
    public String indexFunction() {
        return "function(doc) { " +
                    "var index=new Document(); " +
                    "index.add(doc.field1, {field: 'field1'}); " +
                    "index.add(doc.field2, {field: 'field2'}); " +
                    "return index;" +
                "}";
    }
}
