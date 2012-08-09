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
     * Enumeration that represents the different
     * server side game events that can be sent.
     */
    public class GameEvent {

        public static const CREATE:String = "CREATE";
        public static const BEGIN:String = "BEGIN";
        public static const END:String = "END";
        public static const CHAT:String = "CHAT";
        public static const MOVE_COMPLETE:String = "MOVE_COMPLETE";
        public static const PLAYER_CHANGE:String = "PLAYER_CHANGE";
        public static const PLAYER_JOIN:String = "PLAYER_JOIN";
        public static const CONDITION_RAISED:String = "CONDITION_RAISED";
        public static const CONDITION_RESOLVED:String = "CONDITION_RESOLVED";
        public static const CONDITION_TRIGGERED:String = "CONDITION_TRIGGERED";
        public static const STATE_CHANGE:String = "STATE_CHANGE";
        public static const GAME_CHANGE:String = "GAME_CHANGE";
        public static const EXPIRED:String = "EXPIRED";
        public static const DESTROY:String = "DESTROY";
    }
}
