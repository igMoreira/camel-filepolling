package au.com.marlo.tests.routes.splitting;

import au.com.marlo.tests.group.GroupByField;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by igMoreira on 24/10/17.
 */
public class SplittingByFieldRouteBuilder extends RouteBuilder {

    public void configure() throws Exception {
        from("file:/tmp/camel_test_input?noop=true")
                .unmarshal().csv()
                .bean(GroupByField.class)
                .split(body())
                    .log("${body}")
                .end();
    }
}
