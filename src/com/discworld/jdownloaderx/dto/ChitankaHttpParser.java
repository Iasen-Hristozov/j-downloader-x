package com.discworld.jdownloaderx.dto;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class ChitankaHttpParser extends HTTPParser
{

   public final static String DOMAIN = "chitanka.info";
//          DOMAIN = "chitanka.it-tali.net",
            
   private final static String 
          AUTHOR_BGN = "<span itemscope itemtype=\"http://schema\\.org/Person\"><a href=\"/person/[\\w\\-]+\" itemprop=\"name\" data-edit=\"/admin/person/\\d+/edit\">",
          AUTHOR_END = "</a></span>",
          TITLE_BGN = "<a class=\"selflink\" itemprop=\"name\" data-edit=\"/admin/((\\bbook\\b)|(\\btext\\b))/\\d+/edit\">",
//          TITLE_BGN = "<a class=\"selflink\" itemprop=\"name\" data-edit=\"/admin/book/\\d+/edit\">",
          TITLE_END = "</a>",
          VOLUME_BGN = "<h2><span>",
          VOLUME_END = "</span></h2>",
          URL_DWN_BGN = "http://" + DOMAIN,
          URL_FB2 = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 fb2.zip\" class=\"(btn btn-default )?dl dl-fb2 action\"><span( class=\"sr-only\")?>fb2.zip</span>",
          URL_EPUB = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 epub\" class=\"(btn btn-default )?dl dl-epub action\"><span( class=\"sr-only\")?>epub</span>",
          URL_TXT = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 txt.zip( \\(.*\\))?\" class=\"(btn btn-default )?dl dl-txt action\"><span( class=\"sr-only\")?>txt.zip</span>",
          URL_SFB = "<a href=\"/((\\bbook\\b)|(\\btext\\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 sfb.zip\" class=\"(btn btn-default )?dl dl-sfb action\"><span( class=\"sr-only\")?>sfb.zip</span>",
//          URL_FB2 = "<a href=\"/((\bbook\b)|(\btext\b))/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 fb2.zip\" class=\"btn btn-default dl dl-fb2 action\"><span class=\"sr-only\">fb2.zip</span>",
//          URL_EPUB = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 epub\" class=\"btn btn-default dl dl-epub action\"><span class=\"sr-only\">epub</span>",
//          URL_TXT = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 txt.zip\" class=\"btn btn-default dl dl-txt action\"><span class=\"sr-only\">txt.zip</span>",
//          URL_SFB = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 sfb.zip\" class=\"btn btn-default dl dl-sfb action\"><span class=\"sr-only\">sfb.zip</span>",
          
//          URL_FB2 = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 fb2.zip\" class=\"dl dl-fb2 action\"><span>fb2.zip</span>",
//          URL_EPUB = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 epub\" class=\"dl dl-epub action\"><span>epub</span>",
//          URL_TXT = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 txt.zip\" class=\"dl dl-txt action\"><span>txt.zip</span>",
//          URL_SFB = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 sfb.zip\" class=\"dl dl-sfb action\"><span>sfb.zip</span>",
          URL_BGN = "<a href=\"",
          URL_END = "\"",
          EXT_FB2 = ".fb2",
          EXT_EPUB = ".epub",
          EXT_SFB = ".sfb",
          EXT_TXT = ".txt";
   
   private boolean bDownloadFB2 = true,
                   bDownloadEPUB = true,
                   bDownloadSFB = false,
                   bDownloadTXT = false;
   
   private String sAuthor,
                  sTitle,
                  sVolume,
                  sUrlFb2,
                  sUrlEpub,
                  sUrlTxt,
                  sUrlSfb;
   
   private Pattern ptnAuthotBgn = Pattern.compile(AUTHOR_BGN),
                   ptnTitleBgn = Pattern.compile(TITLE_BGN),
                   ptnUrlFb2 = Pattern.compile(URL_FB2),
                   ptnUrlEpub = Pattern.compile(URL_EPUB),
                   ptnUrlTxt = Pattern.compile(URL_TXT),
                   ptnUrlSfb = Pattern.compile(URL_SFB);

//   public ChitankaHttpParser(String sURL,
//                             FileURLsTableModel oFileURLsTableModel, 
//                             Vector<Book> vFilesFnd,
//                             JTabbedPane tabbedPane)
//   {
//      super(sURL, oFileURLsTableModel, vFilesFnd, tabbedPane);
//   }
   
   public ChitankaHttpParser(String sURL, 
                             Vector<CFile> vBooksFnd,
                             Runnable rnbOnDone)
   {
      super(sURL, vBooksFnd, rnbOnDone);
   }   
   
   @Override
   protected String doInBackground() throws Exception
   {
      String sResponse = super.doInBackground();
      
//      sAuthor = sFindString(sResponse, ptnAuthotBgn, AUTHOR_END);
      
      ArrayList<String> alAuthors = sFindStrings(sResponse, ptnAuthotBgn, AUTHOR_END);
      sAuthor = String.join(", ", alAuthors);
      
      sTitle = sFindString(sResponse, ptnTitleBgn, TITLE_END);
      
      sVolume = sFindString(sResponse, VOLUME_BGN, VOLUME_END);
      
      sUrlFb2 = sFindString(sResponse, ptnUrlFb2, URL_BGN, URL_END);

      sUrlEpub = sFindString(sResponse, ptnUrlEpub, URL_BGN, URL_END);
      
      sUrlTxt = sFindString(sResponse, ptnUrlTxt, URL_BGN, URL_END);
      
      sUrlSfb = sFindString(sResponse, ptnUrlSfb, URL_BGN, URL_END); 
      
//      DownloadFile oDownloadFile = new DownloadFile(sUrlFb2, DOWNLOAD_FLD);
//      oDownloadFile.execute();
      
//      String sFileName = DOWNLOAD_FLD + File.separator + (sAuthor != null && !sAuthor.isEmpty() ? sAuthor + " - " : "") + sTitle + (sVolume != null && !sVolume.isEmpty() ? ". " + sVolume : "");
      String sFileName = (sAuthor != null && !sAuthor.isEmpty() ? sAuthor + " - " : "") + sTitle + (sVolume != null && !sVolume.isEmpty() ? ". " + sVolume : "");
      
      Book bkFb2 = null,
           bkEpub = null,
           bkTxt = null,
           bkSfb = null;
      
      if(bDownloadFB2 && sUrlFb2 != null && !sUrlFb2.trim().isEmpty())
      {
         bkFb2 = new Book(sFileName + EXT_FB2, URL_DWN_BGN + sUrlFb2);
         vFilesFnd.add(bkFb2);
      }
      if(bDownloadEPUB && sUrlEpub != null && !sUrlEpub.trim().isEmpty())
      {
         bkEpub = new Book(sFileName + EXT_EPUB, URL_DWN_BGN + sUrlEpub);
         vFilesFnd.add(bkEpub);
      }
      if(bDownloadTXT && sUrlTxt != null && !sUrlTxt.trim().isEmpty())
      {
         bkTxt = new Book(sFileName + EXT_TXT, URL_DWN_BGN + sUrlTxt);
         vFilesFnd.add(bkTxt);
      }
      if(bDownloadSFB && sUrlSfb != null && !sUrlSfb.trim().isEmpty())
      {
         bkSfb = new Book(sFileName + EXT_SFB, URL_DWN_BGN + sUrlSfb);
         vFilesFnd.add(bkSfb);
      }
      
      return sFileName;
   }

   @Override
   protected void done()
   {
      super.done();
      
      try
      {
         String sResponse = get();

//         sAuthor = sFindString(sResponse, ptnAuthotBgn, AUTHOR_END);
//         
//         sTitle = sFindString(sResponse, ptnTitleBgn, TITLE_END);
//         
//         sVolume = sFindString(sResponse, VOLUME_BGN, VOLUME_END);
//         
//         sUrlFb2 = sFindString(sResponse, ptnUrlFb2, URL_BGN, URL_END);
//
//         sUrlEpub = sFindString(sResponse, ptnUrlEpub, URL_BGN, URL_END);
//         
//         sUrlTxt = sFindString(sResponse, ptnUrlTxt, URL_BGN, URL_END);
//         
//         sUrlSfb = sFindString(sResponse, ptnUrlSfb, URL_BGN, URL_END); 
//         
////         DownloadFile oDownloadFile = new DownloadFile(sUrlFb2, DOWNLOAD_FLD);
////         oDownloadFile.execute();
//         
////         String sFileName = DOWNLOAD_FLD + File.separator + (sAuthor != null && !sAuthor.isEmpty() ? sAuthor + " - " : "") + sTitle + (sVolume != null && !sVolume.isEmpty() ? ". " + sVolume : "");
//         String sFileName = (sAuthor != null && !sAuthor.isEmpty() ? sAuthor + " - " : "") + sTitle + (sVolume != null && !sVolume.isEmpty() ? ". " + sVolume : "");
//         
//         Book bkFb2 = null,
//              bkEpub = null,
//              bkTxt = null,
//              bkSfb = null;
//         
//         if(bDownloadFB2 && sUrlFb2 != null && !sUrlFb2.trim().isEmpty())
//         {
//            bkFb2 = new Book(sFileName + EXT_FB2, URL_DWN_BGN + sUrlFb2);
//            vFilesFnd.add(bkFb2);
//         }
//         if(bDownloadEPUB && sUrlEpub != null && !sUrlEpub.trim().isEmpty())
//         {
//            bkEpub = new Book(sFileName + EXT_EPUB, URL_DWN_BGN + sUrlEpub);
//            vFilesFnd.add(bkEpub);
//         }
//         if(bDownloadTXT && sUrlTxt != null && !sUrlTxt.trim().isEmpty())
//         {
//            bkTxt = new Book(sFileName + EXT_TXT, URL_DWN_BGN + sUrlTxt);
//            vFilesFnd.add(bkTxt);
//         }
//         if(bDownloadSFB && sUrlSfb != null && !sUrlSfb.trim().isEmpty())
//         {
//            bkSfb = new Book(sFileName + EXT_SFB, URL_DWN_BGN + sUrlSfb);
//            vFilesFnd.add(bkSfb);
//         }
         
//         oFileURLsTableModel.fireTableDataChanged();
//         
//         tabbedPane.setSelectedIndex(JDownloaderX.PNL_NDX_FND);
         
         rnbOnDone.run();
         
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
   }
}