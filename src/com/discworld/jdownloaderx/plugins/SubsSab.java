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

public class SubsSab extends Plugin
{
   private final static String DOMAIN = "subs.sab.bz";
   
   private final static Pattern ptnTitle = Pattern.compile("<big>(<.+?>)?(.+?)</big>"),
                                ptnURL = Pattern.compile("\u0421\u0412\u0410\u041b\u0418 \u0421\u0423\u0411\u0422\u0418\u0422\u0420\u0418\u0422\u0415&nbsp;</a><center><br/><br/><fb:like href=\"(.+?)\""),
                                ptnFileURL = Pattern.compile("((http:\\/\\/)?(www\\.)?subs\\.sab\\.bz\\/index\\.php\\?(&amp;act=download&amp;)?(s(id)?=[\\d\\w]+(&amp;){1,2})?(act=download&amp;)?(sid=[\\d]+&amp;)?attach_id=.+?)(\\s|\\\")");
   
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
      alHttpProperties.add(new SHttpProperty("Referer", file.getURL()));
      
      new DownloadFileThread(file, sDownloadFolder, alHttpProperties).execute();
   }
   
   @Override
   protected Pattern getUrlPattern()
   {
      return ptnFileURL;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      return ptnFileURL;
   }
   
   @Override
   protected void createCookiesCollection(ArrayList<SHttpProperty> alHttpProperties)
   {
      alHttpProperties.add(new SHttpProperty("Referer", DOMAIN));
   }

   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }   
   
}
