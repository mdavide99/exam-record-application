package it.polimi.tiw.purehtml.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * DAO used to handle interactions with the database regarding reports
 *
 */
public class ReportDAO {

    private Connection con;

    public ReportDAO(Connection con){this.con = con;}

    /**
     * 
     * Gets the current ReportId and increases it, granting each report to have an unique id 
     * 
     */
    public int newReportId() throws SQLException{
        int newId = 0;
        ResultSet result = null;
        String query = "SELECT * FROM report";
        PreparedStatement pstatement = null;
        pstatement = con.prepareStatement(query);
        result = pstatement.executeQuery();
        while (result.next()) {
            newId = result.getInt("idReport");
        }
        query = "UPDATE report SET idReport = idReport + 1";
        pstatement = con.prepareStatement(query);
        pstatement.executeUpdate();
        return newId;
    }
}
