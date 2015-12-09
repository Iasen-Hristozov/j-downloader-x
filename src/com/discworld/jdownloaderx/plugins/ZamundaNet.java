package com.discworld.jdownloaderx.plugins;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class ZamundaNet extends Plugin
{
   private final static String DOMAIN = "zamunda.net",
                               COOKIE_UID_NAME = "uid",
                               COOKIE_PASS_NAME = "pass",
                               SETTINGS_FILE = "zamunda_net.xml",
                               MAGNET_FILE = "magnet.txt",
                               INFO_FILE = "info.txt",
                               HTTP = "http://",
                               WWW = "www.";

   private final static Pattern ptnTitle = Pattern.compile("(<h1>)(.+)(<[\\s]*/h1>)"),
                                ptnTitleParts = Pattern.compile("(.*?)( / .*?)* (\\(\\d+(\\-\\d+)?\\))"),
                                ptnTorrent = Pattern.compile("/download_go\\.php\\?id=(\\d+)\"[\\s]*>(.+?)</a>"),
                                ptnMagnetLink = Pattern.compile("/magnetlink/download_go\\.php\\?id=\\d+&m=x"),
                                ptnMagnet = Pattern.compile("magnet:\\?xt=urn:btih:[\\w]*"),
                                ptnImage = Pattern.compile("img border=\"0\" src=\"((http://)?img.zamunda.net/bitbucket/(.+?))\""),
                                ptnDescription = Pattern.compile("(\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435)(.*?)((\u0421\u0443\u0431\u0442\u0438\u0442\u0440\u0438)|(\u0412\u0438\u0434\u0435\u043e)|(NFO))"),
                                ptnSubsunacs = Pattern.compile("(<a href=)((http://)?(www\\.)?subsunacs.net/((info\\.php\\?id=\\d+)|(get\\.php\\?id=\\d+)|(subtitles/.+?)))(( target=_blank)?>)"),
                                ptnZelkasubs = Pattern.compile("(<a href=)((http://)?(www\\.)?zamunda\\.net/getsubs\\.php/(.+?))( target=_blank)?>"),
                                ptnSubssab = Pattern.compile("(<a href=)((http://)?(www\\.)?subs\\.sab\\.bz/index\\.php\\?act=download&amp;attach_id=.+?)(( target=_blank)?>)"),
                                ptnUrlMovie = Pattern.compile("(http://)?(www.)?zamunda\\.net/banan\\?id=\\d+");   
   
   
   private ZamundaNetSettings oZamundaNetSettings;

   private String              sTitle, 
                               sMagnet,
                               sTorrent,
                               sImage,
                               sDescription,
                               sSubsunacs,
                               sZelkasubs,
                               sSubssab,
                               sFilesName,
                               sFolderName;


   
   private MovieTorrent        oMovieTorrent = null;
   
   private CFile               flImage = null,
                               flSubsunacs = null,
                               flSubssab = null,
                               flZelkasubs = null;


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
      return sURL.contains(DOMAIN);
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
      sZelkasubs = "";
      sSubssab = "";
   
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
         sTitle = sTitle.replace(":", " -");
      }

      if(oZamundaNetSettings.bDownloadTorrent)
      {
         oMatcher = ptnTorrent.matcher(sResponse);
         if(oMatcher.find())
            sTorrent = HTTP + WWW + DOMAIN + "/download.php/" + oMatcher.group(1) + "/" + oMatcher.group(2) + ".torrent";
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
            sImage = oMatcher.group(1);
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
         if(oMatcher.find())
            sSubsunacs = oMatcher.group(2);
         
         oMatcher = ptnZelkasubs.matcher(sResponse);
         if(oMatcher.find())
            sZelkasubs = oMatcher.group(2);
   
         oMatcher = ptnSubssab.matcher(sResponse);
         if(oMatcher.find())
         {
            sSubssab = oMatcher.group(2);
            sSubssab = sSubssab.replace("&amp;", "&");
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
         sFilesName = oMatcher.group(1) + " " + oMatcher.group(3);   
         sFilesName = sFilesName.trim();
      }
      else
         sFilesName = sTitle;
      
      sFolderName = sTitle.replace("/", "").trim();
      String sTorrentName = sTorrent.substring(sTorrent.lastIndexOf("/")+1);
      oMovieTorrent = new MovieTorrent(sFolderName + File.separator + sTorrentName, sTorrent, sDescription, sMagnet);
      vFilesFnd.add(oMovieTorrent);
      
      if(sImage != null && !sImage.isEmpty())
      {
         String sExtension =  sImage.substring(sImage.lastIndexOf(".")+1);
         flImage = new CFile(sFolderName + File.separator + sFilesName + "." + sExtension, sImage);
         vFilesFnd.add(flImage);
      }
      
      if(sSubsunacs != null && !sSubsunacs.isEmpty())
      {
         flSubsunacs = new CFile(sFolderName + File.separator, sSubsunacs);
         vFilesFnd.add(flSubsunacs);
      }
      
      if(sSubssab != null && !sSubssab.isEmpty())
      {
         flSubssab= new CFile(sFolderName + File.separator, sSubssab);
         vFilesFnd.add(flSubssab);
      }
   
      if(sZelkasubs != null && !sZelkasubs.isEmpty())
      {
         String sExtension =  sZelkasubs.substring(sZelkasubs.lastIndexOf(".")+1);
         flZelkasubs = new CFile(sFolderName + File.separator + sFilesName + "." + sExtension, sZelkasubs);
         vFilesFnd.add(flZelkasubs);
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

         if(oFile.getURL().endsWith(".torrent"))
         {
            if(oFile.getName().endsWith(File.separator))
               f = new File(sDownloadFolder + File.separator + oFile.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator) + 1));
            else
               f = new File(sDownloadFolder + File.separator + oFile.getName());
            f.getParentFile().mkdirs();
            File source = new File(saveFilePath);
            Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileOutputStream fos; 
            if(((MovieTorrent) oFile).getMagnet() != null && !((MovieTorrent) oFile).getMagnet().isEmpty())
            {
               f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + MAGNET_FILE);
               f.getParentFile().mkdirs();
               f.createNewFile();
               fos= new FileOutputStream(f);
               fos.write(((MovieTorrent) oFile).getMagnet().getBytes());
               fos.close();
            }
            f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + INFO_FILE);
            f.createNewFile();
            fos = new FileOutputStream(f);
            fos.write(((MovieTorrent) oFile).getInfo().getBytes());
            fos.close();

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
}
