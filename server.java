import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.Timestamp;

class messageNode
{
	String username;
	String IP;
	String Timestamp;
	String message;
	
	public messageNode(String username, String IP, String Timestamp, String message)
	{
		this.username=username;
		this.IP=IP;
		this.Timestamp=Timestamp;
		this.message=message;
	}
}

public class server implements Runnable {
	Socket conn;

	server(Socket sock) {
		this.conn = sock;
	}
	static HashMap<String,LinkedList> map;
	public static void main(String args[]) throws Exception {
		map=new HashMap<String,LinkedList>();
		int port=4905;
                if(args.length>1)
                {
                        if(args[0].equals("-p"))
                        {
                                port=Integer.parseInt(args[1]);
                        }
                }

		System.out.println("listening on port " + port);
		ServerSocket svc = new ServerSocket(port, 5);

		for (;;) {
			Socket conn = svc.accept();	// get a connection from a client
			System.out.println("got a new connection from " 
				+ conn.getRemoteSocketAddress());

			new Thread(new server(conn)).start();
		}
	}
	public void handlePost(BufferedReader fromClient,DataOutputStream toClient,String groupname,String ip)
	{
		String line="";
		String fullMessage="";
		String username="";
		LinkedList<messageNode> l;
		if(!map.containsKey(groupname))
		{
			l=new LinkedList<messageNode>();
			map.put(groupname,l);
		}
		else
		{
			l=map.get(groupname);
		}
		try {
			username=fromClient.readLine();
			if(username!=null)
			{
				toClient.writeBytes("ok\n");
			}
			do{
				fullMessage+=line+"\n";	
				line=fromClient.readLine();
				if(line==null)
				{
					break;
				}
                	}while(line!=null);
		}
		catch(IOException e){
			System.out.println(e);
		}
		Date date=new Date();
		long time=date.getTime();
		Timestamp ts=new Timestamp(time);
		String timestring=""+ts;
		messageNode n=new messageNode(username,ip,timestring,fullMessage);
		l.add(n);

	}
	public void handleGet(BufferedReader fromClient, DataOutputStream toClient,String groupname)
	{
		try{
		LinkedList<messageNode> l=map.get(groupname);
                        if(l!=null){
			toClient.writeBytes(l.size()+" messages\n");
                        ListIterator<messageNode> it=l.listIterator();
                        while(it.hasNext()){
				messageNode n=it.next();
				toClient.writeBytes("From "+n.username+" /"+n.IP+" at "+n.Timestamp+"\n");
                                toClient.writeBytes((n.message)+"\n");
                        }}
			else{
				toClient.writeBytes("This group name has no messages\n");
			}
		}
		catch(IOException e) {
			System.out.println("error");
			System.exit(1);
		}
	}
	public void run() {
		try {
			BufferedReader fromClient = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			DataOutputStream toClient = new DataOutputStream(conn.getOutputStream());
			String line;
			String ip=conn.getRemoteSocketAddress().toString();
			line=fromClient.readLine();
			String groupname=line.substring(line.indexOf(' ')+1);
			if(line.contains("post") && groupname!=null)
			{
				toClient.writeBytes("ok\n");
				handlePost(fromClient,toClient,groupname,ip);
			}
			else if (line.contains("get")&& groupname!=null)
			{
				toClient.writeBytes("ok\n");
				handleGet(fromClient,toClient,groupname);
			}
			else
			{
				toClient.writeBytes("error malformed post or get request\n");
			}
			System.out.println("closing the connection\n");
			conn.close();		// close connection and exit the thread

		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
