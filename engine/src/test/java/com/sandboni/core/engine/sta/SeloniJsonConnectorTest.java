package com.sandboni.core.engine.sta;

import com.sandboni.core.engine.contract.HttpConsts;
import com.sandboni.core.engine.sta.connector.Connector;
import com.sandboni.core.engine.sta.connector.HttpTemplateConnector;
import com.sandboni.core.engine.sta.graph.Link;
import com.sandboni.core.engine.sta.graph.LinkFactory;
import com.sandboni.core.engine.sta.graph.LinkType;
import com.sandboni.core.engine.sta.graph.vertex.CucumberVertex;
import com.sandboni.core.engine.sta.graph.vertex.TestVertex;
import com.sandboni.core.engine.sta.graph.vertex.Vertex;
import com.sandboni.core.scm.scope.ChangeScopeImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.*;

public class SeloniJsonConnectorTest {

    private String sampleVerb =  HttpConsts.getHttpVerb().stream().findFirst().get();

    private Connector httpTemplateConnector;

    private Context setupContext(String ... calleeActions) {
        Context context = new Context(new String[0], new String[0], "", new ChangeScopeImpl(), "./src/test/resources/Seloni.json");

        if (Objects.nonNull(calleeActions)) {

            for (String calleeAction : calleeActions) {
                context.addLink(LinkFactory.createInstance(
                        context.getApplicationId(),
                        new Vertex.Builder(sampleVerb + " " + HttpConsts.HTTP_LOCALHOST, calleeAction)
                                .markSpecial()
                                .build(),
                        new Vertex.Builder("callee", "action").build(),
                        LinkType.HTTP_HANLDER));
            }
        }
        return context;
    }

    @Before
    public void setup(){
        httpTemplateConnector = new HttpTemplateConnector();
    }


    @Test
    public void testNoMatchedUrls(){
        Context context = setupContext("/no/url/should/match");
        httpTemplateConnector.connect(context);

        assertEquals(5, context.getLinks().count());
    }



    @Test
    public void testMultipleMatchedUrlsInOneEntry(){
        Context context = setupContext("/rest/unprocess/count/DND", "/rest/unprocess/count/DND1");
        httpTemplateConnector.connect(context);

        assertEquals(8, context.getLinks().count());

        Optional<Link> result = context.getLinks().filter(l -> l.getCaller().getAction().equals("test-name") && l.getCallee().getAction().equals("/rest/unprocess/count/DND") && l.getLinkType() == LinkType.HTTP_MAP_SELONI).findFirst();
        Assert.assertTrue(result.isPresent());

        result = context.getLinks().filter(l -> l.getCaller().getAction().equals("test-name") && l.getCallee().getAction().equals("/rest/unprocess/count/DND1") && l.getLinkType() == LinkType.HTTP_MAP_SELONI).findFirst();
        Assert.assertTrue(result.isPresent());
    }


    @Test
    public void testMultipleLinksCucumber(){
        Context context = setupContext("/rest/unprocess/count/NEW");
        httpTemplateConnector.connect(context);

        assertEquals(7, context.getLinks().count());

        Optional<Link> result = context.getLinks().filter(l -> l.getCaller().getAction().equals("test-name") && l.getCallee().getAction().equals("/rest/unprocess/count/NEW") && l.getLinkType() == LinkType.HTTP_MAP_SELONI).findFirst();
        Assert.assertTrue(result.isPresent());
    }

    @Test
    public void testLinkCucumber(){
        Context context = setupContext("/rest/unprocess/count/ABC");
        httpTemplateConnector.connect(context);

        assertEquals(6, context.getLinks().count());

        Optional<Link> result = context.getLinks().filter(l -> l.getCaller().getAction().equals("test-name3") && l.getCallee().getAction().equals("/rest/unprocess/count/ABC") && l.getLinkType() == LinkType.HTTP_MAP_SELONI).findFirst();
        Assert.assertTrue(result.isPresent());

        Link link = result.get();

        assertTrue(link.getCaller() instanceof  CucumberVertex);

        assertEquals(9, ((CucumberVertex)link.getCaller()).getScenarioLine());
        assertEquals("I:\\code\\seloni-demo-test\\className2.class", ((CucumberVertex)link.getCaller()).getFeaturePath());
        assertTrue(((CucumberVertex)link.getCaller()).isExternalLocation());
    }

    @Test
    public void testAllFieldPopulated() {
        Context context = setupContext("/rest/unprocess/count/ABC");
        httpTemplateConnector.connect(context);

        Optional<Link> result = context.getLinks().filter(l -> l.getCaller().getAction().equals("test-name3") && l.getCallee().getAction().equals("/rest/unprocess/count/ABC") && l.getLinkType() == LinkType.HTTP_MAP_SELONI).findFirst();
        Assert.assertTrue(result.isPresent());

        Vertex v = result.get().getCaller();
        assertTrue(v instanceof CucumberVertex);
        assertEquals("test-name3", v.getAction());
        assertEquals("className2", v.getActor());
        assertEquals("I:\\code\\seloni-demo-test\\className2.class", ((CucumberVertex)v).getFeaturePath());
        assertEquals(9, ((CucumberVertex)v).getScenarioLine());
    }


    @Test
    public void testLinkJunit(){
        Context context = setupContext("/rest/unprocess/count/junit/1");
        httpTemplateConnector.connect(context);

        assertEquals(6, context.getLinks().count());

        Optional<Link> result = context.getLinks().filter(l -> l.getCaller().getAction().equals("test-name4") && l.getCallee().getAction().equals("/rest/unprocess/count/junit/1") && l.getLinkType() == LinkType.HTTP_MAP_SELONI).findFirst();
        Assert.assertTrue(result.isPresent());

        Link link = result.get();

        assertTrue(link.getCaller() instanceof TestVertex);
        assertTrue(((TestVertex)link.getCaller()).isExternalLocation());
    }

    @Test
    public void testNoLinkCucumber() {
        Context context = setupContext("/handler/with/no/request");
        httpTemplateConnector.connect(context);

        assertEquals(5, context.getLinks().count());
    }

}
