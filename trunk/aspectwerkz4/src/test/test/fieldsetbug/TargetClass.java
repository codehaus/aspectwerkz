package test.fieldsetbug;


/**
 * @author Tomasz Mazan (beniamin)
 */
public class TargetClass {

    public int publicIntField;
    public char publicCharField;
    public long publicLongField;
    public double publicDoubleField;

    public TargetClass() {
        publicIntField    = 1;
        publicCharField   = 'a';
        publicLongField   = 1L;
        publicDoubleField = 1D;
    }

    public TargetClass(int value) {
        publicIntField = value;
    }

    public TargetClass(char value) {
        publicCharField = value;
    }
	
	public TargetClass(long value) {
		publicLongField = value;
	}
	
	public TargetClass(double value) {
		publicDoubleField = value;
	}
}
