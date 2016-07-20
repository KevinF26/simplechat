import java.io.*;
import java.net.*;

public class post {
	public static void main(String args[]) throws Exception {
		if((args.length+1)%2!=0)
		{
			System.out.println("no groupname entered");
			System.exit(1);
		}
		String line;
		String hostname="localhost";
		int port=4905;
		for(int i=0;i<args.length;i++)
		{
			if(args[i].equals("-h"))
			{
				hostname=args[i+1];
				i++;
				continue;
			}
			if(args[i].equals("-p"))
			{
				port=Integer.parseInt(args[i+1]);
			}
		}
		BufferedReader userdata = new BufferedReader(new InputStreamReader(System.in));

		Socket sock=null;
		try{
			sock=new Socket(hostname,port);
		}
		catch (ConnectException e){

			System.out.println("connection refused by server");
			System.exit(1);
		}
		System.out.println("connected!\n");
		DataOutputStream toServer = new DataOutputStream(sock.getOutputStream());
		BufferedReader fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		toServer.writeBytes("post "+args[args.length-1]+"\n");
	
		String ack=fromServer.readLine();
		if(ack.contains("error"))
		{
			System.out.println(ack);
			System.exit(1);
		}
		toServer.writeBytes(System.getProperty("user.name")+"\n");
		ack=fromServer.readLine();
                if(ack.contains("error"))
                {
                        System.out.println(ack);
                        System.exit(1);
                }

		while ((line = userdata.readLine()) != null) {
			toServer.writeBytes(line + '\n');	// send the line to the server
		}
		sock.close();	// we're done with the connection
	}
}
