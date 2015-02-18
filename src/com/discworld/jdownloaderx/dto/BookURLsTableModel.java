package com.discworld.jdownloaderx.dto;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class BookURLsTableModel extends AbstractTableModel
{
   /**
    * 
    */
   private static final long serialVersionUID = -7277799471311518574L;
   
   private static final String[] names = {"Name", "URL"};
   
   private Vector<Book> vBooks;
   
   public BookURLsTableModel(Vector<Book> vBooks)
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
//         case 2:
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
   
}
