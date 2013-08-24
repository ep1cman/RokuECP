/**
 * Author: Sebastian Goscik
 * Created: 16/08/13 21:24
 * Project: RokuECP
 * Searches a network for available Roku devices
 */

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class RokuECP
{
	//Constants
	public static final String SEARCHSTRING =
			"M-SEARCH * HTTP/1.1\r\n" +
					"ST: roku:ecp\r\n" +
					"MAN: \"ssdp:discover\"\r\n" +
					"HOST: 239.255.255.250:1900";

	private static SocketAddress multicastGroup;
	private static MulticastSocket socketMulticast;
	private static NetworkInterface netInterface;

	private RokuECP(){}

	public static Roku[] search(int timeOut) throws IOException
	{
		//Setup socket
		InetAddress localAddr = InetAddress.getLocalHost();
		multicastGroup = new InetSocketAddress("239.255.255.250", 1900);
		socketMulticast = new MulticastSocket(new InetSocketAddress(localAddr, 1990));
		netInterface = NetworkInterface.getByInetAddress(localAddr);
		socketMulticast.joinGroup(multicastGroup, netInterface);

		//Variables
		ArrayList<Roku> devices = new ArrayList<Roku>();

		//Send out search packet
		send(SEARCHSTRING);

		//Set search timeout
		socketMulticast.setSoTimeout(timeOut);

		//Search for devices
		while (true) {
			try {
				//Get packet
				String data = new String(receive().getData());
				String[] newRoku = data.split("\n");

				//Check if its a response from the roku
				if (newRoku[0].equals("HTTP/1.1 200 OK\r") && newRoku[2].equals("ST: roku:ecp\r")) {
					String IP = newRoku[6].split("/")[2].split(":")[0];
					int PORT = Integer.parseInt(newRoku[6].split("/")[2].split(":")[1]);
					String USN = newRoku[3].split(":")[4];

					devices.add(new Roku(IP, PORT, USN));
				}
			}
			//Search got no more responses
			catch (SocketTimeoutException e) {
				return devices.toArray(new Roku[devices.size()]);
			}
		}
	}

	private static void send(String data) throws IOException
	{
		DatagramPacket dp = new DatagramPacket(data.getBytes(), data.length(), multicastGroup);
		socketMulticast.send(dp);
	}

	private static DatagramPacket receive() throws IOException
	{
		byte[] buffer = new byte[1024];
		DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
		socketMulticast.receive(dp);
		return dp;
	}
}
