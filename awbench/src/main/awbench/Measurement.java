package awbench;

/**
 * Interface for weaved class, to allow some warmup phase for JIT or aspectOf etc 
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public interface Measurement {

    /**
     * Some warm-up code
     * Note: Might need to invoke it once per instance to warmup some perInstance based test
     */
    public void warmup();
}
