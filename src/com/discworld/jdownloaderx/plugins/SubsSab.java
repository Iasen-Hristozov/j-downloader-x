package com.discworld.jdownloaderx.plugins;

import java.util.ArrayList;
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
   
   private final static Pattern ptnTitle = Pattern.compile("<big><.+?>?<.+?>(.+?)<\\/div><\\/big>"),
                                ptnURL = Pattern.compile("((http(s)?:\\/\\/)?subs.sab.bz\\/index.php\\?act=download&(amp;)?(sid|attach_id)=\\d+)"),
                                ptnFileURL = Pattern.compile("(((http:\\/\\/)?(www\\.)?subs\\.sab\\.bz\\/index\\.php\\?(&amp;act=download&amp;)?(s(id)?=[\\d\\w]+(&amp;){1,2})?(act=download&amp;)?(sid=[\\d]+&amp;)?attach_id=.+?))(\\s|\\\")");
   
   static
   {
      PluginFactory.registerPlugin(DOMAIN, new SubsSab(DownloaderPassClass.getDownloader()));
   }   
   
   public SubsSab()
   {
      super();
   }
   
   public SubsSab(IDownloader downloader)
   {
      super(downloader);
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }

   @Override
   protected void loadSettings()
   {}

//   @Override
//   protected ArrayList<String> getFileUrl(String sResult)
//   {
//      ArrayList<String> alURLs = super.getFileUrl(sResult); 
//      for(int i = 0; i<alURLs.size(); i++ )
//         alURLs.set(i, alURLs.get(i).replaceAll("&amp;", "&"));
//      return alURLs;
//   }
   
   @Override 
   protected String clearUrl(String sURL)
   {
      return sURL.replaceAll("&amp;", "&");
   }
   
//   @Override
//   protected ArrayList<CFile> doneHttpParse(String sResult)
//   {
//      sResult = sResult.replace("\n", "");
////      String sUrl = getFileUrl(sResult).replaceAll("&amp;", "&"); 
//      String sTitle = getTitle(sResult).replaceAll("<.*?>", "");
//      
//      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
//      alFilesFound.add(new CFile(sTitle + File.separator, sUrl));
//   
//      return alFilesFound;
//   }

   @Override
   public void downloadFile(CFile file, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty(REQ_PROP_REFERER, DOMAIN));
      
      new DownloadFileThread(file, sDownloadFolder, alHttpProperties).execute();
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
   
//   @Override
//   protected String getTitle(String sResult)
//   {
//      return super.getTitle(sResult).replaceAll("</span>", "");
//   }
   
   @Override
   protected void prepareHttpRequestHeader(ArrayList<SHttpProperty> alHttpProperties)
   {
      alHttpProperties.add(new SHttpProperty("Referer", DOMAIN));
   }

   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }

   @Override
   public String getDomain()
   {
      return DOMAIN;
   }   
   
}
