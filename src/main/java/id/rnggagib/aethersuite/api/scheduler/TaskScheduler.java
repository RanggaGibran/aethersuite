package id.rnggagib.aethersuite.api.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface TaskScheduler {
    int runTask(Runnable task);
    
    int runTaskLater(Runnable task, long delayTicks);
    
    int runTaskTimer(Runnable task, long delayTicks, long periodTicks);
    
    int runTaskAsync(Runnable task);
    
    int runTaskLaterAsync(Runnable task, long delayTicks);
    
    int runTaskTimerAsync(Runnable task, long delayTicks, long periodTicks);
    
    int runTaskAtLocation(Runnable task, Location location);
    
    int runTaskLaterAtLocation(Runnable task, Location location, long delayTicks);
    
    int runTaskTimerAtLocation(Runnable task, Location location, long delayTicks, long periodTicks);
    
    int runTaskForEntity(Consumer<Entity> task, Entity entity);
    
    int runTaskLaterForEntity(Consumer<Entity> task, Entity entity, long delayTicks);
    
    int runTaskTimerForEntity(Consumer<Entity> task, Entity entity, long delayTicks, long periodTicks);
    
    <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier);
    
    void cancelTask(int taskId);
    
    void cancelAllTasks();
}