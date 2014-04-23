package org.motechproject.ivr.osgi;

import com.google.gson.JsonParser;
import org.apache.http.impl.client.BasicResponseHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.ivr.service.contract.CallRecordsService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.TestContext;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import javax.inject.Inject;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CallLogBundleIT extends BasePaxIT {

    @Inject
    private CallRecordsService callRecordsService;

    @Override
    protected boolean shouldFakeModuleStartupEvent() {
        return true;
    }

    @Test
    public void testCalllogSearch() throws IOException, InterruptedException {
        String response = getHttpClient().get(String.format("http://localhost:%d/ivr/api/calllog/search",
                TestContext.getJettyPort()), new BasicResponseHandler());
        assertTrue(new JsonParser().parse(response).isJsonObject());
    }

    @Test
    public void testThatCallRecordsServiceIsAvailable() {
        assertNotNull(callRecordsService);
    }
}
