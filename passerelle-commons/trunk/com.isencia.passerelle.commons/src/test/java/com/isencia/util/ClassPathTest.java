/* Copyright 2010 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.util;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * ClassPathTest
 * 
 * @author erwin.de.ley@isencia.be
 */
public class ClassPathTest extends TestCase {

	/*
	 * Class under test for void ClassPath(String)
	 */
	public void testClassPathString() {
	}

	/*
	 * Class under test for void ClassPath(String, FileFilter)
	 */
	public void testClassPathStringFileFilter() {
	}
	
	public void testRegExpFileFilter() {
		FileFilter fileFilter = new FileFilter() {

			public boolean accept(File pathname) {
				if(pathname!=null) {
					Pattern pattern1 = Pattern.compile("passerelle.*\\.(zip|jar)");
					Matcher m = pattern1.matcher(pathname.getName());
					return m.matches();
				} else {
					return false;
				}
			}
			
		};
		
		assertTrue(fileFilter.accept(new File("passerelle123.zip")));
		assertTrue(fileFilter.accept(new File("passerelle123.jar")));
		assertTrue(fileFilter.accept(new File("passerelle123.zip.jar")));
		assertFalse(fileFilter.accept(new File("passerelle123.zip.uerg")));
		assertFalse(fileFilter.accept(new File("123passerelle123.zip")));
	}

}
