package ca.unx.template.Utils;

import ca.unx.template.model.RequestEntity;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Provide http connection utils here.
 */
@Slf4j
public class HttpUtils {
    //HTTP Connections
    private static final int CONNECTION_TIME_OUT = 2000;
    private static final int READ_TIME_OUT = 5000;

    public static String sendHttpRequest(String dst_ip, RequestEntity request) throws Exception {
        final URL url = new URL(dst_ip);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECTION_TIME_OUT);
        connection.setReadTimeout(READ_TIME_OUT);
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
        connection.setRequestProperty("accept","*/*");
        //connection.setRequestProperty("accept","application/json");

        //send http request
        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.write(request.toJSONString().getBytes());
        dataOutputStream.flush();
        dataOutputStream.close();

        if(connection.getResponseCode() == 200){
            log.info("Get 200 response code from server" + dst_ip);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();
            return content.toString();
        }else {
            log.warn("HTTP response failure ip" + dst_ip);
            throw new Exception("HTTP response failure");
        }
    }
}
