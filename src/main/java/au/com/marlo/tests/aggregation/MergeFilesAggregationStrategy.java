package au.com.marlo.tests.aggregation;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import java.util.List;

/**
 * Created by igMoreira on 24/10/17.
 */
public class MergeFilesAggregationStrategy implements AggregationStrategy {

    public Exchange aggregate(Exchange oldMsg, Exchange newMsg) {
        if (oldMsg == null)
            return newMsg;

        List mergedFiles = oldMsg.getIn().getBody(List.class);
        List newFile = newMsg.getIn().getBody(List.class);

        mergedFiles.addAll(newFile);

        return oldMsg;
    }
}
