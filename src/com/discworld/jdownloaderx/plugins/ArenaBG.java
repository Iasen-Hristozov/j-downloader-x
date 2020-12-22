package com.discworld.jdownloaderx.plugins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.MoviePlugin;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;

public class ArenaBG extends MoviePlugin
{
   public final static String DOMAIN_CH = "arenabg.ch",
                              DOMAIN = "arenabg.com",
                              SETTINGS_FILE = "arena_bg.xml", 
                              USER = "Rincewind123",
                              PASSWORD = "suleiman";
   
   private final static Pattern ptnUrlMovie = Pattern.compile("(http(s)?:\\/\\/?(www\\.)?arenabg\\.(ch|com)\\/bg\\/torrents\\/[\\w\\d\\-]+?\\/)"),
                                ptnTorrent = Pattern.compile("\\\"(?<" + GRP_TORRENT + ">\\/bg\\/torrents\\/(download|get)\\/\\?key=.+?)\\\""),
                                ptnTitle = Pattern.compile("<h1>(?<" + GRP_TITLE + ">.+?)<\\/h1>"),
                                ptnMagnet = Pattern.compile("\\\"(?<" + GRP_MAGNET + ">magnet:\\?xt=urn:btih:.*?)\\\""),
                                ptnImage = Pattern.compile("(?<" + GRP_IMAGE + ">http(s)?:\\/\\/cdn\\.arenabg\\.(com|ch)\\/var\\/torrents(\\/[\\d\\-]+)?\\/(\\w+)\\/.+?\\.jpg)"),
                                ptnDescription = Pattern.compile("<div class=\\\"card-body border-left border-right border-bottom p-3\\\">(?<" + GRP_DESCRIPTION + ">[\\S\\s]+?)<\\/div>"),
                                ptnProtocolDomain = Pattern.compile("(http(s)?://)?(www\\.)?arenabg\\.(ch|com)");
      
   public MovieSettings arenaBGSettings;

   public CFile  flImage = null;

   static
   {
      PluginFactory.registerPlugin(DOMAIN_CH, new ArenaBG(DownloaderPassClass.getDownloader()));
      PluginFactory.registerPlugin("cdn." + DOMAIN_CH, new ArenaBG(DownloaderPassClass.getDownloader()));
      PluginFactory.registerPlugin(DOMAIN, new ArenaBG(DownloaderPassClass.getDownloader()));
      PluginFactory.registerPlugin("cdn." + DOMAIN, new ArenaBG(DownloaderPassClass.getDownloader()));
   }
   
   public ArenaBG()
   {
      super();
   }
   
   public ArenaBG(IDownloader downloader)
   {
      super(downloader);
   }

   @Override
   protected Pattern getUrlPattern()
   {
      return ptnUrlMovie;
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }
   
   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      return ptnTorrent;
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

   protected String getTorrentUrl(String sURL, String sResponse)
   {
      String sTorrentUrl = "";
      Matcher matcher = ptnProtocolDomain.matcher(sURL);
      String sProtocolDomain = "";
      if(matcher.find())
         sProtocolDomain = matcher.group();
      else
         sProtocolDomain = HTTP + DOMAIN_CH; 
      matcher = ptnTorrent.matcher(sResponse);
      if(matcher.find())
         sTorrentUrl = sProtocolDomain + matcher.group(GRP_TORRENT);
      return sTorrentUrl;
   }

   @Override
   protected String getMoviesSettingsFile()
   {
      return SETTINGS_FILE;
   }

   @Override
   protected MovieSettings getMovieSettings()
   {
      return arenaBGSettings;
   }

   @Override
   protected void setMoviesSettings(MovieSettings movieSettings)
   {
      arenaBGSettings = movieSettings;
   }
   
   @Override
   protected String getUser()
   {
      return USER;
   }

   @Override
   protected String getPassword()
   {
      return PASSWORD;
   }

   @Override
   public String getDomain()
   {
      return DOMAIN_CH;
   }
   
   @Override
   protected String getLoginUrl()
   {
      return HTTPS + WWW + DOMAIN_CH + "users/login/";
   }

   @Override
   protected String getLoginUrlParamtres()
   {
      return String.format("username=%s&password=%s", arenaBGSettings.sUser, arenaBGSettings.sPassword);
   }

   @Override
   protected String getReferer()
   {
      return HTTPS + WWW + DOMAIN_CH;
   }

   @Override
   protected String getFilesName()
   {
      return sTitle;
   }

   @Override
   public boolean isForCheck()
   {
      return false;
   }

}
