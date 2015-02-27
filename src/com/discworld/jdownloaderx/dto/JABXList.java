package com.discworld.jdownloaderx.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;

//@XmlSeeAlso(Book.class)
@XmlRootElement(name = "list")
//@XmlRootElement(namespace="http://www.w3.org/2005/Atom")
//@XmlAccessorType(XmlAccessType.FIELD)

public class JABXList<VALUE>
{
   private List<VALUE> values;

   public JABXList()
   {
      values = new ArrayList<VALUE>();
   }
   
   public JABXList(List<VALUE> values)
   {
//      super();
      this.values = values;
   }
   
   @XmlAnyElement(lax=true)
//   @XmlSchemaType(name = "atomInlineOtherContent")
//   @XmlElement(name="item")
   public List<VALUE> getValues() 
   {
      return values;
   }
   
   public void add(VALUE v)
   {
      values.add(v);
   }
}
