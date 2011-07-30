package drawBig;

import java.util.GregorianCalendar;

public class ChatMessage {
	public String Message;
	public GregorianCalendar Time;

	public ChatMessage(String m, GregorianCalendar t) {
		Message = m;
		Time = t;
	}
}