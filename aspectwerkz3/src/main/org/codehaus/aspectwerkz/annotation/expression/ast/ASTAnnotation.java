/* Generated By:JJTree: Do not edit this line. ASTAnnotation.java */

package org.codehaus.aspectwerkz.annotation.expression.ast;

public class ASTAnnotation extends SimpleNode {
  public ASTAnnotation(int id) {
    super(id);
  }

  public ASTAnnotation(ExpressionParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
