package com.collabnet.ccf.sw;

public class SWPMetaData {

	public enum SWPType {
		PBI, TASK, SPRINT, TEAM, RELEASE /* ,PROGRAM_RELEASE */, PRODUCT, EPIC, /* PROGRAM, */ /* PROGRAM_EPIC, */ THEME, /* PROGRAM_THEME, */ IMPEDIMENT, USER, UNKNOWN
	}

	public final static String PBI = "PBI";
	public final static String TASK = "Task";
	public final static String SPRINT = "Sprint";
	public final static String TEAM = "Team";
	public final static String RELEASE = "Release";
	//public final static String PROGRAM_RELEASE = "ProgramRelease";
	public final static String PRODUCT = "Product";
	public final static String EPIC = "Epic";
	//public final static String PROGRAM = "Program";
	//public final static String PROGRAM_EPIC = "ProgramEpic";
	public final static String THEME = "Theme";
	//public final static String PROGRAM_THEME = "ProgramTheme";
	public final static String IMPEDIMENT = "Impediment";
	public final static String USER = "User";
	public final static String UNKNOWN = "UNKNOWN";

	/**
	 * Character used to separate the components of a repository id
	 */
	public final static String REPOSITORY_ID_SEPARATOR = "-";

	/**
	 * Parses an SWP repository id and returns the corresponding SWP type
	 * 
	 * @param repositoryId
	 * @return SWP entity type
	 */
	public static SWPType retrieveSWPTypeFromRepositoryId(String repositoryId) {
		if (null == repositoryId)
			return SWPType.UNKNOWN;
		if (repositoryId.endsWith(PBI))
			return SWPType.PBI;
		if (repositoryId.endsWith(TASK))
			return SWPType.TASK;
		if (repositoryId.endsWith(SPRINT))
			return SWPType.SPRINT;
		if (repositoryId.endsWith(TEAM))
			return SWPType.TEAM;
		if (repositoryId.endsWith(IMPEDIMENT))
			return SWPType.IMPEDIMENT;
		//if (repositoryId.endsWith(PROGRAM_RELEASE))
		//	return SWPType.PROGRAM_RELEASE;
		if (repositoryId.endsWith(RELEASE))
			return SWPType.RELEASE;
		if (repositoryId.endsWith(USER))
			return SWPType.USER;
		if (repositoryId.endsWith(PRODUCT))
			return SWPType.PRODUCT;
		//if (repositoryId.endsWith(PROGRAM))
		//	return SWPType.PROGRAM;
		//if (repositoryId.endsWith(PROGRAM_THEME))
		//	return SWPType.PROGRAM_THEME;
		if (repositoryId.endsWith(THEME))
			return SWPType.THEME;
		if (repositoryId.endsWith(EPIC))
			return SWPType.EPIC;
		//if (repositoryId.endsWith(PROGRAM_EPIC))
		//	return SWPType.PROGRAM_EPIC;
		return SWPType.UNKNOWN;
	}

	/**
	 * Parses the repository id and returns the part the encodes the product
	 * 
	 * @param repositoryId
	 * @return name of the product or null if product name could not be
	 *         extracted
	 */
	public final static String retrieveProductFromRepositoryId(String repositoryId) {
		int index = repositoryId.lastIndexOf(REPOSITORY_ID_SEPARATOR);
		if (-1 == index) {
			return null;
		} else {
			return repositoryId.substring(0, index);
		}
	}
	
	public final static Long retrieveProductIdFromRepositoryId(String repositoryId) {
		Long productId = null;
		if (null != repositoryId) {
			int index1 = repositoryId.lastIndexOf("(");
			if (-1 != index1) {
				int index2 = repositoryId.lastIndexOf(")");
				if (index2 > index1 + 1) {
					try {
						productId = Long.parseLong(repositoryId.substring(index1 + 1, index2));
					} catch (Exception e) {}
				}
			}
		}
		return productId;
	}
	
}
