package lii.buildmaster.projecttracker.annotation;

import lii.buildmaster.projecttracker.model.enums.ActionType;
import lii.buildmaster.projecttracker.model.enums.EntityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    ActionType action();

    EntityType entityType();

    String entityIdParam() default "id";

    boolean captureResult() default false;

    boolean captureBeforeState() default false;

    String description() default "";
}
