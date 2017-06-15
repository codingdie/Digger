package storage;

import akka.http.javadsl.model.DateTime;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.lang.reflect.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by xupeng on 2017/5/17.
 */
public class AkkaTest extends TestCase{

    public abstract  class  Foo<T>{
        public Foo(){

        }
    }
    public   class  A<T>{
        public A(){
            Foo<T> foo = new Foo<T>(){};
            Type mySuperClass = foo.getClass().getGenericSuperclass();
            Type type = ((ParameterizedType)mySuperClass).getActualTypeArguments()[0];
            System.out.println(type);


            System.out.println( new TypeToken<A<T>>(){}.getRawType());

        }
    }

     public  void  testA()throws Exception,Throwable{

          new  A<String>();
     }
     private void qsort(int[] tests,int begin ,int end){

         int k= qsortUnit(tests,begin,end);

         if(k-1-begin>1){
             qsort(tests,begin,k-1);
         }
         if(end-k-1>1){
             qsort(tests,k+1,end);
         }

     }
     private int  qsortUnit(int [] array,int low,int high){
         int key = array[low];
         while (low < high)
         {
                /*从后向前搜索比key小的值*/
             while (array[high] >= key && high > low)
                 --high;
                /*比key小的放左边*/
             array[low] = array[high];
                /*从前向后搜索比key大的值，比key大的放右边*/
             while (array[low] <= key && high > low)
                 ++low;
                /*比key大的放右边*/
             array[high] = array[low];
         }
            /*左边都比key小，右边都比key大。//将key放在游标当前位置。//此时low等于high */
         array[low] = key;

        return high;
     }
}
