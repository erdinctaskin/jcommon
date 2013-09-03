/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tr.com.serkanozal.jcommon.util;

/**
 * @author Serkan ÖZAL
 */
public class StringUtil {

	public static final String ARGUMENT_SIGNER = "$";
	
	private StringUtil() {
		
	}
	
	public static String bindArguments(String str, Object ... args) {
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				Object arg = args[i];
				String argStr = (arg == null) ? "null" : arg.toString();
				str = str.replace(ARGUMENT_SIGNER + i , argStr);
			}
		}
		return str;
	}
	
}
