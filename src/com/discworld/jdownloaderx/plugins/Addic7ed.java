package com.discworld.jdownloaderx.plugins;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class Addic7ed extends Plugin
{
//   private final static String DOMAIN = "addic7ed.com";
   private final static String DOMAIN = "www.addic7ed.com";
   
   
   private final static Pattern ptnTitle = Pattern.compile("<span class=\"titulo\">(.+?)<small>"),
                                ptnURL = Pattern.compile("((http:\\/\\/)?(www.)?addic7ed.com\\/\\S*)"),
                                ptnFileURL = Pattern.compile("href=\\\"(\\/(original|updated)\\/.+?)\\\"");
   
   static 
   {
      PluginFactory.registerPlugin(DOMAIN, new Addic7ed(DownloaderPassClass.getDownloader()));
   }
   
   public Addic7ed()
   {
      super();
   }
   
   public Addic7ed(IDownloader oDownloader)
   {
      super(oDownloader);
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }

   public static ArrayList<String> parseResponse(String sResponse)
   {
      ArrayList<String> alUrlMovies = new ArrayList<String>();
   
      Matcher m = ptnFileURL.matcher(sResponse);
      while(m.find())
      {
         String s = m.group(1);
         alUrlMovies.add("http://" + DOMAIN + s);
      }
   
      return alUrlMovies;
   }
   
   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      sResult = sResult.replace("\n", "");
      String sUrl = getFileUrl(sResult);
      String sTitle = getTitle(sResult).replaceAll("<.*?>", "");

      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      alFilesFound.add(new CFile(sTitle, sUrl));
      return alFilesFound;
   }

   @Override
   public void downloadFile(CFile file, String sDownloadFolder)
   {
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
      return ptnFileURL;
   }

   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }

}
