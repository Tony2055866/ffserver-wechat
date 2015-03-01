import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2015/3/1.
 */
public class ThreadTest {

    public static void main(String[] args){
        ExecutorService es = Executors.newFixedThreadPool(3);
        es.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println(1);

            }
        });
    }
}
