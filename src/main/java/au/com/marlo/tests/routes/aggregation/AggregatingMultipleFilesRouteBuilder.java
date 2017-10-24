package au.com.marlo.tests.routes.aggregation;

import au.com.marlo.tests.aggregation.MergeFilesAggregationStrategy;
import au.com.marlo.tests.group.GroupByField;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by igMoreira on 24/10/17.
 */
public class AggregatingMultipleFilesRouteBuilder extends RouteBuilder {

    public void configure() throws Exception {
        from("file:/tmp/camel_test_input?noop=true&maxMessagesPerPoll=10")
                .unmarshal().csv()
                .log("------INPUT------")
                .log("${body}")
                .aggregate(new MergeFilesAggregationStrategy()).constant(true).completionFromBatchConsumer()
                .log("------OUTPUT------")
                .log("${body}");
    }
}
