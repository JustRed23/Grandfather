package dev.JustRed23.grandfather.services;

import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.grandfather.utils.InactivityTimer;
import dev.JustRed23.stonebrick.service.Service;

import java.util.concurrent.TimeUnit;

public class InactivityService extends Service {

    public boolean shouldRun() {
        return Bot.enabled;
    }

    public long delayBetweenRuns() {
        return TimeUnit.SECONDS.toMillis(1);
    }

    public void run() {
        //InactivityTimer.check();
    }
}
