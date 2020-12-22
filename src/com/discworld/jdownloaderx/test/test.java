package com.discworld.jdownloaderx.test;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.Test;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.MoviePlugin;
import com.discworld.jdownloaderx.plugins.SubsUnacs;
import com.discworld.jdownloaderx.plugins.ZamundaSe;
//import com.sun.java.util.jar.pack.Package.Class.Method;

public class test
{
   private final static String PATH = "path";

   @Test
   public void test_ZamundaSe_TheQueensGambit()
   {
      final String TITLE = "The Queen's Gambit - Season 1", 
                   TORRENT_URL = "http://zelka.org/download.php/609691/The.Queens.Gambit.S01.720p.NF.WEBRip.DDP5.1.x264-STRONTiUM%5Brartv%5D.torrent",
                   MAGNET = "magnet:?xt=urn:btih:02591402D36B588ADFB6B8D7BE97C18FDF0CC061",
                   DESCRIPTION = "## Режисьор : Scott Frank  \n" + 
                            "## В ролите : Thomas Brodie-Sangster, Bill Camp, Moses Ingram и др. \n" + 
                            "## IMDB : Линк към IMDB\n" + 
                            "## Държава :  САЩ \n" + 
                            "## Година :  2020\n" + 
                            "## Времетраене : 7 x 60 мин. \n" + 
                            "## Резюме : Бет има доста проблеми, с които да се справя, но шахът не е един от тях, защото тя е направо феноменална, когато започне да играе. А пристрастеността й към седатив до някаква степен й помага да разчиства дъската. Но за да се изправи срещу най-добрите мъже в шаха, ще й се наложи да укроти своите демони. The Queen's Gambit е базиран на книгата на Уолтър Тевис.",
                   IMAGE_URL = "http://img.zamunda.se/bitbucket/gambit.png";
      
      
      // http://zelka.org/details.php?id=609691
      assertTrue(true);
//      fail("Not yet implemented");
      MoviePlugin zamundaSe = new ZamundaSe();
      SubsUnacs subsUnacs = new SubsUnacs();
      
      ArrayList<CFile> alFiles = subsUnacs.checkContetWithPlugin(PATH, TestTexts.the_queen_gambit);
      new CFile(PATH+"\\", "https://subsunacs.net/subtitles/The_Queen_s_Gambit_01x01-140014/");
      assertTrue(alFiles.contains(new CFile(PATH+"\\", "https://subsunacs.net/subtitles/The_Queen_s_Gambit_01x01-140014/")));
      assertTrue(alFiles.contains(new CFile(PATH+"\\", "https://subsunacs.net/subtitles/The_Queen_s_Gambit_01x02-140081/")));
      assertTrue(alFiles.contains(new CFile(PATH+"\\", "https://subsunacs.net/subtitles/The_Queen_s_Gambit_01x03-140087/")));
      assertTrue(alFiles.contains(new CFile(PATH+"\\", "https://subsunacs.net/subtitles/The_Queen_s_Gambit_01x04-140095/")));
      assertTrue(alFiles.contains(new CFile(PATH+"\\", "https://subsunacs.net/subtitles/The_Queen_s_Gambit_01x05-140115/")));
      assertTrue(alFiles.contains(new CFile(PATH+"\\", "https://subsunacs.net/subtitles/The_Queen_s_Gambit_01x06-140132/")));
      assertTrue(alFiles.contains(new CFile(PATH+"\\", "https://subsunacs.net/subtitles/The_Queen_s_Gambit_01x07-140156/")));
      
      try
      {
         Method methodGetTitle = MoviePlugin.class.getDeclaredMethod("getTitle", String.class);
         methodGetTitle.setAccessible(true);
         String title = (String) methodGetTitle.invoke(zamundaSe, TestTexts.the_queen_gambit);
         assertEquals(TITLE, title);
      }
      catch(NoSuchMethodException
            | SecurityException 
            | IllegalAccessException 
            | IllegalArgumentException
            | InvocationTargetException e)
      {
         fail("Fail on getTitle");
         e.printStackTrace();
      }

      try
      {
         Method methodGetTorrentUrl = MoviePlugin.class.getDeclaredMethod("getTorrentUrl", String.class);
         methodGetTorrentUrl.setAccessible(true);
         String torrentUrl = (String) methodGetTorrentUrl.invoke(zamundaSe, TestTexts.the_queen_gambit);
         assertEquals(TORRENT_URL, torrentUrl);
      }
      catch(NoSuchMethodException
            | SecurityException 
            | IllegalAccessException 
            | IllegalArgumentException
            | InvocationTargetException e)
      {
         fail("Fail on getTorrentUrl");
         e.printStackTrace();
      }

      try
      {
         Method methodGetDescription = MoviePlugin.class.getDeclaredMethod("getDescription", String.class);
         methodGetDescription.setAccessible(true);
         String description = (String) methodGetDescription.invoke(zamundaSe, TestTexts.the_queen_gambit);
         assertEquals(DESCRIPTION, description);
      }
      catch(NoSuchMethodException
            | SecurityException 
            | IllegalAccessException 
            | IllegalArgumentException
            | InvocationTargetException e)
      {
         fail("Fail on getTorrentUrl");
         e.printStackTrace();
      }
      
      try
      {
         Method methodGetMagnet = MoviePlugin.class.getDeclaredMethod("getMagnet", String.class);
         methodGetMagnet.setAccessible(true);
         String magnet = (String) methodGetMagnet.invoke(zamundaSe, TestTexts.the_queen_gambit);
         assertEquals(MAGNET, magnet);
      }
      catch(NoSuchMethodException
            | SecurityException 
            | IllegalAccessException 
            | IllegalArgumentException
            | InvocationTargetException e)
      {
         fail("Fail on getTorrentUrl");
         e.printStackTrace();
      }
      
      try
      {
         Method methodGetImageUrl = MoviePlugin.class.getDeclaredMethod("getImageUrl", String.class);
         methodGetImageUrl.setAccessible(true);
         String imageUrl = (String) methodGetImageUrl.invoke(zamundaSe, TestTexts.the_queen_gambit);
         assertEquals(IMAGE_URL, imageUrl);
      }
      catch(NoSuchMethodException
            | SecurityException 
            | IllegalAccessException 
            | IllegalArgumentException
            | InvocationTargetException e)
      {
         fail("Fail on getTorrentUrl");
         e.printStackTrace();
      }
      
//      zamundaSe.getURLsFromContent(sContent)
      
      ArrayList<CFile> alFiles1 = zamundaSe.checkContetWithPlugin("path", TestTexts.the_queen_gambit);
      int a = 1;
   }

}
