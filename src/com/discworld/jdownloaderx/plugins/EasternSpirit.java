package com.discworld.jdownloaderx.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class EasternSpirit extends Plugin
{
   private static final String CONTENT_TYPE_APP_ZIP = "application/zip";
   private static final String CONTENT_TYPE_APP_RAR = "application/x-rar-compressed";
   private static final String CONTENT_TYPE_TXT_HTML = "text/html";

   private final static String DOMAIN = "www.easternspirit.org",
                               SETTINGS_FILE = "easternspirit.xml",
                               COOKIE_UID = "uid",
                               COOKIE_PASS = "pass", 
                               COOKIE_SESSION_FRONT = "ips4_IPSSessionFront",
                               USER_AGENT = "Mozilla/5.0";
   
   private final static Pattern ptnTitle = Pattern.compile("<h3 class=\"title\">(.+?)\\["),
//                                ptnURL = Pattern.compile("<a href=\\'(request\\.php\\?\\d+)\\'> <img src=\\'e107_images\\/generic\\/lite\\/download.png\\' alt=\\'\\' style=\\'border:0\\' \\/>");
                                  ptnURL = Pattern.compile("http:\\/\\/www\\.easternspirit\\.org\\/forum\\/index\\.php\\?\\/files\\/file\\/[\\d\\w\\-]+\\/&amp\\;do=download&amp\\;csrfKey=[\\d\\w]+"),
                                  ptnEasternSpiritUrl = Pattern.compile("http:\\/\\/www\\.easternspirit\\.org\\/forum\\/index\\.php\\?\\/files\\/file\\/[\\d\\w\\-]+\\/"),
                                  ptnEasternSpiritFileUrl = Pattern.compile("<a href='(http:\\/\\/www\\.easternspirit\\.org\\/forum\\/index\\.php\\?\\/files\\/file\\/[\\d\\w\\-]+\\/&amp;do=download&amp;(r=[\\d]+&amp;)?(confirm=1&amp;)?(t=1&amp;)?csrfKey=[\\w\\d]+)' class='ipsButton");

   private static final int TYPE_TEXT = 1,
                            TYPE_ZIP = 2;
   
   
   private String              sTitle,
                               sUrl;
   
   private EasternSpiritSettings easternSpiritSettings;
   
   static
   {
      PluginFactory.getInstance().registerPlugin(DOMAIN, new EasternSpirit(DownloaderPassClass.getDownloader()));
   }
   
   public EasternSpirit()
   {
      super();
   }
   
   public EasternSpirit(IDownloader downloader)
   {
      super(downloader);
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }

   @Override
   public ArrayList<String> parseContent(String sContent)
   {
      ArrayList<String> alUrlMovies = new ArrayList<String>();
   
      Matcher m = ptnURL.matcher(sContent);
      while(m.find())
      {
         String s = m.group(0);
         s = "http://" + DOMAIN + s;
         alUrlMovies.add(s);
      }
   
      return alUrlMovies;
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      sResult = sResult.replace("\n", "");
   
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
      Matcher oMatcher = ptnURL.matcher(sResult);
      if(oMatcher.find())
      {
         sUrl = oMatcher.group(0);
         sUrl = "http://" + DOMAIN + sUrl;
      }      
      
      oMatcher = ptnTitle.matcher(sResult);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group(1);
         sTitle = sTitle.trim();
      }      
   
      vFilesFnd.add(new CFile(sTitle, sUrl));
   
      return vFilesFnd;
   }

   @Override
   public void downloadFile(CFile file, String sDownloadFolder)
   {
//      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
//      alHttpProperties.add(new SHttpProperty("Referer", oFile.getURL()));
//      
//      new DownloadFile(oFile, sDownloadFolder, alHttpProperties).execute();
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      createCookiesCollection(alHttpProperties);
      
//      alHttpProperties.add(new SHttpProperty("Referer", oFile.getURL()));
      
      new DownloadFile(file, sDownloadFolder, alHttpProperties).execute();
//      
//      
//      new DownloadFile(oFile, sDownloadFolder).execute();
   }

   @Override
   protected void downloadFileDone(CFile file, String sDownloadFolder, String saveFilePath)
   {
      super.downloadFileDone(file, sDownloadFolder, saveFilePath);
      try
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
      catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   @Override
   protected void loadSettings()
   {
      try
      {
         JAXBContext jaxbContext = JAXBContext.newInstance(EasternSpiritSettings.class);
         
         File file = new File(SETTINGS_FILE);
         if(file.exists())
         {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            easternSpiritSettings = (EasternSpiritSettings)jaxbUnmarshaller.unmarshal(file);
         }
         else
         {
            easternSpiritSettings = new EasternSpiritSettings();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(easternSpiritSettings, file);
         }
      } 
      catch(JAXBException e1)
      {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }               
   }
   
   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "", propOrder = {"sCookieUID","sCookiePass", "sCookieSessionFront"})
   @XmlRootElement(name = "settings")
   static private class EasternSpiritSettings
   {
      @XmlElement(name = "cookie_uid", required = true)
      public String sCookieUID;
      @XmlElement(name = "cookie_pass", required = true)
      public String sCookiePass;
      @XmlElement(name = "cookie_session_front", required = true)
      public String sCookieSessionFront;
   }
   
//   @Override
   public ArrayList<CFile> _checkContetWithPlugin(String sPath, String sContent)
   {
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      
      Matcher matcher = ptnEasternSpiritUrl.matcher(sContent);
      if(matcher.find())
      {
         String sEasternSpiritURL = matcher.group();
         try
         {
            ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
            createCookiesCollection(alHttpProperties);
            String sEasternSpiritResponse = getHttpResponse(sEasternSpiritURL, alHttpProperties);
            if(sEasternSpiritResponse != null)
            {
               matcher = ptnEasternSpiritFileUrl.matcher(sEasternSpiritResponse);
               while(matcher.find())
               {
                  CFile flEasternSpirit = createFile(sPath,
                                                     matcher.group(1).replaceAll("&amp;", "&"),
                                                     alHttpProperties);
                  alFilesFound.add(flEasternSpirit);               
               }
            }

         }
         catch(Exception e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         
      }
      
      return alFilesFound;
   }
   
   @Override
   public ArrayList<CFile> checkContetWithPlugin(String sPath, String sContent)
   {
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();

      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      createCookiesCollection(alHttpProperties);
      
      Matcher matcher = ptnEasternSpiritUrl.matcher(sContent);
      if(matcher.find())
      {
         String sEasternSpiritURL = matcher.group();
         try
         {
            alFilesFound.addAll(_getUrls(sPath, sEasternSpiritURL, alHttpProperties));
         }
         catch(Exception e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         
      }
      
      return alFilesFound;
   }
   
   
   private ArrayList<CFile> _getUrls(String sPath, String sUrl, ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {            
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      
      String sEasternSpiritResponse = getHttpResponse(sUrl, alHttpProperties);
      
      Matcher matcher = ptnEasternSpiritFileUrl.matcher(sEasternSpiritResponse);
      while(matcher.find())
      {
         String sNewUrl = matcher.group(1).replaceAll("&amp;", "&");
         byte type = _checkUrl(sPath, sNewUrl, alHttpProperties);
         switch(type)
         {
            case TYPE_TEXT:
               alFilesFound.addAll(_getUrls(sPath, sNewUrl, alHttpProperties));
            break;
               
            case TYPE_ZIP:
               CFile flEasternSpirit = _createFile(sPath, sNewUrl);
               alFilesFound.add(flEasternSpirit);
            break;
         }
      }
      return alFilesFound;
   }
   
   private byte _checkUrl(String sPath, String sUrl, ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {
      HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(sUrl).openConnection();

      createRequestHeader(alHttpProperties, httpURLConnection);
    
      int responseCode = httpURLConnection.getResponseCode();
    
      if(responseCode == HttpURLConnection.HTTP_OK)
      {
         String contentType = httpURLConnection.getContentType();
         if(contentType == null)
            throw new Exception("Null content type");

         if(contentType.contains(CONTENT_TYPE_TXT_HTML))
            return TYPE_TEXT;
         else if(contentType.contains(CONTENT_TYPE_APP_ZIP)
                  || contentType.contains(CONTENT_TYPE_APP_RAR))
         {
            return TYPE_ZIP;
         }
         else
            throw new Exception("Unknown content type: " + contentType);
      }
      else if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP
               || responseCode == HttpURLConnection.HTTP_MOVED_PERM)
         {
            sUrl = httpURLConnection.getHeaderField("Location");
            return _checkUrl(sPath, sUrl, alHttpProperties);
         }
      else
         throw new Exception("Http request response code: " + responseCode);
   }
   
   private CFile _createFile(String sPath, String sUrl)
   {
      return new CFile(sPath + File.separator, sUrl);
   }
   
   private int checkUrl(String sURL, ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {
      HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(sURL).openConnection();

      createRequestHeader(alHttpProperties, httpURLConnection);
    
      int responseCode = httpURLConnection.getResponseCode();
    
      if(responseCode == HttpURLConnection.HTTP_OK)
      {
         String contentType = httpURLConnection.getContentType();
         if(contentType == null)
            throw new Exception("Null content type");

         if(contentType.equalsIgnoreCase(CONTENT_TYPE_TXT_HTML))
            return TYPE_TEXT;
         else if(contentType.equalsIgnoreCase(CONTENT_TYPE_APP_ZIP) 
                  || contentType.contains(CONTENT_TYPE_APP_RAR))
         {
            return TYPE_ZIP;
         }
         else
            throw new Exception("Unknown content type: " + contentType);
      }
      else if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP
            || responseCode == HttpURLConnection.HTTP_MOVED_PERM)
      {
         String sNewURL = httpURLConnection.getHeaderField("Location");
         return checkUrl(sNewURL, alHttpProperties);
      }
      else
         throw new Exception("Http request response code: " + responseCode);
   }
   
   private String getFileUrl(String sURL, ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {
      int urlType = checkUrl(sURL, alHttpProperties); 
      if(urlType == TYPE_TEXT)
      {
         HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(sURL).openConnection();

         createRequestHeader(alHttpProperties, httpURLConnection);
       
         int responseCode = httpURLConnection.getResponseCode();
      }
      else if(urlType == TYPE_ZIP)
      {
         return sURL;
      }
      else
      {
         
         
      }
         
         
      return null;
   }

   private CFile createFile(String sPath,
                            String sFileURL,
                            ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {
      sFileURL = checkFileUrl(sFileURL, alHttpProperties);
      CFile flEasternSpirit = new CFile(sPath + File.separator, sFileURL);
      return flEasternSpirit;
   }

   private String checkFileUrl(String sURL,
                             ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {
      String sResponse = null;

      HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(sURL).openConnection();

      createRequestHeader(alHttpProperties, httpURLConnection);
    
      int responseCode = httpURLConnection.getResponseCode();
    
      if(responseCode == HttpURLConnection.HTTP_OK)
      {
         String contentType = httpURLConnection.getContentType();
         if(contentType == null)
            throw new Exception("Null content type");

         if(contentType.contains(CONTENT_TYPE_TXT_HTML))
         {
            sResponse = getHttpResponse(httpURLConnection);
               
            Matcher matcher = ptnEasternSpiritFileUrl.matcher(sResponse);
            if(matcher.find())
            
            {
               String sEasternSpiritFile = matcher.group(1).replaceAll("&amp;", "&");
               return checkFileUrl(sEasternSpiritFile, alHttpProperties);
            }
            else
               throw new Exception("Can't find file URL");
         }
         else if(contentType.contains(CONTENT_TYPE_APP_ZIP) || contentType.contains(CONTENT_TYPE_APP_RAR))
            return sURL;
         else
            throw new Exception("Unknown content type: " + contentType);
            
      }
      else if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP
            || responseCode == HttpURLConnection.HTTP_MOVED_PERM)
      {
         String sNewURL = httpURLConnection.getHeaderField("Location");
         return checkFileUrl(sNewURL, alHttpProperties);
      }
      else
         throw new Exception("Http request response code: " + responseCode);
   }
   
   private CFile createFile1(String sPath,
                            String sFileURL,
                            ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {
      byte btUrlContentType = checkFileUrl1(sFileURL, alHttpProperties); 
      
      CFile flEasternSpirit = null; 
      if(btUrlContentType == TYPE_ZIP)
         flEasternSpirit = new CFile(sPath + File.separator, sFileURL);
      else
      {
         
      }
      return flEasternSpirit;
   }
   
   private byte checkFileUrl1(String sURL,
                               ArrayList<SHttpProperty> alHttpProperties) throws Exception
   {
      HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(sURL).openConnection();

      createRequestHeader(alHttpProperties, httpURLConnection);
    
      int responseCode = httpURLConnection.getResponseCode();
    
      if(responseCode == HttpURLConnection.HTTP_OK)
      {
         String contentType = httpURLConnection.getContentType();
         if(contentType == null)
            throw new Exception("Null content type");

         if(contentType.contains(CONTENT_TYPE_TXT_HTML))
            return TYPE_TEXT;
         else if(contentType.contains(CONTENT_TYPE_APP_ZIP) || contentType.contains(CONTENT_TYPE_APP_RAR))
            return TYPE_ZIP;
         else
            throw new Exception("Unknown content type: " + contentType);
            
      }
      else
         throw new Exception("Http request response code: " + responseCode);
   }
   
   private String getHttpResponse1(HttpURLConnection httpURLConnection) throws Exception
   {
      int responseCode = httpURLConnection.getResponseCode();

      if(responseCode == HttpURLConnection.HTTP_OK)
      {
         String contentType = httpURLConnection.getContentType();
         if(contentType == null)
            throw new Exception("Null content type");
         else if(contentType.equalsIgnoreCase(CONTENT_TYPE_TXT_HTML))
         {
            return getHttpResponse(httpURLConnection);
         }
      }
      else if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP
            || responseCode == HttpURLConnection.HTTP_MOVED_PERM)
      {
         String sNewURL = httpURLConnection.getHeaderField("Location");
         HttpURLConnection newHttpURLConnection = (HttpURLConnection) new URL(sNewURL).openConnection();
         return getHttpResponse1(newHttpURLConnection);
      }
      else
         throw new Exception("Http request response code: " + responseCode);
      return null;
   }

   private void createCookiesCollection(ArrayList<SHttpProperty> alHttpProperties)
   {
//      alHttpProperties = new ArrayList<SHttpProperty>();
      String sCookies = COOKIE_UID + "=" + easternSpiritSettings.sCookieUID + "; " 
                      + COOKIE_PASS + "=" + easternSpiritSettings.sCookiePass  + "; " 
                      + COOKIE_SESSION_FRONT + "=" + easternSpiritSettings.sCookieSessionFront ;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
   }

   private String getHttpResponse(HttpURLConnection httpURLConnection) throws IOException
   {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

      String inputLine;
      StringBuffer sbResponse = new StringBuffer();

      while((inputLine = bufferedReader.readLine()) != null)
         sbResponse.append(inputLine + "\n");
      bufferedReader.close();

      return sbResponse.toString();
   }

   private void createRequestHeader(ArrayList<SHttpProperty> alHttpProperties,
                                    HttpURLConnection httpURLConnection) throws ProtocolException
   {
      // optional default is GET
      httpURLConnection.setRequestMethod("GET");

     //   add reuqest header
      httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
      if(alHttpProperties != null && !alHttpProperties.isEmpty())
      {
         for(SHttpProperty httpProperty : alHttpProperties)
            httpURLConnection.setRequestProperty(httpProperty.name, httpProperty.value);
      }
   }
}
