package net.example.toilets.store;

import net.example.toilets.model.Location;
import net.example.toilets.model.Toilet;
import net.example.toilets.model.ToiletBuilder;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.example.toilets.util.Proximity.RADIUS_OF_EARTH;

/**
 * Created by alanstewart on 23/02/15.
 */
public final class JdbcToiletStoreImpl extends AbstractToiletStoreImpl {

    private static final String DB_CONNECTION = "jdbc:h2:tcp://localhost/~/toiletdb";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private final List<Toilet> toilets = new ArrayList<>();

    @Override
    public List<Toilet> search(ToiletQuery query) {
        String sql = new StringBuilder()
                .append("select *, (")
                .append(RADIUS_OF_EARTH)
                .append(" * acos(cos(radians(?)) * cos(radians(latitude))")
                .append(" * cos(radians(longitude ) - radians(?)) + sin(radians(?))")
                .append(" * sin(radians(latitude)))) as distance")
                .append(" from toilets")
                .append(" group by name, address1, town, state, postcode, address_note, icon_url, latitude, longitude")
                .append(" having distance <= 5")
                .append(" order by distance")
                .append(" limit ?")
                .toString();

        List<Toilet> toilets = new ArrayList<>();
        Location location = query.getLocation();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, location.getLatitude());
            ps.setDouble(2, location.getLongitude());
            ps.setDouble(3, location.getLatitude());
            ps.setInt(4, query.getLimit());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Toilet toilet = new ToiletBuilder()
                        .setName(rs.getString(1))
                        .setAddress1(rs.getString(2))
                        .setTown(rs.getString(3))
                        .setState(rs.getString(4))
                        .setPostcode(rs.getString(5))
                        .setAddressNote(rs.getString(6))
                        .setIconUrl(rs.getString(7))
                        .setLocation(new Location(rs.getDouble(8), rs.getDouble(9)))
                        .build();
                toilets.add((toilet));
            }
            return toilets;
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.<Toilet>emptyList();
        }
    }

    @Override
    public void initialise(InputStream toiletXml) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("drop table if exists toilets");
            stmt.executeUpdate("create table toilets (name varchar(255), address1 varchar(255), town varchar(255), state varchar(100), postcode varchar(4), address_note varchar(255), icon_url varchar(255), latitude double, longitude double)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            readToiletXml(toiletXml);
            if (!toilets.isEmpty()) {
                insertToilets();
                toilets.clear();
            }
        } catch (XMLStreamException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void add(Toilet toilet) {
        if (toilets.size() % 1000 == 0) {
            try {
                insertToilets();
                toilets.clear();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        toilets.add(toilet);
    }

    private void insertToilets() throws SQLException {
        String sql = "insert into toilets (name, address1, town, state, postcode, address_note, icon_url, latitude, longitude) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Toilet toilet : toilets) {
                ps.setString(1, toilet.getName());
                ps.setString(2, toilet.getAddress1());
                ps.setString(3, toilet.getTown());
                ps.setString(4, toilet.getState());
                ps.setString(5, toilet.getPostcode());
                ps.setString(6, toilet.getAddressNote());
                ps.setString(7, toilet.getIconUrl());
                ps.setDouble(8, toilet.getLocation().getLatitude());
                ps.setDouble(9, toilet.getLocation().getLongitude());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
    }
}
