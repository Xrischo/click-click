import java.net.*;
import java.io.*;

public class EmailRead {
	
	private void fetch() throws Exception {
		String website = "https://www.ecs.soton.ac.uk/people/" + getID();
		System.out.println("Name: " + readURL(website));
	}
	
	private String getID() {
		StringBuilder email = new StringBuilder();
		int nextLetter;
		
		try (InputStreamReader in = new InputStreamReader(System.in)) {
			// 13 is LF (pressing enter), 64 is @
			while ((nextLetter = in.read()) != 13 && nextLetter != 64) {
				email.append((char) nextLetter);
			}
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return email.toString();
	}
	
	private String readURL(String url) throws Exception {
		URL site = new URL(url);
		URLConnection connection = site.openConnection();
		BufferedReader in = new BufferedReader(
								new InputStreamReader(
										connection.getInputStream()));
		
		String input = "";
		int count = 0;
		
		while((input = in.readLine()) != null && count != 7) {
			count++;
		}
		
		in.close();
		
		return input.substring(input.indexOf('>')+1, input.indexOf('|'));
	}
	
	public static void main(String[] args) throws Exception {
		EmailRead info = new EmailRead();
		info.fetch();
	}
}
