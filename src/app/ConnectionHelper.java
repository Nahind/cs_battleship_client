package app;

import api.IConnectionHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import gui.GameGUIController;
import javafx.scene.control.TextArea;
import model.Configuration;


public class ConnectionHelper implements IConnectionHelper {

	private static final boolean LOG = true;

	private static final boolean LOCAL = false;

	private String serverName = "-1";
	private int serverPort = -1;
	private String response;

	private PrintWriter out;
	private BufferedReader in;
	private Socket clientSocket;

	private List<String> model_ = new ArrayList<String>();
	private String host;

	private TextArea logView;
    private GameGUIController gameGUIController;
    private Configuration configuration = Configuration.getInstance();

    /** Singleton Pattern */
    private ConnectionHelper() {}

    private static ConnectionHelper INSTANCE = new ConnectionHelper();

    public static ConnectionHelper getInstance() { return INSTANCE; }

    public void setGUIController(GameGUIController c) {
        this.gameGUIController = c;
        this.logView = c.getLogContainer();
    }

	@Override
	public boolean checkConnection() {
		boolean result = isConnected();
		if (!result)
			viewClearConnexion();// error("plus de connexion");
		return result;
	}

	private void openSocket() throws UnknownHostException, IOException {
		if (isConnected()) {
			info("déjà connecté");
			return;
		}
		info("ouverture de la socket " + serverName + "-" + serverPort);
		if (serverPort == -1)
			throw new IOException("port error");

		if (serverName == null || serverName.equals("-1"))
			throw new IOException("host error");

		try {
			clientSocket = new Socket(serverName, serverPort);

			host = clientSocket.getLocalAddress().getHostAddress();
			// int port = ((InetSocketAddress)
			// clientSocket.getLocalSocketAddress()).getPort();
			viewSetLocal(host, serverPort);// port);

		} catch (ConnectException e) {
			error("Connection refusée ");// + e.toString());
			error("Pas de serveur: " + serverName + "-" + serverPort);
			viewClearConnexion();
			viewSetNoServer();
			in = null;
			out = null;
			throw new IOException("connection error");
		}
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		out = new PrintWriter(clientSocket.getOutputStream(), true); // autoflush
		info("Connected to the server");
	}

	private void closeSocket(boolean verbose) {
		if (!isConnected()) {
			if (verbose)
				info("session allready closed");
			return;
		}

		info("closing socket");
		if (clientSocket != null)
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		if (in != null)
			try {
				in.close();
				in = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		if (out != null) {
			out.close();
			out = null;
		}
	}

	private void handleServerResponse() {
		info(response == null ? "session ended" : response);
		if (response == null) {
			closeSocket(true);
			return;
		}


        String[] cmd = response.split(" ");

		switch (cmd[0]) {
            case "player":
                String playerName = cmd[1];
                gameGUIController.setPlayerName(playerName);
                configuration.setPlayerName(playerName);
                break;
            case "opponent":
                String opponentName = cmd[1];
                gameGUIController.setOpponent(opponentName);
                configuration.setOpponentName(opponentName);
                break;
            case "boats":
                String boats = cmd[1];
                gameGUIController.deserializeBoats(boats);
                break;
            case "success":
                String[] split = cmd[2].split(",");
                String state = cmd[1];
                int row = Integer.parseInt(split[0]);
                int col = Integer.parseInt(split[1]);
                if (cmd[3].equals("opponent")) {
                    gameGUIController.updateFieldUI(row, col, state, true);
                } else {
                    gameGUIController.updateFieldUI(row, col, state, false);
                }
                break;
            case "opponent-has-joined":
                gameGUIController.inform(configuration.OPPONENT_JOINED_TEXT);
                break;
			case "error":
				String error = cmd[1];
				if (error.equals("turn")) {
					gameGUIController.inform(configuration.NOT_YOUR_TURN_TEXT);
				} else if (error.equals("already-hit")) {
					gameGUIController.inform(configuration.ALREADY_HIT_TEXT);
				}
				break;
			case "sunk":
				gameGUIController.drawSunkBoat(cmd[1], cmd[2], cmd[3], true);
				System.out.println("sunk received");
				break;
            case "you-sank":
                gameGUIController.drawSunkBoat(cmd[1], cmd[2], cmd[3], false);
                System.out.println("sunk received");
                break;
            case "message":
                String message = response.split("::")[1];
                gameGUIController.appendMessage(message);
                System.out.println("mess = " + message);
                break;
            case "victory":
                gameGUIController.inform("VICTORY ! You badly defeated your opponent !");
                gameGUIController.disableFields();
                break;
            case "defeat":
                gameGUIController.inform("DEFEAT ! Sorry bro !");
                gameGUIController.disableFields();
                break;
        }

        clog(">> " + response);

	}

	private void readServer() {
		info("readServer waiting");
		response = null;
		try {
			response = in.readLine(); // bloquant - attendre la r�ponse du serveur

		} catch (IOException e) {
			info("socket fermée");
			closeSocket(false);
			return;
		} catch (Exception e) {
			error("(2) while read response (" + e.toString() + ")");
			closeSocket(false);
			return;
		}
		if (response == null) {
			info("fin de la connexion");
			closeSocket(false);
			return;
		}
	}

	private static void delay(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void disconnect(boolean verbose) {
		if (isConnected())
			closeSocket(verbose);
		else if (verbose)
			clog("allready disconnected !");
	}

	@Override
	public boolean connect(String host, int port) {
		if (isConnected()) {
			clog("allready connected !");
			return true;
		}
		new SessionThread(host, port).start();
		model_.clear();
		delay(100);
		return isConnected();
	}

	private boolean isConnected() {
		boolean result = clientSocket != null && !clientSocket.isClosed() && clientSocket.isConnected();

		if (!result) {
			if (clientSocket == null)
				clog("clientSocket == null");
			if (clientSocket != null && clientSocket.isClosed())
				clog("clientSocket.isClosed()");
			if (clientSocket != null && !clientSocket.isConnected())
				clog("!clientSocket.isConnected()");
		}
		return result;
	}

	private boolean openConnection() {
		try {
			openSocket();
			// out.println("hello");
			return true;
		} catch (UnknownHostException e) {
			error("serveur inconnu: " + e.getMessage());
		} catch (IOException e) {
			error("erreur de connexion");
		} catch (Exception e) {
			error("(connect 3) " + e.toString());
		}
		return false;
	}

	@Override
	public void send(String host, int port, String cmd) {
		if (cmd != null && !cmd.isEmpty()) {
			if (isConnected()) {
				if (!serverName.equals(host) || serverPort != port)
					disconnect(true);
			}
			if (!isConnected())
				connect(host, port);
			if (isConnected())
				send(cmd);
		} else
			clog("command is empty !");
	}

	@Override
	public boolean send(String cmd) {
		if (cmd != null && !cmd.isEmpty()) {
			info("send " + serverName + ":" + serverPort + "[" + cmd + "]");
			if (!isConnected())
				connect(serverName, serverPort);
			if (isConnected()) {
				out.println(cmd);

				return true;
			} else
				error("no session");
		} else
			clog("command is empty !");
		return false;
	}

	class SessionThread extends Thread {

		public SessionThread(String host, int port) {
			if (isConnected())
				disconnect(true);
			serverName = host;
			serverPort = port;
		}

		@Override
		public void run() {
			if (openConnection()) {
				while (checkConnection()) {

					readServer();
					handleServerResponse();
				}
				disconnect(false);
			}
		}
	}

	private void viewSetLocal(String localhost, int localport) {

        clog("connexion depuis " + localhost + "." + localport + " �tablie");
	}

	private void viewClearConnexion() {

        clog("plus de connexion ");
	}

	private void viewSetNoServer() {

        clog("plus de serveur ");
	}

	private void clog(String mesg) {
		if (LOG) {
			log(mesg);
            System.out.println(mesg);
		}
	}

    public void error(String mesg) {
        response("Err[" + mesg+"]");
    }

    public void info(String mesg) {
        response("Info[" + mesg+"]");
    }

    public void log(final String mesg) {
        response("L_ " + mesg);
    }

    protected void response(final String mesg) {

	    // Can't append messages to TextArea too quickly
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

	    //logView.appendText(mesg+"\r\n");
    }

}
