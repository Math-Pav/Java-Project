package Farm;

import java.util.Timer;
import java.util.TimerTask;

public class Crop {
    private String name;
    private boolean isReady;
    private int growthTime;

    public Crop(String name) {
        this.name = name;
        this.isReady = false;
        this.growthTime = 10 + (int) (Math.random() * 10);
    }

    public String getName() {
        return name;
    }

    public boolean isReady() {
        return isReady;
    }

    public void startGrowth(Runnable onGrowthComplete) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isReady = true;
                if (onGrowthComplete != null) {
                    onGrowthComplete.run();
                }
            }
        }, growthTime * 1000);
    }
}

