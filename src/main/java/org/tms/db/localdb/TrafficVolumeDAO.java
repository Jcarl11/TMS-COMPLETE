package org.tms.db.localdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.entities.VolumeEntity;
import org.tms.model.Period;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TrafficVolumeDAO extends BaseDao {
	public TrafficVolumeDAO() throws ConnectException {
		super();
	}

	public TrafficVolumeDAO(Connection conn) {
		super(conn);
	}
	final Logger log = LoggerFactory.getLogger(TrafficVolumeDAO.class);
	
	public ArrayList<VolumeEntity> getVolumePerHour(String period, String facility) {
		ArrayList<VolumeEntity> responseList = new ArrayList<VolumeEntity>();
		try {
			
			log.info("retrieving db data");
			String command = "";
			String last7DaysFilter = " where facility ='" + facility + "' and timestamp between (SELECT DATETIME('now', '-7 day')) and (SELECT date('now', '1 day')) ";
			String last30DaysFilter = " where facility ='" + facility + "' and timestamp between (SELECT DATETIME('now', '-30 day')) and (SELECT date('now', '1 day')) ";
			log.debug(Period.fromValue(period).toString());
			switch (Period.fromValue(period)) {
			case LAST_7_DAYS:
				command = "select SUM(COUNT) VOLUME, strftime('%m-%d', timestamp) DATE from rawdata" + last7DaysFilter + "group by strftime('%m-%d', timestamp) order by timestamp asc;";
				break;
			case LAST_30_DAYS:
				command = "select SUM(COUNT) VOLUME, strftime('%m-%d', timestamp) DATE from rawdata" + last30DaysFilter + "group by strftime('%m-%d', timestamp) order by timestamp asc;";
				break;
			case ALL:
				command = "select SUM(COUNT) VOLUME, strftime('%m-%d', timestamp) DATE from rawdata where facility ='" + facility + "' group by strftime('%m-%d', timestamp) order by timestamp asc;";
			default:
				break;
			}
			
			log.debug("command: " + command);
			prepareStatement(command);
			ResultSet rs = executeQuery();
			
			while (rs.next()) {
				Double average = rs.getDouble("VOLUME");
				String date = rs.getString("DATE");
				
				responseList.add(new VolumeEntity(average, date));
			}
			
			log.info("finished retrieving data");

		} catch (SQLException e) {
			log.error("SQLException : " + e.getMessage());
		} finally {
			try {
				log.debug("Closing resources...");
				closeResources();
				closeConnection();
			} catch (Exception e) {
			}
		}
		
		return responseList;

	}

	public ArrayList<String> getAreaList() {
		ArrayList<String> responseList = new ArrayList<String>();
		try {

			log.info("retrieving db data");
			String command = "SELECT DISTINCT facility FROM RAWDATA";


			log.debug("command: " + command);
			prepareStatement(command);
			ResultSet rs = executeQuery();

			while (rs.next()) {
				String area = rs.getString("FACILITY");

				responseList.add(area);
			}

			log.info("finished retrieving data");

		} catch (SQLException e) {
			log.error("SQLException : " + e.getMessage());
		} finally {
			try {
				log.debug("Closing resources...");
				closeResources();
				closeConnection();
			} catch (Exception e) {
			}
		}

		return responseList;
	}

}
