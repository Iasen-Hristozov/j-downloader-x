package com.discworld.jdownloaderx.plugins;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class Yavka extends Plugin
{
   private final static String DOMAIN = "yavka.net";
                               
   private final static Pattern ptnTitle = Pattern.compile("&nbsp;(.+?)<\\/h1>"),
                                ptnURL = Pattern.compile("((http(s)?:\\/\\/)yavka.net\\/subs\\/\\d+\\/\\w{1,3})"),
                                ptnFileURL = Pattern.compile("((http(s)?:\\/\\/)yavka.net\\/subs\\/\\d+\\/\\w{1,3}\\/)");

   static
   {
      PluginFactory.registerPlugin(DOMAIN, new Yavka(DownloaderPassClass.getDownloader()));
   }      
   
   public Yavka()
   {
      super();
   }
   
   public Yavka(IDownloader downloader)
   {
      super(downloader);
   }

   @Override
   protected void loadSettings()
   {}
   
   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      sResult = sResult.replace("\n", "");
      String sUrl = getFileUrl(sResult);
      String sTitle = getTitle(sResult).replace("nbsp;", " ").trim();

      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      alFilesFound.add(new CFile(sTitle, sUrl));

      return alFilesFound;
   }   
   
//   @Override
//   protected void initHttpUrlConnection(HttpURLConnection httpURLConnection,
//                                        ArrayList<SHttpProperty> alHttpProperties) throws ProtocolException
//     {
//        httpURLConnection.setRequestMethod("POST");
//        Matcher matcher = ptnURL.matcher(httpURLConnection.getURL().toString());
//        if(matcher.find())
//        {
//           String sID = matcher.group(4);
//           String sLNG = matcher.group(5);
//           String sParams = String.format("id=%s&lng=%s", sID, sLNG);
//           byte[] postData= sParams.getBytes(StandardCharsets.UTF_8 );
//           try
//           {
//              httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//              httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postData.length));
//              httpURLConnection.setDoOutput(true);
//              httpURLConnection.getOutputStream().write(postData);
//           }
//           catch(IOException e)
//           {
//              // TODO Auto-generated catch block
//              e.printStackTrace();
//           }
//        }
//           
//        createHttpURLConnectionHeader(alHttpProperties, httpURLConnection);
//     }
   
   
   @Override
   protected void prepareHttpRequestHeader(ArrayList<SHttpProperty> alHttpProperties)
   {
      alHttpProperties.add(new SHttpProperty(REQ_PROP_REFERER, HTTP + DOMAIN));
   }

   @Override
   public void downloadFile(CFile file, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty(REQ_PROP_REFERER, HTTP + DOMAIN));
      
      new DownloadFileThread(file, sDownloadFolder, alHttpProperties).execute();
      
   }
   
   @Override
   public String getDomain()
   {
      return DOMAIN;
   }

   @Override
   protected Pattern getUrlPattern()
   {
      return ptnURL;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      return ptnFileURL;
   }

   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }
}
