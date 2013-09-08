/**
 * Author: Sebastian Goscik
 * Created: 17/08/13 19:24
 * Project: RokuECP
 * All functions a Roku device has as according to the Roku ECP specification: http://sdkdocs.roku.com/display/sdkdoc/External+Control+Guide
 */

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class Roku
{
	private String IP;
	private int PORT;
	private String USN;
	private Socket socket;
	private long lastSent; //Hack to fix issue where roku stops responding to requests after a few seconds	

	public Roku(String ipAddress, int portNumber, String uniqueSerialNumber) throws IOException
	{
		IP = ipAddress;
		PORT = portNumber;
		USN = uniqueSerialNumber;
		socket = new Socket(IP, PORT);
	}

	public String getIP()  { return IP; }

	public int getPORT() { return PORT; }

	public String getUSN() { return USN; }

	public void reconnect() throws IOException { socket = new Socket(IP, PORT); }

	//Available Keys:
	//Home,Rev,Fwd,Play,Select,Left,Right,Up,Down,Back,
	//InstantReplay,Info,Backspace,Search,Enter and Lit_* (Where * is a character)

	public void keyPress(String key) throws IOException { send("POST /keypress/" + key + " HTTP/1.1\r\n\r\n"); }

	public void keyDown(String key) throws IOException { send("POST /keydown/" + key + " HTTP/1.1\r\n\r\n"); }

	public void keyUp(String key) throws IOException { send("POST /keyup/" + key + " HTTP/1.1\r\n\r\n"); }

	public void launchApp(int appID) throws IOException { send("POST /launch/" + appID + " HTTP/1.1\r\n\r\n"); }

	private void send(String data) throws IOException
	{
      		if (!socket.isConnected() || socket.isOutputShutdown() || !socket.isBound()) connect();
    		if (System.currentTimeMillis()-lastSent>2000) connect();
        	lastSent = System.currentTimeMillis();
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
		wr.write(data);
		wr.flush();
	}

	public BufferedImage getIcon(int appID) throws IOException
	{
		BufferedImage img = ImageIO.read(new URL("http://" + IP + ":" + PORT + "/query/icon/" + appID));
		while (img == null) {img = ImageIO.read(socket.getInputStream());}
		return img;
	}

	public HashMap queryApps() throws Exception
	{
		HashMap output = new HashMap();

		URL url = new URL("http://" + IP + ":" + PORT + "/query/apps");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		InputStream xml = connection.getInputStream();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document xmlDocument = db.parse(xml);
		Node appNode = xmlDocument.getFirstChild();
		Node app = appNode.getFirstChild();

		while (app != null)
		{
			String nodeName = app.getNodeName();
			if (nodeName.equals("app"))
				output.put(app.getAttributes().getNamedItem("id").getNodeValue(), app.getTextContent());

			app = app.getNextSibling();
		}

		return output;
	}

	public void input(HashMap input) throws IOException
	{
		String post = "POST /input?";

		Iterator iterator = input.keySet().iterator();

		while (iterator.hasNext())
		{
			String key = iterator.next().toString();
			String value = input.get(key).toString();

			post += key + "=" + value;
			if (iterator.hasNext()) post += "&";
		}
		send(post + " HTTP/1.1\r\n\r\n");
	}

}
