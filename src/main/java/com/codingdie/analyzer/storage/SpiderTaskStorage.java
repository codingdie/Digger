package com.codingdie.analyzer.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by xupeng on 2017/5/10.
 */
public class SpiderTaskStorage {

    private File todoTaskFile;
    private File executingTaskFile;
    private File finishedTaskFile;
    private File failedTaskFile;

    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    private File root;

    public SpiderTaskStorage(File rootPath){
        this.root=rootPath;
        this.todoTaskFile =new File(root.getAbsolutePath()+File.separator+"todo.task");
        this.executingTaskFile =new File(root.getAbsolutePath()+File.separator+"executing.task");
        this.finishedTaskFile =new File(root.getAbsolutePath()+File.separator+"finished.task");
        this.failedTaskFile =new File(root.getAbsolutePath()+File.separator+"failed.task");

        if(!this.todoTaskFile.exists()){
            try {
                this.todoTaskFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!this.executingTaskFile.exists()){
            try {
                this.executingTaskFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!this.finishedTaskFile.exists()){
            try {
                this.finishedTaskFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!this.failedTaskFile.exists()){
            try {
                this.failedTaskFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
