package com.discworld.jdownloaderx;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

class ClipboardListener extends Thread implements ClipboardOwner 
{
   private boolean bEnough=false;
   
   private String sContent;
   
   private Runnable r;

   private Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
   
   public ClipboardListener(Runnable r)
   {
      this.r = r;
   }
   
   public void run() 
   {
      Transferable trans = sysClip.getContents(this);
      regainOwnership(trans);
//      System.out.println("Listening to board...");
      while(true) 
      {
         if(isitEnough())
            break;
      }
//      System.out.println("No more Listening...");
   }

   public void itisEnough()
   {
      bEnough=true;
   }
   
   public void itisNotEnough()
   {
      bEnough=false;
   }
   
   boolean isitEnough()
   {
      return bEnough;
   }

   public String getContent()
   {
      return sContent;
   }
   
   @Override
   public void lostOwnership(Clipboard c, Transferable t) 
   {
      try
      {
         sleep(200);
      }
      catch(Exception e)
      {
         System.out.println("Exception: "+e);
      }
      
      try
      {
         Transferable contents = c.getContents(this); //EXCEPTION
         //processContents(contents);
         regainOwnership(contents);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   void processContents(Transferable t) 
   {
      if(isitEnough())
         return;
   
      DataFlavor[] flavors=t.getTransferDataFlavors();
      for(int i=flavors.length-1;i>=0;i--)
      {
         try
         {
            Object o=t.getTransferData(flavors[i]);
            //System.out.println("Flavor "+i+" gives "+o.getClass().getName());
            if(o instanceof String)
            {
               sContent = (String)o;
               r.run();
//                 System.out.println("String="+(String)o);
//                 if(((String)o).contains(DOMAIN))
//                    txtURL.setText((String)o);                    
               break;
            }
         }
         catch(Exception exp)
         {
            exp.printStackTrace();
         }
      }
      //System.out.println("Processing: ");
   }

   void regainOwnership(Transferable t) 
   {
      sysClip.setContents(t, this);
      processContents(t);
   }
}