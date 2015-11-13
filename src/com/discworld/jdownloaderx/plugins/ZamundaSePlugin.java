package com.discworld.jdownloaderx.plugins;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
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
import com.discworld.jdownloaderx.dto.IDownloader;

public class ZamundaSePlugin extends Plugin
{
   public final static String DOMAIN = "zelka.org";

   private final static String AUTHOR_BGN = "<span itemscope itemtype=\"http://schema\\.org/Person\"><a href=\"/person/[\\w\\-]+\" itemprop=\"name\" data-edit=\"/admin/person/\\d+/edit\">",
            MAGNET_BGN = "<a href='magnet:",
            MAGNET_END = "'>",
//            MAGNET = "<a href='magnet:\\?xt=urn:btih:[\\w]*'>",
            MAGNET = "magnet:\\?xt=urn:btih:[\\w]*",
            
            AUTHOR_END = "</a></span>",
            TITLE_BGN = "<h1>",
            TITLE_END = "<[\\s]*/h1>",
            TITLE = TITLE_BGN + ".*" +TITLE_END,
            TORRENT_BGN = "download.php/",
            TORRENT_END = ".torrent",
            TORRENT = TORRENT_BGN + ".*" +TORRENT_END,
            IMAGE_BGN = "Описание </td><td valign=\"top\" align=left><div id=description><div align=center><img border=\"0\" src=\"",
            IMAGE_END = "\">",
            IMAGE = IMAGE_BGN + ".*" + IMAGE_END,
            DESCRIPTION_BGN = "(Описание)",
            DESCRIPTION_END = "((Свали Субтитри)|(Видео)|(NFO))",
            DESCRIPTION = DESCRIPTION_BGN + "(.*?)" + DESCRIPTION_END, 
            VOLUME_BGN = "<h2><span>",
            VOLUME_END = "</span></h2>",
            URL_DWN_BGN = "http://" + DOMAIN,
            LNKS_BGN = "<div class=\"tab-pane expanded-links text-links\" id=\"text-links\">",
            URL_FB2 = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 fb2.zip\" class=\"(btn btn-default )?dl dl-fb2 action\"><span( class=\"sr-only\")?>fb2.zip</span>",
            URL_EPUB = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 epub\" class=\"(btn btn-default )?dl dl-epub action\"><span( class=\"sr-only\")?>epub</span>",
            URL_TXT = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 txt.zip( \\(.*\\))?\" class=\"(btn btn-default )?dl dl-txt action\"><span( class=\"sr-only\")?>txt.zip</span>",
            URL_SFB = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 sfb.zip\" class=\"(btn btn-default )?dl dl-sfb action\"><span( class=\"sr-only\")?>sfb.zip</span>",
            URL_PDF = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 pdf\" class=\"(btn btn-default )?dl dl-pdf action\"><span( class=\"sr-only\")?>pdf</span>",
            URL_DJVU = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 djvu\" class=\"(btn btn-default )?dl dl-djvu action\"><span( class=\"sr-only\")?>djvu</span>",
            URL_BGN = "<a href=\"",
            URL = "(http://)?zelka\\.org/details\\.php\\?id=(\\d)*",
            URL_END = "\"",
            EXT_FB2 = ".fb2",
            EXT_EPUB = ".epub",
            EXT_SFB = ".sfb",
            EXT_TXT = ".txt",
            EXT_PDF = ".pdf",
            EXT_DJVU = ".djvu",
            EXTS[] =
            { EXT_FB2, EXT_EPUB, EXT_TXT, EXT_SFB, EXT_PDF, EXT_DJVU },
            URLS[] =
            { URL_FB2, URL_EPUB, URL_TXT, URL_SFB, URL_PDF, URL_DJVU },
            COOKIE_UID_NAME = "uid",
            COOKIE_PASS_NAME = "pass"; 

   private boolean             bDownloadFB2 = true, bDownloadEPUB = true,
            bDownloadSFB = false, bDownloadTXT = false, bDownloadPDF = true,
            bDownloadDJVU = true, bDownloads[] =
            { bDownloadFB2, bDownloadEPUB, bDownloadTXT, bDownloadSFB,
            bDownloadPDF, bDownloadDJVU };
   
   private ZamundaSeSettings oZamundaSeSettings;

   private String              sAuthor, 
                               sTitle, 
                               sVolume,
                               sUrls[] = new String[URLS.length],
                               sMagnet,
                               sTorrent,
                               sImage,
                               sDescription;

   private Pattern             ptnAuthotBgn = Pattern.compile(AUTHOR_BGN),
                               ptnTitleBgn = Pattern.compile(TITLE_BGN), 
                               ptnUrlFb2 = Pattern.compile(URL_FB2), 
                               ptnUrlEpub = Pattern.compile(URL_EPUB),
                               ptnUrlTxt = Pattern.compile(URL_TXT), 
                               ptnUrlSfb = Pattern.compile(URL_SFB), 
                               ptnUrlPdf = Pattern.compile(URL_PDF),
                               ptnUrlDjvu = Pattern.compile(URL_DJVU), 
                               ptnUrls[] = { ptnUrlFb2, ptnUrlEpub, ptnUrlTxt, ptnUrlSfb, ptnUrlPdf, ptnUrlDjvu }, 
                               ptnUrlBook = Pattern.compile(URL),
                               ptnMagnet = Pattern.compile(MAGNET),
                               ptnTitle = Pattern.compile(TITLE),
                               ptnTorrent = Pattern.compile(TORRENT),
                               ptnImage = Pattern.compile(IMAGE),
                               ptnDescription = Pattern.compile(DESCRIPTION);


   public ZamundaSePlugin(IDownloader oDownloader)
   {
      super(oDownloader);
   }

   @Override
   protected void doneDownloadFile(CFile oFile, String sDownloadFolder,
            String saveFilePath)
   {
      super.doneDownloadFile(oFile, sDownloadFolder, saveFilePath);
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
      }

//      String sResponse = super.inBackgroundHttpParse(sURL);
      
      String sResponse = getZelka(sURL);
      
      sMagnet = sFindString(sResponse, MAGNET_BGN , MAGNET_END);
      
      Matcher oMatcher = ptnMagnet.matcher(sResponse);
      if(oMatcher.find())
      {
         sMagnet = oMatcher.group();
         System.out.println(sMagnet);
         for(int i = 0;  i < oMatcher.groupCount(); i++)
         {
            System.out.println(oMatcher.group(i));
         }
      }
      
      oMatcher = ptnTitle.matcher(sResponse);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group();
         sTitle = sTitle.replaceAll(TITLE_BGN, "").replaceAll(TITLE_END, "").replace("/", "");
         System.out.println(sTitle);
      }

      oMatcher = ptnTorrent.matcher(sResponse);
      if(oMatcher.find())
      {
         sTorrent = oMatcher.group();
         System.out.println(sTorrent);
      }
      
      oMatcher = ptnImage.matcher(sResponse);
      if(oMatcher.find())
      {
         sImage = oMatcher.group();
         sImage = sImage.replaceAll(IMAGE_BGN, "").replaceAll(IMAGE_END, "");
         System.out.println(sImage);
      }

      String sReponse1 = sResponse.replace("\n", "");
      oMatcher = ptnDescription.matcher(sReponse1);
      if(oMatcher.find())
      {
         sDescription = oMatcher.group(2);
         sDescription = sDescription.replace("<br />", "\n").replaceAll("<.*?>", "");
         System.out.println(sDescription);
      }
      
      
      String sAuthorTitle = sResponse.substring(sResponse.indexOf("<h1>"), sResponse.indexOf("</h1>"));

      // sAuthor = sFindString(sResponse, ptnAuthotBgn, AUTHOR_END);

      ArrayList<String> alAuthors = sFindStrings(sAuthorTitle, ptnAuthotBgn, AUTHOR_END);
      sAuthor = String.join(", ", alAuthors);

      // sTitle = sFindString(sResponse, BOOK_BGN, BOOK_END);
      //
      sTitle = sFindString(sAuthorTitle, ptnTitleBgn, TITLE_END);

      sVolume = sFindString(sResponse, VOLUME_BGN, VOLUME_END);

      String sLinks = sResponse
               .substring(sResponse.indexOf(LNKS_BGN) != -1 ? sResponse
                        .indexOf(LNKS_BGN) : 0);

      // String sUrls[] = new String[URLS.length];
      for(int i = 0; i < URLS.length; i++)
         sUrls[i] = sFindString(sLinks, ptnUrls[i], URL_BGN, URL_END);

      String sFileName = (sAuthor != null && !sAuthor.isEmpty() ? sAuthor
               + " - " : "")
               + sTitle
               + (sVolume != null && !sVolume.isEmpty() ? ". " + sVolume : "");

      sFileName = sFileName.replaceAll("[?]", ".").replace(":", " - ")
               .replace("<br>", "").replace("\n", ". ");
      // sFileName = sFileName.replaceAll("[:]", " - ");
      // sFileName = sFileName.replaceAll("<br>", "");
      if(sFileName.endsWith("."))
         sFileName = sFileName.substring(0, sFileName.length() - 1);
      if(sFileName.length() > 250)
         sFileName = sFileName.substring(0, 247) + "...";

      // Book oBook = null;

      // for(int i = 0; i < URLS.length; i++)
      // {
      // if(bDownloads[i] && sUrls[i] != null && !sUrls[i].trim().isEmpty())
      // {
      // oBook = new Book(sFileName + EXTS[i], URL_DWN_BGN + sUrls[i]);
      // oDownloader.addFile(oBook);
      // // vFilesFnd.add(oBook);
      // }
      // }

      return sFileName;
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      CFile oBook = null;
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
      for(int i = 0; i < URLS.length; i++)
      {
         if(bDownloads[i] && sUrls[i] != null && !sUrls[i].trim().isEmpty())
         {
            oBook = new CFile(sResult + EXTS[i], URL_DWN_BGN + sUrls[i]);
            // oDownloader.addFile(oBook);
            vFilesFnd.add(oBook);
         }
      }

      return vFilesFnd;
   }

   @Override
   public ArrayList<String> parseClipboard(String sContent)
   {
      Pattern ptnUrlBook = Pattern.compile(URL);
      ArrayList<String> alUrlBooks = new ArrayList<String>();

      Matcher m = ptnUrlBook.matcher(sContent);
      while(m.find())
      {
         String s = m.group();
         alUrlBooks.add(s);
      }

      return alUrlBooks;
   }

   @Override
   public String getDomain()
   {
      return DOMAIN;
   }

   @Override
   protected void loadSettings()
   {
      try
      {
         JAXBContext jaxbContext = JAXBContext.newInstance(ZamundaSeSettings.class);
         
         File file = new File("zamunda_se.xml");
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
   
   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "", propOrder = {"bDownloadTorrent","bDownloadMagnet","bDownloadImage","bDownloadDescription","sUser","sPassword","sCookieUID","sCookiePass"})
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
      final String USER_AGENT = "Mozilla/5.0";

      String sResponse = null;

      URL oURL;

      BufferedReader in;

      HttpURLConnection oHTTPConn;

      try
      {
         oURL = new URL(sURL);
         oHTTPConn = (HttpURLConnection) oURL.openConnection();

         // optional default is GET
         oHTTPConn.setRequestMethod("GET");

         // add reuqest header
         oHTTPConn.setRequestProperty("User-Agent", USER_AGENT);
         
         String myCookies = COOKIE_UID_NAME + "=" + oZamundaSeSettings.sCookieUID + "; " + COOKIE_PASS_NAME + "=" + oZamundaSeSettings.sCookiePass;
         oHTTPConn.setRequestProperty("Cookie", myCookies);
//         oHTTPConn.connect();
         if(oHTTPConn.getResponseCode() == 200)
         {
//            in = new BufferedReader(new InputStreamReader(oHTTPConn.getInputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(oHTTPConn.getInputStream(), "Cp1251"));

            String inputLine;
            StringBuffer sbResponse = new StringBuffer();

            while((inputLine = in.readLine()) != null)
               sbResponse.append(inputLine + "\n");
            in.close();

            sResponse = sbResponse.toString();
            
            System.out.print(sResponse);
         }
      } catch(MalformedURLException e)
      {
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
      
      return sResponse;
   }   
}
