/* Generated By:JJTree&JavaCC: Do not edit this line. ExpressionParserConstants.java */
/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.expression.ast;

public interface ExpressionParserConstants {

  int EOF = 0;
  int DIGIT = 3;
  int DOT = 4;
  int BOOLEAN = 5;
  int STRING = 6;
  int CHAR = 7;
  int NEWLINE = 8;
  int LEFT_PARENTHEZIS = 9;
  int RIGHT_PARENTHEZIS = 10;
  int LEFT_BRACKET = 11;
  int RIGHT_BRACKET = 12;
  int COMMA = 13;
  int EQUALS = 14;
  int AT = 15;
  int CHAR_LETTER = 16;
  int ANNOTATION = 17;
  int IDENTIFIER = 18;
  int JAVA_NAME = 19;
  int JAVA_LETTER = 20;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "<DIGIT>",
    "\".\"",
    "<BOOLEAN>",
    "<STRING>",
    "<CHAR>",
    "\"\\n\"",
    "\"(\"",
    "\")\"",
    "\"{\"",
    "\"}\"",
    "\",\"",
    "\"=\"",
    "\"@\"",
    "<CHAR_LETTER>",
    "<ANNOTATION>",
    "<IDENTIFIER>",
    "<JAVA_NAME>",
    "<JAVA_LETTER>",
    "\"\\r\\n\"",
  };

}
