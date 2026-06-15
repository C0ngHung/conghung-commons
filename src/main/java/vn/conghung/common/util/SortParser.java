package vn.conghung.common.util;

import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public final class SortParser {

    private static final String SORT_DELIMITER = ":";
    private static final Sort.Order DEFAULT_ORDER = Sort.Order.asc("id");

    private SortParser() {
    }

    public static Sort.Order parse(String sortExpression) {
        if (!StringUtils.hasLength(sortExpression) || !sortExpression.contains(SORT_DELIMITER)) {
            return DEFAULT_ORDER;
        }

        String[] parts = sortExpression.split(SORT_DELIMITER, 2);
        String field = parts[0].trim();
        String direction = parts[1].trim();

        if (!StringUtils.hasLength(field)) {
            return DEFAULT_ORDER;
        }

        return isDesc(direction)
                ? Sort.Order.desc(field)
                : Sort.Order.asc(field);
    }

    private static boolean isDesc(String value) {
        return value.length() == 4
                && (value.charAt(0) == 'd' || value.charAt(0) == 'D')
                && (value.charAt(1) == 'e' || value.charAt(1) == 'E')
                && (value.charAt(2) == 's' || value.charAt(2) == 'S')
                && (value.charAt(3) == 'c' || value.charAt(3) == 'C');
    }
}
