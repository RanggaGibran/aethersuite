package id.rnggagib.aethersuite.core.teleport;

import java.time.Instant;
import java.util.UUID;

public class TeleportRequest {
    public enum Type {
        TPA,        // Request to teleport to someone
        TPAHERE     // Request for someone to teleport to you
    }
    
    private final UUID senderUuid;
    private final UUID targetUuid;
    private final Type type;
    private final Instant timestamp;
    
    public TeleportRequest(UUID senderUuid, UUID targetUuid, Type type) {
        this.senderUuid = senderUuid;
        this.targetUuid = targetUuid;
        this.type = type;
        this.timestamp = Instant.now();
    }
    
    public UUID getSenderUuid() {
        return senderUuid;
    }
    
    public UUID getTargetUuid() {
        return targetUuid;
    }
    
    public Type getType() {
        return type;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
}