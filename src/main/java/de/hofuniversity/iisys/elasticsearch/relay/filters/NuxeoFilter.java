package de.hofuniversity.iisys.elasticsearch.relay.filters;

import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import de.hofuniversity.iisys.elasticsearch.relay.model.ESQuery;
import de.hofuniversity.iisys.elasticsearch.relay.permissions.UserPermSet;
import de.hofuniversity.iisys.elasticsearch.relay.util.ESConstants;

/**
 * Nuxeo filter allowing objects' creators and people and groups who were
 * given permission to find entries.
 */
public class NuxeoFilter implements IFilter
{
    private static final String NX_CREATOR = "dc:creator";
    private static final String NX_ACL = "ecm:acl";
    
    private final Set<String> fTypes;
    
    /**
     * @param types Nuxeo result types
     */
    public NuxeoFilter(Set<String> types)
    {
        fTypes = types;
    }

    @Override
    public ESQuery addFilter(UserPermSet perms, ESQuery query,
        List<String> indices, List<String> types)
    {
        String user = perms.getUserName();
        
        try
        {
            JSONArray filters = query.getAuthFilterOrArr();
            
            // enable creator to find documents
            JSONObject creatorFilter = new JSONObject();
            
            JSONObject termObj = new JSONObject();
            termObj.put(NX_CREATOR, user);
            
            // TODO: what if permissions are taken away from a user
            
            creatorFilter.put(ESConstants.Q_TERM, termObj);
            
            filters.put(creatorFilter);
            
            // filter by additional ACL rules
            JSONObject aclFilter = new JSONObject();
            
            termObj = new JSONObject();
            termObj.put(NX_ACL, user);
            
            aclFilter.put(ESConstants.Q_TERM, termObj);
            
            filters.put(aclFilter);

            // TODO: evaluate roles via ecm:acl
            for(String group : perms.getNuxeoGroups())
            {
                aclFilter = new JSONObject();
                
                termObj = new JSONObject();
                termObj.put(NX_ACL, group);
                
                aclFilter.put(ESConstants.Q_TERM, termObj);
                
                filters.put(aclFilter);
            }
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return query;
    }
    
}
