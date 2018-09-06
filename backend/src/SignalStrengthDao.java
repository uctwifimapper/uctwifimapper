import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SignalStrengthDao implements Dao<SignalStrength> {

    private Connection connection;
    private final String table = "signal_strength";

    public SignalStrengthDao() {
        connection = Database.getConnection();
    }

    public List<SignalStrength> get(String[] query) {
        return null;
    }

    private List<SignalStrength> get(String column, String value) {

        String query;
        List<SignalStrength> signalStrengthList = new ArrayList<>();

        if(column.isEmpty() && value.isEmpty()){ //For test purposes
            query = "SELECT * FROM access_point";
        }else {
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
                    //TODO
                    break;
                default:
                    break;
            }
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
            preparedStatement.setInt(4, signalStrength.getTimestamp());

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
