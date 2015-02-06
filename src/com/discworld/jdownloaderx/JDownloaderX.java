package com.discworld.jdownloaderx;

import java.awt.EventQueue;
import java.awt.GridLayout;

import javax.jws.Oneway;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import java.awt.Component;

import javax.swing.ImageIcon;

import java.awt.FlowLayout;

import javax.swing.JTextField;




















import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDownloaderX implements ActionListener
{
   private String sAuthor,
                  sTitle,
                  sVolume,
                  sUrlFb2,
                  sUrlEpub,
                  sUrlTxt,
                  sUrlSfb;
   
   private Pattern ptnAuthotBgn,
                   ptnTitleBgn,
                   ptnUrlFb2,
                   ptnUrlEpub,
                   ptnUrlTxt,
                   ptnUrlSfb;
   
   private JButton btnAdd,
                   btnRemove,
                   btnStart,
                   btnStop;

   private JFrame frame;
   private JTable table;
   private JTextField txtURL;
   
   private ClipboardListener           oClipboardListener;
   
   private final static String DOMAIN = "chitanka.info",
                               AUTHOR_BGN = "<span itemscope itemtype=\"http://schema\\.org/Person\"><a href=\"/person/[\\w\\-]+\" itemprop=\"name\" data-edit=\"/admin/person/\\d+/edit\">",
                               AUTHOR_END = "</a></span>",
                               TITLE_BGN = "<a class=\"selflink\" itemprop=\"name\" data-edit=\"/admin/book/\\d+/edit\">",
                               TITLE_END = "</a>",
                               VOLUME_BGN = "<h2><span>",
                               VOLUME_END = "</span></h2>",
                               URL_FB2 = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 fb2.zip\" class=\"dl dl-fb2 action\"><span>fb2.zip</span>",
                               URL_EPUB = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 epub\" class=\"dl dl-epub action\"><span>epub</span>",
                               URL_TXT = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 txt.zip\" class=\"dl dl-txt action\"><span>txt.zip</span>",
                               URL_SFB = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 sfb.zip\" class=\"dl dl-sfb action\"><span>sfb.zip</span>",
                               URL_BGN = "<a href=\"",
                               URL_END = "\"",
                               BOOK = "/book/",
                               DOWNLOAD_FLD = "D://";

   /**
    * Launch the application.
    */
   public static void main(String[] args)
   {
      EventQueue.invokeLater(new Runnable()
      {
         public void run()
         {
            try
            {
               JDownloaderX window = new JDownloaderX();
               window.frame.setVisible(true);
            } catch(Exception e)
            {
               e.printStackTrace();
            }
         }
      });
   }

   /**
    * Create the application.
    */
   public JDownloaderX()
   {
      initialize();
      
      Runnable checkContent = new Runnable()
      {
         
         @Override
         public void run()
         {
            String sContent = oClipboardListener.getContent();
            
            if(sContent.contains(DOMAIN))
               txtURL.setText(sContent);                  
         }
      };      
      
      oClipboardListener = new ClipboardListener(checkContent);
      oClipboardListener.itisNotEnough();
      oClipboardListener.start();
      
      ptnAuthotBgn = Pattern.compile(AUTHOR_BGN);
      ptnTitleBgn = Pattern.compile(TITLE_BGN);
      ptnUrlFb2 = Pattern.compile(URL_FB2);
      ptnUrlEpub = Pattern.compile(URL_EPUB);
      ptnUrlTxt = Pattern.compile(URL_TXT);
      ptnUrlSfb = Pattern.compile(URL_SFB);
   }

   /**
    * Initialize the contents of the frame.
    */
   private void initialize()
   {
      frame = new JFrame();
      frame.setBounds(100, 100, 450, 300);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
      frame.getContentPane().add(tabbedPane, BorderLayout.NORTH);
      
      DefaultTableModel model = new DefaultTableModel(null,
               new String[] {
                  "Column1", "Column2"
               }
            );
      
      model.addRow(new Object []{"2", "3"});
      model.setValueAt("5", 0, 1);
       
      JComponent panel2 = makeTextPanel("Panel #2");
      tabbedPane.addTab("Tab 2", null, panel2,
              "Does twice as much nothing");
      tabbedPane.setMnemonicAt(0, KeyEvent.VK_2);      
      
      tabbedPane.setTabPlacement(JTabbedPane.TOP);
      tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);      
      
      JPanel panel = new JPanel();
      tabbedPane.addTab("New tab", null, panel, null);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
      JPanel pnlAll = new JPanel();
      panel.add(pnlAll);
      pnlAll.setLayout(new BorderLayout(0, 0));
      
      JPanel pnlButtons = new JPanel();
      pnlButtons.setAlignmentY(Component.TOP_ALIGNMENT);
      pnlAll.add(pnlButtons, BorderLayout.NORTH);
      pnlButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
      pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
      
      btnAdd = new JButton("");
      btnAdd.setAlignmentX(Component.CENTER_ALIGNMENT);
      btnAdd.setToolTipText("Add");
      btnAdd.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/1421707472_add.png")));
      btnAdd.addActionListener(this);
      pnlButtons.add(btnAdd);
      
      btnStart = new JButton("");
      btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);
      btnStart.setToolTipText("Start");
      btnStart.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/play.png")));
      pnlButtons.add(btnStart);
      
      btnStop = new JButton("");
      btnStop.setAlignmentX(Component.CENTER_ALIGNMENT);
      btnStop.setToolTipText("Stop");
      btnStop.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/stop.png")));
      pnlButtons.add(btnStop);
      
      btnRemove = new JButton("");
      btnRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
      btnRemove.setToolTipText("Remove");
      btnRemove.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/1421707488_delete.png")));
      pnlButtons.add(btnRemove);
      
      txtURL = new JTextField();
      txtURL.setFont(new Font("Courier New", Font.PLAIN, 13));
      pnlAll.add(txtURL, BorderLayout.SOUTH);
      txtURL.setColumns(10);
      
      JScrollPane scrollPane = new JScrollPane(new JTable(new DefaultTableModel(
         new Object[][] {
         },
         new String[] {
            "Name", "URL", "Progress"
         }
      )));
      panel.add(scrollPane);
   }

   protected JComponent makeTextPanel(String text) {
      JPanel panel = new JPanel(false);
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
//      table = new JTable();
//      JTable table = new JTable(new DefaultTableModel(new Object[]{"Column1", "Column2"}, 0));
//      panel.add(table);
      JLabel filler = new JLabel(text);
      filler.setHorizontalAlignment(JLabel.CENTER);
      panel.add(filler);
      return panel;
  }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      Object oSource = e.getSource();

      if(oSource == btnAdd)
      {
         try
         {
            vParseURL(txtURL.getText());
         } catch(IOException e1)
         {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
      }
   }
   
   private void vParseURL(String sURL) throws IOException
   {
      
      GetFromURL oGetFromURL = new GetFromURL(sURL);
      oGetFromURL.execute();
//      String sResponse = getFromURL(sURL);
//      
//      sAuthor = sFindString(sResponse, ptnAuthotBgn, AUTHOR_END);
//      
//      sTitle = sFindString(sResponse, ptnAuthotBgn, TITLE_END);
//      
//      sVolume = sFindString(sResponse, VOLUME_BGN, VOLUME_END);
//      
//      sUrlFb2 = sFindString(sResponse, ptnUrlFb2, URL_BGN, URL_END);
//
//      sUrlEpub = sFindString(sResponse, ptnUrlEpub, URL_BGN, URL_END);
//      
//      sUrlTxt = sFindString(sResponse, ptnUrlTxt, URL_BGN, URL_END);
//      
//      sUrlSfb = sFindString(sResponse, ptnUrlSfb, URL_BGN, URL_END);
//      
//      String sFileName = sUrlFb2.substring(BOOK.length());
//      String sName = sFileName.substring(0, sFileName.indexOf("."));
//      String sExt = sFileName.substring(sFileName.lastIndexOf("."));
//
//      DownloadFile oDownloadFile = new DownloadFile("http://" + DOMAIN + sUrlFb2, DOWNLOAD_FLD);
//      oDownloadFile.execute();
      
//      HttpDownloadUtility h = new HttpDownloadUtility("http://" + DOMAIN + sUrlFb2, DOWNLOAD_FLD);
//      h.execute();
      
//      SwingWorker<T, V>
//      
////         HttpDownloadUtility.downloadFile("http://" + DOMAIN + sUrlFb2, DOWNLOAD_FLD);
//      HttpDownloadUtility oHttpDownloadUtility = new HttpDownloadUtility("http://" + DOMAIN + sUrlFb2, DOWNLOAD_FLD);
//      try
//      {
//         oHttpDownloadUtility.start();
////         synchronized(oHttpDownloadUtility)
////         {
//            oHttpDownloadUtility.wait();            
////         }
//         
//      } catch(InterruptedException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
   }
   
   private String sFindString(String sSource, Pattern oPattern, String sEnd)
   {
      int iBgn,
          iEnd;
      
      String sResult = null;
      
      Matcher oMatcher = oPattern.matcher(sSource);
      if(oMatcher.find())
      {
         iBgn = oMatcher.end();
         iEnd = sSource.indexOf(sEnd, iBgn);
         sResult = sSource.substring(iBgn, iEnd);
      }
      
      return sResult;
   }

   private String sFindString(String sSource, String sBegin, String sEnd)
   {
      int iBgn,
          iEnd;
      
      String sResult = null;
      
      if((iBgn = sSource.indexOf(sBegin)) > -1)
      {
         iEnd = sSource.indexOf(sEnd, iBgn);
         sResult = sSource.substring(iBgn + sBegin.length(), iEnd);
      }      
      
      return sResult;
   }
   
   private String sFindString(String sSource, Pattern oPattern, String sBegin, String sEnd)
   {
      int iBgn,
          iEnd;
      
      String sResult = null;
      
      Matcher oMatcher = oPattern.matcher(sSource);
      if(oMatcher.find())
      {
         iBgn = oMatcher.start() + sBegin.length();
         iEnd = sSource.indexOf(sEnd, iBgn);
         sResult = sSource.substring(iBgn, iEnd);
      }      
      
      return sResult;
   }
   
   
   
   private String getFromURL(String sURL)
   {
      final String USER_AGENT = "Mozilla/5.0";

      String sResponse = null;

      URL oURL;

      BufferedReader in;

      HttpURLConnection oHTTPConn;

      try
      {
         // String sURLEncoded = URLEncoder.encode(sURL, "UTF-8");

         oURL = new URL(sURL);
         oHTTPConn = (HttpURLConnection) oURL.openConnection();

         // optional default is GET
         oHTTPConn.setRequestMethod("GET");

         // add reuqest header
         oHTTPConn.setRequestProperty("User-Agent", USER_AGENT);

         if(oHTTPConn.getResponseCode() == 200)
         {
            in = new BufferedReader(new InputStreamReader(
                     oHTTPConn.getInputStream(), "UTF-8"));
            // in = new BufferedReader(new
            // InputStreamReader(oHTTPConn.getInputStream()));

            String inputLine;
            StringBuffer sbResponse = new StringBuffer();

            while((inputLine = in.readLine()) != null)
               sbResponse.append(inputLine + "\n");
            in.close();

            sResponse = sbResponse.toString();
         }
      } catch(MalformedURLException e)
      {
         e.printStackTrace();
      } catch(ProtocolException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return sResponse;
   }   
   
   private class GetFromURL extends SwingWorker<String, Void>
   {
      private String sURL;
      
      public GetFromURL(String sURL)
      {
         this.sURL = sURL;
      }

      @Override
      protected String doInBackground() throws Exception
      {
         return getFromURL(sURL);
      }

      @Override
      protected void done()
      {
         super.done();
         
         try
         {
            String sResponse = get();

            sAuthor = sFindString(sResponse, ptnAuthotBgn, AUTHOR_END);
            
            sTitle = sFindString(sResponse, ptnAuthotBgn, TITLE_END);
            
            sVolume = sFindString(sResponse, VOLUME_BGN, VOLUME_END);
            
            sUrlFb2 = sFindString(sResponse, ptnUrlFb2, URL_BGN, URL_END);

            sUrlEpub = sFindString(sResponse, ptnUrlEpub, URL_BGN, URL_END);
            
            sUrlTxt = sFindString(sResponse, ptnUrlTxt, URL_BGN, URL_END);
            
            sUrlSfb = sFindString(sResponse, ptnUrlSfb, URL_BGN, URL_END); 
            
            DownloadFile oDownloadFile = new DownloadFile(sUrlFb2, DOWNLOAD_FLD);
            oDownloadFile.execute();
            
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

   private class DownloadFile extends SwingWorker<Void, Object> 
   {
      HttpDownloadUtility oHttpDownloadUtility;
      public DownloadFile(String fileURL, String saveDir)
      {
         oHttpDownloadUtility = new HttpDownloadUtility("http://" + DOMAIN + fileURL, saveDir);
      }

      @Override
      protected Void doInBackground() throws Exception
      {
         oHttpDownloadUtility.downloadFile();
         return null;
      }

      @Override
      protected void done()
      {
         super.done();
         
         String sFileName = sUrlFb2.substring(BOOK.length());
         String sName = sFileName.substring(0, sFileName.indexOf("."));
         String sExt = sFileName.substring(sFileName.lastIndexOf("."));         
         
         ExtractFile oExtractFile = new ExtractFile(DOWNLOAD_FLD + "/" + sFileName, DOWNLOAD_FLD);
         oExtractFile.execute();
         
      }
   }   
   
   private class ExtractFile extends SwingWorker<Void, Void>
   {
      String zipFilePath, destDirectory;
      
      UnzipUtility oUnzipUtility;
      
      public ExtractFile(String zipFilePath, String destDirectory)
      {
         this.zipFilePath = zipFilePath;
         this.destDirectory = destDirectory;
         oUnzipUtility = new UnzipUtility();
      }
      
      @Override
      protected Void doInBackground() throws Exception
      {
         oUnzipUtility.unzip(zipFilePath, destDirectory);
         return null;
      }
      
   }
}
