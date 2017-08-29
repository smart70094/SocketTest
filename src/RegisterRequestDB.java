import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterRequestDB {
	String originator,subject;
	String localhost="http://169.254.68.146/TeamGoGoal/";
	//String localhost="http://192.168.50.87/android/";
	String params="",php="";
	final String table="registerrequest";
	public void setParams(String originator,String php) {
		params="table=registerrequest & originator="+originator;
		this.php=php;
	}

	public void setParams(String originator,String cmd,String cmdContext,String subject,String php) {
		this.params="table=registerrequest & originator="+originator+" & cmd="+cmd+" & cmdContext="+cmdContext+" & subject="+subject;
		this.php=php;
	}
	public String start() {
		return viaParams(params,php);
	}
	public String viaParams(String urlParameters,String php){
        byte[] postData = new byte[0];
        try {
            postData = urlParameters.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int postDataLength = postData.length;
        String checkurl = localhost+php;
        
        StringBuilder sb=null;
        try {
            URL connectto = new URL(checkurl);
            HttpURLConnection conn = (HttpURLConnection) connectto.openConnection();
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "Accept-Charset", "UTF-8");
            conn.setRequestProperty( "Accept-Encoding", "UTF-8");
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects( false );
            conn.setDoOutput( true );

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
            wr.flush();
            wr.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            conn.disconnect();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        return sb.toString();
	}
}
