/*
 * LICENSE:
 *   Inner logic adapted from a C++ original that was
 *   Copyright (C) 1999 Lucent Technologies
 *   Excerpted from 'The Practice of Programming'
 *   by Brian W. Kernighan and Rob Pike.
 * 
 *   Included by permission of the http://tpop.awl.com/ web site, 
 *   which says:
 *   "You may use this code for any purpose, as long as you leave 
 *   the copyright notice and book citation attached." I have done so.
 *   
 *   Taken from OpenMed project implementation.
 *   [http://sourceforge.net/projects/openmed]
 *   Under The OpenEMed License:
 *    (Copyright 1997, 1998, 1999, 2000, 2001, 2002, 2003 Regents of the University of California. All rights reserved.)
 *    
 *    Permission for the redistribution and use of this software, in source
 *    and binary forms, with or without modification, is made provided that
 *    the following conditions are met. 
 *    
 *      o Redistributions of the OpenEMed source code must reproduce the
 *        following copyright notice, this list of conditions, and the following
 *       disclaimer. 
 *   
 *     o Redistributions in binary form must reproduce the following copyright
 *       notice, this list of conditions, and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution. 
 *   
 *     o All advertising materials mentioning features or use of this software
 *       must display the following acknowledgement: "This product includes
 *       software developed by the University of California, operator of the Los
 *       Alamos National Laboratory under Contract No. W-7405-ENG-36 with the
 *       U.S. Department of Energy, and its contributors."
 *   
 *     o Neither name of the University nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission. 
 *   
 *     o If software is modified to produce derivative works, such modified
 *       software should be clearly marked, so as not to confuse it with the
 *       original version available from the University of California. 
 *   
 *   Copyright Notice
 *   Copyright (c) 2000, Regents of the University of California. All rights reserved.
 *   
 *   DISCLAIMER
 *   
 *   THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS'' AND
 *   ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OF THE UNIVERSITY
 *   OF CALIFORNIA, THE U.S. GOVERNMENT, OR CONTRIBUTORS BE LIABLE FOR ANY
 *   DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *   DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 *   OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *   HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *   STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *   ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE.  NEITHER THE REGENTS OF THE UNIVERSITY OF
 *   CALIFORNIA NOR THE U.S. GOVERNMENT REPRESENT OR WARRANT THAT USE OR
 *   DUPLICATION OF THIS SOFTWARE WILL NOT INFRINGE ANY INTELLECTUAL PROPERTY
 *   RIGHTS OF OTHERS. 
 */
package mx.ecosur.multigame.util;	

/** 
 * Parse comma-separated values (CSV), a common Windows file format.
 * Sample input: "LU",86.25,"11/4/1998","2:19PM",+4.0625
 * 
 * @author Brian W. Kernighan and Rob Pike (C++ original)
 * @author Ian F. Darwin (translation into Java and removal of I/O)
 * @author Ben Ballard (rewrote advQuoted to handle '""' and for readability)
 * @author Andrew Waterman <awaterma@ecosur.mx> (Modified Logger implementation
 * to use the Java logging library)
 * 
 */
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class CSV {

	public static final char DEFAULT_SEP = ',';
    private static Logger log = Logger.getLogger(
			CSV.class.getCanonicalName());

	/** Construct a CSV parser, with the default separator (`,'). */
	public CSV() {
		this(DEFAULT_SEP);
	}

	/** Construct a CSV parser with a given separator. Must be
	 * exactly the string that is the separator, not a list of
	 * separator characters!
	 */
	public CSV(char sep) {
		fieldSep = sep;
	}

	/** The fields in the current String */
	protected ArrayList list = new ArrayList();

	/** the separator char for this parser */
	protected char fieldSep;

	/** parse: break the input String into fields
	 * @return java.util.Iterator containing each field 
	 * from the original as a String, in order.
	 */
	public List parse(String line)
	{
		StringBuffer sb = new StringBuffer();
		list.clear();			// discard previous, if any
		int i = 0;

		if (line.length() == 0) {
			list.add(line);
			return list;
		}

		do {
            sb.setLength(0);
            if (i < line.length() && line.charAt(i) == '"')
                i = advQuoted(line, sb, ++i);	// skip quote
            else
                i = advPlain(line, sb, i);
            list.add(sb.toString());
            log.finest("parse: " +sb.toString());
			i++;
		} while (i < line.length());

		return list;
	}

	/** advQuoted: quoted field; return index of next separator */
	protected int advQuoted(String s, StringBuffer sb, int i)
	{
		int j;
		int len= s.length();
        for (j=i; j<len; j++) {
            if (s.charAt(j) == '"' && j+1 < len) {
                if (s.charAt(j+1) == '"') {
                    j++; // skip escape char
                } else if (s.charAt(j+1) == fieldSep) { //next delimeter
                    j++; // skip end quotes
                    break;
                }
            } else if (s.charAt(j) == '"' && j+1 == len) { // end quotes at end of line
                break; //done
			}
			sb.append(s.charAt(j));	// regular character.
		}
		return j;
	}

	/** advPlain: unquoted field; return index of next separator */
	protected int advPlain(String s, StringBuffer sb, int i)
	{
		int j;

		j = s.indexOf(fieldSep, i); // look for separator
		log.finest("csv: " + "i = " + i + " j = " + j);
        if (j == -1) {               	// none found
            sb.append(s.substring(i));
            return s.length();
        } else {
            sb.append(s.substring(i, j));
            return j;
        }
    }
}


