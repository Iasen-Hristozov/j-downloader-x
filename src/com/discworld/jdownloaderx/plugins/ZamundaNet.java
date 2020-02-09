package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.MoviePlugin;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class ZamundaNet extends MoviePlugin
{
   
   public final static String DOMAIN = "www.zamunda.net",
                              SETTINGS_FILE = "zamunda_net.xml",
                              USER = "Rincewind123",
                              PASSWORD = "suleiman";

   private final static Pattern ptnURL = Pattern.compile("((http(s?):\\/\\/)?(www.)?zamunda\\.net/banan\\?id=\\d+)"),
                                ptnTitle = Pattern.compile("<h1(.*?)>(?<"+GRP_TITLE+">.*?)<\\/.*?>"),
                                ptnTitleParts = Pattern.compile("^(.+?)(\\/.+?)*(\\(\\d+(\\-\\d+)?\\))?([ ]?\\[.+?\\])?$"),
                                ptnTorrent = Pattern.compile("/download_go\\.php\\?id=(\\d+)\"[\\s]*>(.+?)</a>"),
                                ptnMagnetLink = Pattern.compile("/magnetlink/download_go\\.php\\?id=\\d+&m=x"),
                                ptnMagnet = Pattern.compile("magnet:\\?xt=urn:btih:[\\w]*"),
                                ptnImage = Pattern.compile("((http(s?):\\/\\/)?(img(\\d)?.)?zamunda.net\\/(pic\\/)?(img(\\d)?\\/)?bitbucket\\/([\\d]+\\/)?(.+?))\\\""),
                                ptnImage1 = Pattern.compile("img border=(\\\")?0(\\\")? src=\\\"((http:\\/\\/)?i.imgur.com\\/(.+?))\\\""),
                                ptnDescription = Pattern.compile("<div id=description>(<br \\/><br>)?<div align=center>(?<"+GRP_DESCRIPTION+">[\\S\\s]+?)<div align=center"),
                                ptnZamundaSubs = Pattern.compile("((http(s?):\\/\\/)?(www\\.)?zamunda\\.net\\/getsubs\\.php\\/([\\w\\-\\.]+))");
   
   public MovieSettings zamundaNetSettings;

   private String sZamundaSubs;
   
   private CFile flZamundaSubs = null;

   static 
   {
      PluginFactory.registerPlugin(DOMAIN, new ZamundaNet(DownloaderPassClass.getDownloader()));
      PluginFactory.registerPlugin("zamunda.net", new ZamundaNet(DownloaderPassClass.getDownloader()));
      PluginFactory.registerPlugin("img3.zamunda.net", new ZamundaNet(DownloaderPassClass.getDownloader()));
   }
   
   public ZamundaNet()
   {
      super();
   }
   
   public ZamundaNet(IDownloader downloader)
   {
      super(downloader);
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      for(String sDomain : DOMAINS)
//         if(sURL.contains(sDomain))
//            return true;
//      return false;      
//   }

   @Override
   protected String inBackgroundHttpParse(String sURL) throws Exception
   {
      sTitle = super.inBackgroundHttpParse(sURL);

      sZamundaSubs = "";
      
      // TODO Optimize it
      String sResponse = getURLResponse(sURL).replace("\n", "");

      if(zamundaNetSettings.bDownloadSubtitles)
      {
         sZamundaSubs = getZamundaSubs(sResponse);
      }

      sTitle.replace(":", " -").replace("*", "-").replace("?", "").trim();
      return sTitle;
   }

   @Override
   protected String getLoginUrl()
   {
      return HTTPS + DOMAIN + "/takelogin.php";
   }

   @Override
   protected String getLoginUrlParamtres()
   {
      return String.format("username=%s&password=%s", zamundaNetSettings.sUser, zamundaNetSettings.sPassword);
   }

   protected String getZamundaSubs(String sResponse)
   {
      String sZamundaSubs = "";
      Matcher matcher = ptnZamundaSubs.matcher(sResponse);
      if(matcher.find())
         sZamundaSubs = matcher.group();
      return sZamundaSubs;
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      ArrayList<CFile> alFilesFound = super.doneHttpParse(sResult);
      
      addZamundaSubsFile(alFilesFound);

      return alFilesFound;
   }

   @Override
   protected String getFilesName()
   {
      String sFilesName;
      Matcher oMatcher = ptnTitleParts.matcher(sTitle);
      if(oMatcher.find())
      {
         sFilesName = oMatcher.group(1);
         if(oMatcher.group(3) != null)
            sFilesName += " " + oMatcher.group(3); 
         sFilesName = sFilesName.trim();
      }
      else
         sFilesName = sTitle;
      return sFilesName;
   }

   protected void addZamundaSubsFile(ArrayList<CFile> vFilesFnd)
   {
      if(sZamundaSubs != null && !sZamundaSubs.isEmpty())
      {
         String sExtension =  sZamundaSubs.substring(sZamundaSubs.lastIndexOf(".")+1);
         flZamundaSubs = new CFile(sFolderName + File.separator + sFilesName + "." + sExtension, sZamundaSubs);
         vFilesFnd.add(flZamundaSubs);
      }
   }

   // final static Pattern ptnUri = Pattern.compile("^([\\w\\d\\.\\-\\?]*(\\%([A-F\\d]{2}))*[\\w\\d\\.\\-\\?]*)*$");
   final static Pattern ptnUri = Pattern.compile("(?!\\%[A-F\\d]{2})([^\\w\\-\\.\\?\\(\\)]+)");
          
   protected static boolean isValidURI(String uri)
   {
      Matcher oMatcher = ptnUri.matcher(uri);
      return !oMatcher.find();
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
   protected Pattern getTorrentUrlPattern()
   {
      return ptnTorrent;
   }

   @Override
   protected String getTorrentUrl(String sResponse) 
   {
      String sTorrent = ""; 
      Matcher matcher = ptnTorrent.matcher(sResponse);
      if(matcher.find())
      {
         try
         {
            String url = URLEncoder.encode(matcher.group(2), "UTF-8");
            sTorrent = HTTPS + DOMAIN + "/download.php/" + matcher.group(1) + "/" + url + ".torrent";
         }
         catch(UnsupportedEncodingException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      return sTorrent;
   }

   @Override
   protected String getTorrentUrl(String sURL, String sResponse)
   {
      String sTorrent = "";
      Matcher matcher = ptnTorrent.matcher(sResponse);
      try
      {
         if(matcher.find())
         {
            String url = URLEncoder.encode(matcher.group(2), "UTF-8");
            
            sTorrent = HTTPS + DOMAIN + "/download.php/" + matcher.group(1) + "/" + url + ".torrent";
         }
      } 
      catch(UnsupportedEncodingException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return sTorrent;
   }

   @Override
   protected Pattern getMagnetPattern()
   {
      return ptnMagnet;
   }

   @Override
   protected String getMagnet(String sResponse)
   {
      String sMagnet = "";
      Matcher matcher = ptnMagnetLink.matcher(sResponse);
      if(matcher.find())
      {
         sMagnet = matcher.group();
         
         ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
         String sCookies = COOKIE_UID_NAME + "=" + zamundaNetSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + zamundaNetSettings.sCookiePass;
         alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
         
         try
         {
            String sMagnetResult = getHttpResponse(HTTPS + DOMAIN + sMagnet, alHttpProperties);
            matcher = ptnMagnet.matcher(sMagnetResult);
            if(matcher.find())
               sMagnet = matcher.group();
         }
         catch(Exception e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      return sMagnet;
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
   protected String getImageUrl(String sResponse)
   {
      String sImage ="";
      Matcher matcher = ptnImage.matcher(sResponse);
      if(matcher.find())
      {
         try
         {
            String sImageTmp = matcher.group(1);
            
            if(!isValidURI(sImageTmp))
               sImageTmp = URLEncoder.encode(sImageTmp, "UTF-8");
              
            sImage = matcher.group(1);
         } 
         catch(UnsupportedEncodingException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      
      matcher = ptnImage1.matcher(sResponse);
      if(matcher.find())
         sImage= matcher.group(3);
      
      return sImage;
   }

   @Override
   protected String getDescription(String sResponse)
   {
      String sDescription = super.getDescription(sResponse).replaceAll("<br[\\s]*/>", "\n")
                                                           .replace("&nbsp;", " ")
                                                           .replaceAll("<.*?>", "")
                                                           .replaceAll("\n\n", "\n").trim();
      return sDescription;
   }

   @Override
   protected String getMoviesSettingsFile()
   {
      return SETTINGS_FILE;
   }

   @Override
   protected MovieSettings getMovieSettings()
   {
      return zamundaNetSettings;
   }

   @Override
   protected void setMoviesSettings(MovieSettings movieSettings)
   {
      zamundaNetSettings = movieSettings;
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
   protected String getReferer()
   {
      return HTTPS + DOMAIN;
   }

   @Override
   public boolean isForCheck()
   {
      return false;
   }
}
