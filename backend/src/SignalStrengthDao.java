import com.google.gson.Gson;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SignalStrengthDao implements Dao<SignalStrength> {

    private Connection connection;
    private final String table = "signal_strength";

    public SignalStrengthDao() {
        connection = Database.getConnection();
    }

    public List<SignalStrength> get(String[] query) {
        return this.get(query[0], query[1]);
    }

    private List<SignalStrength> get(String column, String value) {

        String query = "";
        List<SignalStrength> signalStrengthList = new ArrayList<>();

        if(column.isEmpty() && value.isEmpty()){ //For test purposes
            query = "SELECT * FROM access_point";
        }else {

            String [] values = new String [0];
            if(value.contains("&")){
                values = value.split("&");
            }

            switch (column) {
                case "bssid":

                    try {
                        PGobject bssid = new PGobject();
                        bssid.setValue(value);
                        bssid.setType("macaddr");

                        query = "SELECT * FROM " + table + " WHERE bssid = '" + bssid + "'";

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case "signalStrength":
                    query = "SELECT * FROM " + table + " WHERE signalStrength = '" + value + "'";
                    break;
                case "location":

                    String[] coordinates = value.split(";", 2);

                    query = "SELECT * FROM " + table + " ORDER BY location <-> " +
                                "POINT(" + Double.parseDouble(coordinates[0]) + "," + Double.parseDouble(coordinates[1]) + ")::point LIMIT 1";

                    break;
                case "timestamp":

                    if (values.length > 0) {
                        if(values[0].equals("d")){
                            long date = System.currentTimeMillis() - (Long.valueOf(values[0])*24*60*60*1000);
                            query = "SELECT * FROM " + table + " WHERE timestamp > '" + date + "'";
                        }else if(values[0].equals("h")){
                            long date = System.currentTimeMillis() - (Long.valueOf(values[0])*60*60*1000);
                            query = "SELECT * FROM " + table + " WHERE timestamp > '" + date + "'";
                        }else if(values[0].equals("m")){
                            long date = System.currentTimeMillis() - (Long.valueOf(values[0])*60*1000);
                            query = "SELECT * FROM " + table + " WHERE timestamp > '" + date + "'";
                        }
                    }else{
                        query = "SELECT * FROM " + table + " WHERE timestamp < '" + System.currentTimeMillis() + "'";
                    }
                    break;
                default:
                    break;
            }
        }

        try(Statement statement = connection.createStatement()) {

            try(ResultSet resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {

                    SignalStrength signalStrength = new SignalStrength();
                    signalStrength.setBssid(resultSet.getString("bssid")!=null ? resultSet.getString("bssid") : "");
                    signalStrength.setSignalStrength(resultSet.getInt("signalStrength"));
                    signalStrength.setTimestamp(resultSet.getLong("timestamp"));

                    if (resultSet.getString("location") != null) {
                        signalStrength.setLocation(new PGpoint(resultSet.getString("location")));
                    }

                    signalStrengthList.add(signalStrength);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        return signalStrengthList;
    }

    @Override
    public List<SignalStrength> getAll() {
        this.get("","");
        return null;
    }

    @Override
    public boolean save(SignalStrength signalStrength) {

        try {
            PGobject pGobject = new PGobject();
            pGobject.setValue(signalStrength.getBssid());
            pGobject.setType("macaddr");

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "+table+" VALUES (?,?,?,?)");
            preparedStatement.setObject(1, pGobject);
            preparedStatement.setInt(2, signalStrength.getSignalStrength());
            preparedStatement.setObject(3, signalStrength.getLocation());
            preparedStatement.setLong(4, System.currentTimeMillis());

            int response = preparedStatement.executeUpdate();
            preparedStatement.close();

            return true;

        }catch (SQLException e){
            return false;
        }
    }

    @Override
    public void update(SignalStrength signalStrength, String[] params) {

    }

    @Override
    public void delete(SignalStrength signalStrength) {

    }
}
