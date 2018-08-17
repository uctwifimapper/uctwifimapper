import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccessPointDao implements Dao<AccessPoint> {

    Connection connection;

    public AccessPointDao() {
        connection = Database.getConnection();
    }

    @Override
    public Optional<AccessPoint> get(long id) {
        return null;
    }

    @Override
    public List<AccessPoint> get(String column, String value) {

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

                        query = "SELECT * FROM access_point WHERE bssid'" + bssid + "'";

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case "location":

                    String [] coordinates = value.split(";",2);

                    if(2 == coordinates.length){ //TODO improve query
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

                    default:break;
            }
        }
        System.out.println(query);
        try(Statement statement = connection.createStatement()) {

            try(ResultSet resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {

                    AccessPoint apn = new AccessPoint();
                    apn.setBssid(resultSet.getString("bssid"));
                    apn.setLinkSpeed(resultSet.getInt("linkSpeed"));
                    apn.setSsid(resultSet.getString("ssid"));

                    if (resultSet.getString("location") != null) {
                        apn.setLocation(new PGpoint(resultSet.getString("location")));
                    }

                    apnList.add(apn);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        return apnList;
    }

    @Override
    public List<AccessPoint> getAll() {
        return get("","");
    }

    @Override
    public boolean save(AccessPoint accessPoint) {

        try {
            PGobject pGobject = new PGobject();
            pGobject.setValue(accessPoint.getBssid());
            pGobject.setType("macaddr");

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO access_point VALUES (?,?,?,?)");
            preparedStatement.setObject(1, pGobject);
            preparedStatement.setString(2, accessPoint.getSsid());
            preparedStatement.setObject(3, accessPoint.getLocation());
            preparedStatement.setInt(4, accessPoint.getLinkSpeed());

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
