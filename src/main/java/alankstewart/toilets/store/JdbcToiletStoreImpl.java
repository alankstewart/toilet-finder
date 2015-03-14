package alankstewart.toilets.store;

import alankstewart.toilets.model.Location;
import alankstewart.toilets.model.Toilet;
import alankstewart.toilets.util.Proximity;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alanstewart on 23/02/15.
 */
public final class JdbcToiletStoreImpl extends AbstractToiletStoreImpl {

    private static final String INSERT_SQL = "insert into toilets (name, address1, town, state, postcode, " +
            "address_note, icon_url, latitude, longitude) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SEARCH_SQL = "select * " +
            "from (select *, (" + Proximity.RADIUS_OF_EARTH + " * acos(cos(radians(@lat)) * cos(radians(latitude)) * " +
            "  cos(radians(longitude) - radians(@lng)) + sin(radians(@lat)) * sin(radians(latitude)))) as distance " +
            "from toilets " +
            "where latitude between @lat - (@radius / 111.1) and @lat + (@radius / 111.1) " +
            "and longitude between @lng - (@radius / (111.1 * cos(radians(@lat)))) and " +
            "  @lng + (@radius / (111.1 * cos(radians(@lat))))) " +
            "group by name, address1, town, state, postcode, address_note, icon_url, latitude, longitude " +
            "having distance <= 5 " +
            "order by distance " +
            "limit ?";

    private final List<Toilet> toilets = new ArrayList<>();

    public JdbcToiletStoreImpl() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.addBatch("drop table if exists toilets");
            stmt.addBatch("create table toilets (name varchar(255), address1 varchar(255), town varchar(255), " +
                    "state varchar(100), postcode varchar(4), address_note varchar(255), icon_url varchar(255), " +
                    "latitude double, longitude double)");
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Toilet> search(ToiletQuery query) {
        List<Toilet> toilets = new ArrayList<>();
        Location location = query.getLocation();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             PreparedStatement ps = conn.prepareStatement(SEARCH_SQL)) {
            stmt.addBatch("set @lat = " + location.getLatitude());
            stmt.addBatch("set @lng = " + location.getLongitude());
            stmt.addBatch("set @radius = 10");
            stmt.executeBatch();
            ps.setInt(1, query.getLimit());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                toilets.add(createToilet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return toilets;
    }

    @Override
    public void initialise(InputStream toiletXml) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("delete from toilets");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        readToiletXml(toiletXml);
        if (!toilets.isEmpty()) {
            insertToilets();
        }
    }

    @Override
    protected void add(Toilet toilet) {
        if (toilets.size() == 1000) {
            insertToilets();
        }
        toilets.add(toilet);
    }

    private void insertToilets() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
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
            if (Arrays.stream(ps.executeBatch()).sum() != toilets.size()) {
                throw new IllegalStateException("Failed to insert toilets into database");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        toilets.clear();
    }

    private Toilet createToilet(ResultSet rs) throws SQLException {
        return new Toilet.Builder()
                .setName(rs.getString(1))
                .setAddress1(rs.getString(2))
                .setTown(rs.getString(3))
                .setState(rs.getString(4))
                .setPostcode(rs.getString(5))
                .setAddressNote(rs.getString(6))
                .setIconUrl(rs.getString(7))
                .setLocation(new Location(rs.getDouble(8), rs.getDouble(9)))
                .build();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:tcp://localhost/~/toiletdb", "sa", "");
    }
}
