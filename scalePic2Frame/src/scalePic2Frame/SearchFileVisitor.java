package scalePic2Frame;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/*
 * https://www.straub.as/java/history/walkfiletree.html
 */

public class SearchFileVisitor implements FileVisitor<Path>
{
   private String searchPattern = "";
   private List<Path> searchList;

   /**
      zulässige Pattern
      * steht für eine Gruppe von Buchstaben, die für Dateinamen zulässig sind: "abc*.*"
      ? steht für einen Buchstaben, der für Dateinamen zulässig ist: "abc?fg?.j*"
    */
   public static List<Path> searchFor(Path startDir, String searchPattern, boolean searchInFile) throws IOException
   {
      SearchFileVisitor fileVisitor = new SearchFileVisitor(searchPattern);
      Files.walkFileTree(startDir, fileVisitor);

      return fileVisitor.getResultList();
   }

   // private constructor
   private SearchFileVisitor(String searchPattern)
   {
      this.searchPattern = searchPattern;
      this.searchList = new ArrayList<>();
   }

   /**
    */
   @Override
   public FileVisitResult visitFile(Path path, BasicFileAttributes bfa) throws IOException
   {
      
      FileSystem fileSystem = FileSystems.getDefault();
      PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + searchPattern);
      // Man muß zum Vergleich den reinen Dateinamen nehmen ohne Pfad !!!
      if (pathMatcher.matches(path.getFileName()))
      {
         searchList.add(path);
      }
      return FileVisitResult.CONTINUE;
   }

   /**
    */
   @Override
   public FileVisitResult visitFileFailed(Path path, IOException ex) throws IOException
   {
      return FileVisitResult.CONTINUE;
   }
   
   @Override
   public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes bfa) throws IOException
   {
      return FileVisitResult.CONTINUE;
   }

   @Override
   public FileVisitResult postVisitDirectory(Path path, IOException ex) throws IOException
   {
      return FileVisitResult.CONTINUE;
   }


   public List<Path> getResultList()
   {
      return searchList;
   }
}
