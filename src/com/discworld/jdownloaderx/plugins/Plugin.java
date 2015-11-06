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

public abstract class Plugin
{
//   protected String DOMAIN = "domain";
   public static final String DOMAIN = "domain";
   
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

   private class DownloadFile extends SwingWorker<Boolean, Integer> 
   {
      private static final int BUFFER_SIZE = 4096;
      
      String sDownloadFolder,
             saveFilePath;
      
      private CFile oFile = null;

      public DownloadFile(CFile aFile, String sDownloadFolder)
      {
         this.oFile = aFile; 
         this.sDownloadFolder = sDownloadFolder;
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
            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) 
            {
               String fileName = "";
               String disposition = httpConn.getHeaderField("Content-Disposition");
               String contentType = httpConn.getContentType();
               int contentLength = httpConn.getContentLength();

               if (disposition != null) 
               {
                  // extracts file name from header field
                  int index = disposition.indexOf("filename=");
                  if (index > 0) 
                  {
                     fileName = disposition.substring(index + 10, disposition.length() - 1);
                  }
               } 
               else 
               {
                  // extracts file name from URL
                  fileName = sURL.substring(sURL.lastIndexOf("/") + 1, sURL.length());
               }

               System.out.println("Content-Type = " + contentType);
               System.out.println("Content-Disposition = " + disposition);
               System.out.println("Content-Length = " + contentLength);
               System.out.println("fileName = " + fileName);

               // opens input stream from the HTTP connection
               InputStream inputStream = httpConn.getInputStream();
               File flDwnFolder = new File(sDownloadFolder);
               if(!flDwnFolder.exists())
                  flDwnFolder.mkdir();
//               String saveFilePath = DOWNLOAD_FLD + fileName;
               saveFilePath = sDownloadFolder + File.separator + fileName;
               
               // opens an output stream to save into file
               FileOutputStream outputStream = new FileOutputStream(saveFilePath);
   
               int bytesRead = -1;
               int iTotalBytesRead = 0;
               int progress;
               publish(iTotalBytesRead);
               byte[] buffer = new byte[BUFFER_SIZE];
               while ((bytesRead = inputStream.read(buffer)) != -1) 
               {
//                  System.out.println("bIsStarted = " + String.valueOf(bIsStarted));
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
//                        System.out.println(fileName +": " + 0);
                     
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
         } 
         catch(IOException e)
         {
            e.printStackTrace();
            bResult = false;
         } 
         
         return bResult;
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
            }
         } 
         catch(InterruptedException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch(ExecutionException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } 
      }
   }      
   
   protected String inBackgroundHttpParse(String sURL)
   {
      final String USER_AGENT = "Mozilla/5.0";

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

         if(oHTTPConn.getResponseCode() == 200)
         {
            in = new BufferedReader(new InputStreamReader(oHTTPConn.getInputStream(), "UTF-8"));

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
   
   abstract protected ArrayList<CFile> doneHttpParse(String sResult);

   protected void doneDownloadFile(CFile oFile, String sDownloadFolder, String saveFilePath)
   {
      oDownloader.deleteFile(oFile);
      
      oDownloader.saveFiles();
   }
   
   public void vParseUrl(String sURL)
   {
//      oHttpParser = new HTTPParser(sURL);
//      oHttpParser.execute();
      new HTTPParser(sURL).execute();
   }
   
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
//      oDownloadFile = new DownloadFile(oFile, sDownloadFolder);
//      oDownloadFile.execute();
      new DownloadFile(oFile, sDownloadFolder).execute();
   }

   protected static String sFindString(String sSource, Pattern oPattern, String sEnd)
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
   
   protected static ArrayList<String> sFindStrings(String sSource, Pattern oPattern, String sEnd)
   {
      int iBgn = 0,
          iEnd = 0;
      
      ArrayList<String> alResult = new ArrayList<String>();
      
      String sResult = null,
             sSubSource = sSource;
      
      while(true)
      {
         sSubSource = sSubSource.substring(iEnd);
         Matcher oMatcher = oPattern.matcher(sSubSource);
         if(oMatcher.find())
         {
            iBgn = oMatcher.end();
            iEnd = sSubSource.indexOf(sEnd, iBgn);
            sResult = sSubSource.substring(iBgn, iEnd);
            alResult.add(sResult);
         }
         else 
            break;
      }
      
      
      return alResult;
   }

   protected static String sFindString(String sSource, String sBegin, String sEnd)
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
   
   protected static String sFindString(String sSource, Pattern oPattern, String sBegin, String sEnd)
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
   
   abstract public ArrayList<String> parseClipboard(String sContent);
   
   abstract public String getDomain();
   
   abstract protected void loadSettings();
   

}
