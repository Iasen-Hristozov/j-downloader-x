package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
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
                                ptnURL = Pattern.compile("(http://)?subtitrite.net/download/\\d+/.*?/");
   
   private String              sTitle,
                               sUrl;

   static
   {
      PluginFactory.registerPlugin(DOMAIN, new Subtitrite(DownloaderPassClass.getDownloader()));
   }      
   
   public Subtitrite()
   {
      super();
   }
   
   public Subtitrite(IDownloader oDownloader)
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
      ArrayList<String> alUrlFiles = new ArrayList<String>();
   
      Matcher m = ptnURL.matcher(sContent);
      while(m.find())
      {
         String s = m.group();
         alUrlFiles.add(s);
      }
   
      return alUrlFiles;
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      sResult = sResult.replace("\n", "");
   
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
      Matcher oMatcher = ptnURL.matcher(sResult);
      if(oMatcher.find())
         sUrl = oMatcher.group();
      
      oMatcher = ptnTitle.matcher(sResult);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group(1);
      }      
   
      vFilesFnd.add(new CFile(sTitle, sUrl));
   
      return vFilesFnd;
   }

   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      oFile.setURL(oFile.getURL().replaceAll("/subs/", "/download/"));
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty("Referer", oFile.getURL()));
      
      new DownloadFileThread(oFile, sDownloadFolder, alHttpProperties).execute();
//      new DownloadFile(oFile, sDownloadFolder).execute();
   }

   @Override
   protected void downloadFileDone(CFile oFile, String sDownloadFolder, String saveFilePath)
   {
      super.downloadFileDone(oFile, sDownloadFolder, saveFilePath);
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
