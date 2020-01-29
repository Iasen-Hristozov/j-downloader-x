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

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.Movie;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class ZamundaNet extends Plugin
{
   
   private final static String DOMAIN = "www.zamunda.net",
                               COOKIE_UID_NAME = "uid",
                               COOKIE_PASS_NAME = "pass",
                               SETTINGS_FILE = "zamunda_net.xml",
                               MAGNET_FILE = "magnet.txt",
                               INFO_FILE = "info.txt";
   
   private final static Pattern ptnURL = Pattern.compile("((http(s?)://)?(www.)?zamunda\\.net/banan\\?id=\\d+)"),
                                ptnTitle = Pattern.compile("<h1(.*?)>(.*?)<\\/.*?>"),
                                ptnTitleParts = Pattern.compile("^(.+?)(\\/.+?)*(\\(\\d+(\\-\\d+)?\\))?([ ]?\\[.+?\\])?$"),
                                ptnTorrent = Pattern.compile("/download_go\\.php\\?id=(\\d+)\"[\\s]*>(.+?)</a>"),
                                ptnMagnetLink = Pattern.compile("/magnetlink/download_go\\.php\\?id=\\d+&m=x"),
                                ptnMagnet = Pattern.compile("magnet:\\?xt=urn:btih:[\\w]*"),
                                ptnImage = Pattern.compile("img border=(\\\")?0(\\\")? src=\\\"(((http(s?):\\/\\/)?(img(\\d)?.)?zamunda.net\\/(pic\\/)?(img(\\d)\\/)?)bitbucket\\/([\\d]+\\/)?(.+?))\\\""),
                                ptnImage1 = Pattern.compile("img border=(\\\")?0(\\\")? src=\\\"((http:\\/\\/)?i.imgur.com\\/(.+?))\\\""),
                                ptnDescription = Pattern.compile("<div id=description>(<br \\/><br>)?<div align=center>([\\S\\s]+?)<div align=center"),
                                ptnZamundaSubs = Pattern.compile("((http(s?):\\/\\/)?(www\\.)?zamunda\\.net\\/getsubs\\.php\\/([\\w\\-\\.]+))");
   
   private ZamundaNetSettings oZamundaNetSettings;

   private String              sTitle, 
                               sMagnet,
                               sTorrent,
                               sImage,
                               sDescription,
                               sZamundaSubs,
                               sFilesName,
                               sFolderName;


   
   private Movie        oMovieTorrent = null;
   
   private CFile               flImage = null,
                               flZamundaSubs = null;
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
   
   public ZamundaNet(IDownloader oDownloader)
   {
      super(oDownloader);
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
      sMagnet = "";
      sTorrent = "";
      sImage = "";
      sDescription = "";
      sZamundaSubs = "";
   
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
               
               sTorrent = HTTPS + DOMAIN + "/download.php/" + oMatcher.group(1) + "/" + url + ".torrent";
            }
         } 
         catch(UnsupportedEncodingException e)
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
            
            String sMagnetResult = getHttpResponse(HTTPS + DOMAIN + sMagnet, alHttpProperties);
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
               String sImageTmp = oMatcher.group(3);
               
              if(!isValidURI(sImageTmp))
                 sImageTmp = URLEncoder.encode(sImageTmp, "UTF-8");
                 
               sImage = oMatcher.group(3);
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
            sDescription = sDescription.replaceAll("<br[\\s]*/>", "\n").replace("&nbsp;", " ").replaceAll("<.*?>", "").replaceAll("\n\n", "\n").trim();
         }
      }

      if(oZamundaNetSettings.bDownloadSubtitles)
      {
         oMatcher = ptnZamundaSubs.matcher(sResponse);
         if(oMatcher.find())
            sZamundaSubs = oMatcher.group();
      }

      downloader.checkContetsVsPlugins(sTitle.replace("/", "").trim(), sResponse);
      
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
      oMovieTorrent = new Movie(sFolderName + File.separator + sTorrentName, sTorrent, sMagnet, sDescription);
      vFilesFnd.add(oMovieTorrent);
      
      if(sImage != null && !sImage.isEmpty())
      {
         String sExtension =  sImage.substring(sImage.lastIndexOf(".")+1);
         flImage = new CFile(sFolderName + File.separator + sFilesName + "." + sExtension, sImage);
         vFilesFnd.add(flImage);
      }
   
      if(sZamundaSubs != null && !sZamundaSubs.isEmpty())
      {
         String sExtension =  sZamundaSubs.substring(sZamundaSubs.lastIndexOf(".")+1);
         flZamundaSubs = new CFile(sFolderName + File.separator + sFilesName + "." + sExtension, sZamundaSubs);
         vFilesFnd.add(flZamundaSubs);
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
      
      new DownloadFileThread(oFile, sDownloadFolder, alHttpProperties).execute();
   }

   @Override
   protected void downloadFileDone(CFile file, String sDownloadFolder, String saveFilePath)
   {
      downloader.deleteFileFromLists(file);

      downloader.saveFilesList();
      
      try
      {
         File f;

         if(file instanceof Movie)
         {
            Movie oMovie = (Movie) file;
            sFolderName = oMovie.getName().substring(0, oMovie.getName().lastIndexOf(File.separator));
            
            if(oMovie.getName().endsWith(File.separator))
               f = new File(sDownloadFolder + File.separator + file.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator) + 1));
            else
               f = new File(sDownloadFolder + File.separator + file.getName());
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
            if(file.getName().endsWith(File.separator))
               f = new File(sDownloadFolder + File.separator + file.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator) + 1));
            else
               f = new File(sDownloadFolder + File.separator + file.getName());
            f.getParentFile().mkdirs();
            File source = new File(saveFilePath);
            Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }
      } 
      catch(IOException e)
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
      String request        = HTTPS + DOMAIN + "/takelogin.php";
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
         conn.setRequestProperty("Referer", HTTPS + DOMAIN);
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
   
   private String getZamunda(String sURL) throws Exception
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
      // TODO Auto-generated method stub
      return null;
   }
   
   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }

}
