import junit.framework.TestCase;

import java.io.*;
import java.nio.Buffer;

/**
 * Created by xupeng on 2017/5/10.
 */
public class FileTest extends TestCase {

   public void testModifyLine(){
       File file=new File("test.txt");
       int count=0;
       try {
           BufferedReader reader=new BufferedReader(new FileReader(file));
           BufferedWriter writer=new BufferedWriter(new FileWriter(file));

           String line=null;
           while ((line=reader.readLine())!=null){
               System.out.println(line);
               count++;
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
       assert count==3;
   }
}
