package storage;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import junit.framework.TestCase;

import static akka.pattern.PatternsCS.ask;

/**
 * Created by xupeng on 2017/5/17.
 */
public class AkkaTest extends TestCase {

    public static class A extends AbstractActor {

        @Override
        public Receive createReceive() {
            return receiveBuilder().matchEquals("123", p -> {


                getSender().tell(System.currentTimeMillis(), null);
            }).build();
        }
    }

    public void testA() throws Exception {
        ActorSystem system = ActorSystem.create();

        ActorRef actor = system.actorOf(Props.create(A.class), "123");
        final long yime = System.currentTimeMillis();


        ask(actor, "123", 40000).toCompletableFuture().whenCompleteAsync((o, throwable) -> {

            System.out.println(o);
            if (o == null) o = "0";
//            print(Thread.currentThread());

            System.out.println("result1:" + (Long.valueOf(String.valueOf(o)).longValue() - yime));
        });

//        ask(actor, "123", 7000).toCompletableFuture().whenComplete((o, throwable) -> {
//            if (o == null) o = "0";
//            print(Thread.currentThread());
//
//            System.out.println("result2:" + (Long.valueOf(String.valueOf(o)).longValue() - yime));
//
//        });
//        ask(actor, "123", 12000).toCompletableFuture().whenComplete((o, throwable) -> {
//            if (o == null) o = "0";
//            print(Thread.currentThread());
//
//            System.out.println("result3:" + (Long.valueOf(String.valueOf(o)).longValue() - yime));
//        });
    }

    public void print(Thread root) {


        if (root == null) return;
        System.out.println("root:" + root.getId() + "\t" + root.getName());

        ThreadGroup group = root.getThreadGroup();
        Thread[] list = new Thread[group.activeCount()];
        group.enumerate(list);
        for (Thread thread : list) {
            System.out.println(thread.getId() + "\t" + thread.getName());
        }
    }
}
