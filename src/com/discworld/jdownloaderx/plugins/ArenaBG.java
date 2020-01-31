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
   public final static String DOMAIN = "arenabg.com";

   private static final String SETTINGS_FILE = "arena_bg.xml";

   private static final String USER = "Rincewind123";

   private static final String PASSWORD = "suleiman";

//   private static final String COOKIE_UID_NAME = "uid";
//
//   private static final String COOKIE_PASS_NAME = "pass";

//   private static final String MAGNET_FILE = "magnet.txt";
//
//   private static final String INFO_FILE = "info.txt";

   private final static Pattern ptnUrlMovie = Pattern.compile("((http(s)?://)?(www\\.)?arenabg.com/[\\w\\d\\-]+?/)"),
                                ptnTorrent = Pattern.compile("/(download|get)/key:.+?/"),
                                ptnTitle = Pattern.compile("<title>(?<"+GRP_TITLE+">.+?) (\\.\\.\\. )?\u0441\u0432\u0430\u043b\u044f\u043d\u0435</title>"),
                                ptnMagnet = Pattern.compile("magnet:\\?xt=urn:btih:[\\w]*"),
                                ptnImage = Pattern.compile("http(s)?:\\/\\/cdn.arenabg.com\\/resize\\/500\\/-\\/var\\/assets\\/posters\\/([\\d\\-]\\/)?.+?\\.jpg"),
                                ptnDescription = Pattern.compile("<div class=\"torrent-text\">(?<"+GRP_DESCRIPTION+">.+?)</div>"),
                                ptnProtocolDomain = Pattern.compile("(http(s)?://)?(www\\.)?arenabg.com");
      
   public MovieSettings àrenaBGSettings;

//   public String sFilesName;
//                  sTitle, 
//                  sTorrent,
//                  sImage,
//                  sDescription,
//                  sMagnet;
   
//   public Movie  movie = null;
   
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

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }
   
//   @Override
//   public ArrayList<String> getURLsFromContent(String sContent)
//   {
//      ArrayList<String> alUrlMovies = new ArrayList<String>();
//   
//      Matcher m = ptnUrlMovie.matcher(sContent);
//      while(m.find())
//      {
//         String s = m.group();
//         alUrlMovies.add(s);
//      }
//   
//      return alUrlMovies;
//   }

//   @Override
//   protected String inBackgroundHttpParse(String sURL) throws Exception
//   {
//      if(àrenaBGSettings.sCookieUID == null || 
//         àrenaBGSettings.sCookieUID.isEmpty() || 
//         àrenaBGSettings.sCookiePass == null || 
//         àrenaBGSettings.sCookiePass.isEmpty())
//      {
//         loginArenaBG();
//         saveSettings();
//      }
//      
//
//      String sResponse = getURLResponse(sURL).replace("\n", "");
//      
//      sTitle = getTitle(sResponse);
//
//      if(àrenaBGSettings.bDownloadTorrent)
//      {
//         sTorrent = getTorrentUrl(sURL, sResponse);
//      }
//      if(àrenaBGSettings.bDownloadMagnet)
//      {
//         sMagnet = getMagnet(sResponse);
//      }
//
//      if(àrenaBGSettings.bDownloadImage)
//      {
//         sImage = getImageUrl(sResponse).replace("https", "http");
//      }
//
//      if(àrenaBGSettings.bDownloadDescription)
//      {
//         sDescription = getDescription(sResponse).replaceAll("<br[\\s]*/>", "\n")
//                                                 .replace("&nbsp;", " ")
//                                                 .replaceAll("<.*?>", "")
//                                                 .replaceAll("\n\n", "\n")
//                                                 .trim();
//      }
//
//      if(àrenaBGSettings.bDownloadSubtitles)
//      {
//         downloader.checkContetsVsPlugins(sTitle.replace("/", "").trim(), sResponse);
//      }
//      
//      return sTitle;
//   }

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
//
//            sFolderName = oMovie.getName().substring(0, oMovie.getName().lastIndexOf(File.separator));
//            
//            if(oMovie.getName().endsWith(File.separator))
//               f = new File(sDownloadFolder + File.separator + oMovie.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator) + 1));
//            else
//               f = new File(sDownloadFolder + File.separator + oMovie.getName());
//
//            f.getParentFile().mkdirs();
//            File source = new File(saveFilePath);
//            Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
//            FileOutputStream fos; 
//            if(oMovie.getMagnet() != null && !oMovie.getMagnet().isEmpty())
//            {
//               f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + MAGNET_FILE);
//               f.getParentFile().mkdirs();
//               f.createNewFile();
//               fos= new FileOutputStream(f);
//               fos.write(oMovie.getMagnet().getBytes());
//               fos.close();
//            }
//            f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + INFO_FILE);
//            f.createNewFile();
//            fos = new FileOutputStream(f);
//            fos.write(oMovie.getInfo().getBytes());
//            fos.close();
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
//         e.printStackTrace();
//      }
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
//            àrenaBGSettings = (MovieSettings)jaxbUnmarshaller.unmarshal(file);
//         }
//         else
//         {
//            àrenaBGSettings = new MovieSettings();
//            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
//            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            jaxbMarshaller.marshal(àrenaBGSettings, file);
//         }
//      } 
//      catch(JAXBException e)
//      {
//         e.printStackTrace();
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
//         jaxbMarshaller.marshal(àrenaBGSettings, file);
//         
//      } catch(JAXBException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//   }
   
//   private void loginArenaBG()
//   {
//      String urlParameters  = String.format("username=%s&password=%s", àrenaBGSettings.sUser, àrenaBGSettings.sPassword);
//      String request        = HTTPS + WWW + DOMAIN + "users/login/";
//      URL url;
//      BufferedReader in;
//      String sResponse;
//      try
//      {
//         url = new URL( request );
//         HttpURLConnection conn= (HttpURLConnection) url.openConnection();
//         
//         conn.setRequestMethod("POST");
//         conn.setDoOutput(true);
//         conn.setUseCaches(false);
//         conn.setInstanceFollowRedirects(false);
//         conn.setRequestProperty("Host", "www.arenabg.com");
//         conn.setRequestProperty("User-Agent", "Mozilla/5.0");
//         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//         conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//         conn.setRequestProperty("Referer", HTTPS + WWW + DOMAIN);
//         conn.setRequestProperty("Connection", "keep-alive");
//         conn.setDoInput(true);
//
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
//                     àrenaBGSettings.sCookieUID = cookieValue;
//                  else if(cookieName.equals(COOKIE_PASS_NAME))
//                     àrenaBGSettings.sCookiePass = cookieValue;
//                     
//                  System.out.println(cookie);
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
//
//            sResponse = sbResponse.toString();
//            
//            System.out.print(sResponse);            
//         }
//         
//      } 
//      catch(MalformedURLException e)
//      {
//         e.printStackTrace();
//      } 
//      catch(ProtocolException e)
//      {
//         e.printStackTrace();
//      } 
//      catch(IOException e)
//      {
//         e.printStackTrace();
//      }
//   }
   
   @Override
   protected Pattern getUrlPattern()
   {
      return ptnUrlMovie;
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
   protected Pattern getTorrentUrlPattern()
   {
      return ptnTorrent;
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
   protected MovieSettings getMovieSettings()
   {
      return àrenaBGSettings;
   }

   @Override
   protected String getMoviesSettingsFile()
   {
      return SETTINGS_FILE;
   }

   @Override
   protected void setMoviesSettings(MovieSettings movieSettings)
   {
      àrenaBGSettings = movieSettings;
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
