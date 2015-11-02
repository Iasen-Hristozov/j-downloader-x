package com.discworld.jdownloaderx;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import com.discworld.jdownloaderx.dto.CFile;

public class FileURLsTableModel extends AbstractTableModel
{
   /**
    * 
    */
   private static final long serialVersionUID = -7277799471311518574L;
   
   private static final String[] names = {"Name", "URL"};
   
   private Vector<CFile> vFiles;
   
   public FileURLsTableModel(Vector<CFile> vFiles)
   {
      this.vFiles = vFiles;
   }
   
   @Override
   public int getRowCount()
   {
      return vFiles.size();
   }

   @Override
   public int getColumnCount()
   {
      return names.length;
   }

   @Override
   public Object getValueAt(int rowIndex, int columnIndex)
   {
      switch (columnIndex) 
      {
         case 0:
            return vFiles.get(rowIndex).getName();
         case 1:
            return vFiles.get(rowIndex).getURL();
//         case 2:
         default:
            return "";
       }   
   }

   @Override
   public String getColumnName(int col) {
       return names[col];
   }
   
   public void setValues(Vector<CFile> vFiles)
   {
      this.vFiles = vFiles;
   }
   
}
