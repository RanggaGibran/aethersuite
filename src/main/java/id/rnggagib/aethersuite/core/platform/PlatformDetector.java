package id.rnggagib.aethersuite.core.platform;

public class PlatformDetector {
    private static final String FOLIA_CLASS = "io.papermc.paper.threadedregions.RegionizedServer";
    
    public static PlatformType detectPlatform() {
        try {
            Class.forName(FOLIA_CLASS);
            return PlatformType.FOLIA;
        } catch (ClassNotFoundException e) {
            return PlatformType.PAPER;
        }
    }
}