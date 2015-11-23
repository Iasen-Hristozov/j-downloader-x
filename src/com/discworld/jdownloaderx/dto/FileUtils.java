package com.discworld.jdownloaderx.dto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils
{
   static final boolean bOverride = true;
   
   public static void renameFile(String sOldName, String sNewName)
   {
   // File (or directory) with old name
      File file = new File(sOldName);

      // File (or directory) with new name
      File file2 = new File(sNewName);
      file2.getParentFile().mkdirs();
      
      renameFile(file, file2);
   }
   
   public static void renameFile(File flOld, File flNew)
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

   public static void deleteFile(File file) 
   {
      if (file.isDirectory()) 
         for (File sub : file.listFiles()) 
             deleteFile(sub);
      
      file.delete();
   }
   
   public static String getClassName(String sFile) throws IOException
   {
      String sClassName = "";
      ZipInputStream zip = new ZipInputStream(new FileInputStream(sFile));
      for(ZipEntry entry = zip.getNextEntry(); entry!=null && sClassName.isEmpty(); entry=zip.getNextEntry())
      {
         if(entry.getName().endsWith(".class") && !entry.isDirectory()) 
         {
            // This ZipEntry represents a class. Now, what class does it represent?
            StringBuilder className=new StringBuilder();
            for(String part : entry.getName().split("/")) 
            {
               if(className.length() != 0)
                  className.append(".");
                  className.append(part);
               if(part.endsWith(".class"))
                  className.setLength(className.length()-".class".length());
            }
            sClassName = className.toString();
         }
      }
      zip.close();
      return sClassName;
   }   
   

}
