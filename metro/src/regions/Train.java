package regions;

import entities.Inspector;
import entities.Pawn;
import java.util.LinkedList;
import pt.ua.gboard.GBoard;
import pt.ua.gboard.Gelem;
import pt.ua.gboard.basic.ImageGelem;
import pt.ua.gboard.basic.Position;
import pt.ua.gboard.games.Labyrinth;

/**
 * General description: Definition of the Train region.
 *
 * @author Tiago Madeira 76321
 */
public class Train
{

    /**
     * Internal data
     */
    private Labyrinth metro;
    private GBoard board;
    private Track track;
    private LinkedList<String> trackMoves;
    private int l, c;
    private int N;
    private int layer;
    private int moveCnt;
    private Gelem trainRightUp, trainRight, trainRightDown, trainDownRight,
            trainDown, trainDownLeft, trainLeftDown, trainLeft, trainLeftUp,
            trainUpLeft, trainUp, trainUpRight;
    private Gelem currTrain;
    // Offset to draw Gelem correctly
    private int offsetL;
    private int offsetC;

    private Station currStation = null;

    private boolean canShowTicket = false;
    private boolean nextPassengerCanShowTicket = false;
    private char ticketBeingChecked = '0';
    private Pawn passengerBeingChecked = null;

    private boolean trainIsInStation = false;
    private boolean lastPassengerToAwake = false;

    private int numPassengers = 0;
    private int numPassengersToAwake = 0;
    private int numAwakenPassengers = 0;

    /**
     * Constructor
     *
     * @param metroMap Labyrinth object
     * @param board    GBoard object
     * @param track    the Track on which the train moves
     * @param startPos the starting position in which the train spawns
     * @param moveCnt  the index for the list of moves of the cycle
     * @param cellSize the cell size used in the visual representation
     * @param layer    the layer in which to draw the train Gelem
     */
    public Train(Labyrinth metroMap, GBoard board, Track track,
            Position startPos, int moveCnt, int cellSize, int layer)
    {
        assert metroMap != null;
        assert board != null;
        assert track != null;
        this.metro = metroMap;
        this.board = board;
        this.track = track;
        this.trackMoves = track.getTrackMoves();
        this.l = startPos.line() * cellSize;
        this.c = startPos.column() * cellSize;
        this.moveCnt = moveCnt;

        this.N = cellSize;
        this.layer = layer;

        loadImages();
        setupTrain();
    }

    ///////////////////////////////////////////////////////////////////////
    // Pawn
    /**
     * Sleeps waiting for a signal from the Inspector he can show the ticket.
     * When woken up saves information in the shared variables about themselves
     * and their ticket. In the end wakes up the Salesman so he can check the
     * information provided and process the next Pawn.
     */
    public synchronized void showTicket()
    {

        while (!canShowTicket)
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {

            }
        }
        canShowTicket = false;

        ticketBeingChecked = ((Pawn) Thread.currentThread()).checkTicket();
        passengerBeingChecked = ((Pawn) Thread.currentThread());

        // wake up inspector
        nextPassengerCanShowTicket = true;
        notifyAll();
    }

    /**
     * Get on the train
     */
    public synchronized void getOn()
    {
        numPassengers++;
    }

    /**
     * Sleep waiting for the Conductor to signal a station has been reached.
     * Wake up and increment the number of awaken passengers, check the current
     * station of the train. If it was the last passenger to wake up then signal
     * the Conductor to wake up. Return the current station.
     *
     * @return the Station in which the train is currently
     */
    public synchronized Station waitForStop()
    {

        // Wait for the train to reach a station
        while (!trainIsInStation)
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {

            }
        }

        numAwakenPassengers++;
        Station s = currStation;

        // Last passeneger to awake
        if (numAwakenPassengers == numPassengersToAwake)
        {
            //System.out.println("I was last to wake up in train!");
            lastPassengerToAwake = true;
            notifyAll();
        }

        return s;
    }

    /**
     * Get off the train
     */
    public synchronized void getOff()
    {
        numPassengers--;
    }

    ///////////////////////////////////////////////////////////////////////
    // Inspector
    /**
     * Infinitely checking tickets within a train. Signal that one ticket may be
     * shown and sleep. Wait to be awaken by a Pawn who placed information in
     * the shared variables about themselves and their ticket. Check whether the
     * ticket is valid and apply a fine if it's not. Reset variables.
     */
    public synchronized void checkTickets()
    {
        //System.out.println("Waiting for tickets!");

        canShowTicket = true;
        notifyAll();

        // Forever checking tickets
        while (true)
        {
            try
            {
                while (!nextPassengerCanShowTicket)
                {
                    try
                    {
                        wait();
                    } catch (InterruptedException e)
                    {
                        throw e;
                    }
                }

                nextPassengerCanShowTicket = false;

                if (ticketBeingChecked == track.getSymbol())
                {
                    System.out.println(((Inspector) Thread.currentThread()).getName()
                            + ": " + passengerBeingChecked.getName() + "'s ticket is valid for this train!\n");
                } else
                {
                    System.out.println(((Inspector) Thread.currentThread()).getName()
                            + ": This ticket is not valid! " + passengerBeingChecked.getName()
                            + " will be fined!\n");
                    passengerBeingChecked.updateWalletValue(50);
                }
                passengerBeingChecked = null;
                ticketBeingChecked = '0';

                canShowTicket = true;
                notifyAll();
            } catch (InterruptedException e)
            {

            }
        }

    }

    ///////////////////////////////////////////////////////////////////////
    // Conductor
    /**
     * If there are passengers within the train, save the station reference in
     * the shared variable and signal them to wake up. Sleep waiting for all of
     * them to wake up and check the station. Upon being awaken by the last
     * passenger to wake up and check, reset the variables.
     *
     * @param s the Station reference to be saved in the shared variable
     */
    public synchronized void letPassengersOffTheTrain(Station s)
    {
        if (numPassengers == 0)
        {
            return;
        }

        this.currStation = s;
        this.numPassengersToAwake = numPassengers;
        trainIsInStation = true;
        notifyAll();

        // Sleep waiting for passengers to wake up and check the current station
        while (!lastPassengerToAwake)
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {

            }
        }

        // All the passengers have checked the station and left if they should
        this.currStation = null;
        this.numPassengersToAwake = 0;
        this.numAwakenPassengers = 0;
        trainIsInStation = false;
        lastPassengerToAwake = false;
    }

    /**
     * Load images and create the Gelems required by the train animation.
     */
    private void loadImages()
    {
        // Create sprite
        trainRightUp = new ImageGelem("trainRightUp.png", board, 100, 4, 4);
        // Right
        trainRight = new ImageGelem("trainRight.png", board, 100, N / 4, N);

        trainRightDown = new ImageGelem("trainRightDown.png", board, 100, 4, 4);
        trainDownRight = new ImageGelem("trainDownRight.png", board, 100, 4, 4);
        // Down
        trainDown = new ImageGelem("trainDown.png", board, 100, N, N / 4);

        trainDownLeft = new ImageGelem("trainDownLeft.png", board, 100, 4, 4);
        trainLeftDown = new ImageGelem("trainLeftDown.png", board, 100, 4, 4);
        // Left
        trainLeft = new ImageGelem("trainLeft.png", board, 100, N / 4, N);

        trainLeftUp = new ImageGelem("trainLeftUp.png", board, 100, 4, 4);
        trainUpLeft = new ImageGelem("trainUpLeft.png", board, 100, 4, 4);
        // Up
        trainUp = new ImageGelem("trainUp.png", board, 100, N, N / 4);

        trainUpRight = new ImageGelem("trainUpRight.png", board, 100, 4, 4);
    }

    /**
     * Place the train on the track and draw the corresponding Gelem.
     */
    private void setupTrain()
    {

        String move = trackMoves.get(moveCnt);

        // First move of the track positions train
        switch (move)
        {
            case "Right":
                offsetL = 0;
                offsetC = 0;
                currTrain = trainRight;
                break;
            case "Left":
                offsetL = 3;
                offsetC = 0;
                currTrain = trainLeft;
                break;
            case "Down":
                offsetL = 0;
                offsetC = 3;
                currTrain = trainDown;
                break;
            case "Up":
                offsetL = 0;
                offsetC = 0;
                currTrain = trainUp;
                break;
        }

        // Bring sprite to life
        board.draw(currTrain, l + offsetL, c + offsetC, layer);

        //System.out.println("Starting station: ");
        //System.out.println(l + ", " + c);
        //System.out.println("################");
        GBoard.sleep(200);

    }

    /**
     * Moving function for the train. Takes into consideration the next two
     * steps in oder to correctly animate the motion using the Image Gelems.
     */
    public void move()
    {

        // Step
        int cInc = N / 2;
        int lInc = N / 2;

        Gelem oldTrain = currTrain;
        int oldL = l + offsetL;
        int oldC = c + offsetC;

        String move, nextMove;
        //markPosition(l / N, c / N);
        move = trackMoves.get(moveCnt % trackMoves.size());
        nextMove = trackMoves.get((moveCnt + 1) % trackMoves.size());

        switch (move)
        {
            case "Right":
                if (nextMove.equals(move))
                {
                    c += cInc * 1;
                    offsetL = 0;
                    offsetC = 0;
                    currTrain = trainRight;
                } else if (nextMove.equals("Down"))
                {   // outer turn
                    l += lInc * -1;
                    offsetL = 2;
                    offsetC = 0;
                    currTrain = trainRightDown;
                } else if (nextMove.equals("Up"))
                {   //inner turn
                    l += lInc * -1;
                    c += cInc * 2;
                    offsetL = 0;
                    offsetC = -2;
                    currTrain = trainRightUp;
                }
                break;
            case "Left":
                if (nextMove.equals(move))
                {
                    c += cInc * -1;
                    offsetL = 3;
                    offsetC = 0;
                    currTrain = trainLeft;
                } else if (nextMove.equals("Down"))
                {   // inner turn
                    l += lInc * 1;
                    c += cInc * -2;
                    offsetL = 0;
                    offsetC = 2;
                    currTrain = trainLeftDown;
                } else if (nextMove.equals("Up"))
                {   // outer turn
                    l += lInc * 1;
                    offsetL = -2;
                    offsetC = 0;
                    currTrain = trainLeftUp;
                }
                break;
            case "Down":
                if (nextMove.equals(move))
                {
                    l += lInc * 1;
                    offsetL = 0;
                    offsetC = 3;
                    currTrain = trainDown;
                } else if (nextMove.equals("Right"))
                {   // inner turn
                    l += lInc * 2;
                    c += cInc * 1;
                    offsetL = -2;
                    offsetC = 0;
                    currTrain = trainDownRight;
                } else if (nextMove.equals("Left"))
                {   // outer turn
                    c += cInc * 1;
                    offsetL = 0;
                    offsetC = -2;
                    currTrain = trainDownLeft;
                }
                break;
            case "Up":
                if (nextMove.equals(move))
                {
                    l += lInc * -1;
                    offsetL = 0;
                    offsetC = 0;
                    currTrain = trainUp;
                } else if (nextMove.equals("Right"))
                {   // outer turn
                    c += cInc * -1;
                    offsetL = 0;
                    offsetC = 2;
                    currTrain = trainUpRight;
                } else if (nextMove.equals("Left"))
                {   // inner turn
                    l += lInc * -2;
                    c += cInc * -1;
                    offsetL = 2;
                    offsetC = 0;
                    currTrain = trainUpLeft;
                }
                break;
        }

        // Redraw the Gelem
        board.erase(oldTrain, oldL, oldC, layer);
        board.draw(currTrain, l + offsetL, c + offsetC, layer);

        // increase the index for the list of moves
        moveCnt++;
    }

    /**
     * Returns the station where the Train is currently at.
     *
     * @return the Station in which the train is currently.
     */
    public Station getCurrentStation()
    {
        return track.getStation(l, c);
    }

    /**
     * Returns the track on which the train rides.
     *
     * @return the Track on which the train rides.
     */
    public Track getTrack()
    {
        return track;
    }
}
