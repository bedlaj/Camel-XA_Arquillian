package eu.janbednar;

import eu.janbednar.module.ejbTest.beans.ImplBean;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
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
    ImplBean implBean;

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
       implBean.process(new DefaultExchange(new DefaultCamelContext()));
    }
}
