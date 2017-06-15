package com.codingdie.analyzer.storage.spider;

import com.codingdie.analyzer.spider.task.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.helper.StringUtil;


import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by xupeng on 2017/5/10.
 */
public class TaskStorage<T extends Task> {

    private File taskFile;


    private File root;
    private Class<T> tClass;

    public   TaskStorage(File rootPath,Class<T> tClass) {
        this.tClass=tClass;
        this.root = rootPath;
        this.taskFile = new File(root.getAbsolutePath() + File.separator+tClass.getSimpleName().toLowerCase()+".task"  );

        if (!this.taskFile.exists()) {
            try {
                this.taskFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public List<T> parseAndRebuild(){
        List<T> pageTs=parse();

        try {
            this.taskFile.delete();
            this.taskFile.createNewFile();

            if(pageTs.stream().anyMatch(i->{
                return i.status!=T.STATUS_FINISHED;
            })){
                pageTs.iterator().forEachRemaining(i->{
                    if(i.status!=T.STATUS_FINISHED){
                        i.status=T.STATUS_TODO;
                    }
                });
                 pageTs.sort((o1, o2) -> {
                    return o1.compareTo(o2);
                 });

                saveList(pageTs);
            }else{
                pageTs.clear();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return  pageTs;

    }

   private List<T> parse(){
       Map<String,T> pageTMap=new HashMap<>();
        try {
            BufferedReader bufferedReader=new BufferedReader(new FileReader(taskFile));
            String line=null;
            while ((line=bufferedReader.readLine())!=null){
                if(!StringUtil.isBlank(line)){
                    T task=new Gson().fromJson(line,tClass);
                    pageTMap.put(task.getKey(),task);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
       return  pageTMap.entrySet().stream().map(i->{
         return i.getValue();
       }).collect(Collectors.toList());
   }


    public void saveList(List<T> pageTs) {
        try {
            BufferedWriter todoWriter = new BufferedWriter(new FileWriter(taskFile,true));

            for(int i=0;i<pageTs.size();i++){
                T pageT=pageTs.get(i);
                try {
                    todoWriter.write(new Gson().toJson(pageT));
                    todoWriter.write("\n");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if(i%50==49){
                    todoWriter.flush();
                }
            }

            todoWriter.flush();
            todoWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void save(T task) {
        try {
            BufferedWriter todoWriter = new BufferedWriter(new FileWriter(taskFile,true));
            todoWriter.write(new Gson().toJson(task));
            todoWriter.write("\n");
            todoWriter.flush();
            todoWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
