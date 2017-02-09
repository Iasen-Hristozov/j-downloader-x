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
import com.discworld.jdownloaderx.dto.CMovie;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class ArenaBG extends Plugin
{
   private final static String DOMAIN = "arenabg.com",
                               SETTINGS_FILE = "arena_bg.xml",
                               COOKIE_UID_NAME = "uid",
                               COOKIE_PASS_NAME = "pass",
                               MAGNET_FILE = "magnet.txt",
                               INFO_FILE = "info.txt",
                               BUKVI_URL = "http://bukvi.bg";

   private final static Pattern ptnTitle = Pattern.compile("<title>(.+?) (\\.\\.\\. )?\u0441\u0432\u0430\u043b\u044f\u043d\u0435</title>"),
                                ptnTorrent = Pattern.compile("/download/key:.+?/"),
                                ptnImage = Pattern.compile("http(s)?:\\/\\/cdn.arenabg.com\\/resize\\/500\\/-\\/var\\/assets\\/posters\\/([\\d\\-]\\/)?.+?\\.jpg"),
                                ptnDescription = Pattern.compile("<div class=\"torrent-text\">(.+?)</div>"),
                                ptnSubsunacs = Pattern.compile("<a href=\"((http://)?subsunacs\\.net/.+?)\""),
                                ptnAddic7ed = Pattern.compile("href=\"((http://)?(www.)?addic7ed.com/.+?)\""),
                                ptnSubssab = Pattern.compile("<a href=\"((http://)?(www\\.)?subs\\.sab\\.bz/index\\.php\\?act=download\\&amp;attach_id=\\d+)\"( target=\"_blank\")?>"),
                                ptnSubssabURL = Pattern.compile("<a href=\\\"((http:\\/\\/)?(www\\.)?subs\\.sab\\.bz\\/(index\\.php)?\\?(s=.*?)?(&amp;)?act=search(&amp;sid=.+?)?&amp;movie=.+?)\\\"( target=\\\"_blank\\\")?>"),
                                ptnSubssabURLs = Pattern.compile("\\\"((http:\\/\\/)?(www\\.)?subs\\.sab\\.bz\\/index\\.php\\?(s=.*?)?(&amp;)?act=download(&amp;sid=.+?)?&attach_id=.+?)\"( target=\\\"_blank\\\")?"),
                                ptnUrlMovie = Pattern.compile("(http(s)?://)?(www\\.)?arenabg.com/[\\w\\d\\-]+?/"),
                                ptnUrlAddic7ed = Pattern.compile("href=\"(/(original|updated)/.+?)\""),
                                ptnBukvi = Pattern.compile("(http://)?bukvi.bg/load/(\\d+/\\w+/)?[\\d\\-]+"),
                                ptnBukviFile = Pattern.compile("<a href=\"(((http://)?bukvi.bg)?/load/[\\d\\-]+)\" onmouseover=\"return overlib\\('\u0421\u0432\u0430\u043b\u0438 \u0441\u0443\u0431\u0442\u0438\u0442\u0440\u0438\u0442\u0435\'\\);\""),
                                ptnProtocolDomain = Pattern.compile("(http(s)?://)?(www\\.)?arenabg.com");
   
   
   private ArenaBGSettings oArenaBGSettings;

   private String              sTitle, 
                               sTorrent,
                               sImage,
                               sDescription,
                               sSubsunacs,
                               sSubssab,
                               sAddic7ed,
                               sBukvi,
                               sBukviFile,
                               sFilesName,
                               sFolderName;

   ArrayList<String> alAddic7ed = new ArrayList<String>(),
                     alSubsunacs = new ArrayList<String>(),
                     alSubssab = new ArrayList<String>();
   
   private CMovie        oMovieTorrent = null;
   
   private CFile               flImage = null,
                               flSubsunacs = null,
                               flSubssab = null,
                               flAddic7ed = null,
                               flBukvi = null;


   public ArenaBG()
   {
      super();
   }
   
   public ArenaBG(IDownloader oDownloader)
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
      sTorrent = "";
      sImage = "";
      sDescription = "";
      sSubsunacs = "";
      sSubssab = "";
      sAddic7ed = "";
      sBukvi = "";
      sBukviFile = "";
      
      if(oArenaBGSettings.sCookieUID == null || 
         oArenaBGSettings.sCookieUID.isEmpty() || 
         oArenaBGSettings.sCookiePass == null || 
         oArenaBGSettings.sCookiePass.isEmpty())
      {
         loginArenaBG();
         saveSettings();
      }
      

      String sResponse = getArenaBG(sURL).replace("\n", "");
      
//      String sResponse = getHttpResponse(sURL).replace("\n", "");

      Matcher oMatcher = ptnTitle.matcher(sResponse);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group(1);
         sTitle = sTitle.replace(":", " -");
      }

      if(oArenaBGSettings.bDownloadTorrent)
      {
         oMatcher = ptnProtocolDomain.matcher(sURL);
         String sProtocolDomain = "";
         if(oMatcher.find())
            sProtocolDomain = oMatcher.group();
         else
            sProtocolDomain = HTTP + DOMAIN; 
         oMatcher = ptnTorrent.matcher(sResponse);
         if(oMatcher.find())
            sTorrent = sProtocolDomain + oMatcher.group();
      }

      if(oArenaBGSettings.bDownloadImage)
      {
         oMatcher = ptnImage.matcher(sResponse);
         if(oMatcher.find())
         {
            sImage = oMatcher.group();
            sImage = sImage.replace("https", "http");
         }
         
      }

      if(oArenaBGSettings.bDownloadDescription)
      {
         oMatcher = ptnDescription.matcher(sResponse);
         if(oMatcher.find())
         {
            sDescription = oMatcher.group(1);
            sDescription = sDescription.replaceAll("<br[\\s]*/>", "\n").replace("&nbsp;", " ").replaceAll("<.*?>", "");
         }
      }

      if(oArenaBGSettings.bDownloadSubtitles)
      {
         oMatcher = ptnSubsunacs.matcher(sResponse);
         while(oMatcher.find())
         {
            sSubsunacs = oMatcher.group(1);
            alSubsunacs.add(sSubsunacs);
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
         
         oMatcher = ptnAddic7ed.matcher(sResponse);
         if(oMatcher.find())
         {
            sAddic7ed = oMatcher.group(1);
            
            String sAddic7edRespone = getHttpResponse(sAddic7ed);

            if(sAddic7edRespone != null)
            {
               oMatcher = ptnUrlAddic7ed.matcher(sAddic7edRespone);
               while(oMatcher.find())
                  alAddic7ed.add(HTTP + "addic7ed.com" + oMatcher.group(1));
            }
         }
         
         oMatcher = ptnBukvi.matcher(sResponse);
         if(oMatcher.find())
         {
            sBukvi = oMatcher.group();
            
            String sBukviResponse = getHttpResponse(sBukvi);

            if(sBukviResponse != null)
            {
               oMatcher = ptnBukviFile.matcher(sBukviResponse);
               while(oMatcher.find())
               {
                  sBukviFile = oMatcher.group(1);
                  if(!sBukviFile.contains(BUKVI_URL))
                     sBukviFile = BUKVI_URL + sBukviFile;
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

      sFilesName = sTitle;
      
      sFolderName = sTitle.replace("/", "").trim();
      String sTorrentName = sTorrent.substring(sTorrent.lastIndexOf("/")+1);
      oMovieTorrent = new CMovie(sFolderName + File.separator + sTorrentName, sTorrent, null, sDescription);
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
      
      if(alAddic7ed != null && !alAddic7ed.isEmpty())
      {
         for(String sAddic7ed : alAddic7ed)
         {
            flAddic7ed = new CFile(sFolderName + File.separator, sAddic7ed);
            vFilesFnd.add(flAddic7ed);
         }
         alAddic7ed.clear();
      }
      
      if(sBukviFile != null && !sBukviFile.isEmpty())
      {
         flBukvi = new CFile(sFolderName + File.separator + sFilesName + ".rar", sBukviFile);
         vFilesFnd.add(flBukvi);
      }      
      
   
      return vFilesFnd;
   }

   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      
      String sCookies = COOKIE_UID_NAME + "=" + oArenaBGSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + oArenaBGSettings.sCookiePass;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
      alHttpProperties.add(new SHttpProperty("Referer", DOMAIN));

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
               f = new File(sDownloadFolder + File.separator + oMovie.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator) + 1));
            else
               f = new File(sDownloadFolder + File.separator + oMovie.getName());

            f.getParentFile().mkdirs();
            File source = new File(saveFilePath);
            Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileOutputStream fos; 
            if(oMovie.getMagnet() != null && !oMovie.getMagnet().isEmpty())
            {
               f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + MAGNET_FILE);
               f.getParentFile().mkdirs();
               f.createNewFile();
               fos= new FileOutputStream(f);
               fos.write(oMovie.getMagnet().getBytes());
               fos.close();
            }
            f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + INFO_FILE);
            f.createNewFile();
            fos = new FileOutputStream(f);
            fos.write(oMovie.getInfo().getBytes());
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
      } 
      catch(IOException e)
      {
         e.printStackTrace();
      }

   }

   @Override
   protected void loadSettings()
   {
      try
      {
         JAXBContext jaxbContext = JAXBContext.newInstance(ArenaBGSettings.class);
         
         File file = new File(SETTINGS_FILE);
         if(file.exists())
         {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            oArenaBGSettings = (ArenaBGSettings)jaxbUnmarshaller.unmarshal(file);
         }
         else
         {
            oArenaBGSettings = new ArenaBGSettings();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(oArenaBGSettings, file);
         }
      } 
      catch(JAXBException e)
      {
         e.printStackTrace();
      }         
   }
   
   private void saveSettings()
   {
      JAXBContext jaxbContext;
      try
      {
         jaxbContext = JAXBContext.newInstance(ArenaBGSettings.class);
         File file = new File(SETTINGS_FILE);
         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         jaxbMarshaller.marshal(oArenaBGSettings, file);
         
      } catch(JAXBException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   private void loginArenaBG()
   {
      String urlParameters  = String.format("username=%s&password=%s", oArenaBGSettings.sUser, oArenaBGSettings.sPassword);
      String request        = HTTPS + WWW + DOMAIN + "users/login/";
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
         conn.setRequestProperty("Host", "www.arenabg.com");
         conn.setRequestProperty("User-Agent", "Mozilla/5.0");
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
         conn.setRequestProperty("Referer", HTTPS + WWW + DOMAIN);
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
                     oArenaBGSettings.sCookieUID = cookieValue;
                  else if(cookieName.equals(COOKIE_PASS_NAME))
                     oArenaBGSettings.sCookiePass = cookieValue;
                     
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
   
   
   private String getArenaBG(String sURL)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      String sCookies = COOKIE_UID_NAME + "=" + oArenaBGSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + oArenaBGSettings.sCookiePass;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
      
      return getHttpResponse(sURL, alHttpProperties);
   }
 
   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "", propOrder = {"bDownloadTorrent","bDownloadImage","bDownloadDescription","bDownloadSubtitles","sUser","sPassword","sCookieUID","sCookiePass"})
   @XmlRootElement(name = "settings")
   static private class ArenaBGSettings
   {
      @XmlElement(name = "download_torrent", required = true)
      public boolean bDownloadTorrent = true;
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
