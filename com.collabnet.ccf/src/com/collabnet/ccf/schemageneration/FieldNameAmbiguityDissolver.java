/*
 * Copyright 2009 CollabNet, Inc. ("CollabNet")
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package com.collabnet.ccf.schemageneration;

import java.util.HashMap;

/**
 * This component is used to enrich the generic artifact XML schema with
 * field-meta data. Furthermore it will rename fields which original name cannot
 * be used as the name of an XML element. Last but not least it remaps field
 * names if custom fields and mandatory fields have the same name The output of
 * this processor is typically used to automatically generate repository
 * specific schemas used for graphical mapping purposes.
 * 
 * @author jnicolai
 * 
 */
public class FieldNameAmbiguityDissolver {
	
	private static final char[] specialChars = { ' ', '~','!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '+', '|', '}', '{', '"', ':', '?', '>', '<', '`', '-', '=', '\\', ']', '[', '\'', ';', '/', '.',','};
	private static final String[] replaceChars={ "","_T_","_E_","_R_","_ASH_","_D_","_P_","_C_","_A_","_S_","_OP_","_CL_","_PL_","_OR_","_CB_","_OB_","_DQ_","_CLN_","_Q_","_GT_","_LT_","_QO_","-","_EQ_","_BS_","_BC_","_BO_","_AP_","_SCL_","_FS_","_DOT_","_COM_"};
	
	/**
	 * This method has been inspired by the netbeans project
	 * @param name
	 * @return
	 */
	private static String getReplacement(char c)
	{
		for (int i=0;i<specialChars.length;i++) {
			if(specialChars[i]==c) {
				return replaceChars[i];
			}
		}
		return null;
	}

	/**
	 * This method has been inspired by the netbeans project
	 * @param name
	 * @return
	 */
	private static String makeValidElementName(String name) {
		StringBuffer elementName = new StringBuffer();
		if (null == name)
			name = "";

		name = name.trim();
		int size = name.length();
		char ncChars[] = name.toCharArray();

		int i = 0;

		for (i = 0; i < size; i++) {
			char ch = ncChars[i];
			if (((0 == i)
					&& !(Character.isJavaIdentifierStart(ch) && ('$' != ch)) && !Character
					.isDigit(ch))
					|| ((0 < i) && !(Character.isJavaIdentifierPart(ch) && ('$' != ch)))) {
				String replace = getReplacement(ch);
				if (null != replace) {
					elementName.insert(elementName.length(), replace);

				} else {
					elementName.insert(elementName.length(), "_Z_");
				}
			} else {
				elementName.append(ncChars[i]);
			}
		}
		if ((0 < i) && Character.isDigit(elementName.charAt(0))) {
			elementName.insert(0, "X_");
		}
		if ((0 < i) && '_' == elementName.charAt(0)) {
			elementName.insert(0, "X");
		}
		return elementName.toString();
	}

	/**
	 * attribute contains the mapping from the original field name to a field
	 * name that can reused as an XML element name
	 */
	private HashMap<String, Boolean> fieldMapping = new HashMap<String, Boolean>();
	
	/**
	 * attribute contains the reverse mapping from the XML element friendly name
	 * back to the original field name
	 */
	private HashMap<String, String> reverseFieldMapping = new HashMap<String, String>();

	/**
	 * Generate XML-element-friendly name out of original field name
	 * 
	 * @param fieldName
	 *            original field name
	 * @param isFlexField
	 *            true if it is a custom field
	 * @return new XML-element-friendly name
	 */
	public String generateNewFieldName(String fieldName, boolean isFlexField) {
		String encodedName = makeValidElementName(fieldName);
		String firstEncodedName = encodedName;
		int number = 1;
		while (true) {
			// no key may show up twice
			if (fieldMapping.containsKey(encodedName)) {
					++number;
					encodedName = firstEncodedName + "_"
							+ Integer.toString(number);
			} else {
				// create entry
				fieldMapping.put(encodedName, new Boolean(isFlexField));
				reverseFieldMapping.put(encodedName, fieldName);
				return encodedName;
			}
		}
	}

	/**
	 * reset the field (reverse) mapping data structures
	 */
	public void resetFieldNameMapping() {
		fieldMapping.clear();
		reverseFieldMapping.clear();
	}

}

	

	

