package com.kartersanamo.rift.api.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PlayerOnly {

    /**
     * The error message to send if executed from console.
     *
     * @return The error message
     */
    String message() default "This command can only be executed by players.";
}