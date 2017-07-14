package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class SubsUnacs extends Plugin
{
   private final static String DOMAIN = "subsunacs.net",
                               HTTP = "http://",
                               HTTPS = "https://",
                               DWN = "http://subsunacs.net/get.php?id=",
                               FILE_NAME = "subsunacs.txt"; 
                               
   private final static Pattern ptnTitle = Pattern.compile("<h1>(.+?)</h1>"),
                                ptnURL = Pattern.compile("<div id=\"buttonBox\"><a href=\"(.+?)\""),
                                ptnID = Pattern.compile("http://(www\\.)?subsunacs\\.net(/){1,2}((subtitles/.+?-)|(info\\.php\\?id=))(\\d+)/?"),
                                ptnURLs = Pattern.compile("<a href=\"(\\/subtitles\\/[\\w\\d_\\-]+\\/)?\"");
   
   private String              sTitle,
                               sUrl;
   
   public SubsUnacs()
   {
      super();
   }
   
   public SubsUnacs(IDownloader oDownloader)
   {
      super(oDownloader);
   }

   @Override
   protected void doneDownloadFile(CFile oFile, String sDownloadFolder, String saveFilePath)
   {
      super.doneDownloadFile(oFile, sDownloadFolder, saveFilePath);
      try
      {
         File f;
         if(oFile.getName().endsWith(File.separator))
            f = new File(sDownloadFolder + File.separator + oFile.getName() + saveFilePath.substring(saveFilePath.lastIndexOf(File.separator)+ 1));
         else
            f = new File(sDownloadFolder + File.separator + oFile.getName());
         f.getParentFile().mkdirs();
         File source = new File(saveFilePath);
         Files.move(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } 
      catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
      Matcher oMatcher = ptnURL.matcher(sResult);
      if(oMatcher.find())
      {
         sUrl = oMatcher.group(1);
         sUrl = "http://" + DOMAIN + sUrl;
      }
      
      oMatcher = ptnTitle.matcher(sResult);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group(1);
         sTitle = sTitle.replace("nbsp;", " ");
      }      

      vFilesFnd.add(new CFile(sTitle, sUrl));

      return vFilesFnd;
   }

   @Override
   public ArrayList<String> parseContent(String sContent)
   {
      ArrayList<String> alUrlMovies = new ArrayList<String>();
      
      Matcher m = ptnURL.matcher(sContent);
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
   }
   
   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
//      alHttpProperties.add(new SHttpProperty("Referer", DOMAIN));
      
      Matcher oMatcher = ptnID.matcher(oFile.getURL());
      if(oMatcher.find())
         oFile.setURL(DWN + oMatcher.group(6));
      if(!oFile.getURL().contains(HTTP) && !oFile.getURL().contains(HTTPS))
         oFile.setURL(HTTP + oFile.getURL());
      
//      new DownloadFile(oFile, sDownloadFolder, alHttpProperties).execute();
      
      String sFolderName = oFile.getName().substring(0, oFile.getName().lastIndexOf(File.separator));
      File f = new File(sDownloadFolder + File.separator + sFolderName + File.separator + FILE_NAME);
      try
      {
         f.getParentFile().mkdirs();
         f.createNewFile();
         FileOutputStream fos = new FileOutputStream(f);
         fos.write(oFile.getURL().getBytes());
         fos.close();
         
         super.doneDownloadFile(oFile, sDownloadFolder, f.getParentFile().toString());
         
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

   @Override
   public boolean isMine(String sURL)
   {
      return sURL.contains(DOMAIN);
   }
}
