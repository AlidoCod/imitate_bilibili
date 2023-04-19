package org.demo.core.classes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatches {
	
	public static void main(String args[]) {
		String str = "bytes=-20";
		String pattern = "^bytes=\\d*-\\d*(/\\d*)?(,\\d*-\\d*(/\\d*)?)*$";
		String pattern1 = "^bytes=(-)?\\d*$";
		//^, $是匹配开始和结束，
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(str);
		System.out.println(m.matches());
	}

}