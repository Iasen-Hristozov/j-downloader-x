package com.discworld.jdownloaderx.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import sun.net.www.URLConnection;
//import javax.net.ssl.HttpsURLConnection;
import sun.net.www.http.HttpClient;

public class http_test
{
   final static String UID_NAME = "uid",
                       PASS_NAME = "pass"; 

   static String ckUidValue;

   static String ckPassValue;
   
   
   public static void main(String[] args)
   {
      loginZelka();
      getZelka();

   }
   
   private static void loginZelka()
   {
      String urlParameters  = "username=Rincewind123&password=suleiman";
      byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
      int    postDataLength = postData.length;
      String request        = "http://zelka.org/takelogin.php";
      URL url;
      BufferedReader in;
      String sResponse;
      try
      {
         url = new URL( request );
//         url = new URL(null, request,new sun.net.www.protocol.https.Handler());
         HttpURLConnection conn= (HttpURLConnection) url.openConnection();
//         java.net.URLConnection conn = new URL(request).openConnection();
//         Map<String, List<String>> headers = conn.getHeaderFields(); 
//         List<String> values = headers.get("Set-Cookie");

//         conn.connect();

         //         conn.setRequestMethod( "POST" );
//         conn.setDoOutput( true );
//         conn.setInstanceFollowRedirects( false );
//         conn.setRequestProperty("User-Agent", "Mozilla/5.0");
//         conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
//         conn.setRequestProperty( "charset", "utf-8");
//         conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
//         conn.setUseCaches( false );
         
         conn.setRequestMethod("POST");
//         conn.setRequestProperty("method", "POST");
         conn.setDoOutput(true);
         conn.setUseCaches(false);
         conn.setInstanceFollowRedirects(false);
         conn.setRequestProperty("Host", "zelka.org");
         conn.setRequestProperty("User-Agent", "Mozilla/5.0");
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
////         conn.setRequestProperty("Content-Length", String.valueOf(postData));
         conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
////         conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
         conn.setRequestProperty("Referer", "http://zelka.org");
         conn.setRequestProperty("Connection", "keep-alive");
         conn.setDoInput(true);
//         conn.getOutputStream().write(postData);
      // Send post request

         conn.setRequestProperty("Content-Length", Integer.toString(urlParameters.length()));
         conn.connect();

         DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
         wr.writeBytes(urlParameters);
         wr.flush();
         wr.close();
         
         if(conn.getResponseCode() == 302)
         {
//            Cookie[] cookies = request.getCookies();
            
//            String headerName=null;
//            for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
//               if (headerName.equals("Set-Cookie")) {                  
//               String cookie = conn.getHeaderField(i);
//               System.out.println(cookie);
//               }
//            }

            
            List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
            if (cookies != null) 
            {
               for(String cookie : cookies)
               {
                  cookie = cookie.substring(0, cookie.indexOf(";"));
                  String cookieName = cookie.substring(0, cookie.indexOf("="));
                  String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
                  if(cookieName.equals(UID_NAME))
                     ckUidValue = cookieValue;
                  else if(cookieName.equals(PASS_NAME))
                     ckPassValue = cookieValue;
                     
                  System.out.println(cookie);
               }
//             for (Cookie cookie : cookies) {
//               if (cookie.getName().equals("cookieName")) {
//                 //do something
//                 //value can be retrieved using #cookie.getValue()
//                }
//              }
            }
            

         }
         if(conn.getResponseCode() == 200)
         {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            String inputLine;
            StringBuffer sbResponse = new StringBuffer();

            while((inputLine = in.readLine()) != null)
               sbResponse.append(inputLine + "\n");
            in.close();

            sResponse = sbResponse.toString();
            
            System.out.print(sResponse);            
         }
//         DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
//         wr.write( postData );
//         sResponse = wr.toString();
//         
//         System.out.print(sResponse);
         
      } catch(MalformedURLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch(ProtocolException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   private static void getZelka()
   {
      final String USER_AGENT = "Mozilla/5.0";

      String sResponse = null,
                         sURL = "http://zelka.org/details.php?id=454728";

      URL oURL;

      BufferedReader in;

      HttpURLConnection oHTTPConn;

      try
      {
         oURL = new URL(sURL);
         oHTTPConn = (HttpURLConnection) oURL.openConnection();

         // optional default is GET
         oHTTPConn.setRequestMethod("GET");

         // add reuqest header
         oHTTPConn.setRequestProperty("User-Agent", USER_AGENT);
         
         String myCookies = UID_NAME + "=" + ckUidValue + "; " + PASS_NAME + "=" + ckPassValue;
         oHTTPConn.setRequestProperty("Cookie", myCookies);
         oHTTPConn.connect();
         if(oHTTPConn.getResponseCode() == 200)
         {
            in = new BufferedReader(new InputStreamReader(oHTTPConn.getInputStream(), "UTF-8"));

            String inputLine;
            StringBuffer sbResponse = new StringBuffer();

            while((inputLine = in.readLine()) != null)
               sbResponse.append(inputLine + "\n");
            in.close();

            sResponse = sbResponse.toString();
            
            System.out.print(sResponse);
         }
      } catch(MalformedURLException e)
      {
         e.printStackTrace();
      } catch(ProtocolException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }      
   }

}
