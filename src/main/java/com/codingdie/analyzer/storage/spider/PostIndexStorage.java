package com.codingdie.analyzer.storage.spider;

import com.codingdie.analyzer.spider.model.PostIndex;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by xupeng on 2017/5/10.
 */
public class PostIndexStorage {
    Logger logger=Logger.getLogger("postindex");
    public static int FILE_SIZE = 10;
    private File root;
    private List<File> indexFiles = new ArrayList<>(FILE_SIZE);
    private ConcurrentHashMap<Long,PostIndex> longPostIndexHashMap=new ConcurrentHashMap<>(200000);
    public PostIndexStorage(File rootPath) {
        try {
            this.root = rootPath;
            for (int i = 0; i < FILE_SIZE; i++) {
                NumberFormat format = NumberFormat.getInstance();
                format.setMaximumIntegerDigits(2);
                format.setMinimumIntegerDigits(2);
                File indexFile = new File(root.getAbsolutePath() + File.separator + (format.format(i) + "_post.index"));
                if (!indexFile.exists()) {
                    indexFile.createNewFile();
                }
                indexFiles.add(indexFile);
            }
            refreshAndInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshAndInit() {
        final List<File> rebuildFileList=new ArrayList<>();
        for (int i = 0; i < FILE_SIZE; i++) {
            NumberFormat format = NumberFormat.getInstance();
            format.setMaximumIntegerDigits(2);
            format.setMinimumIntegerDigits(2);
            File indexFile = new File(root.getAbsolutePath() + File.separator + (format.format(i) + "_post.index.rebuild"));
            if (!indexFile.exists()) {
                try {
                    indexFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                rebuildFileList.add(indexFile);
            }
        }
        for(int i=0;i<indexFiles.size();i++){
            try {
                File indexFile=indexFiles.get(i);
                File indexFileRebuld=rebuildFileList.get(i);

                BufferedReader bufferedReader=new BufferedReader(new FileReader(indexFile));
                String line=null;

                HashMap<Long,PostIndex> tmpMap=new HashMap<>(2000);

                while ((line=bufferedReader.readLine())!=null){
                    if(!StringUtil.isBlank(line)){
                        PostIndex postIndex=new Gson().fromJson(line,PostIndex.class);

                        longPostIndexHashMap.put(postIndex.getPostId(),postIndex);
                        tmpMap.put(postIndex.getPostId(),postIndex);

                    }
                }
                final  BufferedWriter todoWriter = new BufferedWriter(new FileWriter(indexFileRebuld,true));

                tmpMap.entrySet().stream().forEach(item->{
                    try {
                        todoWriter.write(new Gson().toJson(item.getValue()));
                        todoWriter.write("\n");

                        todoWriter.flush();
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                });
                todoWriter.close();


                indexFile.delete();
                indexFileRebuld.renameTo(indexFile);
                indexFileRebuld.delete();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }


    public void putIndex(PostIndex postIndex){
        if(longPostIndexHashMap.containsKey(postIndex.getPostId())){
            logger.info("duplicate:"+new Gson().toJson(postIndex));
        }else{
            longPostIndexHashMap.put(postIndex.getPostId(),postIndex);
            saveIndex(postIndex);
        }
    }

    public void modifyIndex(PostIndex postIndex){
        if(!longPostIndexHashMap.containsKey(postIndex.getPostId())){
            logger.info("index not exit");
        }else{
            longPostIndexHashMap.put(postIndex.getPostId(),postIndex);
            saveIndex(postIndex);
        }
    }


    public int countAllIndex(){
       return longPostIndexHashMap.size();
    }
    public PostIndex getIndex(long postId){
      return  longPostIndexHashMap.get(postId);
    }

    public void iterateNoContentIndex(Consumer<PostIndex> function){

        longPostIndexHashMap.forEachEntry(1,longPostIndexEntry -> {
            PostIndex value = longPostIndexEntry.getValue();
            if(value.getStatus()==PostIndex.STATUS_NO_CONTENT) {
                function.accept(value);
            }
        });
    }


    private void saveIndex(PostIndex postIndex){
        try {
            int index = (int) (postIndex.getPostId()  % FILE_SIZE) ;

            File file=indexFiles.get(index);
            BufferedWriter todoWriter = new BufferedWriter(new FileWriter(file,true));
            todoWriter.write(new Gson().toJson(postIndex));
            todoWriter.write("\n");
            todoWriter.flush();
            todoWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
