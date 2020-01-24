package com.discworld.jdownloaderx.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class SubsUnacs extends Plugin
{
   private final static String DOMAIN = "subsunacs.net",
                               HTTP = "http://",
                               HTTPS = "https://",
                               DWN = "http://subsunacs.net/get.php?id="; 
                               
   private final static Pattern ptnTitle = Pattern.compile("<h1>(.+?)</h1>"),
                                ptnFileURL = Pattern.compile("<div id=\"buttonBox\"><a href=\"(.+?)\""),
                                ptnID = Pattern.compile("http(s?)://(www\\.)?subsunacs\\.net(/){1,2}((subtitles/.+?-)|(info\\.php\\?id=))(\\d+)/?"),
                                ptnUrl = Pattern.compile("((http(s)?:\\/\\/)?(www\\.)?subsunacs.net\\/(((get|info)\\.php\\?id=\\d+)|(subtitles\\/.+?\\/)))");
//                                ptnURLs = Pattern.compile("<a href=\"(\\/subtitles\\/[\\w\\d_\\-]+\\/)?\"");
   
   private String              sTitle,
                               sUrl;

   static
   {
      PluginFactory.registerPlugin(DOMAIN, new SubsUnacs(DownloaderPassClass.getDownloader()));
   }      
   
   public SubsUnacs()
   {
      super();
   }
   
   public SubsUnacs(IDownloader downloader)
   {
      super(downloader);
   }

   @Override
   protected void loadSettings()
   {
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
      Matcher matcher = ptnFileURL.matcher(sResult);
      if(matcher.find())
      {
         sUrl = matcher.group(1);
         sUrl = HTTP + DOMAIN + sUrl;
      }
      
      matcher = ptnTitle.matcher(sResult);
      if(matcher.find())
      {
         sTitle = matcher.group(1);
         sTitle = sTitle.replace("nbsp;", " ");
      }      

      vFilesFnd.add(new CFile(sTitle, sUrl));

      return vFilesFnd;
   }

   @Override
   public ArrayList<String> parseContent(String sContent)
   {
      ArrayList<String> alUrlMovies = new ArrayList<String>();
      
      Matcher m = ptnFileURL.matcher(sContent);
      while(m.find())
      {
         String s = m.group();
         alUrlMovies.add(s);
      }

      return alUrlMovies;
   }
   
   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      Matcher oMatcher = ptnID.matcher(oFile.getURL());
      if(oMatcher.find())
         oFile.setURL(DWN + oMatcher.group(7));
      if(!oFile.getURL().contains(HTTP) && !oFile.getURL().contains(HTTPS))
         oFile.setURL(HTTP + oFile.getURL());
      
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty("Referer", oFile.getURL()));
      
      new DownloadFileThread(oFile, sDownloadFolder, alHttpProperties).execute();
      
   }

   @Override
   protected void downloadFileDone(CFile file, String sDownloadFolder, String saveFilePath)
   {
      super.downloadFileDone(file, sDownloadFolder, saveFilePath);
      try
      {
         super.moveFileToSavePath(file, sDownloadFolder, saveFilePath);      
      } 
      catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   @Override
   protected Pattern getUrlPattern()
   {
      return ptnUrl;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      return ptnFileURL;
   }
   
   @Override
   public String getDomain()
   {
      return DOMAIN;
   }
   
   @Override
   protected void createCookiesCollection(ArrayList<SHttpProperty> alHttpProperties)
   {
      alHttpProperties.add(new SHttpProperty("Referer", DOMAIN));
   }
   
   @Override
   public ArrayList<CFile> checkContetWithPlugin(String sPath, String sContent)
   {
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();

      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      createCookiesCollection(alHttpProperties);
      
      Matcher matcher = getUrlPattern().matcher(sContent);
      while(matcher.find())
      {
         String sURL = matcher.group(1).replaceAll("&amp;", "&");
         try
         {
//            alHttpProperties.add(new SHttpProperty("Referer", sURL));
            checkAddHttpProperty(alHttpProperties, new SHttpProperty("Referer", sURL));
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

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }
}
