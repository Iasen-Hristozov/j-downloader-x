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

public class SubsSab extends Plugin
{
   private final static String DOMAIN = "subs.sab.bz";
   
   private final static Pattern ptnTitle = Pattern.compile("<big>(<.+?>)?(.+?)</big>"),
                                ptnURL = Pattern.compile("\u0421\u0412\u0410\u041b\u0418 \u0421\u0423\u0411\u0422\u0418\u0422\u0420\u0418\u0422\u0415&nbsp;</a><center><br/><br/><fb:like href=\"(.+?)\""),
                                ptnSubssabFileUlr = Pattern.compile("((http:\\/\\/)?(www\\.)?subs\\.sab\\.bz\\/index\\.php\\?(&amp;act=download&amp;)?(s(id)?=[\\d\\w]+(&amp;){1,2})?(act=download&amp;)?(sid=[\\d]+&amp;)?attach_id=.+?)(\\s|\\\")");
   
   private String              sTitle,
                               sUrl;
   
   static
   {
      PluginFactory.registerPlugin(DOMAIN, new SubsSab(DownloaderPassClass.getDownloader()));
   }   
   
   public SubsSab()
   {
      super();
   }
   
   public SubsSab(IDownloader oDownloader)
   {
      super(oDownloader);
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }

   @Override
   protected void loadSettings()
   {
   }

   @Override
   public ArrayList<String> parseContent(String sContent)
   {
      ArrayList<String> alUrlMovies = new ArrayList<String>();
   
      Matcher m = ptnURL.matcher(sContent);
      while(m.find())
      {
         String s = m.group();
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
         sUrl = oMatcher.group(1);
      
      oMatcher = ptnTitle.matcher(sResult);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group(oMatcher.groupCount());
         sTitle = sTitle.replaceAll("<.*?>", "");
      }      
   
      vFilesFnd.add(new CFile(sTitle, sUrl));
   
      return vFilesFnd;
   }

   @Override
   public void downloadFile(CFile file, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty("Referer", file.getURL()));
      
      new DownloadFileThread(file, sDownloadFolder, alHttpProperties).execute();
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
   protected Pattern getUrlPattern()
   {
      return ptnSubssabFileUlr;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      return ptnSubssabFileUlr;
   }
   
   @Override
   protected void createCookiesCollection(ArrayList<SHttpProperty> alHttpProperties)
   {
      alHttpProperties.add(new SHttpProperty("Referer", DOMAIN));
   }   
   
}
