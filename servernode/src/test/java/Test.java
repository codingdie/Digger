import com.firetalk.client.FTConnection;
import com.firetalk.client.task.TaskCallback;
import com.firetalk.client.exception.FTException;

/**
 * Created by xupeng on 2017/1/19.
 */
public class Test {
    public static void main(String[] args) {
        FTConnection ftConnection =new FTConnection();
        ftConnection.connect("127.0.0.1", 9700, new TaskCallback() {
            public void success() {
                System.out.println(ftConnection.getHost()+":"+ ftConnection.getPort()+":"+ ftConnection.getState());
            }

            public void failed(FTException e) {
                e.printStackTrace();
                ftConnection.destroy();
            }
        });
    }
}
