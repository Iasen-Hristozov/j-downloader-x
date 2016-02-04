package com.discworld.jdownloaderx.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.CRPTDump;
import com.discworld.jdownloaderx.dto.IDownloader;

public class SlaviShow extends Plugin
{
   private final static String DOMAIN = "slavishow.com",
                               RTMP_DUMP_CMD = "rtmpdump.exe -v -r \"rtmp://video.slavishow.com/slavishow/\" -a \"slavishow/\" -f \"WIN 13,0,0,214\" -W \"http://www.slavishow.com/content/themes/slavishow/swf/flowplayer.commercial-3.2.18.swf\" -p \"%s\" -y \"slavishow/%s\" --tcUrl \"rtmp://video.slavishow.com/slavishow/\" -R --buffer 2000 -o \"%s%s\"",
                               RTMP_DUMP_PATH = "plugins" + File.separator,
                               lat = "A B V G D E J Z I Y K L M N O P R S T U F H C Ch Sh Sht Y Yu Ya a b v g d e j z i y k l m n o p r s t u f h c ch sh sht y y y yu ya",
                               cyr = "\u0410 \u0411 \u0412 \u0413 \u0414 \u0415 \u0416 \u0417 \u0418 \u0419 \u041A \u041B \u041C \u041D \u041E \u041F \u0420 \u0421 \u0422 \u0423 \u0424 \u0425 \u0426 \u0427 \u0428 \u0429 \u042A \u042E \u042F \u0430 \u0431 \u0432 \u0433 \u0434 \u0435 \u0436 \u0437 \u0438 \u0439 \u043A \u043B \u043C \u043D \u043E \u043F \u0440 \u0441 \u0442 \u0443 \u0444 \u0445 \u0446 \u0447 \u0448 \u0449 \u044A \u044B \u044C \u044E \u044F";


   private final static Pattern ptnName = Pattern.compile("www\\.slavishow\\.com/(.+?)/"),
//                                ptnMp4 = Pattern.compile("\"url\":\"slavishow/(.+?)\","),
                                ptnMp4 = Pattern.compile("slavishow/(.+?\\.mp4)\""),
                                ptnPrg = Pattern.compile("\\d{1,6}\\.\\d{3} kB / \\d{1,5}\\.\\d{2} sec \\((\\d{1,3})\\.\\d{1}%\\)");   
   
   
   private String              sName,
                               sNameLat,
                               sMP4, 
                               sURL,
                               sURLEnc;
   
   private RTMPDumpThread      oRTMPDumpThread;
   
   
   public SlaviShow()
   {
      super();
   }
   
   public SlaviShow(IDownloader oDownloader)
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
   
      Matcher m = ptnName.matcher(sContent);
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
      String sResponse = null;
      try
      {
         this.sURL = sURL; 
         
         Matcher oMatcher = ptnName.matcher(sURL);
         if(!oMatcher.find())
            return null;
         
         sName = oMatcher.group(1);
         
         String sNameEnc = URLEncoder.encode(sName, "UTF-8");
         
         sURLEnc = HTTP + WWW + DOMAIN + "/" + sNameEnc + "/";
         
         sResponse = getHttpResponse(sURLEnc);

         oMatcher = ptnMp4.matcher(sResponse);
         if(!oMatcher.find())
            return null;
         
         sMP4 = oMatcher.group(1);
         
         int iDateEnd = sMP4.indexOf("_") >= 0 ? sMP4.indexOf("_") : sMP4.indexOf(".");
         
         String sDate = sMP4.substring(0, iDateEnd);

         sName = sDate + "_" + sName;

         sNameLat = cyr2lat(sName);
         
      } 
      catch(UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }

      return sResponse;
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();

      CFile oMovie = new CRPTDump(sNameLat+".flv", sURL, sMP4, sURLEnc); 
      vFilesFnd.add(oMovie);
      
      return vFilesFnd;
   }

   @Override
   public void downloadFile(CFile oFile, String sDownloadFolder)
   {
      if (oFile instanceof CRPTDump) 
      {
         
         File flDownload = new File(sDownloadFolder);
         flDownload.mkdirs();
         
         final String sRTMPDumpCmd = String.format(RTMP_DUMP_CMD, ((CRPTDump)oFile).getURLEnc(), ((CRPTDump)oFile).getMp4(), flDownload.getAbsolutePath() + File.separator, oFile.getName());
         System.out.print(sRTMPDumpCmd);
         
         String sRTMPDump = RTMP_DUMP_PATH + sRTMPDumpCmd;
         
         String cmd[] =
         {
                  "cmd",
                  "/c",
                  sRTMPDump 
         };
         
         oRTMPDumpThread = new RTMPDumpThread(cmd, oDownloader, oFile);
         oRTMPDumpThread.start();
         
         

      }
   }

   @Override
   protected void doneDownloadFile(CFile oFile, String sDownloadFolder, String saveFilePath)
   {
      super.doneDownloadFile(oFile, sDownloadFolder, saveFilePath);
   }

   @Override
   protected void loadSettings()
   {
   }
   
   private static String cyr2lat(String sCyr)
   {
      String[] listLat = lat.split(" ");
      String[] listCyr = cyr.split(" ");

      String sLat = sCyr;
      for(int i = 0; i < listCyr.length; i++)
      {
         sLat = sLat.replaceAll(listCyr[i], listLat[i]);
      }

      return sLat;
   }
   
   class RTMPDumpThread extends Thread
   {
      InputStream  is;
      String       type;
      OutputStream os;

      String[]     cmd;
      IDownloader oDownloader;
      CFile oFile;
      
      StreamGobbler errorGobbler, outputGobbler;

      Process p;
      
      RTMPDumpThread(String[] cmd, IDownloader oDownloader, CFile oFile)
      {
         this.cmd = cmd;
         this.oDownloader = oDownloader;
         this.oFile = oFile;
      }

      public void run()
      {
         try
         {
            p = Runtime.getRuntime().exec(cmd);

            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), oDownloader, oFile);

            // any output?
            // StreamGobbler(p.getInputStream(), "OUTPUT", fos);
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), oDownloader, oFile);

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            p.waitFor();
//            int exitVal = p.waitFor();
//            System.out.println("ExitValue: " + exitVal);
         } 
         catch(IOException ioe)
         {
            ioe.printStackTrace();
         } 
         catch(InterruptedException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         finally
         {
            oDownloader.deleteFile(oFile);
            oDownloader.saveFiles();
         }
      }
   }
   
   class StreamGobbler extends Thread
   {
      InputStream  is;
      String       type = null;
      OutputStream os = null;
      IDownloader  oDownloader;
      CFile        oFile;
      
      int          iPrgPos = -1,
                   iLen;

      StreamGobbler(InputStream is)
      {
         this.is = is;
      }

      StreamGobbler(InputStream is, IDownloader oDownloader, CFile oFile)
      {
         this(is);
         this.oDownloader = oDownloader; 
         this.oFile = oFile;
      }
      
      
      StreamGobbler(InputStream is, String type)
      {
         this(is);
         this.type = type;
      }

      StreamGobbler(InputStream is, String type, OutputStream redirect)
      {
         this.is = is;
         this.type = type;
         this.os = redirect;
      }

      public void run()
      {
         try
         {
//            PrintWriter pw = null;
//            if(os != null)
//               pw = new PrintWriter(os);

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while((line = br.readLine()) != null)
            {
               Matcher oMatcher = ptnPrg.matcher(line);
               if(oMatcher.find())
               {
                  String sPrg = oMatcher.group(1);
                  int iPrg = Integer.valueOf(sPrg);
                  oDownloader.setFileProgress(oFile, iPrg);
//                  System.out.print(line+"\r");
               }
               else
                  System.out.println(line);
            }
//            if(pw != null)
//               pw.flush();
         } 
         catch(IOException ioe)
         {
            ioe.printStackTrace();
         }
      }
   }   
}
