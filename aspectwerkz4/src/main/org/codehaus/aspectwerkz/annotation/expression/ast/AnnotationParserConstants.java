/* Generated By:JJTree&JavaCC: Do not edit this line. AnnotationParserConstants.java */
/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.expression.ast;

public interface AnnotationParserConstants {

  int EOF = 0;
  int DOT = 3;
  int INTEGER = 4;
  int HEXNUMBER = 5;
  int OCTNUMBER = 6;
  int FLOAT = 7;
  int EXPONENT = 8;
  int DIGIT = 9;
  int BOOLEAN = 10;
  int STRING = 11;
  int CHAR = 12;
  int NEWLINE = 13;
  int LEFT_PARENTHEZIS = 14;
  int RIGHT_PARENTHEZIS = 15;
  int LEFT_BRACKET = 16;
  int RIGHT_BRACKET = 17;
  int COMMA = 18;
  int EQUALS = 19;
  int AT = 20;
  int ANNOTATION = 21;
  int JAVA_NAME = 22;
  int JAVA_TYPE_MAYBEARRAY = 23;
  int JAVA_TYPE = 24;
  int JAVA_LETTER = 25;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\".\"",
    "<INTEGER>",
    "<HEXNUMBER>",
    "<OCTNUMBER>",
    "<FLOAT>",
    "<EXPONENT>",
    "<DIGIT>",
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
    "<ANNOTATION>",
    "<JAVA_NAME>",
    "<JAVA_TYPE_MAYBEARRAY>",
    "<JAVA_TYPE>",
    "<JAVA_LETTER>",
    "\"\\r\\n\"",
  };

}
