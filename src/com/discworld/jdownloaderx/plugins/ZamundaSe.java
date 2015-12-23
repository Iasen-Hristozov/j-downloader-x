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
import com.discworld.jdownloaderx.dto.FileUtils;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class ZamundaSe extends Plugin
{
   private final static String DOMAIN = "zelka.org",
                               URL = "(http://)?zelka\\.org/details\\.php\\?id=(\\d)*",
                               COOKIE_UID_NAME = "uid",
                               COOKIE_PASS_NAME = "pass",
                               SETTINGS_FILE = "zamunda_se.xml",
                               MAGNET_FILE = "magnet.txt",
                               INFO_FILE = "info.txt";

   private final static String[] DOMAINS = {"zelka.org", "zamunda.se"};

   private final static Pattern ptnTitle = Pattern.compile("(<h1>)(.+)(<[\\s]*/h1>)"),
                                ptnTitleParts = Pattern.compile("(.*?)( / .*?)* (\\(\\d+(\\-\\d+)?\\))"),
                                ptnTorrent = Pattern.compile("download.php/\\S+\\.(torrent?)"),
                                ptnMagnet = Pattern.compile("magnet:\\?xt=urn:btih:[\\w]*"),
                                ptnImage = Pattern.compile("(<div id=description>(<div align=center>)?<img border=\"0\" src=\")(.+?)(\">)"),
                                ptnDescription = Pattern.compile("(\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435)(.*?)((\u0421\u0432\u0430\u043b\u0438 \u0421\u0443\u0431\u0442\u0438\u0442\u0440\u0438)|(\u0412\u0438\u0434\u0435\u043e)|(NFO))"),
                                ptnSubsunacs = Pattern.compile("(<a href=)((http://)?(www\\.)?subsunacs.net/(((get|info)\\.php\\?id=\\d+)|(subtitles/.+?)))(( target=_blank)?>)"),
                                ptnZelkasubs = Pattern.compile("(<a href=)((http://)?(www\\.)?((zelka.org)|(zamunda.se))/getsubs.php/(.+?))( target=_blank)?>"),
                                ptnSubssab = Pattern.compile("(<a href=)((http://)?(www\\.)?subs\\.sab\\.bz/index\\.php\\?(s=[\\d\\w]+&amp;)?act=download&amp;attach_id=.+?)((target=_blank)?>)");

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

   private ZamundaSeSettings   oZamundaSeSettings;
   
   private CFile               flImage = null,
                               flSubsunacs = null,
                               flSubssab = null,
                               flZelkasubs = null;


   public ZamundaSe()
   {
      super();
   }
   
   public ZamundaSe(IDownloader oDownloader)
   {
      super(oDownloader);
   }

   @Override
   public ArrayList<String> parseContent(String sContent)
   {
      Pattern ptnUrlMovie = Pattern.compile(URL);
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

      if(oZamundaSeSettings.sCookieUID == null || 
         oZamundaSeSettings.sCookieUID.isEmpty() || 
         oZamundaSeSettings.sCookiePass == null || 
         oZamundaSeSettings.sCookiePass.isEmpty())
      {
         loginZelka();
         saveSettings();
      }
      
      String sResponse = getZelka(sURL).replace("\n", "");

      Matcher oMatcher = ptnTitle.matcher(sResponse);
      if(oMatcher.find())
         sTitle = oMatcher.group(2);

      if(oZamundaSeSettings.bDownloadTorrent)
      {
         oMatcher = ptnTorrent.matcher(sResponse);
         if(oMatcher.find())
            sTorrent = oMatcher.group();
      }
      
      if(oZamundaSeSettings.bDownloadMagnet)
      {
         oMatcher = ptnMagnet.matcher(sResponse);
         if(oMatcher.find())
            sMagnet = oMatcher.group();
      }

      if(oZamundaSeSettings.bDownloadImage)
      {
         oMatcher = ptnImage.matcher(sResponse);
         if(oMatcher.find())
            sImage = oMatcher.group(3);
      }

      if(oZamundaSeSettings.bDownloadDescription)
      {
         oMatcher = ptnDescription.matcher(sResponse);
         if(oMatcher.find())
         {
            sDescription = oMatcher.group(2);
            sDescription = sDescription.replace("<br />", "\n").replace("&nbsp;", " ").replaceAll("<.*?>", "");
         }
      }

      if(oZamundaSeSettings.bDownloadSubtitles)
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
      
      sFolderName = sTitle.trim().replace("/", "").replace(":", " -");
      
      String sTorrentName = sTorrent.substring(sTorrent.lastIndexOf("/") + 1);
      oMovieTorrent = new MovieTorrent(sFolderName + File.separator + sTorrentName, "http://" + DOMAIN + "/" + sTorrent, sDescription, sMagnet);
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
      String sCookies = COOKIE_UID_NAME + "=" + oZamundaSeSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + oZamundaSeSettings.sCookiePass;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
      new DownloadFile(oFile, sDownloadFolder, alHttpProperties).execute();
   }

   @Override
   protected void doneDownloadFile(CFile oFile, String sDownloadFolder, String saveFilePath)
   {
      super.doneDownloadFile(oFile, sDownloadFolder, saveFilePath);
      
      FileUtils.renameFile(saveFilePath, sDownloadFolder + File.separator + oFile.getName());
      if(oFile instanceof MovieTorrent)
      {
         try
         {
            File f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + MAGNET_FILE);
            f.getParentFile().mkdirs();
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(((MovieTorrent)oFile).getMagnet().getBytes());
            fos.close();
            f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + INFO_FILE);
            f.createNewFile();
            fos = new FileOutputStream(f);
            fos.write(((MovieTorrent)oFile).getInfo().getBytes());
            fos.close();
            
         } catch(IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   @Override
   protected void loadSettings()
   {
      try
      {
         JAXBContext jaxbContext = JAXBContext.newInstance(ZamundaSeSettings.class);
         
         File file = new File(SETTINGS_FILE);
         if(file.exists())
         {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            oZamundaSeSettings = (ZamundaSeSettings)jaxbUnmarshaller.unmarshal(file);
         }
         else
         {
            oZamundaSeSettings = new ZamundaSeSettings();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(oZamundaSeSettings, file);
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
         jaxbContext = JAXBContext.newInstance(ZamundaSeSettings.class);
         File file = new File(SETTINGS_FILE);
         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         jaxbMarshaller.marshal(oZamundaSeSettings, file);
         
      } catch(JAXBException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   private void loginZelka()
   {
      String urlParameters  = String.format("username=%s&password=%s", oZamundaSeSettings.sUser, oZamundaSeSettings.sPassword);
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
         conn.setRequestProperty("Host", "zelka.org");
         conn.setRequestProperty("User-Agent", "Mozilla/5.0");
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
         conn.setRequestProperty("Referer", HTTP + DOMAIN);
         conn.setRequestProperty("Connection", "keep-alive");
         conn.setDoInput(true);

         // Send post request
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
                     oZamundaSeSettings.sCookieUID = cookieValue;
                  else if(cookieName.equals(COOKIE_PASS_NAME))
                     oZamundaSeSettings.sCookiePass = cookieValue;
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
         }
      } catch(MalformedURLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch(ProtocolException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   private String getZelka(String sURL)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      String sCookies = COOKIE_UID_NAME + "=" + oZamundaSeSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + oZamundaSeSettings.sCookiePass;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
      
      return getHttpResponse(sURL, alHttpProperties);
   }
   
   @Override
   public boolean isMine(String sURL)
   {
      for(String sDomain : DOMAINS)
         if(sURL.contains(sDomain))
            return true;
      return false;
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "", propOrder = {"bDownloadTorrent","bDownloadMagnet","bDownloadImage","bDownloadDescription","bDownloadSubtitles", "sUser","sPassword","sCookieUID","sCookiePass"})
   @XmlRootElement(name = "settings")
   static private class ZamundaSeSettings
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
