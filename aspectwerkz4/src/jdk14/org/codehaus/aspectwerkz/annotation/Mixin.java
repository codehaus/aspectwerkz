package org.codehaus.aspectwerkz.annotation;

/**
 * Mixin annotation
 * Annotate the mixin implementation class
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public interface Mixin {
    /**
     * Pointcut the mixin applies to (within / hasMethod / hasField)
     * When used, all others elements are assumed to their default value
     */
    public String value();

    /**
     * Pointcut the mixin applies to (within / hasMethod / hasField)
     * Used when deploymentModel / isTransient is specified
     */
    public String expression();

    /**
     * Mixin deployment model.
     * Defaults to ?? Only "perClass" and "perInstance" are supported for now
     * @see org.codehaus.aspectwerkz.DeploymentModel
     */
    public String deploymentModel();

    /**
     * True if mixin should behave as transient and not be serialized alongside the class it is introduced to.
     * Defaults to false.
     */
    public boolean isTransient();
}
