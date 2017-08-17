package au.com.marlo.tests;

import au.com.marlo.tests.routes.MyTestRouteBuilder;
import au.com.marlo.tests.routes.NewRouteBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.main.Main;

/**
 * Hello world!
 *
 */
public class CamelApp
{
    private Main main = new Main();

    public static void main( String[] args ) throws Exception {
        CamelApp app = new CamelApp();
        app.boot();
    }

    public void boot() throws Exception {
        CamelContext context = new DefaultCamelContext();

        PropertiesComponent pc = new PropertiesComponent("classpath:au.com.marlo.environment.properties");
        context.addComponent("properties", pc);
        context.addRoutes(new NewRouteBuilder());

        main.getCamelContexts().add(context);
        main.run();
    }
}
