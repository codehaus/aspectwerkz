/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench;

import org.codehaus.aspectwerkz.util.Strings;

import java.io.File;
import java.io.LineNumberReader;
import java.io.FileReader;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Iterator;

import gnu.trove.TIntArrayList;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Output {

    static final List heads = new ArrayList();

    public static void main(String args[]) throws Throwable {
        heads.add("before");
        heads.add("before, SJP");
        heads.add("before, JP");
        heads.add("after returning");
        heads.add("after throwing");
        heads.add("before + after");
        heads.add("before, args() primitives");
        heads.add("before, args() objects");
        heads.add("before, args() target()");
        heads.add("around, JP");
        heads.add("around, SJP");
        heads.add("around x 2, args() target()");

        LineNumberReader reader = new LineNumberReader(
                new FileReader(args[0])
        );

        List fks = new ArrayList();

        String lastFkRunnned = null;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (lookupFk(line)!=null) {
                lastFkRunnned = lookupFk(line);
            } else if (lookupResults(line) != null) {
                Fk fk = new Fk();
                fk.name = lastFkRunnned;
                System.out.println("********* " + lastFkRunnned);
                fk.parseResults(lookupResults(line));
                fks.add(fk);
            } else {
                ;
            }
        }

        crlf();crlf();crlf();
        dump(fks);
        crlf();crlf();crlf();
        dumpRelative(fks);
    }

    static void dump(List fks) {
        // headers
        col();col();p("AWBench (ns/invocation)");col();col();
        for (Iterator iterator = fks.iterator(); iterator.hasNext();) {
            Fk fk = (Fk) iterator.next();
            p(fk.name);col();col();
        }

        // results
        crlf();
        int metrics = heads.size();
        for (int i = 0; i < metrics; i++) {
            col();p(heads.get(i).toString());col();
            for (Iterator iterator = fks.iterator(); iterator.hasNext();) {
                Fk fk = (Fk) iterator.next();
                int time = fk.results.get(i);
                if (time > 0) {
                    p(""+time);
                } else {
                    p("NA");
                }
                col();
            }
            crlf();
        }
    }

    static void dumpRelative(List fks) {
        // headers
        col();col();p("AWBench (relative %)");col();col();
        for (Iterator iterator = fks.iterator(); iterator.hasNext();) {
            Fk fk = (Fk) iterator.next();
            p(fk.name);col();col();
        }

        // results
        crlf();
        int metrics = heads.size();
        for (int i = 0; i < metrics; i++) {
            col();p(heads.get(i).toString());col();
            for (Iterator iterator = fks.iterator(); iterator.hasNext();) {
                Fk fk = (Fk) iterator.next();
                int time = fk.results.get(i);
                if (time > 0) {
                    int refTime = ((Fk)fks.get(0)).results.get(i);
                    if (refTime > 0) {
                        int relative = (int) (100 * time / refTime);
                        p(""+relative);
                    } else {
                        p("NA");
                    }
                } else {
                    p("NA");
                }
                col();
            }
            crlf();
        }
    }

    static void p(String s) {
        System.out.print(s);
    }
    static void crlf() {
        System.out.println("");
    }
    static void col() {
        p("|");
    }

    static String lookupFk(String line) {
        if (line.startsWith("run:") && line.length() > 4) {
            String name = line.substring(4).trim();
            if (name.endsWith(":"))
                name = name.substring(0, name.length()-1);
            return name;
        } else {
            return null;
        }
    }

    static String lookupResults(String line) {
        if (line.indexOf("CSV : ") > 0) {
            return line.substring(line.indexOf(":") + 1).trim();
        } else {
            return null;
        }
    }

    static class Fk {
        String name;
        TIntArrayList results = new TIntArrayList();

        void parseResults(String csv) {
            String[] res = Strings.splitString(csv, ";");
            for (int i = 0; i < res.length; i++) {
                String re = res[i];
                if (re != null && re.length() > 0) {
                    System.out.println("x " + re);
                    results.add(Integer.parseInt(re));
                } else {
                    results.add(0);
                }
            }
        }
    }
}
