package uk.co.javahelp.fitnesse.slim;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class JdbcSlimRunner {

	private final JdbcTemplate template;
	
	private final String sql;

	public JdbcSlimRunner(final String driverClassname, final String url, final String username, final String password, final String sql)
	        throws ClassNotFoundException {
		Class.forName(driverClassname);
		this.template = new JdbcTemplate(new SingleConnectionDataSource(url, username, password, false));
		this.sql = sql;
	}
	
	public List<Object> query() {
		final List<Object> rows = new ArrayList<Object>();
		final List<Map<String, Object>> dbRows = this.template.queryForList(this.sql);
		for(Map<String, Object> dbRow : dbRows) {
    		final List<List<Object>> row = new ArrayList<List<Object>>();
			for(Map.Entry<String, Object> dbCell : dbRow.entrySet()) {
				row.add(asList(dbCell.getKey(), dbCell.getValue()));
			}
			rows.add(row);
		}
		return rows;
	}
}
