import com.google.gson.Gson;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
/* Authors: Martin Flanagan, Leith Coupland, Tawanda Muhwati

* Class implements DAO interface
*
* The class performs queries for getting access points and savind access points in the database
*
* */

public class AccessPointDao implements Dao<AccessPoint> {

    Connection connection;

    public AccessPointDao() {
        connection = Database.getConnection();
    }

    public List<AccessPoint> get(String [] query) {
        return this.get(query[0], query[1]);
    }

    /*
    * Get list of access points according to query passed
    * 1. Perform sql SELECT query
    * 2. Map response to access point object
    * 3. return list of objects to requester.
    *
    * Column - the column in the table to look up the data
    * Value - The data that's being looked up in the database column.
    * return - list of access point(s)
    * */
    private List<AccessPoint> get(String column, String value) {

        String query = "";
        List<AccessPoint> apnList = new ArrayList<>();

        if(column.isEmpty() && value.isEmpty()){ //For test purposes
            query = "SELECT * FROM access_point";
        }else{

            switch (column){
                case "bssid":

                    try {
                        PGobject bssid = new PGobject();
                        bssid.setValue(value);
                        bssid.setType("macaddr");

                        query = "SELECT * FROM access_point WHERE bssid = '" + bssid + "'";

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case "location":

                    String [] coordinates = value.split(";",2);

                    if(2 == coordinates.length){
                        query = "SELECT * FROM access_point ORDER BY location <-> " +
                                "POINT("+Double.parseDouble(coordinates[0])+","+Double.parseDouble(coordinates[1])+")::point LIMIT 1";
                    }

                    break;
                case "ssid":
                    query = "SELECT * FROM access_point WHERE ssid = '" + value + "'";
                    break;
                case "name":
                    query = "SELECT * FROM access_point WHERE name = '" + value + "'";
                    break;

                    default:
                        query = "SELECT * FROM access_point";
                        break;
            }
        }

        try(Statement statement = connection.createStatement()) {

            try(ResultSet resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {

                    AccessPoint apn = new AccessPoint();
                    apn.setBssid(resultSet.getString("bssid")!=null?resultSet.getString("bssid"):"");
                    apn.setLinkSpeed(resultSet.getInt("linkSpeed"));
                    apn.setSsid(resultSet.getString("ssid")!=null?resultSet.getString("ssid"):"");
                    apn.setTimestamp(resultSet.getLong("timestamp"));

                    if (resultSet.getString("location") != null) {
                        apn.setLocation(new PGpoint(resultSet.getString("location")));
                    }

                    apnList.add(apn);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        System.out.println("DB Response: "+new Gson().toJson(apnList));

        return apnList;
    }

    @Override
    public List<AccessPoint> getAll() {
        return get("","");
    }

    /*
    * Method responsible for saving data to database
    * 1. input - the access point to save
    * 2. perform INSERT query
    * 3. response -true if successfully saved, false otherwise
    * */
    @Override
    public boolean save(AccessPoint accessPoint) {

        try {
            PGobject pGobject = new PGobject();
            pGobject.setValue(accessPoint.getBssid());
            pGobject.setType("macaddr");

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO access_point VALUES (?,?,?,?,?)");
            preparedStatement.setObject(1, pGobject);
            preparedStatement.setString(2, accessPoint.getSsid());
            preparedStatement.setObject(3, accessPoint.getLocation());
            preparedStatement.setInt(4, accessPoint.getLinkSpeed());
            preparedStatement.setLong(5, System.currentTimeMillis());

            int response = preparedStatement.executeUpdate();
            preparedStatement.close();

            return true;

        }catch (SQLException e){
            return false;
        }
    }

    @Override
    public void update(AccessPoint t, String[] params) {

    }

    @Override
    public void delete(AccessPoint t) {

    }
}
