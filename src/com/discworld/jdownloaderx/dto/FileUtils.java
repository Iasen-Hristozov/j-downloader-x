package com.discworld.jdownloaderx.dto;

import java.io.File;

public class FileUtils
{
   static final boolean bOverride = true;
   
   public static void renameFile(String sOldName, String sNewName)
   {
   // File (or directory) with old name
      File file = new File(sOldName);

      // File (or directory) with new name
      File file2 = new File(sNewName);
      
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
   

}
