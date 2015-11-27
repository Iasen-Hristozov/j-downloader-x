package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.SHttpProperty;

public class SubsSab extends Plugin
{
   private final static String DOMAIN = "subs.sab.bz";
   
   private final static Pattern ptnTitle = Pattern.compile("<big>(<.+?>)?(.+?)</big>"),
                                ptnURL = Pattern.compile("\u0421\u0412\u0410\u041b\u0418 \u0421\u0423\u0411\u0422\u0418\u0422\u0420\u0418\u0422\u0415&nbsp;</a><center><br/><br/><fb:like href=\"(.+?)\"");
   
   private String              sTitle,
                               sUrl;
   
   public SubsSab()
   {
      super();
   }
   
   public SubsSab(IDownloader oDownloader)
   {
      super(oDownloader);
   }

   @Override
   public boolean isMine(String sURL)
   {
      return sURL.contains(DOMAIN) ? true : false;
   }

   @Override
   public ArrayList<String> parseClipboard(String sContent)
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
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      sResult = sResult.replace("\n", "");
   
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
      Matcher oMatcher = ptnURL.matcher(sResult);
      if(oMatcher.find())
      {
         sUrl = oMatcher.group(1);
         System.out.println(sUrl);
      }
      
      oMatcher = ptnTitle.matcher(sResult);
      if(oMatcher.find())
      {
         sTitle = oMatcher.group(oMatcher.groupCount());
         sTitle = sTitle.replaceAll("<.*?>", "");
         System.out.println(sTitle);
      }      
   
      vFilesFnd.add(new CFile(sTitle, sUrl));
   
      return vFilesFnd;
   }

   @Override
      public void downloadFile(CFile oFile, String sDownloadFolder)
      {
         ArrayList<SHttpProperty> alHttpProperties = new ArrayList<SHttpProperty>();
   //      alHttpProperties.add(new SHttpProperty("Referer", "http://" + DOMAIN + "/"));
         alHttpProperties.add(new SHttpProperty("Referer", oFile.getURL()));
   //      alHttpProperties.add(new SHttpProperty("Cookie", "__utma=210296727.525576910.1446566714.1446566714.1446566714.1; __utmc=210296727; __utmz=210296727.1446566714.1.1.utmccn=(direct)|utmcsr=(direct)|utmcmd=(none); __utma=210296727.1474209869.1412840724.1436793912.1436801075.39; __utmz=210296727.1436801075.39.27.utmccn=(referral)|utmcsr=zelka.org|utmcct=/details.php|utmcmd=referral; PHPSESSID=sntt9n9o9v8qkk0t570ph2cpq2; newsubs=73437; _c=y; session_id=bb3c0172dca9110c3d65981ceb8e49f0"));
         
         
         new DownloadFile(oFile, sDownloadFolder, alHttpProperties).execute();
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
   protected void loadSettings()
   {
   }
}
