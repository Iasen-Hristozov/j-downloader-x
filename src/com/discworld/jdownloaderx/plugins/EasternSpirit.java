package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.SHttpProperty;
import com.discworld.jdownloaderx.plugins.Plugin.DownloadFile;

public class EasternSpirit extends Plugin
{
   private final static String DOMAIN = "easternspirit.org";
   
   private final static Pattern ptnTitle = Pattern.compile("<h3 class=\"title\">(.+?)\\["),
                                ptnURL = Pattern.compile("<a href=\\'(request\\.php\\?\\d+)\\'> <img src=\\'e107_images\\/generic\\/lite\\/download.png\\' alt=\\'\\' style=\\'border:0\\' \\/>");
   
   private String              sTitle,
                               sUrl;
   
   public EasternSpirit()
   {
      super();
   }
   
   public EasternSpirit(IDownloader oDownloader)
   {
      super(oDownloader);
   }

   @Override
   public boolean isMine(String sURL)
   {
      return sURL.contains(DOMAIN);
   }

   @Override
   public ArrayList<String> parseContent(String sContent)
   {
      ArrayList<String> alUrlMovies = new ArrayList<String>();
   
      Matcher m = ptnURL.matcher(sContent);
      while(m.find())
      {
         String s = m.group(1);
         s = "http://www." + DOMAIN + s;
         alUrlMovies.add(s);
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
      {
         sUrl = oMatcher.group(1);
         sUrl = "http://www." + DOMAIN + sUrl;
      }      
      
      oMatcher = ptnTitle.matcher(sResult);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group(1);
         sTitle = sTitle.trim();
      }      
   
      vFilesFnd.add(new CFile(sTitle, sUrl));
   
      return vFilesFnd;
   }

   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
//      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
//      alHttpProperties.add(new SHttpProperty("Referer", oFile.getURL()));
//      
//      new DownloadFile(oFile, sDownloadFolder, alHttpProperties).execute();
      new DownloadFile(oFile, sDownloadFolder).execute();
   }

   @Override
   protected void doneDownloadFile(CFile oFile, String sDownloadFolder, String saveFilePath)
   {
      super.doneDownloadFile(oFile, sDownloadFolder, saveFilePath);
      try
      {
         File f;
         if(oFile.getName().endsWith(File.separator))
            f = new File(sDownloadFolder + File.separator + oFile.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator)+ 1));
         else
            f = new File(sDownloadFolder + File.separator + oFile.getName());
         f.getParentFile().mkdirs();
         File source = new File(saveFilePath);
         Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
}
