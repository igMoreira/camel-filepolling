package au.com.marlo.tests.group;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by igMoreira on 24/10/17.
 */
public class GroupByField {
    Logger LOG = LoggerFactory.getLogger(GroupByField.class.getSimpleName());


    public List group(List csvContent)
    {
        Map groupedData = new HashMap<String, List<String>>();

        for (Object line : csvContent )
        {
            List<String> row = (List) line;
            String fieldToGroup = row.size() == 2 ? row.get(1) : "";
            List group;
            if ( !groupedData.containsKey(fieldToGroup) )
                groupedData.put(fieldToGroup, new ArrayList<String>());
            group = (List) groupedData.get(fieldToGroup);
            group.add(row);
        }
        return new ArrayList(groupedData.values());
    }

}
