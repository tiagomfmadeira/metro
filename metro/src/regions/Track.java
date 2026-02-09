package regions;

import entities.Salesman;
import static java.lang.System.err;
import static java.lang.System.exit;
import java.util.LinkedList;
import pt.ua.gboard.basic.Position;
import pt.ua.gboard.games.Labyrinth;

/**
 * General description: Definition of the Track region. Including logic of the
 * calculation of the move cycles that trains must follow
 *
 * @author Tiago Madeira 76321
 */
public class Track
{

    /**
     * Internal data
     */
    private LinkedList<String> trackMoves = new LinkedList<>();
    private LinkedList<Station> stations = new LinkedList<>();
    private Labyrinth metro;
    private char symbol;
    private char stationSymbol;
    private int N;

    /**
     * Constructor
     *
     * @param metro         the Labyrinth object
     * @param startPos      the Position corresponding to where the cycle of the
     *                      track should begin
     * @param endPos        the Position corresponding to where the cycle of the
     *                      track should end
     * @param stationSymbol the symbol associated with the stations of this
     *                      track
     * @param symbol        the symbol associated with the track
     * @param N             the cell size used in the visual representation
     */
    public Track(Labyrinth metro, Position startPos, Position endPos, char symbol, char stationSymbol, int N)
    {
        assert metro != null;
        this.metro = metro;
        this.symbol = symbol;
        this.stationSymbol = stationSymbol;
        this.N = N;
        Position[] trackSym = concatArrays(metro.symbolPositions(symbol), metro.symbolPositions(stationSymbol));
        LinkedList<String> trackPartA = determineTrack(trackSym, startPos);
        trackSym = concatArrays(metro.symbolPositions(symbol), metro.symbolPositions(stationSymbol));
        LinkedList<String> trackPartB = determineTrack(trackSym, endPos);
        LinkedList<String> Track = new LinkedList<>();
        Track.addAll(trackPartA);
        addUTurn(Track);
        Track.addAll(trackPartB);
        addUTurn(Track);
        this.trackMoves = Track;
        createStationList();
    }

    /**
     * Returns the list of all the stations in this track
     *
     * @return a LinkedList containing all the stations in this track
     */
    public LinkedList<Station> getStations()
    {
        return this.stations;
    }

    /**
     * Returns the symbol associated with the track
     *
     * @return a char representing the symbol associated with the track
     */
    public char getSymbol()
    {
        return this.symbol;
    }

    /**
     * Determines what station exists at a certain location of the track
     *
     * @param l line of location of station to be found times the cell size.
     * @param c column of location of station to be found times the cell size.
     *
     * @return the reference to the found station or null it does not exist
     */
    public Station getStation(int l, int c)
    {
        assert l / N < metro.numberOfLines;
        assert c / N < metro.numberOfColumns;

        for (Station station : stations)
        {
            if (((station.getLocation().line() * N) == l) && (station.getLocation().column() * N) == c)
            {
                return station;
            }
        }
        return null;
    }

    /**
     * Returns the number of moves in a full circulation of the track
     *
     * @return an integer representing the number of moves in a full circulation
     *         of the track
     */
    public int getMovesSize()
    {
        return trackMoves.size();
    }

    /**
     * Returns the list of moves for this track's cycle
     *
     * @return a LinkedList containing all the moves for the track: e.g. "Down",
     *         "Up", "Right" or "Left"
     */
    public LinkedList<String> getTrackMoves()
    {
        return this.trackMoves;
    }

    /**
     * Determine the list of Stations along this Track
     */
    private void createStationList()
    {
        Position[] stationPositions = metro.symbolPositions(stationSymbol);
        for (Position stationPosition : stationPositions)
        {
            Station tmpStation = new Station(stationPosition, this.symbol);
            stations.add(tmpStation);
            Salesman ticketSalesman = new Salesman("Salesman_at_" + tmpStation.getLocation(), tmpStation);
            ticketSalesman.start();
        }
    }

    /**
     * Determines the moves corresponding to a track based on an array of
     * Positions
     *
     * @return a LinkedList containing all the moves for the track: e.g. "Down",
     *         "Up", "Right" or "Left"
     */
    private LinkedList<String> determineTrack(Position[] trackCoords, Position start)
    {
        assert trackCoords != null;
        assert start != null;

        int nrCoords = trackCoords.length;
        LinkedList<String> track = new LinkedList<>();

        Position currCoord = null;
        Position nextCoord = null;
        Position nextNextCoord;

        // Start at point provided
        for (int i = 0; i < nrCoords; i++)
        {
            if (trackCoords[i].isEqual(start))
            {
                currCoord = trackCoords[i];
                trackCoords[i] = null;
            }
        }

        if (currCoord == null)
        {
            err.println("Starting point for determination of moves must be part of the track!");
            exit(1);
        }

        String tmpNext = "";
        for (int j = 0; j < nrCoords; j++)
        {
            if (trackCoords[j] == null)
            {
                continue;
            }
            tmpNext = determineDir(currCoord, trackCoords[j]);
            if (!tmpNext.equals(""))
            {
                nextCoord = trackCoords[j];
                trackCoords[j] = null;
                break;
            }
        }

        if (nextCoord == null)
        {
            err.println("There's a break in the track!");
            exit(1);
        }

        track.add(tmpNext);

        tmpNext = "";
        for (int i = 0; i < nrCoords - 2; i++)
        {
            nextNextCoord = null;
            String tmpNextNext = "";

            for (int j = 0; j < nrCoords; j++)
            {
                if (trackCoords[j] == null)
                {
                    continue;
                }

                tmpNextNext = determineDir(nextCoord, trackCoords[j]);
                if (!tmpNextNext.equals(""))
                {
                    nextNextCoord = trackCoords[j];
                    trackCoords[j] = null;
                    break;
                }
            }

            if (nextNextCoord == null)
            {
                err.println("There's a break in the track!");
                exit(1);
            }

            if (isOuterTurn(tmpNext, tmpNextNext))
            {
                track.add(tmpNext);
                track.add(tmpNext);
            }

            if (!isInnerTurn(tmpNext, tmpNextNext))
            {
                track.add(tmpNextNext);
                track.add(tmpNextNext);
            }

            if (i == trackCoords.length - 3)
            {
                track.add(tmpNextNext);
                track.add(tmpNextNext);
            }

            nextCoord = nextNextCoord;
            tmpNext = tmpNextNext;
        }

        return track;
    }

    /**
     * Determines a direction based on two consecutive Positions
     *
     * @return a string corresponding to the direction: "Down", "Up", "Right" or
     *         "Left"
     */
    private String determineDir(Position curr, Position next)
    {
        assert curr != null;
        assert next != null;

        String dir = "";
        if (next.column() == curr.column())
        {
            if (next.line() == curr.line() + 1)
            {
                dir = "Down";
            } else if (next.line() == curr.line() - 1)
            {
                dir = "Up";
            }
        } else if (next.line() == curr.line())
        {
            if (next.column() == curr.column() + 1)
            {
                dir = "Right";
            } else if (next.column() == curr.column() - 1)
            {
                dir = "Left";
            }
        }
        return dir;
    }

    /**
     * Adds a U turn to the end of a track, allowing for the train turn back
     */
    private void addUTurn(LinkedList<String> route)
    {
        assert route != null;

        String end = route.peekLast();
        switch (end)
        {
            case "Right":
                route.add("Down");
                route.add("Down");
                route.add("Left");
                break;
            case "Left":
                route.add("Up");
                route.add("Up");
                route.add("Right");
                break;
            case "Down":
                route.add("Left");
                route.add("Left");
                route.add("Up");
                break;
            case "Up":
                route.add("Right");
                route.add("Right");
                route.add("Down");
                break;
        }
    }

    /**
     * Checks whether a pair of directions corresponds to an outer turn
     *
     * @return <code>true</code> if it corresponds to an outer turn;
     *         <code>false</code> otherwise
     */
    private boolean isOuterTurn(String currDir, String nextDir)
    {
        return currDir.equals("Right") && nextDir.equals("Down")
                || currDir.equals("Down") && nextDir.equals("Left")
                || currDir.equals("Left") && nextDir.equals("Up")
                || currDir.equals("Up") && nextDir.equals("Right");
    }

    /**
     * Checks whether a pair of directions corresponds to an inner turn
     *
     * @return <code>true</code> if it corresponds to an inner turn;
     *         <code>false</code> otherwise
     */
    private boolean isInnerTurn(String currDir, String nextDir)
    {
        return currDir.equals("Down") && nextDir.equals("Right")
                || currDir.equals("Left") && nextDir.equals("Down")
                || currDir.equals("Up") && nextDir.equals("Left")
                || currDir.equals("Right") && nextDir.equals("Up");
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
