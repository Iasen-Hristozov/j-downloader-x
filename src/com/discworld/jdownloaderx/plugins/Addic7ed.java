package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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
                                ptnURL = Pattern.compile("((http(s)?:\\/\\/)?(www\\.)?addic7ed\\.com\\/.*?)(\\\"|\\s|<)"),
                                ptnFileURL = Pattern.compile("href=\\\"(\\/(original|updated)\\/.+?)\\\"");
   
   static 
   {
      PluginFactory.registerPlugin(DOMAIN, new Addic7ed(DownloaderPassClass.getDownloader()));
   }
   
   public Addic7ed()
   {
      super();
   }
   
   public Addic7ed(IDownloader downloader)
   {
      super(downloader);
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }
   
   @Override
   public ArrayList<String> getURLsFromContent(String sContent)
   {
      ArrayList<String> alURLS = super.getURLsFromContent(sContent);
      for(int i = 0; i < alURLS.size(); i++)
      {
         try
         {
            alURLS.set(i, convertURLtoUTF8(alURLS.get(i)));
         }
         catch(MalformedURLException
               | UnsupportedEncodingException
               | URISyntaxException e)
         {
            e.printStackTrace();
         }
      }
      return alURLS;
   }

   protected String convertURLtoUTF8(String sURL) throws MalformedURLException,
                                                  URISyntaxException,
                                                  UnsupportedEncodingException
   {
      URL url = new URL(sURL);
      URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), URLEncoder.encode(url.getPath(), "UTF-8").replaceAll("%2F", "/"), url.getQuery(), url.getRef());
      sURL = uri.toString();
      return sURL;
   }
   
   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();

      sResult = sResult.replace("\n", "");
      ArrayList<String> alURLs = getFileUrl(sResult);

      String sTitle = getTitle(sResult);
      for(String sURL: alURLs)
         alFilesFound.add(new CFile(sTitle + File.separator, HTTPS + DOMAIN + sURL));

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
   protected void prepareHttpRequestHeader(ArrayList<SHttpProperty> alHttpProperties)
   {
      alHttpProperties.add(new SHttpProperty(REQ_PROP_REFERER, HTTPS + DOMAIN));
   }   
   
   @Override
   protected void loadSettings()
   {}

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

   @Override
   public String getDomain()
   {
      return DOMAIN;
   }

   @Override
   public boolean isForCheck()
   {
      return true;
   }

}
