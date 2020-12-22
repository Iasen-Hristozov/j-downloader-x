package com.discworld.jdownloaderx.plugins;

import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;

public class Picbg extends Plugin
{
   private final static String DOMAIN = "store.picbg.net";
   
   static
   {
      PluginFactory.registerPlugin(DOMAIN, new Picbg(DownloaderPassClass.getDownloader()));
   }

   public Picbg()
   {
      super();
   }
   
   public Picbg(IDownloader downloader)
   {
      super(downloader);
   }
   
   @Override
   public String getDomain()
   {
      return DOMAIN;
   }

   @Override
   protected void loadSettings()
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected Pattern getUrlPattern()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected Pattern getTitlePattern()
   {
      return null;
   }

   @Override
   public boolean isForCheck()
   {
      return false;
   }
}
