package tiebaspider;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.codingdie.tiebaspider.akka.message.QueryPageMessage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 2017/4/24.
 */
public class ServerNode {

    public  static int count=0;
    public static void main(String[] args) throws Exception {
        List<String> configPaths=new ArrayList<>();
        configPaths.add("slave.conf");
        if(args.length>0){
            configPaths.add(args[0]);
        }

        List<String>  slaves= getSlaves(configPaths);
        if(slaves.size()==0){
            System.out.println("找不到配置文件或未配置slave节点");
            return;
        }
        ActorSystem system = ActorSystem.create("server");
        List<ActorSelection> slaveActorSelections=new ArrayList<>();
        slaves.iterator().forEachRemaining(item->{
            ActorSelection queryPageTaskControlActor =system.actorSelection("akka.tcp://slave@"+item+":2552/user/QueryPageTaskControlActor");
            slaveActorSelections.add(queryPageTaskControlActor);
        });


        for(;count<20;count++){
            ActorSelection queryPageTaskControlActor=slaveActorSelections.get(count%slaveActorSelections.size());
            queryPageTaskControlActor.tell(new QueryPageMessage(count*50),queryPageTaskControlActor.anchor());
        }

    }

    private static List<String> getSlaves(List<String> confPaths) {
        List<String> slavesHosts=new ArrayList<>();

        try {
            String confPath= confPaths.stream().filter(s -> {
                if(new File(s).exists()){
                    System.out.println(new File(s).getAbsolutePath());

                    return true;
                }
                return  false;
            }).findFirst().orElse(null);
            if(confPath!=null){
                File file=new File(confPath);
                FileReader fileReader=new FileReader(file);
                BufferedReader bufferedReader=new BufferedReader(fileReader);
                String line=null;
                while ((line=bufferedReader.readLine())!=null){
                    slavesHosts.add(line);
                }
                bufferedReader.close();
                fileReader.close();
            }

       }catch (Exception ex){
           ex.printStackTrace();
       }finally {
            return slavesHosts;

        }

    }
}
