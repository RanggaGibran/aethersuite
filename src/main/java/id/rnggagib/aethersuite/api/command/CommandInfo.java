package id.rnggagib.aethersuite.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {
    String name();
    String[] aliases() default {};
    String description() default "";
    String usage() default "";
    String permission() default "";
    boolean playerOnly() default false;
}