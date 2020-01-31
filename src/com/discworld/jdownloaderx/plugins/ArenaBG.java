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
   public final static String DOMAIN = "arenabg.com",
                              SETTINGS_FILE = "arena_bg.xml", 
                              USER = "Rincewind123",
                              PASSWORD = "suleiman";

   private final static Pattern ptnUrlMovie = Pattern.compile("((http(s)?://)?(www\\.)?arenabg.com/[\\w\\d\\-]+?/)"),
                                ptnTorrent = Pattern.compile("/(download|get)/key:.+?/"),
                                ptnTitle = Pattern.compile("<title>(?<"+GRP_TITLE+">.+?) (\\.\\.\\. )?\u0441\u0432\u0430\u043b\u044f\u043d\u0435</title>"),
                                ptnMagnet = Pattern.compile("magnet:\\?xt=urn:btih:[\\w]*"),
                                ptnImage = Pattern.compile("(?<"+GRP_IMAGE+">http(s)?:\\/\\/cdn.arenabg.com\\/resize\\/500\\/-\\/var\\/assets\\/posters\\/([\\d\\-]\\/)?.+?\\.jpg)"),
                                ptnDescription = Pattern.compile("<div class=\"torrent-text\">(?<"+GRP_DESCRIPTION+">.+?)</div>"),
                                ptnProtocolDomain = Pattern.compile("(http(s)?://)?(www\\.)?arenabg.com");
      
   public MovieSettings àrenaBGSettings;

   public CFile  flImage = null;

   static
   {
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
      String sTorrent = "";
      Matcher matcher = ptnProtocolDomain.matcher(sURL);
      String sProtocolDomain = "";
      if(matcher.find())
         sProtocolDomain = matcher.group();
      else
         sProtocolDomain = HTTP + DOMAIN; 
      matcher = ptnTorrent.matcher(sResponse);
      if(matcher.find())
         sTorrent = sProtocolDomain + matcher.group();
      return sTorrent;
   }

   @Override
   protected String getMoviesSettingsFile()
   {
      return SETTINGS_FILE;
   }

   @Override
   protected MovieSettings getMovieSettings()
   {
      return àrenaBGSettings;
   }

   @Override
   protected void setMoviesSettings(MovieSettings movieSettings)
   {
      àrenaBGSettings = movieSettings;
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
      return DOMAIN;
   }
   
   @Override
   protected String getLoginUrl()
   {
      return HTTPS + WWW + DOMAIN + "users/login/";
   }

   @Override
   protected String getLoginUrlParamtres()
   {
      return String.format("username=%s&password=%s", àrenaBGSettings.sUser, àrenaBGSettings.sPassword);
   }

   @Override
   protected String getReferer()
   {
      return HTTPS + WWW + DOMAIN;
   }

   @Override
   protected String getFilesName()
   {
      return sTitle;
   }

}
