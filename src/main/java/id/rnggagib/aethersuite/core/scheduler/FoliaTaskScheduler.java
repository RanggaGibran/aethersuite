package id.rnggagib.aethersuite.core.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import id.rnggagib.aethersuite.api.scheduler.TaskScheduler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FoliaTaskScheduler implements TaskScheduler {
    private final JavaPlugin plugin;
    private final AtomicInteger nextTaskId = new AtomicInteger(1);
    private final Map<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();
    
    public FoliaTaskScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    private int storeTask(ScheduledTask task) {
        int id = nextTaskId.getAndIncrement();
        tasks.put(id, task);
        return id;
    }
    
    @Override
    public int runTask(Runnable task) {
        ScheduledTask scheduledTask = Bukkit.getServer().getGlobalRegionScheduler()
                .run(plugin, scheduledTaskContext -> task.run());
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskLater(Runnable task, long delayTicks) {
        ScheduledTask scheduledTask = Bukkit.getServer().getGlobalRegionScheduler()
                .runDelayed(plugin, scheduledTaskContext -> task.run(), delayTicks);
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        ScheduledTask scheduledTask = Bukkit.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, scheduledTaskContext -> task.run(), delayTicks, periodTicks);
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskAsync(Runnable task) {
        ScheduledTask scheduledTask = Bukkit.getServer().getAsyncScheduler()
                .runNow(plugin, scheduledTaskContext -> task.run());
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskLaterAsync(Runnable task, long delayTicks) {
        long delayMillis = delayTicks * 50; // Convert ticks to milliseconds
        ScheduledTask scheduledTask = Bukkit.getServer().getAsyncScheduler()
                .runDelayed(plugin, scheduledTaskContext -> task.run(), delayMillis, TimeUnit.MILLISECONDS);
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskTimerAsync(Runnable task, long delayTicks, long periodTicks) {
        long delayMillis = delayTicks * 50;
        long periodMillis = periodTicks * 50;
        ScheduledTask scheduledTask = Bukkit.getServer().getAsyncScheduler()
                .runAtFixedRate(plugin, scheduledTaskContext -> task.run(), delayMillis, periodMillis, TimeUnit.MILLISECONDS);
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskAtLocation(Runnable task, Location location) {
        ScheduledTask scheduledTask = Bukkit.getServer().getRegionScheduler()
                .run(plugin, location, scheduledTaskContext -> task.run());
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskLaterAtLocation(Runnable task, Location location, long delayTicks) {
        ScheduledTask scheduledTask = Bukkit.getServer().getRegionScheduler()
                .runDelayed(plugin, location, scheduledTaskContext -> task.run(), delayTicks);
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskTimerAtLocation(Runnable task, Location location, long delayTicks, long periodTicks) {
        ScheduledTask scheduledTask = Bukkit.getServer().getRegionScheduler()
                .runAtFixedRate(plugin, location, scheduledTaskContext -> task.run(), delayTicks, periodTicks);
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskForEntity(Consumer<Entity> task, Entity entity) {
        ScheduledTask scheduledTask = entity.getScheduler()
                .run(plugin, scheduledTaskContext -> task.accept(entity), () -> {});
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskLaterForEntity(Consumer<Entity> task, Entity entity, long delayTicks) {
        ScheduledTask scheduledTask = entity.getScheduler()
                .runDelayed(plugin, scheduledTaskContext -> task.accept(entity), () -> {}, delayTicks);
        return storeTask(scheduledTask);
    }
    
    @Override
    public int runTaskTimerForEntity(Consumer<Entity> task, Entity entity, long delayTicks, long periodTicks) {
        ScheduledTask scheduledTask = entity.getScheduler()
                .runAtFixedRate(plugin, scheduledTaskContext -> task.accept(entity), () -> {}, delayTicks, periodTicks);
        return storeTask(scheduledTask);
    }
    
    @Override
    public <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        Bukkit.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> {
            try {
                T result = supplier.get();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    @Override
    public void cancelTask(int taskId) {
        ScheduledTask task = tasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }
    
    @Override
    public void cancelAllTasks() {
        for (ScheduledTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
    }
}