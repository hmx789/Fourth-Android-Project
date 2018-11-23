package com.example.hassan.project4;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("HandlerLeak")
    private Handler mainHandler = new Handler() {
        public void handleMessage (Message msg) {
            int what = msg.what;
            String display;
            switch(what) {
                case SECRET_NUMBER_P1:
                    display = "Secret: " + msg.obj;
                    p1View.setText(display);
                    break;
                case SECRET_NUMBER_P2:
                    display = "Secret: " + msg.obj;
                    p2View.setText(display);
                    break;
                case RECEIVE_GUESS_P2:
                    Message p1Msg = t1Handler.obtainMessage(RECEIVE_GUESS);
                    p1Msg.obj = msg.obj;
                    p1Msg.arg1 = msg.arg1;
                    p1Msg.setData(msg.getData());
                    t1Handler.sendMessage(p1Msg);
                    break;

                case RECEIVE_GUESS_P1:
                    Message p2Msg = t2Handler.obtainMessage(RECEIVE_GUESS);
                    p2Msg.obj = msg.obj;
                    p2Msg.arg1 = msg.arg1;
                    t2Handler.sendMessage(p2Msg);
                    break;


                case UPDATE_P1:
                    int guessNum = msg.getData().getInt("guessNum");

                    if (guessNum > 20) {
                        return;
                    }

                    String guess = msg.getData().getString("guess");
                    ArrayList<String> reps = msg.getData().getStringArrayList("response");

                    TextView tv = new TextView(getApplicationContext());
                    display = guessNum + ". " + guess;
                    tv.setText(display);
                    p1Layout.addView(tv);

                    for (String x : reps) {
                        TextView responseViewP1 = new TextView(getApplicationContext());
                        display = guessNum + ". " + x;
                        responseViewP1.setText(display);
                        rp1Layout.addView(responseViewP1);
                    }
                    if (guess.equals(opponentNum2)) {
                        closeThreads();
                        PLAYER_WON = true;
                        display = "Player 1 Wins!";
                        gameStatus.setText(display);
                    }
                    break;
                case UPDATE_P2:
                    int guessNum2 = msg.getData().getInt("guessNum");

                    if (guessNum2 > 20) {
                        return;
                    }
                    String guess2 = msg.getData().getString("guess");
                    ArrayList<String> reps2 = msg.getData().getStringArrayList("response");

                    TextView tv2 = new TextView(getApplicationContext());
                    display = guessNum2 + ". " + guess2;
                    tv2.setText(display);
                    p2Layout.addView(tv2);

                    for (String x : reps2) {
                        TextView responseViewP1 = new TextView(getApplicationContext());
                        display = guessNum2 + ". " + x;
                        responseViewP1.setText(display);
                        rp2Layout.addView(responseViewP1);
                    }
                    if (guess2.equals(opponentNum)) {
                        closeThreads();
                        PLAYER_WON = true;
                        display = "Player 2 Wins!";
                        gameStatus.setText(display);
                    }
                    break;
                case TIE:
                    closeThreads();
                    TIED = true;
                    display = "Tie! No one guessed correctly";
                    gameStatus.setText(display);
            }
        }
    };

    Handler t1Handler;
    Handler t2Handler;

    private Button startButton;
    private TextView p1View;
    private TextView p2View;
    private TextView gameStatus;
    private TextView p1guess;
    private TextView p2guess;
    private LinearLayout p1Layout;      //Guesses layout
    private LinearLayout p2Layout;
    private LinearLayout rp2Layout;     // Responses layout
    private LinearLayout rp1Layout;
    Thread p1 = null;
    Thread p2 = null;
    String opponentNum;
    String opponentNum2;
    int countGuesses = 0;
    int countGuesses2 = 0;
    boolean TIED = false;
    boolean PLAYER_WON = false;

    private static final String TAG = "MainActivity";
    public static final int SECRET_NUMBER_P1 = 0;
    public static final int SECRET_NUMBER_P2 = 1;
    public static final int RECEIVE_GUESS_P1 = 2;
    public static final int RECEIVE_GUESS_P2 = 3;
    public static final int RECEIVE_GUESS = 4;
    public static final int P2_WINS = 8;
    public static final int P1_WINS = 9;
    public static final int STOP = 10;
    public static final int UPDATE_P1 = 11;
    public static final int UPDATE_P2 = 12;
    public static final int UPDATE_RESPONSE_AND_GUESS = 13;
    public static final int TIE = 14;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        p1View = findViewById(R.id.player1num);
        p2View = findViewById(R.id.player2num);
        gameStatus = findViewById(R.id.gameStatus);
        p1guess = findViewById(R.id.guess1);
        p2guess = findViewById(R.id.guess2);
        p1Layout = (LinearLayout) findViewById(R.id.scrollLayout);
        p2Layout = (LinearLayout) findViewById(R.id.scrollLayout2);
        rp2Layout = (LinearLayout) findViewById(R.id.responseLayout2);
        rp1Layout = (LinearLayout) findViewById(R.id.responseLayout);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (p1 != null || p2 != null) {

                    if (PLAYER_WON || TIED) {   // Threads already closed if player has won or there has been a tie
                        clearBoard();
                        clearVariables();
                    }
                    else {                      // If closing in the middle of the game, need to close threads also
                        closeThreads();
                        clearBoard();
                        clearVariables();
                    }

                }
                p1 = new Thread(new P1Runnable());
                p2 = new Thread(new P2Runnable());
                p1.start();
                p2.start();
                gameStatus.setText("Game Status: Started");
            }
        });

    }

    void closeThreads() {
        if (p1.isAlive()) {
            p1.interrupt();
        }
        if (p2.isAlive()) {
            p2.interrupt();
        }
        mainHandler.removeCallbacksAndMessages(null);
        if (p1.isAlive() ) {
            t1Handler.removeCallbacksAndMessages(null);
        }
        if (p2.isAlive()) {
            t2Handler.removeCallbacksAndMessages(null);
        }
        if (p1.isAlive()) {
            t1Handler.getLooper().quitSafely();
        }
        if (p2.isAlive()) {
            t2Handler.getLooper().quitSafely();
        }

    }

    void clearVariables() {
        countGuesses = 0;
        countGuesses2 = 0;
        opponentNum = "";
        opponentNum2 = "";
        TIED = false;
        PLAYER_WON = false;
    }

    void clearBoard() {
        p1Layout.removeAllViews();
        p2Layout.removeAllViews();
        rp1Layout.removeAllViews();
        rp2Layout.removeAllViews();
    }

    // Strategy for player1
    public String guessNumber1(ArrayList<String> responses) {
        char newGuess[] = new char[4];
        ArrayList<Character> chars = new ArrayList<>();
        ArrayList<Character> charsUsed = new ArrayList<>();
        Random rand = new Random();
        String number = generateRandomNumber();

        if (responses == null) {
            return number;
        }
        for (String response : responses) {     // Puts all the characters in the right position
            if (! response.contains("wrong") ) {
                int index = response.charAt(response.length()-1) - '0';
                newGuess[index] =response.charAt(0);
                charsUsed.add(response.charAt(0));
            }
            else {          //If they were not in the right pos, but right numbers save them
                chars.add(response.charAt(0));
            }
        }
        for (int i = 0; i < newGuess.length;i++) {
            if (newGuess[i] == '\u0000') {
                if (chars.size() > 0) {     // If their is some saved nums, not used, use them
                    int index = rand.nextInt(chars.size());
                    newGuess[i] = chars.get(index);
                    charsUsed.add(chars.get(index));
                    chars.remove(index);
                }
                else {  // If used all the save numbers, generate random ones that aren't being used
                    char x = Integer.toString(rand.nextInt(9)).charAt(0);
                    while (charsUsed.contains(x)) {
                        x = Integer.toString(rand.nextInt(9)).charAt(0);
                    }
                    newGuess[i] = x;
                    charsUsed.add(x);
                }
            }
        }
        String sentGuess = new String(newGuess);
        return sentGuess;
    }

    // Strategy for player 2
    public String guessNumber2() {
        return generateRandomNumber();
    }

    // Generates a random number that makes sure all digits are different
    public String generateRandomNumber() {
        int rNum = 0;
        HashSet<Integer> uniqueNums = new HashSet<>();
        Random rand = new Random();
        final StringBuilder sb = new StringBuilder();

        while (rNum == 0) {
            //Making sure first number isnt 0
            rNum = rand.nextInt(9);
        }
        uniqueNums.add(rNum);
        sb.append(rNum);

        while (uniqueNums.size() < 4) {

            rNum = rand.nextInt(9);

            if (uniqueNums.contains(rNum))
                continue;

            uniqueNums.add(rNum);
            sb.append(rNum);
        }
        return (sb.toString());
    }

    // Given the correct number and guess, make responses for the player
    public ArrayList<String> getResponses(String guess, String correctNum) {
        ArrayList<String> responses = new ArrayList<>();
        String response = "";
        for (int i = 0; i < guess.length();i++) {
            char guessChar = guess.charAt(i);
            int index = correctNum.indexOf(guessChar);

            if (index == i) {
                response = guessChar + " in correct pos:" + index;
                responses.add(response);


            }
            else if (index != -1) {
                response = guessChar + " correct number, wrong pos";
                responses.add(response);
            }

        }
        if (responses.size() <= 0) {
            response = "All numbers wrong";
            responses.add(response);
        }

        return responses;
    }


    public class P1Runnable implements Runnable {
        String myGuess;
        ArrayList<String> responses = new ArrayList<String>();
        @SuppressLint("HandlerLeak")
        public void run() {

            //Generating and sending secret number to main
            opponentNum = generateRandomNumber();
            Message numMsg = mainHandler.obtainMessage(SECRET_NUMBER_P1);
            numMsg.obj = opponentNum;
            mainHandler.sendMessage(numMsg);

            //Sending initial guess to player2
            countGuesses++;
            myGuess = generateRandomNumber();
            Message msg = mainHandler.obtainMessage(RECEIVE_GUESS_P1);
            msg.obj = myGuess;
            msg.arg1 = countGuesses;

            mainHandler.sendMessage(msg);    //Sending the guess with how many times ive guessed

            Looper.prepare();

            t1Handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    int what = msg.what;
                    Message guessMsg;
                    String guessedNum;
                    String response;
                    switch(what) {
                        case RECEIVE_GUESS:
                            // Checking to see if T2's guess was right

                            // Generating response for guess from P2 and sending back to P2
                            if (msg.arg1 <= 20) {
                                responses = getResponses((msg.obj).toString(), opponentNum);
                                Message msg1 = t2Handler.obtainMessage(UPDATE_RESPONSE_AND_GUESS);
                                Bundle b = new Bundle();
                                b.putStringArrayList("response", responses);
                                b.putString("guess", (msg.obj).toString());
                                b.putInt("guessNum", msg.arg1);
                                msg1.setData(b);
                                t2Handler.sendMessage(msg1);
                            }

                            // Making my guess and sending to main
                            countGuesses++;
                            guessedNum = guessNumber1(msg.getData().getStringArrayList("response"));
                            Message msgR = mainHandler.obtainMessage(RECEIVE_GUESS_P1);
                            msgR.obj = guessedNum;
                            msgR.arg1 = countGuesses;

                            if (countGuesses > 20 && msg.arg1 > 20) {
                                Message mainTie = mainHandler.obtainMessage(TIE);
                                mainHandler.sendMessageAtFrontOfQueue(mainTie);
                            }

                            mainHandler.sendMessage(msgR);

                            break;

                        case P1_WINS:
                            return;
                        case STOP:
                            return;
                        case UPDATE_RESPONSE_AND_GUESS:
                            // Updating the response and guess for P1
                            Message uGuess = mainHandler.obtainMessage(UPDATE_P1);
                            uGuess.setData(msg.getData());
                            mainHandler.sendMessage(uGuess);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;

                    }
                }
            };

            Looper.loop();


        }

    }

    public class P2Runnable implements Runnable {
        Message msg;
        String myGuess;
        ArrayList<String> responses = new ArrayList<>();
        @SuppressLint("HandlerLeak")
        public void run() {

            //Getting a random number
            opponentNum2 = generateRandomNumber();
            // Sending random number for player 1 thread
            msg = mainHandler.obtainMessage(SECRET_NUMBER_P2);
            msg.obj = opponentNum2;

            mainHandler.sendMessage(msg);

            Looper.prepare();
            t2Handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    int what = msg.what;
                    String guessedNum;
                    String response;
                    switch(what) {
                        case RECEIVE_GUESS:

                            // Generating response and sending to t1
                            if (msg.arg1 <= 20) {
                                responses = getResponses((msg.obj).toString(), opponentNum2);
                                Message msg1 = t1Handler.obtainMessage(UPDATE_RESPONSE_AND_GUESS);
                                Bundle b = new Bundle();
                                b.putStringArrayList("response", responses);
                                b.putString("guess", (msg.obj).toString());
                                b.putInt("guessNum", msg.arg1);
                                msg1.setData(b);
                                t1Handler.sendMessage(msg1);
                            }

                            //Making my guess and sending to main
                            countGuesses2++;
                            guessedNum = guessNumber2();
                            Message mMsg = mainHandler.obtainMessage(RECEIVE_GUESS_P2);
                            Bundle responseBundle = new Bundle();
                            responseBundle.putStringArrayList("response",responses);
                            mMsg.obj = guessedNum;
                            mMsg.arg1 = countGuesses2;
                            mMsg.setData(responseBundle);

                            if (countGuesses2 > 20 && msg.arg1 > 20) {
                                Message mainTie = mainHandler.obtainMessage(TIE);
                                mainHandler.sendMessageAtFrontOfQueue(mainTie);
                            }
                            mainHandler.sendMessage(mMsg);

                            break;
                        case P2_WINS:
                            return;
                        case STOP:
                            return;
                        case UPDATE_RESPONSE_AND_GUESS:
                            // Updating the response and guess for P2
                            Message uGuess = mainHandler.obtainMessage(UPDATE_P2);
                            uGuess.setData(msg.getData());
                            mainHandler.sendMessage(uGuess);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            };

            Looper.loop();

        }

    }

}
