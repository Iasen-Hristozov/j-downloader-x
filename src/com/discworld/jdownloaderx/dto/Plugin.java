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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

public abstract class Plugin
{
   private static final int TYPE_TEXT = 1,
                            TYPE_ZIP = 2;
   
   private static final String CHARSET_WIN_1251 = "windows-1251",
                               CHARSET_UTF_8 = "UTF-8",
                               CHARSET_CP1251 = "Cp1251",
                               CHARSET_ISO8859 = "ISO8859-1";
   
   private static final String CONTENT_TYPE_APP_ZIP = "application/zip",
                               CONTENT_TYPE_APP_RAR = "application/x-rar-compressed",
                               CONTENT_TYPE_APP_DWN = "application/download",
                               CONTENT_TYPE_APP_OCT = "application/octet-stream",
                               CONTENT_TYPE_TXT_HTML = "text/html";
   

   protected final static String HTTP = "http://",
            HTTPS = "https://",
            WWW = "www.",
            USER_AGENT = "Mozilla/5.0",
            USR_AGN_ALL = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.88 Safari/537.36 Vivaldi/1.7.735.46",
            // USR_AGN_ALL =
            // "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)"
            // USR_AGN_ALL =
            // "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0"
            REQ_PROP_USER_AGENT = "User-Agent",
            REQ_PROP_ACCEPT_CHARSET = "Accept-Charset",
            REQ_PROP_REFERER = "Referer",
            HDR_FLD_LOCATION = "Location",
            HDR_FLD_CNT_DSP = "Content-Disposition",
            HDR_FLD_CNT_TYPE = "Content-Type",
            HDR_FLD_CNT_tYPE = "Content-type";


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

   protected class DownloadFileThread extends SwingWorker<Boolean, Integer>
   {


      private static final int BUFFER_SIZE = 4096;

      String sDownloadFolder, saveFilePath;

      private CFile file = null;

      private ArrayList<SHttpProperty> alHttpProperties = null;

      public DownloadFileThread(CFile file, String sDownloadFolder)
      {
         this.file = file;
         this.sDownloadFolder = sDownloadFolder;
      }

      public DownloadFileThread(CFile file,
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
               if(contentType != null && contentType.equalsIgnoreCase(CONTENT_TYPE_TXT_HTML))
                  throw new Exception("Wrong content type: " + contentType);
               bResult = writeHttpResponseToFile(httpURLConnection);
            }
            else if(responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP)
            {
               String sNewURL = httpURLConnection.getHeaderField(HDR_FLD_LOCATION);
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
         httpURLConnection.setRequestProperty(REQ_PROP_ACCEPT_CHARSET, CHARSET_UTF_8);
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
         String disposition = httpURLConnection.getHeaderField(HDR_FLD_CNT_DSP);
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
            fileName = disposition.substring(index + 10, disposition.length() - 1);
            fileName = new String(fileName.getBytes(CHARSET_ISO8859), CHARSET_UTF_8).replace(":", "-");
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

//   abstract protected ArrayList<CFile> doneHttpParse(String sResult);

   protected void downloadFileDone(CFile file,
                                   String sDownloadFolder,
                                   String saveFilePath)
   {
      downloader.deleteFileFromLists(file);

      downloader.saveFilesList();
      
      try
      {
         moveFileToSavePath(file, sDownloadFolder, saveFilePath);
      }
      catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   protected void moveFileToSavePath(CFile file,
                                   String sDownloadFolder,
                                   String saveFilePath) throws IOException 
   {
      File f;
      if(file.getName().endsWith(File.separator))
         f = new File(sDownloadFolder + File.separator + file.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator)+ 1));
      else
         f = new File(sDownloadFolder + File.separator + file.getName());
      f.getParentFile().mkdirs();
      File source = new File(saveFilePath);
      Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
      new DownloadFileThread(oFile, sDownloadFolder).execute();
   }

   public void downloadFile(CFile oFile,
                            String sDownloadFolder,
                            ArrayList<SHttpProperty> alHttpProperties)
   {
      new DownloadFileThread(oFile, sDownloadFolder, alHttpProperties).execute();
   }

   public ArrayList<String> getURLsFromContent(String sContent)
   {
      ArrayList<String> alURLs = new ArrayList<String>();
   
      Matcher m = getUrlPattern().matcher(sContent);
      while(m.find())
      {
         String s = m.group(1);
         alURLs.add(s);
      }
   
      return alURLs;
   }
   
   public String getDomain()
   {
      return "";
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
         String sNewURL = httpURLConnection.getHeaderField(HDR_FLD_LOCATION);
         return getHttpResponse(sNewURL, alHttpProperties);
      }
      else
      {
         throw new Exception("HTTP Response code: " + iResponseCode);
      }
   }

   protected void initHttpUrlConnection(HttpURLConnection httpURLConnection,
                                      ArrayList<SHttpProperty> alHttpProperties) throws ProtocolException
   {
      httpURLConnection.setRequestMethod("GET");
      createHttpURLConnectionHeader(alHttpProperties, httpURLConnection);
   }
   
   protected void createHttpURLConnectionHeader(ArrayList<SHttpProperty> alHttpProperties,
                                               HttpURLConnection httpURLConnection)
   {
      httpURLConnection.setRequestProperty(REQ_PROP_USER_AGENT, USER_AGENT);
      if(alHttpProperties != null && !alHttpProperties.isEmpty())
      {
         for(SHttpProperty oHttpProperty : alHttpProperties)
            httpURLConnection.setRequestProperty(oHttpProperty.name, oHttpProperty.value);
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
      if((cookies = httpURLConnection.getHeaderFields().get(HDR_FLD_CNT_TYPE)) == null)
         cookies = httpURLConnection.getHeaderFields().get(HDR_FLD_CNT_tYPE);
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

   public ArrayList<CFile> checkContetWithPlugin(String sPath, String sContent)
   {
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();

      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      prepareHttpRequestHeader(alHttpProperties);
      
      Matcher matcher = getUrlPattern().matcher(sContent);
      while(matcher.find())
      {
         String sURL = matcher.group(1).replaceAll("&amp;", "&");
         try
         {
            alFilesFound.addAll(getFilesFromUrl(sPath, alHttpProperties, sURL));
         }
         catch(Exception e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      
      return alFilesFound;
   }

   protected void checkAddHttpProperty(ArrayList<SHttpProperty> alHttpProperties, SHttpProperty newHttpProperty)
   {
      boolean isHttpPropertyFound = false;
      for(SHttpProperty httpProperty: alHttpProperties)
      {
         if(httpProperty.name.equalsIgnoreCase(newHttpProperty.name))
         {
            httpProperty.value = newHttpProperty.value;
            isHttpPropertyFound = true;
            break;
         }
      }
      if(!isHttpPropertyFound)
         alHttpProperties.add(newHttpProperty);
   }
   
   protected ArrayList<CFile> getFilesFromUrl(String sPath,
                                            ArrayList<SHttpProperty> alHttpProperties,
                                            String sURL) throws Exception
   {
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      byte type = getUrlContentType(sURL, alHttpProperties);
      switch(type)
      {
         case TYPE_TEXT:
            alFilesFound.addAll(getFilesUrlsFormUrl(sPath, sURL, alHttpProperties));
         break;
            
         case TYPE_ZIP:
            CFile file = createFile(sPath, sURL);
            alFilesFound.add(file);
         break;
      }
      return alFilesFound;
   }
   
   private ArrayList<CFile> getFilesUrlsFormUrl(String sPath, 
                                                String sURL, 
                                                ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {            
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      
      String sHttpResponse = getHttpResponse(sURL, alHttpProperties);
      
      Matcher matcher = getFileUrlPattern().matcher(sHttpResponse);
      while(matcher.find())
      {
         String sNewURL = matcher.group(1).replaceAll("&amp;", "&");
         if(!sNewURL.contains(getDomain()))
            sNewURL = HTTPS + getDomain() + sNewURL;
         alFilesFound.addAll(getFilesFromUrl(sPath, alHttpProperties, sNewURL));
      }
      return alFilesFound;
   }
   
   private byte getUrlContentType(String sURL, ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {
      HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(sURL).openConnection();

//      createRequestHeader(alHttpProperties, httpURLConnection);
      initHttpUrlConnection(httpURLConnection, alHttpProperties);
    
      int responseCode = httpURLConnection.getResponseCode();
    
      if(responseCode == HttpURLConnection.HTTP_OK)
      {
         String contentType = httpURLConnection.getContentType();
         if(contentType == null)
            throw new Exception("Null content type");

         if(contentType.contains(CONTENT_TYPE_TXT_HTML))
            return TYPE_TEXT;
         else if(contentType.contains(CONTENT_TYPE_APP_ZIP)
                  || contentType.contains(CONTENT_TYPE_APP_RAR)
                  || contentType.contains(CONTENT_TYPE_APP_DWN)
                  || contentType.contains(CONTENT_TYPE_APP_OCT))
         {
            return TYPE_ZIP;
         }
         else
            throw new Exception("Unknown content type: " + contentType);
      }
      else if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP
               || responseCode == HttpURLConnection.HTTP_MOVED_PERM)
         {
            sURL = httpURLConnection.getHeaderField(HDR_FLD_LOCATION);
            return getUrlContentType(sURL, alHttpProperties);
         }
      else
         throw new Exception("Http request response code: " + responseCode);
   }
   
   private CFile createFile(String sPath, String sURL)
   {
      return new CFile(sPath + File.separator, sURL);
   }

//   private void createRequestHeader(ArrayList<SHttpProperty> alHttpProperties,
//                                    HttpURLConnection httpURLConnection) throws ProtocolException
//   {
//      httpURLConnection.setRequestMethod("GET");
//
//      //header
//      httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
//      if(alHttpProperties != null && !alHttpProperties.isEmpty())
//      {
//         for(SHttpProperty httpProperty : alHttpProperties)
//            httpURLConnection.setRequestProperty(httpProperty.name, httpProperty.value);
//      }
//   }
   
   protected void prepareHttpRequestHeader(ArrayList<SHttpProperty> alHttpProperties)
   {}
   
//   public ArrayList<CFile> checkContetWithPlugin(String sPath, String sContent)
//   {
//      return new ArrayList<CFile>();
//   }
   
   
   abstract protected Pattern getUrlPattern();

   abstract protected Pattern getFileUrlPattern();

   /*
    * TODO Make them abstract
    */

   
   abstract protected Pattern getTitlePattern();
   
//   protected int getUrlPatternGroupId()
//   {
//      return 0;
//   }
//      
//   protected int getFileUrlPatternGroupId()
//   {
//      return 0;
//   }
   
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      sResult = sResult.replace("\n", "");
      String sUrl = getFileUrl(sResult);
      String sTitle = getTitle(sResult).trim();

      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      alFilesFound.add(new CFile(sTitle + File.separator, sUrl));
   
      return alFilesFound;
   }

protected String getTitle(String sResult)
{
   String sTitle = "";
   Matcher matcher = getTitlePattern().matcher(sResult);
   if(matcher.find())
      sTitle = matcher.group(1).replace("/", "-").trim();
   return sTitle;
}

   protected String getFileUrl(String sResult)
   {
      String sURL = "";
      Matcher matcher = getFileUrlPattern().matcher(sResult);
      if(matcher.find())
         sURL = matcher.group();
      return sURL;
   }

   protected static class Logger
   {
      protected static void log(String text)
      {
         System.out.println(text);
      }
   }
}
