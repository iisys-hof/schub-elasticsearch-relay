package de.hofuniversity.iisys.elasticsearch.relay.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Elasticsearch query, disassembled into path, parameters and body.
 * Also contains a persistent filtering "or array" to attach filters to that
 * is included in the resulting query. this array is not added to the body
 * automatically (not here at least).
 */
public class ESQuery
{
    private String[] fPath;
    
    private Map<String, String> fParams;
    
    private JSONObject fBody;
    
    private JSONArray fAuthFilterOrArr;
    
    private boolean fCancelled = false;
    
    /**
     * Creates an empty query.
     */
    public ESQuery()
    {
        this(null);
    }
    
    /**
     * @param path query path
     */
    public ESQuery(String[] path)
    {
        this(path, (JSONObject)null);
    }
    
    /**
     * @param path query path
     * @param body query body
     */
    public ESQuery(String[] path, JSONObject body)
    {
        this(path, null, body);
    }
    
    /**
     * @param path query path
     * @param params query parameters
     */
    public ESQuery(String[] path, Map<String, String> params)
    {
        this(path, params, null);
    }
    
    /**
     * @param path query path
     * @param params query parameters
     * @param body query body
     */
    public ESQuery(String[] path, Map<String, String> params, JSONObject body)
    {
        fPath = path;
        fParams = params;
        fBody = body;
        
        fAuthFilterOrArr = new JSONArray();
    }
    
    public String[] getQueryPath()
    {
        return fPath;
    }
    
    public void setQueryPath(String[] path)
    {
        fPath = path;
    }
    
    public Map<String, String> getParams()
    {
        return fParams;
    }
    
    public void setParams(Map<String, String> params)
    {
        fParams = params;
    }
    
    public JSONObject getQuery()
    {
        return fBody;
    }
    
    public void setQuery(JSONObject query)
    {
        fBody = query;
    }
    
    public JSONArray getAuthFilterOrArr()
    {
        return fAuthFilterOrArr;
    }
    
    /**
     * @return whether this query has been cancelled internally
     */
    public boolean isCancelled()
    {
        return fCancelled;
    }
    
    /**
     * Cancel this query internally - do not process further and do not send.
     */
    public void cancel()
    {
        fCancelled = true;
    }
    
    /**
     * @return reassembled query URL (without the server)
     */
    public String getQueryUrl()
    {
        StringBuffer urlBuff = new StringBuffer();
        
        // reconstruct request path
        if(fPath != null)
        {
            for(String frag : fPath)
            {
                // skip empty elements
                if(!frag.isEmpty())
                {
                    urlBuff.append(frag);
                    urlBuff.append("/");
                }
            }
        }
        
        // add parameters
        if(fParams != null
            && !fParams.isEmpty())
        {
            //construct URL with all parameters
            Iterator<Entry<String, String>> paramIter =
                fParams.entrySet().iterator();
            Entry<String, String> entry = paramIter.next();
            
            urlBuff.append("?");
            urlBuff.append(entry.getKey());
            urlBuff.append("=");
            urlBuff.append(entry.getValue());
            
            while(paramIter.hasNext())
            {
                entry = paramIter.next();
                urlBuff.append("&");
                urlBuff.append(entry.getKey());
                urlBuff.append("=");
                urlBuff.append(entry.getValue());
            }
        }
        
        return urlBuff.toString();
    }
}
