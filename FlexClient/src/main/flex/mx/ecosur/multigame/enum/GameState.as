/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.enum {
	
	/**
	 * Enumeration that represents the different game status.
	 */
	public class GameState {
		
		public static const WAITING:String = "WAITING";
		public static const BEGIN:String = "BEGIN";
		public static const PLAY:String = "PLAY";
		public static const ENDED:String = "ENDED";
	
		public static function getDescription(state:String):String{
			switch (state){
				case WAITING:
					return "Waiting for more players";
				break;
				case BEGIN:
				case PLAY:
					return "In progress";
				break;
				case ENDED:
					return "Finished";
				break;
			}
			return "";
		}
	}
}