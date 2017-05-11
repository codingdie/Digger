package com.codingdie.analyzer.storage;

import java.io.File;

/**
 * Created by xupeng on 2017/5/10.
 */
public class TieBaFileSystem {
    public  static  final int ROLE_MASTER=0;
    public  static  final int ROLE_SLAVE=1;

    private  SpiderTaskStorage spiderTaskStorage;
    private  PostIndexStorage postIndexStorage;
    private  PostContentStorage postContentStorage;

    private File root;
    public TieBaFileSystem(String name,int role){
        this.root=new File("storage/"+name);
        if(!this.root.exists()){
            this.root.mkdirs();
        }
        if(role==ROLE_MASTER){
            File spiderTaskRootPath = new File(root.getAbsolutePath() + File.separatorChar + "spidertask");
            if(!spiderTaskRootPath.exists()){
                spiderTaskRootPath.mkdirs();
            }
            this.spiderTaskStorage=new SpiderTaskStorage(spiderTaskRootPath);
            File postIndexRootPath = new File(root.getAbsolutePath() + File.separatorChar + "postindex");
            if(!postIndexRootPath.exists()){
                postIndexRootPath.mkdirs();
            }
            this.postIndexStorage=new PostIndexStorage(postIndexRootPath);
        }
        if(role==ROLE_SLAVE){
            File postContentRootPath = new File(root.getAbsolutePath() + File.separatorChar + "postcontent");
            if(!postContentRootPath.exists()){
                postContentRootPath.mkdirs();
            }
            this.postContentStorage=new PostContentStorage(postContentRootPath);
        }
    }

    public SpiderTaskStorage getSpiderTaskStorage() {
        return spiderTaskStorage;
    }

    public PostIndexStorage getPostIndexStorage() {
        return postIndexStorage;
    }

    public PostContentStorage getPostContentStorage() {
        return postContentStorage;
    }

    public void clear(){
        this.root.delete();
    }

}
