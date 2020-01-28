package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.util.ArrayList;
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
//   private static final String CONTENT_TYPE_APP_ZIP = "application/zip",
//                               CONTENT_TYPE_APP_RAR = "application/x-rar-compressed",
//                               CONTENT_TYPE_TXT_HTML = "text/html";

   private final static String DOMAIN = "www.easternspirit.org",
                               SETTINGS_FILE = "easternspirit.xml",
                               COOKIE_UID = "uid",
                               COOKIE_PASS = "pass", 
                               COOKIE_SESSION_FRONT = "ips4_IPSSessionFront";
//                               USER_AGENT = "Mozilla/5.0";
   
   private final static Pattern ptnTitle = Pattern.compile("<h3 class=\"title\">(.+?)\\["),
//                                ptnURL = Pattern.compile("<a href=\\'(request\\.php\\?\\d+)\\'> <img src=\\'e107_images\\/generic\\/lite\\/download.png\\' alt=\\'\\' style=\\'border:0\\' \\/>");
//                                  ptnURL = Pattern.compile("(http:\\/\\/www\\.easternspirit\\.org\\/forum\\/index\\.php\\?\\/files\\/file\\/[\\d\\w\\-]+\\/&amp\\;do=download&amp\\;csrfKey=[\\d\\w]+)"),
                                  ptnURL = Pattern.compile("(http:\\/\\/www\\.easternspirit\\.org\\/forum\\/index\\.php\\?\\/files\\/file\\/[\\d\\w\\-]+\\/)"),
                                  ptnFileURL = Pattern.compile("<a href='(http:\\/\\/www\\.easternspirit\\.org\\/forum\\/index\\.php\\?\\/files\\/file\\/[\\d\\w\\-]+\\/&amp;do=download&amp;(r=[\\d]+&amp;)?(confirm=1&amp;)?(t=1&amp;)?csrfKey=[\\w\\d]+)' class='ipsButton");

//   private static final int TYPE_TEXT = 1,
//                            TYPE_ZIP = 2;
   
//   private String              sTitle,
//                               sUrl;
   
   private EasternSpiritSettings easternSpiritSettings;
   
   static
   {
      PluginFactory.registerPlugin(DOMAIN, new EasternSpirit(DownloaderPassClass.getDownloader()));
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
   public ArrayList<String> getURLsFromContent(String sContent)
   {
      ArrayList<String> alUrlMovies = new ArrayList<String>();
   
      Matcher m = ptnFileURL.matcher(sContent);
      while(m.find())
      {
         String s = m.group(1);
         s = "http://" + DOMAIN + s;
         alUrlMovies.add(s);
      }
   
      return alUrlMovies;
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      sResult = sResult.replace("\n", "");
      String sUrl = getFileUrl(sResult);
      sUrl = "http://" + DOMAIN + sUrl;
      String sTitle = getTitle(sResult);

      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      alFilesFound.add(new CFile(sTitle, sUrl));
   
      return alFilesFound;
   }

   @Override
   public void downloadFile(CFile file, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      createCookiesCollection(alHttpProperties);
      
      new DownloadFileThread(file, sDownloadFolder, alHttpProperties).execute();
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
   
   @Override
   protected Pattern getUrlPattern()
   {
      return ptnURL;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      return ptnFileURL;
   }

   
//   @Override
//   public ArrayList<CFile> checkContetWithPlugin(String sPath, String sContent)
//   {
//      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
//
//      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
//      createCookiesCollection(alHttpProperties);
//      
//      Matcher matcher = getUrlPattern().matcher(sContent);
//      if(matcher.find())
//      {
//         String sEasternSpiritURL = matcher.group();
//         try
//         {
//            alFilesFound.addAll(getFilesFormUrl(sPath, sEasternSpiritURL, alHttpProperties));
//         }
//         catch(Exception e)
//         {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//         }
//      }
//      
//      return alFilesFound;
//   }
   
//   private ArrayList<CFile> getFilesFormUrl(String sPath, String sUrl, ArrayList<SHttpProperty> alHttpProperties) throws Exception
//   {            
//      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
//      
//      String sEasternSpiritResponse = getHttpResponse(sUrl, alHttpProperties);
//      
//      Matcher matcher = getFileUrlPattern().matcher(sEasternSpiritResponse);
//      while(matcher.find())
//      {
//         String sNewUrl = matcher.group(1).replaceAll("&amp;", "&");
//         byte type = getUrlContentType(sNewUrl, alHttpProperties);
//         switch(type)
//         {
//            case TYPE_TEXT:
//               alFilesFound.addAll(getFilesFormUrl(sPath, sNewUrl, alHttpProperties));
//            break;
//               
//            case TYPE_ZIP:
//               CFile flEasternSpirit = createFile(sPath, sNewUrl);
//               alFilesFound.add(flEasternSpirit);
//            break;
//         }
//      }
//      return alFilesFound;
//   }
   
//   private byte getUrlContentType(String sUrl, ArrayList<SHttpProperty> alHttpProperties) throws Exception
//   {
//      HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(sUrl).openConnection();
//
//      createRequestHeader(alHttpProperties, httpURLConnection);
//    
//      int responseCode = httpURLConnection.getResponseCode();
//    
//      if(responseCode == HttpURLConnection.HTTP_OK)
//      {
//         String contentType = httpURLConnection.getContentType();
//         if(contentType == null)
//            throw new Exception("Null content type");
//
//         if(contentType.contains(CONTENT_TYPE_TXT_HTML))
//            return TYPE_TEXT;
//         else if(contentType.contains(CONTENT_TYPE_APP_ZIP)
//                  || contentType.contains(CONTENT_TYPE_APP_RAR))
//         {
//            return TYPE_ZIP;
//         }
//         else
//            throw new Exception("Unknown content type: " + contentType);
//      }
//      else if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP
//               || responseCode == HttpURLConnection.HTTP_MOVED_PERM)
//         {
//            sUrl = httpURLConnection.getHeaderField("Location");
//            return getUrlContentType(sUrl, alHttpProperties);
//         }
//      else
//         throw new Exception("Http request response code: " + responseCode);
//   }
   
//   private CFile createFile(String sPath, String sUrl)
//   {
//      return new CFile(sPath + File.separator, sUrl);
//   }
   
   @Override
   protected void createCookiesCollection(ArrayList<SHttpProperty> alHttpProperties)
   {
      String sCookies = COOKIE_UID + "=" + easternSpiritSettings.sCookieUID + "; " 
                      + COOKIE_PASS + "=" + easternSpiritSettings.sCookiePass  + "; " 
                      + COOKIE_SESSION_FRONT + "=" + easternSpiritSettings.sCookieSessionFront ;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
   }

   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
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
}
