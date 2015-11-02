package com.discworld.jdownloaderx.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "list")

public class JABXList<VALUE>
{
   private List<VALUE> values;

   public JABXList()
   {
      values = new ArrayList<VALUE>();
   }
   
   public JABXList(List<VALUE> values)
   {
      this.values = values;
   }
   
   @XmlAnyElement(lax=true)
   public List<VALUE> getValues() 
   {
      return values;
   }
   
   public void add(VALUE v)
   {
      values.add(v);
   }
}
