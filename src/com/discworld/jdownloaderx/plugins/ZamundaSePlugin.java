package com.discworld.jdownloaderx.plugins;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.IDownloader;

public class ZamundaSePlugin extends Plugin
{
   public final static String DOMAIN = "zelka.org";
   // DOMAIN = "chitanka.it-tali.net",

   private final static String AUTHOR_BGN = "<span itemscope itemtype=\"http://schema\\.org/Person\"><a href=\"/person/[\\w\\-]+\" itemprop=\"name\" data-edit=\"/admin/person/\\d+/edit\">",
            MAGNET_BGN = "<a href='magnet:",
            MAGNET_END = "'>",
            MAGNET = "<a href='magnet:\\?xt=urn:btih:[\\w]*'>",
            AUTHOR_END = "</a></span>",
            TITLE_BGN = "<a class=\"selflink\" itemprop=\"name\" data-edit=\"/admin/((\\bbook\\b)|(\\btext\\b))/\\d+/edit\">",
            TITLE_END = "</a>",
            BOOK_BGN = "<i itemprop=\"name\">",
            BOOK_END = "</i>",
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
            { URL_FB2, URL_EPUB, URL_TXT, URL_SFB, URL_PDF, URL_DJVU };

   private boolean             bDownloadFB2 = true, bDownloadEPUB = true,
            bDownloadSFB = false, bDownloadTXT = false, bDownloadPDF = true,
            bDownloadDJVU = true, bDownloads[] =
            { bDownloadFB2, bDownloadEPUB, bDownloadTXT, bDownloadSFB,
            bDownloadPDF, bDownloadDJVU };

   private String              sAuthor, sTitle, sVolume,
            sUrls[] = new String[URLS.length];;

   private Pattern             ptnAuthotBgn = Pattern.compile(AUTHOR_BGN),
            ptnTitleBgn = Pattern.compile(TITLE_BGN), ptnUrlFb2 = Pattern
                     .compile(URL_FB2), ptnUrlEpub = Pattern.compile(URL_EPUB),
            ptnUrlTxt = Pattern.compile(URL_TXT), ptnUrlSfb = Pattern
                     .compile(URL_SFB), ptnUrlPdf = Pattern.compile(URL_PDF),
            ptnUrlDjvu = Pattern.compile(URL_DJVU), ptnUrls[] =
            { ptnUrlFb2, ptnUrlEpub, ptnUrlTxt, ptnUrlSfb, ptnUrlPdf,
            ptnUrlDjvu }, ptnUrlBook = Pattern.compile(URL);

   public ZamundaSePlugin()
   {
   }

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
      String sResponse = super.inBackgroundHttpParse(sURL);

      String sMagnet = sFindString(sResponse, MAGNET_BGN , MAGNET_END);
      
      String sAuthorTitle = sResponse.substring(sResponse.indexOf("<h1>"),
               sResponse.indexOf("</h1>"));

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
      // TODO Auto-generated method stub
      
   }
}
