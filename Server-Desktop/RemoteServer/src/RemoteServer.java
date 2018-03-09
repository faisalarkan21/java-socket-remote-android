import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteServer {

	private static ServerSocket server = null;
	private static Socket client = null;
	private static BufferedReader in = null;
	private static String line;
	private static boolean isConnected = true;
	private static Robot robot;
	private static final int SERVER_PORT = 5000;

	public static void main(String[] args) {

		System.out.println("Server Running!");
		try {
			robot = new Robot();
			server = new ServerSocket(SERVER_PORT);
			client = server.accept();
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			System.out.println("Socket Open successfully");
		} catch (IOException e) {
			System.out.println("Error in opening Socket");
			System.exit(-1);
		} catch (AWTException e) {
			System.out.println("Error in creating robot instance");
			System.exit(-1);
		}

		while (isConnected) {
			try {
				line = in.readLine();
				System.out.println(line);

				if (line.contains(",")) {
					float movex = Float.parseFloat(line.split(",")[0]);
					float movey = Float.parseFloat(line.split(",")[1]);
					Point point = MouseInfo.getPointerInfo().getLocation();
					float nowx = point.x;
					float nowy = point.y;
					robot.mouseMove((int) (nowx + movex), (int) (nowy + movey));
				}

				else if (line.contains("left_click")) {

					robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

				} else if (line.contains("middle_click")) {

					robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
					robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);

				} else if (line.contains("right_click")) {

					robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
					robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);

				}

				else if (line.equalsIgnoreCase("exit")) {
					isConnected = false;
					server.close();
					client.close();
				}
			} catch (IOException e) {
				System.out.println("Read failed");
				System.exit(-1);
			}
		}
	}
}