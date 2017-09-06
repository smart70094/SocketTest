import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class SocketTest {
	private static Thread th_close; // 執行緒
	private static int serverport = 12345;
	private static ServerSocket serverSocket; // 伺服端的Socket
	private static Map<String,Socket> map=new HashMap<String,Socket>();
	private static RegisterRequestDB db=new RegisterRequestDB();
	static String  ip="192.168.0.102";
	public static String localhost="http://"+ip+"/TeamGoGoal/";
	private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;
	
	public static void main(String[] args) {
		try {
			/*Timer timer=new Timer();
			
			Calendar calendar = Calendar.getInstance();  
	        calendar.set(Calendar.HOUR_OF_DAY, 14); //凌晨1点  
	        calendar.set(Calendar.MINUTE, 59);  
	        calendar.set(Calendar.SECOND, 0);  
	        Date date=calendar.getTime(); //第一次执行定时任务的时间   
	        timer.schedule(new Scheduling(),date,PERIOD_DAY);    
			new Scheduling().reSet();*/
	        //new Scheduling().reSet();
			
			
			serverSocket = new ServerSocket(serverport); // 啟動Server開啟Port接口
			System.out.println("Server開始執行");
			th_close = new Thread(Judge_Close); // 賦予執行緒工作(判斷socketlist內有沒有客戶端網路斷線)
			th_close.start(); // 讓執行緒開始執行
			// 當Server運作中時
			while (!serverSocket.isClosed()) {
				// 呼叫等待接受客戶端連接
				waitNewSocket();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		}
	}

	private static Runnable Judge_Close = new Runnable() { // 讓執行緒每兩秒判斷一次SocketList內是否有客戶端強制斷線
		@Override
		public void run() {// 在此抓取的是關閉wifi等斷線動作
			// TODO Auto-generated method stub
			try {
				while (true) {
					Thread.sleep(2000); // 每兩秒執行一輪
					for (Map.Entry<String, Socket> set : map.entrySet()) {
						synchronized(this) {
							if (isServerClose(set.getValue())) {
								set.getValue().close();
								map.remove(set.getKey());
								System.out.println(set.getKey()+":is disconnect.");
							}
							
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private static Boolean isServerClose(Socket socket) { // 判斷連線是否中斷
		try {
			socket.sendUrgentData(0); // 發送一個字節的緊急數據,默認情況下是沒有開啟緊急數據處理,不影響正常連線
			return false; // 如正常則回傳false
		} catch (Exception e) {
			return true; // 如連線中斷則回傳true
		}
	}

	// 等待接受客戶端連接
	public static void waitNewSocket() {
		try {
			Socket socket = serverSocket.accept();
			// 呼叫創造新的使用者
			createNewThread(socket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 創造新的使用者
	public static void createNewThread(final Socket socket) {
		// 以新的執行緒來執行
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 增加新的使用者
                    
                    //取得網路輸出串流
                    OutputStream out=socket.getOutputStream(); 
                    // 取得網路輸入串流
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String tmp,result = null;
                    
                    // 當Socket已連接時連續執行
                    while (socket.isConnected()) {
                              //宣告一個緩衝,從br串流讀取值
                        // 如果不是空訊息
                        if(br.ready()){
                            //從客戶端取得值後做拆解,可使用switch做不同動作的處理與回應
                        	tmp = br.readLine();  
                        	System.out.println(tmp);
                        	String arr[]=tmp.split(",");
                        	String cmd=arr[0];
                        	//System.out.println(tmp);
                        	switch(cmd) {
                        		case "register_online":
                        			result=registerOnline(arr[1],socket);
                        			break;
                        		case "register_request":
                        			result=registerRequest(arr[1],arr[2],arr[3],socket);
                        			break;
                        		case "register_cheer":
                        			result=registerCheer(arr[1],arr[2],arr[3],socket);
                        	
                        		
                        	}
                        	if(result!=null) {
                        		result=result.trim();
                        		result+="\n";
                            	out.write(result.getBytes());
                            	out.flush();
                            	result=null;
                        	}
                        }
                        
                    	
                	}     
                } catch (Exception e) {
                    e.printStackTrace();
                }  
            }
        });
        // 啟動執行緒
        t.start();
    }
	public static String registerCheer(String originator,String subject,String msg,Socket socket) {
		String result=null;
		try {
			if(map.containsKey(subject)) {
				result=msg.trim()+"\n";
				writer(subject,result);
			}else {
				db.setParams(originator, "request_cheer", msg, subject, "createRegisterRequest.php");
				db.start();
			}
			
		}catch(Exception e) {
			System.out.println(e.toString());
		}
		return "\n";
	}
	public static String registerOnline(String originator,Socket socket) {
		String result=null;
		try {
			if(!map.containsKey(originator)) {
				map.put(originator, socket);
				System.out.println(originator+": is online");
		
			}
		}catch(Exception e) {
			System.out.println(e.toString());
		}
		return "\n";
	}
	public static String registerRequest(String originator,String subject,String tid,Socket socket) {
		try {
			if(map.containsKey(subject)) {
				String params=tid+"\n";
				writer(subject,params);
			}
		}catch(Exception e) {
			System.out.println(e.toString());
		}
		return "\n";A
	}
	public static void writer(String subject,String msg) throws IOException {
		Socket socket=map.get(subject);
		OutputStream out=socket.getOutputStream(); 
    	out.write(msg.getBytes());
    	out.flush();
    	
	}
}
