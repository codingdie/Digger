package tiebaspider.akka.message;

/**
 * Created by xupeng on 2017/4/19.
 */
public class QueryPageMessage {
    public  QueryPageMessage(int pn){
        this.pn=pn;
    }
    public int pn=50;
}
