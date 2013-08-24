import javax.swing.*;
import java.util.HashMap;

/**
 * Author: Sebastian Goscik
 * Created: 16/08/13 21:39
 * Project: RokuECP
 * A small program to demonstrate the functionality of this library
 */

public class Tester
{
	public static void main(String[] args)
	{
		try {
			//Searching for Roku devices on the network
			Roku[] devices = RokuECP.search(3000);

			//Connecting to the first that responded
			Roku device = devices[0];

			//Button press
			device.keyPress("Right");
			Thread.sleep(600);
			device.keyPress("Left");
			Thread.sleep(600);

			//Launch App
			device.launchApp(11);
			Thread.sleep(2000);
			device.keyPress("Home"); //back to menu

			//Get list of apps
			HashMap apps = device.queryApps();
			if (apps != null) {
				for (Object myVal : apps.keySet()) {
					System.out.println((String) myVal + " " + (String) apps.get(myVal));
				}
			}

			//Get app icon
			ImageIcon icon = new ImageIcon();
			icon.setImage(device.getIcon(11));
			JOptionPane.showMessageDialog(null, icon);

			//Input
			HashMap inputs = new HashMap();
			inputs.put("acceleration.x", 0.0);
			inputs.put("acceleration.y", 0.0);
			inputs.put("acceleration.z", 9.8);
			device.input(inputs);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
