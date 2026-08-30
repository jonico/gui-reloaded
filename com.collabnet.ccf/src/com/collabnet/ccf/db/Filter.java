package com.collabnet.ccf.db;

public class Filter {
	private String columnName;
	private int filterType;
	private String value;
	private boolean stringValue = true;

	public final static int FILTER_TYPE_EQUAL = 0;
	public final static int FILTER_TYPE_NOT_EQUAL = 1;
	public final static int FILTER_TYPE_LESS_THAN = 2;
	public final static int FILTER_TYPE_LESS_THAN_OR_EQUAL_TO = 3;
	public final static int FILTER_TYPE_GREATER_THAN = 4;
	public final static int FILTER_TYPE_GREATER_THAN_OR_EQUAL_TO = 5;
	public final static int FILTER_TYPE_LIKE = 6;
	
	public final static String HOSPITAL_FILTERS_SET = "hospitalFilters.set"; //$NON-NLS-1$
	public final static String HOSPITAL_FILTERS_ACTIVE = "hospitalFilters.active"; //$NON-NLS-1$
	public final static String HOSPITAL_FILTER_VALUE = "hospitalFilter.value."; //$NON-NLS-1$
	public final static String HOSPITAL_FILTER_TYPE = "hospitalFilter.type."; //$NON-NLS-1$
	
	public final static String IDENTITY_MAPPING_FILTERS_SET = "identityMappingFilters.set"; //$NON-NLS-1$
	public final static String IDENTITY_MAPPING_FILTERS_ACTIVE = "identityMappingFilters.active"; //$NON-NLS-1$
	public final static String IDENTITY_MAPPING_FILTER_VALUE = "identityMappingFilter.value."; //$NON-NLS-1$
	public final static String IDENTITY_MAPPING_FILTER_TYPE = "identityMappingFilter.type."; //$NON-NLS-1$
	
	public Filter(String columnName, String value, boolean stringValue, int filterType) {
		this.columnName = columnName;
		this.filterType = filterType;
		this.value = value;
		this.stringValue = stringValue;
	}
	
	public Filter(String columnName, String value, boolean stringValue) {
		this(columnName, value, stringValue, FILTER_TYPE_EQUAL);
	}
	
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getFilterType() {
		return filterType;
	}

	public void setFilterType(int filterType) {
		this.filterType = filterType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString() {
		StringBuffer expression = new StringBuffer(columnName);
		switch (filterType) {
		case FILTER_TYPE_EQUAL:
			expression.append(" = "); //$NON-NLS-1$
			break;
		case FILTER_TYPE_NOT_EQUAL:
			expression.append(" != "); //$NON-NLS-1$
			break;
		case FILTER_TYPE_LESS_THAN:
			expression.append(" < "); //$NON-NLS-1$
			break;
		case FILTER_TYPE_LESS_THAN_OR_EQUAL_TO:
			expression.append(" <= "); //$NON-NLS-1$
			break;
		case FILTER_TYPE_GREATER_THAN:
			expression.append(" > "); //$NON-NLS-1$
			break;
		case FILTER_TYPE_GREATER_THAN_OR_EQUAL_TO:
			expression.append(" >= "); //$NON-NLS-1$
			break;		
		case FILTER_TYPE_LIKE:
			expression.append(" LIKE "); //$NON-NLS-1$
			break;						
		default:
			break;
		}
		if (stringValue) expression.append("'"); //$NON-NLS-1$
		if (FILTER_TYPE_LIKE == filterType && !(value.startsWith("%"))) //$NON-NLS-1$
			expression.append("%"); //$NON-NLS-1$
		expression.append(value);
		if (FILTER_TYPE_LIKE == filterType && !(value.endsWith("%"))) //$NON-NLS-1$
			expression.append("%");		 //$NON-NLS-1$
		if (stringValue) expression.append("'"); //$NON-NLS-1$
		return expression.toString();
	}
	
	public static String getQuery(String sql, Filter[] filters) {
		StringBuffer query = new StringBuffer(sql);
		if (null != filters && 0 < filters.length && null != filters[0]) {
			query.append(" WHERE "); //$NON-NLS-1$
			for (int i = 0; i < filters.length; i++) {
				if (0 < i) query.append(" AND "); //$NON-NLS-1$
				query.append(filters[i].toString());
			}
		}
		return query.toString();
	}
	
	public static String getQuery(String sql, Filter[][] filters) {
		if (null == filters || 0 == filters.length) return sql;
		if (1 == filters.length) return getQuery(sql, filters[0]);
		StringBuffer query = new StringBuffer(sql);
		query.append(" WHERE "); //$NON-NLS-1$
		for (int i = 0; i < filters.length; i++) {
			if (0 < i) query.append(" OR "); //$NON-NLS-1$
			query.append("("); //$NON-NLS-1$
			Filter[] orGroup = filters[i];
			for (int j = 0; j < orGroup.length; j++) {
				if (0 < j) query.append(" AND "); //$NON-NLS-1$
				query.append(orGroup[j].toString());
			}
			query.append(")"); //$NON-NLS-1$
		}
		return query.toString();
	}
		
}
