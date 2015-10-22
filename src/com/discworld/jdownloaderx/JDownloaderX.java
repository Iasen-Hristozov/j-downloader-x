package com.discworld.jdownloaderx;

import static java.nio.file.StandardCopyOption.*;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import java.awt.Component;

import javax.swing.ImageIcon;

import java.awt.FlowLayout;

import javax.swing.JTextField;

import com.discworld.jdownloaderx.dto.Book;
import com.discworld.jdownloaderx.dto.BookDownloadTableModel;
import com.discworld.jdownloaderx.dto.BookURLsTableModel;
import com.discworld.jdownloaderx.dto.ChitankaHttpParser;
import com.discworld.jdownloaderx.dto.JABXList;

import java.awt.Font;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class JDownloaderX extends JFrame implements ActionListener
{
   public final static int     PNL_NDX_DWN = 0,
                               PNL_NDX_FND = 1,
                               MAX_DWN = 2;
   
   private final static String DOWNLOAD_FLD = "Download",
                               BOOKS_FILE = "file.xml";
   
   private static String sVersion;
   
   private boolean bIsStarted = false,
                   bOverride = true;
   
   private JButton btnAdd,
                   btnRemove,
                   btnStart,
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
                vBooksFnd,
                vBooksCur;
   
   
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
      super("JDownloaderX " + (sVersion != null ? sVersion : "" ));
      
      initialize();
      
      vBooksCur = new Vector<Book>();
      
      Runnable checkContent = new Runnable()
      {
         
         @Override
         public void run()
         {
            String sContent = oClipboardListener.getContent();
            
            if(sContent.contains(ChitankaHttpParser.DOMAIN))
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
      
      loadBooks();
      oBookDownloadTableModel.setValues(vBooksDwn);
      oBookDownloadTableModel.fireTableDataChanged();
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
         vParseURL(txtURL.getText());
      } catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   private void vAdd()
   {
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
         new downloadThread().execute();
      }
   }
   
   private synchronized void vToggleButton()
   {
      setIsStarted(!isStarted());
      btnStart.setIcon(new ImageIcon(JDownloaderX.class.getResource(isStarted() ? "/icons/stop.png" : "/icons/play.png")));
   }
   
   private void vParseURL(String sURL) throws IOException
   {
      if(sURL.contains(ChitankaHttpParser.DOMAIN))
      {
         new ChitankaHttpParser(sURL, vBooksFnd, rnbUpdateTable).execute();
      }
   }

   private class DownloadFile extends SwingWorker<Boolean, Integer> 
   {
      private static final int BUFFER_SIZE = 4096;
      
      String saveFilePath;
      
      private Book oBook = null;

      public DownloadFile(Book aBook)
      {
         this.oBook = aBook; 
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
            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
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
               saveFilePath = DOWNLOAD_FLD + File.separator + fileName;
               
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
                  System.out.println("File " + fileName + " downloaded");
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
               deleteBook(oBook);
               
               saveBooks();
               
               if(oBook.getURL().endsWith(".zip"))
               {
                  File oFolder = new File(saveFilePath.substring(0, saveFilePath.lastIndexOf(".zip")));
                  ExtractFile oExtractFile = new ExtractFile(saveFilePath, oFolder.getPath());
                  oExtractFile.execute();
                  oExtractFile.get();
                  new File(saveFilePath).delete();
//                  File oFolder = new File(oBook.getName());
                  if(oFolder.listFiles().length == 1)
                  {
                     File file = oFolder.listFiles()[0];
                     
                     Files.move(file.toPath(), new File(DOWNLOAD_FLD + File.separator + oBook.getName()).toPath(), REPLACE_EXISTING);
                     deleteFile(oFolder);
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
//                        file.renameTo(new File(oFolder.getPath() + File.separator + oBook.getName()));
//                        Files.move(file.toPath(), new File(oFolder.getPath() + File.separator + oBook.getName()).toPath(), REPLACE_EXISTING);
                        renameFile(file.getPath(), oFolder.getPath() + File.separator + oBook.getName());
                     }
                     
                     renameFile(oFolder.getPath(), DOWNLOAD_FLD + File.separator + oBook.getName());
                  }
               }
               else
               {
                  renameFile(saveFilePath, DOWNLOAD_FLD + File.separator + oBook.getName());
               }               
            }
            else
            {
//               iDwns = MAX_DWN;
               setBookProgress(oBook, 0);
            }
         } 
         catch(InterruptedException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch(ExecutionException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch(IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
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
      
      @Override
      protected void done()
      {
         // TODO Auto-generated method stub
         super.done();
      }
   }
   
   private synchronized void saveBooks()
   {
      try 
      {
         File file = new File(BOOKS_FILE);
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
         File file = new File(BOOKS_FILE);
         JAXBContext jaxbContext = JAXBContext.newInstance(JABXList.class, Book.class);
         Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//         JABXList<?> Books = (JABXList<?>)jaxbUnmarshaller.unmarshal(file);
         JABXList<Book> Books = (JABXList<Book>)jaxbUnmarshaller.unmarshal(file);

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
      @Override
      protected Void doInBackground() throws Exception
      {
         try
         {
            while(isStarted())
            {
               if(vBooksDwn.size() == 0)
               {
                  vToggleButton();
                  setIsStarted(false);
                  break;
               }
               
               for(Book oBook: vBooksDwn)
               {
                  if(!vBooksCur.contains(oBook))
                  {
                     addFile(oBook);
                     new DownloadFile(oBook).execute();
                  }
                  if(vBooksCur.size() >= MAX_DWN)
                     break;
                  
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
   
   private synchronized void deleteBook(Book oBook)
   {
      vBooksDwn.remove(oBook);
      oBookDownloadTableModel.setValues(vBooksDwn);
      oBookDownloadTableModel.fireTableDataChanged();
      vBooksCur.remove(oBook);
   }

   private synchronized void addFile(Book oBook)
   {
      vBooksCur.add(oBook);
   }

   private void renameFile(String sOldName, String sNewName)
   {
   // File (or directory) with old name
      File file = new File(sOldName);

      // File (or directory) with new name
      File file2 = new File(sNewName);
      
      renameFile(file, file2);
   }
   
   private void renameFile(File flOld, File flNew)
   {
      boolean success;
      
      if (flNew.exists())
      {
         if(bOverride)
         {
            deleteFile(flNew);
         }
         else
            return;
//         throw new java.io.IOException("file exists");
      }

      // Rename file (or directory)
      success = flOld.renameTo(flNew);

      if (!success) {
         // File was not successfully renamed
      }      
   }

   private void deleteFile(File file) 
   {
      if (file.isDirectory()) 
         for (File sub : file.listFiles()) 
             deleteFile(sub);
      
      file.delete();
   }
   
   private Runnable rnbUpdateTable = new Runnable()
   {
      
      @Override
      public void run()
      {
         oBookURLsTableModel.fireTableDataChanged();
         
         tabbedPane.setSelectedIndex(PNL_NDX_FND);
      }
   };
}
