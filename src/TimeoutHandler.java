import java.io.IOException;
import java.util.TimerTask;


/**
 * Timeout handler class that will call Router.sendDistanceVector();
 * periodically at fixed intervals in order to update other routers.
 *
 * @author Raza Qazi
 * @version 1.0
 */
public class TimeoutHandler extends TimerTask {
    private Router r;

    // Initialize instance of Router
    public TimeoutHandler(Router router) {
        this.r = router;
    }

    @Override
    public void run() {
        try {
            // Call sendDistanceVector in worker thread.
            this.r.sendDistanceVector();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
