package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.util.ArrayList;
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
   private final static String DOMAIN = "www.easternspirit.org",
                               SETTINGS_FILE = "easternspirit.xml",
                               COOKIE_UID = "uid",
                               COOKIE_PASS = "pass", 
                               COOKIE_SESSION_FRONT = "ips4_IPSSessionFront";
   
   private final static Pattern ptnTitle = Pattern.compile("<span class='ipsType_break ipsContained'>(.+?)<\\/span>"),
                                ptnURL = Pattern.compile("(http:\\/\\/www\\.easternspirit\\.org\\/forum\\/index\\.php\\?\\/files\\/file\\/[\\d\\w\\-]+\\/)"),
                                ptnFileURL = Pattern.compile("<a href='(http:\\/\\/www\\.easternspirit\\.org\\/forum\\/index\\.php\\?\\/files\\/file\\/[\\d\\w\\-]+\\/&amp;do=download&amp;(r=[\\d]+&amp;)?(confirm=1&amp;)?(t=1&amp;)?csrfKey=[\\w\\d]+)' class='ipsButton");
   
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

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }

//   @Override
//   public ArrayList<String> getURLsFromContent(String sContent)
//   {
//      ArrayList<String> alUrlMovies = new ArrayList<String>();
//   
//      Matcher m = ptnFileURL.matcher(sContent);
//      while(m.find())
//      {
//         String sURL = HTTP + DOMAIN + m.group(1);
//         alUrlMovies.add(sURL);
//      }
//   
//      return alUrlMovies;
//   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      sResult = sResult.replace("\n", "");
//      String sUrl = getFileUrl(sResult);
      ArrayList<String> alURLs = getFileUrl(sResult);
      String sTitle = getTitle(sResult);

      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
//      alFilesFound.add(new CFile(sTitle + File.separator, sUrl));
   
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      prepareHttpRequestHeader(alHttpProperties);
      
      try
      {
         for(String sURL: alURLs)
            alFilesFound.addAll(getFilesFromUrl(sTitle,
                                                alHttpProperties,
                                                sURL));
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      
      return alFilesFound;
   }

   @Override
   public void downloadFile(CFile file, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      prepareHttpRequestHeader(alHttpProperties);
      
      new DownloadFileThread(file, sDownloadFolder, alHttpProperties).execute();
   }
   
   @Override
   protected void prepareHttpRequestHeader(ArrayList<SHttpProperty> alHttpProperties)
   {
      String sCookies = COOKIE_UID + "=" + easternSpiritSettings.sCookieUID + "; " 
                      + COOKIE_PASS + "=" + easternSpiritSettings.sCookiePass  + "; " 
                      + COOKIE_SESSION_FRONT + "=" + easternSpiritSettings.sCookieSessionFront ;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
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

   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }

   @Override
   public String getDomain()
   {
      return DOMAIN;
   }

   @Override
   public boolean isForCheck()
   {
      return true;
   }
}
