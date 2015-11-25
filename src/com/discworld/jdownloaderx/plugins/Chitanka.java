package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.ExtractFile;
import com.discworld.jdownloaderx.dto.FileUtils;

public class Chitanka extends Plugin
{
   private final String DOMAIN = "chitanka.info";
//          DOMAIN = "chitanka.it-tali.net",
            
   private final static String 
          DOMAIND = "chitanka.info",
          AUTHOR_BGN = "<span itemscope itemtype=\"http://schema\\.org/Person\"><a href=\"/person/[\\w\\-]+\" itemprop=\"name\" data-edit=\"/admin/person/\\d+/edit\">",
          AUTHOR_END = "</a></span>",
          TITLE_BGN = "<a class=\"selflink\" itemprop=\"name\" data-edit=\"/admin/((\\bbook\\b)|(\\btext\\b))/\\d+/edit\">",
          TITLE_END = "</a>",
          BOOK_BGN = "<i itemprop=\"name\">",
          BOOK_END = "</i>",
          VOLUME_BGN = "<h2><span>",
          VOLUME_END = "</span></h2>",
          URL_DWN_BGN = "http://" + DOMAIND,
          LNKS_BGN = "<div class=\"tab-pane expanded-links text-links\" id=\"text-links\">",
          URL_FB2 = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 fb2.zip\" class=\"(btn btn-default )?dl dl-fb2 action\"><span( class=\"sr-only\")?>fb2.zip</span>",
          URL_EPUB = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 epub\" class=\"(btn btn-default )?dl dl-epub action\"><span( class=\"sr-only\")?>epub</span>",
          URL_TXT = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 txt.zip( \\(.*\\))?\" class=\"(btn btn-default )?dl dl-txt action\"><span( class=\"sr-only\")?>txt.zip</span>",
          URL_SFB = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 sfb.zip\" class=\"(btn btn-default )?dl dl-sfb action\"><span( class=\"sr-only\")?>sfb.zip</span>",
          URL_PDF = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 pdf\" class=\"(btn btn-default )?dl dl-pdf action\"><span( class=\"sr-only\")?>pdf</span>",
          URL_DJVU = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 djvu\" class=\"(btn btn-default )?dl dl-djvu action\"><span( class=\"sr-only\")?>djvu</span>",
          URL_BGN = "<a href=\"",
          URL_BOOK = "http://chitanka\\.info/((book)|(text))/\\d*",
          URL_END = "\"",
          EXT_FB2 = ".fb2",
          EXT_EPUB = ".epub",
          EXT_SFB = ".sfb",
          EXT_TXT = ".txt",
          EXT_PDF = ".pdf",
          EXT_DJVU = ".djvu",
          EXTS[] = {EXT_FB2, EXT_EPUB, EXT_TXT, EXT_SFB, EXT_PDF, EXT_DJVU},
          URLS[] = {URL_FB2, URL_EPUB, URL_TXT, URL_SFB, URL_PDF, URL_DJVU};
   
//   private boolean bDownloadFB2 = true,
//                   bDownloadEPUB = true,
//                   bDownloadSFB = false,
//                   bDownloadTXT = false,
//                   bDownloadPDF = true,
//                   bDownloadDJVU = true,
//                   bDownloads[] = {bDownloadFB2, bDownloadEPUB, bDownloadTXT, bDownloadSFB, bDownloadPDF, bDownloadDJVU};
   
   private String sAuthor,
                  sTitle,
                  sVolume,
                  sUrls[] = new String[URLS.length];;
   
   private Pattern ptnAuthotBgn = Pattern.compile(AUTHOR_BGN),
                   ptnTitleBgn = Pattern.compile(TITLE_BGN),
                   ptnUrlFb2 = Pattern.compile(URL_FB2),
                   ptnUrlEpub = Pattern.compile(URL_EPUB),
                   ptnUrlTxt = Pattern.compile(URL_TXT),
                   ptnUrlSfb = Pattern.compile(URL_SFB),
                   ptnUrlPdf = Pattern.compile(URL_PDF),
                   ptnUrlDjvu = Pattern.compile(URL_DJVU),
                   ptnUrls[] = {ptnUrlFb2, ptnUrlEpub, ptnUrlTxt, ptnUrlSfb, ptnUrlPdf, ptnUrlDjvu},
                   ptnUrlBook = Pattern.compile(URL_BOOK);
   
   private ChitankaSettings oChitankaSettings;
   
//   public ChitankaPlugin()
//   {
//   }
//   
//   public ChitankaPlugin(IDownloader oDownloader)
//   {
//      super(oDownloader);
//   }

   @Override
   protected void doneDownloadFile(CFile oFile, 
                                   String sDownloadFolder,
                                   String saveFilePath)
   {
      super.doneDownloadFile(oFile, sDownloadFolder, saveFilePath);
      
      if(oFile.getURL().endsWith(".zip"))
      {
         File oFolder = new File(saveFilePath.substring(0, saveFilePath.lastIndexOf(".zip")));
         ExtractFile oExtractFile = new ExtractFile(saveFilePath, oFolder.getPath());
         oExtractFile.execute();
         try
         {
            oExtractFile.get();
            new File(saveFilePath).delete();
            if(oFolder.listFiles().length == 1)
            {
               File file = oFolder.listFiles()[0];
            
               Files.move(file.toPath(), new File(sDownloadFolder + File.separator + oFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
               FileUtils.deleteFile(oFolder);
            }
            else
            {
               FileFilter filter = new FileFilter() 
               {
                  @Override
                  public boolean accept(File pathname) 
                  {
                     return pathname.getName().endsWith(".sfb")|| pathname.getName().endsWith(".fb2") || pathname.getName().endsWith(".txt") || pathname.getName().endsWith(".epub");
                  }
               };                  
               for(int i = 0; i < oFolder.listFiles(filter).length; i++)
               {
                  File file = oFolder.listFiles(filter)[i];
                  FileUtils.renameFile(file.getPath(), oFolder.getPath() + File.separator + oFile.getName());
               }
            
               FileUtils.renameFile(oFolder.getPath(), sDownloadFolder + File.separator + oFile.getName());
            }         
         } 
         catch(InterruptedException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } 
         catch(ExecutionException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } 
         catch(IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      else
      {
         FileUtils.renameFile(saveFilePath, sDownloadFolder + File.separator + oFile.getName());
      }                     
   }

   @Override
   protected String inBackgroundHttpParse(String sURL)
   {
      String sResponse = super.inBackgroundHttpParse(sURL);
      
      String sAuthorTitle = sResponse.substring(sResponse.indexOf("<h1>"), sResponse.indexOf("</h1>"));
      
//      sAuthor = sFindString(sResponse, ptnAuthotBgn, AUTHOR_END);
      
      ArrayList<String> alAuthors = sFindStrings(sAuthorTitle, ptnAuthotBgn, AUTHOR_END);
      sAuthor = String.join(", ", alAuthors);
      
//      sTitle = sFindString(sResponse, BOOK_BGN, BOOK_END);
//      
      sTitle = sFindString(sAuthorTitle, ptnTitleBgn, TITLE_END);
      
      sVolume = sFindString(sResponse, VOLUME_BGN, VOLUME_END);
      
      String sLinks = sResponse.substring(sResponse.indexOf(LNKS_BGN) != -1 ? sResponse.indexOf(LNKS_BGN) : 0);
      
//      String sUrls[] = new String[URLS.length];
      for(int i = 0; i < URLS.length; i++)
         sUrls[i] = sFindString(sLinks, ptnUrls[i], URL_BGN, URL_END);
      
      String sFileName = (sAuthor != null && !sAuthor.isEmpty() ? sAuthor + " - " : "") + sTitle + (sVolume != null && !sVolume.isEmpty() ? ". " + sVolume : "");
      
      sFileName = sFileName.replaceAll("[?]", ".")
                           .replace(":", " - ")
                           .replace("<br>", "")
                           .replace("\n", ". ")
                           .replace("&#039;", "'")
                           .replace("&gt;", " ");;
      
//      sFileName = sFileName.replaceAll("[:]", " - ");
//      sFileName = sFileName.replaceAll("<br>", "");
      if(sFileName.endsWith("."))
         sFileName = sFileName.substring(0, sFileName.length()-1);
      if(sFileName.length() > 250)
         sFileName = sFileName.substring(0, 247) + "...";
      
//      Book oBook = null;
      
//      for(int i = 0; i < URLS.length; i++)
//      {
//         if(bDownloads[i] && sUrls[i] != null && !sUrls[i].trim().isEmpty())
//         {
//            oBook = new Book(sFileName + EXTS[i], URL_DWN_BGN + sUrls[i]);
//            oDownloader.addFile(oBook);
////            vFilesFnd.add(oBook);
//         }
//      }
      
      return sFileName;   
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      CFile oBook = null;
      ArrayList<CFile> vFilesFnd = new ArrayList<CFile>();
      for(int i = 0; i < URLS.length; i++)
      {
         if(oChitankaSettings.bDownloads[i] && sUrls[i] != null && !sUrls[i].trim().isEmpty())
         {
            oBook = new CFile(sResult + EXTS[i], URL_DWN_BGN + sUrls[i]);
//            oDownloader.addFile(oBook);
            vFilesFnd.add(oBook);
         }
      }
      
      return vFilesFnd;
   }
   
   @Override
   public ArrayList<String> parseClipboard(String sContent)
   {
      Pattern ptnUrlBook = Pattern.compile(URL_BOOK);
      ArrayList<String> alUrlBooks = new ArrayList<String>();
      
      Matcher m = ptnUrlBook.matcher(sContent);
      while (m.find()) 
      {
          String s = m.group();
          alUrlBooks.add(s);
      }      
      
      return alUrlBooks;
   }

//   @Override
//   public String getDomain()
//   {
//      return DOMAIN;
//   }

   
   @Override
   protected void loadSettings()
   {
      try
      {
         JAXBContext jaxbContext = JAXBContext.newInstance(ChitankaSettings.class);
         
         File file = new File("chitanka.xml");
         if(file.exists())
         {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            oChitankaSettings = (ChitankaSettings)jaxbUnmarshaller.unmarshal(file);
            oChitankaSettings.reload();
         }
         else
         {
            oChitankaSettings = new ChitankaSettings();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(oChitankaSettings, file);
         }
      } 
      catch(JAXBException e1)
      {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }         
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "", propOrder = {"bDownloadFB2","bDownloadEPUB","bDownloadSFB","bDownloadTXT","bDownloadPDF","bDownloadDJVU"})
   @XmlRootElement(name = "settings")
   static private class ChitankaSettings
   {
      @XmlElement(name = "download_fb2", required = true)
      public boolean bDownloadFB2 = true;
      @XmlElement(name = "download_epub", required = true)
      public boolean bDownloadEPUB = true;
      @XmlElement(name = "download_sfb", required = true)
      public boolean bDownloadSFB = false;
      @XmlElement(name = "download_txt", required = true)
      public boolean bDownloadTXT = false;
      @XmlElement(name = "download_pdf", required = true)
      public boolean bDownloadPDF = true;
      @XmlElement(name = "download_djvu", required = true)
      public boolean bDownloadDJVU = true;
      @XmlTransient
      public boolean bDownloads[] = {bDownloadFB2, bDownloadEPUB, bDownloadTXT, bDownloadSFB, bDownloadPDF, bDownloadDJVU};
      
      public void reload() 
      {
         bDownloads[0] = bDownloadFB2;
         bDownloads[1] = bDownloadEPUB;
         bDownloads[2] = bDownloadTXT; 
         bDownloads[3] = bDownloadSFB; 
         bDownloads[4] = bDownloadPDF; 
         bDownloads[5] = bDownloadDJVU;
      }
   }

   
   @Override
   public boolean isMine(String sURL)
   {
      if(sURL.contains(DOMAIN))
         return true;
      else
         return false;
   }
}
