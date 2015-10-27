package com.discworld.jdownloaderx.dto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

public class HTTPParser extends SwingWorker<String, Void>
{
   private String sURL;
   
   protected Vector<CFile> vFilesFnd;
   
   protected FileURLsTableModel oFileURLsTableModel;
   
   protected JTabbedPane tabbedPane;
   
   protected Runnable rnbOnDone;
  
   public HTTPParser(String sURL,
                     FileURLsTableModel oFileURLsTableModel, 
                     Vector<CFile> vFilesFnd,
                     JTabbedPane tabbedPane)
   {
      this.sURL = sURL;
      this.oFileURLsTableModel = oFileURLsTableModel;
      this.vFilesFnd = vFilesFnd;
      this.tabbedPane = tabbedPane;
   }
   
   public HTTPParser(String sURL,
                     Vector<CFile> vFilesFnd,
                     Runnable rnbOnDone)
   {
      this.sURL = sURL;
      this.vFilesFnd = vFilesFnd;
      this.rnbOnDone = rnbOnDone;
   }
   
   @Override
   protected String doInBackground() throws Exception
   {
      final String USER_AGENT = "Mozilla/5.0";

      String sResponse = null;

      URL oURL;

      BufferedReader in;

      HttpURLConnection oHTTPConn;

      try
      {
         // String sURLEncoded = URLEncoder.encode(sURL, "UTF-8");

         oURL = new URL(sURL);
         oHTTPConn = (HttpURLConnection) oURL.openConnection();

         // optional default is GET
         oHTTPConn.setRequestMethod("GET");

         // add reuqest header
         oHTTPConn.setRequestProperty("User-Agent", USER_AGENT);

         if(oHTTPConn.getResponseCode() == 200)
         {
            in = new BufferedReader(new InputStreamReader(
                     oHTTPConn.getInputStream(), "UTF-8"));
            // in = new BufferedReader(new
            // InputStreamReader(oHTTPConn.getInputStream()));

            String inputLine;
            StringBuffer sbResponse = new StringBuffer();

            while((inputLine = in.readLine()) != null)
               sbResponse.append(inputLine + "\n");
            in.close();

            sResponse = sbResponse.toString();
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

      return sResponse;
   }      
   
   
   protected String sFindString(String sSource, Pattern oPattern, String sEnd)
   {
      int iBgn,
          iEnd;
      
      String sResult = null;
      
      Matcher oMatcher = oPattern.matcher(sSource);
      if(oMatcher.find())
      {
         iBgn = oMatcher.end();
         iEnd = sSource.indexOf(sEnd, iBgn);
         sResult = sSource.substring(iBgn, iEnd);
      }
      
      return sResult;
   }

   protected String sFindString(String sSource, String sBegin, String sEnd)
   {
      int iBgn,
          iEnd;
      
      String sResult = null;
      
      if((iBgn = sSource.indexOf(sBegin)) > -1)
      {
         iEnd = sSource.indexOf(sEnd, iBgn);
         sResult = sSource.substring(iBgn + sBegin.length(), iEnd);
      }      
      
      return sResult;
   }
   
   protected String sFindString(String sSource, Pattern oPattern, String sBegin, String sEnd)
   {
      int iBgn,
          iEnd;
      
      String sResult = null;
      
      Matcher oMatcher = oPattern.matcher(sSource);
      if(oMatcher.find())
      {
         iBgn = oMatcher.start() + sBegin.length();
         iEnd = sSource.indexOf(sEnd, iBgn);
         sResult = sSource.substring(iBgn, iEnd);
      }      
      
      return sResult;
   }

   public static void OnDownloadDone()
   {
      
   }
   
}
