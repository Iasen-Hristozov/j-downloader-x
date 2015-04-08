package com.discworld.jdownloaderx;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;

import javax.jws.Oneway;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import java.awt.Component;

import javax.swing.ImageIcon;

import java.awt.FlowLayout;

import javax.swing.JTextField;

import com.discworld.jdownloaderx.dto.Book;
import com.discworld.jdownloaderx.dto.BookDownloadTableModel;
import com.discworld.jdownloaderx.dto.BookURLsTableModel;
import com.discworld.jdownloaderx.dto.JABXList;

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JDownloaderX implements ActionListener
{
   private final static int    PNL_NDX_DWN = 0,
                               PNL_NDX_FND = 1,
                               MAX_DWN = 2;
   
   private final static String DOMAIN = "chitanka.info",
                               AUTHOR_BGN = "<span itemscope itemtype=\"http://schema\\.org/Person\"><a href=\"/person/[\\w\\-]+\" itemprop=\"name\" data-edit=\"/admin/person/\\d+/edit\">",
                               AUTHOR_END = "</a></span>",
                               TITLE_BGN = "<a class=\"selflink\" itemprop=\"name\" data-edit=\"/admin/book/\\d+/edit\">",
                               TITLE_END = "</a>",
                               VOLUME_BGN = "<h2><span>",
                               VOLUME_END = "</span></h2>",
                               URL_DWN_BGN = "http://" + DOMAIN,
                               URL_FB2 = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 fb2.zip\" class=\"dl dl-fb2 action\"><span>fb2.zip</span>",
                               URL_EPUB = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 epub\" class=\"dl dl-epub action\"><span>epub</span>",
                               URL_TXT = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 txt.zip\" class=\"dl dl-txt action\"><span>txt.zip</span>",
                               URL_SFB = "<a href=\"/book/[\\d\\w\\-\\.]+\" title=\"\u0421\u0432\u0430\u043b\u044f\u043d\u0435 \u0432\u044a\u0432 \u0444\u043e\u0440\u043c\u0430\u0442 sfb.zip\" class=\"dl dl-sfb action\"><span>sfb.zip</span>",
                               URL_BGN = "<a href=\"",
                               URL_END = "\"",
                               BOOK = "/book/",
                               DOWNLOAD_FLD = "Download",
                               EXT_FB2 = ".fb2",
                               EXT_EPUB = ".epub",
                               EXT_SFB = ".sfb",
                               EXT_TXT = ".txt";
   
   private static String sVersion;
   
   private boolean bIsStarted = false;

   private String sAuthor,
                  sTitle,
                  sVolume,
                  sUrlFb2,
                  sUrlEpub,
                  sUrlTxt,
                  sUrlSfb;
   
   private int iDwns = MAX_DWN;
   
   private Pattern ptnAuthotBgn,
                   ptnTitleBgn,
                   ptnUrlFb2,
                   ptnUrlEpub,
                   ptnUrlTxt,
                   ptnUrlSfb;
   
   private JButton btnAdd,
                   btnRemove,
                   btnStart,
                   btnStop,
                   btnSearch;

   private JFrame frame;
   
   JTabbedPane tabbedPane;

   private JTextField txtURL;
   
   private JTable tblBooksUrl;
   
   private ClipboardListener oClipboardListener;
   
   private JTable tblBooksDwn;
   
   private JScrollPane spBooksDwn;
   
   BookURLsTableModel oBookURLsTableModel;
   
   BookDownloadTableModel oBookDownloadTableModel;
   
   Vector<Book> vBooksDwn,
                vBooksFnd;
   
   private downloadThread download;
   
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
               Package p = this.getClass().getPackage();
               sVersion = p.getImplementationVersion();

               
               JDownloaderX window = new JDownloaderX();
               window.frame.setVisible(true);
               
               
            } catch(Exception e)
            {
               e.printStackTrace();
            }
         }
      });
   }
   
   public class ProgressCellRender extends JProgressBar implements TableCellRenderer 
   {
      /**
       * 
       */
      private static final long serialVersionUID = -2555436479986175987L;

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
      {
          int progress = 0;
          if (value instanceof Float) 
          {
              progress = Math.round(((Float) value) * 100f);
          } 
          else if (value instanceof Integer) 
          {
              progress = (Integer) value;
          }
          setValue(progress);
          return this;
      }
  }   

   /**
    * Create the application.
    */
   public JDownloaderX()
   {
//      super("JDownloaderX " + (sVersion != null ? sVersion : "" ));
      
      initialize();
      
      Runnable checkContent = new Runnable()
      {
         
         @Override
         public void run()
         {
            String sContent = oClipboardListener.getContent();
            
            if(sContent.contains(DOMAIN))
            {
               txtURL.setText(sContent);
               try
               {
                  vParseURL(sContent);
               } catch(IOException e)
               {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
            
            
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
      
      loadBooks();
      oBookDownloadTableModel.setValues(vBooksDwn);
      oBookDownloadTableModel.fireTableDataChanged();
      
//      download = new downloadThread();
   }

   /**
    * Initialize the contents of the frame.
    */
   private void initialize()
   {
      frame = new JFrame("JDownloaderX " + (sVersion != null ? sVersion : "" ));
      frame.setBounds(100, 100, 348, 319);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      JPanel pnlButtons = new JPanel();
      frame.getContentPane().add(pnlButtons, BorderLayout.NORTH);
      
      btnAdd = new JButton("");
      btnAdd.setAlignmentX(Component.CENTER_ALIGNMENT);
      btnAdd.setToolTipText("Add");
      btnAdd.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/1421707472_add.png")));
      btnAdd.addActionListener(this);
      pnlButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      pnlButtons.add(btnAdd);
      
      btnStart = new JButton("");
      btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);
      btnStart.setToolTipText("Start");
      btnStart.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/play.png")));
      btnStart.addActionListener(this);
      pnlButtons.add(btnStart);
      
//      btnStop = new JButton("");
//      btnStop.setAlignmentX(Component.CENTER_ALIGNMENT);
//      btnStop.setToolTipText("Stop");
//      btnStop.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/stop.png")));
//      btnStop.addActionListener(this);
//      pnlButtons.add(btnStop);
      
      btnRemove = new JButton("");
      btnRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
      btnRemove.setToolTipText("Remove");
      btnRemove.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/1421707488_delete.png")));
      btnRemove.addActionListener(this);
      pnlButtons.add(btnRemove);
      
      tabbedPane = new JTabbedPane(JTabbedPane.TOP);
      frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
      
      JPanel panel = new JPanel(false);
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      spBooksDwn = new JScrollPane();
      panel.add(spBooksDwn);
      
      tblBooksDwn = new JTable();
      vBooksDwn = new Vector<Book>();
      oBookDownloadTableModel = new BookDownloadTableModel(vBooksDwn);
      tblBooksDwn.setModel(oBookDownloadTableModel);
      tblBooksDwn.getColumn("Progress").setCellRenderer(new ProgressCellRender());
      
      spBooksDwn.setViewportView(tblBooksDwn);
      
      tabbedPane.addTab("Downloads", null, panel, null);
      tabbedPane.setMnemonicAt(0, KeyEvent.VK_2);      
      
      tabbedPane.setTabPlacement(JTabbedPane.TOP);
      
      panel = new JPanel();
      tabbedPane.addTab("Link Grabber", null, panel, null);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
      JPanel pnlSearch = new JPanel();
      panel.add(pnlSearch);
      pnlSearch.setLayout(new BorderLayout(0, 0));
      
      txtURL = new JTextField();
      txtURL.setHorizontalAlignment(SwingConstants.LEFT);
      pnlSearch.add(txtURL, BorderLayout.CENTER);
      txtURL.setFont(new Font("Courier New", Font.PLAIN, 13));
      txtURL.setColumns(10);
      
      btnSearch = new JButton("");
      btnSearch.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/Search.png")));
      btnSearch.setToolTipText("Search");
      btnSearch.setAlignmentX(0.5f);
      btnSearch.addActionListener(this);
      pnlSearch.add(btnSearch, BorderLayout.EAST);
      
      tblBooksUrl = new JTable();
      
      vBooksFnd = new Vector<Book>();
      oBookURLsTableModel = new BookURLsTableModel(vBooksFnd);
      tblBooksUrl.setModel(oBookURLsTableModel);
      
      JScrollPane spBooksUrl = new JScrollPane(tblBooksUrl);
      
      JScrollBar sb = spBooksUrl.getVerticalScrollBar();
      sb.setPreferredSize(new Dimension(50, 0));

      // Put it to the left.
      spBooksUrl.remove(sb);
      panel.add(spBooksUrl);
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      Object oSource = e.getSource();

      if(oSource == btnSearch)
         vSearch();
      else if(oSource == btnAdd)
         vAdd();
      else if(oSource == btnRemove)
         vRemove();
      else if(oSource == btnStart)
         vStartStop();
   }
   
   private void vSearch()
   {
      try
      {
         JAXBContext jaxbContext = JAXBContext.newInstance(Book.class);
         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
         Book bkA = new Book("AAA", "BB");
         StringWriter sw = new StringWriter();
         jaxbMarshaller.marshal(bkA, sw);
         String xmlString = sw.toString();
         
         JAXBContext jc = JAXBContext.newInstance(Book.class);
         Unmarshaller unmarshaller = jc.createUnmarshaller();
         
         StringReader reader = new StringReader(xmlString);
         Book BookNew = (Book) unmarshaller.unmarshal(reader);
         
         JABXList<Book> Books = new JABXList<Book>();
         Books.add(bkA);
         
         Book bkB = new Book("asd", "asdad");
         Books.add(bkB);
         
         JAXBContext jc1 = JAXBContext.newInstance(JABXList.class, Book.class);
         Marshaller marshaller1 = jc1.createMarshaller();
         marshaller1.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         StringWriter sw1 = new StringWriter();
         marshaller1.marshal(Books, sw1);
         xmlString = sw1.toString();
         
         unmarshaller = jc1.createUnmarshaller();
         reader = new StringReader(xmlString);
//         Str
//         Object o = unmarshaller.unmarshal(reader, JABXList.class);
//         unmarshaller.unmarshal();
         JABXList<?> Books1 = (JABXList<?>)unmarshaller.unmarshal(new StreamSource(reader), JABXList.class).getValue();
         
         
         Book c = (Book) Books1.getValues().get(0);
         
         vParseURL(txtURL.getText());
      } catch(IOException e1)
      {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      } catch(JAXBException e1)
      {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }      
   }
   
   private void vAdd()
   {
//    vBooksDwn.addAll(vBooksFnd);
      for(Book oBook : vBooksFnd)
      {
         if(!vBooksDwn.contains(oBook))
            vBooksDwn.add(oBook);
      }
      
      saveBooks();
    
      oBookDownloadTableModel.fireTableDataChanged();
      tabbedPane.setSelectedIndex(PNL_NDX_DWN);
      vBooksFnd.removeAllElements();
      oBookURLsTableModel.fireTableDataChanged();
   }
   
   private void vRemove()
   {
      int[] tiRowNdxs;
      switch(tabbedPane.getSelectedIndex())
      {
         case PNL_NDX_DWN:
            tiRowNdxs = tblBooksDwn.getSelectedRows();
            
            if(tiRowNdxs.length == 0)
               break;
            
            for(int i = 0; i < tiRowNdxs.length; i++)
               vBooksDwn.remove(tiRowNdxs[i]);
            oBookDownloadTableModel.fireTableDataChanged();
            
            saveBooks();
         break;
         
         case PNL_NDX_FND:
            tiRowNdxs = tblBooksUrl.getSelectedRows();
            
            if(tiRowNdxs.length == 0)
               break;
            
            for(int i = 0; i < tiRowNdxs.length; i++)
               vBooksFnd.remove(tiRowNdxs[i]);
            oBookURLsTableModel.fireTableDataChanged();
         break;
      }      
   }
   
   private void vStartStop()
   {
      vToggleButton();
      
      if(isStarted())
      {
//         DownloadFileA oDownloadFileA = new DownloadFileA(vBooksDwn.get(0), oBookDownloadTableModel);         
//         oDownloadFileA.execute();
         download = new downloadThread();   
         download.execute();
      }
   }
   
   private synchronized void vToggleButton()
   {
//      bIsStarted = !bIsStarted;
      setIsStarted(!isStarted());
      btnStart.setIcon(new ImageIcon(JDownloaderX.class.getResource(isStarted() ? "/icons/stop.png" : "/icons/play.png")));
   }
   
   private void vParseURL(String sURL) throws IOException
   {
      
      GetFromURL oGetFromURL = new GetFromURL(sURL);
      oGetFromURL.execute();
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
            
            sTitle = sFindString(sResponse, ptnTitleBgn, TITLE_END);
            
            sVolume = sFindString(sResponse, VOLUME_BGN, VOLUME_END);
            
            sUrlFb2 = sFindString(sResponse, ptnUrlFb2, URL_BGN, URL_END);

            sUrlEpub = sFindString(sResponse, ptnUrlEpub, URL_BGN, URL_END);
            
            sUrlTxt = sFindString(sResponse, ptnUrlTxt, URL_BGN, URL_END);
            
            sUrlSfb = sFindString(sResponse, ptnUrlSfb, URL_BGN, URL_END); 
            
//            DownloadFile oDownloadFile = new DownloadFile(sUrlFb2, DOWNLOAD_FLD);
//            oDownloadFile.execute();
            
            String sFileName = DOWNLOAD_FLD + "/" + (sAuthor != null && !sAuthor.isEmpty() ? sAuthor + " - " : "") + sTitle;
            
            Book bkFb2 = null,
                 bkEpub = null,
                 bkTxt = null,
                 bkSfb = null;
            
            if(sUrlFb2 != null && !sUrlFb2.trim().isEmpty())
            {
               bkFb2 = new Book(sFileName + EXT_FB2, URL_DWN_BGN + sUrlFb2);
               vBooksFnd.add(bkFb2);
            }
            if(sUrlEpub != null && !sUrlEpub.trim().isEmpty())
            {
               bkEpub = new Book(sFileName + EXT_EPUB, URL_DWN_BGN + sUrlEpub);
               vBooksFnd.add(bkEpub);
            }
            if(sUrlTxt != null && !sUrlTxt.trim().isEmpty())
            {
               bkTxt = new Book(sFileName + EXT_TXT, URL_DWN_BGN + sUrlTxt);
               vBooksFnd.add(bkTxt);
            }
            if(sUrlSfb != null && !sUrlSfb.trim().isEmpty())
            {
               bkSfb = new Book(sFileName + EXT_SFB, URL_DWN_BGN + sUrlSfb);
               vBooksFnd.add(bkSfb);
            }
            
//            Vector<Book> vBooks = new Vector<Book>();
//            vBooks.add(bkFb2);
//            vBooks.add(bkEpub);
            
//            BookURLsTableModel oBookURLsTableModel = new BookURLsTableModel(vBooks);
//            tblBooksUrl.setModel(oBookURLsTableModel);
            
//            vBooks.add(bkTxt);
//            vBooks.add(bkSfb);
//            oBookURLsTableModel.setValues(vBooksFnd);

//            Collections.rotate(vBooksFnd.subList(0, 4), -1);
            
//            vMoveUp(vBooksFnd, 0);
            
            oBookURLsTableModel.fireTableDataChanged();
            
            tabbedPane.setSelectedIndex(PNL_NDX_FND);
            
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
   
   private void vMoveUp(Vector<Book> vBook, int index)
   {
      if(index == 0)
         return;
      Collections.rotate(vBook.subList(index-1, index+1), -1);
   }
   
   private void vMoveDown(Vector<Book> vBook, int index)
   {
      if(index == vBook.size()-1)
         return;
      Collections.rotate(vBook.subList(index, index+2), -1);
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

   private class DownloadFileA extends SwingWorker<Boolean, Integer> 
   {
      private static final int BUFFER_SIZE = 4096;
      
      private Book oBook = null;
      BookDownloadTableModel oBookDownloadTableModel = null;
      
      public DownloadFileA(Book aBook, BookDownloadTableModel aBookDownloadTableModel)
      {
         this.oBook = aBook; 
         this.oBookDownloadTableModel = aBookDownloadTableModel;
      }

      @Override
      protected Boolean doInBackground()
      {
         boolean bResult = true;
         String sURL = oBook.getURL();
         URL url;
         try
         {
            url = new URL(sURL);

            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) 
            {
               String fileName = "";
               String disposition = httpConn.getHeaderField("Content-Disposition");
               String contentType = httpConn.getContentType();
               int contentLength = httpConn.getContentLength();

               if (disposition != null) 
               {
                  // extracts file name from header field
                  int index = disposition.indexOf("filename=");
                  if (index > 0) 
                  {
                     fileName = disposition.substring(index + 10, disposition.length() - 1);
                  }
               } 
               else 
               {
                  // extracts file name from URL
                  fileName = sURL.substring(sURL.lastIndexOf("/") + 1, sURL.length());
               }

               System.out.println("Content-Type = " + contentType);
               System.out.println("Content-Disposition = " + disposition);
               System.out.println("Content-Length = " + contentLength);
               System.out.println("fileName = " + fileName);

               // opens input stream from the HTTP connection
               InputStream inputStream = httpConn.getInputStream();
               File flDwnFolder = new File(DOWNLOAD_FLD);
               if(!flDwnFolder.exists())
                  flDwnFolder.mkdir();
//               String saveFilePath = DOWNLOAD_FLD + fileName;
               String saveFilePath = DOWNLOAD_FLD + File.separator + fileName;
               
               // opens an output stream to save into file
               FileOutputStream outputStream = new FileOutputStream(saveFilePath);
   
               int bytesRead = -1;
               int iTotalBytesRead = 0;
               int progress;
               publish(iTotalBytesRead);
               byte[] buffer = new byte[BUFFER_SIZE];
               while ((bytesRead = inputStream.read(buffer)) != -1) 
               {
//                  System.out.println("bIsStarted = " + String.valueOf(bIsStarted));
                  outputStream.write(buffer, 0, bytesRead);
                  iTotalBytesRead += bytesRead;
//                  float fProgress =((float)iTotalBytesRead / (float)contentLength); 
//                  publish(fProgress);
                  
                  if(isStarted())
                  {
                     progress = (int) Math.round(((float)iTotalBytesRead / (float)contentLength) * 100f);
                     publish(progress);
                  }
                  else
                  {
//                     publish(0);
//                        System.out.println(fileName +": " + 0);
                     
                     bResult = false;
                     break;
                     
//                     throw new CException();
                  }
                  
                  
//                  setProgress(progress);
                  
               }
   
               outputStream.close();
               inputStream.close();
   
               if(bResult)
                  System.out.println("File downloaded");
            } 
            else 
            {
               System.out.println("No file to download. Server replied HTTP code: " + responseCode);
               bResult = false;
            }
            
            httpConn.disconnect();
         } 
         catch(MalformedURLException e)
         {
            e.printStackTrace();
            bResult = false;
         } 
         catch(IOException e)
         {
            e.printStackTrace();
            bResult = false;
         } 
         
         return bResult;
      }
      
      @Override
      protected void process(List<Integer> chunks)
      {
         int progress = chunks.get(0);
//         oBookDownloadTableModel.updateStatus(oBook, progress);
         setBookProgress(oBook, progress);
         
//         if(progress == 0)
//            System.out.println("Progress: " + progress);
      }      

      @Override
      protected void done()
      {
         super.done();
         
         try
         {
            boolean status = get();
            
            if(status)
            {
               int i = vBooksDwn.indexOf(oBook);
               if(i >=0 )
                  vBooksDwn.remove(i);
               
               oBookDownloadTableModel.setValues(vBooksDwn);
               oBookDownloadTableModel.fireTableDataChanged();
               
               iDwns++;
            }
            else
            {
               iDwns = MAX_DWN;
               setBookProgress(oBook, 0);
            }
         } catch(InterruptedException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch(ExecutionException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         
//         
//         String sFileName = sUrlFb2.substring(BOOK.length());
//         String sName = sFileName.substring(0, sFileName.indexOf("."));
//         String sExt = sFileName.substring(sFileName.lastIndexOf("."));         
//         
//         ExtractFile oExtractFile = new ExtractFile(DOWNLOAD_FLD + "/" + sFileName, DOWNLOAD_FLD);
//         oExtractFile.execute();
         
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

   private class ExtractFileA extends SwingWorker<Void, Integer>
   {
      private static final int BUFFER_SIZE = 4096;
      
      private int iTotalBytesRead = 0;
      
      private String zipFilePath, destDirectory;
      
      public ExtractFileA(String zipFilePath, String destDirectory)
      {
         this.zipFilePath = zipFilePath;
         this.destDirectory = destDirectory;
      }
      
      @Override
      protected Void doInBackground() throws Exception
      {
         File destDir = new File(destDirectory);
         if (!destDir.exists()) 
         {
             destDir.mkdir();
         }
         ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
         ZipEntry entry = zipIn.getNextEntry();
         // iterates over entries in the zip file
         while(entry != null) 
         {
             String filePath = destDirectory + File.separator + entry.getName();
             if(!entry.isDirectory()) 
             {
                 // if the entry is a file, extracts it
                 extractFile(zipIn, filePath);
             } 
             else 
             {
                 // if the entry is a directory, make the directory
                 File dir = new File(filePath);
                 dir.mkdir();
             }
             zipIn.closeEntry();
             entry = zipIn.getNextEntry();
         }
         zipIn.close();         return null;
      }
      
      @Override
      protected void process(List<Integer> chunks)
      {

         int progress = chunks.get(0);
//         oBookDownloadTableModel.updateStatus(oBook, progress);
      }            
      
      private void extractFile(ZipInputStream zipIn, String filePath) throws IOException 
      {
          BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
          byte[] bytesIn = new byte[BUFFER_SIZE];
          int read = 0;
          while ((read = zipIn.read(bytesIn)) != -1) 
          {
             bos.write(bytesIn, 0, read);
          }
          bos.close();
      }
   }
   
   
   private void saveBooks()
   {
      try 
      {
         File file = new File("file.xml");
         JAXBContext jaxbContext = JAXBContext.newInstance(JABXList.class, Book.class);
         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    
//         JABXList<Book> Books = new JABXList<Book>();
//         for(Book oBook: vBooksDwn)
//            Books.add(oBook);
         
         JABXList<Book> Books = new JABXList<Book>(vBooksDwn);
         
         // output pretty printed
         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

         jaxbMarshaller.marshal(Books, file);
         jaxbMarshaller.marshal(Books, System.out);
         
         
//         jaxbMarshaller.marshal(vBooksDwn, file);
//         jaxbMarshaller.marshal(vBooksDwn, System.out);
    
      } 
      catch (JAXBException e) 
      {
         e.printStackTrace();
      }      
   }
   
   private void loadBooks()
   {
      try 
      {
         File file = new File("file.xml");
         JAXBContext jaxbContext = JAXBContext.newInstance(JABXList.class, Book.class);
         Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//         JABXList<?> Books = (JABXList<?>)jaxbUnmarshaller.unmarshal(file);
         JABXList<Book> Books = (JABXList<Book>)jaxbUnmarshaller.unmarshal(file);
//         System.out.println(Books);

         vBooksDwn.clear();
         
         vBooksDwn = new Vector<Book>(Books.getValues());
         
//         for(int i = 0; i < Books.getValues().size(); i++)
//            vBooksDwn.add((Book)Books.getValues().get(i));
      } 
      catch (JAXBException e) 
      {
         e.printStackTrace();
      }
   }

   private class downloadThread extends SwingWorker<Void, Void> 
   {
//      private boolean bIsRunning = false;
      
//      @Override
//      public void run()
//      {
//         Book oBook;
//         
//         Vector<Book> vBooksCur = new Vector<Book>(vBooksDwn);
//         
//         int j = 0;
//         
//         try
//         {
//            while(bIsStarted)
//            {
//               if(iDwns > 0)
//               {
//                  oBook = vBooksCur.get(j);
//                  if(j < vBooksCur.size()-1)
//                     j++;
//                  
//                  new DownloadFileA(oBook, oBookDownloadTableModel).execute();
//                  iDwns--;
//               }
//               Thread.sleep(100);
//            }
//         } 
//         catch(InterruptedException e)
//         {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//         }
//      }
      
//      @Override
//      public void start()
//      {
////         bIsRunning = true;
//         super.start();
//      }
//      
//      public void vStop()
//      {
////         bIsRunning = false;
//      }

      @Override
      protected Void doInBackground() throws Exception
      {
         Book oBook;
            
         Vector<Book> vBooksCur = new Vector<Book>(vBooksDwn);
            
         int j = 0;
         
         try
         {
            while(isStarted())
            {
               if(iDwns > 0)
               {
                  if(vBooksDwn.size() == 0)
                  {
                     vToggleButton();
                     setIsStarted(false);
//                     bIsStarted = false;
                     break;
                  }
                  oBook = vBooksCur.get(j);
                  if(j < vBooksCur.size()-1)
                     j++;
                  
                  new DownloadFileA(oBook, oBookDownloadTableModel).execute();
                  iDwns--;
               }
               Thread.sleep(100);
            }
         } 
         catch(InterruptedException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         return null;
      }
   }
   
   private synchronized boolean isStarted()
   {
      return bIsStarted;
   }
   
   private synchronized void setIsStarted(boolean bIsStarted)
   {
      this.bIsStarted = bIsStarted; 
   }
   
   private synchronized void setBookProgress(Book oBook, int progress)
   {
      oBookDownloadTableModel.updateStatus(oBook, progress);
   }
   
   private class CException extends Exception 
   {
      
   }
}
