package mx.ecosur.multigame.manantiales.enum {

    import mx.ecosur.multigame.enum.GameEvent;

    public class ManantialesEvent extends GameEvent {

        /* Manantiales Specific Game Events */

        public static const BEGIN:String = GameEvent.BEGIN;
        public static const END:String = GameEvent.END;
        public static const CHAT:String = GameEvent.CHAT;
        public static const MOVE_COMPLETE:String = GameEvent.MOVE_COMPLETE;
        public static const PLAYER_CHANGE:String = GameEvent.PLAYER_CHANGE;
        public static const CONDITION_RAISED:String = GameEvent.CONDITION_RAISED;
        public static const CONDITION_RESOLVED:String = GameEvent.CONDITION_RESOLVED;
        public static const CONDITION_TRIGGERED:String = GameEvent.CONDITION_TRIGGERED;
        public static const STATE_CHANGE:String = GameEvent.STATE_CHANGE;
        public static const GAME_CHANGE:String = GameEvent.GAME_CHANGE;

        /* Manantiales Specific */

        public static const SUGGESTION_APPLIED:String = "SUGGESTION_APPLIED";
        public static const SUGGESTION_EVALUATED:String = "SUGGESTION_EVALUATED";
    }
}