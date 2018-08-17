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

        String query;
        List<AccessPoint> apnList = new ArrayList<>();

        if(column.isEmpty() && value.isEmpty()){ //For test purposes
            query = "SELECT * FROM access_point";
        }else{
            query = "SELECT * FROM access_point WHERE '" + column + "'='" + value + "'";
        }

        System.out.println(query);

        try(Statement statement = connection.createStatement()) {

            try(ResultSet resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {

                    AccessPoint apn = new AccessPoint();
                    apn.setBssid(resultSet.getString("bssid"));
                    apn.setLinkSpeed(resultSet.getInt("link_speed"));
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
