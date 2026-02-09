package regions;

import java.util.LinkedList;
import pt.ua.gboard.basic.Position;
import pt.ua.gboard.games.Labyrinth;

/**
 * General description: Definition of the MetroMap region. The information
 * centre for Pawns to ask about the map.
 *
 * @author Tiago Madeira 76321
 */
public class MetroMap
{

    /**
     * Internal data
     */
    private Labyrinth metro;
    private Track[] tracks;

    /**
     * Constructor
     *
     * @param metro  Labyrinth object
     * @param tracks array of the tracks that exist in the world
     */
    public MetroMap(Labyrinth metro, Track[] tracks)
    {
        assert metro != null;
        assert tracks != null;

        this.metro = metro;
        this.tracks = tracks;
    }

    /**
     * Calculates the closest station to a certain point in the map
     *
     * @param pos position of a point within the map
     *
     * @return a Station closest to a certain map position
     */
    public Station getNearestStation(Position pos)
    {
        assert pos != null;
        assert pos.column() < metro.numberOfColumns;
        assert pos.line() < metro.numberOfLines;

        Station targetStation = null;
        double dist = Double.MAX_VALUE;

        for (Track track : tracks)
        {
            LinkedList<Station> stationList = track.getStations();

            for (Station station : stationList)
            {
                double tmpDist = distance(station.getLocation(), pos);
                if (tmpDist < dist)
                {
                    dist = tmpDist;
                    targetStation = station;
                }
            }
        }

        return targetStation;
    }

    /**
     * Determines a train ride for a certain start and destination. Calculates
     * the closest station to the Destination and the closest station of that
     * same track to the Start.
     *
     * @param startPos  the Position within the map for the start of the travel
     * @param targetPos the Position within the map for the end of the travel
     *
     * @return a list of Stations containing the Station to get on the train and
     *         the Station to get off the train
     */
    public Station[] getPathStations(Position startPos, Position targetPos)
    {
        assert startPos != null;
        assert targetPos != null;
        assert startPos.column() < metro.numberOfColumns;
        assert startPos.line() < metro.numberOfLines;
        assert targetPos.column() < metro.numberOfColumns;
        assert targetPos.line() < metro.numberOfLines;

        Station[] stations = new Station[2];

        // get the station near to the target.
        Station getOffStation = null;
        Station getOnStation = null;
        Track tmpTrack = null;
        double dist = Double.MAX_VALUE;
        double distToFirst = Double.MAX_VALUE;

        for (Track track : tracks)
        {
            LinkedList<Station> stationList = track.getStations();

            for (Station station : stationList)
            {
                double tmpDist = distance(station.getLocation(), targetPos);
                if (tmpDist < dist)
                {
                    dist = tmpDist;
                    getOffStation = station;
                    tmpTrack = track;
                }
            }
        }

        // get the fist station to get on
        LinkedList<Station> stationList = tmpTrack.getStations();

        for (Station station : stationList)
        {
            double tmpDist = distance(station.getLocation(), startPos);
            if (tmpDist < distToFirst)
            {
                distToFirst = tmpDist;
                getOnStation = station;
            }
        }

        stations[0] = getOnStation;
        stations[1] = getOffStation;

        return stations;
    }

    /**
     * Returns the possible destiny positions for the Pawns.
     *
     * @return an array of Positions for the pawns to walk toward.
     */
    public Position[] getDestinyPositions()
    {
        return metro.symbolPositions(' ');
    }

    /**
     * Returns the possible spawning positions for the pawns.
     *
     * @return an array of Positions for the pawns to spawning in.
     */
    public Position[] getSpawningPositions()
    {
        return concatArrays(metro.symbolPositions(' '), metro.symbolPositions('X'));
    }

    /**
     * Returns the limit of the map in terms of lines
     *
     * @return an integer representing total number of lines of the map
     */
    public int maxL()
    {
        return metro.numberOfLines - 1;
    }

    /**
     * Returns the limit of the map in terms of columns
     *
     * @return an integer representing total number of columns of the map
     */
    public int maxC()
    {
        return metro.numberOfColumns - 1;
    }

    /**
     * Calculates the euclidian distance between two Positions
     */
    private double distance(Position pos1, Position pos2)
    {
        assert pos1 != null;
        assert pos2 != null;

        int x = Math.abs(pos1.column() - pos2.column());
        int y = Math.abs(pos1.line() - pos2.line());

        return Math.sqrt(x * x + y * y);
    }

    /**
     * Concatenates two arrays of Positions
     *
     * @return an array of Positions containing the concatenated arrays
     */
    private Position[] concatArrays(Position[] first, Position[] second)
    {
        int size = first.length + second.length;
        Position[] a = new Position[size];
        int pt = 0;

        for (Position tmpPos : first)
        {
            a[pt] = tmpPos;
            pt++;
        }

        for (Position tmpPos : second)
        {
            a[pt] = tmpPos;
            pt++;
        }

        return a;
    }
}
