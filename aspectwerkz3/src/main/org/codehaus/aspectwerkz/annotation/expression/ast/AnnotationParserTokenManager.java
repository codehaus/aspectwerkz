/* Generated By:JJTree&JavaCC: Do not edit this line. AnnotationParserTokenManager.java */

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.expression.ast;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Modifier;

public class AnnotationParserTokenManager implements AnnotationParserConstants {
    public static java.io.PrintStream debugStream = System.out;
    static final long[] jjbitVec0 = { 0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL };
    static final int[] jjnextStates = {
                                          57, 58, 60, 36, 37, 42, 43, 46, 47, 9, 51, 54, 55, 5, 6, 9, 38, 39, 9, 46, 47,
                                          9, 52, 53, 7, 8, 27, 28, 40, 41, 44, 45, 48, 49,
                                      };
    public static final String[] jjstrLiteralImages = {
                                                          "", null, null, "\56", null, null, null, null, null, null,
                                                          null, null, null, "\12", "\50", "\51", "\173", "\175", "\54",
                                                          "\75", "\100", null, null, null, null, null, "\15\12",
                                                      };
    public static final String[] lexStateNames = { "DEFAULT", };
    static final long[] jjtoToken = { 0x5fffcf9L, };
    static final long[] jjtoSkip = { 0x6L, };
    static protected SimpleCharStream input_stream;
    static private final int[] jjrounds = new int[61];
    static private final int[] jjstateSet = new int[122];
    static protected char curChar;
    static int curLexState = 0;
    static int defaultLexState = 0;
    static int jjnewStateCnt;
    static int jjround;
    static int jjmatchedPos;
    static int jjmatchedKind;

    public AnnotationParserTokenManager(SimpleCharStream stream) {
        if (input_stream != null)
            throw new TokenMgrError("ERROR: Second call to constructor of static lexer. You must use ReInit() to initialize the static variables.",
                                    TokenMgrError.STATIC_LEXER_ERROR);
        input_stream = stream;
    }

    public AnnotationParserTokenManager(SimpleCharStream stream, int lexState) {
        this(stream);
        SwitchTo(lexState);
    }

    public static void setDebugStream(java.io.PrintStream ds) {
        debugStream = ds;
    }

    private static final int jjStopStringLiteralDfa_0(int pos, long active0) {
        switch (pos) {
            case 0:
                if ((active0 & 0x100000L) != 0L)
                    return 34;
                if ((active0 & 0x8L) != 0L)
                    return 5;
                return -1;
            default:
                return -1;
        }
    }

    private static final int jjStartNfa_0(int pos, long active0) {
        return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
    }

    static private final int jjStopAtPos(int pos, int kind) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        return pos + 1;
    }

    static private final int jjStartNfaWithStates_0(int pos, int kind, int state) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            return pos + 1;
        }
        return jjMoveNfa_0(state, pos + 1);
    }

    static private final int jjMoveStringLiteralDfa0_0() {
        switch (curChar) {
            case 9:
                return jjStopAtPos(0, 2);
            case 10:
                return jjStopAtPos(0, 13);
            case 13:
                return jjMoveStringLiteralDfa1_0(0x4000000L);
            case 32:
                return jjStopAtPos(0, 1);
            case 40:
                return jjStopAtPos(0, 14);
            case 41:
                return jjStopAtPos(0, 15);
            case 44:
                return jjStopAtPos(0, 18);
            case 46:
                return jjStartNfaWithStates_0(0, 3, 5);
            case 61:
                return jjStopAtPos(0, 19);
            case 64:
                return jjStartNfaWithStates_0(0, 20, 34);
            case 123:
                return jjStopAtPos(0, 16);
            case 125:
                return jjStopAtPos(0, 17);
            default:
                return jjMoveNfa_0(0, 0);
        }
    }

    static private final int jjMoveStringLiteralDfa1_0(long active0) {
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(0, active0);
            return 1;
        }
        switch (curChar) {
            case 10:
                if ((active0 & 0x4000000L) != 0L)
                    return jjStopAtPos(1, 26);
                break;
            default:
                break;
        }
        return jjStartNfa_0(0, active0);
    }

    static private final void jjCheckNAdd(int state) {
        if (jjrounds[state] != jjround) {
            jjstateSet[jjnewStateCnt++] = state;
            jjrounds[state] = jjround;
        }
    }

    static private final void jjAddStates(int start, int end) {
        do {
            jjstateSet[jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }

    static private final void jjCheckNAddTwoStates(int state1, int state2) {
        jjCheckNAdd(state1);
        jjCheckNAdd(state2);
    }

    static private final void jjCheckNAddStates(int start, int end) {
        do {
            jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }

    static private final void jjCheckNAddStates(int start) {
        jjCheckNAdd(jjnextStates[start]);
        jjCheckNAdd(jjnextStates[start + 1]);
    }

    static private final int jjMoveNfa_0(int startState, int curPos) {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 61;
        int i = 1;
        jjstateSet[0] = startState;
        int j;
        int kind = 0x7fffffff;
        for (;;) {
            if (++jjround == 0x7fffffff)
                ReInitRounds();
            if (curChar < 64) {
                long l = 1L << curChar;
MatchLoop: 
                do {
                    switch (jjstateSet[--i]) {
                        case 0:
                            if ((0xffffff7bffffdbffL & l) != 0L) {
                                if (kind > 21)
                                    kind = 21;
                            } else if (curChar == 39)
                                jjstateSet[jjnewStateCnt++] = 30;
                            else if (curChar == 34)
                                jjCheckNAddTwoStates(27, 28);
                            if ((0x3ff081800000000L & l) != 0L) {
                                if (kind > 23)
                                    kind = 23;
                                jjCheckNAddStates(0, 2);
                            } else if (curChar == 46)
                                jjCheckNAdd(5);
                            else if (curChar == 45)
                                jjstateSet[jjnewStateCnt++] = 1;
                            if ((0x3ff000000000000L & l) != 0L)
                                jjCheckNAddStates(3, 9);
                            if ((0x3fe000000000000L & l) != 0L) {
                                if (kind > 4)
                                    kind = 4;
                                jjCheckNAddTwoStates(2, 3);
                            } else if (curChar == 48) {
                                if (kind > 4)
                                    kind = 4;
                                jjCheckNAddStates(10, 12);
                            }
                            break;
                        case 1:
                            if ((0x3fe000000000000L & l) == 0L)
                                break;
                            if (kind > 4)
                                kind = 4;
                            jjCheckNAddTwoStates(2, 3);
                            break;
                        case 2:
                            if ((0x3ff000000000000L & l) == 0L)
                                break;
                            if (kind > 4)
                                kind = 4;
                            jjCheckNAddTwoStates(2, 3);
                            break;
                        case 4:
                            if (curChar == 46)
                                jjCheckNAdd(5);
                            break;
                        case 5:
                            if ((0x3ff000000000000L & l) == 0L)
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjCheckNAddStates(13, 15);
                            break;
                        case 7:
                            if ((0x280000000000L & l) != 0L)
                                jjCheckNAdd(8);
                            break;
                        case 8:
                            if ((0x3ff000000000000L & l) == 0L)
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjCheckNAddTwoStates(8, 9);
                            break;
                        case 26:
                            if (curChar == 34)
                                jjCheckNAddTwoStates(27, 28);
                            break;
                        case 27:
                            if ((0xffffff7bffffdbffL & l) != 0L)
                                jjCheckNAddTwoStates(27, 28);
                            break;
                        case 28:
                            if ((curChar == 34) && (kind > 11))
                                kind = 11;
                            break;
                        case 29:
                            if (curChar == 39)
                                jjstateSet[jjnewStateCnt++] = 30;
                            break;
                        case 30:
                            if ((0xffffff7bffffdbffL & l) != 0L)
                                jjstateSet[jjnewStateCnt++] = 31;
                            break;
                        case 31:
                            if ((curChar == 39) && (kind > 12))
                                kind = 12;
                            break;
                        case 32:
                            if (((0xffffff7bffffdbffL & l) != 0L) && (kind > 21))
                                kind = 21;
                            break;
                        case 34:
                            if ((0x3ff081800000000L & l) == 0L)
                                break;
                            if (kind > 22)
                                kind = 22;
                            jjstateSet[jjnewStateCnt++] = 34;
                            break;
                        case 35:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjCheckNAddStates(3, 9);
                            break;
                        case 36:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjCheckNAddTwoStates(36, 37);
                            break;
                        case 37:
                            if (curChar != 46)
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjCheckNAddStates(16, 18);
                            break;
                        case 38:
                            if ((0x3ff000000000000L & l) == 0L)
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjCheckNAddStates(16, 18);
                            break;
                        case 40:
                            if ((0x280000000000L & l) != 0L)
                                jjCheckNAdd(41);
                            break;
                        case 41:
                            if ((0x3ff000000000000L & l) == 0L)
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjCheckNAddTwoStates(41, 9);
                            break;
                        case 42:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjCheckNAddTwoStates(42, 43);
                            break;
                        case 44:
                            if ((0x280000000000L & l) != 0L)
                                jjCheckNAdd(45);
                            break;
                        case 45:
                            if ((0x3ff000000000000L & l) == 0L)
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjCheckNAddTwoStates(45, 9);
                            break;
                        case 46:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjCheckNAddStates(19, 21);
                            break;
                        case 48:
                            if ((0x280000000000L & l) != 0L)
                                jjCheckNAdd(49);
                            break;
                        case 49:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjCheckNAddTwoStates(49, 9);
                            break;
                        case 50:
                            if (curChar != 48)
                                break;
                            if (kind > 4)
                                kind = 4;
                            jjCheckNAddStates(10, 12);
                            break;
                        case 52:
                            if ((0x3ff000000000000L & l) == 0L)
                                break;
                            if (kind > 5)
                                kind = 5;
                            jjAddStates(22, 23);
                            break;
                        case 54:
                            if ((0xff000000000000L & l) == 0L)
                                break;
                            if (kind > 6)
                                kind = 6;
                            jjCheckNAddTwoStates(54, 55);
                            break;
                        case 56:
                            if ((0x3ff081800000000L & l) == 0L)
                                break;
                            if (kind > 23)
                                kind = 23;
                            jjCheckNAddStates(0, 2);
                            break;
                        case 57:
                            if ((0x3ff081800000000L & l) == 0L)
                                break;
                            if (kind > 23)
                                kind = 23;
                            jjCheckNAddTwoStates(57, 58);
                            break;
                        case 58:
                            if (curChar == 46)
                                jjCheckNAdd(59);
                            break;
                        case 59:
                            if ((0x3ff081800000000L & l) == 0L)
                                break;
                            if (kind > 23)
                                kind = 23;
                            jjCheckNAddTwoStates(58, 59);
                            break;
                        case 60:
                            if ((0x3ff081800000000L & l) == 0L)
                                break;
                            if (kind > 24)
                                kind = 24;
                            jjCheckNAdd(60);
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else if (curChar < 128) {
                long l = 1L << (curChar & 077);
MatchLoop: 
                do {
                    switch (jjstateSet[--i]) {
                        case 0:
                            if ((0xffffffffefffffffL & l) != 0L) {
                                if (kind > 21)
                                    kind = 21;
                            }
                            if ((0x7fffffe87fffffeL & l) != 0L) {
                                if (kind > 23)
                                    kind = 23;
                                jjCheckNAddStates(0, 2);
                            } else if (curChar == 64)
                                jjCheckNAdd(34);
                            if (curChar == 70)
                                jjstateSet[jjnewStateCnt++] = 24;
                            else if (curChar == 84)
                                jjstateSet[jjnewStateCnt++] = 20;
                            else if (curChar == 102)
                                jjstateSet[jjnewStateCnt++] = 16;
                            else if (curChar == 116)
                                jjstateSet[jjnewStateCnt++] = 12;
                            break;
                        case 3:
                            if (((0x100000001000L & l) != 0L) && (kind > 4))
                                kind = 4;
                            break;
                        case 6:
                            if ((0x2000000020L & l) != 0L)
                                jjAddStates(24, 25);
                            break;
                        case 9:
                            if (((0x5000000050L & l) != 0L) && (kind > 7))
                                kind = 7;
                            break;
                        case 10:
                            if ((curChar == 101) && (kind > 10))
                                kind = 10;
                            break;
                        case 11:
                            if (curChar == 117)
                                jjCheckNAdd(10);
                            break;
                        case 12:
                            if (curChar == 114)
                                jjstateSet[jjnewStateCnt++] = 11;
                            break;
                        case 13:
                            if (curChar == 116)
                                jjstateSet[jjnewStateCnt++] = 12;
                            break;
                        case 14:
                            if (curChar == 115)
                                jjCheckNAdd(10);
                            break;
                        case 15:
                            if (curChar == 108)
                                jjstateSet[jjnewStateCnt++] = 14;
                            break;
                        case 16:
                            if (curChar == 97)
                                jjstateSet[jjnewStateCnt++] = 15;
                            break;
                        case 17:
                            if (curChar == 102)
                                jjstateSet[jjnewStateCnt++] = 16;
                            break;
                        case 18:
                            if ((curChar == 69) && (kind > 10))
                                kind = 10;
                            break;
                        case 19:
                            if (curChar == 85)
                                jjCheckNAdd(18);
                            break;
                        case 20:
                            if (curChar == 82)
                                jjstateSet[jjnewStateCnt++] = 19;
                            break;
                        case 21:
                            if (curChar == 84)
                                jjstateSet[jjnewStateCnt++] = 20;
                            break;
                        case 22:
                            if (curChar == 83)
                                jjCheckNAdd(18);
                            break;
                        case 23:
                            if (curChar == 76)
                                jjstateSet[jjnewStateCnt++] = 22;
                            break;
                        case 24:
                            if (curChar == 65)
                                jjstateSet[jjnewStateCnt++] = 23;
                            break;
                        case 25:
                            if (curChar == 70)
                                jjstateSet[jjnewStateCnt++] = 24;
                            break;
                        case 27:
                            if ((0xffffffffefffffffL & l) != 0L)
                                jjAddStates(26, 27);
                            break;
                        case 30:
                            if ((0xffffffffefffffffL & l) != 0L)
                                jjstateSet[jjnewStateCnt++] = 31;
                            break;
                        case 32:
                            if (((0xffffffffefffffffL & l) != 0L) && (kind > 21))
                                kind = 21;
                            break;
                        case 33:
                            if (curChar == 64)
                                jjCheckNAdd(34);
                            break;
                        case 34:
                            if ((0x7fffffe87fffffeL & l) == 0L)
                                break;
                            if (kind > 22)
                                kind = 22;
                            jjCheckNAdd(34);
                            break;
                        case 39:
                            if ((0x2000000020L & l) != 0L)
                                jjAddStates(28, 29);
                            break;
                        case 43:
                            if ((0x2000000020L & l) != 0L)
                                jjAddStates(30, 31);
                            break;
                        case 47:
                            if ((0x2000000020L & l) != 0L)
                                jjAddStates(32, 33);
                            break;
                        case 51:
                            if ((0x100000001000000L & l) != 0L)
                                jjCheckNAdd(52);
                            break;
                        case 52:
                            if ((0x7e0000007eL & l) == 0L)
                                break;
                            if (kind > 5)
                                kind = 5;
                            jjCheckNAddTwoStates(52, 53);
                            break;
                        case 53:
                            if (((0x100000001000L & l) != 0L) && (kind > 5))
                                kind = 5;
                            break;
                        case 55:
                            if (((0x100000001000L & l) != 0L) && (kind > 6))
                                kind = 6;
                            break;
                        case 56:
                            if ((0x7fffffe87fffffeL & l) == 0L)
                                break;
                            if (kind > 23)
                                kind = 23;
                            jjCheckNAddStates(0, 2);
                            break;
                        case 57:
                            if ((0x7fffffe87fffffeL & l) == 0L)
                                break;
                            if (kind > 23)
                                kind = 23;
                            jjCheckNAddTwoStates(57, 58);
                            break;
                        case 59:
                            if ((0x7fffffe87fffffeL & l) == 0L)
                                break;
                            if (kind > 23)
                                kind = 23;
                            jjCheckNAddTwoStates(58, 59);
                            break;
                        case 60:
                            if ((0x7fffffe87fffffeL & l) == 0L)
                                break;
                            if (kind > 24)
                                kind = 24;
                            jjCheckNAdd(60);
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else {
                int i2 = (curChar & 0xff) >> 6;
                long l2 = 1L << (curChar & 077);
MatchLoop: 
                do {
                    switch (jjstateSet[--i]) {
                        case 0:
                            if (((jjbitVec0[i2] & l2) != 0L) && (kind > 21))
                                kind = 21;
                            break;
                        case 27:
                            if ((jjbitVec0[i2] & l2) != 0L)
                                jjAddStates(26, 27);
                            break;
                        case 30:
                            if ((jjbitVec0[i2] & l2) != 0L)
                                jjstateSet[jjnewStateCnt++] = 31;
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                jjmatchedKind = kind;
                jjmatchedPos = curPos;
                kind = 0x7fffffff;
            }
            ++curPos;
            if ((i = jjnewStateCnt) == (startsAt = 61 - (jjnewStateCnt = startsAt)))
                return curPos;
            try {
                curChar = input_stream.readChar();
            } catch (java.io.IOException e) {
                return curPos;
            }
        }
    }

    static public void ReInit(SimpleCharStream stream) {
        jjmatchedPos = jjnewStateCnt = 0;
        curLexState = defaultLexState;
        input_stream = stream;
        ReInitRounds();
    }

    static private final void ReInitRounds() {
        int i;
        jjround = 0x80000001;
        for (i = 61; i-- > 0;) {
            jjrounds[i] = 0x80000000;
        }
    }

    static public void ReInit(SimpleCharStream stream, int lexState) {
        ReInit(stream);
        SwitchTo(lexState);
    }

    static public void SwitchTo(int lexState) {
        if ((lexState >= 1) || (lexState < 0))
            throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.",
                                    TokenMgrError.INVALID_LEXICAL_STATE);
        else
            curLexState = lexState;
    }

    static protected Token jjFillToken() {
        Token t = Token.newToken(jjmatchedKind);
        t.kind = jjmatchedKind;
        String im = jjstrLiteralImages[jjmatchedKind];
        t.image = (im == null) ? input_stream.GetImage() : im;
        t.beginLine = input_stream.getBeginLine();
        t.beginColumn = input_stream.getBeginColumn();
        t.endLine = input_stream.getEndLine();
        t.endColumn = input_stream.getEndColumn();
        return t;
    }

    public static Token getNextToken() {
        int kind;
        Token specialToken = null;
        Token matchedToken;
        int curPos = 0;

EOFLoop: 
        for (;;) {
            try {
                curChar = input_stream.BeginToken();
            } catch (java.io.IOException e) {
                jjmatchedKind = 0;
                matchedToken = jjFillToken();
                return matchedToken;
            }

            jjmatchedKind = 0x7fffffff;
            jjmatchedPos = 0;
            curPos = jjMoveStringLiteralDfa0_0();
            if (jjmatchedKind != 0x7fffffff) {
                if ((jjmatchedPos + 1) < curPos)
                    input_stream.backup(curPos - jjmatchedPos - 1);
                if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
                    matchedToken = jjFillToken();
                    return matchedToken;
                } else {
                    continue EOFLoop;
                }
            }
            int error_line = input_stream.getEndLine();
            int error_column = input_stream.getEndColumn();
            String error_after = null;
            boolean EOFSeen = false;
            try {
                input_stream.readChar();
                input_stream.backup(1);
            } catch (java.io.IOException e1) {
                EOFSeen = true;
                error_after = (curPos <= 1) ? "" : input_stream.GetImage();
                if ((curChar == '\n') || (curChar == '\r')) {
                    error_line++;
                    error_column = 0;
                } else
                    error_column++;
            }
            if (!EOFSeen) {
                input_stream.backup(1);
                error_after = (curPos <= 1) ? "" : input_stream.GetImage();
            }
            throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar,
                                    TokenMgrError.LEXICAL_ERROR);
        }
    }
}
