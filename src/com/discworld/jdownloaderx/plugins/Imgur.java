package com.discworld.jdownloaderx.plugins;

import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;


public class Imgur extends Plugin
{
   private final static String DOMAIN = "imgur.com";

   static
   {
      PluginFactory.registerPlugin("i." + DOMAIN, new Imgur(DownloaderPassClass.getDownloader()));
   }

   public Imgur()
   {
      super();
   }
   
   public Imgur(IDownloader downloader)
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
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean isForCheck()
   {
      // TODO Auto-generated method stub
      return false;
   }

}
