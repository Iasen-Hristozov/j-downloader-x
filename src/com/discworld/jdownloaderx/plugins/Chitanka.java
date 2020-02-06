package com.discworld.jdownloaderx.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
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
   private final static int MAX_FILE_LEN = 90;
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
                                URLS[] = {ptnUrlFb2, ptnUrlEpub, ptnUrlTxt, ptnUrlSfb, ptnUrlPdf, ptnUrlDjvu};
   
   private String sAuthor,
                  sTitle,
                  sVolume,
                  sUrls[];
   
   
   private ChitankaSettings chitankaSettings;
   
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
//      sUrls = new String[URLS.length];
      
      String sResponse = super.inBackgroundHttpParse(sURL).replace("\n", "");
      
      String  sAuthorTitle = getAuthorTitle(sResponse);
      
      sAuthor = getAuthor(sAuthorTitle);

      sTitle = getTitle(sAuthorTitle);
      
      sVolume = getVolume(sResponse);

      sUrls = getUrls(sResponse);
      
      String sFileName = getFileName();
      
      return sFileName;   
   }

   protected String getAuthorTitle(String sResponse)
   {
      String sAuthorTitle = null;
      Matcher matcher = ptnAuthorTitle.matcher(sResponse);
      if(matcher.find())
         sAuthorTitle = matcher.group(1);
      return sAuthorTitle;
   }

   protected String getAuthor(String sAuthorTitle)
   {
      String sAuthor = null;
      ArrayList<String> alAuthors = new ArrayList<>();
   
      Matcher matcher = ptnAuthor.matcher(sAuthorTitle);
      String sAuthorTmp;
      while(matcher.find())
      {
         sAuthorTmp = matcher.group(1);
         alAuthors.add(sAuthorTmp);
      }
      sAuthor = String.join(", ", alAuthors);
      return sAuthor;
   }

   protected String getTitle(String sAuthorTitle)
   {
      String sTitle = null;
      Matcher matcher = ptnTitle.matcher(sAuthorTitle);
      if(matcher.find())
         sTitle = matcher.group(2);
      
      return sTitle;
   }

   protected String getVolume(String sResponse)
   {
      String sVolume = null;
      Matcher matcher = ptnVolume.matcher(sResponse);
      if(matcher.find())
         sVolume = matcher.group(1);
      
      return sVolume;
   }

   protected String[] getUrls(String sResponse)
   {
      String sUrls[] = new String[URLS.length];
      
      Matcher matcher = ptnUrlFb2.matcher(sResponse);
      if(matcher.find())
         sUrls[0] = matcher.group(1);
      
      
      for(int i = 0; i < URLS.length; i++)
      {
         matcher = URLS[i].matcher(sResponse);
         if(matcher.find())
            sUrls[i] = matcher.group(1);
      }
      return sUrls;
   }

   protected String getFileName()
   {
      String sFileName = (sAuthor != null && !sAuthor.isEmpty() ? sAuthor + " - " : "") + sTitle + (sVolume != null && !sVolume.isEmpty() ? ". " + sVolume : "");
      
      sFileName = sFileName.replaceAll("[?]", ".")
                           .replace(":", " - ")
                           .replace("<br>", "")
                           .replace("\n", ". ")
                           .replace("&#039;", "'")
                           .replace("&gt;", " ");;
      
      if(sFileName.endsWith("."))
         sFileName = sFileName.substring(0, sFileName.length()-1);
      if(sFileName.length() > MAX_FILE_LEN)
         sFileName = sFileName.substring(0, MAX_FILE_LEN) + "...";
      return sFileName;
   }

   @Override
   protected ArrayList<CFile> doneHttpParse(String sResult)
   {
      Book book = null;
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      for(int i = 0; i < URLS.length; i++)
      {
         if(chitankaSettings.bDownloads[i] && sUrls[i] != null && !sUrls[i].trim().isEmpty())
         {
            book = new Book(sResult + EXTS[i], URL_DWN_BGN + sUrls[i], sAuthor, sTitle, sVolume);
            alFilesFound.add(book);
         }
      }
      
      return alFilesFound;
   }

   @Override
   protected void downloadFileDone(CFile file, 
                                   String sDownloadFolder,
                                   String sSaveFilePath)
   {
//      super.downloadFileDone(oFile, sDownloadFolder, saveFilePath);
      
      downloader.deleteFileFromLists(file);

      downloader.saveFilesList();

      String sSavePath = getSavePath(file, sDownloadFolder);
         
      try
      {
         if(file.getURL().endsWith(".zip"))
            extractFiles(sSaveFilePath, sSavePath);
         else
            FileUtils.renameFile(sSaveFilePath, sSavePath);
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

   protected void extractFiles(String saveFilePath, String sSavePath) throws InterruptedException, ExecutionException, IOException
   {
      File extractFolder = new File(saveFilePath.substring(0, saveFilePath.lastIndexOf(".zip")));
      ExtractFile extractFile = new ExtractFile(saveFilePath, extractFolder.getPath());
      extractFile.execute();
      extractFile.get();
      new File(saveFilePath).delete();
      File file;
      if(extractFolder.listFiles().length == 1)
      {
         file = extractFolder.listFiles()[0];
      
//               Files.move(file.toPath(), new File(sSavePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
         FileUtils.renameFile(file.getPath(), sSavePath);
         FileUtils.deleteFile(extractFolder);
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
         for(int i = 0; i < extractFolder.listFiles(filter).length; i++)
         {
            file = extractFolder.listFiles(filter)[i];
            FileUtils.renameFile(file.getPath(), sSavePath);
         }
      
         FileUtils.renameFile(extractFolder.getPath(), sSavePath);
      }         
   }

   protected String getSavePath(CFile file, String sDownloadFolder)
   {
      String sSavePath;
      if(file instanceof Book)
      {
         Book book = (Book) file;
         sSavePath = sDownloadFolder 
                   + (chitankaSettings.bAuthorFolder ? File.separator + book.getAuthor() : "")
                   + (chitankaSettings.bTitleFolder ? File.separator + book.getTitle() : "")                                  
                   + File.separator 
                   + book.getName();
      }
      else
         sSavePath = sDownloadFolder 
                   + File.separator 
                   + file.getName();
      return sSavePath;
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
            chitankaSettings = (ChitankaSettings)jaxbUnmarshaller.unmarshal(file);
            chitankaSettings.reload();
         }
         else
         {
            chitankaSettings = new ChitankaSettings();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(chitankaSettings, file);
         }
      } 
      catch(JAXBException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
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

   @Override
   public boolean isForCheck()
   {
      return false;
   }
}
