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
import java.nio.charset.StandardCharsets;
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
   private final static String DOMAIN = "zelka.org";

   private final static String[] DOMAINS = {"zelka.org", "zamunda.se"};
   
   private final static String TITLE = "(<h1>)(.+)(<[\\s]*/h1>)",
                               TITLE_PARTS = "(.+ )(/? )(.* )(\\(\\d+\\))",
                               TORRENT = "download.php/\\S+\\.(torrent?)",
                               MAGNET = "magnet:\\?xt=urn:btih:[\\w]*",
                               IMAGE = "(<div id=description>(<div align=center>)?<img border=\"0\" src=\")(.+?)(\">)",
                               SUBSUNACS = "(<a href=)((http://)?(www\\.)?subsunacs.net/((get\\.php\\?id=\\d+)|(subtitles/.+?)))(( target=_blank)?>)",
                               SUBSUNACS_SUBS = "\"(/subtitles/.+?/)\"",
                               ZELKASUBS = "(<a href=)((http://)?(www\\.)?((zelka.org)|(zamunda.se))/getsubs.php/(.+?))( target=_blank)?>",
                               SUBSSAB = "(<a href=)((http://)?(www\\.)?subs\\.sab\\.bz/index\\.php\\?act=download&amp;attach_id=.+?)((target=_blank)?>)",
                               DESCRIPTION = "(\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435)(.*?)((\u0421\u0432\u0430\u043b\u0438 \u0421\u0443\u0431\u0442\u0438\u0442\u0440\u0438)|(\u0412\u0438\u0434\u0435\u043e)|(NFO))",
                               URL = "(http://)?zelka\\.org/details\\.php\\?id=(\\d)*",
                               COOKIE_UID_NAME = "uid",
                               COOKIE_PASS_NAME = "pass",
                               SETTINGS_FILE = "zamunda_se.xml",
                               MAGNET_FILE = "magnet.txt",
                               INFO_FILE = "info.txt"; 
   
   private ZamundaSeSettings oZamundaSeSettings;

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

   private Pattern             ptnTitle = Pattern.compile(TITLE),
                               ptnTitleParts = Pattern.compile(TITLE_PARTS),
//                               ptnTitleBgn = Pattern.compile(TITLE_BGN), 
                               ptnTorrent = Pattern.compile(TORRENT),
                               ptnMagnet = Pattern.compile(MAGNET),
                               ptnImage = Pattern.compile(IMAGE),
                               ptnDescription = Pattern.compile(DESCRIPTION),
                               ptnSubsunacs = Pattern.compile(SUBSUNACS),
                               ptnSubsunacsSubs = Pattern.compile(SUBSUNACS_SUBS),
                               ptnZelkasubs = Pattern.compile(ZELKASUBS),
                               ptnSubssab = Pattern.compile(SUBSSAB);
   
   private MovieTorrent        oMovieTorrent = null;
   
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
   protected void doneDownloadFile(CFile oFile, String sDownloadFolder, String saveFilePath)
   {
      super.doneDownloadFile(oFile, sDownloadFolder, saveFilePath);
      
      FileUtils.renameFile(saveFilePath, sDownloadFolder + File.separator + oFile.getName());
      if(oFile.getURL().endsWith(".torrent"))
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
   protected String inBackgroundHttpParse(String sURL)
   {
      if(oZamundaSeSettings.sCookieUID == null || 
         oZamundaSeSettings.sCookieUID.isEmpty() || 
         oZamundaSeSettings.sCookiePass == null || 
         oZamundaSeSettings.sCookiePass.isEmpty())
      {
         loginZelka();
         saveSettings();
      }

//      String sResponse = super.inBackgroundHttpParse(sURL);
      
      String sResponse = getZelka(sURL).replace("\n", "");

      Matcher oMatcher = ptnTitle.matcher(sResponse);
      if(oMatcher.find())
      {
//         sTitle = oMatcher.group();
//         sTitle = sTitle.replaceAll(TITLE_BGN, "").replaceAll(TITLE_END, "").replace("/", "");
         sTitle = oMatcher.group(2);
         System.out.println(sTitle);
      }

      if(oZamundaSeSettings.bDownloadTorrent)
      {
         oMatcher = ptnTorrent.matcher(sResponse);
         if(oMatcher.find())
         {
            sTorrent = oMatcher.group();
            System.out.println(sTorrent);
         }
      }
      
      
//      sMagnet = sFindString(sResponse, MAGNET_BGN , MAGNET_END);
      
      if(oZamundaSeSettings.bDownloadMagnet)
      {
         oMatcher = ptnMagnet.matcher(sResponse);
         if(oMatcher.find())
         {
            sMagnet = oMatcher.group();
            System.out.println(sMagnet);
         }
      }

      if(oZamundaSeSettings.bDownloadImage)
      {
         oMatcher = ptnImage.matcher(sResponse);
         if(oMatcher.find())
         {
            sImage = oMatcher.group(3);
            System.out.println(sImage);
         }
      }

      if(oZamundaSeSettings.bDownloadDescription)
      {
         oMatcher = ptnDescription.matcher(sResponse);
         if(oMatcher.find())
         {
            sDescription = oMatcher.group(2);
            sDescription = sDescription.replace("<br />", "\n").replace("&nbsp;", " ").replaceAll("<.*?>", "");
            System.out.println(sDescription);
         }
      }

      if(oZamundaSeSettings.bDownloadSubtitles)
      {
         oMatcher = ptnSubsunacs.matcher(sResponse);
         if(oMatcher.find())
         {
            sSubsunacs = oMatcher.group(2);
            
//            if(sSubsunacs.contains("get.php?id="))
//            {
//               String sSubsuncasResult = getHttpResponse(sSubsunacs);
//               
//               oMatcher = ptnSubsunacsSubs.matcher(sSubsuncasResult);
//               if(oMatcher.find())
//               {
//                  sSubsunacs = oMatcher.group(1);
//               }
//            }
            
            System.out.println(sSubsunacs);
         }
         
         oMatcher = ptnZelkasubs.matcher(sResponse);
         if(oMatcher.find())
         {
            sZelkasubs = oMatcher.group(2);
            System.out.println(sZelkasubs);
         }
   
         oMatcher = ptnSubssab.matcher(sResponse);
         if(oMatcher.find())
         {
            sSubssab = oMatcher.group(2);
            sSubssab = sSubssab.replace("&amp;", "&");
            System.out.println(sSubssab);
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
         for(int i = 0; i <= oMatcher.groupCount(); i++)
         {
            System.out.println(oMatcher.group(i));
         }

         if(oMatcher.groupCount() == 4)
            sFilesName = oMatcher.group(1) + oMatcher.group(4);   
         else if(oMatcher.groupCount() == 2)
            sFilesName = oMatcher.group(1) + oMatcher.group(2);
         
         System.out.println(sFilesName);
      }
      
      sFolderName = sTitle.replace("/", "");
      
      String sTorrentName = sTorrent.substring(sTorrent.lastIndexOf("/")+1);
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
   public ArrayList<String> parseClipboard(String sContent)
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
   
   private void loginZelka()
   {
      
      
      String urlParameters  = "username=Rincewind123&password=suleiman";
      byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
      int    postDataLength = postData.length;
      String request        = "http://zelka.org/takelogin.php";
      URL url;
      BufferedReader in;
      String sResponse;
      try
      {
         url = new URL( request );
//         url = new URL(null, request,new sun.net.www.protocol.https.Handler());
         HttpURLConnection conn= (HttpURLConnection) url.openConnection();
//         java.net.URLConnection conn = new URL(request).openConnection();
//         Map<String, List<String>> headers = conn.getHeaderFields(); 
//         List<String> values = headers.get("Set-Cookie");

//         conn.connect();

         //         conn.setRequestMethod( "POST" );
//         conn.setDoOutput( true );
//         conn.setInstanceFollowRedirects( false );
//         conn.setRequestProperty("User-Agent", "Mozilla/5.0");
//         conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
//         conn.setRequestProperty( "charset", "utf-8");
//         conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
//         conn.setUseCaches( false );
         
         conn.setRequestMethod("POST");
//         conn.setRequestProperty("method", "POST");
         conn.setDoOutput(true);
         conn.setUseCaches(false);
         conn.setInstanceFollowRedirects(false);
         conn.setRequestProperty("Host", "zelka.org");
         conn.setRequestProperty("User-Agent", "Mozilla/5.0");
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
////         conn.setRequestProperty("Content-Length", String.valueOf(postData));
         conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
////         conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
         conn.setRequestProperty("Referer", "http://zelka.org");
         conn.setRequestProperty("Connection", "keep-alive");
         conn.setDoInput(true);
//         conn.getOutputStream().write(postData);
      // Send post request

         conn.setRequestProperty("Content-Length", Integer.toString(urlParameters.length()));
         conn.connect();

         DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
         wr.writeBytes(urlParameters);
         wr.flush();
         wr.close();
         
         if(conn.getResponseCode() == 302)
         {
//            Cookie[] cookies = request.getCookies();
            
//            String headerName=null;
//            for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
//               if (headerName.equals("Set-Cookie")) {                  
//               String cookie = conn.getHeaderField(i);
//               System.out.println(cookie);
//               }
//            }

            
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
                     
                  System.out.println(cookie);
               }
//             for (Cookie cookie : cookies) {
//               if (cookie.getName().equals("cookieName")) {
//                 //do something
//                 //value can be retrieved using #cookie.getValue()
//                }
//              }
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
//         DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
//         wr.write( postData );
//         sResponse = wr.toString();
//         
//         System.out.print(sResponse);
         
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
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = null;
      if(oFile.getURL().endsWith(".torrent"))
      {
         alHttpProperties = new ArrayList<SHttpProperty>();
         String sCookies = COOKIE_UID_NAME + "=" + oZamundaSeSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + oZamundaSeSettings.sCookiePass;
         alHttpProperties.add(new SHttpProperty("Cookie", sCookies));
      }
      new DownloadFile(oFile, sDownloadFolder, alHttpProperties).execute();
   }


   
   @Override
   public boolean isMine(String sURL)
   {
      for(String sDomain : DOMAINS)
         if(sURL.contains(sDomain))
            return true;
      return false;
   }
}
