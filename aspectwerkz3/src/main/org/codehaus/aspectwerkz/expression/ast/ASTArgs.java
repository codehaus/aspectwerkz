/* Generated By:JJTree: Do not edit this line. ASTArgs.java */

package org.codehaus.aspectwerkz.expression.ast;

public class ASTArgs extends SimpleNode {
  public ASTArgs(int id) {
    super(id);
  }

  public ASTArgs(ExpressionParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
