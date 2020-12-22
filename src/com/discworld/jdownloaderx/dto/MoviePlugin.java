package com.discworld.jdownloaderx.dto;

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

public abstract class MoviePlugin extends Plugin
{
   protected final static String COOKIE_UID_NAME = "uid",
                                 COOKIE_PASS_NAME = "pass",
                                 MAGNET_FILE = "magnet.txt",
                                 INFO_FILE = "info.txt",
                                 GRP_TITLE = "title",
                                 GRP_IMAGE = "image",
                                 GRP_DESCRIPTION = "description",
                                 GRP_MAGNET = "magnet",
                                 GRP_TORRENT = "torrent",
                                 TORRENT_SUFIX = "torrent";
   
   protected String sFolderName;
   
   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "", propOrder = {"bDownloadTorrent","bDownloadMagnet","bDownloadImage","bDownloadDescription","bDownloadSubtitles","sUser","sPassword","sCookieUID","sCookiePass"})
   @XmlRootElement(name = "settings")
   protected static class MovieSettings
   {
//      public MovieSettings()
//      {
//         sUser = getUser();
//         sPassword = getPassword();
//      }
      
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
      public String sUser;
      @XmlElement(name = "password", required = true)
      public String sPassword;
      @XmlElement(name = "cookie_uid", required = true)
      public String sCookieUID;
      @XmlElement(name = "cookie_pass", required = true)
      public String sCookiePass;
   }

   protected String sTitle, 
                    sTorrentUrl,
                    sImageUrl,
                    sDescription,
                    sMagnet,
                    sFilesName;
   
   protected CFile flImage = null;
   
   public MoviePlugin()
   {
      super();
   }

   public MoviePlugin(IDownloader downloader)
   {
      super(downloader);
   }
   
   @Override
   protected String inBackgroundHttpParse(String sURL) throws Exception
   {
      sMagnet = "";
      sTorrentUrl = "";
      sImageUrl = "";
      sDescription = "";
   
      if(getMovieSettings().sCookieUID == null || 
         getMovieSettings().sCookieUID.isEmpty() || 
         getMovieSettings().sCookiePass == null || 
         getMovieSettings().sCookiePass.isEmpty())
      {
         login();
         saveSettings();
      }
      String sResponse = getURLResponse(sURL).replace("\n", "");
      
      sTitle = getTitle(sResponse);

      if(getMovieSettings().bDownloadTorrent)
         sTorrentUrl = getTorrentUrl(sURL, sResponse);

      if(getMovieSettings().bDownloadMagnet)
         sMagnet = getMagnet(sResponse);

      if(getMovieSettings().bDownloadImage)
         sImageUrl = getImageUrl(sResponse);

      if(getMovieSettings().bDownloadDescription)
      {
         sDescription = getDescription(sResponse);
      }

      if(getMovieSettings().bDownloadSubtitles)
      {
         downloader.checkContetsVsPlugins(sTitle.replace("/", "").trim(), sResponse);
      }
      
      return sTitle;
   }

   protected void login()
      {
         URL url;
         BufferedReader in;
   //      String sResponse;
         try
         {
            url = new URL( getLoginUrl() );
            HttpURLConnection conn= (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Host", getDomain());
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Referer", getReferer());
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setDoInput(true);
               conn.setRequestProperty("Content-Length", Integer.toString(getLoginUrlParamtres().length()));
            conn.connect();
      
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(getLoginUrlParamtres());
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
                        getMovieSettings().sCookieUID = cookieValue;
                     else if(cookieName.equals(COOKIE_PASS_NAME))
                        getMovieSettings().sCookiePass = cookieValue;
                        
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
      
   //            sResponse = sbResponse.toString();
   //            
   //            System.out.print(sResponse);            
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

   abstract protected String getLoginUrl();

   abstract protected String getLoginUrlParamtres();

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
   
      sFilesName = getFilesName();
      
      sFolderName = sTitle.replace("/", "").trim();
      String sTorrentName = sTorrentUrl.substring(sTorrentUrl.lastIndexOf("/")+1);
      if(!sTorrentName.endsWith("." + TORRENT_SUFIX))
         sTorrentName = sFolderName + "." + TORRENT_SUFIX; 
      Movie movie = new Movie(sFolderName + File.separator + sTorrentName, sTorrentUrl, sMagnet, sDescription);
      alFilesFound.add(movie);
      
      addImageFile(alFilesFound);
   
      return alFilesFound;
   }

   abstract protected String getFilesName();

   protected void addImageFile(ArrayList<CFile> alFilesFound)
   {
      if(sImageUrl != null && !sImageUrl.isEmpty())
      {
         String sExtension =  sImageUrl.substring(sImageUrl.lastIndexOf(".")+1);
         flImage = new CFile(sFolderName + File.separator + sFilesName + "." + sExtension, sImageUrl);
         alFilesFound.add(flImage);
      }
   }

   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      
      String sCookies = COOKIE_UID_NAME + "=" + getMovieSettings().sCookieUID + "; " + COOKIE_PASS_NAME + "=" + getMovieSettings().sCookiePass;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
      alHttpProperties.add(new SHttpProperty("Referer", getDomain()));
   
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
            
            if(saveFilePath != null)
            {
               File source = new File(saveFilePath);
               Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

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

   abstract protected Pattern getUrlPattern();

   @Override
   abstract protected Pattern getTitlePattern();

   @Override
   abstract protected Pattern getFileUrlPattern();

   abstract protected Pattern getTorrentUrlPattern();

   abstract protected String getTorrentUrl(String sURL, String sResponse);

   abstract protected Pattern getMagnetPattern();

   abstract protected Pattern getImageUrlPattern();

   abstract protected Pattern getDescriptionPattern();

   protected String getTitle(String sResponse)
   {
      String sTitle = "";
      Matcher oMatcher = getTitlePattern().matcher(sResponse);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group(GRP_TITLE);
         sTitle = sTitle.replace(":", " -")
                        .replace("*", "-")
                        .replace("?", "")
                        .replace("&quot;", "")
                        .replace("/", "")
                        .trim();
      }
      
      return sTitle;
   }

   protected String getTorrentUrl(String sContent)
   {
      return null;
   }

   protected String getMagnet(String sResponse)
   {
      String sMagnet = "";
      Matcher matcher = getMagnetPattern().matcher(sResponse);
      if(matcher.find())
      {
         sMagnet = matcher.group(GRP_MAGNET);
      }
      return sMagnet;
   }

   protected String getImageUrl(String sResponse)
   {
      String sImageUrl = "";
      Matcher matcher = getImageUrlPattern().matcher(sResponse);
      if(matcher.find())
      {
         sImageUrl = matcher.group(GRP_IMAGE).replace("https", "http");
      }
      
      return sImageUrl;
   }

   protected String getDescription(String sResponse)
   {
      String sDescription = "";
      Matcher matcher = getDescriptionPattern().matcher(sResponse);
      if(matcher.find())
      {
         sDescription = matcher.group(GRP_DESCRIPTION).replaceAll("<br[\\s]*/>", "\n")
                  .replace("&nbsp;", " ")
                  .replaceAll("<.*?>", "")
                  .replaceAll("\n\n", "\n")
                  .replaceAll("\t", "")
                  .trim();
      }
      return sDescription;
   }

   protected void loadSettings()
   {
      try
      {
         JAXBContext jaxbContext = JAXBContext.newInstance(MovieSettings.class);
         MovieSettings movieSettings = null;
         
         File file = new File(getMoviesSettingsFile());
         if(file.exists())
         {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            movieSettings = (MovieSettings)jaxbUnmarshaller.unmarshal(file);
         }
         else
         {
            movieSettings = new MovieSettings();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(movieSettings, file);
         }
         setMoviesSettings(movieSettings);
      } 
      catch(JAXBException e)
      {
         e.printStackTrace();
      }         
   }
   
   protected void saveSettings()
   {
      JAXBContext jaxbContext;
      try
      {
         jaxbContext = JAXBContext.newInstance(MovieSettings.class);
         File file = new File(getMoviesSettingsFile());
         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         jaxbMarshaller.marshal(getMovieSettings(), file);
         
      } catch(JAXBException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   abstract protected String getMoviesSettingsFile();

   abstract protected MovieSettings getMovieSettings();
   
   abstract protected void setMoviesSettings(MovieSettings movieSettings);

   abstract protected String getUser();

   abstract protected String getPassword();

   protected String getURLResponse(String sURL) throws Exception
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      String sCookies = COOKIE_UID_NAME + "=" + getMovieSettings().sCookieUID + "; " + COOKIE_PASS_NAME + "=" + getMovieSettings().sCookiePass;
      alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
      
      return getHttpResponse(sURL, alHttpProperties);
   }
   
   abstract protected String getReferer();
}
