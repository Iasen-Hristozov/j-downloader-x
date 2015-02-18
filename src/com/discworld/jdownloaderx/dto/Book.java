package com.discworld.jdownloaderx.dto;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"sName","sURL"})
@XmlRootElement(name = "book")


public class Book
{
   @XmlElement(name = "name", required = true)
   private String sName;
   @XmlElement(name = "url", required = true)
   private String sURL;
   
   public Book()
   {
      sName = "";
      sURL = "";
   }
   
   public Book(String sName, String sURL)
   {
      this.sName = sName;
      this.sURL = sURL;
   }
   
   public String getName()
   {
      return sName;
   }

   public void setName(String sName)
   {
      this.sName = sName;
   }

   public String getURL()
   {
      return sURL;
   }

   public void setURL(String sURL)
   {
      this.sURL = sURL;
   }

}
