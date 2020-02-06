package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.MoviePlugin;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;

public class ZamundaSe extends MoviePlugin
{
   private final static String DOMAIN = "zelka.org",
                               SETTINGS_FILE = "zamunda_se.xml";

   private final static Pattern ptnURL = Pattern.compile("((http:\\/\\/)?zelka\\.org\\/details\\.php\\?id=(\\d)*)"),
                                ptnTitle = Pattern.compile("(<h1>)(?<" + GRP_TITLE + ">.+)(<[\\s]*/h1>)"),
                                ptnTitleParts = Pattern.compile("(.*?)( / .*?)* (\\(\\d+(\\-\\d+)?\\))"),
                                ptnTorrent = Pattern.compile("(download.php/\\S+\\.(torrent?))"),
                                ptnMagnet = Pattern.compile("magnet:\\?xt=urn:btih:[\\w]*"),
                                ptnImage = Pattern.compile("<img border=\\\"0\\\" src=\\\"(?<"+GRP_IMAGE+">.+?)\\\">"),
                                ptnDescription = Pattern.compile("(\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435)(?<"+GRP_DESCRIPTION+">.*?)((\u0421\u0432\u0430\u043b\u0438 \u0421\u0443\u0431\u0442\u0438\u0442\u0440\u0438)|(NFO))"),
                                ptnZelkasubs = Pattern.compile("(<a href=)((http://)?(www\\.)?((zelka.org)|(zamunda.se))/getsubs.php/(.+?))( target=_blank)?>");
   
   private String sZelkasubs;
   
//   ArrayList<String> alAddic7ed = new ArrayList<String>();
   
   private MovieSettings zamundaSeSettings;

   static 
   {
      PluginFactory.registerPlugin(DOMAIN, new ZamundaSe(DownloaderPassClass.getDownloader()));
      PluginFactory.registerPlugin("zamunda.se", new ZamundaSe(DownloaderPassClass.getDownloader()));
      PluginFactory.registerPlugin("img.zamunda.se", new ZamundaSe(DownloaderPassClass.getDownloader()));
   }
   
   private CFile flZelkasubs = null;
//                               flSubtitrite = null;

   public ZamundaSe()
   {
      super();
   }
   
   public ZamundaSe(IDownloader downloader)
   {
      super(downloader);
   }
   
   @Override
   protected String inBackgroundHttpParse(String sURL) throws Exception
   {
      sTitle = super.inBackgroundHttpParse(sURL);

      sZelkasubs = "";
      
      // TODO Optimize it
      String sResponse = getURLResponse(sURL).replace("\n", "");

      if(zamundaSeSettings.bDownloadSubtitles)
      {
         sZelkasubs = getZelkaSubs(sResponse);
      }

      sTitle.replace(":", " -").replace("*", "-").replace("?", "").trim();
      return sTitle;
   }   

   protected String getZelkaSubs(String sResponse)
   {
      String sZelkaSubs = "";
      Matcher matcher = ptnZelkasubs.matcher(sResponse);
      if(matcher.find())
         sZelkaSubs = matcher.group(2);
      return sZelkaSubs;
   }

   protected String getTorrentUrl(String sResponse)
   {
      String sTorrentUrl = "";
      Matcher matcher = ptnTorrent.matcher(sResponse);
      if(matcher.find())
         sTorrentUrl = HTTP + DOMAIN + "/" + matcher.group();
      return sTorrentUrl;
   }
   
   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      ArrayList<CFile> alFilesFound = super.doneHttpParse(sResult);
      
      addZelkaSubsFile(alFilesFound);

      return alFilesFound;
   }

   protected void addZelkaSubsFile(ArrayList<CFile> vFilesFnd)
   {
      if(sZelkasubs != null && !sZelkasubs.isEmpty())
      {
         String sExtension =  sZelkasubs.substring(sZelkasubs.lastIndexOf(".")+1);
         flZelkasubs = new CFile(sFolderName + File.separator + sFilesName + "." + sExtension, sZelkasubs);
         vFilesFnd.add(flZelkasubs);
      }
   }
   
   @Override
   protected String getFilesName()
   {
      String sFilesName;
      Matcher matcher = ptnTitleParts.matcher(sTitle);
      if(matcher.find())
      {
         sFilesName = matcher.group(1) + " " + matcher.group(3);   
         sFilesName = sFilesName.trim().replace("&quot;", "");
      }
      else
         sFilesName = sTitle;      
      return sFilesName;
   }   

   @Override
   protected Pattern getUrlPattern()
   {
      return ptnURL;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      return ptnTorrent;
   }

   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }

   @Override
   protected String getTorrentUrl(String sURL, String sResponse)
   {
      String sTorrentUrl = "";
      Matcher matcher = ptnTorrent.matcher(sResponse);
      if(matcher.find())
         sTorrentUrl = HTTP + DOMAIN + "/" + matcher.group();
      return sTorrentUrl;
   }

   @Override
   protected String getMoviesSettingsFile()
   {
      return SETTINGS_FILE;
   }

   @Override
   protected String getUser()
   {
      return null;
   }

   @Override
   protected String getPassword()
   {
      return null;
   }

   @Override
   protected MovieSettings getMovieSettings()
   {
      return zamundaSeSettings;
   }

   @Override
   protected void setMoviesSettings(MovieSettings movieSettings)
   {
      zamundaSeSettings = movieSettings;
   }

   @Override
   protected Pattern getTorrentUrlPattern()
   {
      return ptnTorrent;
   }

   @Override
   protected Pattern getMagnetPattern()
   {
      return ptnMagnet;
   }

   @Override
   protected Pattern getImageUrlPattern()
   {
      return ptnImage;
   }

   @Override
   protected Pattern getDescriptionPattern()
   {
      return ptnDescription;
   }

   @Override
   protected String getLoginUrl()
   {
      return HTTP + WWW + DOMAIN + "/takelogin.php";
   }

   @Override
   protected String getLoginUrlParamtres()
   {
      return String.format("username=%s&password=%s", zamundaSeSettings.sUser, zamundaSeSettings.sPassword);
   }

   @Override
   protected String getReferer()
   {
      return HTTP + DOMAIN;
   }

   @Override
   public String getDomain()
   {
      return DOMAIN;
   }

   @Override
   public boolean isForCheck()
   {
      return false;
   }
}
