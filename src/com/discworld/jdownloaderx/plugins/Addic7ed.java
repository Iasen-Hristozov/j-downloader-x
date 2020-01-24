package com.discworld.jdownloaderx.plugins;

import java.io.IOException;
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
                                ptnURL = Pattern.compile("href=\\\"(\\/(original|updated)\\/.+?)\\\""),
                                ptnAddic7edUrl = Pattern.compile("((http:\\/\\/)?(www.)?addic7ed.com\\/\\S*)"),
                                ptnAddic7edFileUrl = Pattern.compile("href=\\\"(\\/(original|updated)\\/.+?)\\\"");

   private String              sTitle,
                               sUrl;
   
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

   @Override
   public ArrayList<String> parseContent(String sContent)
   {
      return parseResponse(sContent);
   }

   public static ArrayList<String> parseResponse(String sResponse)
   {
      ArrayList<String> alUrlMovies = new ArrayList<String>();
   
      Matcher m = ptnURL.matcher(sResponse);
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
   
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
      Matcher oMatcher = ptnURL.matcher(sResult);
      if(oMatcher.find())
         sUrl = oMatcher.group(1);
      
      oMatcher = ptnTitle.matcher(sResult);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group(1);
         sTitle = sTitle.replaceAll("<.*?>", "");
      }      
   
      vFilesFnd.add(new CFile(sTitle, sUrl));
   
      return vFilesFnd;
   }

   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty("Referer", oFile.getURL()));
      
      new DownloadFileThread(oFile, sDownloadFolder, alHttpProperties).execute();
   }

   @Override
   protected void downloadFileDone(CFile file, String sDownloadFolder, String saveFilePath)
   {
      super.downloadFileDone(file, sDownloadFolder, saveFilePath);
      try
      {
         super.moveFileToSavePath(file, sDownloadFolder, saveFilePath);
      } 
      catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   @Override
   protected void loadSettings()
   {
   }

   @Override
   protected Pattern getUrlPattern()
   {
      return ptnAddic7edUrl;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      return ptnAddic7edFileUrl;
   }

}
