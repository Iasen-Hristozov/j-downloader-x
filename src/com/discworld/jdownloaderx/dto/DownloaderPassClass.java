package com.discworld.jdownloaderx.dto;

public class DownloaderPassClass
{
   private static IDownloader downloader;

   public static IDownloader getDownloader()
   {
      return downloader;
   }

   public static void setDownloader(IDownloader downloader)
   {
      DownloaderPassClass.downloader = downloader;
   }
}
