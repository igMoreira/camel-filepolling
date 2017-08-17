package au.com.marlo.tests.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by isilva on 16/08/17.
 */
public class NewRouteBuilder extends RouteBuilder {


    String mySharedVar = "";
    Processor p;

    public NewRouteBuilder() {

        p =new Processor() {
            public void process(Exchange exchange) throws Exception {
                log.info("I'm going to sleep....");
                Thread.sleep(10000);
                log.info("Woke up.");
                mySharedVar = exchange.getExchangeId();
            }
        };
    }

    public void configure() throws Exception {
        from("quartz2://myGroup/myTimerName?trigger.repeatInterval=1&trigger.repeatCount=5")
                .routeId("myRoute")
                .setBody(constant("MEU CORPO"))
                .to("log:my.main?showAll=true")
                .multicast().to("seda:goodGuy", "seda:badGuy");

        from("seda:goodGuy?concurrentConsumers=2")
                .log("Good guys says hello, I'm concurrent")
                .to("direct:join");

        from("seda:badGuy?concurrentConsumers=2")
                .log("Bad guys says hello, I'm concurrent")
                .to("direct:join");

        from("direct:join?synchronous=true")
                .log("I'm in a join DIRECT")
                .process(p);
    }
}
