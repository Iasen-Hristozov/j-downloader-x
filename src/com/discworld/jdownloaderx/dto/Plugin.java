package com.discworld.jdownloaderx.dto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

public abstract class Plugin
{
   private static final String CHARSET_WIN_1251 = "windows-1251";

   private static final String CHARSET_UTF_8 = "UTF-8";

   private static final String CHARSET_CP1251 = "Cp1251";

   protected final static String HTTP = "http://",
            HTTPS = "https://",
            WWW = "www.",
            USR_AGN_MOZZILA = "Mozilla/5.0",
            USR_AGN_ALL = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.88 Safari/537.36 Vivaldi/1.7.735.46",
            // USR_AGN_ALL =
            // "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)"
            // USR_AGN_ALL =
            // "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0"
            REQ_PROP_USER_AGENT = "User-Agent",
            REQ_PROP_ACCEPT_CHARSET = "Accept-Charset";

   protected String DOMAIN = "domain";

   protected IDownloader downloader;

   public Plugin()
   {
      loadSettings();
   }

   public Plugin(IDownloader oDownloader)
   {
      this();
      this.downloader = oDownloader;
   }

   public void setDownloader(IDownloader oDownloader)
   {
      this.downloader = oDownloader;
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
            downloader.onHttpParseDone(doneHttpParse(sResult));

         }
         catch(InterruptedException
               | ExecutionException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   protected class DownloadFile extends SwingWorker<Boolean, Integer>
   {

      private static final int BUFFER_SIZE = 4096;

      String sDownloadFolder, saveFilePath;

      private CFile file = null;

      private ArrayList<SHttpProperty> alHttpProperties = null;

      public DownloadFile(CFile file, String sDownloadFolder)
      {
         this.file = file;
         this.sDownloadFolder = sDownloadFolder;
      }

      public DownloadFile(CFile file,
                          String sDownloadFolder,
                          ArrayList<SHttpProperty> alHttpProperties)
      {
         this(file, sDownloadFolder);
         this.alHttpProperties = alHttpProperties;
      }

      @Override
      protected Boolean doInBackground()
      {
         boolean bResult = true;
         try
         {
            bResult = getResponse();
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
         catch(Exception e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         return bResult;
      }

      private boolean getResponse() throws Exception
      {
         boolean bResult = true;
         
         HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(file.getURL()).openConnection();
         initHttpURLConnection(httpURLConnection);

         try
         {
            int responseCode = httpURLConnection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK)
            {
               String contentType = httpURLConnection.getContentType();
               if(contentType != null
                  && contentType.equalsIgnoreCase("text/html"))
                  throw new Exception("Wrong content type: " + contentType);
               bResult = writeHttpResponseToFile(httpURLConnection);
            }
            else if(responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP)
            {
               String sNewURL = httpURLConnection.getHeaderField("Location");
               file.setURL(sNewURL);
               bResult = getResponse();
            }
            else
            {
               Logger.log("No file to download. Server replied HTTP code: "
                        + responseCode);
               bResult = false;
            }
         }
         catch(Exception e)
         {
            throw e;
         }
         finally
         {
            httpURLConnection.disconnect();
//            bResult = false;
         }
         return bResult;
      }

      private void initHttpURLConnection(HttpURLConnection httpURLConnection)
      {
         httpURLConnection.setRequestProperty(REQ_PROP_USER_AGENT, USR_AGN_ALL);
         httpURLConnection.setRequestProperty(REQ_PROP_ACCEPT_CHARSET,
                                              CHARSET_UTF_8);
         if(alHttpProperties != null && !alHttpProperties.isEmpty())
         {
            for(SHttpProperty oHttpProperty : alHttpProperties)
               httpURLConnection.setRequestProperty(oHttpProperty.name,
                                                    oHttpProperty.value);
         }

      }

      private boolean writeHttpResponseToFile(HttpURLConnection httpURLConnection) throws UnsupportedEncodingException,
                                                                                  IOException,
                                                                                  FileNotFoundException
      {
         boolean bResult = true;

         createDwonloadFolder();
         String fileName = extractFileName(httpURLConnection);
         saveFilePath = sDownloadFolder + File.separator + fileName;
         File file = createSaveFile();

         // opens an output stream to save into file
         FileOutputStream outputStream = new FileOutputStream(file);

         int bytesRead = -1;
         int iTotalBytesRead = 0;
         int progress;
         int contentLength = httpURLConnection.getContentLength();

         publish(iTotalBytesRead);

         byte[] buffer = new byte[BUFFER_SIZE];

         // opens input stream from the HTTP connection
         InputStream inputStream = httpURLConnection.getInputStream();
         while((bytesRead = inputStream.read(buffer)) != -1)
         {
            outputStream.write(buffer, 0, bytesRead);
            iTotalBytesRead += bytesRead;

            if(downloader.isStarted())
            {
               progress = (int) Math.round(((float) iTotalBytesRead / (float) contentLength) * 100f);
               publish(progress);
            }
            else
            {
               bResult = false;
               break;
            }
         }

         outputStream.close();
         inputStream.close();

         if(bResult)
            System.out.println("File " + fileName + " downloaded");
         return bResult;
      }

      private File createSaveFile() throws IOException
      {
         File file = new File(saveFilePath);
         file.getParentFile().mkdirs();
         file.createNewFile();
         return file;
      }

      private void createDwonloadFolder()
      {
         File flDwnFolder = new File(sDownloadFolder);
         if(!flDwnFolder.exists())
            flDwnFolder.mkdir();
      }

      private String extractFileName(HttpURLConnection httpURLConnection) throws UnsupportedEncodingException
      {
         String fileName;
         String disposition = httpURLConnection.getHeaderField("Content-Disposition");
         // String contentType = httpConn.getContentType();
         // System.out.println("Content-Type = " + contentType);
         // System.out.println("Content-Disposition = " + disposition);
         // System.out.println("Content-Length = " + contentLength);
         // System.out.println("fileName = " + fileName);
         if(disposition != null)
         {
            // extracts file name from header field
            fileName = extractFileNameFromDisposition(disposition);
         }
         else
         {
            fileName = extractFileNameFromURL(httpURLConnection);
         }
         return fileName;
      }

      private String extractFileNameFromDisposition(String disposition) throws UnsupportedEncodingException
      {
         String fileName = "";
         int index = disposition.indexOf("filename=");
         if(index > 0)
         {
            fileName = disposition.substring(index + 10,
                                             disposition.length() - 1);
            fileName = new String(fileName.getBytes("ISO8859-1"), CHARSET_UTF_8).replace(":",
                                                                                         "-");
         }
         return fileName;
      }

      private String extractFileNameFromURL(HttpURLConnection httpURLConnection)
      {
         String fileName;
         String URL = httpURLConnection.getURL().toString();
         fileName = URL.substring((URL.lastIndexOf("/") > URL.lastIndexOf("=") ? URL.lastIndexOf("/") : URL.lastIndexOf("=")) + 1,
                                  URL.length());
         return fileName;
      }

      @Override
      protected void process(List<Integer> chunks)
      {
         int progress = chunks.get(0);
         downloader.setFileProgress(file, progress);
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
               downloadFileDone(file, sDownloadFolder, saveFilePath);
            }
            else
            {
               downloader.setFileProgress(file, 0);
               downloader.deleteFileFromQueue(file);
            }
         }
         catch(InterruptedException e)
         {
            Logger.log(e.getStackTrace().toString());
         }
         catch(ExecutionException e)
         {
            Logger.log(e.getStackTrace().toString());
         }
      }
   }

   protected String inBackgroundHttpParse(String sURL) throws Exception
   {
      return getHttpResponse(sURL);
   }

   abstract protected ArrayList<CFile> doneHttpParse(String sResult);

   protected void downloadFileDone(CFile oFile,
                                   String sDownloadFolder,
                                   String saveFilePath)
   {
      downloader.deleteFile(oFile);

      downloader.saveFiles();
   }

   public void parseUrl(String sURL)
   {
      HTTPParser oHttpParser = new HTTPParser(sURL);
      oHttpParser.execute();
      try
      {
         oHttpParser.get();
      }
      catch(InterruptedException
            | ExecutionException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      // new HTTPParser(sURL).execute();
   }

   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      new DownloadFile(oFile, sDownloadFolder).execute();
   }

   public void downloadFile(CFile oFile,
                            String sDownloadFolder,
                            ArrayList<SHttpProperty> alHttpProperties)
   {
      new DownloadFile(oFile, sDownloadFolder, alHttpProperties).execute();
   }

   abstract public ArrayList<String> parseContent(String sContent);

   public String getDomain()
   {
      return DOMAIN;
   }

   abstract protected void loadSettings();

   // abstract public boolean isMine(String sURL);

   protected String getHttpResponse(String sURL) throws Exception
   {
      return getHttpResponse(sURL, null);
   }

   protected String getHttpResponse(String sURL,
                                    ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {
      HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(sURL).openConnection();

      initHttpUrlConnection(httpURLConnection, alHttpProperties);

      int iResponseCode = httpURLConnection.getResponseCode();

      if(iResponseCode == HttpURLConnection.HTTP_OK)
      {
         String sCharset = getHttpResponseCharset(httpURLConnection);
         return getHttpResponse(httpURLConnection, sCharset);
      }
      else if(iResponseCode == HttpURLConnection.HTTP_MOVED_TEMP
              || iResponseCode == HttpURLConnection.HTTP_MOVED_PERM)
      {
         String sNewURL = httpURLConnection.getHeaderField("Location");
         return getHttpResponse(sNewURL, alHttpProperties);
      }
      else
      {
         throw new Exception("HTTP Response code: " + iResponseCode);
      }
   }

   private void initHttpUrlConnection(HttpURLConnection httpURLConnection,
                                      ArrayList<SHttpProperty> alHttpProperties) throws ProtocolException
   {
      httpURLConnection.setRequestMethod("GET");
      createHttpURLConncectionHeader(alHttpProperties, httpURLConnection);
   }

   private void createHttpURLConncectionHeader(ArrayList<SHttpProperty> alHttpProperties,
                                               HttpURLConnection httpURLConnection)
   {
      httpURLConnection.setRequestProperty("User-Agent", USR_AGN_MOZZILA);
      if(alHttpProperties != null && !alHttpProperties.isEmpty())
      {
         for(SHttpProperty oHttpProperty : alHttpProperties)
            httpURLConnection.setRequestProperty(oHttpProperty.name,
                                                 oHttpProperty.value);
      }
   }

   private String getHttpResponse(HttpURLConnection httpURLConnection,
                                  String sCharset) throws UnsupportedEncodingException,
                                                  IOException
   {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),
                                                                               sCharset));

      String inputLine;
      StringBuffer sbResponse = new StringBuffer();

      while((inputLine = bufferedReader.readLine()) != null)
         sbResponse.append(inputLine + "\n");
      bufferedReader.close();

      return sbResponse.toString();
   }

   private String getHttpResponseCharset(HttpURLConnection httpURLConnection)
   {
      final Pattern pattern = Pattern.compile("charset=\"?(\\w+\\-\\d+)\"?");

      List<String> cookies;
      if((cookies = httpURLConnection.getHeaderFields().get("Content-Type")) == null)
         cookies = httpURLConnection.getHeaderFields().get("Content-type");
      Matcher matcher = pattern.matcher(cookies.get(0));
      if(matcher.find())
      {
         if(matcher.group(1).equalsIgnoreCase(CHARSET_WIN_1251))
            return CHARSET_CP1251;
         else
            return CHARSET_UTF_8;
      }
      else
         return CHARSET_CP1251;
   }

   /*
    * TODO Make it abstract
    */
   public ArrayList<CFile> checkContetWithPlugin(String sPath, String sContent)
   {
      return new ArrayList<CFile>();
   }
   
   protected static class Logger
   {
      protected static void log(String text)
      {
         System.out.println(text);
      }
   }
}
