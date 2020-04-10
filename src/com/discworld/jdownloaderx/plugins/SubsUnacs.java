package com.discworld.jdownloaderx.plugins;

import java.io.File;
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
                               DWN = "http://subsunacs.net/get.php?id=",
                               GRP_ID = "id"; 
                               
   private final static Pattern ptnURL = Pattern.compile("((http(s)?:\\/\\/)?((www|utf)\\.)?subsunacs.net\\/(((get|info)\\.php\\?id=\\d+)|(subtitles\\/.+?\\/)))"), 
                                ptnTitle = Pattern.compile("<h1>(.+?)</h1>"),
                                ptnFileURL = Pattern.compile("<div id=\"buttonBox\"><a href=\"(.+?)\""),
                                ptnID = Pattern.compile("http(s?)://((www|utf)\\.)?subsunacs\\.net(/){1,2}((subtitles/.+?-)|(info\\.php\\?id=))(?<" + GRP_ID + ">\\d+)/?");

   static
   {
      PluginFactory.registerPlugin(DOMAIN, new SubsUnacs(DownloaderPassClass.getDownloader()));
      PluginFactory.registerPlugin("utf.subsunacs.net", new SubsUnacs(DownloaderPassClass.getDownloader()));
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
      sResult = sResult.replace("\n", "");
      ArrayList<String> alURLs = getFileUrl(sResult);
      String sTitle = getTitle(sResult);

      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      for(String sURL: alURLs)
         alFilesFound.add(new CFile(sTitle + File.separator, HTTPS + DOMAIN + sURL));

      return alFilesFound;
   }
   
   @Override
   public void downloadFile(CFile file, String sDownloadFolder)
   {
      Matcher oMatcher = ptnID.matcher(file.getURL());
      if(oMatcher.find())
//         file.setURL(DWN + oMatcher.group(7));
         file.setURL(DWN + oMatcher.group(GRP_ID));
      if(!file.getURL().contains(HTTP) && !file.getURL().contains(HTTPS))
         file.setURL(HTTPS + file.getURL());
      
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty(REQ_PROP_REFERER, file.getURL()));
      
      new DownloadFileThread(file, sDownloadFolder, alHttpProperties).execute();
      
   }
   
   @Override
   protected void prepareHttpRequestHeader(ArrayList<SHttpProperty> alHttpProperties)
   {
      alHttpProperties.add(new SHttpProperty(REQ_PROP_REFERER, HTTPS+DOMAIN));
   }

   @Override
   public String getDomain()
   {
      return DOMAIN;
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
   public boolean isForCheck()
   {
      return true;
   }
}
