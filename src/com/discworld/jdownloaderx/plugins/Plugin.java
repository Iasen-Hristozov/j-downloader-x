package com.discworld.jdownloaderx.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public abstract class Plugin
{
   protected final static String HTTP = "http://",
                                 HTTPS = "https://",
                                 WWW = "www.";   
//   protected String DOMAIN = "domain";
   protected String DOMAIN = "domain";
   
   protected IDownloader oDownloader;
   
   
   public Plugin()
   {
      loadSettings();
   }
   
   public Plugin(IDownloader oDownloader)
   {
      this();
      this.oDownloader = oDownloader;
   }
   
   public void setDownloader(IDownloader oDownloader)
   {
      this.oDownloader = oDownloader;
   }
   
   class HTTPParser extends SwingWorker<String, Void>
   {
      private String sURL;
      
      public HTTPParser(String sURL)
      {
         super();
         this.sURL = sURL;
      }

      @Override
      protected String doInBackground() throws Exception
      {
         return inBackgroundHttpParse(sURL);
      }

      @Override
      protected void done()
      {
         try
         {
            String sResult = get();
            oDownloader.onHttpParseDone(doneHttpParse(sResult));

         } catch(InterruptedException | ExecutionException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   protected class DownloadFile extends SwingWorker<Boolean, Integer> 
   {
      private static final int BUFFER_SIZE = 4096;
      
      String sDownloadFolder,
             saveFilePath;
      
      private CFile oFile = null;
      
      private ArrayList<SHttpProperty> alHttpProperties = null;

      public DownloadFile(CFile aFile, String sDownloadFolder)
      {
         this.oFile = aFile; 
         this.sDownloadFolder = sDownloadFolder;
      }
      
      public DownloadFile(CFile aFile, String sDownloadFolder, ArrayList<SHttpProperty> alHttpProperties)
      {
         this(aFile, sDownloadFolder);
         this.alHttpProperties = alHttpProperties; 
      }

      @Override
      protected Boolean doInBackground()
      {
         boolean bResult = true;
         String sURL = oFile.getURL();
         URL url;
         try
         {
            url = new URL(sURL);

            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
//            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
//            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");
            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.88 Safari/537.36 Vivaldi/1.7.735.46");
            if(alHttpProperties != null && !alHttpProperties.isEmpty())
            {
               for(SHttpProperty oHttpProperty : alHttpProperties)
                  httpConn.setRequestProperty(oHttpProperty.name, oHttpProperty.value);
            }
            httpConn.setRequestProperty("Accept-Charset", "UTF-8");
            int responseCode = httpConn.getResponseCode();
            

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) 
            {
               String fileName = "";
               String disposition = httpConn.getHeaderField("Content-Disposition");
//               String contentType = httpConn.getContentType();
//             System.out.println("Content-Type = " + contentType);
//             System.out.println("Content-Disposition = " + disposition);
//             System.out.println("Content-Length = " + contentLength);
//             System.out.println("fileName = " + fileName);
               int contentLength = httpConn.getContentLength();
               String contentType = httpConn.getContentType();
               if(contentType != null && contentType.equalsIgnoreCase("text/html"))
                  throw new IOException();
               if (disposition != null) 
               {
                  // extracts file name from header field
                  int index = disposition.indexOf("filename=");
                  if (index > 0) 
                  {
                     fileName = disposition.substring(index + 10, disposition.length() - 1);
                     fileName = new String(fileName.getBytes("ISO8859-1"),"UTF-8").replace(":", "-");
                  }
               } 
               else 
               {
                  // extracts file name from URL
//                  fileName = sURL.substring(sURL.lastIndexOf("/") + 1, sURL.length());
                  String URL = httpConn.getURL().toString();
                  fileName = URL.substring(URL.lastIndexOf("/") + 1, URL.length());
               }
               // opens input stream from the HTTP connection
               InputStream inputStream = httpConn.getInputStream();
               File flDwnFolder = new File(sDownloadFolder);
               if(!flDwnFolder.exists())
                  flDwnFolder.mkdir();
//               String saveFilePath = DOWNLOAD_FLD + fileName;
               saveFilePath = sDownloadFolder + File.separator + fileName;
               
               File f = new File(saveFilePath);

               f.getParentFile().mkdirs();
               f.createNewFile();
               
               // opens an output stream to save into file
//               FileOutputStream outputStream = new FileOutputStream(saveFilePath);
               FileOutputStream outputStream = new FileOutputStream(f);
   
               int bytesRead = -1;
               int iTotalBytesRead = 0;
               int progress;
               publish(iTotalBytesRead);
               byte[] buffer = new byte[BUFFER_SIZE];
               while ((bytesRead = inputStream.read(buffer)) != -1) 
               {
                  outputStream.write(buffer, 0, bytesRead);
                  iTotalBytesRead += bytesRead;

                  if(oDownloader.isStarted())
                  {
                     progress = (int) Math.round(((float)iTotalBytesRead / (float)contentLength) * 100f);
                     publish(progress);
                  }
                  else
                  {
//                     publish(0);
                     
                     bResult = false;
                     break;
                     
//                     throw new CException();
                  }
                  
//                  setProgress(progress);
               }
   
               outputStream.close();
               inputStream.close();
   
               if(bResult)
                  System.out.println("File " + fileName + " downloaded");
            }
            else if(responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP)
            {
               String Location = httpConn.getHeaderField("Location");
               System.out.println(Location);
               bResult = false;
            }
            else 
            {
               System.out.println("No file to download. Server replied HTTP code: " + responseCode);
               bResult = false;
            }
            
            httpConn.disconnect();
         } 
         catch(MalformedURLException e)
         {
            e.printStackTrace();
            bResult = false;
            return false;
         } 
         catch(IOException e)
         {
            System.out.println("File: " + saveFilePath);
            e.printStackTrace();
            bResult = false;
            return false;
         }
//         finally
//         {
            return bResult;   
//         }
      }
      
      @Override
      protected void process(List<Integer> chunks)
      {
         int progress = chunks.get(0);
         oDownloader.setFileProgress(oFile, progress);
         
//         if(progress == 0)
//            System.out.println("Progress: " + progress);
      }      

      @Override
      protected void done()
      {
         super.done();
         
         try
         {
            boolean status = get();
            
            if(status)
            {
               doneDownloadFile(oFile, sDownloadFolder, saveFilePath);
            }
            else
            {
//               iDwns = MAX_DWN;
               oDownloader.setFileProgress(oFile, 0);
               oDownloader.deleteFileFromQueue(oFile);
            }
         } 
         catch(InterruptedException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } 
         catch(ExecutionException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } 
      }
   }      
   
   protected String inBackgroundHttpParse(String sURL)
   {
      return getHttpResponse(sURL);
   }
   
   abstract protected ArrayList<CFile> doneHttpParse(String sResult);

   protected void doneDownloadFile(CFile oFile, String sDownloadFolder, String saveFilePath)
   {
      oDownloader.deleteFile(oFile);
      
      oDownloader.saveFiles();
   }
   
   public void vParseUrl(String sURL)
   {
      HTTPParser oHttpParser = new HTTPParser(sURL);
      oHttpParser.execute();
      try
      {
         oHttpParser.get();
      } catch(InterruptedException | ExecutionException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
//      new HTTPParser(sURL).execute();
   }
   
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      new DownloadFile(oFile, sDownloadFolder).execute();
   }
   
   public void downloadFile(CFile oFile, String sDownloadFolder, ArrayList<SHttpProperty> alHttpProperties)
   {
      new DownloadFile(oFile, sDownloadFolder, alHttpProperties).execute();
   }

   abstract public ArrayList<String> parseContent(String sContent);
   
   public String getDomain()
   {
      return DOMAIN;
   }
   
   abstract protected void loadSettings();
   
   abstract public boolean isMine(String sURL);
   
   protected String getHttpResponse(String sURL)
   {
      return getHttpResponse(sURL, null);
   }
   
   protected String getHttpResponse(String sURL, ArrayList<SHttpProperty> alHttpProperties)
   {
      final String USER_AGENT = "Mozilla/5.0";
      
//      final Pattern ptnCharset = Pattern.compile("charset=\"?(.+?)\"?");
      final Pattern ptnCharset = Pattern.compile("charset=\"?(\\w+\\-\\d+)\"?");
      
      String sResponse = null;

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
         if(alHttpProperties != null && !alHttpProperties.isEmpty())
         {
            for(SHttpProperty oHttpProperty : alHttpProperties)
               oHTTPConn.setRequestProperty(oHttpProperty.name, oHttpProperty.value);
         }
         
         int rc = oHTTPConn.getResponseCode();
         
         if(rc == HttpURLConnection.HTTP_OK)
         {
            List<String> cookies = oHTTPConn.getHeaderFields().get("Content-Type");
            if(cookies == null)
               cookies = oHTTPConn.getHeaderFields().get("Content-type");
            Matcher oMatcher =ptnCharset.matcher(cookies.get(0));
            String sCharset = "Cp1251";
            if(oMatcher.find())
            {
               sCharset = oMatcher.group(1); 
               if(sCharset.equalsIgnoreCase("windows-1251"))
                  sCharset = "Cp1251";
               else
                  sCharset = "UTF-8";
                  
            }
            
            in = new BufferedReader(new InputStreamReader(oHTTPConn.getInputStream(), sCharset));

            String inputLine;
            StringBuffer sbResponse = new StringBuffer();

            while((inputLine = in.readLine()) != null)
               sbResponse.append(inputLine + "\n");
            in.close();

            sResponse = sbResponse.toString();
         }
         else if(rc == HttpURLConnection.HTTP_MOVED_TEMP )
         {
            String Location = oHTTPConn.getHeaderField("Location");
            System.out.println(Location);
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
}
