package eu.janbednar;

import eu.janbednar.module.camelXA.processor.NoOpProcessor;
import eu.janbednar.module.camelXA.routebuilder.MainRouteBuilder;
import eu.janbednar.module.camelXA.transaction.CdiTransactionManager;
import eu.janbednar.module.domain.dao.AbstractDao;
import eu.janbednar.module.domain.dao.TestDao;
import eu.janbednar.module.domain.entity.TestEntity;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.ServiceStatus;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConstants;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.service.ArquillianConfig;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
@RunWith(Arquillian.class)
public class EmptyDeploymentTest {

    private static String webxml = "<web-app></web-app>";
    private static String jmsRouteNonXA = "jms-non-xa:queue:Test?exchangePattern=InOnly";
    private static String jmsOutNonXA = "jms-non-xa:queue:TestOut?exchangePattern=InOnly";
    private static String dlqRoutenonXA = "jms-non-xa:queue:DLQ?exchangePattern=InOnly";
    private static String jbossHome = "C://Portable/wildfly-11.0.0.Final";

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    @Inject
    MainRouteBuilder mainRouteBuilder;

    @Inject
    TestDao testDao;

    ProducerTemplate producerTemplate;
    ConsumerTemplate consumerTemplate;

    @Before
    public void init(){
        producerTemplate = mainRouteBuilder.getContext().createProducerTemplate();
        consumerTemplate = mainRouteBuilder.getContext().createConsumerTemplate();
    }

    @Deployment
    public static Archive deployCamelXA() throws Exception{

        File[] maven = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();

        return ShrinkWrap.create(WebArchive.class, "camelXA.war")
                .addPackage(MainRouteBuilder.class.getPackage()) //routebuilder package
                .addPackage(CdiTransactionManager.class.getPackage()) //transaction package
                .addPackage(AbstractDao.class.getPackage()) //dao package
                .addPackage(TestEntity.class.getPackage()) //entity package
                .addPackage(NoOpProcessor.class.getPackage()) // processor package
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsWebInfResource(new StringAsset(webxml), ArchivePaths.create("web.xml"))
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsLibraries(maven);
    }

    @Test
    public void emptyInContainerTest() throws Exception{
        //Preparation
        clearQueue("seda:dlq");
        clearQueue(jmsRouteNonXA);
        clearQueue(dlqRoutenonXA);
        clearQueue(jmsOutNonXA);

        mainRouteBuilder.getContext().startRoute("input");

        //Test
        String correlationId = UUID.randomUUID().toString();
        System.out.println("correlationId is "+correlationId);
        //producerTemplate.sendBody("direct:hello", "hi");
        producerTemplate.sendBodyAndHeader(jmsRouteNonXA, correlationId, "JMSCorrelationID", correlationId);

        Exchange dlq = consumerTemplate.receive("seda:dlq", 60000);

        System.out.println("received: "+dlq.getIn().getBody(String.class));
        Assert.assertEquals(correlationId, dlq.getIn().getBody(String.class));

        List<TestEntity> dbResultAll = testDao.getByClass(TestEntity.class);
        System.out.println("dbResultAll: "+dbResultAll);

        List<TestEntity> dbResult = testDao.getByValue(correlationId);
        System.out.println("dbResultByValue: "+dbResult);
        Assert.assertEquals(3, dbResult.size());

        //Validate commit to TestOut was not parformed
        Assert.assertNull(consumerTemplate.receiveNoWait(jmsOutNonXA));
    }

    private void clearQueue(String queue){
        System.out.println("About to clear "+queue);
        Exchange toClear;
        while ((toClear = consumerTemplate.receiveNoWait(queue)) != null){
            System.out.println("Body "+toClear.getIn().getBody()+" thrown away from " +queue);
        }
    }
}
