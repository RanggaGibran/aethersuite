package id.rnggagib.aethersuite.core.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import id.rnggagib.aethersuite.api.scheduler.TaskScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PaperTaskScheduler implements TaskScheduler {
    private final JavaPlugin plugin;
    
    public PaperTaskScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public int runTask(Runnable task) {
        return Bukkit.getScheduler().runTask(plugin, task).getTaskId();
    }
    
    @Override
    public int runTaskLater(Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks).getTaskId();
    }
    
    @Override
    public int runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks).getTaskId();
    }
    
    @Override
    public int runTaskAsync(Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task).getTaskId();
    }
    
    @Override
    public int runTaskLaterAsync(Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks).getTaskId();
    }
    
    @Override
    public int runTaskTimerAsync(Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks).getTaskId();
    }
    
    @Override
    public int runTaskAtLocation(Runnable task, Location location) {
        return runTask(task);
    }
    
    @Override
    public int runTaskLaterAtLocation(Runnable task, Location location, long delayTicks) {
        return runTaskLater(task, delayTicks);
    }
    
    @Override
    public int runTaskTimerAtLocation(Runnable task, Location location, long delayTicks, long periodTicks) {
        return runTaskTimer(task, delayTicks, periodTicks);
    }
    
    @Override
    public int runTaskForEntity(Consumer<Entity> task, Entity entity) {
        return runTask(() -> task.accept(entity));
    }
    
    @Override
    public int runTaskLaterForEntity(Consumer<Entity> task, Entity entity, long delayTicks) {
        return runTaskLater(() -> task.accept(entity), delayTicks);
    }
    
    @Override
    public int runTaskTimerForEntity(Consumer<Entity> task, Entity entity, long delayTicks, long periodTicks) {
        return runTaskTimer(() -> task.accept(entity), delayTicks, periodTicks);
    }
    
    @Override
    public <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
        Bukkit.getScheduler().cancelTask(taskId);
    }
    
    @Override
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}