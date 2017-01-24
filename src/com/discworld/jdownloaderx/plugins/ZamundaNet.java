package com.discworld.jdownloaderx.plugins;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.CMovie;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class ZamundaNet extends Plugin
{
   
   private final static String DOMAIN = "zamunda.net",
                               SUBSUNACS_DOMAIN = "subsunacs.net",
                               COOKIE_UID_NAME = "uid",
                               COOKIE_PASS_NAME = "pass",
                               SETTINGS_FILE = "zamunda_net.xml",
                               MAGNET_FILE = "magnet.txt",
                               INFO_FILE = "info.txt",
                               HTTP = "http://",
                               WWW = "www.",
                               BUKVI_URL = "http://bukvi.bg",
                               EASTERN_SPIRIT_URL = "easternspirit.org";
   
//   private final static String[] DOMAINS = {DOMAIN, "subsland.com"};
   private final static String[] DOMAINS = {DOMAIN, "imgur.com"};

   private final static Pattern 
//                                ptnTitle = Pattern.compile("<h1>(.+?</h1>)"),
//                                ptnTitle = Pattern.compile("<title>(.*?)<\\/title>"),
                                ptnTitle = Pattern.compile("<h1(.*?)>(.*?)<\\/.*?>"),
//                                ptnTitle = Pattern.compile("(<h1>)(.+)(<[\\s]*/h1>)"),
//                                ptnTitleParts = Pattern.compile("(.+?)( / .+?)* (\\(\\d+(\\-\\d+)?\\))?"),
                                ptnTitleParts = Pattern.compile("^(.+?)(\\/.+?)*(\\(\\d+(\\-\\d+)?\\))?([ ]?\\[.+?\\])?$"),
                                ptnTorrent = Pattern.compile("/download_go\\.php\\?id=(\\d+)\"[\\s]*>(.+?)</a>"),
                                ptnMagnetLink = Pattern.compile("/magnetlink/download_go\\.php\\?id=\\d+&m=x"),
                                ptnMagnet = Pattern.compile("magnet:\\?xt=urn:btih:[\\w]*"),
                                ptnImage = Pattern.compile("img border=(\\\")?0(\\\")? src=\"((http:\\/\\/)?img.zamunda.net\\/bitbucket\\/(.+?))\\\""),
                                ptnImage1 = Pattern.compile("img border=(\\\")?0(\\\")? src=\\\"((http:\\/\\/)?i.imgur.com\\/(.+?))\\\""),
                                ptnDescription = Pattern.compile("(\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435)(.*?)((\u0421\u0443\u0431\u0442\u0438\u0442\u0440\u0438)|(\u0412\u0438\u0434\u0435\u043e)|(NFO))"),
                                ptnSubsunacs = Pattern.compile("((http:\\/\\/)?(www\\.)?subsunacs.net(\\/){1,2}((info\\.php\\?id=\\d+)|(get\\.php\\?id=\\d+)|(subtitles\\/.+?\\/)))"),
                                ptnSubsunacsURL = Pattern.compile("<a href=((http:\\/\\/)?(www\\.)?subsunacs\\.net\\/search\\.php.+?)( target=_blank)?>"),
                                ptnSubsunacsURLs = Pattern.compile("<a href=\"(\\/subtitles\\/[\\w\\d_\\-]+\\/)?\""),
//                                ptnSubssab = Pattern.compile("<a href=((http://)?(www\\.)?subs\\.sab\\.bz/index\\.php\\?act=download&amp;attach_id=.+?)( target=_blank)?>"),
                                ptnSubssabURL = Pattern.compile("<a href=((http:\\/\\/)?(www\\.)?subs\\.sab\\.bz\\/index\\.php\\?(s=.*?)?(&amp;)?act=search(&amp;sid=.+?)?&amp;movie=.+?)( target=_blank)?>"),
                                ptnSubssabURLs = Pattern.compile("\\\"((http:\\/\\/)?(www\\.)?subs\\.sab\\.bz\\/index\\.php\\?(s=.*?)?(&amp;)?act=download(&amp;sid=.+?)?&attach_id=.+?)\"( target=\\\"_blank\\\")?"),
                                ptnSubssab = Pattern.compile("((http:\\/\\/)?(www\\.)?subs\\.sab\\.bz/index\\.php\\?(s=.*?)?(&amp;)?act=download(&amp;sid=.+?)?&amp;attach_id=\\d+)"),
                                ptnZamundaSubs = Pattern.compile("((http:\\/\\/)?(www\\.)?zamunda\\.net\\/getsubs\\.php\\/([\\w\\-\\.]+))"),
                                ptnUrlMovie = Pattern.compile("(http://)?(www.)?zamunda\\.net/banan\\?id=\\d+"),
                                ptnSubsland = Pattern.compile("(http:\\/\\/)?(www\\.)?subsland.com\\/subtitles\\.php\\?id=\\d+"),
                                ptnSubslandFile = Pattern.compile("(http://)?subsland\\.com/downloadsubtitles/(.+?)(\\.rar)|(\\.zip)"),
                                ptnBukvi = Pattern.compile("(http:\\/\\/)?bukvi\\.bg\\/load\\/(\\d+\\/\\w+\\/)?[\\d\\-]+"),
                                ptnBukviFile = Pattern.compile("a href=(\\\'|\\\")(((http:\\/\\/)?bukvi(\\.mmcenter)?\\.bg)?\\/load\\/[\\d\\-]+)(\\\"|\\\')><button"),
                                ptnEasternSpirit = Pattern.compile("<a href=((http:\\/\\/)?(www\\.)?easternspirit\\.org\\/download\\.php\\?view\\.\\d+) target="),
                                ptnEasternSpiritFile = Pattern.compile("<a href=\\'(request\\.php\\?\\d+)\\'> <img src=\\'e107_images\\/generic\\/lite\\/download.png\\' alt=\\'\\' style=\\'border:0\\' \\/>");
   
   
   private ZamundaNetSettings oZamundaNetSettings;

   private String              sTitle, 
                               sMagnet,
                               sTorrent,
                               sImage,
                               sDescription,
                               sSubsunacs,
                               sZamundaSubs,
                               sSubssab,
                               sSubslandFile,
                               sBukvi,
                               sBukviFile,
                               sEasternSpirit,
                               sEasternSpiritFile,
                               sFilesName,
                               sFolderName;


   
   private CMovie        oMovieTorrent = null;
   
   private CFile               flImage = null,
                               flSubsunacs = null,
                               flSubssab = null,
                               flZamundaSubs = null,
                               flSubsland = null,
                               flBukvi = null,
                               flEasternSpirit = null;
   
   private ArrayList<String>   alSubsunacs = new ArrayList<String>(),
                               alSubssab = new ArrayList<String>();

   public ZamundaNet()
   {
      super();
   }
   
   public ZamundaNet(IDownloader oDownloader)
   {
      super(oDownloader);
   }

   @Override
   public boolean isMine(String sURL)
   {
      for(String sDomain : DOMAINS)
         if(sURL.contains(sDomain))
            return true;
      return false;      
   }

   @Override
   public ArrayList<String> parseContent(String sContent)
   {
      ArrayList<String> alUrlMovies = new ArrayList<String>();
   
      Matcher m = ptnUrlMovie.matcher(sContent);
      while(m.find())
      {
         String s = m.group();
         alUrlMovies.add(s);
      }
   
      return alUrlMovies;
   }

   @Override
   protected String inBackgroundHttpParse(String sURL)
   {
      sMagnet = "";
      sTorrent = "";
      sImage = "";
      sDescription = "";
      sSubsunacs = "";
      sZamundaSubs = "";
      sSubssab = "";
      sSubslandFile = "";
      sBukvi = "";
      sEasternSpirit = "";
   
      if(oZamundaNetSettings.sCookieUID == null || 
         oZamundaNetSettings.sCookieUID.isEmpty() || 
         oZamundaNetSettings.sCookiePass == null || 
         oZamundaNetSettings.sCookiePass.isEmpty())
      {
         loginZamunda();
         saveSettings();
      }

      String sResponse = getZamunda(sURL).replace("\n", "");

      Matcher oMatcher = ptnTitle.matcher(sResponse);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group(2);
         sTitle = sTitle.replace(":", " -").replace("*", "-").replace("?", "").trim();
      }

      if(oZamundaNetSettings.bDownloadTorrent)
      {
         oMatcher = ptnTorrent.matcher(sResponse);
         try
         {
            if(oMatcher.find())
            {
               String url = URLEncoder.encode(oMatcher.group(2), "UTF-8");
               
               sTorrent = HTTP + WWW + DOMAIN + "/download.php/" + oMatcher.group(1) + "/" + url + ".torrent";
            }
//            if(oMatcher.find())
//               sTorrent = HTTP + WWW + DOMAIN + "/download.php/" + oMatcher.group(1) + "/" + oMatcher.group(2) + ".torrent";
            

         } catch(UnsupportedEncodingException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      
      if(oZamundaNetSettings.bDownloadMagnet)
      {
         oMatcher = ptnMagnetLink.matcher(sResponse);
         if(oMatcher.find())
         {
            sMagnet = oMatcher.group();
            
            ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
            String sCookies = COOKIE_UID_NAME + "=" + oZamundaNetSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + oZamundaNetSettings.sCookiePass;
            alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
            
            String sMagnetResult = getHttpResponse(HTTP + WWW + DOMAIN + sMagnet, alHttpProperties);
            oMatcher = ptnMagnet.matcher(sMagnetResult);
            if(oMatcher.find())
               sMagnet = oMatcher.group();
         }
      }

      if(oZamundaNetSettings.bDownloadImage)
      {
         oMatcher = ptnImage.matcher(sResponse);
         if(oMatcher.find())
         {
            try
            {
//               sImage = oMatcher.group(5);
//               String sImageTmp = oMatcher.group(3).substring(oMatcher.group(3).lastIndexOf("/")+1);
               String sImageTmp = oMatcher.group(5);

//             sImage = oMatcher.group(3);
//               if(!sImage.contains("%"))
//               if(isUrlUtf8Encoded(sImage))
//               if(isUrlIsoEncoded(sImage))
               
                 if(!isValidURI(sImageTmp))
                    sImageTmp = URLEncoder.encode(sImageTmp, "UTF-8");
                 
               sImage = "http://img.zamunda.net/bitbucket/" + sImageTmp;
            } 
            catch(UnsupportedEncodingException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
         
         oMatcher = ptnImage1.matcher(sResponse);
         if(oMatcher.find())
            sImage= oMatcher.group(3);
      }

      if(oZamundaNetSettings.bDownloadDescription)
      {
         oMatcher = ptnDescription.matcher(sResponse);
         if(oMatcher.find())
         {
            sDescription = oMatcher.group(2);
            sDescription = sDescription.replaceAll("<br[\\s]*/>", "\n").replace("&nbsp;", " ").replaceAll("<.*?>", "");
         }
      }

      if(oZamundaNetSettings.bDownloadSubtitles)
      {
         oMatcher = ptnSubsunacs.matcher(sResponse);
         while(oMatcher.find())
         {
            sSubsunacs = oMatcher.group(1);
            alSubsunacs.add(sSubsunacs);
         }
         
         oMatcher = ptnSubsunacsURL.matcher(sResponse);
         while(oMatcher.find())
         {

            String sSubsunacsURL = oMatcher.group(1).replace("&amp;", "&");
            
            String sSubsunacsRespone = getHttpResponse(sSubsunacsURL);
            
            Matcher m = ptnSubsunacsURLs.matcher(sSubsunacsRespone);
            while(m.find())
            {
               String s = m.group(1);
               alSubsunacs.add("http://" + SUBSUNACS_DOMAIN + s);
            }
         }

         oMatcher = ptnSubssab.matcher(sResponse);
         while(oMatcher.find())
         {
            sSubssab = oMatcher.group(1).replace("&amp;", "&");;
            alSubssab.add(sSubssab);
         }
         
         oMatcher = ptnSubssabURL.matcher(sResponse);
         while(oMatcher.find())
         {

            String sSubssabURL = oMatcher.group(1).replace("&amp;", "&");
            
            String sSubssabRespone = getHttpResponse(sSubssabURL);
            
            Matcher m = ptnSubssabURLs.matcher(sSubssabRespone);
            while(m.find())
            {
               String s = m.group(1);
               s = s.replace("&amp;", "&");
//               alSubssab.add("http://" + SUBSUNACS_DOMAIN + s);
               alSubssab.add(s);
            }
            
         }
         
         oMatcher = ptnZamundaSubs.matcher(sResponse);
         if(oMatcher.find())
            sZamundaSubs = oMatcher.group();

         oMatcher = ptnSubsland.matcher(sResponse);
         if(oMatcher.find())
         {
            String sSubslandURL = oMatcher.group().replace("&amp;", "&");
            String sSubslandRespone = getHttpResponse(sSubslandURL);
            oMatcher = ptnSubslandFile.matcher(sSubslandRespone);
            if(oMatcher.find())
               sSubslandFile = oMatcher.group();
            
         }
         
         oMatcher = ptnSubslandFile.matcher(sResponse);
         if(oMatcher.find())
            sSubslandFile = oMatcher.group();

         oMatcher = ptnBukvi.matcher(sResponse);
         if(oMatcher.find())
         {
            sBukvi = oMatcher.group();
            
            String sBukviResponse = getHttpResponse(sBukvi);
            
            if(sBukviResponse != null)
            {
               if(!sBukviResponse.startsWith("Rar!"))
               {
                  oMatcher = ptnBukviFile.matcher(sBukviResponse);
                  while(oMatcher.find())
                  {
                     sBukviFile = oMatcher.group(2);
                     if(!sBukviFile.contains("http://bukvi."))
                        sBukviFile = BUKVI_URL + sBukviFile;
                  }
               }
               else
                  sBukviFile = sBukvi;
            }
         }
         
         oMatcher = ptnEasternSpirit.matcher(sResponse);
         if(oMatcher.find())
         {
            sEasternSpirit = oMatcher.group(1);
            String sEasternSpiritResponse = getHttpResponse(sEasternSpirit);
            
            if(sEasternSpiritResponse != null)
            {
               oMatcher = ptnEasternSpiritFile.matcher(sEasternSpiritResponse);
               while(oMatcher.find())
               {
                  sEasternSpiritFile = oMatcher.group(1);
                  if(!sEasternSpiritFile.contains(EASTERN_SPIRIT_URL))
                     sEasternSpiritFile = "http://www." + EASTERN_SPIRIT_URL + "/" +sEasternSpiritFile;
               }
            }
         }
      }

      return sTitle;
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
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
      
      sFolderName = sTitle.replace("/", "").trim();
      String sTorrentName = sTorrent.substring(sTorrent.lastIndexOf("/")+1);
      oMovieTorrent = new CMovie(sFolderName + File.separator + sTorrentName, sTorrent, sMagnet, sDescription);
      vFilesFnd.add(oMovieTorrent);
      
      if(sImage != null && !sImage.isEmpty())
      {
         String sExtension =  sImage.substring(sImage.lastIndexOf(".")+1);
         flImage = new CFile(sFolderName + File.separator + sFilesName + "." + sExtension, sImage);
         vFilesFnd.add(flImage);
      }
      
      if(alSubsunacs != null && !alSubsunacs.isEmpty())
      {
         for(String sSubsunacs : alSubsunacs)
         {
            flSubsunacs = new CFile(sFolderName + File.separator, sSubsunacs);
            vFilesFnd.add(flSubsunacs);
         }
         alSubsunacs.clear();
      }
      
      if(alSubssab != null && !alSubssab.isEmpty())
      {
         for(String sSubssab : alSubssab)
         {
            flSubssab= new CFile(sFolderName + File.separator, sSubssab);
            vFilesFnd.add(flSubssab);
         }
         alSubssab.clear();
      }
   
      if(sZamundaSubs != null && !sZamundaSubs.isEmpty())
      {
         String sExtension =  sZamundaSubs.substring(sZamundaSubs.lastIndexOf(".")+1);
         flZamundaSubs = new CFile(sFolderName + File.separator + sFilesName + "." + sExtension, sZamundaSubs);
         vFilesFnd.add(flZamundaSubs);
      }
      
      if(sSubslandFile != null && !sSubslandFile.isEmpty())
      {
         flSubsland = new CFile(sFolderName + File.separator, sSubslandFile);
         vFilesFnd.add(flSubsland);
      }      

      if(sBukviFile != null && !sBukviFile.isEmpty())
      {
         flBukvi = new CFile(sFolderName + File.separator + sFilesName + ".rar", sBukviFile);
         vFilesFnd.add(flBukvi);
      }      

      if(sEasternSpiritFile != null && !sEasternSpiritFile.isEmpty())
      {
         flEasternSpirit = new CFile(sFolderName + File.separator, sEasternSpiritFile);
         vFilesFnd.add(flEasternSpirit);
      }      
      
      return vFilesFnd;
   }

   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = null;

      alHttpProperties = new ArrayList<SHttpProperty>();
      String sCookies = COOKIE_UID_NAME + "=" + oZamundaNetSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + oZamundaNetSettings.sCookiePass;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
      
      new DownloadFile(oFile, sDownloadFolder, alHttpProperties).execute();
   }

   @Override
   protected void doneDownloadFile(CFile oFile, String sDownloadFolder, String saveFilePath)
   {
      super.doneDownloadFile(oFile, sDownloadFolder, saveFilePath);
      
      try
      {
         File f;

         if(oFile instanceof CMovie)
         {
            CMovie oMovie = (CMovie) oFile;
            sFolderName = oMovie.getName().substring(0, oMovie.getName().lastIndexOf(File.separator));
            
            if(oMovie.getName().endsWith(File.separator))
               f = new File(sDownloadFolder + File.separator + oFile.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator) + 1));
            else
               f = new File(sDownloadFolder + File.separator + oFile.getName());
            f.getParentFile().mkdirs();
            File source = new File(saveFilePath);
            Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileOutputStream fos; 
            
            if(oMovie.getMagnet() != null && !oMovie.getMagnet().isEmpty())
            {
               f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + MAGNET_FILE);
               f.createNewFile();
               fos = new FileOutputStream(f);
               fos.write(oMovie.getMagnet().getBytes());
               fos.close();
            }

            if(oMovie.getInfo() != null && !oMovie.getInfo().isEmpty())
            {
               f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + INFO_FILE);
               f.createNewFile();
               fos = new FileOutputStream(f);
               fos.write(oMovie.getInfo().getBytes());
               fos.close();
            }

         } 
         else
         {
            if(oFile.getName().endsWith(File.separator))
               f = new File(sDownloadFolder + File.separator + oFile.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator) + 1));
            else
               f = new File(sDownloadFolder + File.separator + oFile.getName());
            f.getParentFile().mkdirs();
            File source = new File(saveFilePath);
            Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }
      } catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   @Override
   protected void loadSettings()
   {
      try
      {
         JAXBContext jaxbContext = JAXBContext.newInstance(ZamundaNetSettings.class);
         
         File file = new File(SETTINGS_FILE);
         if(file.exists())
         {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            oZamundaNetSettings = (ZamundaNetSettings)jaxbUnmarshaller.unmarshal(file);
         }
         else
         {
            oZamundaNetSettings = new ZamundaNetSettings();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(oZamundaNetSettings, file);
         }
      } 
      catch(JAXBException e1)
      {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }         
   }
   
   private void saveSettings()
   {
      JAXBContext jaxbContext;
      try
      {
         jaxbContext = JAXBContext.newInstance(ZamundaNetSettings.class);
         File file = new File(SETTINGS_FILE);
         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         jaxbMarshaller.marshal(oZamundaNetSettings, file);
         
      } catch(JAXBException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   private void loginZamunda()
   {
      String urlParameters  = String.format("username=%s&password=%s", oZamundaNetSettings.sUser, oZamundaNetSettings.sPassword);
      String request        = HTTP + WWW + DOMAIN + "/takelogin.php";
      URL url;
      BufferedReader in;
      String sResponse;
      try
      {
         url = new URL( request );
         HttpURLConnection conn= (HttpURLConnection) url.openConnection();
         
         conn.setRequestMethod("POST");
         conn.setDoOutput(true);
         conn.setUseCaches(false);
         conn.setInstanceFollowRedirects(false);
         conn.setRequestProperty("Host", "www.zamunda.net");
         conn.setRequestProperty("User-Agent", "Mozilla/5.0");
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
         conn.setRequestProperty("Referer", HTTP + WWW + DOMAIN);
         conn.setRequestProperty("Connection", "keep-alive");
         conn.setDoInput(true);

         conn.setRequestProperty("Content-Length", Integer.toString(urlParameters.length()));
         conn.connect();

         DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
         wr.writeBytes(urlParameters);
         wr.flush();
         wr.close();
         
         if(conn.getResponseCode() == 302)
         {
            List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
            if (cookies != null) 
            {
               for(String cookie : cookies)
               {
                  cookie = cookie.substring(0, cookie.indexOf(";"));
                  String cookieName = cookie.substring(0, cookie.indexOf("="));
                  String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
                  if(cookieName.equals(COOKIE_UID_NAME))
                     oZamundaNetSettings.sCookieUID = cookieValue;
                  else if(cookieName.equals(COOKIE_PASS_NAME))
                     oZamundaNetSettings.sCookiePass = cookieValue;
                     
                  System.out.println(cookie);
               }
            }
         }
         if(conn.getResponseCode() == 200)
         {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            String inputLine;
            StringBuffer sbResponse = new StringBuffer();

            while((inputLine = in.readLine()) != null)
               sbResponse.append(inputLine + "\n");
            in.close();

            sResponse = sbResponse.toString();
            
            System.out.print(sResponse);            
         }
         
      } 
      catch(MalformedURLException e)
      {
         e.printStackTrace();
      } 
      catch(ProtocolException e)
      {
         e.printStackTrace();
      } 
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }
   
   private String getZamunda(String sURL)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      String sCookies = COOKIE_UID_NAME + "=" + oZamundaNetSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + oZamundaNetSettings.sCookiePass;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
      
      return getHttpResponse(sURL, alHttpProperties);
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "", propOrder = {"bDownloadTorrent","bDownloadMagnet","bDownloadImage","bDownloadDescription","bDownloadSubtitles", "sUser","sPassword","sCookieUID","sCookiePass"})
   @XmlRootElement(name = "settings")
   static private class ZamundaNetSettings
   {
      @XmlElement(name = "download_torrent", required = true)
      public boolean bDownloadTorrent = true;
      @XmlElement(name = "download_magnet", required = true)
      public boolean bDownloadMagnet = true;
      @XmlElement(name = "download_image", required = true)
      public boolean bDownloadImage = true;
      @XmlElement(name = "download_description", required = true)
      public boolean bDownloadDescription = true;
      @XmlElement(name = "download_subtitles", required = true)
      public boolean bDownloadSubtitles = true;
      @XmlElement(name = "user", required = true)
      public String sUser = "Rincewind123";
      @XmlElement(name = "password", required = true)
      public String sPassword = "suleiman";
      @XmlElement(name = "cookie_uid", required = true)
      public String sCookieUID;
      @XmlElement(name = "cookie_pass", required = true)
      public String sCookiePass;
   }

   
// final static Pattern ptnUri = Pattern.compile("^([\\w\\d\\.\\-\\?]*(\\%([A-F\\d]{2}))*[\\w\\d\\.\\-\\?]*)*$");
 final static Pattern ptnUri = Pattern.compile("(?!\\%[A-F\\d]{2})([^\\w\\-\\.\\?\\(\\)]+)");
          
 protected static boolean isValidURI(String uri)
 {
//    return !Pattern.matches("(?!\\%[A-F\\d]{2})([^\\w\\-\\.\\?]+)", uri);
    Matcher oMatcher = ptnUri.matcher(uri);
    if(!oMatcher.find())
       return true;
    else
       return false;
 }
 
// protected static boolean isUtf8Encoded(String url) 
// {
//    return isAlphaNumeric(url);
// }
//
// public static boolean isUrlUtf8Encoded(String url) throws UnsupportedEncodingException 
// {
//    return isAlphaNumeric(URLDecoder.decode(url, "UTF-8"));
// }
//
// public static boolean isUrlIsoEncoded(String url)throws UnsupportedEncodingException 
// {
//    return isAlphaNumeric(URLDecoder.decode(url, "ISO-8859-1"));
// }
//
// private static boolean isAlphaNumeric(String decode) 
// {
//    for (char c : decode.toCharArray()) 
//    {
//       if (!Character.isLetterOrDigit(c)) 
//       {
//          return false;
//       }
//    }
//    return true;
// }   
}
