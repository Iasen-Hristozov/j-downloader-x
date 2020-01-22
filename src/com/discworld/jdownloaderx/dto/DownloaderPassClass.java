package com.discworld.jdownloaderx.dto;

public class DownloaderPassClass
{
   private static IDownloader oDownloader;

   public static IDownloader getDownloader()
   {
      return oDownloader;
   }

   public static void setDownloader(IDownloader oDownloader)
   {
      DownloaderPassClass.oDownloader = oDownloader;
   }
}
