/* Generated By:JJTree: Do not edit this line. ASTWithinCode.java */

package org.codehaus.aspectwerkz.expression.ast;

public class ASTWithinCode extends SimpleNode {
  public ASTWithinCode(int id) {
    super(id);
  }

  public ASTWithinCode(ExpressionParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
