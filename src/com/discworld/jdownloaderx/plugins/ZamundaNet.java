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
   
   public final static String DOMAIN = "www.zamunda.net";

   private static final String //                               COOKIE_UID_NAME = "uid",
//                               COOKIE_PASS_NAME = "pass",
    SETTINGS_FILE = "zamunda_net.xml";

   private static final String //                               MAGNET_FILE = "magnet.txt",
//                               INFO_FILE = "info.txt",
    USER = "Rincewind123";

   private static final String PASSWORD = "suleiman";

   private final static Pattern ptnURL = Pattern.compile("((http(s?)://)?(www.)?zamunda\\.net/banan\\?id=\\d+)"),
                                ptnTitle = Pattern.compile("<h1(.*?)>(?<"+GRP_TITLE+">.*?)<\\/.*?>"),
                                ptnTitleParts = Pattern.compile("^(.+?)(\\/.+?)*(\\(\\d+(\\-\\d+)?\\))?([ ]?\\[.+?\\])?$"),
                                ptnTorrent = Pattern.compile("/download_go\\.php\\?id=(\\d+)\"[\\s]*>(.+?)</a>"),
                                ptnMagnetLink = Pattern.compile("/magnetlink/download_go\\.php\\?id=\\d+&m=x"),
                                ptnMagnet = Pattern.compile("magnet:\\?xt=urn:btih:[\\w]*"),
                                ptnImage = Pattern.compile("img border=(\\\")?0(\\\")? src=\\\"(((http(s?):\\/\\/)?(img(\\d)?.)?zamunda.net\\/(pic\\/)?(img(\\d)\\/)?)bitbucket\\/([\\d]+\\/)?(.+?))\\\""),
                                ptnImage1 = Pattern.compile("img border=(\\\")?0(\\\")? src=\\\"((http:\\/\\/)?i.imgur.com\\/(.+?))\\\""),
                                ptnDescription = Pattern.compile("<div id=description>(<br \\/><br>)?<div align=center>(?<"+GRP_DESCRIPTION+">[\\S\\s]+?)<div align=center"),
                                ptnZamundaSubs = Pattern.compile("((http(s?):\\/\\/)?(www\\.)?zamunda\\.net\\/getsubs\\.php\\/([\\w\\-\\.]+))");
   
   public MovieSettings zamundaNetSettings;

   private String sZamundaSubs;
//                  sFilesName;
//                  sTitle, 
//                  sMagnet,
//                  sTorrent,
//                  sImage,
//                  sDescription;
//                               sFolderName;


   
//   private Movie        oMovieTorrent = null;
   
   private CFile               flZamundaSubs = null;
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
//      sMagnet = "";
//      sTorrent = "";
//      sImage = "";
//      sDescription = "";
//      sZamundaSubs = "";
//   
//      if(zamundaNetSettings.sCookieUID == null || 
//         zamundaNetSettings.sCookieUID.isEmpty() || 
//         zamundaNetSettings.sCookiePass == null || 
//         zamundaNetSettings.sCookiePass.isEmpty())
//      {
//         login();
//         saveSettings();
//      }
//
//      String sResponse = getURLResponse(sURL).replace("\n", "");
//
//      
//      sTitle = getTitle(sResponse);
////      Matcher oMatcher = ptnTitle.matcher(sResponse);
////      if(oMatcher.find())
////      {
////         sTitle = oMatcher.group(2);
////         sTitle = sTitle.replace(":", " -").replace("*", "-").replace("?", "").trim();
////      }
//
//      if(zamundaNetSettings.bDownloadTorrent)
//      {
//         sTorrent = getTorrentUrl(sResponse);
//      }
//      
//      if(zamundaNetSettings.bDownloadMagnet)
//      {
//         sMagnet = getMagnet(sResponse);
//      }
//
//      if(zamundaNetSettings.bDownloadImage)
//      {
//         sImage = getImageUrl(sResponse);
//      }
//
//      if(zamundaNetSettings.bDownloadDescription)
//      {
//         sDescription = getDescription(sResponse);
//      }
//
//      if(zamundaNetSettings.bDownloadSubtitles)
//      {
//         sZamundaSubs = getZamundaSubs(sResponse);
//
//         downloader.checkContetsVsPlugins(sTitle.replace("/", "").trim(), sResponse);
//      }

      
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

   protected String getZamundaSubs(String sResponse)
   {
      String sZamundaSubs = "";
      Matcher matcher = ptnZamundaSubs.matcher(sResponse);
      if(matcher.find())
         sZamundaSubs = matcher.group();
      return sZamundaSubs;
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
   protected String getImageUrl(String sResponse)
   {
      String sImage ="";
      Matcher matcher = ptnImage.matcher(sResponse);
      if(matcher.find())
      {
         try
         {
            String sImageTmp = matcher.group(3);
            
            if(!isValidURI(sImageTmp))
               sImageTmp = URLEncoder.encode(sImageTmp, "UTF-8");
              
            sImage = matcher.group(3);
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
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
//      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
//      sFilesName = getFilesName();
//      
//      sFolderName = sTitle.replace("/", "").trim();
//      String sTorrentName = sTorrent.substring(sTorrent.lastIndexOf("/")+1);
//      oMovieTorrent = new Movie(sFolderName + File.separator + sTorrentName, sTorrent, sMagnet, sDescription);
//      vFilesFnd.add(oMovieTorrent);
//      
//      if(sImage != null && !sImage.isEmpty())
//      {
//         String sExtension =  sImage.substring(sImage.lastIndexOf(".")+1);
//         flImage = new CFile(sFolderName + File.separator + sFilesName + "." + sExtension, sImage);
//         vFilesFnd.add(flImage);
//      }
      
      ArrayList<CFile> alFilesFound = super.doneHttpParse(sResult);
      
      addZamundaSubsFile(alFilesFound);

      return alFilesFound;
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

//   @Override
//   public void downloadFile(CFile oFile, String sDownloadFolder)
//   {
//      ArrayList<SHttpProperty> alHttpProperties = null;
//
//      alHttpProperties = new ArrayList<SHttpProperty>();
//      String sCookies = COOKIE_UID_NAME + "=" + zamundaNetSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + zamundaNetSettings.sCookiePass;
//      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
//      
//      new DownloadFileThread(oFile, sDownloadFolder, alHttpProperties).execute();
//   }
//
//   @Override
//   protected void downloadFileDone(CFile file, String sDownloadFolder, String saveFilePath)
//   {
//      downloader.deleteFileFromLists(file);
//
//      downloader.saveFilesList();
//      
//      try
//      {
//         File f;
//
//         if(file instanceof Movie)
//         {
//            Movie oMovie = (Movie) file;
//            sFolderName = oMovie.getName().substring(0, oMovie.getName().lastIndexOf(File.separator));
//            
//            if(oMovie.getName().endsWith(File.separator))
//               f = new File(sDownloadFolder + File.separator + file.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator) + 1));
//            else
//               f = new File(sDownloadFolder + File.separator + file.getName());
//            f.getParentFile().mkdirs();
//            File source = new File(saveFilePath);
//            Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
//            FileOutputStream fos; 
//            
//            if(oMovie.getMagnet() != null && !oMovie.getMagnet().isEmpty())
//            {
//               f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + MAGNET_FILE);
//               f.createNewFile();
//               fos = new FileOutputStream(f);
//               fos.write(oMovie.getMagnet().getBytes());
//               fos.close();
//            }
//
//            if(oMovie.getInfo() != null && !oMovie.getInfo().isEmpty())
//            {
//               f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + INFO_FILE);
//               f.createNewFile();
//               fos = new FileOutputStream(f);
//               fos.write(oMovie.getInfo().getBytes());
//               fos.close();
//            }
//
//         } 
//         else
//         {
//            if(file.getName().endsWith(File.separator))
//               f = new File(sDownloadFolder + File.separator + file.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator) + 1));
//            else
//               f = new File(sDownloadFolder + File.separator + file.getName());
//            f.getParentFile().mkdirs();
//            File source = new File(saveFilePath);
//            Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
//         }
//      } 
//      catch(IOException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//
//   }

//   @Override
//   protected void loadSettings()
//   {
//      try
//      {
//         JAXBContext jaxbContext = JAXBContext.newInstance(MovieSettings.class);
//         
//         File file = new File(SETTINGS_FILE);
//         if(file.exists())
//         {
//            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//            zamundaNetSettings = (MovieSettings)jaxbUnmarshaller.unmarshal(file);
//         }
//         else
//         {
//            zamundaNetSettings = new MovieSettings();
//            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
//            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            jaxbMarshaller.marshal(zamundaNetSettings, file);
//         }
//      } 
//      catch(JAXBException e1)
//      {
//         // TODO Auto-generated catch block
//         e1.printStackTrace();
//      }         
//   }
   
//   private void saveSettings()
//   {
//      JAXBContext jaxbContext;
//      try
//      {
//         jaxbContext = JAXBContext.newInstance(MovieSettings.class);
//         File file = new File(SETTINGS_FILE);
//         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
//         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//         jaxbMarshaller.marshal(zamundaNetSettings, file);
//         
//      } catch(JAXBException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//   }
   
   
   
//   private String getZamunda(String sURL) throws Exception
//   {
//      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
//      String sCookies = COOKIE_UID_NAME + "=" + zamundaNetSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + zamundaNetSettings.sCookiePass;
//      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
//      
//      return getHttpResponse(sURL, alHttpProperties);
//   }

//   @XmlAccessorType(XmlAccessType.FIELD)
//   @XmlType(name = "", propOrder = {"bDownloadTorrent","bDownloadMagnet","bDownloadImage","bDownloadDescription","bDownloadSubtitles", "sUser","sPassword","sCookieUID","sCookiePass"})
//   @XmlRootElement(name = "settings")
//   static private class ZamundaNetSettings
//   {
//      @XmlElement(name = "download_torrent", required = true)
//      public boolean bDownloadTorrent = true;
//      @XmlElement(name = "download_magnet", required = true)
//      public boolean bDownloadMagnet = true;
//      @XmlElement(name = "download_image", required = true)
//      public boolean bDownloadImage = true;
//      @XmlElement(name = "download_description", required = true)
//      public boolean bDownloadDescription = true;
//      @XmlElement(name = "download_subtitles", required = true)
//      public boolean bDownloadSubtitles = true;
//      @XmlElement(name = "user", required = true)
//      public String sUser = "Rincewind123";
//      @XmlElement(name = "password", required = true)
//      public String sPassword = "suleiman";
//      @XmlElement(name = "cookie_uid", required = true)
//      public String sCookieUID;
//      @XmlElement(name = "cookie_pass", required = true)
//      public String sCookiePass;
//   }

   
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
   public String getDomain()
   {
      return DOMAIN;
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

   @Override
   protected String getReferer()
   {
      return HTTPS + DOMAIN;
   }
}
