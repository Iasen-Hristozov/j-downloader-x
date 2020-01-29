package com.discworld.jdownloaderx.plugins;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class Subtitrite extends Plugin
{
   private final static String DOMAIN = "subtitrite.net";
   
   private final static Pattern ptnTitle = Pattern.compile("<h2>\u0421\u0443\u0431\u0442\u0438\u0442\u0440\u0438 \u0437\u0430 (.+?)</h2>"),
                                ptnURL = Pattern.compile("((http://)?subtitrite.net/download/\\d+/.*?/)");
   
//   private String              sTitle;
//                               sUrl;

   static
   {
      PluginFactory.registerPlugin(DOMAIN, new Subtitrite(DownloaderPassClass.getDownloader()));
   }      
   
   public Subtitrite()
   {
      super();
   }
   
   public Subtitrite(IDownloader downloader)
   {
      super(downloader);
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }

   @Override
   public void downloadFile(CFile file, String sDownloadFolder)
   {
      file.setURL(file.getURL().replaceAll("/subs/", "/download/"));
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty(REQ_PROP_REFERER, file.getURL()));
      
      new DownloadFileThread(file, sDownloadFolder, alHttpProperties).execute();
   }

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
      return ptnURL;
   }
   
   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }
   
}
