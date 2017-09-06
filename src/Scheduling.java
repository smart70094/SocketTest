import java.awt.List;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TimerTask;

public class Scheduling extends TimerTask {
	
	ParticipatorDB participatorDB=new ParticipatorDB();
	MissionDB missionDB=new MissionDB();
	
	public void run() {
		
		
		reSet();
	}
	public void reSet() {
			Map<String,String> map =participatorDB.read();
			Iterator<Entry<String, String>> it=map.entrySet().iterator();
			
			String arr[] = null,t_arr[] = null;
			int result[] = null;
			while(it.hasNext()) {
				//測試code
				/*String test="5";
				t_arr=test.split(",");
				for(int i=0;i<=1000;i++) {
					match(0,t_arr);
					for(int j=0;j<result.length;j++) {
						System.out.print(result[j]+",");
					}
					System.out.println();
				}*/
				
				Map.Entry<String,String> set=(Map.Entry<String, String>) it.next();
				String tid=set.getKey().trim();
				
				
				t_arr=set.getValue().split(",");
				//去除index 0是null
				if(t_arr.length==2) {
					missionDB.writeCollaborator(tid,t_arr[1],t_arr[1]);
				}else {
					arr=new String[t_arr.length-1];
					for(int i=1;i<=t_arr.length-1;i++) arr[i-1]=t_arr[i];
					result=match(arr);
					for(int i=0;i<result.length;i++) 
						missionDB.writeCollaborator(tid,arr[i], Integer.toString(result[i]).trim());
				}
			}
	}
	
	public int[] match(String arr[]) {
		int size=arr.length;
		int result[]=new int[size];
		Random ran=new SecureRandom();
		int pos,target,m=size-1,t;
		ArrayList<Integer> list=new ArrayList<Integer>();
		for(int i=0;i<size;i++)  list.add(Integer.parseInt(arr[i]));
		
		for(int i=0;i<size;i++) {
			target=Integer.parseInt(arr[i]);
			while(true) {		
				pos=ran.nextInt((m - 0) + 1) + 0;
				if(target==list.get(pos)) {
					if(m==0) {
						m=size-2;
						pos=ran.nextInt((m - 0) + 1) + 0;
						t=result[pos];
						result[pos]=list.get(0);
						result[i]=t;
						break;
					}
					continue;
				}else {
					result[i]=list.get(pos);
					list.remove(pos);
					m--;
					break;
				}
			}
		}
		return result;
	}

}
