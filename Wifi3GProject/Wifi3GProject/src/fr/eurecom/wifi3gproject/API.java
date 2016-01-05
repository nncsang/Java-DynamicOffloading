package fr.eurecom.wifi3gproject;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;


public final class API {
	
	private static String ipWifi = null;
	private static String ip3G = null;
	private static String gatewayWifi = null;
	private static String gateway3G = null;
	private static String interfaceWifi = null;
	private static String interface3G = null;
	
	
	public static int runScript(String script_name, String script,StringBuilder res, long timeout, boolean asroot) {
		final File file = new File(MainActivity.context.getCacheDir(), script_name);
		final ScriptRunner runner = new ScriptRunner(file, script, res, asroot);
		runner.start();
		try {
			if (timeout > 0) {
				runner.join(timeout);
			} else {
				runner.join();
			}
			if (runner.isAlive()) {
				// Timed-out
				runner.interrupt();
				runner.join(150);
				runner.destroy();
				runner.join(50);
			}
		} catch (InterruptedException ex) {
		}
		return runner.exitcode;
	}

	private static final class ScriptRunner extends Thread {
		private final File file;
		private final String script;
		private final StringBuilder res;
		private final boolean asroot;
		public int exitcode = -1;
		private Process exec;

		/**
		 * Creates a new script runner.
		 * 
		 * @param file
		 *            temporary script file
		 * @param script
		 *            script to run
		 * @param res
		 *            response output
		 * @param asroot
		 *            if true, executes the script as root
		 */
		public ScriptRunner(File file, String script, StringBuilder res, boolean asroot) {
			this.file = file;
			this.script = script;
			this.res = res;
			this.asroot = asroot;
		}

		@Override
		public void run() {
			try {
				file.createNewFile();
				final String abspath = file.getAbsolutePath();
				// make sure we have execution permission on the script file
				Runtime.getRuntime().exec("chmod 777 " + abspath).waitFor();
				// Write the script to be executed
				final OutputStreamWriter out = new OutputStreamWriter(
						new FileOutputStream(file));
				if (new File("/system/bin/sh").exists()) {
					out.write("#!/system/bin/sh\n");
				}
				out.write(script);
				if (!script.endsWith("\n"))
					out.write("\n");
				out.write("exit\n");
				out.flush();
				out.close();
				if (this.asroot) {
					// Create the "su" request to run the script
					exec = Runtime.getRuntime().exec("su -c " + abspath);
				} else {
					// Create the "sh" request to run the script
					exec = Runtime.getRuntime().exec("sh " + abspath);
				}
				InputStreamReader r = new InputStreamReader(
						exec.getInputStream());
				final char buf[] = new char[1024];
				int read = 0;
				// Consume the "stdout"
				while ((read = r.read(buf)) != -1) {
					if (res != null)
						res.append(buf, 0, read);
				}
				// Consume the "stderr"
				r = new InputStreamReader(exec.getErrorStream());
				read = 0;
				while ((read = r.read(buf)) != -1) {
					if (res != null)
						res.append(buf, 0, read);
				}
				// get the process exit code
				if (exec != null)
					this.exitcode = exec.waitFor();
			} catch (InterruptedException ex) {
				if (res != null)
					res.append("\nOperation timed-out");
			} catch (Exception ex) {
				if (res != null)
					res.append("\n" + ex);
			} finally {
				destroy();
			}
		}

		/**
		 * Destroy this script runner
		 */
		public synchronized void destroy() {
			if (exec != null)
				exec.destroy();
			exec = null;
		}
	}
	
	public static boolean setIPDualMode() throws IOException {
	
		ipWifi = null;
		ip3G = null;
		interfaceWifi = null;
		interface3G = null;
	
		try {
			for (NetworkInterface intf : Collections.list(NetworkInterface
					.getNetworkInterfaces())) {
				for (InetAddress addr : Collections.list(intf
						.getInetAddresses())) {
					if (!addr.isLoopbackAddress())

					{
						if (intf.getName().startsWith("wlan")
								|| intf.getName().startsWith("tiwlan")
								|| intf.getName().startsWith("ra")) {

							interfaceWifi = intf.getName();
							ipWifi = addr.getHostAddress().toString();
						}

						if (intf.getName().startsWith("rmnet")
								|| intf.getName().startsWith("pdp")
								|| intf.getName().startsWith("uwbr")
								|| intf.getName().startsWith("wimax")
								|| intf.getName().startsWith("vsnet")
								|| intf.getName().startsWith("ccmni")
								|| intf.getName().startsWith("usb")
								|| intf.getName().startsWith("eth")) {

							interface3G = intf.getName();
							ip3G = addr.getHostAddress().toString();
						}
					}
				}
			}
			
		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}
		
		if (interface3G == null || interfaceWifi == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean SetIPGateways() throws IOException{
	
		gateway3G = null;
		gatewayWifi = null;
		
		gateway3G = ExecRootCommand("traceroute -i "+ interface3G +" -m 1 www.google.com \n");
		gatewayWifi = ExecRootCommand("traceroute -i "+ interfaceWifi + " -m 1 www.google.com \n");
		
		if(gateway3G == null || gatewayWifi==null){
			return false;	
		}else{
			return true;
		}
		
	}
		
	public static String ExecRootCommand(String cmd){
		
		String Gateway = null;
		
		try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            stdin.write(cmd.getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();

            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
            	if (Constants.debug) System.out.println("TRACE: " + line);
                if(line.matches("^(\\s*)1.*")){
                	String []a1 = line.split("\\(");
                	String []a2 = a1[1].trim().split("\\)");
                	Gateway=a2[0].trim();
                }
                
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
            	if (Constants.debug) System.out.println("TRACE: " + line);
            	if (Constants.debug) System.out.println("[Error]" + line);
            }
            br.close();

			process.waitFor();
			process.destroy();

        } catch (Exception ex) {
        }
		
		return Gateway;
	}
	
	public static String GetWifiIP(){
		return ipWifi;
	}
	
	public static String Get3GIP(){
		return ip3G;
	}
	
	public static String GetGatewayWifiIP(){
		return gatewayWifi;
	}
	
	public static String GetGateway3GIP(){
		return gateway3G;
	}
	
	public static String GetInterfaceWifi(){
		return interfaceWifi;
	}
	
	public static String GetInterface3G(){
		return interface3G;
	}
}