package de.hofuniversity.iisys.elasticsearch.relay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import de.hofuniversity.iisys.elasticsearch.relay.handler.ESQueryHandler;
import de.hofuniversity.iisys.elasticsearch.relay.model.ESQuery;
import de.hofuniversity.iisys.elasticsearch.relay.util.ESConstants;

/**
 * Elasticsearch Relay main servlet taking in all GET and POST requests and
 * their bodies and returning the response created by the ESQueryHandler.
 */
public class ESRelay extends HttpServlet
{
    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";
    
    private final ESRelayConfig fConfig;

    private final Logger fLogger;
    
    private ESQueryHandler fHandler;
    
    public ESRelay()
    {
        fConfig = new ESRelayConfig();
        
        fLogger = Logger.getLogger(this.getClass().getName());
    }
    
    @Override
    public void init() throws ServletException
    {
        //initialize query handler
        try
        {
            fHandler = new ESQueryHandler(fConfig);
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }
    
    private Map<String, String> getParams(HttpServletRequest request)
    {
        Map<String, String> parameters = new HashMap<String, String>();
        
        String key = null;
        String value = null;
        Enumeration<?> paramEnum = request.getParameterNames();
        while(paramEnum.hasMoreElements())
        {
            key = paramEnum.nextElement().toString();
            value = request.getParameter(key);
            parameters.put(key, value);
        }
        
        return parameters;
    }
    
    private String getBody(HttpServletRequest request)
    {
        StringBuffer buffer = new StringBuffer();
        
        try
        {
            BufferedReader reader = request.getReader();
            
            String line = reader.readLine();
            while(line != null)
            {
                buffer.append(line);
                
                line = reader.readLine();
            }
        }
        catch(Exception e)
        {
            // TODO: ?
            fLogger.log(Level.SEVERE, "failed to read request body", e);
        }
        
        return buffer.toString();
    }
    
    private String[] getFixedPath(HttpServletRequest request)
    {
        // arrange path elements in a predictable manner 
        
        String pathString = request.getPathInfo();
        while(pathString.startsWith("/")
            && pathString.length() > 1)
        {
            pathString = pathString.substring(1);
        }
        String[] path = pathString.split("/");
        
        String[] fixedPath = new String[3];
        
        // detect if there is "_search" in there and move it to index 2
        // TODO: possible different structures?
        
        // indices fragment
        if(path.length > 0
            && !ESConstants.SEARCH_FRAGMENT.equals(path[0]))
        {
            fixedPath[0] = path[0];
        }
        else
        {
            fixedPath[0] = "";
        }
        
        // types fragment
        if(path.length > 1
            && !ESConstants.SEARCH_FRAGMENT.equals(path[1]))
        {
            fixedPath[1] = path[1];
        }
        else
        {
            fixedPath[1] = "";
        }
        
        // search fragment
        fixedPath[2] = ESConstants.SEARCH_FRAGMENT;
        
        return fixedPath;
    }
    
    @Override
    public void doGet(HttpServletRequest request,
        HttpServletResponse response)
            throws ServletException, IOException
    {
        //get authenticated user
        String user = request.getRemoteUser();
        
        // extract and forward request path
        String[] path = getFixedPath(request);
        
        //TODO: properly extract query parameters
        Map<String, String> parameters = getParams(request);
        
        //read request body
        String requestBody = getBody(request);

        PrintWriter out = response.getWriter();
        try
        {
            JSONObject jsonRequest = null;
            if(!requestBody.isEmpty())
            {
                jsonRequest = new JSONObject(requestBody);
            }
            
            ESQuery query = new ESQuery(path, parameters, jsonRequest);
            
            // process request, forward to ES instances
            JSONObject result = fHandler.handleRequest(query, user);
            
            // return result
            response.setContentType(CONTENT_TYPE);
            out.println(result.toString());
            
        }
        catch(Exception e)
        {
            response.setStatus(500);
            response.resetBuffer();
            e.printStackTrace();
            
            JSONObject jsonError = new JSONObject();
            try
            {
                jsonError.put(ESConstants.R_ERROR, e.toString());
                jsonError.put(ESConstants.R_STATUS, 500);
            }
            catch (JSONException e1)
            {
                fLogger.log(Level.SEVERE, "Error during error JSON generation", e1);
            }
            out.println(jsonError);
        }
        
        out.flush();
        out.close();
    }
    
    @Override
    public void doPost(HttpServletRequest request,
        HttpServletResponse response)
            throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    @Override
    public void destroy()
    {
        // destroy query handler and its threads
        fHandler.destroy();
        fHandler = null;
    }
}