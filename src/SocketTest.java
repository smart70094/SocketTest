import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SocketTest {
	private static Thread th_close; // �����
	private static int serverport = 12345;
	private static ServerSocket serverSocket; // ���A�ݪ�Socket
	private static Map<String,Socket> map=new HashMap<String,Socket>();
	private static RegisterRequestDB db=new RegisterRequestDB();
	
	
	
	public static void main(String[] args) {
		try {
			serverSocket = new ServerSocket(serverport); // �Ұ�Server�}��Port���f
			System.out.println("Server�}�l����");
			th_close = new Thread(Judge_Close); // �ᤩ������u�@(�P�_socketlist�����S���Ȥ�ݺ����_�u)
			th_close.start(); // ��������}�l����
			// ��Server�B�@����
			while (!serverSocket.isClosed()) {
				// �I�s���ݱ����Ȥ�ݳs��
				waitNewSocket();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Runnable Judge_Close = new Runnable() { // ��������C���P�_�@��SocketList���O�_���Ȥ�ݱj���_�u
		@Override
		public void run() {// �b��������O����wifi���_�u�ʧ@
			// TODO Auto-generated method stub
			try {
				while (true) {
					Thread.sleep(2000); // �C������@��
					synchronized(this) {
						for (Map.Entry<String, Socket> set : map.entrySet()) {
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

	private static Boolean isServerClose(Socket socket) { // �P�_�s�u�O�_���_
		try {
			socket.sendUrgentData(0); // �o�e�@�Ӧr�`�����ƾ�,�q�{���p�U�O�S���}�Һ��ƾڳB�z,���v�T���`�s�u
			return false; // �p���`�h�^��false
		} catch (Exception e) {
			return true; // �p�s�u���_�h�^��true
		}
	}

	// ���ݱ����Ȥ�ݳs��
	public static void waitNewSocket() {
		try {
			Socket socket = serverSocket.accept();
			// �I�s�гy�s���ϥΪ�
			createNewThread(socket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// �гy�s���ϥΪ�
	public static void createNewThread(final Socket socket) {
		// �H�s��������Ӱ���
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // �W�[�s���ϥΪ�
                    
                    //���o������X��y
                    OutputStream out=socket.getOutputStream(); 
                    // ���o������J��y
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String tmp,result = null;
                    
                    // ��Socket�w�s���ɳs�����
                    while (socket.isConnected()) {
                              //�ŧi�@�ӽw��,�qbr��yŪ����
                        // �p�G���O�ŰT��
                        if(br.ready()){
                            //�q�Ȥ�ݨ��o�ȫᰵ���,�i�ϥ�switch�����P�ʧ@���B�z�P�^��
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
        // �Ұʰ����
        t.start();
    }
	public static String registerCheer(String originator,String subject,String msg,Socket socket) {
		String result=null;
		try {
			if(!map.containsKey(originator)) {
				result=msg.trim()+"\n";
				return result;
			}else {
				db.setParams(originator, "request_cheer", msg, subject, "createRegisterRequest.php");
				db.start();
				return "\n";
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
				//trans request to app
				String params=tid+"\n";
				//System.out.println(params);
				return params;
			}
		}catch(Exception e) {
			System.out.println(e.toString());
		}
		return "";
	}
}
