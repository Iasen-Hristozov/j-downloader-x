package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.Plugin;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class Bukvi extends Plugin
{
   private final static String DOMAIN = "bukvi.";
   
   private final static Pattern ptnTitle = Pattern.compile("<h1><title>(.+?) - \u041a\u0430\u0442\u0430\u043b\u043e\u0433 \u0441\u0443\u0431\u0442\u0438\u0442\u0440\u0438 \u002d \u041a\u0430\u0447\u0432\u0430\u043d\u0435 \u043d\u0430 \u043d\u043e\u0432 \u0444\u0430\u0439\u043b\u0021 \u002d \u0042\u0075\u006b\u0076\u0069\u0042\u0047 \u002d \u0042\u0075\u006c\u0067\u0061\u0072\u0069\u0061\u006e \u0054\u0072\u0061\u006e\u0073\u006c\u0061\u0074\u006f\u0072 \u005a\u006f\u006e\u0065\u0021</title></h1>"),
//                                ptnURL = Pattern.compile("<a href=\"(.+?)\" onmouseover=\"return overlib('\u0421\u0432\u0430\u043b\u0438 \u0441\u0443\u0431\u0442\u0438\u0442\u0440\u0438\u0442\u0435');\"");
                                ptnUrl = Pattern.compile("((http:\\/\\/)?bukvi.bg\\/load\\/\\d+(\\/\\w+)?(/[\\d\\-]+)?)"),
//                                ptnUrlFile = Pattern.compile("<a href=\"((http://)?bukvi.bg/load/[\\d\\-]+)\" onmouseover=\"return overlib\\('\u0421\u0432\u0430\u043b\u0438 \u0441\u0443\u0431\u0442\u0438\u0442\u0440\u0438\u0442\u0435\'\\);\"");
                                ptnUrlFile = Pattern.compile("<a href=\\\"((http:\\/\\/)?bukvi(.mmcenter)?.bg\\/load\\/[\\d\\-]+)\\\"");
   
   
   private String              sTitle,
                               sUrl;
   
   static {
      PluginFactory.registerPlugin("bukvi.bg", new Bukvi(DownloaderPassClass.getDownloader()));
      PluginFactory.registerPlugin("bukvi.mmcenter.bg", new Bukvi(DownloaderPassClass.getDownloader()));
   }
   
   public Bukvi()
   {
      super();
   }
   
   public Bukvi(IDownloader oDownloader)
   {
      super(oDownloader);
   }

//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }

   @Override
   public ArrayList<String> parseContent(String sContent)
   {
      ArrayList<String> alUrlMovies = new ArrayList<String>();
   
      Matcher m = ptnUrl.matcher(sContent);
      while(m.find())
      {
         String s = m.group();
         alUrlMovies.add(s);
      }
   
      return alUrlMovies;
   }

   @Override
   protected String inBackgroundHttpParse(String sURL) throws Exception
   {
      String sBukviResponse = getHttpResponse(sURL);
   
      if(sBukviResponse != null)
      {
         Matcher oMatcher = ptnUrlFile.matcher(sBukviResponse);
         if(oMatcher.find())
            sUrl = oMatcher.group(1);
      }
      
      return sBukviResponse;
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      sResult = sResult.replace("\n", "");
   
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();

//      Matcher oMatcher = ptnUrl.matcher(sResult);
//      if(oMatcher.find())
//         sUrl = oMatcher.group();
////         sUrl = oMatcher.group(1);
      
      Matcher oMatcher = ptnTitle.matcher(sResult);
      if(oMatcher.find())
         sTitle = oMatcher.group(1);
         sTitle = sTitle.replace("/", "-") + ".rar";
   
      vFilesFnd.add(new CFile(sTitle, sUrl));
   
      return vFilesFnd;
   }

   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
      alHttpProperties.add(new SHttpProperty("Referer", oFile.getURL()));
      
      new DownloadFileThread(oFile, sDownloadFolder, alHttpProperties).execute();
   }

   @Override
   protected void downloadFileDone(CFile file, String sDownloadFolder, String saveFilePath)
   {
      super.downloadFileDone(file, sDownloadFolder, saveFilePath);
      try
      {
         super.moveFileToSavePath(file, sDownloadFolder, saveFilePath);
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
   }
   
   @Override
   protected Pattern getUrlPattern()
   {
      return ptnUrl;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      return ptnUrlFile;
   }
   
}
