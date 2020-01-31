package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.discworld.jdownloaderx.PluginFactory;
import com.discworld.jdownloaderx.dto.Book;
import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.ExtractFile;
import com.discworld.jdownloaderx.dto.FileUtils;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.Plugin;

public class Chitanka extends Plugin
{
   private final static String 
          DOMAIN = "chitanka.info",
          URL_DWN_BGN = "http://" + DOMAIN,
          EXT_FB2 = ".fb2",
          EXT_EPUB = ".epub",
          EXT_SFB = ".sfb",
          EXT_TXT = ".txt",
          EXT_PDF = ".pdf",
          EXT_DJVU = ".djvu",
          EXTS[] = {EXT_FB2, EXT_EPUB, EXT_TXT, EXT_SFB, EXT_PDF, EXT_DJVU},
          URL = "<a href=\"(/(book|text)/[\\d\\w\\-\\.]+)\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 %1$s\" class=\"(btn btn-default )?dl dl-%2$s action\"><span( class=\"sr-only\")?>%1$s</span>";

   private final static Pattern ptnAuthor = Pattern.compile("<span itemscope itemtype=\"http://schema\\.org/Person\"><a href=\"/person/[\\w\\-]+\" itemprop=\"name\" data-edit=\"/admin/person/\\d+/edit\">(.+?)</a></span>"),
                                ptnAuthorTitle = Pattern.compile("<h1>(.+?)</h1>"),
                                ptnTitle = Pattern.compile("<a class=\"selflink\" itemprop=\"name\" data-edit=\"/admin/(book|text)/\\d+/edit\">(.+?)</a>"),
                                ptnVolume = Pattern.compile("<h2><span>(.+?)</span></h2>"),
                                ptnUrlBook = Pattern.compile("((http(s?)://)?chitanka\\.info/((book)|(text))/\\d*)"),
                                ptnUrlFb2 = Pattern.compile(String.format(URL, "fb2.zip", "fb2")),
                                ptnUrlEpub = Pattern.compile(String.format(URL, "epub", "epub")),
                                ptnUrlTxt = Pattern.compile(String.format(URL, "txt.zip", "txt")),
                                ptnUrlSfb = Pattern.compile(String.format(URL, "sfb.zip", "sfb")),
                                ptnUrlPdf = Pattern.compile(String.format(URL, "pdf", "pdf")),
                                ptnUrlDjvu = Pattern.compile(String.format(URL, "djvu", "djvu")),
//                                ptnUrlFb2 = Pattern.compile(URL_BGN + URL_TTL + "fb2.zip" + URL_BTN + "fb2" + URL_BTN_END + "fb2.zip" + URL_END),
//                                ptnUrlEpub = Pattern.compile(URL_BGN + URL_TTL + "epub" + URL_BTN + "epub" + URL_BTN_END + "epub" + URL_END),
//                                ptnUrlTxt = Pattern.compile(URL_BGN + URL_TTL + "txt.zip( \\(.*\\))?" + URL_BTN + "txt" + URL_BTN_END + "txt.zip" + URL_END),
//                                ptnUrlSfb = Pattern.compile(URL_BGN + URL_TTL + "sfb.zip" + URL_BTN + "sfb" + URL_BTN_END + "sfb.zip" + URL_END),
//                                ptnUrlPdf = Pattern.compile(URL_BGN + URL_TTL + "pdf" + URL_BTN + "pdf" + URL_BTN_END + "pdf" + URL_END),
//                                ptnUrlDjvu = Pattern.compile(URL_BGN + URL_TTL + "djvu" + URL_BTN + "djvu" + URL_BTN_END + "djvu" + URL_END),
                                URLS[] = {ptnUrlFb2, ptnUrlEpub, ptnUrlTxt, ptnUrlSfb, ptnUrlPdf, ptnUrlDjvu};
   
   private String sAuthor,
                  sTitle,
                  sVolume,
                  sUrls[];
   
   
   private ChitankaSettings oChitankaSettings;
   
   static
   {
      PluginFactory.registerPlugin(DOMAIN, new Chitanka(DownloaderPassClass.getDownloader()));
   }

   public Chitanka()
   {
      super();
   }
   
   public Chitanka(IDownloader downloader)
   {
      super(downloader);
   }
   
//   @Override
//   public boolean isMine(String sURL)
//   {
//      return sURL.contains(DOMAIN);
//   }

   @Override
   protected String inBackgroundHttpParse(String sURL) throws Exception
   {
      sAuthor = null;
      sTitle = null;
      sVolume = null;
      String sAuthorTitle = null;
      ArrayList<String> alAuthors = new ArrayList<>();
      sUrls = new String[URLS.length];
      
      String sResponse = super.inBackgroundHttpParse(sURL).replace("\n", "");
      
      Matcher oMatcher = ptnAuthorTitle.matcher(sResponse);
      if(oMatcher.find())
         sAuthorTitle = oMatcher.group(1);
      
      oMatcher = ptnAuthor.matcher(sAuthorTitle);
      String sAuthorTmp;
      while(oMatcher.find())
      {
         sAuthorTmp = oMatcher.group(1);
         alAuthors.add(sAuthorTmp);
      }
      sAuthor = String.join(", ", alAuthors);

      oMatcher = ptnTitle.matcher(sAuthorTitle);
      if(oMatcher.find())
         sTitle = oMatcher.group(2);
      
      oMatcher = ptnVolume.matcher(sResponse);
      if(oMatcher.find())
         sVolume = oMatcher.group(1);

      oMatcher = ptnUrlFb2.matcher(sResponse);
      if(oMatcher.find())
         sUrls[0] = oMatcher.group(1);
      
      
      for(int i = 0; i < URLS.length; i++)
      {
         oMatcher = URLS[i].matcher(sResponse);
         if(oMatcher.find())
            sUrls[i] = oMatcher.group(1);
      }
      
      String sFileName = (sAuthor != null && !sAuthor.isEmpty() ? sAuthor + " - " : "") + sTitle + (sVolume != null && !sVolume.isEmpty() ? ". " + sVolume : "");
      
      sFileName = sFileName.replaceAll("[?]", ".")
                           .replace(":", " - ")
                           .replace("<br>", "")
                           .replace("\n", ". ")
                           .replace("&#039;", "'")
                           .replace("&gt;", " ");;
      
      if(sFileName.endsWith("."))
         sFileName = sFileName.substring(0, sFileName.length()-1);
      if(sFileName.length() > 90)
         sFileName = sFileName.substring(0, 90) + "...";
      
      return sFileName;   
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
//      CFile oBook = null;
      Book oBook = null;
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      for(int i = 0; i < URLS.length; i++)
      {
         if(oChitankaSettings.bDownloads[i] && sUrls[i] != null && !sUrls[i].trim().isEmpty())
         {
            oBook = new Book(sResult + EXTS[i], URL_DWN_BGN + sUrls[i], sAuthor, sTitle, sVolume);
            alFilesFound.add(oBook);
         }
      }
      
      return alFilesFound;
   }

   @Override
   protected void downloadFileDone(CFile oFile, 
                                   String sDownloadFolder,
                                   String saveFilePath)
   {
      super.downloadFileDone(oFile, sDownloadFolder, saveFilePath);
      String sSavePath;
      if(oFile instanceof Book)
      {
         Book oBook = (Book) oFile;
         sSavePath = sDownloadFolder 
                   + (oChitankaSettings.bAuthorFolder ? File.separator + oBook.getAuthor() : "")
                   + (oChitankaSettings.bTitleFolder ? File.separator + oBook.getTitle() : "")                                  
                   + File.separator 
                   + oBook.getName();
      }
      else
         sSavePath = sDownloadFolder 
                   + File.separator 
                   + oFile.getName();
         
         
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
            
//               Files.move(file.toPath(), new File(sSavePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
               FileUtils.renameFile(file.getPath(), sSavePath);
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
                  FileUtils.renameFile(file.getPath(), sSavePath);
               }
            
               FileUtils.renameFile(oFolder.getPath(), sSavePath);
            }         
         } 
         catch(Exception e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } 
      }
      else
      {
         FileUtils.renameFile(saveFilePath, sSavePath);
      }                     
   }
   
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

//   @XmlAccessorType(XmlAccessType.FIELD)
//   @XmlType(name = "", propOrder = {"bDownloadFB2","bDownloadEPUB","bDownloadSFB","bDownloadTXT","bDownloadPDF","bDownloadDJVU"})
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
      @XmlElement(name = "author_folder", required = true)
      public boolean bAuthorFolder = false;
      @XmlElement(name = "title_folder", required = true)
      public boolean bTitleFolder = false;
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
   protected Pattern getUrlPattern()
   {
      return ptnUrlBook;
   }

   @Override
   protected Pattern getFileUrlPattern()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected Pattern getTitlePattern()
   {
      return ptnTitle;
   }

   @Override
   public String getDomain()
   {
      return DOMAIN;
   }
}
