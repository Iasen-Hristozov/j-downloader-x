package com.discworld.jdownloaderx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.Plugin;

public class PluginFactory
{
   private static PluginFactory instance = new PluginFactory();

   private HashMap<String, Plugin> registeredPlugins = new HashMap<String, Plugin>();
   
   private PluginFactory()
   {
//      System.out.println("Singleton(): Initializing Instance");
   }

//   public static PluginFactory getInstance()
//   {    
//      return instance;
//   }

   public static void registerPlugin(String pluginID, Plugin p)    
   {
//      m_RegisteredPlugins.put(pluginID, p);
      instance.registeredPlugins.put(pluginID, p);
   }
   
   public static Plugin getPlugin(String pluginID)
   {
//      ((Product)m_RegisteredProducts.get(pluginID)).createProduct();
//      return (Plugin) m_RegisteredPlugins.get(pluginID);
      return (Plugin) instance.registeredPlugins.get(pluginID);
   }
   
   public static ArrayList<CFile> checkContetWithPlugins(String sPath, String sContent)
   {
      ArrayList<CFile> alFilesFound = new ArrayList<CFile>();
      
      for(Entry<String, Plugin> entry : instance.registeredPlugins.entrySet())
      {
         Plugin value = entry.getValue();
         if(value.isForCheck())
         {
            alFilesFound.addAll(value.checkContetWithPlugin(sPath, sContent));
         }
      }

//      instance.registeredPlugins.forEach((key,value) -> alFilesFound.addAll(value.checkContetWithPlugin(sPath, sContent)));
      
      return alFilesFound;
   }
}
