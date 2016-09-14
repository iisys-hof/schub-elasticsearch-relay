package de.hofuniversity.iisys.elasticsearch.relay.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

public class HttpUtil
{
    public static String getText(URL url) throws Exception
    {
        final StringBuffer buffer = new StringBuffer();
        
        HttpURLConnection connection = (HttpURLConnection)
            url.openConnection();
        
        //avoid Resteasy bug
        connection.setRequestProperty("Accept",
            "text/html,application/xhtml+xml,application/xml;"
            + "q=0.9,image/webp,*/*;q=0.8");

        //read response
        readResponse(connection, buffer);
        
        return buffer.toString();
    }
    
    public static String getAuthenticatedText(URL url, String user, String pass)
        throws Exception
    {
        final StringBuffer buffer = new StringBuffer();
        
        String login = user + ":" + pass;
        String encoded = Base64.encodeBase64String(login.getBytes());
        
        HttpURLConnection connection = (HttpURLConnection)
            url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + encoded);
        
        //avoid Resteasy bug
        connection.setRequestProperty("Accept",
            "text/html,application/xhtml+xml,application/xml;"
            + "q=0.9,image/webp,*/*;q=0.8");

        //read response
        readResponse(connection, buffer);
        
        return buffer.toString();
    }
    
    public static String sendJson(URL url, String method, String data)
        throws Exception
    {
        final StringBuffer buffer = new StringBuffer();
        
        HttpURLConnection connection = (HttpURLConnection)
            url.openConnection();

        //send json data
        connection.setRequestMethod(method);
        
        if(data != null)
        {
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length",
                String.valueOf(data.getBytes().length));

            OutputStreamWriter writer = new OutputStreamWriter(
                connection.getOutputStream());
            writer.write(data);
            writer.flush();
            writer.close();
        }

        //read response
        readResponse(connection, buffer);
        
        return buffer.toString();
    }
    
    private static void readResponse(HttpURLConnection connection,
        StringBuffer buffer) throws Exception
    {
        BufferedReader reader = null;
        boolean error = false;
        
        if(connection.getResponseCode() < 400)
        {
            // normal response
            reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream(), "UTF-8"));
        }
        else
        {
            // error response
            reader = new BufferedReader(new InputStreamReader(
                connection.getErrorStream(), "UTF-8"));
            error = true;
        }

        String line = reader.readLine();
        while (line != null)
        {
            buffer.append(line);
            line = reader.readLine();
        }

        reader.close();
        
        if(error)
        {
            throw new Exception("Server error code "
                + connection.getResponseCode()
                + ": " + buffer.toString());
        }
    }
}
