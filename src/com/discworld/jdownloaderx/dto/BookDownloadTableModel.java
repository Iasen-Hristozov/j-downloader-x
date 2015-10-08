package com.discworld.jdownloaderx.dto;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class BookDownloadTableModel extends AbstractTableModel
{
   /**
    * 
    */
   private static final long serialVersionUID = -7277799471311518574L;
   
   private static final String[] names = {"Name", "URL", "Progress"};
   
   private Vector<Book> vBooks;
   
   public BookDownloadTableModel(Vector<Book> vBooks)
   {
      this.vBooks = vBooks;
   }
   
   @Override
   public int getRowCount()
   {
      return vBooks.size();
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
            return vBooks.get(rowIndex).getName();
         case 1:
            return vBooks.get(rowIndex).getURL();
         case 2:
            return vBooks.get(rowIndex).getStatus();
         default:
            return "";
       }   
   }

   @Override
   public String getColumnName(int col) {
       return names[col];
   }
   
   public void setValues(Vector<Book> vBooks)
   {
      this.vBooks = vBooks;
   }

   @Override
   public void setValueAt(Object oValue, int rowIndex, int columnIndex)
   {
      Book oBook = vBooks.get(rowIndex);
      switch (columnIndex) 
      {
         case 2:
            if (oValue instanceof Integer) 
            {
               oBook.setStatus((Integer) oValue);
            }
          break;
      }
   }

   public void updateStatus(Book oBook, int progress) 
   {
      int i = vBooks.indexOf(oBook);
      if(i < 0)
         return;
      Book rowData = vBooks.get(i);
               
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
