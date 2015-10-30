//package com.discworld.jdownloaderx.dto;
//
//import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
//
//import java.io.File;
//import java.io.FileFilter;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.nio.file.Files;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//
//import javax.swing.SwingWorker;
//
//class DownloadFile extends SwingWorker<Boolean, Integer> 
//{
//   private static final int BUFFER_SIZE = 4096;
//   
//   String saveFilePath;
//   
//   private CFile oFile = null;
//
//   public DownloadFile(CFile aFile)
//   {
//      this.oFile = aFile; 
//   }
//
//   @Override
//   protected Boolean doInBackground()
//   {
//      boolean bResult = true;
//      String sURL = oFile.getURL();
//      URL url;
//      try
//      {
//         url = new URL(sURL);
//
//         HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
//         httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
//         int responseCode = httpConn.getResponseCode();
//
//         // always check HTTP response code first
//         if (responseCode == HttpURLConnection.HTTP_OK) 
//         {
//            String fileName = "";
//            String disposition = httpConn.getHeaderField("Content-Disposition");
//            String contentType = httpConn.getContentType();
//            int contentLength = httpConn.getContentLength();
//
//            if (disposition != null) 
//            {
//               // extracts file name from header field
//               int index = disposition.indexOf("filename=");
//               if (index > 0) 
//               {
//                  fileName = disposition.substring(index + 10, disposition.length() - 1);
//               }
//            } 
//            else 
//            {
//               // extracts file name from URL
//               fileName = sURL.substring(sURL.lastIndexOf("/") + 1, sURL.length());
//            }
//
//            System.out.println("Content-Type = " + contentType);
//            System.out.println("Content-Disposition = " + disposition);
//            System.out.println("Content-Length = " + contentLength);
//            System.out.println("fileName = " + fileName);
//
//            // opens input stream from the HTTP connection
//            InputStream inputStream = httpConn.getInputStream();
//            File flDwnFolder = new File(DOWNLOAD_FLD);
//            if(!flDwnFolder.exists())
//               flDwnFolder.mkdir();
////            String saveFilePath = DOWNLOAD_FLD + fileName;
//            saveFilePath = DOWNLOAD_FLD + File.separator + fileName;
//            
//            // opens an output stream to save into file
//            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
//
//            int bytesRead = -1;
//            int iTotalBytesRead = 0;
//            int progress;
//            publish(iTotalBytesRead);
//            byte[] buffer = new byte[BUFFER_SIZE];
//            while ((bytesRead = inputStream.read(buffer)) != -1) 
//            {
////               System.out.println("bIsStarted = " + String.valueOf(bIsStarted));
//               outputStream.write(buffer, 0, bytesRead);
//               iTotalBytesRead += bytesRead;
//
//               if(isStarted())
//               {
//                  progress = (int) Math.round(((float)iTotalBytesRead / (float)contentLength) * 100f);
//                  publish(progress);
//               }
//               else
//               {
////                  publish(0);
////                     System.out.println(fileName +": " + 0);
//                  
//                  bResult = false;
//                  break;
//                  
////                  throw new CException();
//               }
//               
////               setProgress(progress);
//            }
//
//            outputStream.close();
//            inputStream.close();
//
//            if(bResult)
//               System.out.println("File " + fileName + " downloaded");
//         } 
//         else 
//         {
//            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
//            bResult = false;
//         }
//         
//         httpConn.disconnect();
//      } 
//      catch(MalformedURLException e)
//      {
//         e.printStackTrace();
//         bResult = false;
//      } 
//      catch(IOException e)
//      {
//         e.printStackTrace();
//         bResult = false;
//      } 
//      
//      return bResult;
//   }
//   
//   @Override
//   protected void process(List<Integer> chunks)
//   {
//      int progress = chunks.get(0);
//      setFileProgress(oFile, progress);
//      
////      if(progress == 0)
////         System.out.println("Progress: " + progress);
//   }      
//
//   @Override
//   protected void done()
//   {
//      super.done();
//      
//      try
//      {
//         boolean status = get();
//         
//         if(status)
//         {
//            deleteFile(oFile);
//            
//            saveFiles();
//            
//            if(oFile.getURL().endsWith(".zip"))
//            {
//               File oFolder = new File(saveFilePath.substring(0, saveFilePath.lastIndexOf(".zip")));
//               ExtractFile oExtractFile = new ExtractFile(saveFilePath, oFolder.getPath());
//               oExtractFile.execute();
//               oExtractFile.get();
//               new File(saveFilePath).delete();
////               File oFolder = new File(oFile.getName());
//               if(oFolder.listFiles().length == 1)
//               {
//                  File file = oFolder.listFiles()[0];
//                  
//                  Files.move(file.toPath(), new File(DOWNLOAD_FLD + File.separator + oFile.getName()).toPath(), REPLACE_EXISTING);
//                  deleteFile(oFolder);
//               }
//               else
//               {
//                  FileFilter filter = new FileFilter() 
//                  {
//                     @Override
//                     public boolean accept(File pathname) 
//                     {
//                        return pathname.getName().endsWith(".sfb")|| pathname.getName().endsWith(".fb2") || pathname.getName().endsWith(".txt") || pathname.getName().endsWith(".epub");
//                     }
//                  };                  
//                  for(int i = 0; i < oFolder.listFiles(filter).length; i++)
//                  {
//                     File file = oFolder.listFiles(filter)[i];
////                     file.renameTo(new File(oFolder.getPath() + File.separator + oFile.getName()));
////                     Files.move(file.toPath(), new File(oFolder.getPath() + File.separator + oFile.getName()).toPath(), REPLACE_EXISTING);
//                     renameFile(file.getPath(), oFolder.getPath() + File.separator + oFile.getName());
//                  }
//                  
//                  renameFile(oFolder.getPath(), DOWNLOAD_FLD + File.separator + oFile.getName());
//               }
//            }
//            else
//            {
//               renameFile(saveFilePath, DOWNLOAD_FLD + File.separator + oFile.getName());
//            }               
//         }
//         else
//         {
////            iDwns = MAX_DWN;
//            setFileProgress(oFile, 0);
//         }
//      } 
//      catch(InterruptedException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      } catch(ExecutionException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      } catch(IOException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//   }
//}   