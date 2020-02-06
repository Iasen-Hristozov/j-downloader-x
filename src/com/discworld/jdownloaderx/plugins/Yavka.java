package com.discworld.jdownloaderx.plugins;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class Yavka extends Plugin
{
   private final static String DOMAIN = "yavka.net";
                               
   private final static Pattern ptnTitle = Pattern.compile("<h1>.*&nbsp;(.+?)<\\/h1>"),
                                ptnURL = Pattern.compile("((http(s)?:\\/\\/)yavka.net\\/subs\\/\\d+\\/\\w{1,3})"),
                                ptnFileURL = Pattern.compile("((http(s)?:\\/\\/)yavka.net\\/subs\\/\\d+\\/\\w{1,3}\\/)");

   static
   {
      PluginFactory.registerPlugin(DOMAIN, new Yavka(DownloaderPassClass.getDownloader()));
   }      
   
   public Yavka()
   {
      super();
   }
   
   public Yavka(IDownloader downloader)
   {
      super(downloader);
   }

   @Override
   protected void loadSettings()
   {}
 
   @Override
   protected void prepareHttpRequestHeader(ArrayList<SHttpProperty> alHttpProperties)
   {
      alHttpProperties.add(new SHttpProperty(REQ_PROP_REFERER, HTTP + DOMAIN));
   }

   @Override
   public void downloadFile(CFile file, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty(REQ_PROP_REFERER, HTTP + DOMAIN));
      
      new DownloadFileThread(file, sDownloadFolder, alHttpProperties).execute();
      
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
