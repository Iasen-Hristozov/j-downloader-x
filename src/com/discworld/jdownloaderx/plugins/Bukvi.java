package com.discworld.jdownloaderx.plugins;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.Plugin;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class Bukvi extends Plugin
{
   private final static String DOMAIN = "bukvi.";
   
   private final static Pattern ptnTitle = Pattern.compile("<div class=\\\"main-head\\\">(.+?)</div>"),
                                ptnUrl = Pattern.compile("((http:\\/\\/)?bukvi.bg\\/load\\/\\d+(\\/\\w+)?(/[\\d\\-]+)?)"),
                                ptnUrlFile = Pattern.compile("<a href=\\\"((http:\\/\\/)?bukvi(.mmcenter)?.bg\\/load\\/[\\d\\-]+)\\\"");
   
   static {
      PluginFactory.registerPlugin("bukvi.bg", new Bukvi(DownloaderPassClass.getDownloader()));
      PluginFactory.registerPlugin("bukvi.mmcenter.bg", new Bukvi(DownloaderPassClass.getDownloader()));
   }
   
   public Bukvi()
   {
      super();
   }
   
   public Bukvi(IDownloader oDownloader)
   {
      super(oDownloader);
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }

   @Override
   protected void loadSettings()
   {}

   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty("Referer", oFile.getURL()));
      
      new DownloadFileThread(oFile, sDownloadFolder, alHttpProperties).execute();
   }

   @Override
   protected Pattern getUrlPattern()
   {
      return ptnUrl;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      return ptnUrlFile;
   }

   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }

   @Override
   public String getDomain()
   {
      // TODO Check it
      return DOMAIN;
   }

   @Override
   public boolean isForCheck()
   {
      return true;
   }
   
}
