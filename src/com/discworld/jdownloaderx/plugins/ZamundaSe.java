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
//                               COOKIE_UID_NAME = "uid",
//                               COOKIE_PASS_NAME = "pass",
                               SETTINGS_FILE = "zamunda_se.xml";
//                               MAGNET_FILE = "magnet.txt",
//                               INFO_FILE = "info.txt";

   private final static Pattern ptnURL = Pattern.compile("((http://)?zelka\\.org/details\\.php\\?id=(\\d)*)"),
                                ptnTitle = Pattern.compile("(<h1>)(?<" + GRP_TITLE + ">.+)(<[\\s]*/h1>)"),
                                ptnTitleParts = Pattern.compile("(.*?)( / .*?)* (\\(\\d+(\\-\\d+)?\\))"),
                                ptnTorrent = Pattern.compile("(download.php/\\S+\\.(torrent?))"),
                                ptnMagnet = Pattern.compile("magnet:\\?xt=urn:btih:[\\w]*"),
                                ptnImage = Pattern.compile("<img border=\\\"0\\\" src=\\\"(?<"+GRP_IMAGE+">.+?)\\\">"),
                                ptnDescription = Pattern.compile("(\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435)(?<"+GRP_DESCRIPTION+">.*?)((\u0421\u0432\u0430\u043b\u0438 \u0421\u0443\u0431\u0442\u0438\u0442\u0440\u0438)|(NFO))"),
                                ptnZelkasubs = Pattern.compile("(<a href=)((http://)?(www\\.)?((zelka.org)|(zamunda.se))/getsubs.php/(.+?))( target=_blank)?>");
//                                ptnSubtitrite = Pattern.compile("(http://)?subtitrite.net/subs/\\d+/.*?/");

//   private String              sTitle, 
//                               sZelkasubs,
////                               sSutitrite,
//                               sFilesName,
//                               sFolderName;
   
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

//   @Override
//   protected String inBackgroundHttpParse(String sURL) throws Exception
//   {
//      sMagnet = "";
//      sTorrent = "";
//      sImage = "";
//      sDescription = "";
//      sZelkasubs = "";
//
//      if(zamundaSeSettings.sCookieUID == null || 
//         zamundaSeSettings.sCookieUID.isEmpty() || 
//         zamundaSeSettings.sCookiePass == null || 
//         zamundaSeSettings.sCookiePass.isEmpty())
//      {
//         loginZelka();
//         saveSettings();
//      }
//      
//      String sResponse = getZelka(sURL).replace("\n", "");
//
////      Matcher oMatcher = ptnTitle.matcher(sResponse);
////      if(oMatcher.find())
////         sTitle = oMatcher.group(2).trim().replace("/", "").replace(":", " -").replace("&quot;", "");
//
////      if(zamundaSeSettings.bDownloadTorrent)
////      {
////         sTorrent = getTorrentUrl(sResponse);
////      }
//      
////      if(zamundaSeSettings.bDownloadMagnet)
////      {
////         oMatcher = ptnMagnet.matcher(sResponse);
////         if(oMatcher.find())
////            sMagnet = oMatcher.group();
////      }
//
////      if(zamundaSeSettings.bDownloadImage)
////      {
////         oMatcher = ptnImage.matcher(sResponse);
////         if(oMatcher.find())
////            sImage = oMatcher.group(1);
////      }
//
////      if(zamundaSeSettings.bDownloadDescription)
////      {
////         oMatcher = ptnDescription.matcher(sResponse);
////         if(oMatcher.find())
////         {
////            sDescription = oMatcher.group(2);
////            sDescription = sDescription.replace("<br />", "\n").replace("&nbsp;", " ").replaceAll("<.*?>", "").replaceAll("\n\n", "\n").trim();
////         }
////      }
//
//      if(zamundaSeSettings.bDownloadSubtitles)
//      {
//         sZelkasubs = getZelkaSubs(sResponse);
////         oMatcher = ptnSubtitrite.matcher(sResponse);
////         if(oMatcher.find())
////            sSutitrite = oMatcher.group();
//      }
//      
//      downloader.checkContetsVsPlugins(sTitle, sResponse);
//
//      return sTitle;
//   }
   
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

//   @Override
//   protected ArrayList<CFile> doneHttpParse(String sResult)
//   {
//      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
//      Matcher oMatcher = ptnTitleParts.matcher(sTitle);
//      if(oMatcher.find())
//      {
//         sFilesName = oMatcher.group(1) + " " + oMatcher.group(3);   
//         sFilesName = sFilesName.trim().replace("&quot;", "");
//      }
//      else
//         sFilesName = sTitle;
//      
//      sFolderName = sTitle;
//      
//      String sTorrentName = sTorrent.substring(sTorrent.lastIndexOf("/") + 1);
//      oMovieTorrent = new Movie(sFolderName + File.separator + sTorrentName, sTorrent, sMagnet, sDescription);
//      vFilesFnd.add(oMovieTorrent);
//      
//      if(sImage != null && !sImage.isEmpty())
//      {
//         String sExtension =  sImage.substring(sImage.lastIndexOf(".")+1);
//         flImage = new CFile(sFolderName + File.separator + sFolderName + "." + sExtension, sImage);
//         vFilesFnd.add(flImage);
//      }
//   
//      addZelkaSubsFile(vFilesFnd);
//      
////      if(sSutitrite != null && !sSutitrite.isEmpty())
////      {
////         flSubtitrite = new CFile(sFolderName + File.separator, sSutitrite);
////         vFilesFnd.add(flSubtitrite);
////      }
//   
//      return vFilesFnd;
//   }

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

//   @Override
//   public void downloadFile(CFile oFile, String sDownloadFolder)
//   {
//      ArrayList<SHttpProperty> alHttpProperties = null;
//      alHttpProperties = new ArrayList<SHttpProperty>();
//      String sCookies = COOKIE_UID_NAME + "=" + zamundaSeSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + zamundaSeSettings.sCookiePass;
//      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
//      new DownloadFileThread(oFile, sDownloadFolder, alHttpProperties).execute();
//   }

//   @Override
//   protected void downloadFileDone(CFile file, String sDownloadFolder, String saveFilePath)
//   {
//      downloader.deleteFileFromLists(file);
//
//      downloader.saveFilesList();
//      
//      FileUtils.renameFile(saveFilePath, sDownloadFolder + File.separator + file.getName());
//      if(file instanceof Movie)
//      {
//         try
//         {
//            Movie oMovie = (Movie) file;
//            sFolderName = oMovie.getName().substring(0, oMovie.getName().lastIndexOf(File.separator));
//            File f;
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
//         } 
//         catch(IOException e)
//         {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//         }
//      }
//   }

//   @Override
//   protected void loadSettings()
//   {
//      try
//      {
//         JAXBContext jaxbContext = JAXBContext.newInstance(ZamundaSeSettings.class);
//         
//         File file = new File(SETTINGS_FILE);
//         if(file.exists())
//         {
//            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//            zamundaSeSettings = (MovieSettings)jaxbUnmarshaller.unmarshal(file);
//         }
//         else
//         {
//            zamundaSeSettings = new MovieSettings();
//            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
//            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            jaxbMarshaller.marshal(zamundaSeSettings, file);
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
//         jaxbContext = JAXBContext.newInstance(ZamundaSeSettings.class);
//         File file = new File(SETTINGS_FILE);
//         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
//         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//         jaxbMarshaller.marshal(zamundaSeSettings, file);
//         
//      } catch(JAXBException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//   }
//   
//   private void loginZelka()
//   {
//      String urlParameters  = String.format("username=%s&password=%s", zamundaSeSettings.sUser, zamundaSeSettings.sPassword);
//      String request        = HTTP + WWW + DOMAIN + "/takelogin.php";
//      URL url;
//      BufferedReader in;
//      try
//      {
//         url = new URL( request );
//         HttpURLConnection conn= (HttpURLConnection) url.openConnection();
//         
//         conn.setRequestMethod("POST");
//         conn.setDoOutput(true);
//         conn.setUseCaches(false);
//         conn.setInstanceFollowRedirects(false);
//         conn.setRequestProperty("Host", "zelka.org");
//         conn.setRequestProperty("User-Agent", "Mozilla/5.0");
//         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//         conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//         conn.setRequestProperty("Referer", HTTP + DOMAIN);
//         conn.setRequestProperty("Connection", "keep-alive");
//         conn.setDoInput(true);
//
//         // Send post request
//         conn.setRequestProperty("Content-Length", Integer.toString(urlParameters.length()));
//         conn.connect();
//
//         DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
//         wr.writeBytes(urlParameters);
//         wr.flush();
//         wr.close();
//         
//         if(conn.getResponseCode() == 302)
//         {
//            List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
//            if (cookies != null) 
//            {
//               for(String cookie : cookies)
//               {
//                  cookie = cookie.substring(0, cookie.indexOf(";"));
//                  String cookieName = cookie.substring(0, cookie.indexOf("="));
//                  String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
//                  if(cookieName.equals(COOKIE_UID_NAME))
//                     zamundaSeSettings.sCookieUID = cookieValue;
//                  else if(cookieName.equals(COOKIE_PASS_NAME))
//                     zamundaSeSettings.sCookiePass = cookieValue;
//               }
//            }
//         }
//         if(conn.getResponseCode() == 200)
//         {
//            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//
//            String inputLine;
//            StringBuffer sbResponse = new StringBuffer();
//
//            while((inputLine = in.readLine()) != null)
//               sbResponse.append(inputLine + "\n");
//            in.close();
//         }
//      } 
//      catch(MalformedURLException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      } 
//      catch(ProtocolException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      } 
//      catch(IOException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//   }
   
//   private String getZelka(String sURL) throws Exception
//   {
//      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
//      String sCookies = COOKIE_UID_NAME + "=" + zamundaSeSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + zamundaSeSettings.sCookiePass;
//      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
//      
//      return getHttpResponse(sURL, alHttpProperties);
//   }
   
//   @Override
//   public boolean isMine(String sURL)
//   {
//      for(String sDomain : DOMAINS)
//         if(sURL.contains(sDomain))
//            return true;
//      return false;
//   }

//   @XmlAccessorType(XmlAccessType.FIELD)
//   @XmlType(name = "", propOrder = {"bDownloadTorrent","bDownloadMagnet","bDownloadImage","bDownloadDescription","bDownloadSubtitles", "sUser","sPassword","sCookieUID","sCookiePass"})
//   @XmlRootElement(name = "settings")
//   static private class ZamundaSeSettings
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
}
