import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        //String url = System.getenv().getOrDefault("TARGET_URL", "http://notify-service:8080/notify");
        String content = URLEncoder.encode("联通直连25号前需要充值！！", StandardCharsets.UTF_8) ;
        String url = String.format("http://www.todofix.ltd:5506/wxsend?content=%s", content);
        String targetTimeStr = System.getenv().getOrDefault("TARGET_TIME", "11:00");
        int targetDay = Integer.parseInt(System.getenv().getOrDefault("TARGET_DAY", "20"));
        
        LocalTime targetTime = LocalTime.parse(targetTimeStr);
        LocalDateTime now = LocalDateTime.now();
        
        // Calculate next run time
        LocalDateTime nextRun = now.withDayOfMonth(targetDay).with(targetTime);
        
        // If the target time for this month has already passed, schedule for next month
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusMonths(1);
        }
        
        long initialDelay = Duration.between(now, nextRun).getSeconds();
        // Since months have variable lengths, we cannot use scheduleAtFixedRate with a fixed period.
        // Instead, we will use schedule and then recursively schedule the next run.
        
        System.out.println("Current time: " + now);
        System.out.println("Next run time: " + nextRun);
        System.out.println("Initial delay: " + initialDelay + " seconds");

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    int code = conn.getResponseCode();
                    System.out.println("Notify trigger response: " + code);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Schedule next run
                    LocalDateTime currentNow = LocalDateTime.now();
                    LocalDateTime nextMonthRun = currentNow.plusMonths(1).withDayOfMonth(targetDay).with(targetTime);
                    long delay = Duration.between(currentNow, nextMonthRun).getSeconds();
                    System.out.println("Next run scheduled at: " + nextMonthRun);
                    executor.schedule(this, delay, TimeUnit.SECONDS);
                }
            }
        };
        
        executor.schedule(task, initialDelay, TimeUnit.SECONDS);
    }
}
