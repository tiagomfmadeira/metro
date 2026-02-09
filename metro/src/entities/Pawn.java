package entities;

import static java.lang.System.err;
import java.util.concurrent.ThreadLocalRandom;
import pt.ua.gboard.GBoard;
import pt.ua.gboard.basic.Position;
import regions.MetroMap;
import pt.ua.gboard.Gelem;
import pt.ua.gboard.basic.CircleGelem;
import pt.ua.gboard.games.Labyrinth;
import regions.Station;
import regions.Train;

/**
 * General description: definition of the Pawn.
 *
 * @author Tiago Madeira 76321
 */
public class Pawn extends Thread
{

    /**
     * Internal data
     */
    private Position startPos;
    private Position destinationPos;
    private MetroMap metroMap;
    private Labyrinth metro;
    private GBoard board;
    private int l, c;
    private int layer;
    private int N;
    private Gelem pawnGelem;
    private int wallet;
    private char ticket = '0';

    /**
     * Constructor
     *
     * @param name     Pawn thread name
     * @param board    GBoard object
     * @param metroMap the information centre of the map
     * @param metro    Labyrinth object
     * @param N        the cell size used in the visual representation
     * @param layer    the layer in which to draw the pawn Gelem
     */
    public Pawn(String name, GBoard board, MetroMap metroMap, Labyrinth metro, int N, int layer)
    {
        super(name);
        assert metroMap != null;
        assert board != null;
        assert metro != null;
        this.board = board;
        this.metroMap = metroMap;
        this.metro = metro;
        this.N = N;
        startPos = getRandomRoadPosition(metroMap.getSpawningPositions());
        destinationPos = getRandomRoadPosition(metroMap.getDestinyPositions());

        l = startPos.line() * N;
        c = startPos.column() * N;
        this.layer = layer;
        this.wallet = 100;
    }

    /**
     * Life cycle
     */
    @Override
    public void run()
    {
        // Randomize the color of the pawn
        float H = (float) ThreadLocalRandom.current().nextDouble(0, 6);
        float S = (float) ThreadLocalRandom.current().nextDouble(0, 1);
        float B = (float) ThreadLocalRandom.current().nextDouble(0, 1);
        java.awt.Color pawnColor = java.awt.Color.getHSBColor(H, S, B);

        // Create sprite
        pawnGelem = new CircleGelem(pawnColor, 100, 1, 1);

        // Bring sprite to life
        board.draw(pawnGelem, l, c, layer);

        // Mark X on destination
        metro.putRoadSymbol(destinationPos.line(), destinationPos.column(), 'X');

        // Get the stations in the path
        Station[] stations = metroMap.getPathStations(startPos, destinationPos);

        while (notAtTarget(stations[0].getLocation()))
        {
            move(stations[0].getLocation());
            GBoard.sleep(100);
        }

        // Buy ticket
        stations[0].buyTicket();

        // Sleep waiting for train at station
        Train t = stations[0].waitForTrain();

        // Pawn went into the train
        board.erase(pawnGelem, l, c, layer);

        // Show the ticket to the inspector
        t.showTicket();

        // Get on the train
        t.getOn();

        // Sleep inside train
        Station currStation = t.waitForStop();
        l = currStation.getLocation().line() * N;
        c = currStation.getLocation().column() * N;

        // Wake up every station, update position, and go back to sleep
        while (!stations[1].equals(currStation))
        {
            currStation = t.waitForStop();
            l = currStation.getLocation().line() * N;
            c = currStation.getLocation().column() * N;
        }

        // Get off the train at destiny station
        t.getOff();
        //System.out.print("I'm out the train!");

        // Bring sprite back
        board.draw(pawnGelem, l, c, layer);

        // Move to the target
        while (notAtTarget(destinationPos))
        {
            move(destinationPos);
            GBoard.sleep(100);
        }

        System.out.println(this.getName() + ": I have arrived at my destination!\n");

        // Unmark X on destination
        metro.putRoadSymbol(destinationPos.line(), destinationPos.column(), ' ');

        board.erase(pawnGelem, l, c, layer);
    }

    /**
     * Returns the amount of money currently in the wallet.
     *
     * @return an integer representing the amount of money in the wallet.
     */
    public int getWalletValue()
    {
        return this.wallet;
    }

    /**
     * Updates the amount of money currently in the Pawn's wallet by adding a
     * transaction value to the current balance.
     *
     * @param transaction amount of money gained or lost. positive if gained;
     *                    negative if lost.
     */
    public void updateWalletValue(int transaction)
    {
        wallet = wallet + transaction;
    }

    /**
     * Used to check and consume the ticket.
     *
     * @return the char representing the ticket.
     */
    public char checkTicket()
    {
        char t = this.ticket;
        this.ticket = '0';
        return t;
    }

    /**
     * Used to see the ticket information.
     *
     * @return the char representing the ticket.
     */
    public char seeTicket()
    {
        return this.ticket;
    }

    /**
     * Assigns a char to the internal ticket variable.
     *
     * @param t the char representing the ticket.
     */
    public void saveTicket(char t)
    {
        this.ticket = t;
    }

    /**
     * Checks if the Pawn is currently not at a target location.
     */
    private boolean notAtTarget(Position target)
    {
        assert target != null;

        Position targetPos = new Position(target.line(), target.column());
        Position currPos = new Position(l / N, c / N);
        double d = distance(currPos, targetPos);

        return d > 0;
    }

    /**
     * Moves the Pawn one step closer to a target location. Makes the decision
     * based on an euclidean distance heuristic.
     *
     * @param target
     */
    private void move(Position target)
    {
        assert target != null;

        Position targetPos = new Position(target.line() * N, target.column() * N);
        Position currPos = new Position(l, c);
        double d = distance(currPos, targetPos);
        // System.out.println("Distance to target is: " + d);
        Position nextPosition = null;

        Position[] possibleMoves = new Position[4];
        possibleMoves[0] = new Position(l + 1, c);
        possibleMoves[1] = new Position(l, c + 1);
        possibleMoves[2] = new Position(l - 1, c);
        possibleMoves[3] = new Position(l, c - 1);

        for (int i = 0; i < 4; i++)
        {
            double tmpDist = distance(possibleMoves[i], targetPos);
            if (tmpDist < d)//&& metro.roadSymbol(possibleMoves[i].line() / N, possibleMoves[i].column() / N) == ' '
            {
                d = tmpDist;
                nextPosition = possibleMoves[i];
            }
        }

        if (nextPosition != null)
        {
            board.move(pawnGelem, l, c, nextPosition.line(), nextPosition.column());
            this.l = nextPosition.line();
            this.c = nextPosition.column();
        } else
        {
            err.println("Don't know how to reach the station!");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns a random position from the possible walking positions.
     */
    private Position getRandomRoadPosition(Position[] walkingPos)
    {
        assert walkingPos != null;

        int idx = ThreadLocalRandom.current().nextInt(0, walkingPos.length);
        return walkingPos[idx];
    }

    /**
     * Calculated the euclidean distance between two Positions.
     */
    private double distance(Position pos1, Position pos2)
    {
        int x = Math.abs(pos1.column() - pos2.column());
        int y = Math.abs(pos1.line() - pos2.line());

        return Math.sqrt(x * x + y * y);
    }

}
