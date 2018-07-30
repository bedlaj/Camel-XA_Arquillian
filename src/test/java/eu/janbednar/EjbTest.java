package eu.janbednar;

import eu.janbednar.module.ejbTest.beans.ImplBean;
import eu.janbednar.module.ejbTest.beans.Operation;
import eu.janbednar.module.ejbTest.beans.Processor;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
@RunWith(Arquillian.class)
public class EjbTest {

    private static String webxml = "<web-app></web-app>";
    private static String beanxml = /*"<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\n" +
            "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "       xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee \n" +
            "\t\thttp://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd\"\n" +
            "       bean-discovery-mode=\"all\">\n" +
            "</beans>"*/"";


    @Inject
    CamelContext camelContext;

    @Deployment
    public static Archive deployejbTest() throws Exception{

        File[] maven = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();

        return ShrinkWrap.create(WebArchive.class, "ejbTest.war")
                .addPackage(ImplBean.class.getPackage())
                .addAsWebInfResource(new StringAsset(beanxml), ArchivePaths.create("beans.xml"))
                .addAsWebInfResource(new StringAsset(webxml), ArchivePaths.create("web.xml"))
                //.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsLibraries(maven)
                ;
    }

    @Test
    public void test() throws Exception{

        //Assert.assertEquals(1, CDI.current().getBeanManager().getBeans("implBean").size());
        System.out.println(camelContext.getRegistry().lookupByName("implBean"));
        camelContext.getRegistry().lookupByName("implBean");

        ((Operation)camelContext.getRegistry().lookupByName("implBean")).operation("hello");
//        ((Processor)camelContext.getRegistry().lookupByName("implBean")).process(new Object());
        ((Processor)camelContext.getRegistry().lookupByName("someDummyCdiAndStatelessBean")).process("hello there");

        //implBean.process(new DefaultExchange(new DefaultCamelContext()));

        camelContext.createProducerTemplate().sendBody("bean:implBean", "Hi");
    }

    @Test
    public void testParallelStatelessInjection() throws Throwable{

        ExecutorService executor = Executors.newFixedThreadPool(1000);
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        final List<Throwable> ex = new ArrayList<>();
        for (int i = 0; i<100000; i++){
            int finalI = i;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        producerTemplate.sendBody("bean:implBean", finalI);
                        producerTemplate.sendBody("bean:someDummyCdiAndStatelessBean", finalI);
                        producerTemplate.sendBody("bean:anotherDummyCdiAndStatelessBean", finalI);
                        producerTemplate.sendBody("bean:anotherDummyCdiBean", finalI);
                    } catch (Throwable t){
                        ex.add(t);
                    }

                }
            };
            executor.execute(run);
        }
        executor.shutdown();
        boolean result = executor.awaitTermination(300, TimeUnit.SECONDS);

        if (ex.size()>0){
            for (Throwable t: ex) {
                t.printStackTrace();
            }
            throw ex.get(0);
        }

        if (!result){
            throw new IllegalStateException("Executor terminated");
        }
    }
}
