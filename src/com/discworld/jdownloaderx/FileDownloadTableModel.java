package com.discworld.jdownloaderx;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import com.discworld.jdownloaderx.dto.CFile;

public class FileDownloadTableModel extends AbstractTableModel
{
   /**
    * 
    */
   private static final long serialVersionUID = -7277799471311518574L;
   
   private static final String[] names = {"Name", "URL", "Progress"};
   
   private Vector<CFile> vFiles;
   
   public FileDownloadTableModel(Vector<CFile> vFiles)
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
         case 2:
            return vFiles.get(rowIndex).getStatus();
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

   @Override
   public void setValueAt(Object oValue, int rowIndex, int columnIndex)
   {
      CFile oFile = vFiles.get(rowIndex);
      switch (columnIndex) 
      {
         case 2:
            if (oValue instanceof Integer) 
            {
               oFile.setStatus((Integer) oValue);
            }
          break;
      }
   }

   public void updateStatus(CFile oFile, int progress) 
   {
      int i = vFiles.indexOf(oFile);
      if(i < 0)
         return;
      CFile rowData = vFiles.get(i);
               
      if (rowData != null) 
      {
//          int row = rows.indexOf(rowData);
//          float p = (float) progress / 100f;
//          setValueAt(p, i, 2);
          setValueAt(progress, i, 2);
          fireTableCellUpdated(i, 2);
      }
      
//      if(progress == 0)
//         System.out.println(oBook.getName() + " :"+ progress);
  }      
}
