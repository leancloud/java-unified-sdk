package cn.leancloud.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Denotes that the annotated element should only be accessed from within a
 * specific scope (as defined by {@link Scope}).
 * <p>
 * Example of restricting usage within a library (based on gradle group ID):
 * <pre><code>
 *   &#64;RestrictTo(GROUP_ID)
 *   public void resetPaddingToInitialValues() { ...
 * </code></pre>
 * Example of restricting usage to tests:
 * <pre><code>
 *   &#64;RestrictScope(TESTS)
 *   public abstract int getUserId();
 * </code></pre>
 * Example of restricting usage to subclasses:
 * <pre><code>
 *   &#64;RestrictScope(SUBCLASSES)
 *   public void onDrawForeground(Canvas canvas) { ...
 * </code></pre>
 */
@Retention(CLASS)
@Target({ANNOTATION_TYPE,TYPE,METHOD,CONSTRUCTOR,FIELD,PACKAGE})
public @interface RestrictTo {
    /**
     * The scope to which usage should be restricted.
     */
    Scope[] value();
    enum Scope {
        /**
         * Restrict usage to code within the same group ID (based on gradle
         * group ID).
         */
        GROUP_ID,
        /**
         * Restrict usage to tests.
         */
        TESTS,
        /**
         * Restrict usage to subclasses of the enclosing class.
         * <p>
         * <strong>Note:</strong> This scope should not be used to annotate
         * packages.
         */
        SUBCLASSES,
    }
}