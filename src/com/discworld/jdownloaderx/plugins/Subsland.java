package com.discworld.jdownloaderx.plugins;

import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;

public class Subsland extends Plugin
{
   private final static String DOMAIN = "subsland.com";
   
   private final static Pattern ptnTitle = Pattern.compile("<TITLE>subsland.com - \u0421\u0443\u0431\u0442\u0438\u0442\u0440\u0438 (.+?)</TITLE>"),
                                ptnURL = Pattern.compile("((http(s)?://)?subsland.com/subtitles/.+?\\.html)"),
                                ptnFileURL = Pattern.compile("((http(s)?://)?subsland\\.com/downloadsubtitles/(.+?)(\\.rar)|(\\.zip))");
   
   static
   {
      PluginFactory.registerPlugin(DOMAIN, new Subsland(DownloaderPassClass.getDownloader()));
   }
   
   public Subsland()
   {
      super();
   }
   
   public Subsland(IDownloader downloader)
   {
      super(downloader);
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }

   @Override
   protected void loadSettings()
   {
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
