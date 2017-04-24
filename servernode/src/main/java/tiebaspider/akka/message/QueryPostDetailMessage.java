package tiebaspider.akka.message;

/**
 * Created by xupeng on 2017/4/19.
 */
public class QueryPostDetailMessage {
    public QueryPostDetailMessage(String postId){
        this.postId=postId;
    }
    public String postId;
}
