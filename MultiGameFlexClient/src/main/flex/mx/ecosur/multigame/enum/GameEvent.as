package mx.ecosur.multigame.enum {
	
	/**
	 * Enumeration that represents the different
	 * server side game events that can be sent.
	 */
	public class GameEvent {
		
		public static const BEGIN:String = "BEGIN";
		public static const END:String = "END";
		public static const MOVE_COMPLETE:String = "MOVE_COMPLETE";
		public static const QUALIFY_MOVE:String = "QUALIFY_MOVE";
		public static const CHAT:String = "CHAT";
		public static const PLAYER_CHANGE:String = "PLAYER_CHANGE";

	}
}