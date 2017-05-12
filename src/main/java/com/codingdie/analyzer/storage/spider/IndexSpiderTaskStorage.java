package com.codingdie.analyzer.storage.spider;

import com.codingdie.analyzer.spider.model.PageTask;
import com.google.gson.Gson;
import org.jsoup.helper.StringUtil;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by xupeng on 2017/5/10.
 */
public class IndexSpiderTaskStorage {

    private File taskFile;


    private File root;

    public IndexSpiderTaskStorage(File rootPath) {
        this.root = rootPath;
        this.taskFile = new File(root.getAbsolutePath() + File.separator + "task.task");

        if (!this.taskFile.exists()) {
            try {
                this.taskFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public List<PageTask> parseAndRebuild(){
        List<PageTask> pageTasks=parse();

        try {
            this.taskFile.delete();
            this.taskFile.createNewFile();

            if(pageTasks.stream().anyMatch(i->{
                return i.status!=PageTask.STATUS_FINISHED;
            })){
                pageTasks.iterator().forEachRemaining(i->{
                    if(i.status!=PageTask.STATUS_FINISHED){
                        i.status=PageTask.STATUS_TODO;
                    }
                });
                pageTasks.sort((o1, o2) ->
                {
                    return  (o1.pn-o2.pn)*10+(o1.status-o2.status);
                });
                saveTasks(pageTasks);
            }else{
                pageTasks.clear();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return  pageTasks;

    }

   private List<PageTask> parse(){
       Map<Integer,PageTask> pageTaskMap=new HashMap<>();
        try {
            BufferedReader bufferedReader=new BufferedReader(new FileReader(taskFile));
            String line=null;
            while ((line=bufferedReader.readLine())!=null){
                if(!StringUtil.isBlank(line)){
                    PageTask pageTask=new Gson().fromJson(line,PageTask.class);
                    pageTaskMap.put(pageTask.pn,pageTask);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
       return  pageTaskMap.entrySet().stream().map(i->{
         return i.getValue();
       }).collect(Collectors.toList());
   }


    public void saveTasks(List<PageTask> pageTasks) {
        try {
            BufferedWriter todoWriter = new BufferedWriter(new FileWriter(taskFile,true));

            for(int i=0;i<pageTasks.size();i++){
                PageTask pageTask=pageTasks.get(i);
                try {
                    todoWriter.write(new Gson().toJson(pageTask));
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
    public void saveTask(PageTask pageTask) {
        try {
            BufferedWriter todoWriter = new BufferedWriter(new FileWriter(taskFile,true));
            todoWriter.write(new Gson().toJson(pageTask));
            todoWriter.write("\n");
            todoWriter.flush();
            todoWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
